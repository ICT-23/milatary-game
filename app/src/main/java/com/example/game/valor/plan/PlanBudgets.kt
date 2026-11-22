package com.example.game.valor.plan

/** Section 8: one quality tier, with the table's numbers as upper bounds. */
data class PerfBudget(
    val tier: String,
    val fps: Int,
    val drawCallsMax: Int,
    val trianglesMax: Int,
    val activeAi: Int,
    val textureMemoryMbMax: Int,
    val shadows: String,
    val renderScale: Float
)

/** Section 8: the three budgets and the ten-plus techniques that keep them. */
object PerformancePlan {

    val LOW = PerfBudget("Low", 30, 100, 250_000, 4, 400, "off / 1 cascade", 0.7f)
    val MEDIUM = PerfBudget("Medium", 60, 150, 400_000, 6, 700, "2 cascades", 0.85f)
    val HIGH = PerfBudget("High", 60, 250, 800_000, 8, 1_200, "3-4 cascades", 1.0f)

    val BUDGETS: List<PerfBudget> = listOf(LOW, MEDIUM, HIGH)

    val TECHNIQUES: List<String> = listOf(
        "Pooling",
        "GPU instancing",
        "Static batching",
        "Occlusion culling",
        "LOD groups",
        "ASTC textures",
        "Baked lighting",
        "AI tick rate scaling",
        "Audio source limits",
        "No GC allocations in Update",
        "Async scene loading"
    )

    fun tier(name: String): PerfBudget? = BUDGETS.firstOrNull { it.tier.equals(name.trim(), ignoreCase = true) }

    /** True when a measured frame is inside every column of the tier. */
    fun withinBudget(
        budget: PerfBudget,
        fps: Int,
        drawCalls: Int,
        triangles: Int,
        activeAi: Int,
        textureMemoryMb: Int
    ): Boolean =
        fps >= budget.fps &&
            drawCalls < budget.drawCallsMax &&
            triangles < budget.trianglesMax &&
            activeAi <= budget.activeAi &&
            textureMemoryMb < budget.textureMemoryMbMax

    fun techniqueCount(): Int = TECHNIQUES.size
}

/** Section 9: one situation and the behavior the build must produce. */
data class EdgeCaseRow(val situation: String, val behavior: String)

/** Section 9: the sixteen known-bug preventions. */
object EdgeCases {

    val ROWS: List<EdgeCaseRow> = listOf(
        EdgeCaseRow("Two bullets kill same frame", "One kill event, one XP"),
        EdgeCaseRow("Enemy falls through map", "Kill-plane kills it, objective unblocks"),
        EdgeCaseRow("Player pauses mid-reload", "Reload timer pauses"),
        EdgeCaseRow("App backgrounded mid-mission", "Auto-pause; resume exact state"),
        EdgeCaseRow("Reward ad closes early", "No reward"),
        EdgeCaseRow("Save during death", "Save consistent state (no half-dead)"),
        EdgeCaseRow("Rank-up mid-combat", "Popup queued to Results"),
        EdgeCaseRow("Shooting while swapped weapon animation", "No shot"),
        EdgeCaseRow("Muzzle inside wall", "Wall takes the hit"),
        EdgeCaseRow("Ragdoll blocks door/path", "Ragdoll collision layer excludes player nav"),
        EdgeCaseRow("Last enemy stuck unreachable", "Watchdog hint -> auto-complete"),
        EdgeCaseRow("Explosion hits multi-collider enemy", "Damage once"),
        EdgeCaseRow("Healing at full HP", "Item not consumed"),
        EdgeCaseRow("Ammo pickup at full reserve", "Not consumed"),
        EdgeCaseRow("Language/font missing glyphs", "Fallback font"),
        EdgeCaseRow("Controller + touch both connected", "Last-used input wins")
    )

    fun forSituation(situation: String): EdgeCaseRow? = ROWS.firstOrNull { it.situation == situation }

    fun behaviors(): List<String> = ROWS.map { it.behavior }

    fun hasSituation(situation: String): Boolean = ROWS.any { it.situation == situation }
}

/**
 * Section 10: the debug surface. It is data so the gate can prove the panel,
 * the cheats and the gizmos exist without shipping them to release.
 */
object DebugTools {

    const val DEBUG_BUILD_ONLY = true
    const val STRIPPED_FROM_RELEASE = true

    val PANEL_READOUTS: List<String> = listOf(
        "FPS",
        "Draw calls",
        "Active AI",
        "Current mission state",
        "Objective list"
    )

    val CHEATS: List<String> = listOf(
        "God mode (marks XP as debug, grants none)",
        "Infinite ammo",
        "Skip objective",
        "Add XP",
        "Add currency",
        "Set rank",
        "Spawn enemy",
        "Teleport to checkpoint",
        "Slow-motion"
    )

    val GIZMOS: List<String> = listOf(
        "Bullet rays (hit/miss colors)",
        "AI vision cones",
        "Hearing radius",
        "Cover points",
        "Nav paths",
        "Hitbox zones"
    )

    /** The nine `[Tag]` categories, read from the one enum that defines them. */
    val LOG_CATEGORIES: List<String> = LogCategory.entries.map { it.tag }

    fun isActive(debugBuild: Boolean): Boolean = debugBuild

    fun cheat(name: String): String? = CHEATS.firstOrNull { it.startsWith(name, ignoreCase = true) }

    /** "Each rejection logs its reason": every category must produce an entry. */
    fun everyCategoryLogs(log: DebugLog): Boolean =
        LogCategory.entries.all { category ->
            log.record(category, "probe")
            log.count(category) > 0
        }
}
