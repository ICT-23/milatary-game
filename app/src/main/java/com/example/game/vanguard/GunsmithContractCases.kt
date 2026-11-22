package com.example.game.vanguard

import com.example.game.vanguard.core.errorCodeOrNull
import com.example.game.vanguard.core.valueOrNull
import com.example.game.vanguard.gunsmith.BallisticStatAggregator
import com.example.game.vanguard.gunsmith.BoltType
import com.example.game.vanguard.gunsmith.Caliber
import com.example.game.vanguard.gunsmith.KickOffBuffer
import com.example.game.vanguard.gunsmith.PartSocket
import com.example.game.vanguard.gunsmith.ReticlePlane
import com.example.game.vanguard.gunsmith.StockSpec
import com.example.game.vanguard.gunsmith.TriggerSpec
import com.example.game.vanguard.gunsmith.VanguardArmory
import com.example.game.vanguard.gunsmith.VanguardLoadouts

/**
 * Section 3.2: the seven sockets and the numbers each part must carry. A part
 * that cannot meet the written figure is a failure, not a rounding choice.
 */
object GunsmithContractCases {

    fun run(): List<ContractCase> {
        val cases = CaseRecorder(VanguardContractAudit.GROUP_GUNSMITH)
        checkSockets(cases)
        checkParts(cases)
        checkAssembly(cases)
        return cases.result()
    }

    private fun checkSockets(cases: CaseRecorder) {
        cases.check(
            title = "The workbench exposes exactly seven sockets",
            expectation = "7 sockets",
            passed = PartSocket.entries.size == 7,
            detail = observed(PartSocket.entries.size, 7)
        )

        val calibres = Caliber.entries.map { it.label }
        cases.check(
            title = "All six specified calibres are available",
            expectation = "9x19, .45ACP, 5.56x45, .308Win, .300WinMag, 12 Gauge",
            passed = calibres == listOf("9x19mm", ".45ACP", "5.56x45mm", ".308Win", ".300WinMag", "12 Gauge"),
            detail = observed(calibres, "six calibres")
        )

        val lengths = VanguardLoadouts.ALL_BARRELS.map { it.lengthCm }.toSet()
        val profiles = VanguardLoadouts.ALL_BARRELS.map { it.profileMm }.toSet()
        cases.check(
            title = "Barrels come in 57 cm and 62 cm with a 16 mm profile",
            expectation = "lengths {57, 62}, profile 16 mm everywhere",
            passed = lengths == setOf(57f, 62f) && profiles == setOf(16f),
            detail = "lengths=$lengths profiles=$profiles"
        )

        cases.check(
            title = "Every barrel carries the M14 muzzle thread",
            expectation = "M14 on all seven barrels",
            passed = VanguardLoadouts.ALL_BARRELS.all { it.muzzleThread.equals("M14", ignoreCase = true) },
            detail = observed(VanguardLoadouts.ALL_BARRELS.map { it.muzzleThread }.toSet(), "M14")
        )
    }

    private fun checkParts(cases: CaseRecorder) {
        val brx1 = VanguardArmory.BOLT_BRX1_LINEAR
        cases.check(
            title = "The BRX1 is a 45 deg linear bolt with reversible ejection",
            expectation = "45 deg handle, reversible, zero-jam, 780 rpm",
            passed = brx1.handleDegrees == 45 && brx1.reversibleEjection && brx1.zeroJamRapidCycle &&
                brx1.type.cycleRateRpm == 780f,
            detail = "handle=${brx1.handleDegrees} reversible=${brx1.reversibleEjection} rpm=${brx1.type.cycleRateRpm}"
        )

        cases.check(
            title = "The standard rotating bolt cycles slower than the linear bolt",
            expectation = "660 rpm against 780 rpm",
            passed = BoltType.STANDARD_ROTATING.cycleRateRpm == 660f &&
                BoltType.BRX1_LINEAR.cycleRateRpm == 780f,
            detail = "standard=${BoltType.STANDARD_ROTATING.cycleRateRpm} linear=${BoltType.BRX1_LINEAR.cycleRateRpm}"
        )

        val triggers = listOf(
            VanguardArmory.TRIGGER_STAGE_1,
            VanguardArmory.TRIGGER_STAGE_2,
            VanguardArmory.TRIGGER_STAGE_3
        )
        cases.check(
            title = "All three trigger stages sit inside the 900-1500 g window",
            expectation = "900 g, 1200 g, 1500 g all in spec",
            passed = triggers.map { it.pullWeightGrams } == listOf(900, 1_200, 1_500) &&
                triggers.all { it.isWithinSpec } &&
                TriggerSpec.MIN_GRAMS == 900 && TriggerSpec.MAX_GRAMS == 1_500,
            detail = "weights=${triggers.map { it.pullWeightGrams }}"
        )

        val sfp = VanguardArmory.OPTIC_SFP_5_25
        cases.check(
            title = "An SFP reticle is exact at 25x and doubles at 12.5x",
            expectation = "1.0 MOA at 25x, 2.0 MOA at 12.5x",
            passed = sfp.plane == ReticlePlane.SFP &&
                approxEquals(sfp.moaSubtenseAt(25f), 1f) &&
                approxEquals(sfp.moaSubtenseAt(12.5f), 2f),
            detail = "at25x=${sfp.moaSubtenseAt(25f)} at12.5x=${sfp.moaSubtenseAt(12.5f)}"
        )

        val ffp = VanguardArmory.OPTIC_FFP_5_25
        cases.check(
            title = "An FFP reticle holds constant subtense across 5-25x",
            expectation = "1.0 MOA at both 5x and 25x",
            passed = ffp.plane == ReticlePlane.FFP &&
                approxEquals(ffp.moaSubtenseAt(5f), 1f) &&
                approxEquals(ffp.moaSubtenseAt(25f), 1f),
            detail = "at5x=${ffp.moaSubtenseAt(5f)} at25x=${ffp.moaSubtenseAt(25f)}"
        )

        cases.check(
            title = "Parallax adjustment runs from 10 m to infinity",
            expectation = "invalid at 9.9 m, valid from 10 m",
            passed = !ffp.parallaxValidAt(9.9f) && ffp.parallaxValidAt(10f) && ffp.hasZeroStop,
            detail = "at9.9=${ffp.parallaxValidAt(9.9f)} at10=${ffp.parallaxValidAt(10f)} zeroStop=${ffp.hasZeroStop}"
        )

        val suppressor = VanguardArmory.MUZZLE_SUPPRESSOR
        cases.check(
            title = "The suppressor hides flash, caps smoke and models the first-shot pop",
            expectation = "flash hidden, smoke alpha 0.35, 6 dB pop, 45% acoustic radius",
            passed = suppressor.hidesFlash &&
                approxEquals(suppressor.smokeParticleAlphaCap, 0.35f) &&
                approxEquals(suppressor.firstShotPopDecibels, 6f) &&
                approxEquals(suppressor.acousticRadiusPercent, 45f),
            detail = "alpha=${suppressor.smokeParticleAlphaCap} pop=${suppressor.firstShotPopDecibels} radius=${suppressor.acousticRadiusPercent}"
        )

        cases.check(
            title = "Length of pull is 365 mm plus 12.7 mm spacers",
            expectation = "365.0 mm bare, 377.7 mm with one spacer",
            passed = approxEquals(StockSpec.BASE_LENGTH_OF_PULL_MM, 365f) &&
                approxEquals(StockSpec.SPACER_MM, 12.7f) &&
                approxEquals(VanguardArmory.STOCK_MICROCORE_365.totalLengthOfPullMm, 365f) &&
                approxEquals(VanguardArmory.STOCK_KICKOFF_MEGA.totalLengthOfPullMm, 377.7f),
            detail = "base=${VanguardArmory.STOCK_MICROCORE_365.totalLengthOfPullMm} mega=${VanguardArmory.STOCK_KICKOFF_MEGA.totalLengthOfPullMm}"
        )

        cases.check(
            title = "Kick-off buffers stop at 70% reduction",
            expectation = "0 / 25 / 45 / 70 percent",
            passed = listOf(
                KickOffBuffer.NONE,
                KickOffBuffer.KICK_OFF_I,
                KickOffBuffer.KICK_OFF_PLUS,
                KickOffBuffer.KICK_OFF_MEGA
            ).map { it.reduction } == listOf(0f, 0.25f, 0.45f, 0.70f),
            detail = observed(KickOffBuffer.entries.map { it.reduction }, listOf(0f, 0.25f, 0.45f, 0.70f))
        )
    }

    private fun checkAssembly(cases: CaseRecorder) {
        val precision = BallisticStatAggregator.assemble(VanguardLoadouts.PRECISION_308).valueOrNull()
        cases.check(
            title = "The precision build assembles into a fireable weapon",
            expectation = "mass 5.41 kg, OAL 1237.7 mm, 0.95 MOA, 780 rpm",
            passed = precision != null &&
                approxEquals(precision.massKg, 5.41f) &&
                approxEquals(precision.overallLengthMm, 1237.7f, 0.1f) &&
                approxEquals(precision.mechanicalMoa, 0.95f) &&
                precision.hudRpm == 780,
            detail = "mass=${precision?.massKg} oal=${precision?.overallLengthMm} moa=${precision?.mechanicalMoa} rpm=${precision?.hudRpm}"
        )

        cases.check(
            title = "The assembled precision build reports its muzzle behaviour",
            expectation = "flash hidden, smoke 0.35, 10-round magazine",
            passed = precision != null && precision.hidesFlash &&
                approxEquals(precision.smokeParticleAlphaCap, 0.35f) &&
                precision.magazineCapacity == 10,
            detail = "flash=${precision?.hidesFlash} alpha=${precision?.smokeParticleAlphaCap} capacity=${precision?.magazineCapacity}"
        )

        val wrongBolt = BallisticStatAggregator.assemble(
            VanguardLoadouts.CARBINE_556.copy(barrel = VanguardArmory.BARREL_308_MAGNUM)
        )
        cases.check(
            title = "A bolt head that cannot take the calibre is refused",
            expectation = "BOLT_CALIBER_MISMATCH",
            passed = wrongBolt.errorCodeOrNull() == "BOLT_CALIBER_MISMATCH",
            detail = observed(wrongBolt.errorCodeOrNull(), "BOLT_CALIBER_MISMATCH")
        )

        val wrongThread = BallisticStatAggregator.assemble(
            VanguardLoadouts.PRECISION_308.copy(
                barrel = VanguardArmory.BARREL_308_MAGNUM.copy(
                    id = "BARREL_TEST_HALF_INCH",
                    muzzleThread = "1/2x28"
                )
            )
        )
        cases.check(
            title = "A muzzle device that does not thread onto the barrel is refused",
            expectation = "MUZZLE_THREAD_MISMATCH",
            passed = wrongThread.errorCodeOrNull() == "MUZZLE_THREAD_MISMATCH",
            detail = observed(wrongThread.errorCodeOrNull(), "MUZZLE_THREAD_MISMATCH")
        )

        val lightTrigger = BallisticStatAggregator.assemble(
            VanguardLoadouts.PRECISION_308.copy(
                trigger = VanguardArmory.TRIGGER_STAGE_3.copy(
                    id = "TRIGGER_TEST_500G",
                    name = "Match 500 g",
                    pullWeightGrams = 500
                )
            )
        )
        cases.check(
            title = "A trigger outside 900-1500 g is refused",
            expectation = "TRIGGER_OUT_OF_SPEC",
            passed = lightTrigger.errorCodeOrNull() == "TRIGGER_OUT_OF_SPEC",
            detail = observed(lightTrigger.errorCodeOrNull(), "TRIGGER_OUT_OF_SPEC")
        )

        val emptyMag = BallisticStatAggregator.assemble(
            VanguardLoadouts.PRECISION_308.copy(
                magazine = VanguardLoadouts.PRECISION_308.magazine.copy(id = "MAG_TEST_EMPTY", capacity = 0)
            )
        )
        cases.check(
            title = "A magazine that holds nothing is refused",
            expectation = "EMPTY_MAGAZINE_SPEC",
            passed = emptyMag.errorCodeOrNull() == "EMPTY_MAGAZINE_SPEC",
            detail = observed(emptyMag.errorCodeOrNull(), "EMPTY_MAGAZINE_SPEC")
        )

        val swap = BallisticStatAggregator.swapBarrel(
            VanguardLoadouts.PRECISION_308,
            VanguardArmory.BARREL_300WM_MAGNUM
        ).valueOrNull()
        cases.check(
            title = "A .300 Win Mag barrel swap keeps the bedding under 3 MOA",
            expectation = "Ok, zero shift 0.215 MOA, bedding preserved",
            passed = swap != null && swap.beddingPreserved &&
                approxEquals(swap.zeroShiftMoa, 0.215f) &&
                swap.loadout.barrel.caliber == Caliber.THREE_HUNDRED_WIN_MAG,
            detail = "zeroShift=${swap?.zeroShiftMoa} preserved=${swap?.beddingPreserved}"
        )

        val degradedChassis = VanguardArmory.CHASSIS_TRI_ALLOY.copy(
            id = "CHASSIS_TEST_DEGRADED",
            beddingGeometryRating = 0.5f
        )
        val refusedSwap = BallisticStatAggregator.swapBarrel(
            VanguardLoadouts.PRECISION_308.copy(chassis = degradedChassis),
            VanguardArmory.BARREL_300WM_MAGNUM
        )
        cases.check(
            title = "A chassis whose bedding cannot hold sub-3 MOA refuses the swap",
            expectation = "ZERO_SHIFT_EXCEEDS_SPEC",
            passed = refusedSwap.errorCodeOrNull() == "ZERO_SHIFT_EXCEEDS_SPEC",
            detail = observed(refusedSwap.errorCodeOrNull(), "ZERO_SHIFT_EXCEEDS_SPEC")
        )

        val carbine = BallisticStatAggregator.assemble(VanguardLoadouts.CARBINE_556).valueOrNull()
        cases.check(
            title = "The carbine build is a valid, lighter 5.56 assembly",
            expectation = "Ok, 5.56 calibre, mass below the precision build",
            passed = carbine != null && precision != null &&
                carbine.caliber == Caliber.FIVE_FIVE_SIX &&
                carbine.massKg < precision.massKg,
            detail = "carbineMass=${carbine?.massKg} precisionMass=${precision?.massKg}"
        )
    }
}
