package com.example.ui.screens

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.game.valor.gdd.Campaign
import com.example.game.valor.gdd.DroneSupport
import com.example.game.valor.gdd.GddMedal
import com.example.game.valor.gdd.GearTable
import com.example.game.valor.gdd.Gunsmith
import com.example.game.valor.gdd.Prestige
import com.example.game.valor.gdd.RankCurve
import com.example.game.valor.gdd.StatBars
import com.example.game.valor.gdd.StoryCast
import com.example.game.valor.gdd.StoryDelivery
import com.example.game.valor.gdd.SupportCalls
import com.example.game.valor.gdd.WeaponCategories
import com.example.game.valor.gdd.WeaponHandling
import com.example.ui.components.MonoLabel
import com.example.ui.components.TacticalDetailRow
import com.example.ui.components.TacticalSectionCard
import com.example.ui.theme.CoolSteel
import com.example.ui.theme.RadarCyan
import com.example.ui.theme.SignalAmber

/** Spacer plus card, so every GDD section starts at the same rhythm. */
@Composable
internal fun GddSection(
    title: String,
    subtitle: String,
    accent: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Spacer(modifier = Modifier.height(12.dp))
    TacticalSectionCard(title, subtitle, accent, content)
}

private fun xpLabel(xp: Int?): String = if (xp == null) "STORY" else "%,d XP".format(xp)

/** Section 3: the seven characters, the six acts and the three endings. */
@Composable
internal fun GddStorySection() {
    GddSection("STORY & CAMPAIGN", "SECTION 3 - CAST, ACTS, ENDINGS", RadarCyan) {
        StoryCast.CHARACTERS.forEach { character ->
            TacticalDetailRow(
                character.name,
                if (character.playable) "PLAYABLE" else character.role.displayName
            )
        }
        MonoLabel(text = "ACTS", size = 8, weight = FontWeight.Bold)
        Campaign.ACTS.forEach { act ->
            TacticalDetailRow(
                "Act ${act.number} - ${act.title}",
                "${act.openingRank} > ${act.closingRank}"
            )
        }
        Campaign.ACTS.filter { it.boss != null }.forEach { act ->
            MonoLabel(text = "BOSS ${act.number}: ${act.boss}", color = SignalAmber)
        }
        Campaign.tutorials().forEach { MonoLabel(text = "TRAINING - $it") }
        Campaign.ENDINGS.forEach { ending ->
            TacticalDetailRow(ending.displayName, ending.trigger)
        }
        TacticalDetailRow(
            "DELIVERY",
            "${StoryDelivery.CHANNELS.size} channels, " +
                "${StoryDelivery.CUTSCENE_MIN_SECONDS}-${StoryDelivery.CUTSCENE_MAX_SECONDS} s"
        )
    }
}

/** Section 4: the twenty-rank ladder and prestige. */
@Composable
internal fun GddRankSection() {
    GddSection("RANK LADDER", "SECTION 4 - 20 RUNGS, PRESTIGE", CoolSteel) {
        RankCurve.RANKS.forEach { rank ->
            TacticalDetailRow("${rank.index}. ${rank.title}", xpLabel(rank.cumulativeXp))
        }
        TacticalDetailRow(
            "COMMANDER",
            "rank ${RankCurve.COMMANDER_RANK} at ${xpLabel(RankCurve.FINAL_XP)}"
        )
        MonoLabel(
            text = "PRESTIGE opens at rank ${Prestige.REQUIRED_RANK}: " +
                Prestige.RULES.joinToString("; "),
            size = 8
        )
    }
}

/** Section 4.1: the eight awards and their requirements. */
@Composable
internal fun GddMedalSection() {
    GddSection("MEDALS", "SECTION 4.1 - EIGHT AWARDS", SignalAmber) {
        GddMedal.entries.forEach { medal ->
            TacticalDetailRow(medal.displayName, medal.requirement)
        }
    }
}

/** Sections 5.1 - 5.3: categories, stats, workshop slots and gear. */
@Composable
internal fun GddArsenalSection() {
    GddSection("ARSENAL", "SECTIONS 5.1-5.3 - CATEGORIES, STATS, WORKSHOP", RadarCyan) {
        WeaponCategories.CATEGORIES.forEach { category ->
            TacticalDetailRow(category.category, category.era)
        }
        MonoLabel(text = "NINE STATS", size = 8, weight = FontWeight.Bold)
        StatBars.STATS.forEach { stat ->
            MonoLabel(text = "${stat.displayName} - ${stat.note}")
        }
        MonoLabel(text = "WORKSHOP STEPS", size = 8, weight = FontWeight.Bold)
        Gunsmith.STEPS.forEach { step ->
            TacticalDetailRow("${step.order}. ${step.name}", step.detail)
        }
        MonoLabel(text = "SLOTS", size = 8, weight = FontWeight.Bold)
        Gunsmith.SLOTS.forEach { slot ->
            TacticalDetailRow(
                slot.slot,
                slot.unlockRank?.let { "RANK $it" } ?: "${slot.options.size} options"
            )
        }
    }
}

/** Sections 5.4 - 6: handling rules, gear, drones and radio calls. */
@Composable
internal fun GddSupportSection() {
    GddSection("HANDLING, GEAR & SUPPORT", "SECTIONS 5.4-6 - DAMAGE, DRONES, RADIO", SignalAmber) {
        TacticalDetailRow(
            "HIT ZONES",
            "Head x${WeaponHandling.HEAD_MULTIPLIER} / torso x${WeaponHandling.TORSO_MULTIPLIER}" +
                " / limb x${WeaponHandling.LIMB_MULTIPLIER}"
        )
        TacticalDetailRow("FIRE MODES", WeaponHandling.FIRE_MODES.joinToString(" / "))
        GearTable.ROWS.forEach { gear ->
            TacticalDetailRow(gear.item, if (gear.tiered) "TIERS" else "STANDARD")
        }
        MonoLabel(text = "DRONES", size = 8, weight = FontWeight.Bold)
        DroneSupport.DRONES.forEach { drone ->
            TacticalDetailRow(
                drone.displayName,
                "rank ${drone.unlockRank}" +
                    (drone.cooldownSeconds?.let { " / ${it}s cd" } ?: "") +
                    (drone.durationSeconds?.let { " / ${it}s" } ?: "")
            )
        }
        SupportCalls.CALLS.forEach { call ->
            TacticalDetailRow(
                call.name,
                call.unlockRank?.let { "RANK $it" } ?: if (call.eraDependent) "ERA" else "ALWAYS"
            )
        }
    }
}
