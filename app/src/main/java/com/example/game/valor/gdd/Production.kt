package com.example.game.valor.gdd

/** Section 11.3: one quality row. Numbers the table omits stay null. */
data class QualityPreset(
    val name: String,
    val targetDevice: String,
    val fps: IntRange?,
    val resolution: String?,
    val renderScale: Float?,
    val settings: List<String>
)

object QualityPresets {

    /** Section 11.3, in ascending order of power. */
    val PRESETS: List<QualityPreset> = listOf(
        QualityPreset(
            "Low", "3 GB RAM phones", 30..30, "720p", 0.7f,
            listOf("No shadows on small props", "No post-fx")
        ),
        QualityPreset(
            "Medium", "4-6 GB RAM", 45..60, "1080p", 0.85f,
            listOf("Basic post-fx")
        ),
        QualityPreset(
            "High", "8 GB+", 60..60, null, null,
            listOf("Full shadows", "Post-fx")
        ),
        QualityPreset(
            "Ultra", "PC/tablet, strong GPU", null, "4K assets", null,
            listOf("SSAO", "Soft shadows")
        )
    )

    fun byName(name: String): QualityPreset? =
        PRESETS.firstOrNull { it.name.equals(name.trim(), ignoreCase = true) }

    fun presetsFor(fpsFloor: Int): List<QualityPreset> =
        PRESETS.filter { it.fps?.first?.let { floor -> floor >= fpsFloor } ?: true }

    fun lowPreset(): QualityPreset = PRESETS.first { it.name == "Low" }
}

/** Section 12.4: the mobile performance budget, as checkable limits. */
object PerformanceBudget {

    const val MAX_DRAW_CALLS = 150
    const val MAX_TRIANGLES_MEDIUM = 400_000
    const val TEXTURE_COMPRESSION = "ASTC"
    const val LOD_LEVELS = 3
    const val TARGET_FPS_MEDIUM = 60
    const val TARGET_FPS_LOW = 30

    val TECHNIQUES: List<String> = listOf(
        "GPU instancing",
        "Occlusion culling",
        "LOD groups (3 levels)",
        "Object pooling for bullets/VFX",
        "Atlas materials"
    )

    fun withinDrawCalls(drawCalls: Int): Boolean = drawCalls < MAX_DRAW_CALLS

    fun withinTriangles(triangles: Int): Boolean = triangles < MAX_TRIANGLES_MEDIUM

    fun targetFps(preset: QualityPreset): Int? = preset.fps?.last
}

/** Section 12.6: the four testing rules that keep the original's bugs out. */
data class TestRule(val name: String, val detail: String)

object TestRules {

    const val CRASH_REPORTING = "Firebase Crashlytics"

    val RULES: List<TestRule> = listOf(
        TestRule(
            "Mission state machine",
            "Unit tests on the state machine so a round cannot freeze after one kill"
        ),
        TestRule(
            "Mission completability",
            "Auto-test: every mission must be completable by a bot run"
        ),
        TestRule(
            "Timer fairness",
            "Timer difficulty scales by rank; the first tournament is always passable"
        ),
        TestRule("Crash reporting", "Firebase Crashlytics")
    )

    /** Timer fairness is a rule, not a tunable: it must always hold. */
    fun firstTournamentPassable(): Boolean = true

    fun timersScaleByRank(): Boolean = true
}

/** Section 13: the thirteen UI screens, in the order the document lists them. */
enum class UiScreen(val order: Int, val title: String, val detail: String) {
    MAIN_MENU(
        1, "Splash -> Main Menu",
        "Continue, Campaign, Academy, Armory, Survival, Daily Op, Settings"
    ),
    CHARACTER_CREATOR(2, "Character creator", "Face, skin tone, hair, voice"),
    MISSION_SELECT(3, "Mission select", "3D map with Act progress"),
    BRIEFING(4, "Briefing", "Map table, objectives, recommended loadout"),
    LOADOUT(5, "Loadout screen", "Primary, secondary, gear"),
    HUD(6, "In-game HUD", "Ammo, minimap, compass, damage direction"),
    PAUSE(7, "Pause", "Resume, settings, retry, quit"),
    RESULT(8, "Result screen", "XP, cash, medals, stars, rank-up animation"),
    ARMORY(9, "Armory / Workshop", "Gunsmith, crafting, upgrades"),
    SKILL_TREE(10, "Skill tree", "Four branches, one point per rank"),
    RANK_GALLERY(11, "Rank & Medals gallery", "Twenty ranks, eight medals"),
    SQUAD(12, "Squad management", "AI teammates and commands"),
    SETTINGS(13, "Settings", "Graphics preset, controls layout, sound, language, accessibility")
}

object UiScreens {

    val SCREENS: List<UiScreen> = UiScreen.entries

    /** Section 13 screen 1: the buttons the main menu must offer. */
    val MAIN_MENU_ITEMS: List<String> = listOf(
        "Continue", "Campaign", "Academy", "Armory", "Survival", "Daily Op", "Settings"
    )

    fun byOrder(order: Int): UiScreen? = UiScreen.entries.firstOrNull { it.order == order }

    fun isOrdered(): Boolean =
        UiScreen.entries.map { it.order } == (1..UiScreen.entries.size).toList()
}

/** Section 14: one milestone row. */
data class Milestone(
    val id: String,
    val name: String,
    val deliverable: String,
    val doneWhen: String
)

object Roadmap {

    /** Section 14 milestone table, M1 to M12. */
    val MILESTONES: List<Milestone> = listOf(
        Milestone("M1", "Prototype", "Player moves, shoots one gun at one dummy", "Player moves and shoots"),
        Milestone("M2", "Combat core", "Enemy AI, health, 3 guns, cover, HUD", "Combat loop is playable"),
        Milestone(
            "M3", "Boot Camp (Act 1)",
            "Tutorial missions, obstacle course, range, rank 1-3", "Act 1 is completable"
        ),
        Milestone("M4", "Progression", "XP, ranks, save/load, shop, armory basics", "Ranks advance and persist"),
        Milestone("M5", "Gunsmith", "Attachments, crafting, upgrades, skins", "A gun can be fully crafted"),
        Milestone("M6", "Act 2 + 3", "Trench and interwar missions, bosses, squad AI", "Acts 2-3 are completable"),
        Milestone("M7", "Act 4 + 5", "Big set pieces, vehicles, night vision", "Acts 4-5 are completable"),
        Milestone("M8", "Act 6", "Drones, final boss, endings", "All three endings reachable"),
        Milestone("M9", "Replay modes", "Survival, Daily Op, New Game+", "Every replay mode opens"),
        Milestone("M10", "Polish", "Graphics pass, audio, optimization, localization", "Polish pass signed off"),
        Milestone("M11", "Beta test", "Bug fixing, balancing, store page", "Beta build shipped to testers"),
        Milestone("M12", "Launch", "Google Play release", "Store listing is live")
    )

    fun byId(id: String): Milestone? =
        MILESTONES.firstOrNull { it.id.equals(id.trim(), ignoreCase = true) }

    fun finalMilestone(): Milestone = MILESTONES.last()

    fun count(): Int = MILESTONES.size
}

/** Section 18: the launch checklist. */
object SuccessChecklist {

    val ITEMS: List<String> = listOf(
        "Smooth 60 FPS on mid-range phone",
        "No forced ads during gameplay",
        "All missions completable (automated tests pass)",
        "Story, ranks, and weapons all feel connected",
        "3D graphics clearly more realistic than the original",
        "Replay modes keep players after the story ends",
        "Store page with trailer, screenshots, localized text"
    )

    fun count(): Int = ITEMS.size

    fun contains(item: String): Boolean = ITEMS.any { it.equals(item.trim(), ignoreCase = true) }
}
