package com.example.game.valor.gdd

/**
 * Section 5.5: one row of the gear table. [function] keeps the document's
 * sentence; [tiered] marks the two rows the table calls "tiers".
 */
data class GearRow(
    val item: String,
    val function: String,
    val tiered: Boolean = false
)

object GearTable {

    /** Section 5.5, all eight rows. */
    val ROWS: List<GearRow> = listOf(
        GearRow("Helmet (tiers)", "Reduces headshot damage", tiered = true),
        GearRow("Body armor (tiers)", "Absorbs damage, breaks after limit", tiered = true),
        GearRow("Medkit / bandage", "Heal"),
        GearRow("Binoculars", "Mark enemies"),
        GearRow("Night vision / thermal", "Dark missions"),
        GearRow("Radio", "Call smoke, artillery, drone"),
        GearRow("Gas mask", "WW1 gas sequences"),
        GearRow("Backpack", "More ammo/grenade carry")
    )

    fun byItem(item: String): GearRow? =
        ROWS.firstOrNull { it.item.equals(item.trim(), ignoreCase = true) }

    fun tieredRows(): List<GearRow> = ROWS.filter { it.tiered }

    fun count(): Int = ROWS.size

    /** Section 5.5: the radio is the only row that places support calls. */
    fun radioRow(): GearRow = ROWS.first { it.item == "Radio" }
}

/**
 * Section 5.4: how a weapon behaves once it is in hand. These are the rules the
 * HUD and the shooting code both read, kept next to the gear they interact with.
 */
object WeaponHandling {

    const val HEAD_MULTIPLIER = 2.5f
    const val TORSO_MULTIPLIER = 1f
    const val LIMB_MULTIPLIER = 0.7f

    /** Section 5.4 "Fire modes": single, burst, auto - only where the gun supports it. */
    val FIRE_MODES: List<String> = listOf("Single", "Burst", "Auto")

    /** Section 5.4 "Hit zones", in the order the section prints them. */
    val HIT_ZONES: List<String> = listOf("Head", "Torso", "Limbs")

    /** Section 5.4: bullet travel time and drop on snipers, hitscan up close. */
    fun usesProjectile(sniperClass: Boolean): Boolean = sniperClass

    /**
     * Section 5.4: "Suppressors - Reduce enemy alert radius in stealth missions."
     * The document states the direction but no factor, so this reports the rule
     * rather than inventing a multiplier.
     */
    fun reducesAlertRadius(fittedSuppressor: Boolean): Boolean = fittedSuppressor

    /** Section 5.4: only WW1-era guns can jam, and only when the option is on. */
    fun canJam(eraWorldWarOne: Boolean, jamsEnabled: Boolean): Boolean =
        eraWorldWarOne && jamsEnabled
}
