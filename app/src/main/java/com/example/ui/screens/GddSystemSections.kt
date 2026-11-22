package com.example.ui.screens

import androidx.compose.runtime.Composable
import com.example.game.valor.gdd.Academy
import com.example.game.valor.gdd.AccessibilityOptions
import com.example.game.valor.gdd.BossRoster
import com.example.game.valor.gdd.CampaignShape
import com.example.game.valor.gdd.ControlScheme
import com.example.game.valor.gdd.Currencies
import com.example.game.valor.gdd.EnemyAi
import com.example.game.valor.gdd.EnemyRoster
import com.example.game.valor.gdd.MissionMastery
import com.example.game.valor.gdd.MissionTypes
import com.example.game.valor.gdd.Monetization
import com.example.game.valor.gdd.ReplayModes
import com.example.game.valor.gdd.SkillTreeSpec
import com.example.game.valor.gdd.UpgradeCategory
import com.example.game.valor.gdd.WorldMap
import com.example.game.valor.gdd.WorldSystems
import com.example.ui.components.MonoLabel
import com.example.ui.components.TacticalDetailRow
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CoolSteel
import com.example.ui.theme.RadarCyan
import com.example.ui.theme.SignalAmber

/** Sections 7.1 - 7.3 and 7.5: mission types, academy, replay and bosses. */
@Composable
internal fun GddModeSection() {
    GddSection("MISSION MODES", "SECTIONS 7.1-7.3, 7.5 - TYPES, ACADEMY, REPLAY", RadarCyan) {
        TacticalDetailRow(
            "CAMPAIGN",
            "${CampaignShape.APPROX_MISSIONS} missions / ${CampaignShape.ACTS} acts / " +
                "${CampaignShape.MIN_MINUTES}-${CampaignShape.MAX_MINUTES} min"
        )
        MissionTypes.TYPES.forEach { type ->
            TacticalDetailRow("${type.code} - ${type.name}", type.objective)
        }
        Academy.ACTIVITIES.forEach { activity ->
            TacticalDetailRow(activity.name, if (activity.timed) "TIMED" else "FREE")
        }
        ReplayModes.MODES.forEach { mode ->
            MonoLabel(text = "${mode.name} - ${mode.detail}")
        }
        TacticalDetailRow("MASTERY", MissionMastery.CRITERIA.joinToString(" / "))
        BossRoster.BOSSES.forEach { boss ->
            TacticalDetailRow(boss.name, boss.act?.let { "ACT $it" } ?: "ERA")
        }
    }
}

/** Section 7.4: the enemy roster and the eight AI arrows. */
@Composable
internal fun GddAiSection() {
    GddSection("ENEMIES & AI", "SECTION 7.4 - ROSTER AND STATE MACHINE", AlertRed) {
        EnemyRoster.ENEMIES.forEach { enemy ->
            TacticalDetailRow(enemy.enemy, enemy.behavior)
        }
        MonoLabel(text = "STATE MACHINE", size = 8)
        EnemyAi.TRANSITIONS.forEach { transition ->
            MonoLabel(
                text = "${transition.from.displayName} -[${transition.event.displayName}]-> " +
                    transition.to.displayName
            )
        }
        MonoLabel(text = "ANY STATE -[KILLED]-> Dead", color = AlertRed)
    }
}

/** Section 8: the touch scheme and the accessibility switches. */
@Composable
internal fun GddControlSection() {
    GddSection("CONTROLS", "SECTION 8 - TOUCH SCHEME, ACCESSIBILITY", CoolSteel) {
        ControlScheme.BINDINGS.forEach { binding ->
            TacticalDetailRow(binding.control, binding.action)
        }
        TacticalDetailRow("AIM ASSIST", ControlScheme.aimAssistLabels().joinToString(" / "))
        AccessibilityOptions.OPTIONS.forEach { option ->
            MonoLabel(text = "${option.displayName} - ${option.detail}")
        }
    }
}

/** Section 9: currencies, upgrades, the skill tree and fair play. */
@Composable
internal fun GddEconomySection() {
    GddSection("ECONOMY & SKILLS", "SECTION 9 - CURRENCIES, UPGRADES, MONETIZATION", SignalAmber) {
        Currencies.ALL.forEach { currency ->
            TacticalDetailRow(currency.name, currency.use)
        }
        UpgradeCategory.entries.forEach { category ->
            MonoLabel(text = "${category.displayName} - ${category.detail}")
        }
        SkillTreeSpec.BRANCHES.forEach { branch ->
            MonoLabel(text = "${branch.name} - ${branch.effects.joinToString(", ")}")
        }
        TacticalDetailRow(
            "SKILL POINTS",
            "${SkillTreeSpec.POINTS_PER_RANK} per rank (${SkillTreeSpec.pointsEarnedAtRank(20)} at 20)"
        )
        Monetization.RULES.forEach { rule ->
            MonoLabel(text = "${rule.kind} - ${rule.detail}")
        }
    }
}

/** Section 10: environments, weather and lite destruction. */
@Composable
internal fun GddWorldSection() {
    GddSection("WORLD", "SECTION 10 - ENVIRONMENTS, WEATHER, DESTRUCTION", RadarCyan) {
        TacticalDetailRow("TOTAL", "${WorldMap.totalEnvironments()} environments")
        WorldMap.ACTS.forEach { act ->
            TacticalDetailRow("Act ${act.act} - ${act.title}", act.environments.joinToString(", "))
        }
        TacticalDetailRow("WEATHER", WorldSystems.WEATHER.joinToString(" / ") { it.displayName })
        TacticalDetailRow(
            "DESTRUCTION",
            WorldSystems.DESTRUCTION.joinToString(" / ") { it.displayName }
        )
    }
}
