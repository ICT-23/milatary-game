package com.example.game.valor.ux

/**
 * Section 11: subtitle sizing and presentation.
 */
enum class SubtitleSize(val displayName: String, val scale: Float) {
    SMALL("S", 0.85f),
    MEDIUM("M", 1.0f),
    LARGE("L", 1.25f)
}

/** Section 11: colour-blind palettes for enemy and friendly outlines and markers. */
enum class ColorBlindMode(
    val displayName: String,
    val enemyOutlineArgb: Long,
    val friendlyOutlineArgb: Long
) {
    STANDARD("Standard", 0xFFC84C4C, 0xFF38D39F),
    PROTANOPIA("Protanopia", 0xFF3B82F6, 0xFFF59E0B),
    DEUTERANOPIA("Deuteranopia", 0xFF06B6D4, 0xFFF97316),
    TRITANOPIA("Tritanopia", 0xFFF43F5E, 0xFF22C55E)
}

enum class AimAssistLevel(val displayName: String, val strength: Float) {
    OFF("Off", 0f),
    LOW("Low", 0.25f),
    MEDIUM("Medium", 0.5f),
    HIGH("High", 0.8f)
}

enum class HoldToggle { HOLD, TOGGLE }

/** Section 11: the full accessibility state the settings screen edits. */
data class AccessibilityProfile(
    val subtitleSize: SubtitleSize = SubtitleSize.MEDIUM,
    val subtitleBackgroundBox: Boolean = true,
    val subtitleSpeakerNames: Boolean = true,
    val colorBlindMode: ColorBlindMode = ColorBlindMode.STANDARD,
    val enemyOutlines: Boolean = false,
    val crosshairColorArgb: Long = 0xFFFFFFFF,
    val crosshairSizeSp: Int = 18,
    val crosshairShape: String = "cross",
    val aimAssist: AimAssistLevel = AimAssistLevel.MEDIUM,
    val adsBehaviour: HoldToggle = HoldToggle.HOLD,
    val crouchBehaviour: HoldToggle = HoldToggle.TOGGLE,
    val sensitivity: Float = 1f,
    val deadZone: Float = 0.1f,
    val gyroEnabled: Boolean = false,
    val reduceCameraShake: Boolean = false,
    val reduceMotionBlur: Boolean = false,
    val reduceFlashEffects: Boolean = false,
    val reducedViolence: Boolean = false,
    val oneHandedLayout: Boolean = false,
    val hapticsEnabled: Boolean = true,
    val textScale: Float = 1f,
    val controlRemap: Boolean = false
) {
    /** Reduced violence removes blood and softens death animations. */
    val bloodEnabled: Boolean get() = !reducedViolence
    val softDeathAnimations: Boolean get() = reducedViolence
}

object AccessibilityRules {
    const val PHOTOSENSITIVITY_WARNING_ON_FIRST_LAUNCH = true
    const val DIFFICULTY_CHANGEABLE_MID_CAMPAIGN = true
    const val FULL_CONTROL_REMAP_GAMEPAD = true
    const val TEXT_SIZE_SCALING = true
    const val ONE_HANDED_LAYOUT_PRESET = true
    const val ENEMY_OUTLINE_OPTION = true
    const val SUBTITLE_BACKGROUND_BOX_OPTION = true

    val SENSITIVITY_RANGE: ClosedFloatingPointRange<Float> = 0.1f..3f
    val DEAD_ZONE_RANGE: ClosedFloatingPointRange<Float> = 0f..0.3f
    val TEXT_SCALE_RANGE: ClosedFloatingPointRange<Float> = 0.85f..1.5f

    val CROSSHAIR_COLORS_ARGB: List<Long> =
        listOf(0xFFFFFFFF, 0xFF38D39F, 0xFFE7A83E, 0xFF4FA3E3, 0xFFFF4D4D)

    val CROSSHAIR_SIZES_SP: List<Int> = listOf(14, 18, 24)

    val CROSSHAIR_SHAPES: List<String> = listOf("cross", "dot", "circle", "chevron")

    fun clampSensitivity(value: Float): Float = value.coerceIn(SENSITIVITY_RANGE)

    fun clampDeadZone(value: Float): Float = value.coerceIn(DEAD_ZONE_RANGE)

    fun clampTextScale(value: Float): Float = value.coerceIn(TEXT_SCALE_RANGE)

    fun subtitleTextSp(baseSp: Int, profile: AccessibilityProfile): Float =
        baseSp * profile.subtitleSize.scale * profile.textScale

    /** Aim assist never turns a miss into a hit; it scales the magnetism window only. */
    fun magnetismDegrees(level: AimAssistLevel): Float = level.strength * 4f

    fun outlineArgb(profile: AccessibilityProfile, enemy: Boolean): Long =
        if (enemy) profile.colorBlindMode.enemyOutlineArgb else profile.colorBlindMode.friendlyOutlineArgb
}

/**
 * Section 12: one supported locale.
 */
data class GameLocale(
    val code: String,
    val englishName: String,
    val nativeName: String,
    val rtl: Boolean = false
)

object LocaleRegistry {

    val LOCALES: List<GameLocale> = listOf(
        GameLocale("en", "English", "English"),
        GameLocale("hi", "Hindi", "\u0939\u093F\u0928\u094D\u0926\u0940"),
        GameLocale("es", "Spanish", "Espa\u00F1ol"),
        GameLocale("pt-BR", "Portuguese (BR)", "Portugu\u00EAs (BR)"),
        GameLocale("fr", "French", "Fran\u00E7ais"),
        GameLocale("de", "German", "Deutsch"),
        GameLocale("ru", "Russian", "\u0420\u0443\u0441\u0441\u043A\u0438\u0439"),
        GameLocale("ar", "Arabic", "\u0627\u0644\u0639\u0631\u0628\u064A\u0629", rtl = true),
        GameLocale("id", "Indonesian", "Bahasa Indonesia"),
        GameLocale("tr", "Turkish", "T\u00FCrk\u00E7e"),
        GameLocale("ja", "Japanese", "\u65E5\u672C\u8A9E"),
        GameLocale("ko", "Korean", "\uD55C\uAD6D\uC5B4"),
        GameLocale("zh-CN", "Simplified Chinese", "\u7B80\u4F53\u4E2D\u6587")
    )

    const val LAUNCH_VOICE_LOCALE = "en"
    const val PSEUDO_LOCALE = "en-XA"
    const val TEXT_EXPANSION_MIN = 0.30f
    const val TEXT_EXPANSION_MAX = 0.40f
    const val UNITS_METRIC = true
    const val IMPERIAL_OPTIONAL = true
    const val LOCALIZED_NUMBERS_DATES_CURRENCY = true
    const val PSEUDO_LOCALE_QA = true
    const val RTL_LAYOUT_QA = true

    fun localeFor(code: String): GameLocale? =
        LOCALES.firstOrNull { it.code.equals(code.trim(), ignoreCase = true) }

    fun rtlLocales(): List<GameLocale> = LOCALES.filter { it.rtl }

    fun isRtl(code: String): Boolean = localeFor(code)?.rtl == true

    /** Layout must survive the largest permitted string expansion. */
    fun reservedWidth(baseWidth: Float): Float = baseWidth * (1f + TEXT_EXPANSION_MAX)

    fun pseudoLocalized(text: String): String = "\u00C5\u00DF\u00E7 " + text + " \u00E5\u00DF\u00E7"

    fun supportsVoiceAtLaunch(code: String): Boolean =
        code.equals(LAUNCH_VOICE_LOCALE, ignoreCase = true)

    fun subtitleLocaleCount(): Int = LOCALES.size
}
