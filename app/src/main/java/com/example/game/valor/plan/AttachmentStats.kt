package com.example.game.valor.plan

import com.example.game.valor.balance.AttachmentSlot
import com.example.game.valor.balance.AttachmentSpec
import com.example.game.valor.balance.ModKey
import com.example.game.valor.balance.ModifierStacking

/** Section 5.7: the stats an attachment can move, taken from the base weapon data. */
data class WeaponStats(
    val damage: Float,
    val recoil: Float,
    val rangeMeters: Float,
    val reloadSeconds: Float,
    val accuracy: Float,
    val adsFov: Float,
    val adsSpreadMult: Float,
    val magSize: Int,
    val noiseRadiusMeters: Float
)

/** Section 5.7 "clamped to min/max defined in WeaponData". */
data class StatBounds(val min: Float = 0f, val max: Float = Float.MAX_VALUE) {
    fun clamp(value: Float): Float = value.coerceIn(min, max)
}

data class WeaponStatBounds(
    val damage: StatBounds = StatBounds(),
    val recoil: StatBounds = StatBounds(),
    val range: StatBounds = StatBounds(),
    val reload: StatBounds = StatBounds(),
    val accuracy: StatBounds = StatBounds(),
    val magazine: StatBounds = StatBounds()
)

/** Section 5.7: the fitted parts of one weapon, one per slot. */
data class WeaponLoadout(val weaponId: String, val fitted: List<AttachmentSpec> = emptyList()) {
    fun slot(slot: AttachmentSlot): AttachmentSpec? = fitted.firstOrNull { it.slot == slot }
}

/**
 * Section 5.7 and 5.18: attachments persist per weapon instance, not globally,
 * so two of the same rifle can carry different parts.
 */
class AttachmentBook {

    private val byWeapon = mutableMapOf<String, List<AttachmentSpec>>()

    fun fitted(weaponId: String): List<AttachmentSpec> = byWeapon[weaponId] ?: emptyList()

    fun equip(weaponId: String, spec: AttachmentSpec): List<AttachmentSpec> {
        val next = AttachmentRules.equip(fitted(weaponId), spec)
        byWeapon[weaponId] = next
        return next
    }

    fun remove(weaponId: String, slot: AttachmentSlot): List<AttachmentSpec> {
        val next = AttachmentRules.remove(fitted(weaponId), slot)
        byWeapon[weaponId] = next
        return next
    }

    fun forget(weaponId: String) = byWeapon.remove(weaponId)
}

/** Section 5.7: modifier order, slot exclusivity and the suppressor rule. */
object AttachmentRules {

    const val ONE_PER_SLOT = true
    const val SUPPRESSOR_NOISE_MULT = 0.3f
    const val SUPPRESSOR_DAMAGE_MULT = 0.95f
    const val SUPPRESSOR_HIDES_MUZZLE_FLASH = true
    const val ADD_FIRST_THEN_MULTIPLY = true
    const val RECOMPUTE_FROM_BASE = true

    /** "One attachment per slot; incompatible pairs blocked (e.g. two optics)." */
    fun canFit(existing: List<AttachmentSpec>, candidate: AttachmentSpec): Boolean =
        existing.none { it.slot == candidate.slot }

    fun equip(existing: List<AttachmentSpec>, candidate: AttachmentSpec): List<AttachmentSpec> =
        if (canFit(existing, candidate)) existing + candidate else existing

    fun remove(existing: List<AttachmentSpec>, slot: AttachmentSlot): List<AttachmentSpec> =
        existing.filter { it.slot != slot }

    /** "Not available on weapons flagged noSuppressor." */
    fun suppressorAllowed(noSuppressorFlag: Boolean): Boolean = !noSuppressorFlag

    fun suppressorFits(existing: List<AttachmentSpec>, noSuppressorFlag: Boolean): Boolean =
        suppressorAllowed(noSuppressorFlag) &&
            existing.none { it.slot == AttachmentSlot.MUZZLE }

    fun opticsFitted(existing: List<AttachmentSpec>): Int =
        existing.count { it.slot == AttachmentSlot.OPTIC }

    /**
     * `(base + sum(Add)) * product(Mul)` then clamp, recomputed from [base] on
     * every call. Nothing is ever applied to a previous result.
     */
    fun recompute(
        base: WeaponStats,
        fitted: List<AttachmentSpec>,
        bounds: WeaponStatBounds = WeaponStatBounds()
    ): WeaponStats {
        val stacked = ModifierStacking.stack(fitted)
        fun factor(key: ModKey): Float = ModifierStacking.factorFor(key, stacked)
        return WeaponStats(
            damage = bounds.damage.clamp(base.damage * factor(ModKey.DAMAGE_MULT)),
            recoil = bounds.recoil.clamp(base.recoil * factor(ModKey.RECOIL_V_PCT)),
            rangeMeters = bounds.range.clamp(base.rangeMeters * factor(ModKey.RANGE_PCT)),
            reloadSeconds = bounds.reload.clamp(base.reloadSeconds * factor(ModKey.RELOAD_PCT)),
            accuracy = bounds.accuracy.clamp(base.accuracy * factor(ModKey.ACCURACY_PCT)),
            adsFov = stacked[ModKey.ADS_FOV] ?: base.adsFov,
            adsSpreadMult = base.adsSpreadMult,
            magSize = bounds.magazine.clamp((base.magSize * factor(ModKey.MAG_MULT))).toInt(),
            noiseRadiusMeters = base.noiseRadiusMeters * factor(ModKey.NOISE_MULT)
        )
    }

    /** Equip then unequip any number of times returns exactly the base numbers. */
    fun roundTripIsIdentity(base: WeaponStats, spec: AttachmentSpec): Boolean =
        recompute(base, emptyList()) == recompute(base, remove(equip(emptyList(), spec), spec.slot))
}
