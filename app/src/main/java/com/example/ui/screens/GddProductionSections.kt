package com.example.ui.screens

import androidx.compose.runtime.Composable
import com.example.game.valor.gdd.PerformanceBudget
import com.example.game.valor.gdd.QualityPresets
import com.example.game.valor.gdd.Roadmap
import com.example.game.valor.gdd.SuccessChecklist
import com.example.game.valor.gdd.TestRules
import com.example.game.valor.gdd.UiScreens
import com.example.ui.components.MonoLabel
import com.example.ui.components.TacticalDetailRow
import com.example.ui.theme.CoolSteel
import com.example.ui.theme.RadarCyan
import com.example.ui.theme.SignalAmber

private fun fpsLabel(fps: IntRange?): String =
    if (fps == null) "UNSPECIFIED" else "${fps.first}-${fps.last} FPS"

/** Sections 11.3, 12.4 and 12.6: presets, budget and testing. */
@Composable
internal fun GddProductionSection() {
    GddSection("PRODUCTION", "SECTIONS 11.3, 12.4, 12.6 - PRESETS, BUDGET, TESTING", CoolSteel) {
        QualityPresets.PRESETS.forEach { preset ->
            MonoLabel(
                text = "${preset.name}: ${preset.targetDevice} / ${fpsLabel(preset.fps)} / " +
                    (preset.resolution ?: "native") +
                    (preset.renderScale?.let { " / ${it} scale" } ?: "")
            )
        }
        TacticalDetailRow("DRAW CALLS", "under ${PerformanceBudget.MAX_DRAW_CALLS}")
        TacticalDetailRow(
            "TRIANGLES",
            "under ${PerformanceBudget.MAX_TRIANGLES_MEDIUM} on medium"
        )
        TacticalDetailRow(
            "FRAME TARGET",
            "${PerformanceBudget.TARGET_FPS_MEDIUM} FPS med / " +
                "${PerformanceBudget.TARGET_FPS_LOW} FPS low"
        )
        TacticalDetailRow(
            "TEXTURES",
            "${PerformanceBudget.TEXTURE_COMPRESSION} / ${PerformanceBudget.LOD_LEVELS} LODs"
        )
        PerformanceBudget.TECHNIQUES.forEach { MonoLabel(text = "OPT - $it") }
        TestRules.RULES.forEach { rule ->
            MonoLabel(text = "${rule.name} - ${rule.detail}")
        }
    }
}

/** Section 13: the thirteen screens of the flow. */
@Composable
internal fun GddScreenFlowSection() {
    GddSection("SCREEN FLOW", "SECTION 13 - 13 SCREENS", RadarCyan) {
        UiScreens.SCREENS.forEach { screen ->
            MonoLabel(text = "${screen.order}. ${screen.title} - ${screen.detail}")
        }
        TacticalDetailRow(
            "MAIN MENU",
            UiScreens.MAIN_MENU_ITEMS.joinToString(" / ")
        )
    }
}

/** Sections 14 and 18: the milestone plan and the launch checklist. */
@Composable
internal fun GddRoadmapSection() {
    GddSection("ROADMAP", "SECTIONS 14 + 18 - MILESTONES AND SHIP CRITERIA", SignalAmber) {
        Roadmap.MILESTONES.forEach { milestone ->
            TacticalDetailRow("${milestone.id} ${milestone.name}", milestone.doneWhen)
        }
        SuccessChecklist.ITEMS.forEach { MonoLabel(text = "CHECK - $it") }
    }
}
