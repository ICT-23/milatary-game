package com.example.game.combat

import kotlin.math.max
import kotlin.math.min

/**
 * Hit zones mandated by section 8.3 of the design document.
 *
 *   Head / helmet       1.70x  helmet protection may reduce it further
 *   Upper torso         1.00x  standard reference zone
 *   Lower torso         0.90x
 *   Arm                 0.65x
 *   Leg                 0.60x
 *   Protected entity    0.00x  never a valid combat target
 *
 * The plate row is not a hit-zone multiplier: a plate is armour, and armour is
 * applied through [ArmourGrade] or the durability model in [DamageResolver].
 */
enum class HitZone(val displayName: String, val zoneMultiplier: Float) {
    HEAD("Head / Helmet", 1.70f),
    UPPER_TORSO("Upper Torso", 1.00f),
    LOWER_TORSO("Lower Torso", 0.90f),
    ARM("Arm", 0.65f),
    LEG("Leg", 0.60f),
    ARMOR_PLATE("Armour Plate", 0.45f),
    PROTECTED("Protected / Non-Combatant", 0.00f);

    /** Kept as a method so every existing call site reads unchanged. */
    fun multiplier(): Float = zoneMultiplier

    /** Civilians, medics, surrendered characters and marked neutrals. */
    val isProtected: Boolean get() = this == PROTECTED
}

/** Section 8.4 armour multiplier: 1.0 unarmoured, 0.65 light, 0.45 plate. */
enum class ArmourGrade(val multiplier: Float) {
    UNARMOURED(1.0f),
    LIGHT(0.65f),
    PLATE(0.45f)
}

/**
 * Mission-level modifiers that travel with a validated hit. The mission rules, not the
 * weapon, decide how hard a round lands, so the target never guesses.
 */
data class DamageModifiers(
    val armourGrade: ArmourGrade = ArmourGrade.UNARMOURED,
    val missionDifficultyMultiplier: Float = 1f,
    val temporaryStatusMultiplier: Float = 1f
)

/** Surface a round actually struck. Drives the impact VFX, never the damage. */
enum class SurfaceType(val displayName: String) {
    CONCRETE("Concrete"),
    STEEL("Steel"),
    WOOD("Wood"),
    DIRT("Dirt"),
    FLESH("Flesh"),
    GENERIC("Generic")
}

/**
 * Verified damage package. Mirrors the documented `DamagePayload` contract:
 * raw damage, zone, impact geometry, bullet velocity, penetration power, attacker.
 * A payload can only be produced by [BallisticsEngine] after the physical hit check.
 */
data class DamagePayload(
    val rawDamage: Float,
    val zone: HitZone,
    val hitPoint: Vec3,
    val hitNormal: Vec3,
    val bulletVelocity: Vec3,
    val armorPenetrationPower: Float,
    val attackerId: String,
    val distanceMeters: Float = 0f,
    val surface: SurfaceType = SurfaceType.FLESH
)

/** Anything that can receive verified damage. */
interface Damageable {
    val isDestroyedOrDead: Boolean
    /** Physical anchor the aim and muzzle validation are measured against. */
    val transformRef: Vec3
    fun applyDamage(payload: DamagePayload, modifiers: DamageModifiers = DamageModifiers())
}

/** Plate / helmet state used by the armor mitigation stage. */
data class ArmorProfile(
    val plateRating: Int = 0,
    val plateDurability: Int = 0,
    val helmetTier: Int = 0
) {
    val hasPlate: Boolean get() = plateDurability > 0
}

/** Final numbers produced by the armor mitigation stage. */
data class DamageResolution(
    val appliedDamage: Int,
    val zoneMultiplier: Float,
    val falloff: Float,
    val armorAbsorbed: Int,
    /** True when a protected entity was struck, so mission rules can respond. */
    val protectedViolation: Boolean = false
) {
    /** The round carried enough energy past the plate to wound the target. */
    val penetrated: Boolean get() = appliedDamage > 0
}

/**
 * The single damage formula required by section 8.4:
 *
 * ```text
 * finalDamage = baseDamage * rangeMultiplier * hitZoneMultiplier
 *             * armourMultiplier * missionDifficultyMultiplier
 *             * temporaryStatusMultiplier
 * ```
 *
 * Nothing else in the project re-derives these numbers, and the result is
 * clamped to a valid non-negative range.
 */
object DamageFormula {

    fun compute(
        baseDamage: Float,
        rangeMultiplier: Float,
        hitZoneMultiplier: Float,
        armourMultiplier: Float,
        missionDifficultyMultiplier: Float = 1f,
        temporaryStatusMultiplier: Float = 1f
    ): Float {
        val product = baseDamage *
            rangeMultiplier.coerceAtLeast(0f) *
            hitZoneMultiplier.coerceAtLeast(0f) *
            armourMultiplier.coerceAtLeast(0f) *
            missionDifficultyMultiplier.coerceAtLeast(0f) *
            temporaryStatusMultiplier.coerceAtLeast(0f)
        return product.coerceAtLeast(0f)
    }

    /**
     * Data-driven range curve: full power inside effective range, then a linear
     * decline to [DamageResolver.MIN_RANGE_FALLOFF] at twice the effective range.
     * A shot past that still registers and uses the floor value.
     */
    fun rangeMultiplier(distanceMeters: Float, effectiveRangeMeters: Float, minMultiplier: Float): Float {
        if (effectiveRangeMeters <= 0f) return 1f
        if (distanceMeters <= effectiveRangeMeters) return 1f
        val overshoot = ((distanceMeters - effectiveRangeMeters) / effectiveRangeMeters).coerceIn(0f, 1f)
        return 1f - (1f - minMultiplier) * overshoot
    }
}

/**
 * Converts a validated shot into the documented payload. Returns null for every
 * rejected shot, so callers physically cannot apply damage from a miss.
 */
fun ShotResolution.toDamagePayload(
    rawDamage: Float,
    armorPenetrationPower: Float,
    bulletVelocityMetersPerSecond: Float,
    attackerId: String
): DamagePayload? {
    if (!canApplyDamage) return null
    return DamagePayload(
        rawDamage = rawDamage,
        zone = hitZone,
        hitPoint = hitPoint,
        hitNormal = hitNormal,
        bulletVelocity = direction * bulletVelocityMetersPerSecond,
        armorPenetrationPower = armorPenetrationPower,
        attackerId = attackerId,
        distanceMeters = distanceMeters,
        surface = surface
    )
}

/**
 * Single source of truth for `BaseDamage * Zone * Falloff * Penetration`.
 * Nothing else in the project is allowed to re-derive these numbers.
 */
object DamageResolver {

    /** Documented bullet drop-off: full power at the muzzle, 40% at effective range. */
    const val MIN_RANGE_FALLOFF = 0.4f

    /** Surplus energy that survives a plate it had to defeat. */
    const val PLATE_PASS_THROUGH_RETENTION = 0.75f

    fun rangeFalloff(distanceMeters: Float, effectiveRangeMeters: Float): Float {
        if (effectiveRangeMeters <= 0f) return 1f
        val ratio = (distanceMeters / effectiveRangeMeters).coerceIn(0f, 1f)
        return 1f - (1f - MIN_RANGE_FALLOFF) * ratio
    }

    fun zoneMultiplier(zone: HitZone, helmetTier: Int): Float {
        if (zone.isProtected) return 0f
        if (zone != HitZone.HEAD) return zone.multiplier()
        // Helmets degrade the cranial multiplier instead of removing it.
        return max(1f, zone.multiplier() - 0.5f * helmetTier.coerceAtLeast(0))
    }

    /**
     * Applies the documented formula. Protected entities return zero damage so a
     * civilian can never lose combat health, whatever the shot looked like.
     */
    fun resolve(
        baseDamage: Float,
        zone: HitZone,
        distanceMeters: Float,
        effectiveRangeMeters: Float,
        armorPenetration: Float,
        armor: ArmorProfile = ArmorProfile(),
        armourGrade: ArmourGrade = ArmourGrade.UNARMOURED,
        missionDifficultyMultiplier: Float = 1f,
        temporaryStatusMultiplier: Float = 1f
    ): DamageResolution {
        val falloff = rangeFalloff(distanceMeters, effectiveRangeMeters)
        if (zone.isProtected) {
            return DamageResolution(
                appliedDamage = 0,
                zoneMultiplier = 0f,
                falloff = falloff,
                armorAbsorbed = 0,
                protectedViolation = true
            )
        }

        val zoneMultiplier = zoneMultiplier(zone, armor.helmetTier)
        val penetration = armorPenetration.coerceIn(0f, 1f)
        // A torso plate cannot absorb a round that struck the head or a limb.
        val plateCoversZone = zone == HitZone.UPPER_TORSO ||
            zone == HitZone.LOWER_TORSO ||
            zone == HitZone.ARMOR_PLATE
        val durabilityPlate = armor.hasPlate && plateCoversZone
        // A durability plate owns the mitigation; the grade covers everything else.
        val armourMultiplier = if (durabilityPlate) 1f else armourGrade.multiplier

        var remaining = DamageFormula.compute(
            baseDamage = baseDamage,
            rangeMultiplier = falloff,
            hitZoneMultiplier = zoneMultiplier,
            armourMultiplier = armourMultiplier,
            missionDifficultyMultiplier = missionDifficultyMultiplier,
            temporaryStatusMultiplier = temporaryStatusMultiplier
        )

        var absorbed = 0
        if (durabilityPlate && remaining > 0f) {
            // Penetration power erodes the plate before the body takes the round.
            val capacity = (armor.plateDurability * (1f - penetration * 0.5f)).toInt().coerceAtLeast(0)
            absorbed = min(remaining.toInt(), capacity)
            remaining -= absorbed
            // Anything that defeats the plate still loses energy crossing it.
            if (absorbed > 0) remaining *= PLATE_PASS_THROUGH_RETENTION
        }

        return DamageResolution(
            appliedDamage = remaining.toInt().coerceAtLeast(0),
            zoneMultiplier = zoneMultiplier,
            falloff = falloff,
            armorAbsorbed = absorbed
        )
    }
}
