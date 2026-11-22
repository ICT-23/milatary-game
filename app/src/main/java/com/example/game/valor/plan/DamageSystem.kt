package com.example.game.valor.plan

import com.example.game.valor.balance.ArmorMath
import com.example.game.valor.balance.DamageMath
import kotlin.math.max

/**
 * Section 5.4: one damage application, before any multiplier. Ranges and
 * multipliers come from weapon / ammo / difficulty data, never from this file.
 */
data class DamageRequest(
    val baseDamage: Float,
    val zone: HitZone,
    val distanceMeters: Float,
    val weaponRangeMeters: Float,
    val minDamageMult: Float,
    val falloffStartMeters: Float = 0f,
    val ammoTypeMultiplier: Float = 1f,
    val difficultyMultiplier: Float = 1f,
    val targetDamageReduction: Float = 0f,
    val armorAbsorbRatio: Float = DamageSystem.ARMOR_ABSORB_EXAMPLE,
    val armorPierce: Float = 0f,
    val penetrationMultiplier: Float = 1f,
    val type: DamageType = DamageType.BULLET
)

/** Section 5.4: the result, including whether health actually fell. */
data class DamageOutcome(
    val applied: Boolean,
    val finalDamage: Float,
    val healthLost: Float,
    val armorAbsorbed: Float,
    val armorRemaining: Float,
    val killed: Boolean,
    val ignoredBecause: String? = null
)

/**
 * Section 5.4: the plan's helmet line is `max(1, 2.5 - tier * 0.5)` while the
 * tuned balance table ends at 1.2. Section 2.4 keeps the tuned table live and
 * records the disagreement here instead of silently overwriting either one.
 */
object HelmetAdapter {

    const val BASELINE = 2.5f
    const val STEP_PER_TIER = 0.5f
    const val FLOOR = 1f

    fun planMultiplier(helmetTier: Int?): Float =
        max(FLOOR, BASELINE - STEP_PER_TIER * (helmetTier ?: 0))

    /** The shipping value, read from the single source in `balance`. */
    fun shippingMultiplier(helmetTier: Int?): Float = ArmorMath.headMultiplier(helmetTier)

    fun agreesAt(helmetTier: Int): Boolean =
        planMultiplier(helmetTier) == shippingMultiplier(helmetTier)

    val DIVERGENT_TIERS: List<Int> = listOf(0, 1, 2, 3).filter { !agreesAt(it) }
}

/** Section 5.4: the computed hit, including the armour share it consumed. */
data class DamageComputation(val finalDamage: Float, val armorAbsorbed: Float)

/**
 * Section 5.4: the single place damage is computed and applied. Nothing else in
 * the build may subtract health, which is what keeps the numbers honest.
 */
object DamageSystem {

    const val ARMOR_ABSORB_EXAMPLE = 0.6f
    const val HEALTH_NEVER_BELOW_ZERO = true
    const val HEALTH_NEVER_ABOVE_MAX = true

    /**
     * "1.0 until falloffStart, linear to minDamageMult at maxRange." With a zero
     * start this equals `DamageMath.damageAtRange`, so the tuned falloff and the
     * plan formula stay one implementation.
     */
    fun falloffMultiplier(
        distanceMeters: Float,
        falloffStartMeters: Float,
        maxRangeMeters: Float,
        minDamageMult: Float
    ): Float = when {
        distanceMeters <= falloffStartMeters -> 1f
        maxRangeMeters <= falloffStartMeters -> minDamageMult
        distanceMeters >= maxRangeMeters -> minDamageMult
        else -> {
            val span = maxRangeMeters - falloffStartMeters
            val travelled = distanceMeters - falloffStartMeters
            1f - (1f - minDamageMult) * (travelled / span)
        }
    }

    /** Section 5.4 formula, without touching the target. */
    fun computeDetailed(request: DamageRequest, target: Combatant): DamageComputation {
        var final = request.baseDamage
        final *= if (request.zone == HitZone.HEAD) {
            ArmorMath.headMultiplier(target.helmetTier)
        } else {
            request.zone.multiplier
        }
        final *= falloffMultiplier(
            request.distanceMeters,
            request.falloffStartMeters,
            request.weaponRangeMeters,
            request.minDamageMult
        )
        final *= request.ammoTypeMultiplier
        final *= request.difficultyMultiplier
        final *= 1f - request.targetDamageReduction.coerceIn(0f, 1f)
        final *= request.penetrationMultiplier.coerceAtLeast(0f)
        var absorbed = 0f
        if (armorApplies(request.zone, target)) {
            absorbed = ArmorMath.absorbedDamage(
                final.coerceAtLeast(0f), target.armorPoints, request.armorAbsorbRatio
            )
            final -= absorbed * (1f - request.armorPierce.coerceIn(0f, 1f))
        }
        return DamageComputation(final.coerceAtLeast(0f), absorbed)
    }

    fun compute(request: DamageRequest, target: Combatant): Float =
        computeDetailed(request, target).finalDamage

    /** "zone != Head or helmet absent": armour only covers an unprotected head. */
    fun armorApplies(zone: HitZone, target: Combatant): Boolean =
        target.armorPoints > 0f && (zone != HitZone.HEAD || target.helmetTier == null)

    /**
     * Applies one hit. Refused hits cost nothing and say why; an accepted hit
     * publishes `OnDamageDealt` once and `OnEntityKilled` once.
     */
    fun apply(
        request: DamageRequest,
        target: Combatant,
        sourceId: String?,
        sourceTeam: Team,
        weaponId: String? = null,
        missionPlaying: Boolean = true,
        nowSeconds: Float = 0f,
        scriptedBeat: Boolean = false,
        bus: EventBus? = null,
        log: DebugLog? = null
    ): DamageOutcome {
        fun refuse(reason: String): DamageOutcome {
            log?.record(LogCategory.DAMAGE, reason, nowSeconds)
            return DamageOutcome(
                applied = false, finalDamage = 0f, healthLost = 0f,
                armorAbsorbed = 0f, armorRemaining = target.armorPoints,
                killed = false, ignoredBecause = reason
            )
        }

        if (!target.isAlive) return refuse("Target is already dead")
        if (target.isInvulnerable(nowSeconds)) return refuse("Target is invulnerable")
        if (!missionPlaying) return refuse("Mission is not in Playing")

        val computation = computeDetailed(request, target)
        var final = computation.finalDamage
        if (target.isPlayer && final > 0f) {
            final = max(final, HealthRules.PLAYER_MIN_DAMAGE)
        }
        final = StoryProtection.clampLethal(final, target, scriptedBeat)
        if (final <= 0f) return refuse("Damage resolves to zero")

        val armorAbsorbed = computation.armorAbsorbed
        if (armorAbsorbed > 0f) {
            val spent = armorAbsorbed * (1f - request.armorPierce.coerceIn(0f, 1f))
            target.armorPoints = (target.armorPoints - spent).coerceAtLeast(0f)
        }

        val before = target.current
        val applied = target.receiveDamage(
            DamageInfo(
                amount = final,
                zone = request.zone,
                type = request.type,
                sourceId = sourceId,
                sourceTeam = sourceTeam,
                weaponId = weaponId
            )
        )
        target.lastDamageAtSeconds = nowSeconds
        val healthLost = before - target.current
        val killed = target.dead && target.markKillReported()
        if (applied) {
            bus?.emit(PlanEventType.DAMAGE_DEALT, target.id, nowSeconds)
            if (killed) bus?.emit(PlanEventType.ENTITY_KILLED, target.id, nowSeconds)
        }
        return DamageOutcome(
            applied = applied,
            finalDamage = final,
            healthLost = healthLost,
            armorAbsorbed = armorAbsorbed,
            armorRemaining = target.armorPoints,
            killed = killed
        )
    }

    /** Headshots must always beat torso shots for the same shell (Section 5.4 test). */
    fun headAlwaysBeatsTorso(
        baseDamage: Float,
        distanceMeters: Float,
        weaponRangeMeters: Float,
        minDamageMult: Float,
        helmetTier: Int? = null
    ): Boolean {
        val head = DamageMath.damageAtRange(baseDamage, distanceMeters, weaponRangeMeters, minDamageMult) *
            ArmorMath.headMultiplier(helmetTier)
        val torso = DamageMath.damageAtRange(baseDamage, distanceMeters, weaponRangeMeters, minDamageMult)
        return head >= torso
    }
}
