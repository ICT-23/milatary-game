package com.example.game.valor.plan

import com.example.game.valor.balance.Difficulty
import com.example.game.valor.balance.FriendlyFirePolicy

/**
 * Section 5.3: one thing a bullet ray met, as reported by the physics step.
 * The samples arrive nearest-first; the rules decide what happens to them.
 */
data class RaySample(
    val distanceMeters: Float,
    val entityId: String?,
    val layer: PhysicsLayer,
    val team: Team? = null,
    val zone: HitZone = HitZone.TORSO,
    val alive: Boolean = true,
    val penetrable: Boolean = false,
    val thicknessMeters: Float = Float.MAX_VALUE
)

/** Section 5.3 "Processing a hit": the six things that can happen. */
enum class HitOutcome(val displayName: String) {
    NO_HIT("Missed everything"),
    DAMAGED("Damaged a hostile"),
    BLOCKED("Blocked by material"),
    IGNORED_DEAD("Hit a dead body"),
    FRIENDLY_BLOCKED("Friendly, no damage"),
    FRIENDLY_DAMAGE("Friendly, damage allowed")
}

/** Section 5.3 friendly-fire row, read straight off the difficulty policy. */
enum class FriendlyOutcome(val displayName: String) {
    NO_DAMAGE("No damage"),
    WARNING_ONLY("No damage plus warning UI"),
    DAMAGE("Damage"),
    DAMAGE_AND_FAIL_ON_DEATH("Damage, ally death fails the mission")
}

/** Section 5.3: the weapon facts a ray resolution needs. */
data class RayWeapon(
    val maxRangeMeters: Float,
    val penetrationPower: Float,
    val penetrationFalloff: Float
)

data class HitResult(
    val outcome: HitOutcome,
    val targetId: String? = null,
    val zone: HitZone = HitZone.TORSO,
    val damageMultiplier: Float = 1f,
    val friendly: FriendlyOutcome? = null,
    val impactLayer: PhysicsLayer? = null
) {
    val dealsDamage: Boolean
        get() = outcome == HitOutcome.DAMAGED || outcome == HitOutcome.FRIENDLY_DAMAGE
}

/**
 * Section 5.3: damage is dealt only when the bullet path physically intersects a
 * valid, alive, hostile hitbox. Being in the crosshair is not a hit.
 */
object HitDetection {

    val MATERIAL_LAYERS: Set<PhysicsLayer> = setOf(
        PhysicsLayer.ENVIRONMENT,
        PhysicsLayer.COVER,
        PhysicsLayer.GLASS,
        PhysicsLayer.DESTRUCTIBLE
    )

    const val ONE_TARGET_PER_RAY = true
    const val SHOTGUN_PELLETS_ARE_INDEPENDENT = true

    /** "Raycast from camera center first to find the aim point." */
    fun aimPoint(cameraOrigin: Vec3, cameraDirection: Vec3, distanceMeters: Float): Vec3 =
        cameraOrigin + cameraDirection.normalized() * distanceMeters

    /** Then fire the real bullet "from the muzzle toward that aim point". */
    fun convergenceDirection(muzzle: Vec3, aimPoint: Vec3): Vec3 = (aimPoint - muzzle).normalized()

    fun aimDistance(muzzle: Vec3, aimPoint: Vec3): Float = muzzle.distanceTo(aimPoint)

    /** "If the muzzle to aim point ray is blocked earlier, that object takes the hit." */
    fun blockedBeforeAimPoint(firstBlockingDistance: Float, aimPointDistance: Float): Boolean =
        firstBlockingDistance < aimPointDistance

    /** Section 5.3 friendly-fire row: OFF for AI, zero damage to allies on Normal. */
    fun friendlyRule(policy: FriendlyFirePolicy, shooterTeam: Team): FriendlyOutcome = when (policy) {
        FriendlyFirePolicy.OFF -> FriendlyOutcome.NO_DAMAGE
        FriendlyFirePolicy.OFF_FOR_PLAYER_TO_ALLY ->
            if (shooterTeam.playerSide) FriendlyOutcome.WARNING_ONLY else FriendlyOutcome.NO_DAMAGE
        FriendlyFirePolicy.ON -> FriendlyOutcome.DAMAGE
        FriendlyFirePolicy.ON_ALLY_DEATH_FAILS -> FriendlyOutcome.DAMAGE_AND_FAIL_ON_DEATH
    }

    fun friendlyRule(difficulty: Difficulty, shooterTeam: Team): FriendlyOutcome =
        friendlyRule(difficulty.friendlyFire, shooterTeam)

    /**
     * Resolves one bullet against the ordered samples of its path. The first
     * sample that stops the bullet decides the outcome, so one ray can never
     * damage two targets unless the weapon penetrates.
     */
    fun resolve(
        shooterId: String?,
        shooterTeam: Team,
        samples: List<RaySample>,
        weapon: RayWeapon,
        policy: FriendlyFirePolicy
    ): HitResult {
        var falloff = 1f
        for (sample in samples.sortedBy { it.distanceMeters }) {
            if (sample.distanceMeters > weapon.maxRangeMeters) return HitResult(HitOutcome.NO_HIT)
            if (!BulletMask.accepts(sample.layer)) continue
            if (sample.layer in MATERIAL_LAYERS) {
                val thinEnough = sample.penetrable && weapon.penetrationPower > sample.thicknessMeters
                if (thinEnough) {
                    falloff *= weapon.penetrationFalloff
                    continue
                }
                return HitResult(HitOutcome.BLOCKED, impactLayer = sample.layer)
            }
            val targetId = sample.entityId ?: continue
            if (targetId == shooterId) continue
            if (!sample.alive) return HitResult(HitOutcome.IGNORED_DEAD, targetId, sample.zone)
            val targetTeam = sample.team ?: Team.NEUTRAL
            if (Team.sameSide(shooterTeam, targetTeam) && targetTeam != Team.NEUTRAL) {
                val rule = friendlyRule(policy, shooterTeam)
                return when (rule) {
                    FriendlyOutcome.NO_DAMAGE, FriendlyOutcome.WARNING_ONLY ->
                        HitResult(HitOutcome.FRIENDLY_BLOCKED, targetId, sample.zone, friendly = rule)
                    FriendlyOutcome.DAMAGE, FriendlyOutcome.DAMAGE_AND_FAIL_ON_DEATH ->
                        HitResult(
                            HitOutcome.FRIENDLY_DAMAGE, targetId, sample.zone,
                            damageMultiplier = falloff, friendly = rule
                        )
                }
            }
            return HitResult(HitOutcome.DAMAGED, targetId, sample.zone, damageMultiplier = falloff)
        }
        return HitResult(HitOutcome.NO_HIT)
    }

    /** "Each pellet is an independent ray; damage per pellet." */
    fun resolveVolley(
        shooterId: String?,
        shooterTeam: Team,
        pelletPaths: List<List<RaySample>>,
        weapon: RayWeapon,
        policy: FriendlyFirePolicy
    ): List<HitResult> = pelletPaths.map { resolve(shooterId, shooterTeam, it, weapon, policy) }
}
