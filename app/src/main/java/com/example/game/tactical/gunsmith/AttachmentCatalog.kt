package com.example.game.tactical.gunsmith

/**
 * Section 4.1, "6-Slot Attachment Customization".
 *
 * Every attachment is pure data. The numbers that the specification fixes (zoom
 * factors, magazine sizes, armour-piercing share, hollow-point bonus) are exact;
 * the remaining tuning values live here as named constants so balance changes
 * never leak into the combat code.
 */
enum class AttachmentSlot(val displayName: String) {
    MUZZLE("Muzzle"),
    BARREL("Barrel"),
    OPTIC("Optic"),
    UNDERBARREL("Underbarrel"),
    MAGAZINE("Magazine"),
    AMMUNITION("Ammunition Type")
}

data class Attachment(
    val id: String,
    val slot: AttachmentSlot,
    val name: String,
    val recoilGripModifier: Float = 0f,
    val recoilStockModifier: Float = 0f,
    val recoilMuzzleModifier: Float = 0f,
    val adsWeightMs: Float = 0f,
    val damageMultiplier: Float = 1f,
    val rangeMultiplier: Float = 1f,
    val mobilityMultiplier: Float = 1f,
    val zoomMin: Float = 1f,
    val zoomMax: Float = 1f,
    val magazineSize: Int = 0,
    val reloadSecondsMultiplier: Float = 1f,
    val armorIgnorePercent: Int = 0,
    val unarmoredDamageBonusPercent: Int = 0,
    val suppressed: Boolean = false,
    val hidesFlash: Boolean = false,
    val muzzleVelocityPenaltyMps: Float = 0f,
    val hipAccuracyBonusPercent: Int = 0
)

object AttachmentCatalog {

    // Tuning constants used where the design document gives no number.
    const val COMPENSATOR_VERTICAL_RECOIL = 0.18f
    const val VERTICAL_FOREGRIP_RECOIL = 0.22f
    const val ANGLED_GRIP_ADS_MS = -35f
    const val SHORT_BARREL_ADS_MS = -90f
    const val HEAVY_BARREL_ADS_MS = 70f
    const val HEAVY_BARREL_DAMAGE = 1.10f
    const val HEAVY_BARREL_RANGE = 1.30f
    const val SHORT_BARREL_RANGE = 0.80f
    const val HEAVY_BARREL_MOBILITY = 0.92f
    const val DRUM_MAGAZINE_MOBILITY = 0.85f
    const val SUPPRESSOR_VELOCITY_PENALTY = 18f
    const val LASER_HIP_ACCURACY = 30
    const val FAST_MAG_RELOAD_MULTIPLIER = 0.75f

    // ------------------------------------------------------------- muzzles
    val FLASH_HIDER = Attachment(
        id = "MUZZLE_FLASH_HIDER",
        slot = AttachmentSlot.MUZZLE,
        name = "Flash Hider",
        hidesFlash = true
    )

    val COMPENSATOR = Attachment(
        id = "MUZZLE_COMPENSATOR",
        slot = AttachmentSlot.MUZZLE,
        name = "Compensator",
        recoilMuzzleModifier = COMPENSATOR_VERTICAL_RECOIL
    )

    val SUPPRESSOR = Attachment(
        id = "MUZZLE_SUPPRESSOR",
        slot = AttachmentSlot.MUZZLE,
        name = "Suppressor",
        suppressed = true,
        hidesFlash = true,
        muzzleVelocityPenaltyMps = SUPPRESSOR_VELOCITY_PENALTY
    )

    // -------------------------------------------------------------- barrels
    val SHORT_CQB_BARREL = Attachment(
        id = "BARREL_SHORT_CQB",
        slot = AttachmentSlot.BARREL,
        name = "Short CQB Barrel",
        adsWeightMs = SHORT_BARREL_ADS_MS,
        rangeMultiplier = SHORT_BARREL_RANGE
    )

    val HEAVY_LONG_BARREL = Attachment(
        id = "BARREL_HEAVY_LONG",
        slot = AttachmentSlot.BARREL,
        name = "Heavy Long Barrel",
        adsWeightMs = HEAVY_BARREL_ADS_MS,
        damageMultiplier = HEAVY_BARREL_DAMAGE,
        rangeMultiplier = HEAVY_BARREL_RANGE,
        mobilityMultiplier = HEAVY_BARREL_MOBILITY
    )

    // ---------------------------------------------------------------- optics
    val IRON_SIGHTS = Attachment(id = "OPTIC_IRON", slot = AttachmentSlot.OPTIC, name = "Iron Sights")
    val RED_DOT = Attachment(
        id = "OPTIC_RED_DOT",
        slot = AttachmentSlot.OPTIC,
        name = "Red Dot 1.5x",
        zoomMin = 1.5f,
        zoomMax = 1.5f
    )
    val ACOG = Attachment(
        id = "OPTIC_ACOG",
        slot = AttachmentSlot.OPTIC,
        name = "ACOG 4x",
        zoomMin = 4f,
        zoomMax = 4f
    )
    val VARIABLE_SNIPER_SCOPE = Attachment(
        id = "OPTIC_VARIABLE_SNIPER",
        slot = AttachmentSlot.OPTIC,
        name = "Variable Sniper Scope 8x-12x",
        zoomMin = 8f,
        zoomMax = 12f
    )

    // ----------------------------------------------------------- underbarrel
    val ANGLED_GRIP = Attachment(
        id = "UNDERBARREL_ANGLED_GRIP",
        slot = AttachmentSlot.UNDERBARREL,
        name = "Angled Grip",
        recoilGripModifier = 0f,
        adsWeightMs = ANGLED_GRIP_ADS_MS
    )

    val VERTICAL_FOREGRIP = Attachment(
        id = "UNDERBARREL_VERTICAL_GRIP",
        slot = AttachmentSlot.UNDERBARREL,
        name = "Vertical Foregrip",
        recoilGripModifier = VERTICAL_FOREGRIP_RECOIL
    )

    val LASER_SIGHT = Attachment(
        id = "UNDERBARREL_LASER",
        slot = AttachmentSlot.UNDERBARREL,
        name = "Laser Sight",
        hipAccuracyBonusPercent = LASER_HIP_ACCURACY
    )

    // ------------------------------------------------------------ magazines
    val STANDARD_MAGAZINE = Attachment(
        id = "MAGAZINE_STANDARD",
        slot = AttachmentSlot.MAGAZINE,
        name = "Standard Magazine",
        magazineSize = 30
    )
    val FAST_MAGAZINE = Attachment(
        id = "MAGAZINE_FAST",
        slot = AttachmentSlot.MAGAZINE,
        name = "Fast Magazine",
        magazineSize = 30,
        reloadSecondsMultiplier = FAST_MAG_RELOAD_MULTIPLIER
    )
    val EXTENDED_DRUM = Attachment(
        id = "MAGAZINE_DRUM",
        slot = AttachmentSlot.MAGAZINE,
        name = "Extended Drum",
        magazineSize = 60,
        mobilityMultiplier = DRUM_MAGAZINE_MOBILITY
    )

    // ----------------------------------------------------------- ammunition
    val STANDARD_FMJ = Attachment(
        id = "AMMO_FMJ",
        slot = AttachmentSlot.AMMUNITION,
        name = "Standard FMJ"
    )
    val ARMOR_PIERCING = Attachment(
        id = "AMMO_ARMOR_PIERCING",
        slot = AttachmentSlot.AMMUNITION,
        name = "Armor Piercing",
        armorIgnorePercent = 50
    )
    val HOLLOW_POINT = Attachment(
        id = "AMMO_HOLLOW_POINT",
        slot = AttachmentSlot.AMMUNITION,
        name = "Hollow Point",
        unarmoredDamageBonusPercent = 25
    )

    val ALL: List<Attachment> = listOf(
        FLASH_HIDER, COMPENSATOR, SUPPRESSOR,
        SHORT_CQB_BARREL, HEAVY_LONG_BARREL,
        IRON_SIGHTS, RED_DOT, ACOG, VARIABLE_SNIPER_SCOPE,
        ANGLED_GRIP, VERTICAL_FOREGRIP, LASER_SIGHT,
        STANDARD_MAGAZINE, FAST_MAGAZINE, EXTENDED_DRUM,
        STANDARD_FMJ, ARMOR_PIERCING, HOLLOW_POINT
    )

    fun byId(id: String): Attachment? = ALL.firstOrNull { it.id == id }

    fun optionsFor(slot: AttachmentSlot): List<Attachment> = ALL.filter { it.slot == slot }

    /** The loadout a fresh Recruit is issued before any customisation. */
    val DEFAULT: Map<AttachmentSlot, Attachment> = mapOf(
        AttachmentSlot.MUZZLE to FLASH_HIDER,
        AttachmentSlot.BARREL to SHORT_CQB_BARREL,
        AttachmentSlot.OPTIC to IRON_SIGHTS,
        AttachmentSlot.UNDERBARREL to ANGLED_GRIP,
        AttachmentSlot.MAGAZINE to STANDARD_MAGAZINE,
        AttachmentSlot.AMMUNITION to STANDARD_FMJ
    )
}
