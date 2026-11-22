package com.example.game.valor.balance

import com.example.game.valor.art.WeaponClass

/** The era column of the weapon tables. */
enum class WeaponEra(val displayName: String) {
    ALL("All"),
    WW1("WW1"),
    WW1_WW2("WW1/2"),
    WW2("WW2"),
    COLD_WAR("Cold War"),
    MODERN("Modern")
}

/**
 * Section 2: one row of the weapon tables, in table order.
 *
 * [damage] is per bullet or pellet against the torso; [rangeMeters] is the
 * distance at which the class reaches its [DamageFalloff] floor; [targetStk] is
 * the "STK (body)" column and is null where the table prints prose instead.
 * Recoil is null for the sniper and launcher rows, which carry no RecV column.
 */
data class WeaponSpec(
    val id: String,
    val name: String,
    val weaponClass: WeaponClass,
    val era: WeaponEra,
    val damage: Float,
    val pellets: Int,
    val rpm: Float,
    val rangeMeters: Float,
    val accuracy: Float,
    val recoilVerticalDeg: Float?,
    val reloadSeconds: Float,
    val magSize: Int,
    val reserveAmmo: Int,
    val unlockRank: Int,
    val price: Int,
    val targetStk: Int?,
    val reloadPerShell: Boolean = false
) {
    val shotDamage: Float get() = DamageMath.shotDamage(damage, pellets)

    /** Body shots the numbers actually produce against a 100 HP enemy. */
    val derivedStk: Int get() = DamageMath.bodyShotsToKill(shotDamage)

    val minDamageMult: Float get() = DamageMath.minDamageMultFor(weaponClass)
}

/**
 * Section 2.5: melee and throwables, which carry no cyclic rate or magazine.
 * Unused fields stay null rather than being filled with a guess.
 */
data class FieldItemSpec(
    val id: String,
    val name: String,
    val damage: Float?,
    val radiusMeters: Float?,
    val fuseSeconds: Float?,
    val durationSeconds: Float?,
    val note: String,
    val unlockRank: Int,
    val price: Int,
    val pricedEach: Boolean
)

object WeaponCatalog {

    private fun weapon(
        id: String,
        name: String,
        weaponClass: WeaponClass,
        era: WeaponEra,
        damage: Float,
        rpm: Float,
        rangeMeters: Float,
        accuracy: Float,
        recoilVertical: Float?,
        reloadSeconds: Float,
        magSize: Int,
        reserveAmmo: Int,
        unlockRank: Int,
        price: Int,
        targetStk: Int?,
        pellets: Int = 1,
        reloadPerShell: Boolean = false
    ) = WeaponSpec(
        id, name, weaponClass, era, damage, pellets, rpm, rangeMeters, accuracy,
        recoilVertical, reloadSeconds, magSize, reserveAmmo, unlockRank, price,
        targetStk, reloadPerShell
    )

    val WEAPONS: List<WeaponSpec> = listOf(
        // Section 2.1 - Pistols
        weapon("pistol_mk1", "Service Pistol Mk1", WeaponClass.PISTOL, WeaponEra.ALL, 34f,
            300f, 35f, 0.75f, 1.0f, 1.5f, 7, 49, 1, 0, 3),
        weapon("revolver_frontier", "Frontier Revolver", WeaponClass.PISTOL, WeaponEra.WW1, 55f,
            150f, 40f, 0.80f, 2.0f, 3.0f, 6, 36, 3, 800, 2),
        weapon("pistol_9mm", "Sidearm 9mm", WeaponClass.PISTOL, WeaponEra.MODERN, 28f,
            420f, 30f, 0.72f, 0.9f, 1.4f, 15, 75, 9, 2_500, 4),
        weapon("pistol_auto", "Machine Pistol \"Wasp\"", WeaponClass.PISTOL, WeaponEra.MODERN, 18f,
            900f, 20f, 0.55f, 1.1f, 1.7f, 20, 100, 13, 5_500, 6),

        // Section 2.2 - Rifles. The Design doc's rank-6 Vanguard example was a
        // placeholder; rank 11 is the authoritative value for a new build.
        weapon("bolt_pioneer1", "Pioneer Bolt Rifle", WeaponClass.RIFLE, WeaponEra.WW1, 85f,
            45f, 90f, 0.90f, 2.5f, 3.2f, 5, 40, 1, 0, 2),
        weapon("bolt_pioneer2", "Pioneer Mk2", WeaponClass.RIFLE, WeaponEra.WW1_WW2, 95f,
            55f, 100f, 0.90f, 2.4f, 2.9f, 5, 40, 4, 3_000, 2),
        weapon("semi_liberty", "Liberty Battle Rifle", WeaponClass.RIFLE, WeaponEra.WW2, 48f,
            240f, 70f, 0.80f, 1.6f, 2.6f, 8, 64, 6, 4_500, 3),
        weapon("ar_vanguard", "AR Vanguard", WeaponClass.RIFLE, WeaponEra.COLD_WAR, 28f,
            650f, 60f, 0.78f, 1.2f, 2.1f, 30, 180, 11, 9_000, 4),
        weapon("carbine_raptor", "Carbine \"Raptor\"", WeaponClass.RIFLE, WeaponEra.MODERN, 25f,
            700f, 45f, 0.80f, 0.9f, 2.0f, 30, 180, 12, 10_000, 4),
        weapon("ar_sentinel", "AR Sentinel (heavy)", WeaponClass.RIFLE, WeaponEra.MODERN, 33f,
            550f, 70f, 0.82f, 1.4f, 2.3f, 30, 150, 14, 16_000, 3),
        weapon("dmr_hawk", "DMR \"Hawk\"", WeaponClass.RIFLE, WeaponEra.MODERN, 62f,
            200f, 120f, 0.90f, 1.8f, 2.4f, 10, 60, 15, 22_000, 2),

        // Section 2.3 - SMGs, LMGs, Shotguns
        weapon("smg_trench", "Trench Broom SMG", WeaponClass.SMG, WeaponEra.WW1, 24f,
            600f, 25f, 0.65f, 0.9f, 2.2f, 32, 192, 4, 2_800, 5),
        weapon("smg_hornet", "Hornet SMG", WeaponClass.SMG, WeaponEra.WW2, 22f,
            750f, 28f, 0.68f, 0.8f, 2.0f, 30, 180, 7, 4_000, 5),
        weapon("smg_viper", "Viper SMG (suppressed)", WeaponClass.SMG, WeaponEra.MODERN, 21f,
            800f, 30f, 0.72f, 0.7f, 1.8f, 30, 180, 13, 9_500, 5),
        weapon("lmg_anvil", "Machine Gun \"Anvil\"", WeaponClass.LMG, WeaponEra.WW1, 30f,
            500f, 70f, 0.60f, 1.5f, 5.5f, 100, 400, 7, 7_000, 4),
        weapon("lmg_titanbelt", "Belt-fed LMG \"Bulwark\"", WeaponClass.LMG, WeaponEra.MODERN, 32f,
            700f, 80f, 0.62f, 1.3f, 6.0f, 150, 450, 16, 25_000, 4),
        weapon("sg_breaker", "Pump Shotgun \"Breaker\"", WeaponClass.SHOTGUN, WeaponEra.ALL, 9f,
            70f, 18f, 0.55f, 3.5f, 0.55f, 6, 36, 5, 3_500, 2, pellets = 8, reloadPerShell = true),
        weapon("sg_auto", "Auto Shotgun \"Hurricane\"", WeaponClass.SHOTGUN, WeaponEra.MODERN, 8f,
            180f, 15f, 0.50f, 3.0f, 3.2f, 10, 50, 13, 12_000, 2, pellets = 8),

        // Section 2.4 - Snipers and launcher: no RecV column and no numeric STK.
        weapon("sniper_longwatch", "Sniper \"Longwatch\"", WeaponClass.SNIPER, WeaponEra.WW2, 140f,
            40f, 200f, 0.97f, null, 3.6f, 5, 30, 8, 9_000, null),
        weapon("sniper_titan", "Anti-Material \"Titan\"", WeaponClass.SNIPER, WeaponEra.MODERN, 300f,
            30f, 250f, 0.98f, null, 4.5f, 5, 25, 17, 40_000, null),
        weapon("launcher_hammer", "Rocket Launcher \"Hammer\"", WeaponClass.LAUNCHER, WeaponEra.MODERN, 250f,
            20f, 120f, 0.85f, null, 4.5f, 1, 6, 12, 20_000, null)
    )

    val FIELD_ITEMS: List<FieldItemSpec> = listOf(
        FieldItemSpec("knife_trench", "Trench Knife", 60f, null, null, null,
            "Instant kill from behind (stealth); silent; 0.6 s swing", 1, 0, false),
        FieldItemSpec("knife_combat", "Combat Knife", 70f, null, null, null,
            "Faster swing, 0.45 s", 8, 1_500, false),
        FieldItemSpec("grenade_frag", "Frag Grenade", 150f, 7f, 3.0f, null,
            "Falloff exponent 1.5", 2, 100, true),
        FieldItemSpec("grenade_smoke", "Smoke Grenade", null, 6f, null, 12f,
            "Cloud blocks AI vision", 5, 80, true),
        FieldItemSpec("grenade_flash", "Flash Grenade", null, null, null, 4f,
            "Blinds enemies inside the view cone", 9, 120, true),
        FieldItemSpec("grenade_gas", "Gas Grenade (WW1)", 12f, 6f, null, 8f,
            "Damage per second for 8 s; gas mask negates 80%", 4, 100, true),
        FieldItemSpec("claymore_mine", "Proximity Mine", 200f, 5f, null, null,
            "Triggers on an enemy within 3 m", 10, 250, true)
    )

    fun byId(id: String): WeaponSpec? = WEAPONS.firstOrNull { it.id == id }

    fun ofClass(weaponClass: WeaponClass): List<WeaponSpec> =
        WEAPONS.filter { it.weaponClass == weaponClass }

    fun unlockedAt(rank: Int): List<WeaponSpec> = WEAPONS.filter { it.unlockRank <= rank }

    /**
     * Section 1 frames the STK column as a target, so the catalogue reports where
     * the authored damage misses it instead of silently rounding the mismatch.
     */
    fun stkGaps(): List<WeaponSpec> =
        WEAPONS.filter { spec -> spec.targetStk != null && spec.targetStk != spec.derivedStk }

    /** Weapons priced above zero, used by the gunsmith cost rule. */
    val PURCHASABLE: List<WeaponSpec> get() = WEAPONS.filter { it.price > 0 }
}
