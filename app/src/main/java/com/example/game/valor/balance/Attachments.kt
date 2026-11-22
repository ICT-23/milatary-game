package com.example.game.valor.balance

/** Section 2.7 slot column. */
enum class AttachmentSlot(val displayName: String) {
    OPTIC("Optic"),
    MUZZLE("Muzzle"),
    BARREL("Barrel"),
    UNDERBARREL("Underbarrel"),
    MAGAZINE("Magazine"),
    STOCK("Stock"),
    AMMO("Ammo")
}

/**
 * The modifier vocabulary of Section 2.7. Percent keys are stored as the signed
 * percentage the table prints; multiplier keys as the factor it prints.
 */
enum class ModKey(val displayName: String, val unit: String) {
    ADS_FOV("ADS FOV", "deg"),
    ACCURACY_PCT("Accuracy", "%"),
    ADS_TIME_PCT("ADS time", "%"),
    SWAY_PCT("Sway", "%"),
    NOISE_MULT("Noise", "x"),
    DAMAGE_MULT("Damage", "x"),
    RECOIL_V_PCT("Recoil V", "%"),
    RECOIL_H_PCT("Recoil H", "%"),
    RANGE_PCT("Range", "%"),
    MOBILITY_PCT("Mobility", "%"),
    MAG_MULT("Magazine", "x"),
    RELOAD_PCT("Reload", "%"),
    ARMOR_PIERCE_PCT("Armor pierce", "%"),
    DAMAGE_UNARMORED_MULT("Damage vs unarmored", "x"),
    PRONE_RECOIL_PCT("Recoil prone/cover", "%"),
    MUZZLE_FLASH_MULT("Muzzle flash", "x")
}

/** Section 2.7: one craftable part with its cost triple. */
data class AttachmentSpec(
    val slot: AttachmentSlot,
    val name: String,
    val mods: Map<ModKey, Float>,
    val steel: Int,
    val parts: Int,
    val cash: Int,
    val unlockRank: Int,
    val note: String = ""
)

object AttachmentCatalog {

    private fun att(
        slot: AttachmentSlot,
        name: String,
        steel: Int,
        parts: Int,
        cash: Int,
        unlockRank: Int,
        vararg mods: Pair<ModKey, Float>
    ) = AttachmentSpec(slot, name, mods.toMap(), steel, parts, cash, unlockRank)

    val ATTACHMENTS: List<AttachmentSpec> = listOf(
        // Optic
        att(AttachmentSlot.OPTIC, "Red-dot", 5, 2, 300, 3,
            ModKey.ADS_FOV to 50f, ModKey.ACCURACY_PCT to 3f),
        att(AttachmentSlot.OPTIC, "4x scope", 10, 4, 900, 6,
            ModKey.ADS_FOV to 28f, ModKey.ADS_TIME_PCT to 10f),
        att(AttachmentSlot.OPTIC, "8x scope", 18, 8, 2_400, 10,
            ModKey.ADS_FOV to 12f, ModKey.ADS_TIME_PCT to 25f, ModKey.SWAY_PCT to 15f),
        att(AttachmentSlot.OPTIC, "Thermal", 30, 15, 6_000, 15,
            ModKey.ADS_TIME_PCT to 20f),

        // Muzzle
        att(AttachmentSlot.MUZZLE, "Suppressor", 15, 8, 1_800, 8,
            ModKey.NOISE_MULT to 0.3f, ModKey.DAMAGE_MULT to 0.95f, ModKey.MUZZLE_FLASH_MULT to 0f),
        att(AttachmentSlot.MUZZLE, "Compensator", 8, 3, 700, 6, ModKey.RECOIL_V_PCT to -15f),
        att(AttachmentSlot.MUZZLE, "Flash hider", 6, 2, 500, 6, ModKey.MUZZLE_FLASH_MULT to 0.3f),

        // Barrel
        att(AttachmentSlot.BARREL, "Long barrel", 12, 5, 1_200, 9,
            ModKey.RANGE_PCT to 20f, ModKey.MOBILITY_PCT to -5f),
        att(AttachmentSlot.BARREL, "Short barrel", 10, 4, 900, 9,
            ModKey.MOBILITY_PCT to 8f, ModKey.RANGE_PCT to -15f),

        // Underbarrel
        att(AttachmentSlot.UNDERBARREL, "Foregrip", 8, 3, 800, 6,
            ModKey.RECOIL_H_PCT to -12f, ModKey.RECOIL_V_PCT to -8f),
        att(AttachmentSlot.UNDERBARREL, "Bipod", 12, 5, 1_300, 10,
            ModKey.PRONE_RECOIL_PCT to -35f, ModKey.ADS_TIME_PCT to 10f),

        // Magazine
        att(AttachmentSlot.MAGAZINE, "Extended mag", 10, 4, 1_000, 6,
            ModKey.MAG_MULT to 1.4f, ModKey.RELOAD_PCT to 8f),
        att(AttachmentSlot.MAGAZINE, "Fast mag", 10, 4, 1_000, 6, ModKey.RELOAD_PCT to -25f),

        // Stock
        att(AttachmentSlot.STOCK, "Heavy stock", 8, 3, 700, 8,
            ModKey.RECOIL_V_PCT to -10f, ModKey.MOBILITY_PCT to -4f),
        att(AttachmentSlot.STOCK, "Light stock", 8, 3, 700, 8,
            ModKey.MOBILITY_PCT to 6f, ModKey.RECOIL_V_PCT to 5f),

        // Ammo
        att(AttachmentSlot.AMMO, "Armor-piercing", 20, 10, 2_000, 11,
            ModKey.ARMOR_PIERCE_PCT to 40f, ModKey.DAMAGE_MULT to 0.95f),
        att(AttachmentSlot.AMMO, "Hollow-point", 18, 8, 1_800, 11,
            ModKey.DAMAGE_UNARMORED_MULT to 1.15f, ModKey.ARMOR_PIERCE_PCT to -30f),
        att(AttachmentSlot.AMMO, "Tracer", 6, 2, 400, 7).copy(
            note = "Visible trail; accuracy feedback"
        )
    )

    fun forSlot(slot: AttachmentSlot): List<AttachmentSpec> =
        ATTACHMENTS.filter { it.slot == slot }

    fun byName(name: String): AttachmentSpec? =
        ATTACHMENTS.firstOrNull { it.name.equals(name.trim(), ignoreCase = true) }

    fun unlockedAt(rank: Int): List<AttachmentSpec> =
        ATTACHMENTS.filter { it.unlockRank <= rank }

    /** Section 2.7 cost triple, kept together so a craft never reads two sources. */
    fun craftCost(spec: AttachmentSpec): CraftCost = CraftCost(spec.steel, spec.parts, spec.cash)
}

data class CraftCost(val steel: Int, val parts: Int, val cash: Int)

/**
 * Section 2.7 rules: "Add first, then Multiply; clamp; recompute from base".
 * Percent keys therefore sum, multiplier keys multiply, and absolute values
 * (only the optic FOV) survive untouched because one optic is fitted at a time.
 */
object ModifierStacking {

    const val MIN_FACTOR = 0f
    const val MAX_FACTOR = 3f

    val MULTIPLIER_KEYS: Set<ModKey> = setOf(
        ModKey.NOISE_MULT,
        ModKey.DAMAGE_MULT,
        ModKey.MAG_MULT,
        ModKey.MUZZLE_FLASH_MULT,
        ModKey.DAMAGE_UNARMORED_MULT
    )

    fun stack(fitted: List<AttachmentSpec>): Map<ModKey, Float> {
        val stacked = mutableMapOf<ModKey, Float>()
        ModKey.entries.forEach { key ->
            val values = fitted.mapNotNull { it.mods[key] }
            if (values.isEmpty()) return@forEach
            stacked[key] = when (key) {
                in MULTIPLIER_KEYS -> values.fold(1f) { acc, value -> acc * value }
                ModKey.ADS_FOV -> values.first()
                else -> values.fold(0f) { acc, value -> acc + value }
            }
        }
        return stacked
    }

    /** Converts one entry of the stacked map back into a multiplier on the base. */
    fun factorFor(key: ModKey, stacked: Map<ModKey, Float>): Float {
        val value = stacked[key] ?: return 1f
        val raw = when (key) {
            in MULTIPLIER_KEYS -> value
            ModKey.ADS_FOV -> return value
            else -> 1f + value / 100f
        }
        return raw.coerceIn(MIN_FACTOR, MAX_FACTOR)
    }

    fun damageMultiplier(stacked: Map<ModKey, Float>): Float = factorFor(ModKey.DAMAGE_MULT, stacked)

    fun recoilMultiplier(stacked: Map<ModKey, Float>): Float = factorFor(ModKey.RECOIL_V_PCT, stacked)

    fun rangeMultiplier(stacked: Map<ModKey, Float>): Float = factorFor(ModKey.RANGE_PCT, stacked)

    fun reloadMultiplier(stacked: Map<ModKey, Float>): Float = factorFor(ModKey.RELOAD_PCT, stacked)
}
