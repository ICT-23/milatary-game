package com.example.game.vanguard.gunsmith

/**
 * Ready-made assemblies used by the workbench and the contract audit. Each one
 * fills all seven sockets from [VanguardArmory], so a player can start from a
 * complete, fireable weapon instead of hunting for parts to make one work.
 */
object VanguardLoadouts {

    val ALL_BARRELS: List<BarrelSpec> = listOf(
        VanguardArmory.BARREL_9MM_STANDARD,
        VanguardArmory.BARREL_45ACP_STANDARD,
        VanguardArmory.BARREL_556_STANDARD,
        VanguardArmory.BARREL_308_STANDARD,
        VanguardArmory.BARREL_308_MAGNUM,
        VanguardArmory.BARREL_300WM_MAGNUM,
        VanguardArmory.BARREL_12G_STANDARD
    )

    /** The precision build the gunsmith lesson centres on. */
    val PRECISION_308: WeaponLoadout = WeaponLoadout(
        chassis = VanguardArmory.CHASSIS_AICS_AX,
        barrel = VanguardArmory.BARREL_308_MAGNUM,
        bolt = VanguardArmory.BOLT_BRX1_LINEAR,
        trigger = VanguardArmory.TRIGGER_STAGE_3,
        optic = VanguardArmory.OPTIC_FFP_5_25,
        muzzle = VanguardArmory.MUZZLE_SUPPRESSOR,
        stock = VanguardArmory.STOCK_KICKOFF_MEGA,
        magazine = VanguardArmory.MAGAZINE_308_10
    )

    /** The general-purpose carbine issued at the academy range. */
    val CARBINE_556: WeaponLoadout = WeaponLoadout(
        chassis = VanguardArmory.CHASSIS_TRI_ALLOY,
        barrel = VanguardArmory.BARREL_556_STANDARD,
        bolt = VanguardArmory.BOLT_STANDARD_CARBINE,
        trigger = VanguardArmory.TRIGGER_STAGE_2,
        optic = VanguardArmory.OPTIC_FFP_1_6,
        muzzle = VanguardArmory.MUZZLE_FTE_BRAKE,
        stock = VanguardArmory.STOCK_MICROCORE_365,
        magazine = VanguardArmory.MAGAZINE_556_30
    )

    fun barrelById(id: String): BarrelSpec? = ALL_BARRELS.firstOrNull { it.id == id }

    /** The calibre swap the specification calls out: barrel plus bolt head. */
    fun boltFor(caliber: Caliber): BoltSpec? {
        val candidates = listOf(
            VanguardArmory.BOLT_STANDARD_CARBINE,
            VanguardArmory.BOLT_STANDARD_PRECISION,
            VanguardArmory.BOLT_BRX1_LINEAR
        )
        return candidates.firstOrNull { caliber in it.supportedCalibers }
    }
}
