package com.example.game.valor

import com.example.game.valor.art.ArtAct
import com.example.game.valor.audio.MixBus
import com.example.game.valor.audio.SfxCategory
import com.example.game.valor.audio.ValorAudioBible
import com.example.game.valor.pipeline.AiArtPrompts
import com.example.game.valor.pipeline.AssetPipeline
import com.example.game.valor.pipeline.AssetPrefix
import com.example.game.valor.pipeline.LicenseKind
import com.example.game.valor.pipeline.LicenseRecord
import com.example.game.valor.ux.AccessibilityProfile
import com.example.game.valor.ux.AccessibilityRules
import com.example.game.valor.ux.AimAssistLevel
import com.example.game.valor.ux.ColorBlindMode
import com.example.game.valor.ux.FeedbackRules
import com.example.game.valor.ux.HudLayout
import com.example.game.valor.ux.HudPreset
import com.example.game.valor.ux.LocaleRegistry
import com.example.game.valor.ux.ScreenFlow
import com.example.game.valor.ux.ScreenId
import com.example.game.valor.ux.SubtitleSize
import com.example.game.valor.ux.UiStyle
import com.example.game.vanguard.CaseRecorder
import com.example.game.vanguard.ContractCase
import com.example.game.vanguard.approxEquals
import com.example.game.vanguard.observed

/** Sections 9 - 15: audio, UI, accessibility, localization and the asset pipeline. */
internal object ValorSystemsContractCases {

    fun run(): List<ContractCase> =
        audioCases() + uiCases() + accessibilityCases() + localizationCases() + pipelineCases()

    private fun audioCases(): List<ContractCase> {
        val recorder = CaseRecorder(ValorBibleAudit.GROUP_AUDIO)

        recorder.check(
            "Mix buses", "seven buses at their stated levels",
            MixBus.entries.size == 7 &&
                MixBus.MASTER.levelDb == 0f && MixBus.SFX_WEAPONS.levelDb == -6f &&
                MixBus.SFX_IMPACTS.levelDb == -10f && MixBus.VOICE.levelDb == -5f &&
                MixBus.MUSIC.levelDb == -14f && MixBus.AMBIENCE.levelDb == -18f &&
                MixBus.UI.levelDb == -8f,
            observed(MixBus.MUSIC.levelDb, -14f)
        )
        recorder.check(
            "Limiter", "peaks are held at -1 dB",
            approxEquals(ValorAudioBible.clampToLimiter(3f), -1f),
            observed(ValorAudioBible.clampToLimiter(3f), -1f)
        )
        recorder.check(
            "Voice duck", "speech pulls music from -14 to -20 dB and leaves voice alone",
            approxEquals(ValorAudioBible.levelFor(MixBus.MUSIC, true), -20f) &&
                approxEquals(ValorAudioBible.levelFor(MixBus.VOICE, true), -5f),
            observed(ValorAudioBible.levelFor(MixBus.MUSIC, true), -20f)
        )
        recorder.check(
            "Gain conversion", "0 dB is unity and -6 dB is about 0.501",
            approxEquals(ValorAudioBible.dbToGain(0f), 1f) &&
                approxEquals(ValorAudioBible.dbToGain(-6f), 0.501f, 0.002f),
            observed(ValorAudioBible.dbToGain(-6f), 0.501f)
        )
        recorder.check(
            "Voice limit", "32 voices normally, 20 on low-end devices",
            ValorAudioBible.maxVoices(false) == 32 && ValorAudioBible.maxVoices(true) == 20,
            observed(ValorAudioBible.maxVoices(true), 20)
        )
        recorder.check(
            "Variation", "3-5 variants inside a 5% pitch window",
            ValorAudioBible.requiredVariantCount() == 3..5 &&
                approxEquals(ValorAudioBible.variationPitch(0), 0.95f) &&
                approxEquals(ValorAudioBible.variationPitch(2), 1f) &&
                approxEquals(ValorAudioBible.variationPitch(4), 1.05f),
            observed(ValorAudioBible.variationPitch(4), 1.05f)
        )
        recorder.check(
            "SFX categories", "nine categories with eight weapon sounds",
            SfxCategory.entries.size == 9 && SfxCategory.WEAPONS.sounds.size == 8 &&
                SfxCategory.AMBIENCE.sounds.contains("radio static"),
            observed(SfxCategory.entries.size, 9)
        )
        recorder.check(
            "Act scores", "six themes, three layers each, 2 s crossfade",
            ValorAudioBible.THEMES.size == 6 &&
                ValorAudioBible.themeFor(ArtAct.BOOT_CAMP).layers == listOf("Calm", "Training", "Success") &&
                ValorAudioBible.themeFor(ArtAct.DRONE_AGE).layers.last() == "Final boss" &&
                approxEquals(ValorAudioBible.CROSSFADE_SECONDS, 2f),
            observed(ValorAudioBible.themeFor(ArtAct.DRONE_AGE).layers.last(), "Final boss")
        )
        recorder.check(
            "VO naming", "VO_<Character>_<Act>_<Scene>_<Line>",
            ValorAudioBible.voClipName("Brooks", 2, "ambush", "line03") == "VO_Brooks_A2_ambush_line03",
            observed(ValorAudioBible.voClipName("Brooks", 2, "ambush", "line03"), "VO_Brooks_A2_ambush_line03")
        )
        recorder.check(
            "Mixer settings", "seven player-facing controls",
            ValorAudioBible.MIXER_SETTINGS.size == 7 && "headphone spatial mode" in ValorAudioBible.MIXER_SETTINGS,
            observed(ValorAudioBible.MIXER_SETTINGS.size, 7)
        )
        return recorder.result()
    }

    private fun uiCases(): List<ContractCase> {
        val recorder = CaseRecorder(ValorBibleAudit.GROUP_UI)

        recorder.check(
            "Typography", "body text is at least 16 sp and numbers are monospace",
            UiStyle.MIN_BODY_TEXT_SP == 16 && UiStyle.NUMBERS_FONT == "monospace",
            observed(UiStyle.MIN_BODY_TEXT_SP, 16)
        )
        recorder.check(
            "HUD inventory", "nineteen widgets with the crosshair dead centre",
            HudLayout.WIDGETS.size == 19 &&
                HudLayout.widget("crosshair")?.xFraction == 0.5f &&
                HudLayout.widget("crosshair")?.yFraction == 0.5f &&
                HudLayout.widget("ammo") != null,
            observed(HudLayout.WIDGETS.size, 19)
        )
        recorder.check(
            "HUD presets", "Default, Compact and Big at 1.0, 0.85 and 1.2",
            HudLayout.PRESETS.size == 3 && HudPreset.DEFAULT.scale == 1f &&
                HudPreset.COMPACT.scale == 0.85f && HudPreset.BIG.scale == 1.2f,
            observed(HudPreset.COMPACT.scale, 0.85f)
        )
        recorder.check(
            "Left-hand mode", "the minimap mirrors from 0.05 to 0.95",
            HudLayout.LEFT_HAND_MODE && HudLayout.widget("minimap")?.let {
                approxEquals(HudLayout.mirrored(it).xFraction, 0.95f)
            } == true,
            observed(HudLayout.widget("minimap")?.let { HudLayout.mirrored(it).xFraction }, 0.95f)
        )
        recorder.check(
            "Hit markers", "white hit, red headshot, skull kill",
            HudLayout.hitMarker(headshot = false, kill = false) == "white" &&
                HudLayout.hitMarker(headshot = true, kill = false) == "red" &&
                HudLayout.hitMarker(headshot = true, kill = true) == "skull (headshot)",
            observed(HudLayout.hitMarker(true, false), "red")
        )
        recorder.check(
            "Notifications", "top-centre, never covering the crosshair",
            HudLayout.NOTIFICATION_ANCHOR == "top-center" && HudLayout.NOTIFICATION_AVOIDS_CROSSHAIR &&
                HudLayout.DAMAGE_DIRECTION_ARCS && HudLayout.LOW_HEALTH_VIGNETTE,
            observed(HudLayout.NOTIFICATION_ANCHOR, "top-center")
        )
        recorder.check(
            "Screen flow", "seven menu branches, six settings tabs, ten-step campaign chain",
            ScreenFlow.MAIN_MENU_BRANCHES.size == 7 && ScreenFlow.SETTINGS_TABS.size == 6 &&
                ScreenFlow.CAMPAIGN_CHAIN.size == 10 &&
                ScreenFlow.CAMPAIGN_CHAIN.last() == ScreenId.ACT_MAP &&
                ScreenFlow.nextAfterGame() == ScreenId.RESULTS,
            observed(ScreenFlow.MAIN_MENU_BRANCHES.size, 7)
        )
        recorder.check(
            "Screen specs", "results lists six beats and pause offers checkpoint retry",
            ScreenFlow.specFor(ScreenId.RESULTS)?.requirements?.size == 6 &&
                ScreenFlow.specFor(ScreenId.PAUSE)?.requirements?.contains("Retry (from checkpoint)") == true &&
                ScreenFlow.specFor(ScreenId.GUNSMITH)?.requirements?.contains("Confirm") == true,
            observed(ScreenFlow.specFor(ScreenId.RESULTS)?.requirements?.size, 6)
        )
        recorder.check(
            "Feedback", "no dead taps and every interaction sounds",
            !FeedbackRules.DEAD_TAPS_ALLOWED && FeedbackRules.DISABLED_BUTTON_EXPLAINS_ON_TAP &&
                FeedbackRules.SOUND_ON_EVERY_INTERACTION && FeedbackRules.HAPTIC_TICKS_TOGGLE,
            observed(FeedbackRules.DEAD_TAPS_ALLOWED, false)
        )
        return recorder.result()
    }

    private fun accessibilityCases(): List<ContractCase> {
        val recorder = CaseRecorder(ValorBibleAudit.GROUP_ACCESS)

        recorder.check(
            "Subtitles", "three sizes, speaker names, optional box",
            SubtitleSize.entries.size == 3 && SubtitleSize.LARGE.scale > SubtitleSize.MEDIUM.scale &&
                AccessibilityProfile().subtitleSpeakerNames &&
                AccessibilityProfile().subtitleBackgroundBox &&
                approxEquals(
                    AccessibilityRules.subtitleTextSp(20, AccessibilityProfile(subtitleSize = SubtitleSize.LARGE)),
                    25f
                ),
            observed(SubtitleSize.entries.size, 3)
        )
        recorder.check(
            "Colour-blind modes", "protanopia, deuteranopia and tritanopia beyond standard",
            ColorBlindMode.entries.size == 4 && ColorBlindMode.entries.count { it != ColorBlindMode.STANDARD } == 3 &&
                ColorBlindMode.PROTANOPIA.enemyOutlineArgb == 0xFF3B82F6,
            observed(ColorBlindMode.PROTANOPIA.enemyOutlineArgb, 0xFF3B82F6)
        )
        recorder.check(
            "Outline option", "enemy outlines follow the selected palette",
            AccessibilityRules.ENEMY_OUTLINE_OPTION &&
                AccessibilityRules.outlineArgb(AccessibilityProfile(), enemy = true) == 0xFFC84C4C &&
                AccessibilityRules.outlineArgb(
                    AccessibilityProfile(colorBlindMode = ColorBlindMode.DEUTERANOPIA),
                    enemy = false
                ) == 0xFFF97316,
            observed(AccessibilityRules.outlineArgb(AccessibilityProfile(), true), 0xFFC84C4C)
        )
        recorder.check(
            "Reduced violence", "no blood and softer deaths",
            !AccessibilityProfile(reducedViolence = true).bloodEnabled &&
                AccessibilityProfile(reducedViolence = true).softDeathAnimations &&
                AccessibilityProfile().bloodEnabled,
            observed(AccessibilityProfile(reducedViolence = true).bloodEnabled, false)
        )
        recorder.check(
            "Input clamps", "sensitivity 0.1-3.0, dead zone 0-0.3, text scale 0.85-1.5",
            approxEquals(AccessibilityRules.clampSensitivity(5f), 3f) &&
                approxEquals(AccessibilityRules.clampDeadZone(-1f), 0f) &&
                approxEquals(AccessibilityRules.clampTextScale(0.1f), 0.85f),
            observed(AccessibilityRules.clampSensitivity(5f), 3f)
        )
        recorder.check(
            "Aim assist", "off is zero magnetism, high is 3.2 degrees",
            approxEquals(AccessibilityRules.magnetismDegrees(AimAssistLevel.OFF), 0f) &&
                approxEquals(AccessibilityRules.magnetismDegrees(AimAssistLevel.HIGH), 3.2f),
            observed(AccessibilityRules.magnetismDegrees(AimAssistLevel.HIGH), 3.2f)
        )
        recorder.check(
            "Safety and difficulty", "photosensitivity warning on first launch, difficulty changeable",
            AccessibilityRules.PHOTOSENSITIVITY_WARNING_ON_FIRST_LAUNCH &&
                AccessibilityRules.DIFFICULTY_CHANGEABLE_MID_CAMPAIGN &&
                AccessibilityRules.FULL_CONTROL_REMAP_GAMEPAD,
            observed(AccessibilityRules.PHOTOSENSITIVITY_WARNING_ON_FIRST_LAUNCH, true)
        )
        return recorder.result()
    }

    private fun localizationCases(): List<ContractCase> {
        val recorder = CaseRecorder(ValorBibleAudit.GROUP_ACCESS)

        recorder.check(
            "Locale table", "thirteen locales with Arabic as the only RTL pack",
            LocaleRegistry.LOCALES.size == 13 && LocaleRegistry.rtlLocales().size == 1 &&
                LocaleRegistry.isRtl("ar") && !LocaleRegistry.isRtl("ja") &&
                LocaleRegistry.localeFor("pt-BR")?.englishName == "Portuguese (BR)",
            observed(LocaleRegistry.LOCALES.size, 13)
        )
        recorder.check(
            "Text expansion", "layout reserves the full 40% allowance",
            approxEquals(LocaleRegistry.reservedWidth(100f), 140f) &&
                approxEquals(LocaleRegistry.TEXT_EXPANSION_MAX, 0.40f),
            observed(LocaleRegistry.reservedWidth(100f), 140f)
        )
        recorder.check(
            "Voice and subtitles", "English voice at launch, thirteen subtitle locales, pseudo-locale QA",
            LocaleRegistry.supportsVoiceAtLaunch("en") && !LocaleRegistry.supportsVoiceAtLaunch("hi") &&
                LocaleRegistry.subtitleLocaleCount() == 13 && LocaleRegistry.PSEUDO_LOCALE == "en-XA" &&
                LocaleRegistry.PSEUDO_LOCALE_QA && LocaleRegistry.RTL_LAYOUT_QA,
            observed(LocaleRegistry.supportsVoiceAtLaunch("hi"), false)
        )
        return recorder.result()
    }

    private fun pipelineCases(): List<ContractCase> {
        val recorder = CaseRecorder(ValorBibleAudit.GROUP_PIPELINE)

        recorder.check(
            "Naming prefixes", "thirteen prefixes and every asset must carry one",
            AssetPrefix.entries.size == 13 &&
                AssetPipeline.namingViolation("SM_Rock").isEmpty() &&
                AssetPipeline.namingViolation("Rock").isNotEmpty(),
            observed(AssetPrefix.entries.size, 13)
        )
        recorder.check(
            "Checklists", "seven import checks and nine art QA checks",
            AssetPipeline.IMPORT_CHECKLIST.size == 7 && AssetPipeline.ART_QA_CHECKLIST.size == 9 &&
                AssetPipeline.checklistComplete(AssetPipeline.IMPORT_CHECKLIST) &&
                !AssetPipeline.checklistComplete(AssetPipeline.IMPORT_CHECKLIST.drop(1)),
            observed(AssetPipeline.ART_QA_CHECKLIST.size, 9)
        )
        val complete = LicenseRecord(
            "SM_Rock", "https://example.org/rock", "Studio", LicenseKind.CC0, "2026-10-31",
            commercialUse = true, attributionRequired = false
        )
        val incomplete = complete.copy(sourceUrl = "")
        recorder.check(
            "License log", "complete commercial records ship, incomplete ones do not",
            complete.isComplete && complete.isShippable && !incomplete.isComplete && !incomplete.isShippable &&
                AssetPipeline.unloggedAssets(listOf("SM_Rock", "SM_Tree"), listOf(complete)) == listOf("SM_Tree"),
            observed(AssetPipeline.unloggedAssets(listOf("SM_Rock", "SM_Tree"), listOf(complete)), listOf("SM_Tree"))
        )
        recorder.check(
            "Placeholders", "same name, pivot and sockets so swaps never break references",
            AssetPipeline.PLACEHOLDER_SAME_NAME_AND_PIVOT && AssetPipeline.PLACEHOLDER_PREFAB_VARIANTS &&
                AssetPipeline.PLACEHOLDER_SAME_SOCKET_NAMES,
            observed(AssetPipeline.PLACEHOLDER_SAME_SOCKET_NAMES, true)
        )
        recorder.check(
            "AI art prompts", "five concept prompts, licensing checked before shipping",
            AiArtPrompts.PROMPTS.size == 5 && AiArtPrompts.CONCEPT_ONLY &&
                AiArtPrompts.LICENSE_CHECK_BEFORE_SHIPPING,
            observed(AiArtPrompts.PROMPTS.size, 5)
        )
        return recorder.result()
    }
}
