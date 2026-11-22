package com.example.game.tactical.combat

/**
 * Section 5.1 item 3: the segmented hitboxes and the damage equation
 *
 *     D_final = D_base x M_hitbox x F_dropoff(d) x (1 - A_reduction)
 *
 * Damage only ever comes from this file, which is only ever called with a
 * physically resolved hit. There is no timer and no proximity shortcut.
 */
enum class HitboxComponent(
    val displayName: String,
    val multiplier: Float,
    val appliesLimbPenalty: Boolean
) {
    HEAD("Head", 2.5f, false),
    THORAX("Thorax", 1.2f, false),
    STOMACH("Stomach", 1.0f, false),
    LIMBS("Limbs", 0.75f, true)
}

/** Everything the damage equation needs to know about who was hit. */
data class TargetProfile(
    val isHelmeted: Boolean = false,
    val isArmored: Boolean = false,
    val armorReductionPercent: Float = 0f
)

/** Ammunition type effects from Section 4.1. */
data class AmmoProfile(
    val armorIgnorePercent: Int = 0,
    val unarmoredDamageBonusPercent: Int = 0
) {
    companion object {
        val FMJ = AmmoProfile()
        val ARMOR_PIERCING = AmmoProfile(armorIgnorePercent = 50)
        val HOLLOW_POINT = AmmoProfile(unarmoredDamageBonusPercent = 25)
    }
}

data class DamageResult(
    val hitbox: HitboxComponent,
    val rawDamage: Float,
    val dropoffFactor: Float,
    val effectiveArmorReduction: Float,
    val unarmoredBonusFactor: Float,
    val finalDamage: Float,
    val instantKill: Boolean,
    val aimSwayPenalty: Float,
    val movementPenalty: Float
)

object DamageModel {

    /** Section 5.1: linear from 1.0 at effective range down to 0.4 at max range. */
    const val MIN_DROPOFF = 0.4f
    const val LIMB_AIM_SWAY_PENALTY = 0.25f
    const val LIMB_MOVEMENT_PENALTY = 0.35f

    fun dropoff(distanceMeters: Float, effectiveRangeMeters: Float, maxRangeMeters: Float): Float {
        if (distanceMeters <= effectiveRangeMeters) return 1f
        if (maxRangeMeters <= effectiveRangeMeters) return MIN_DROPOFF
        if (distanceMeters >= maxRangeMeters) return MIN_DROPOFF
        val t = (distanceMeters - effectiveRangeMeters) / (maxRangeMeters - effectiveRangeMeters)
        return 1f - (1f - MIN_DROPOFF) * t
    }

    /**
     * A head hit kills instantly against an unhelmeted target; a sniper rifle
     * defeats the helmet. Every other hit is ordinary damage.
     */
    fun isInstantKill(hitbox: HitboxComponent, target: TargetProfile, isSniperRifle: Boolean): Boolean =
        hitbox == HitboxComponent.HEAD && (!target.isHelmeted || isSniperRifle)

    fun resolve(
        baseDamage: Float,
        hitbox: HitboxComponent,
        distanceMeters: Float,
        effectiveRangeMeters: Float,
        maxRangeMeters: Float,
        target: TargetProfile,
        ammo: AmmoProfile,
        isSniperRifle: Boolean = false
    ): DamageResult {
        val factor = dropoff(distanceMeters, effectiveRangeMeters, maxRangeMeters)

        val armorPercent = if (target.isArmored) target.armorReductionPercent.coerceIn(0f, 1f) else 0f
        val effectiveReduction = armorPercent * (1f - ammo.armorIgnorePercent.coerceIn(0, 100) / 100f)

        val hollowPointApplies = !target.isArmored &&
            (hitbox == HitboxComponent.THORAX || hitbox == HitboxComponent.LIMBS)
        val unarmoredBonus = if (hollowPointApplies) {
            1f + ammo.unarmoredDamageBonusPercent.coerceAtLeast(0) / 100f
        } else {
            1f
        }

        val damage = baseDamage * hitbox.multiplier * factor * (1f - effectiveReduction) * unarmoredBonus

        return DamageResult(
            hitbox = hitbox,
            rawDamage = baseDamage,
            dropoffFactor = factor,
            effectiveArmorReduction = effectiveReduction,
            unarmoredBonusFactor = unarmoredBonus,
            finalDamage = damage.coerceAtLeast(0f),
            instantKill = isInstantKill(hitbox, target, isSniperRifle),
            aimSwayPenalty = if (hitbox.appliesLimbPenalty) LIMB_AIM_SWAY_PENALTY else 0f,
            movementPenalty = if (hitbox.appliesLimbPenalty) LIMB_MOVEMENT_PENALTY else 0f
        )
    }
}
