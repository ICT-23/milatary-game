package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.valor.ValorBibleAudit
import com.example.game.valor.art.AnimationLibrary
import com.example.game.valor.art.ArtAct
import com.example.game.valor.art.CharacterAsset
import com.example.game.valor.art.CharacterArt
import com.example.game.valor.art.EnvironmentRules
import com.example.game.valor.art.SurfaceMaterial
import com.example.game.valor.art.VfxEffect
import com.example.game.valor.art.VfxLibrary
import com.example.game.valor.art.VisualPillar
import com.example.game.valor.art.WeaponArt
import com.example.game.valor.art.WeaponClass
import com.example.game.valor.audio.MixBus
import com.example.game.valor.audio.ValorAudioBible
import com.example.game.valor.pipeline.AssetPipeline
import com.example.game.valor.pipeline.AssetPrefix
import com.example.game.valor.ux.AccessibilityProfile
import com.example.game.valor.ux.AccessibilityRules
import com.example.game.valor.ux.ColorBlindMode
import com.example.game.valor.ux.HudLayout
import com.example.game.valor.ux.LocaleRegistry
import com.example.game.valor.ux.UiStyle
import com.example.ui.components.MonoLabel
import com.example.ui.components.TacticalDetailRow
import com.example.ui.components.TacticalSectionCard
import com.example.ui.components.ValorActBlock
import com.example.ui.components.ValorPillarRow
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CoolSteel
import com.example.ui.theme.DeepGraphite
import com.example.ui.theme.DeepGraphiteBorder
import com.example.ui.theme.DeepGraphiteElevated
import com.example.ui.theme.RadarCyan
import com.example.ui.theme.SignalAmber
import com.example.ui.theme.WeatheredIvory
import kotlin.math.roundToInt

/**
 * Sections 1 - 15 of the Art, Audio & UI Bible as a live read-out.
 *
 * Nothing on this screen is typed by hand: palettes, budgets, mix levels, locale
 * counts and checklists are read back from the valor domain packages, and the
 * footer runs the same contract gate that guards the data.
 */
@Composable
fun ValorBibleScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val audit = remember { ValorBibleAudit.audit() }
    val accessibility = remember { AccessibilityProfile() }
    val grouped = remember(audit) { audit.byGroup() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepGraphite)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("valor_screen")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DeepGraphiteElevated)
                    .border(1.dp, DeepGraphiteBorder, RoundedCornerShape(8.dp))
                    .testTag("valor_back_button")
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = WeatheredIvory)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "ART / AUDIO / UI BIBLE",
                    style = MaterialTheme.typography.titleLarge,
                    color = WeatheredIvory,
                    letterSpacing = 1.5.sp
                )
                MonoLabel(text = "VALOR ACADEMY - VISUAL, SOUND & UX IDENTITY", size = 10)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        TacticalSectionCard("VISUAL PILLARS", "SECTION 1 - FIVE PILLARS", RadarCyan) {
            VisualPillar.entries.forEach { ValorPillarRow(it) }
        }

        Spacer(modifier = Modifier.height(12.dp))
        TacticalSectionCard("ACT DIRECTION", "SECTION 2 - SETTING, PALETTE, LIGHT, GRADE", SignalAmber) {
            ArtAct.entries.forEach { ValorActBlock(it) }
        }

        Spacer(modifier = Modifier.height(12.dp))
        TacticalSectionCard("CHARACTERS & WEAPONS", "SECTIONS 3 - 4 - TRIANGLE BUDGETS", RadarCyan) {
            TacticalDetailRow("RIG BONES", "${CharacterArt.RIG_MAX_BONES} maximum")
            TacticalDetailRow("PLAYER CREATOR", "${CharacterArt.playerFaceCombinations()} face combinations")
            TacticalDetailRow("RANK BADGES", CharacterArt.RANK_BADGES.toString())
            CharacterAsset.entries.forEach { asset ->
                TacticalDetailRow(
                    asset.displayName.uppercase(),
                    "${asset.lod0Triangles} / ${asset.lod1Triangles} / ${asset.lod2Triangles}"
                )
            }
            TacticalDetailRow(
                "WEAPON FP / WORLD",
                "${WeaponArt.FIRST_PERSON_MIN_TRIANGLES / 1000}k-${WeaponArt.FIRST_PERSON_MAX_TRIANGLES / 1000}k" +
                    " / ${WeaponArt.WORLD_MIN_TRIANGLES / 1000}k-${WeaponArt.WORLD_MAX_TRIANGLES / 1000}k"
            )
            TacticalDetailRow(
                "PARTS / SOCKETS",
                "${WeaponArt.SEPARATE_PARTS.size} / ${WeaponArt.allSockets().size}"
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        TacticalSectionCard("ENVIRONMENT", "SECTION 5 - SURFACES & IMPORT RULES", CoolSteel) {
            TacticalDetailRow(
                "SURFACES",
                "${SurfaceMaterial.entries.size} with FX, audio and penetration"
            )
            TacticalDetailRow(
                "PENETRABLE",
                SurfaceMaterial.entries.filter { it.penetrable }.joinToString(", ") { it.displayName }
            )
            TacticalDetailRow(
                "SPLAT LAYERS",
                "${EnvironmentRules.SPLAT_LAYERS_MIN}-${EnvironmentRules.SPLAT_LAYERS_MAX}"
            )
            TacticalDetailRow(
                "PROPS PER 100 M",
                "${EnvironmentRules.propCountRange(100f).first}-" +
                    "${EnvironmentRules.propCountRange(100f).last}"
            )
            TacticalDetailRow("COVER HEIGHT", "1.0 m low / 2.0 m high")
        }

        Spacer(modifier = Modifier.height(12.dp))
        TacticalSectionCard("ANIMATION & VFX", "SECTIONS 6 - 7 - CLIPS AND BUDGETS", RadarCyan) {
            TacticalDetailRow(
                "WEAPON CLIPS",
                "${AnimationLibrary.weaponClipCount()} across ${WeaponClass.entries.size} classes"
            )
            TacticalDetailRow(
                "MELEE / GRENADE / UTILITY",
                "${AnimationLibrary.MELEE_CLIPS.size} / ${AnimationLibrary.GRENADE_CLIPS.size} / " +
                    "${AnimationLibrary.UTILITY_CLIPS.size}"
            )
            TacticalDetailRow("ENEMY CLIPS", AnimationLibrary.enemyClipCount().toString())
            VfxEffect.entries.forEach { effect ->
                TacticalDetailRow(
                    effect.displayName.uppercase(),
                    if (effect.isUiOnly) "UI ONLY" else "${VfxLibrary.fixedBudget(effect)} particles"
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        TacticalSectionCard("AUDIO MIX", "SECTION 9 - BUSES, VOICES, VARIATION", SignalAmber) {
            MixBus.entries.forEach { bus ->
                TacticalDetailRow(
                    bus.displayName.uppercase(),
                    "${bus.levelDb.toInt()} dB / ${(ValorAudioBible.gainFor(bus) * 100f).roundToInt()}%"
                )
            }
            TacticalDetailRow("LIMITER", "${ValorAudioBible.LIMITER_DB.toInt()} dB")
            TacticalDetailRow(
                "VOICES",
                "${ValorAudioBible.maxVoices(false)} / ${ValorAudioBible.maxVoices(true)} low-end"
            )
            TacticalDetailRow(
                "VARIANTS",
                "${ValorAudioBible.VARIATION_MIN}-${ValorAudioBible.VARIATION_MAX} at +/-5% pitch"
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        TacticalSectionCard("HUD & INTERFACE", "SECTION 10 - SAFE AREA, PRESETS, FEEDBACK", CoolSteel) {
            TacticalDetailRow("WIDGETS", "${HudLayout.WIDGETS.size} all movable")
            HudLayout.PRESETS.forEach { preset ->
                TacticalDetailRow(
                    preset.displayName.uppercase(),
                    "${preset.scale}x at ${preset.defaultOpacity} opacity"
                )
            }
            TacticalDetailRow("BODY TEXT", "${UiStyle.MIN_BODY_TEXT_SP} sp minimum")
            TacticalDetailRow("HIT MARKER", HudLayout.hitMarker(headshot = true, kill = false))
            TacticalDetailRow("NOTIFICATIONS", HudLayout.NOTIFICATION_ANCHOR)
        }

        Spacer(modifier = Modifier.height(12.dp))
        TacticalSectionCard("ACCESSIBILITY & LANGUAGE", "SECTIONS 11 - 12", SignalAmber) {
            TacticalDetailRow(
                "SUBTITLES",
                "${accessibility.subtitleSize.displayName} / " +
                    "${AccessibilityRules.subtitleTextSp(20, accessibility)} sp"
            )
            TacticalDetailRow("COLOUR BLIND", "${ColorBlindMode.entries.size} palettes")
            TacticalDetailRow(
                "AIM ASSIST",
                "${accessibility.aimAssist.displayName} / " +
                    "${AccessibilityRules.magnetismDegrees(accessibility.aimAssist)} deg"
            )
            TacticalDetailRow("REDUCED VIOLENCE", if (accessibility.reducedViolence) "ON" else "OFF")
            TacticalDetailRow("LOCALES", "${LocaleRegistry.LOCALES.size} subtitle / EN voice")
            TacticalDetailRow("RTL", LocaleRegistry.rtlLocales().joinToString { it.code })
            TacticalDetailRow(
                "TEXT EXPANSION",
                "${LocaleRegistry.reservedWidth(100f).roundToInt()}% width reserved"
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        TacticalSectionCard("ASSET PIPELINE", "SECTIONS 13 - 15", RadarCyan) {
            TacticalDetailRow("PREFIXES", "${AssetPrefix.entries.size} naming families")
            TacticalDetailRow("IMPORT CHECKS", "${AssetPipeline.IMPORT_CHECKLIST.size} steps")
            TacticalDetailRow("ART QA CHECKS", "${AssetPipeline.ART_QA_CHECKLIST.size} steps")
            TacticalDetailRow("LICENSE LOG", AssetPipeline.LICENSE_LOG_FILE)
        }

        Spacer(modifier = Modifier.height(12.dp))
        TacticalSectionCard(
            "CONTRACT GATE",
            if (audit.allPassed) "ALL CONTRACTS HOLD" else "${audit.failures.size} FAILURES",
            if (audit.allPassed) RadarCyan else AlertRed
        ) {
            TacticalDetailRow("CASES", "${audit.passed} / ${audit.total}")
            ValorBibleAudit.GROUP_ORDER.forEach { group ->
                val cases = grouped[group] ?: return@forEach
                TacticalDetailRow(
                    group,
                    "${cases.count { it.passed }} / ${cases.size}",
                    if (cases.all { it.passed }) RadarCyan else AlertRed
                )
            }
            audit.failures.forEach { failure ->
                TacticalDetailRow(failure.title, failure.detail, AlertRed)
            }
        }
    }
}
