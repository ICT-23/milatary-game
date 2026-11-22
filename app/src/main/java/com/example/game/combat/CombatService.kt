package com.example.game.combat

/**
 * Section 8.2/8.11: the one place a trigger pull becomes damage.
 *
 * Order matters and is fixed:
 *   1. duplicate-event guard (a bullet is only ever processed once),
 *   2. target validity (a disabled target yields no reward),
 *   3. weapon readiness (no round, no shot),
 *   4. physical validation of the aim ray and the muzzle ray,
 *   5. protected / friendly-fire policy,
 *   6. the documented damage formula, applied by the target itself.
 *
 * Nothing here invents a second damage path: [DamageResolver] still owns the numbers.
 */

/** Which side a body belongs to, so friendly-fire policy can be evaluated. */
enum class Faction { OPERATOR, ALLY, HOSTILE, NON_COMBATANT }

/** Mission-level rules that change what a hit is allowed to do. */
data class CombatRules(
    val missionDifficultyMultiplier: Float = 1f,
    val friendlyFireEnabled: Boolean = false,
    val protectedEntityResponse: String = "PROTECTED_ENTITY_VIOLATION",
    val maxRangeMeters: Float = 400f
)

/** Terminal outcome of one trigger pull. */
enum class ShotOutcome {
    APPLIED,
    MISSED,
    BLOCKED,
    NO_ROUND,
    RELOADING,
    DUPLICATE_EVENT,
    TARGET_ALREADY_DOWN,
    PROTECTED_VIOLATION,
    FRIENDLY_FIRE_DENIED
}

/** One trigger pull, already described in world space. */
data class ShotRequest(
    val shotId: String,
    val aimOrigin: Vec3,
    val aimDirection: Vec3,
    val muzzleOrigin: Vec3,
    val bodies: List<BallisticBody>,
    val target: Damageable?,
    val baseDamage: Float,
    val effectiveRangeMeters: Float,
    val targetFaction: Faction = Faction.HOSTILE,
    val armourGrade: ArmourGrade = ArmourGrade.UNARMOURED,
    val armourPenetration: Float = 1f,
    val temporaryStatusMultiplier: Float = 1f,
    val bulletVelocityMetersPerSecond: Float = 880f
)

/** What the caller renders, logs and scores from. */
data class CombatEvent(
    val shotId: String,
    val outcome: ShotOutcome,
    val shot: ShotResolution? = null,
    val damage: DamageResolution? = null,
    val note: String = ""
) {
    val appliedDamage: Int get() = damage?.appliedDamage ?: 0

    /** Only a validated hostile hit that actually wounded can earn score. */
    val awardsScore: Boolean get() = outcome == ShotOutcome.APPLIED && appliedDamage > 0
}

/** Guards against the same bullet arriving twice from an animation and a physics callback. */
class ShotLedger {
    private val claimed = HashSet<String>()

    /** True the first time this shot id is seen, false for every repeat. */
    fun claim(shotId: String): Boolean = claimed.add(shotId)

    fun clear() = claimed.clear()
}

object CombatService {

    fun fire(
        request: ShotRequest,
        weapon: WeaponRuntime?,
        nowSeconds: Float,
        rules: CombatRules = CombatRules(),
        ledger: ShotLedger = ShotLedger()
    ): CombatEvent {
        if (!ledger.claim(request.shotId)) {
            return CombatEvent(
                shotId = request.shotId,
                outcome = ShotOutcome.DUPLICATE_EVENT,
                note = "Shot ${request.shotId} was already resolved"
            )
        }

        val target = request.target
            ?: return CombatEvent(request.shotId, ShotOutcome.MISSED, note = "No damageable body in the line of fire")

        if (target.isDestroyedOrDead) {
            return CombatEvent(
                shotId = request.shotId,
                outcome = ShotOutcome.TARGET_ALREADY_DOWN,
                note = "Target already disabled; no further reward"
            )
        }

        var spreadDegrees = 0f
        if (weapon != null) {
            val attempt = weapon.tryExecuteShot(nowSeconds)
            if (!attempt.fired) {
                val outcome = if (attempt.block == FireBlock.RELOADING) ShotOutcome.RELOADING else ShotOutcome.NO_ROUND
                return CombatEvent(
                    shotId = request.shotId,
                    outcome = outcome,
                    note = attempt.block?.displayName ?: "No round left the barrel"
                )
            }
            spreadDegrees = attempt.spreadDegrees
        }

        val shot = BallisticsEngine.resolve(
            aimOrigin = request.aimOrigin,
            aimDirection = request.aimDirection,
            muzzleOrigin = request.muzzleOrigin,
            bodies = request.bodies,
            maxRangeMeters = rules.maxRangeMeters,
            spreadDegrees = spreadDegrees
        )

        if (!shot.canApplyDamage) {
            val outcome = when (shot.rejectReason) {
                ShotRejectReason.BLOCKED_BY_OBSTRUCTION -> ShotOutcome.BLOCKED
                else -> ShotOutcome.MISSED
            }
            return CombatEvent(
                shotId = request.shotId,
                outcome = outcome,
                shot = shot,
                note = shot.rejectReason.name
            )
        }

        if (shot.hitZone.isProtected) {
            // Zero combat damage, plus the mission-rule response the campaign expects.
            return CombatEvent(
                shotId = request.shotId,
                outcome = ShotOutcome.PROTECTED_VIOLATION,
                shot = shot,
                damage = DamageResolution(
                    appliedDamage = 0,
                    zoneMultiplier = 0f,
                    falloff = DamageResolver.rangeFalloff(shot.distanceMeters, request.effectiveRangeMeters),
                    armorAbsorbed = 0,
                    protectedViolation = true
                ),
                note = rules.protectedEntityResponse
            )
        }

        val friendly = request.targetFaction == Faction.ALLY || request.targetFaction == Faction.NON_COMBATANT
        if (friendly && !rules.friendlyFireEnabled) {
            return CombatEvent(
                shotId = request.shotId,
                outcome = ShotOutcome.FRIENDLY_FIRE_DENIED,
                shot = shot,
                note = "Teammate protected by mission rules; warning logged, no health lost"
            )
        }

        val payload = shot.toDamagePayload(
            rawDamage = request.baseDamage,
            armorPenetrationPower = request.armourPenetration,
            bulletVelocityMetersPerSecond = request.bulletVelocityMetersPerSecond,
            attackerId = "OPERATOR"
        ) ?: return CombatEvent(request.shotId, ShotOutcome.MISSED, shot = shot, note = "Payload refused")

        target.applyDamage(
            payload,
            DamageModifiers(
                armourGrade = request.armourGrade,
                missionDifficultyMultiplier = rules.missionDifficultyMultiplier,
                temporaryStatusMultiplier = request.temporaryStatusMultiplier
            )
        )

        val damage = (target as? DamageableCutout)?.lastResolution ?: DamageResolver.resolve(
            baseDamage = request.baseDamage,
            zone = shot.hitZone,
            distanceMeters = shot.distanceMeters,
            effectiveRangeMeters = request.effectiveRangeMeters,
            armorPenetration = request.armourPenetration,
            armourGrade = request.armourGrade,
            missionDifficultyMultiplier = rules.missionDifficultyMultiplier,
            temporaryStatusMultiplier = request.temporaryStatusMultiplier
        )

        return CombatEvent(
            shotId = request.shotId,
            outcome = ShotOutcome.APPLIED,
            shot = shot,
            damage = damage,
            note = "${shot.hitZone.displayName} at ${shot.distanceMeters.toInt()} m"
        )
    }
}
