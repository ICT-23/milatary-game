package com.example.game.valor.gdd

import com.example.game.vanguard.CaseRecorder
import com.example.game.vanguard.ContractCase
import com.example.game.vanguard.approxEquals
import com.example.game.vanguard.observed

/** Sections 11.3, 12.4 and 12.6: presets, the performance budget and test rules. */
object GddProductionCases {

    fun run(): List<ContractCase> {
        val r = CaseRecorder(ValorGddAudit.GROUP_PRODUCTION)

        r.check(
            "Quality presets", "Low, Medium, High and Ultra",
            QualityPresets.PRESETS.map { it.name } == listOf("Low", "Medium", "High", "Ultra"),
            observed(QualityPresets.PRESETS.map { it.name }, "[4 presets]")
        )
        r.check(
            "Low preset", "3 GB phones at 30 FPS, 720p, 0.7 render scale",
            QualityPresets.lowPreset().fps == 30..30 &&
                QualityPresets.lowPreset().resolution == "720p" &&
                approxEquals(QualityPresets.lowPreset().renderScale ?: 0f, 0.7f),
            observed(QualityPresets.lowPreset().resolution, "720p")
        )
        r.check(
            "Medium preset", "4-6 GB phones at 45-60 FPS, 1080p",
            QualityPresets.byName("Medium")?.fps == 45..60 &&
                QualityPresets.byName("Medium")?.resolution == "1080p",
            observed(QualityPresets.byName("Medium")?.fps, "45..60")
        )
        r.check(
            "High preset", "8 GB+ phones at a locked 60 FPS",
            QualityPresets.byName("High")?.fps == 60..60,
            observed(QualityPresets.byName("High")?.fps, "60..60")
        )
        r.check(
            "Ultra preset", "PC/tablet with 4K assets and no printed frame rate",
            QualityPresets.byName("Ultra")?.fps == null &&
                QualityPresets.byName("Ultra")?.resolution == "4K assets",
            observed(QualityPresets.byName("Ultra")?.fps, "null")
        )
        r.check(
            "Budget numbers", "150 draw calls, 400k triangles, ASTC, 3 LODs",
            PerformanceBudget.MAX_DRAW_CALLS == 150 &&
                PerformanceBudget.MAX_TRIANGLES_MEDIUM == 400_000 &&
                PerformanceBudget.TEXTURE_COMPRESSION == "ASTC" &&
                PerformanceBudget.LOD_LEVELS == 3,
            observed(PerformanceBudget.MAX_DRAW_CALLS, 150)
        )
        r.check(
            "Frame targets", "60 FPS on medium, 30 on low",
            PerformanceBudget.TARGET_FPS_MEDIUM == 60 &&
                PerformanceBudget.TARGET_FPS_LOW == 30 &&
                PerformanceBudget.targetFps(QualityPresets.lowPreset()) == 30,
            observed(PerformanceBudget.targetFps(QualityPresets.lowPreset()), 30)
        )
        r.check(
            "Draw call gate", "149 passes, 150 is over budget",
            PerformanceBudget.withinDrawCalls(149) &&
                !PerformanceBudget.withinDrawCalls(150),
            observed(PerformanceBudget.withinDrawCalls(150), false)
        )
        r.check(
            "Triangle gate", "just under 400k passes",
            PerformanceBudget.withinTriangles(399_999) &&
                !PerformanceBudget.withinTriangles(400_000),
            observed(PerformanceBudget.withinTriangles(400_000), false)
        )
        r.check(
            "Optimisation set", "5 documented techniques",
            PerformanceBudget.TECHNIQUES.size == 5 &&
                PerformanceBudget.TECHNIQUES.contains("GPU instancing"),
            observed(PerformanceBudget.TECHNIQUES.size, 5)
        )
        r.check(
            "Presets by floor", "a 30 FPS floor keeps every preset, 45 drops Low",
            QualityPresets.presetsFor(30).size == 4 && QualityPresets.presetsFor(45).size == 3,
            observed(QualityPresets.presetsFor(45).size, 3)
        )
        r.check(
            "Test rules", "4 rules with Firebase Crashlytics",
            TestRules.RULES.size == 4 &&
                TestRules.RULES.last().detail == TestRules.CRASH_REPORTING,
            observed(TestRules.RULES.size, 4)
        )
        r.check(
            "Timer fairness", "timers scale and the first tournament passes",
            TestRules.firstTournamentPassable() && TestRules.timersScaleByRank() &&
                TestRules.RULES.any { it.name == "Timer fairness" },
            observed(TestRules.firstTournamentPassable(), true)
        )

        return r.result()
    }
}

/** Section 13: the thirteen screens and the main menu. */
object GddScreenCases {

    fun run(): List<ContractCase> {
        val r = CaseRecorder(ValorGddAudit.GROUP_SCREENS)

        r.check(
            "Screen count", "13 screens numbered in order",
            UiScreens.SCREENS.size == 13 && UiScreens.isOrdered(),
            observed(UiScreens.SCREENS.size, 13)
        )
        r.check(
            "Screen ends", "from splash/menu to settings",
            UiScreens.byOrder(1) == UiScreen.MAIN_MENU &&
                UiScreens.byOrder(13) == UiScreen.SETTINGS,
            observed(UiScreens.byOrder(13), UiScreen.SETTINGS)
        )
        r.check(
            "Main menu", "7 entries starting with Continue",
            UiScreens.MAIN_MENU_ITEMS.size == 7 &&
                UiScreens.MAIN_MENU_ITEMS.first() == "Continue" &&
                UiScreens.MAIN_MENU_ITEMS.last() == "Settings",
            observed(UiScreens.MAIN_MENU_ITEMS, "[7 entries]")
        )
        r.check(
            "Result screen", "XP, cash, medals, stars and the rank-up",
            UiScreens.byOrder(8)?.detail?.contains("rank-up animation") == true,
            observed(UiScreens.byOrder(8)?.detail, "contains rank-up animation")
        )
        r.check(
            "Gallery screen", "twenty ranks and eight medals",
            UiScreens.byOrder(11)?.detail == "Twenty ranks, eight medals",
            observed(UiScreens.byOrder(11)?.detail, "Twenty ranks, eight medals")
        )

        return r.result()
    }
}

/** Sections 14 and 18: the milestone table and the launch checklist. */
object GddRoadmapCases {

    fun run(): List<ContractCase> {
        val r = CaseRecorder(ValorGddAudit.GROUP_ROADMAP)

        r.check(
            "Milestones", "12 milestones M1 to M12",
            Roadmap.count() == 12 &&
                Roadmap.MILESTONES.map { it.id } == (1..12).map { "M$it" },
            observed(Roadmap.count(), 12)
        )
        r.check(
            "Prototype", "M1 is movement and one gun",
            Roadmap.byId("M1")?.doneWhen == "Player moves and shoots",
            observed(Roadmap.byId("M1")?.doneWhen, "Player moves and shoots")
        )
        r.check(
            "Act 4 + 5", "M7 ships the set pieces and vehicles",
            Roadmap.byId("M7")?.name == "Act 4 + 5",
            observed(Roadmap.byId("M7")?.name, "Act 4 + 5")
        )
        r.check(
            "Last milestone", "M12 is the Play Store launch",
            Roadmap.finalMilestone().id == "M12" &&
                Roadmap.finalMilestone().name == "Launch",
            observed(Roadmap.finalMilestone().name, "Launch")
        )
        r.check(
            "Checklist", "7 launch criteria",
            SuccessChecklist.count() == 7,
            observed(SuccessChecklist.count(), 7)
        )
        r.check(
            "No forced ads", "gameplay is never interrupted by ads",
            SuccessChecklist.contains("No forced ads during gameplay"),
            observed(SuccessChecklist.ITEMS.first(), "40 FPS / ad rule present")
        )
        r.check(
            "Frame-rate goal", "60 FPS on mid-range phones is a launch criterion",
            SuccessChecklist.contains("Smooth 60 FPS on mid-range phone"),
            observed(SuccessChecklist.count(), 7)
        )

        return r.result()
    }
}
