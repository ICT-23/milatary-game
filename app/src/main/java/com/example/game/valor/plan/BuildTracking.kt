package com.example.game.valor.plan

/**
 * Section 2: the four statuses every system row may carry. A row is only
 * EXISTS_WORKING when the file named beside it actually runs today.
 */
enum class SystemStatus(val displayName: String) {
    EXISTS_WORKING("EXISTS-WORKING"),
    EXISTS_PARTIAL("EXISTS-PARTIAL"),
    EXISTS_BROKEN("EXISTS-BROKEN"),
    MISSING("MISSING")
}

/** Section 3.1: one row of the BUILD_STATUS systems table. */
data class SystemRow(
    val system: String,
    val status: SystemStatus,
    val files: String,
    val notes: String
)

/** Section 2.3: a file the plan may extend but never overwrite. */
data class ProtectedAsset(val path: String, val why: String)

/**
 * Section 3.1: the status file, filled from the inventory this build actually
 * performed. The engine line says what the project is, not what the plan assumed.
 */
object BuildStatus {

    const val FILE_NAME = "BUILD_STATUS.md"
    const val LAST_UPDATED = "2026-10-31"
    const val ENGINE_LABEL = "Kotlin 2.2.10, Jetpack Compose (Unity plan translated to this stack)"

    val SYSTEMS: List<SystemRow> = listOf(
        SystemRow(
            "Player controller", SystemStatus.EXISTS_PARTIAL,
            "ui/EmberwatchViewModel.kt, MainActivity.kt",
            "Tap-driven Compose campaign loop; no third-person 3D controller yet"
        ),
        SystemRow(
            "Weapon system", SystemStatus.EXISTS_WORKING,
            "balance/Weapons.kt, plan/FiringPipeline.kt",
            "Shipped weapon catalogue plus the eight ordered fire checks"
        ),
        SystemRow(
            "Damage system", SystemStatus.EXISTS_WORKING,
            "balance/BattleMath.kt, plan/DamageSystem.kt",
            "Single damage entry point; zones, falloff, armour, helmet all delegated"
        ),
        SystemRow(
            "Enemy AI", SystemStatus.EXISTS_PARTIAL,
            "plan/EnemyAiLogic.kt",
            "Eight-state logic machine; no NavMesh agent or scene perception yet"
        ),
        SystemRow(
            "Mission manager", SystemStatus.EXISTS_WORKING,
            "game/MissionDefinition.kt, plan/MissionFsm.kt",
            "Ten-state machine, objectives, watchdog, checkpoints, single grant"
        ),
        SystemRow(
            "Rank/XP", SystemStatus.EXISTS_WORKING,
            "balance/Ranks.kt, plan/ProgressionService.kt",
            "20 ranks, multi-rank-up loop, XP only through ProgressionService"
        ),
        SystemRow(
            "Economy", SystemStatus.EXISTS_WORKING,
            "balance/Economy.kt, plan/EconomyService.kt",
            "Atomic spend with rollback and duplicate-tap guard"
        ),
        SystemRow(
            "Gunsmith", SystemStatus.EXISTS_WORKING,
            "balance/GunsmithProgression.kt, plan/GunsmithLogic.kt",
            "Levels 1-10 with no skipping; preview never touches live stats"
        ),
        SystemRow(
            "Save/Load", SystemStatus.EXISTS_PARTIAL,
            "plan/SaveSystem.kt, data/EmberwatchDatabase.kt",
            "Atomic JSON save logic proven in memory; platform file binding still to do"
        ),
        SystemRow(
            "HUD/Menus", SystemStatus.EXISTS_WORKING,
            "ux/HudSpec.kt, plan/UiRules.kt, ui/screens",
            "19-widget layout, truth-only readout, queued popups, editable layout"
        ),
        SystemRow(
            "Squad AI", SystemStatus.EXISTS_PARTIAL,
            "plan/SquadStealth.kt",
            "Five commands, downed timer and squad cap; no ally pathing yet"
        ),
        SystemRow(
            "Drones", SystemStatus.EXISTS_PARTIAL,
            "gdd/Support.kt, plan/DroneLogic.kt",
            "Rank gate, charges, cooldowns, strike delay, FPV battery; no flight model"
        ),
        SystemRow(
            "Story/Dialogue", SystemStatus.EXISTS_WORKING,
            "gdd/Story.kt, plan/StoryFlow.kt",
            "Beats once, confirm-only choices, three reachable endings"
        ),
        SystemRow(
            "Audio", SystemStatus.EXISTS_PARTIAL,
            "game/AudioFeedback.kt",
            "Event cues only; no mixer, layering or ad ducking"
        ),
        SystemRow(
            "VFX", SystemStatus.MISSING,
            "-",
            "No particle or decal layer exists in this stack yet"
        )
    )

    val PROTECTED: List<ProtectedAsset> = listOf(
        ProtectedAsset(
            "balance/BattleMath.kt",
            "The only source of zone multipliers, falloff, armour and helmet numbers"
        ),
        ProtectedAsset(
            "balance/Ranks.kt",
            "The 20-rank table; XP and rank labels read from here"
        ),
        ProtectedAsset(
            "balance/Economy.kt",
            "Currency names and reward tables; never re-typed elsewhere"
        ),
        ProtectedAsset(
            "ux/HudSpec.kt",
            "The 19 HUD anchors; the plan's UI rules read this layout"
        ),
        ProtectedAsset(
            "game/MissionDefinition.kt",
            "Shipped mission content; new content is additive only"
        ),
        ProtectedAsset(
            "MainActivity.kt",
            "Navigation shell; new screens are added as branches, never rewrites"
        )
    )

    val OPEN_QUESTIONS: List<String> = listOf(
        "Helmet tier 3: plan says max(1, 2.5 - 0.5*tier) = 1.0, balance.ArmorMath says 1.2 - " +
            "the shipped table wins (Section 2.4)",
        "Aim assist: plan prints Off/Low/High, the design bible lists four settings - the plan's three ship",
        "Unity physics layers have no Android equivalent; PhysicsLayer enum is the mapping",
        "No Gradle wrapper or local.properties in the project, so this build is verified statically",
        "Drones: charge counts per type are not printed in either document"
    )

    val NEXT_TASK: List<String> = listOf(
        "Bind SaveStore to a real Android file implementation and keep the same atomic sequence",
        "Surface the plan gate in the debug panel so a device run reports the same case list"
    )

    fun rows(): List<SystemRow> = SYSTEMS

    fun status(system: String): SystemStatus? = SYSTEMS.firstOrNull { it.system == system }?.status

    fun counts(): Map<SystemStatus, Int> = SystemStatus.entries.associateWith { status ->
        SYSTEMS.count { it.status == status }
    }

    fun ofStatus(status: SystemStatus): List<SystemRow> = SYSTEMS.filter { it.status == status }

    fun allHaveFiles(): Boolean =
        SYSTEMS.all { it.status == SystemStatus.MISSING || it.files != "-" }

    fun protectedPaths(): List<String> = PROTECTED.map { it.path }

    fun isProtected(path: String): Boolean = PROTECTED.any { it.path == path }
}

/**
 * Section 3.2: the changelog every task appends to. The four headings are fixed
 * so a reader can diff two entries without guessing where a change belongs.
 */
object ChangelogSpec {

    const val FILE_NAME = "CHANGELOG.md"

    val SECTIONS: List<String> = listOf("Added", "Modified", "Removed", "Tests")

    fun hasSection(name: String): Boolean = SECTIONS.any { it.equals(name.trim(), ignoreCase = true) }

    fun sections(): List<String> = SECTIONS
}
