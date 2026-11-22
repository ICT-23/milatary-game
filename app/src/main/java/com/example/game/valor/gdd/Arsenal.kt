package com.example.game.valor.gdd

/** Section 5.1: one row of the category table. */
data class WeaponCategory(
    val category: String,
    val era: String,
    val examples: String,
    val role: String
)

object WeaponCategories {

    /** Section 5.1, transcribed row for row (11 categories). */
    val CATEGORIES: List<WeaponCategory> = listOf(
        WeaponCategory(
            "Pistol", "All", "Service Pistol 1911-style, Revolver, Modern 9mm", "Backup, fast draw"
        ),
        WeaponCategory("Bolt-action rifle", "WW1", "Bolt Rifle \"Pioneer\"", "High damage, slow fire"),
        WeaponCategory("Semi-auto rifle", "WW2", "Battle Rifle \"Liberty\"", "Balanced"),
        WeaponCategory(
            "Assault rifle", "Cold War / Modern", "AR \"Vanguard\", Carbine \"Raptor\"", "All-rounder"
        ),
        WeaponCategory("SMG", "WW2 / Modern", "SMG \"Hornet\"", "Close range, fast"),
        WeaponCategory("LMG", "WW1-Modern", "Machine Gun \"Anvil\"", "Suppression"),
        WeaponCategory("Shotgun", "All", "Pump Shotgun \"Breaker\"", "Very close range"),
        WeaponCategory(
            "Sniper", "All", "Sniper \"Longwatch\", Anti-material \"Titan\"", "Long range, one-shot"
        ),
        WeaponCategory("Launcher", "Modern", "Rocket Launcher \"Hammer\"", "Vehicles, bunkers"),
        WeaponCategory("Grenades", "All", "Frag, Smoke, Flash, Gas (WW1)", "Utility"),
        WeaponCategory("Melee", "All", "Trench knife, combat knife", "Silent kill")
    )

    fun byCategory(category: String): WeaponCategory? =
        CATEGORIES.firstOrNull { it.category.equals(category.trim(), ignoreCase = true) }

    fun byEra(era: String): List<WeaponCategory> =
        CATEGORIES.filter { it.era.contains(era, ignoreCase = true) }
}

/** Section 5.2: every gun carries these nine stats. */
enum class WeaponStat(val displayName: String, val note: String) {
    DAMAGE("Damage", "Per bullet or pellet"),
    FIRE_RATE("Fire rate (RPM)", "Rounds per minute"),
    RANGE("Range", "Metres before the falloff floor"),
    ACCURACY("Accuracy", "Cone tightness"),
    RECOIL("Recoil (vertical/horizontal)", "Kick per shot"),
    RELOAD("Reload time", "Seconds"),
    MAGAZINE("Magazine size", "Rounds per magazine"),
    MOBILITY("Mobility", "Move speed while aiming"),
    PENETRATION("Penetration", "Through thin cover")
}

object StatBars {

    val STATS: List<WeaponStat> = WeaponStat.entries

    /** "Stats shown as bars in the Armory UI + numeric on tap." */
    fun barFraction(value: Float, max: Float): Float =
        if (max <= 0f) 0f else (value / max).coerceIn(0f, 1f)

    /** Tap read-out: the number the bar represents, rounded for display. */
    fun numeric(value: Float): String =
        if (value == value.toInt().toFloat()) value.toInt().toString() else value.toString()
}

/** Section 5.3: the seven workshop steps, in order. */
data class GunsmithStep(val order: Int, val name: String, val detail: String)

/**
 * Section 5.3: one attachment slot. [unlockRank] is filled only where Section 4
 * names the slot's rank (optic at 3, grip/barrel at 6); the rest unlock "by
 * rank" without a printed number, so they stay null instead of being guessed.
 */
data class GunsmithSlotSpec(
    val slot: String,
    val options: List<String>,
    val effect: String,
    val unlockRank: Int? = null
)

/** Section 5.3 step 1-7 plus the slot table, the skin list and the level cap. */
object Gunsmith {

    const val MIN_LEVEL = 1
    const val MAX_LEVEL = 10

    /** Section 4 rank 3: "First gun attachment slot (optic)". */
    const val FIRST_SLOT_RANK = 3

    /** Section 4 rank 6: "Second attachment slot (grip/barrel)". */
    const val SECOND_SLOT_RANK = 6

    /** Section 4 rank 14: "Advanced attachments". */
    const val ADVANCED_ATTACHMENT_RANK = 14

    val STEPS: List<GunsmithStep> = listOf(
        GunsmithStep(1, "Pick a base weapon", "Unlocked by rank"),
        GunsmithStep(2, "Blueprints", "Collect blueprint pieces from missions/bosses to unlock new gun variants"),
        GunsmithStep(3, "Craft parts", "Spend Steel, Parts and Cash to craft attachments"),
        GunsmithStep(4, "Attach parts", "Slots unlock by rank"),
        GunsmithStep(5, "Upgrade levels", "Each gun Lv1-Lv10, damage/handling bonus, costs scale up"),
        GunsmithStep(6, "Skins / camos", "Desert, Woodland, Arctic, Gold, Prestige - cosmetic only"),
        GunsmithStep(7, "Weapon mastery", "Kills feed mastery XP, unlocking special attachments/skins")
    )

    val CRAFT_RESOURCES: List<String> = listOf("Steel", "Parts", "Cash")

    val SLOTS: List<GunsmithSlotSpec> = listOf(
        GunsmithSlotSpec(
            "Optic", listOf("Iron sights", "Red-dot", "4x scope", "8x scope", "Thermal"),
            "Aim zoom, accuracy", unlockRank = FIRST_SLOT_RANK
        ),
        GunsmithSlotSpec(
            "Muzzle", listOf("Suppressor", "Compensator", "Flash hider"),
            "Noise, recoil, flash"
        ),
        GunsmithSlotSpec(
            "Barrel", listOf("Short", "Long", "Heavy"),
            "Range vs mobility", unlockRank = SECOND_SLOT_RANK
        ),
        GunsmithSlotSpec(
            "Underbarrel", listOf("Foregrip", "Bipod", "Grenade launcher"),
            "Recoil, stability", unlockRank = SECOND_SLOT_RANK
        ),
        GunsmithSlotSpec(
            "Magazine", listOf("Extended", "Fast-mag", "Drum"),
            "Ammo, reload"
        ),
        GunsmithSlotSpec(
            "Stock", listOf("Light", "Heavy", "Adjustable"),
            "Handling, recoil"
        ),
        GunsmithSlotSpec(
            "Ammo type", listOf("Standard", "Armor-piercing", "Hollow-point", "Tracer"),
            "Damage type"
        )
    )

    /** Section 5.3 step 6: cosmetic only, never a stat change. */
    val SKINS: List<String> = listOf("Desert", "Woodland", "Arctic", "Gold", "Prestige")

    fun slot(name: String): GunsmithSlotSpec? =
        SLOTS.firstOrNull { it.slot.equals(name.trim(), ignoreCase = true) }

    fun slotCount(): Int = SLOTS.size

    fun optionCount(): Int = SLOTS.sumOf { it.options.size }

    /** Slots whose rank the document prints, at or below the given rank. */
    fun unlockedSlots(rank: Int): List<GunsmithSlotSpec> =
        SLOTS.filter { it.unlockRank != null && rank >= it.unlockRank }

    fun advancedAttachmentsAt(rank: Int): Boolean = rank >= ADVANCED_ATTACHMENT_RANK

    fun skinIsCosmetic(): Boolean = true
}

/** Section 5.3 step 2: blueprint pieces unlock variants and come from missions/bosses. */
object Blueprints {

    val SOURCES: List<String> = listOf("Missions", "Bosses")

    fun isSource(name: String): Boolean = SOURCES.any { it.equals(name.trim(), ignoreCase = true) }
}
