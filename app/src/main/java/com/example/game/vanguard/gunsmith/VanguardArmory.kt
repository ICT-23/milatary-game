package com.example.game.vanguard.gunsmith

/**
 * The workbench inventory from Section 3.2. Every entry names a real socket, so
 * a part can never be fitted where it does not belong.
 *
 * All performance figures are game data derived from the specification's
 * mechanical descriptions; nothing here claims real-world ballistics.
 */
data class WeaponLoadout(
    val chassis: ReceiverChassis,
    val barrel: BarrelSpec,
    val bolt: BoltSpec,
    val trigger: TriggerSpec,
    val optic: OpticSpec,
    val muzzle: MuzzleSpec,
    val stock: StockSpec,
    val magazine: MagazineSpec
) {
    /** Seven sockets, seven parts: completeness is enforced by the type itself. */
    val socketCount: Int get() = PartSocket.entries.size

    fun withBarrel(next: BarrelSpec): WeaponLoadout = copy(barrel = next)
}

object VanguardArmory {

    val CHASSIS_AICS_AX = ReceiverChassis(
        id = "CHASSIS_AICS_AX",
        name = "AICS AX Match Chassis",
        material = ChassisMaterial.TITANIUM_MATCH,
        beddingGeometryRating = 0.98f,
        weightKg = 1.60f,
        receiverLengthMm = 240f
    )

    val CHASSIS_TRI_ALLOY = ReceiverChassis(
        id = "CHASSIS_TRI_ALLOY",
        name = "Tri-Alloy Field Chassis",
        material = ChassisMaterial.STANDARD_ALLOY,
        beddingGeometryRating = 0.86f,
        weightKg = 1.38f,
        receiverLengthMm = 235f
    )

    // ------------------------------------------------------------- barrels

    val BARREL_9MM_STANDARD = BarrelSpec(
        id = "BARREL_9MM_57",
        caliber = Caliber.NINE_NINETEEN,
        lengthCm = 57f,
        profileMm = 16f,
        twistInches = "1:10",
        muzzleThread = "M14",
        weightKg = 0.68f,
        expectedMoa = 2.6f
    )

    val BARREL_45ACP_STANDARD = BARREL_9MM_STANDARD.copy(
        id = "BARREL_45ACP_57",
        caliber = Caliber.FORTY_FIVE_ACP,
        weightKg = 0.72f,
        expectedMoa = 2.9f
    )

    val BARREL_556_STANDARD = BARREL_9MM_STANDARD.copy(
        id = "BARREL_556_57",
        caliber = Caliber.FIVE_FIVE_SIX,
        twistInches = "1:7",
        weightKg = 0.94f,
        expectedMoa = 1.35f
    )

    val BARREL_308_STANDARD = BARREL_9MM_STANDARD.copy(
        id = "BARREL_308_57",
        caliber = Caliber.THREE_ZERO_EIGHT,
        twistInches = "1:11",
        weightKg = 1.10f,
        expectedMoa = 0.95f
    )

    val BARREL_308_MAGNUM = BARREL_9MM_STANDARD.copy(
        id = "BARREL_308_62",
        caliber = Caliber.THREE_ZERO_EIGHT,
        lengthCm = 62f,
        twistInches = "1:11",
        weightKg = 1.22f,
        expectedMoa = 0.85f
    )

    val BARREL_300WM_MAGNUM = BARREL_9MM_STANDARD.copy(
        id = "BARREL_300WM_62",
        caliber = Caliber.THREE_HUNDRED_WIN_MAG,
        lengthCm = 62f,
        twistInches = "1:10",
        weightKg = 1.28f,
        expectedMoa = 0.90f
    )

    val BARREL_12G_STANDARD = BARREL_9MM_STANDARD.copy(
        id = "BARREL_12G_57",
        caliber = Caliber.TWELVE_GAUGE,
        twistInches = "Smoothbore",
        weightKg = 1.05f,
        expectedMoa = 6.0f
    )

    // ---------------------------------------------------- bolt assemblies

    val BOLT_STANDARD_CARBINE = BoltSpec(
        id = "BOLT_STANDARD_CARBINE",
        name = "Standard Rotating Bolt",
        type = BoltType.STANDARD_ROTATING,
        handleDegrees = 90,
        reversibleEjection = false,
        zeroJamRapidCycle = false,
        supportedCalibers = setOf(
            Caliber.NINE_NINETEEN,
            Caliber.FORTY_FIVE_ACP,
            Caliber.FIVE_FIVE_SIX,
            Caliber.TWELVE_GAUGE
        )
    )

    val BOLT_STANDARD_PRECISION = BOLT_STANDARD_CARBINE.copy(
        id = "BOLT_STANDARD_PRECISION",
        supportedCalibers = setOf(
            Caliber.THREE_ZERO_EIGHT,
            Caliber.THREE_HUNDRED_WIN_MAG
        )
    )

    val BOLT_BRX1_LINEAR = BoltSpec(
        id = "BOLT_BRX1_LINEAR",
        name = "BRX1 Linear Bolt",
        type = BoltType.BRX1_LINEAR,
        handleDegrees = 45,
        reversibleEjection = true,
        zeroJamRapidCycle = true,
        supportedCalibers = setOf(
            Caliber.FIVE_FIVE_SIX,
            Caliber.THREE_ZERO_EIGHT,
            Caliber.THREE_HUNDRED_WIN_MAG
        )
    )

    // ------------------------------------------------------ trigger groups

    val TRIGGER_STAGE_1 = TriggerSpec(
        id = "TRIGGER_900G",
        name = "Stage 1 - 900 g",
        pullWeightGrams = 900,
        inputDelayMs = 18f,
        jerkMoa = 0.35f
    )

    val TRIGGER_STAGE_2 = TriggerSpec(
        id = "TRIGGER_1200G",
        name = "Stage 2 - 1200 g",
        pullWeightGrams = 1_200,
        inputDelayMs = 22f,
        jerkMoa = 0.20f
    )

    val TRIGGER_STAGE_3 = TriggerSpec(
        id = "TRIGGER_1500G",
        name = "Stage 3 - 1500 g",
        pullWeightGrams = 1_500,
        inputDelayMs = 28f,
        jerkMoa = 0.10f
    )

    // ---------------------------------------------------------------- optics

    val OPTIC_SFP_5_25 = OpticSpec(
        id = "OPTIC_SFP_5_25",
        name = "5-25x SFP MOA",
        plane = ReticlePlane.SFP,
        minZoom = 5f,
        maxZoom = 25f,
        moaSubtenseAtMaxZoom = 1f,
        parallaxMinMeters = 10f,
        hasZeroStop = false,
        weightKg = 0.78f
    )

    val OPTIC_FFP_5_25 = OPTIC_SFP_5_25.copy(
        id = "OPTIC_FFP_5_25",
        name = "5-25x FFP MRAD",
        plane = ReticlePlane.FFP,
        hasZeroStop = true,
        weightKg = 0.86f
    )

    val OPTIC_FFP_1_6 = OpticSpec(
        id = "OPTIC_FFP_1_6",
        name = "1-6x FFP LPVO",
        plane = ReticlePlane.FFP,
        minZoom = 1f,
        maxZoom = 6f,
        moaSubtenseAtMaxZoom = 2f,
        parallaxMinMeters = 10f,
        hasZeroStop = true,
        weightKg = 0.52f
    )

    // ---------------------------------------------------------------- muzzles

    val MUZZLE_THREAD_PROTECTOR = MuzzleSpec(
        id = "MUZZLE_THREAD_PROTECTOR",
        name = "M14 Thread Protector",
        kind = MuzzleDeviceKind.THREAD_PROTECTOR,
        threadPattern = "M14",
        hidesFlash = false,
        smokeParticleAlphaCap = 1f,
        acousticRadiusPercent = 100f,
        recoilReductionPercent = 0f,
        firstShotPopDecibels = 0f,
        weightKg = 0.04f
    )

    val MUZZLE_FTE_BRAKE = MUZZLE_THREAD_PROTECTOR.copy(
        id = "MUZZLE_FTE_BRAKE",
        name = "FTE Muzzle Brake",
        kind = MuzzleDeviceKind.FTE_MUZZLE_BRAKE,
        acousticRadiusPercent = 115f,
        recoilReductionPercent = 18f,
        weightKg = 0.13f
    )

    val MUZZLE_SUPPRESSOR = MUZZLE_THREAD_PROTECTOR.copy(
        id = "MUZZLE_SUPPRESSOR_M14",
        name = "M14 Sound Suppressor",
        kind = MuzzleDeviceKind.SOUND_SUPPRESSOR,
        hidesFlash = true,
        smokeParticleAlphaCap = 0.35f,
        acousticRadiusPercent = 45f,
        recoilReductionPercent = 5f,
        firstShotPopDecibels = 6f,
        weightKg = 0.42f
    )

    // ----------------------------------------------------------------- stocks

    val STOCK_MICROCORE_365 = StockSpec(
        id = "STOCK_MICROCORE_365",
        name = "MicroCore 365 mm",
        lengthOfPullMm = StockSpec.BASE_LENGTH_OF_PULL_MM,
        spacerMm = 0f,
        kickOff = KickOffBuffer.NONE,
        microCorePad = true,
        weightKg = 0.72f
    )

    val STOCK_KICKOFF_PLUS = STOCK_MICROCORE_365.copy(
        id = "STOCK_KICKOFF_PLUS",
        name = "MicroCore + Kick-Off Plus",
        spacerMm = StockSpec.SPACER_MM,
        kickOff = KickOffBuffer.KICK_OFF_PLUS,
        weightKg = 0.88f
    )

    val STOCK_KICKOFF_MEGA = STOCK_MICROCORE_365.copy(
        id = "STOCK_KICKOFF_MEGA",
        name = "MicroCore + Kick-Off Mega",
        spacerMm = StockSpec.SPACER_MM,
        kickOff = KickOffBuffer.KICK_OFF_MEGA,
        weightKg = 0.96f
    )

    // ------------------------------------------------------------- magazines

    val MAGAZINE_308_10 = MagazineSpec(id = "MAG_308_10", name = "10-round .308", capacity = 10)

    val MAGAZINE_308_20 = MagazineSpec(id = "MAG_308_20", name = "20-round .308", capacity = 20)

    val MAGAZINE_556_30 = MagazineSpec(id = "MAG_556_30", name = "30-round 5.56", capacity = 30)

    val MAGAZINE_PISTOL_15 = MagazineSpec(id = "MAG_PISTOL_15", name = "15-round pistol", capacity = 15)
}
