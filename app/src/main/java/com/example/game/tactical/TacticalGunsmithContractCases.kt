package com.example.game.tactical

import com.example.game.tactical.gunsmith.AssemblyPart
import com.example.game.tactical.gunsmith.AssembledWeapon
import com.example.game.tactical.gunsmith.AttachmentCatalog
import com.example.game.tactical.gunsmith.AttachmentSlot
import com.example.game.tactical.gunsmith.BlueprintSource
import com.example.game.tactical.gunsmith.CraftCost
import com.example.game.tactical.gunsmith.MaterialStock
import com.example.game.tactical.gunsmith.PartSnapper
import com.example.game.tactical.gunsmith.PartSocket
import com.example.game.tactical.gunsmith.StockRack
import com.example.game.tactical.gunsmith.Vec3Like
import com.example.game.tactical.gunsmith.WeaponBase
import com.example.game.tactical.gunsmith.WeaponBlueprint
import com.example.game.tactical.gunsmith.WeaponStatCalculator
import com.example.game.tactical.gunsmith.Workbench
import com.example.game.tactical.gunsmith.WorkbenchCamera
import com.example.game.tactical.gunsmith.WorkbenchRig
import com.example.game.vanguard.CaseRecorder
import com.example.game.vanguard.ContractCase
import com.example.game.vanguard.approxEquals
import com.example.game.vanguard.core.errorCodeOrNull
import com.example.game.vanguard.core.valueOrNull
import com.example.game.vanguard.observed

/** Section 4 and Phase 4: the 6-slot catalogue, the stat formulas and the workbench. */
internal object TacticalGunsmithContractCases {

    private val base = WeaponBase(
        id = "AR-4",
        name = "AR-4 Sentinel",
        baseRecoil = 2f,
        baseAdsMs = 300f,
        baseDamage = 34f,
        effectiveRangeMeters = 100f,
        maxRangeMeters = 300f,
        fireRateRpm = 750f,
        mobility = 1f
    )

    private val blueprint = WeaponBlueprint(
        id = "BP-AR4",
        name = "AR-4 Sentinel",
        unlockRankIndex = 0,
        source = BlueprintSource.MISSION,
        cost = CraftCost(alloy = 10, polymer = 5, steel = 2, credits = 100)
    )

    private fun stockRifle(): AssembledWeapon = Workbench.assemble(
        blueprint,
        base,
        AssemblyPart.entries.toSet(),
        StockRack.STANDARD,
        AttachmentCatalog.DEFAULT
    ).valueOrNull()!!

    fun run(): List<ContractCase> = formulaCases() + catalogueCases() + workbenchCases()

    private fun formulaCases(): List<ContractCase> {
        val recorder = CaseRecorder(TacticalContractAudit.GROUP_GUNSMITH)

        recorder.check(
            "Effective recoil formula", "2 x (1 - 0.22 - 0.15 - 0.18) on a base of 10",
            approxEquals(WeaponStatCalculator.effectiveRecoil(10f, 0.22f, 0.15f, 0.18f), 4.5f),
            observed(WeaponStatCalculator.effectiveRecoil(10f, 0.22f, 0.15f, 0.18f), 4.5f)
        )
        recorder.check(
            "ADS weight sum", "300 ms base minus 90 barrel and 35 grip",
            approxEquals(WeaponStatCalculator.adsSpeedMs(300f, listOf(
                AttachmentCatalog.SHORT_CQB_BARREL,
                AttachmentCatalog.ANGLED_GRIP
            )), 175f),
            observed(WeaponStatCalculator.adsSpeedMs(300f, listOf(
                AttachmentCatalog.SHORT_CQB_BARREL,
                AttachmentCatalog.ANGLED_GRIP
            )), 175f)
        )
        recorder.check(
            "ADS floor", "never faster than 40 ms",
            approxEquals(WeaponStatCalculator.adsSpeedMs(10f, emptyList()), 40f),
            observed(WeaponStatCalculator.adsSpeedMs(10f, emptyList()), 40f)
        )
        recorder.check(
            "Default rifle stats", "80 m effective range, 30-round magazine",
            approxEquals(stockRifle().stats.effectiveRangeMeters, 80f) &&
                stockRifle().stats.magazineSize == 30,
            observed(stockRifle().stats.effectiveRangeMeters, 80f)
        )
        recorder.check(
            "Heavy stock", "15% recoil cut",
            approxEquals(
                Workbench.fitStock(stockRifle(), StockRack.HEAVY).stats.effectiveRecoil,
                1.7f
            ),
            observed(Workbench.fitStock(stockRifle(), StockRack.HEAVY).stats.effectiveRecoil, 1.7f)
        )
        return recorder.result()
    }

    private fun catalogueCases(): List<ContractCase> {
        val recorder = CaseRecorder(TacticalContractAudit.GROUP_GUNSMITH)

        recorder.check(
            "Six attachment slots", "Muzzle/Barrel/Optic/Underbarrel/Magazine/Ammunition",
            AttachmentSlot.entries.size == 6 &&
                AttachmentCatalog.optionsFor(AttachmentSlot.OPTIC).size == 4,
            observed(AttachmentSlot.entries.map { it.displayName }, "6 slots")
        )
        recorder.check(
            "Optic magnification", "red dot 1.5x, ACOG 4x, sniper 8x-12x",
            approxEquals(AttachmentCatalog.RED_DOT.zoomMax, 1.5f) &&
                approxEquals(AttachmentCatalog.ACOG.zoomMax, 4f) &&
                approxEquals(AttachmentCatalog.VARIABLE_SNIPER_SCOPE.zoomMin, 8f) &&
                approxEquals(AttachmentCatalog.VARIABLE_SNIPER_SCOPE.zoomMax, 12f),
            observed(AttachmentCatalog.VARIABLE_SNIPER_SCOPE.zoomMax, 12f)
        )
        recorder.check(
            "Magazines", "30 / 30 / 60 with a drum mobility cost",
            AttachmentCatalog.STANDARD_MAGAZINE.magazineSize == 30 &&
                AttachmentCatalog.FAST_MAGAZINE.magazineSize == 30 &&
                AttachmentCatalog.EXTENDED_DRUM.magazineSize == 60 &&
                AttachmentCatalog.EXTENDED_DRUM.mobilityMultiplier < 1f,
            observed(AttachmentCatalog.EXTENDED_DRUM.magazineSize, 60)
        )
        recorder.check(
            "Ammunition types", "AP ignores 50%, hollow point +25%",
            AttachmentCatalog.ARMOR_PIERCING.armorIgnorePercent == 50 &&
                AttachmentCatalog.HOLLOW_POINT.unarmoredDamageBonusPercent == 25,
            observed(AttachmentCatalog.ARMOR_PIERCING.armorIgnorePercent, 50)
        )
        recorder.check(
            "Suppressor", "silenced with an 18 m/s velocity penalty",
            AttachmentCatalog.SUPPRESSOR.suppressed &&
                approxEquals(AttachmentCatalog.SUPPRESSOR.muzzleVelocityPenaltyMps, 18f) &&
                AttachmentCatalog.SUPPRESSOR.hidesFlash,
            observed(AttachmentCatalog.SUPPRESSOR.muzzleVelocityPenaltyMps, 18f)
        )
        recorder.check(
            "Catalogue lookup", "18 attachments reachable by id",
            AttachmentCatalog.ALL.size == 18 &&
                AttachmentCatalog.byId("OPTIC_ACOG") == AttachmentCatalog.ACOG,
            observed(AttachmentCatalog.ALL.size, 18)
        )
        return recorder.result()
    }

    private fun workbenchCases(): List<ContractCase> {
        val recorder = CaseRecorder(TacticalContractAudit.GROUP_GUNSMITH)

        val incomplete = Workbench.assemble(
            blueprint,
            base,
            AssemblyPart.entries.toSet() - AssemblyPart.BOLT_CARRIER_GROUP,
            StockRack.STANDARD,
            AttachmentCatalog.DEFAULT
        )
        recorder.check(
            "Missing part", "a rifle without its BCG cannot assemble",
            incomplete.errorCodeOrNull() == "MISSING_PART",
            observed(incomplete.errorCodeOrNull(), "MISSING_PART")
        )
        val wrongSlot = Workbench.assemble(
            blueprint,
            base,
            AssemblyPart.entries.toSet(),
            StockRack.STANDARD,
            mapOf(AttachmentSlot.MUZZLE to AttachmentCatalog.RED_DOT)
        )
        recorder.check(
            "Wrong slot", "an optic cannot sit in the muzzle slot",
            wrongSlot.errorCodeOrNull() == "WRONG_SLOT",
            observed(wrongSlot.errorCodeOrNull(), "WRONG_SLOT")
        )
        val opticFitted = Workbench.equip(stockRifle(), AttachmentCatalog.RED_DOT)
        recorder.check(
            "Stat recalculation", "red dot raises zoom to 1.5x immediately",
            approxEquals(opticFitted.stats.zoomMax, 1.5f),
            observed(opticFitted.stats.zoomMax, 1.5f)
        )
        val drumFitted = Workbench.equip(stockRifle(), AttachmentCatalog.EXTENDED_DRUM)
        recorder.check(
            "Drum magazine", "60 rounds and a mobility penalty",
            drumFitted.stats.magazineSize == 60 && drumFitted.stats.mobility < 1f,
            observed(drumFitted.stats.magazineSize, 60)
        )
        val suppressed = Workbench.equip(stockRifle(), AttachmentCatalog.SUPPRESSOR)
        recorder.check(
            "Suppressor stat feed", "862 m/s muzzle velocity, suppressed flag",
            suppressed.stats.isSuppressed &&
                approxEquals(suppressed.stats.muzzleVelocityMps, 862f),
            observed(suppressed.stats.muzzleVelocityMps, 862f)
        )
        recorder.check(
            "Blueprint rank gate", "BLUEPRINT_LOCKED above the operator's rank",
            Workbench.craft(
                blueprint.copy(unlockRankIndex = 5),
                MaterialStock(99, 99, 99, 999),
                0
            ).errorCodeOrNull() == "BLUEPRINT_LOCKED",
            observed("BLUEPRINT_LOCKED", "BLUEPRINT_LOCKED")
        )
        recorder.check(
            "Material spend", "INSUFFICIENT_MATERIALS on an empty bench",
            Workbench.craft(blueprint, MaterialStock(0, 0, 0, 0), 0).errorCodeOrNull() ==
                "INSUFFICIENT_MATERIALS",
            observed(
                Workbench.craft(blueprint, MaterialStock(0, 0, 0, 0), 0).errorCodeOrNull(),
                "INSUFFICIENT_MATERIALS"
            )
        )
        val crafted = Workbench.craft(blueprint, MaterialStock(50, 50, 50, 500), 0).valueOrNull()
        recorder.check(
            "Craft cost", "10 alloy, 5 polymer, 2 steel, 100 credits",
            crafted != null && crafted.alloy == 40 && crafted.polymer == 45 &&
                crafted.steel == 48 && crafted.credits == 400,
            observed(crafted?.alloy, 40)
        )
        val socket = PartSocket(AssemblyPart.BARREL, Vec3Like(0f, 0f, 0f))
        recorder.check(
            "Part snapping", "10 mm drop seats, 50 mm drop refuses",
            PartSnapper.snap(AssemblyPart.BARREL, Vec3Like(0.01f, 0f, 0f), listOf(socket)).accepted &&
                !PartSnapper.snap(AssemblyPart.BARREL, Vec3Like(0.05f, 0f, 0f), listOf(socket)).accepted,
            observed(
                PartSnapper.snap(AssemblyPart.BARREL, Vec3Like(0.05f, 0f, 0f), listOf(socket)).distanceMeters,
                0.05f
            )
        )
        recorder.check(
            "Workbench camera limits", "zoom clamps at 1.80 m, pitch at 70 deg",
            approxEquals(WorkbenchRig.zoom(WorkbenchCamera(), 5f).zoomMeters, 1.80f) &&
                approxEquals(WorkbenchRig.orbit(WorkbenchCamera(), 0f, 1_000f).pitchDegrees, 70f),
            observed(WorkbenchRig.zoom(WorkbenchCamera(), 5f).zoomMeters, 1.80f)
        )
        return recorder.result()
    }
}
