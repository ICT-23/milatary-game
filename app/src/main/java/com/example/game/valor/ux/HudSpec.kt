package com.example.game.valor.ux

/**
 * Section 10.1 - 10.2: the shared style and typography tokens.
 */
object UiStyle {
    const val PANEL_STYLE = "dark translucent panels"
    const val HEADING_STYLE = "bold stencil / condensed sans"
    const val BODY_STYLE = "clean readable sans"
    const val ICON_STYLE = "thin line icons"
    const val ACCENT = "brass / olive"
    const val MIN_BODY_TEXT_SP = 16
    const val NUMBERS_FONT = "monospace"
    const val ONE_CONSISTENT_ICON_SET = true
    const val HIGH_CONTRAST_TEXT = true
    const val RTL_SUPPORT = true
    const val LOCALIZATION_FALLBACK_FONTS = true
}

/**
 * Section 10.3: one HUD element and its normalized anchor inside the safe area.
 */
data class HudWidget(
    val id: String,
    val label: String,
    val xFraction: Float,
    val yFraction: Float
)

enum class HudPreset(val displayName: String, val scale: Float, val defaultOpacity: Float) {
    DEFAULT("Default", 1.0f, 0.85f),
    COMPACT("Compact", 0.85f, 0.80f),
    BIG("Big", 1.2f, 0.90f)
}

object HudLayout {

    /** Mirrors the landscape layout in Section 10.3, read left-to-right, top-down. */
    val WIDGETS: List<HudWidget> = listOf(
        HudWidget("minimap", "Minimap", 0.05f, 0.07f),
        HudWidget("compass", "Compass bar", 0.50f, 0.07f),
        HudWidget("objectives", "Objective list", 0.92f, 0.07f),
        HudWidget("health", "Health bar", 0.07f, 0.15f),
        HudWidget("armor", "Armor bar", 0.21f, 0.15f),
        HudWidget("pause", "Pause", 0.95f, 0.15f),
        HudWidget("crosshair", "Crosshair", 0.50f, 0.50f),
        HudWidget("move_stick", "Move stick", 0.09f, 0.82f),
        HudWidget("fire", "Fire", 0.88f, 0.82f),
        HudWidget("ads", "ADS", 0.96f, 0.82f),
        HudWidget("grenade", "Grenade", 0.07f, 0.92f),
        HudWidget("medkit", "Medkit", 0.21f, 0.92f),
        HudWidget("ammo", "Ammo counter", 0.50f, 0.92f),
        HudWidget("weapon", "Weapon", 0.58f, 0.92f),
        HudWidget("reload", "Reload", 0.86f, 0.92f),
        HudWidget("swap", "Swap", 0.95f, 0.92f),
        HudWidget("crouch", "Crouch", 0.09f, 0.97f),
        HudWidget("jump", "Jump", 0.88f, 0.97f),
        HudWidget("cover", "Cover", 0.96f, 0.97f)
    )

    val PRESETS: List<HudPreset> = HudPreset.entries
    val CONTEXT_PROMPTS: List<String> = listOf("Hold to revive", "Plant charge")

    const val MOVABLE = true
    const val RESIZABLE = true
    const val OPACITY_SLIDER = true
    const val LEFT_HAND_MODE = true
    const val NOTIFICATION_ANCHOR = "top-center"
    const val NOTIFICATION_AVOIDS_CROSSHAIR = true
    const val DAMAGE_DIRECTION_ARCS = true
    const val LOW_HEALTH_VIGNETTE = true

    fun widget(id: String): HudWidget? = WIDGETS.firstOrNull { it.id == id }

    /** Left-hand mode mirrors every anchor across the vertical axis. */
    fun mirrored(widget: HudWidget): HudWidget = widget.copy(xFraction = 1f - widget.xFraction)

    fun scaleFor(preset: HudPreset): Float = preset.scale

    fun applyOpacity(opacity: Float): Float = opacity.coerceIn(0f, 1f)

    /** Section 10.3: white = hit, red = headshot, skull = kill. */
    fun hitMarker(headshot: Boolean, kill: Boolean): String = when {
        kill && headshot -> "skull (headshot)"
        kill -> "skull"
        headshot -> "red"
        else -> "white"
    }
}

/**
 * Section 10.4: the screen graph.
 */
enum class ScreenId(val title: String) {
    SPLASH("Splash"),
    MAIN_MENU("Main Menu"),
    CAMPAIGN("Campaign"),
    ACT_MAP("Act Map"),
    MISSION_BRIEFING("Mission Briefing"),
    LOADOUT("Loadout"),
    GAME("Game"),
    RESULTS("Results"),
    RANK_UP("Rank-Up"),
    ACADEMY("Academy"),
    ARMORY("Armory"),
    GUNSMITH("Gunsmith"),
    SQUAD("Squad"),
    SKILL_TREE("Skill Tree"),
    MEDALS("Medals"),
    SURVIVAL("Survival"),
    DAILY_OP("Daily Op"),
    WEEKLY("Weekly"),
    BOSS_RUSH("Boss Rush"),
    SHOP("Shop"),
    SETTINGS("Settings"),
    GRAPHICS("Graphics"),
    CONTROLS("Controls"),
    AUDIO("Audio"),
    ACCESSIBILITY("Accessibility"),
    LANGUAGE("Language"),
    PRIVACY("Privacy"),
    PAUSE("Pause")
}

data class ScreenSpec(val screen: ScreenId, val requirements: List<String>)

object ScreenFlow {

    val CAMPAIGN_CHAIN: List<ScreenId> = listOf(
        ScreenId.SPLASH, ScreenId.MAIN_MENU, ScreenId.CAMPAIGN, ScreenId.ACT_MAP,
        ScreenId.MISSION_BRIEFING, ScreenId.LOADOUT, ScreenId.GAME, ScreenId.RESULTS,
        ScreenId.RANK_UP, ScreenId.ACT_MAP
    )

    /** The seven branches the main menu offers. */
    val MAIN_MENU_BRANCHES: List<ScreenId> = listOf(
        ScreenId.CAMPAIGN, ScreenId.ACADEMY, ScreenId.ARMORY, ScreenId.SQUAD,
        ScreenId.SURVIVAL, ScreenId.SHOP, ScreenId.SETTINGS
    )

    val SETTINGS_TABS: List<ScreenId> = listOf(
        ScreenId.GRAPHICS, ScreenId.CONTROLS, ScreenId.AUDIO,
        ScreenId.ACCESSIBILITY, ScreenId.LANGUAGE, ScreenId.PRIVACY
    )

    val SPECS: List<ScreenSpec> = listOf(
        ScreenSpec(
            ScreenId.RESULTS,
            listOf(
                "stars animate", "XP bar fills", "cash counter", "medals earned",
                "Double reward (ad) button", "Next / Retry"
            )
        ),
        ScreenSpec(
            ScreenId.RANK_UP,
            listOf("3D insignia reveal", "new unlocks list", "skill point notice")
        ),
        ScreenSpec(
            ScreenId.ARMORY,
            listOf(
                "left list of weapons", "center rotating 3D model",
                "right stat bars (current vs preview)", "bottom slot buttons"
            )
        ),
        ScreenSpec(
            ScreenId.GUNSMITH,
            listOf("tap slot", "pick part", "shows cost and stat delta", "Confirm")
        ),
        ScreenSpec(
            ScreenId.MISSION_BRIEFING,
            listOf(
                "3D map table with markers", "objectives", "enemy intel",
                "recommended loadout", "difficulty selector"
            )
        ),
        ScreenSpec(
            ScreenId.PAUSE,
            listOf("Resume", "Settings", "Retry (from checkpoint)", "Quit", "shows objectives")
        )
    )

    fun specFor(screen: ScreenId): ScreenSpec? = SPECS.firstOrNull { it.screen == screen }

    fun nextAfterGame(): ScreenId = ScreenId.RESULTS
}

/**
 * Section 10.6: feedback and polish.
 */
object FeedbackRules {
    const val BUTTON_PRESS_ANIMATION = true
    const val HAPTIC_TICKS_TOGGLE = true
    const val SOUND_ON_EVERY_INTERACTION = true
    const val DEAD_TAPS_ALLOWED = false
    const val DISABLED_BUTTON_EXPLAINS_ON_TAP = true
    const val LOADING_SCREENS_WITH_TIPS_AND_LORE = true
}
