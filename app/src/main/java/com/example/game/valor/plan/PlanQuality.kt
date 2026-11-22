package com.example.game.valor.plan

/** Section 7.1: one pure-logic area and the tests it must carry. */
data class UnitTestArea(val area: String, val tests: String)

/** Section 7.4: an original bug and the test that guards against it returning. */
data class RegressionRow(val originalBug: String, val ourTest: String)

/** Sections 7 and 12: the test plan and the definition of done. */
object PlanQuality {

    val UNIT_AREAS: List<UnitTestArea> = listOf(
        UnitTestArea("DamageSystem", "zone mult, falloff, armor, helmet, invulnerable, dead target, clamp"),
        UnitTestArea("Ammo", "reload math, cancel, pickup caps, swap keeps state"),
        UnitTestArea("Attachments", "modifier order, recompute from base, clamp, incompatibility"),
        UnitTestArea("Progression", "multi-rank-up, no negative XP, rewards once, prestige"),
        UnitTestArea("Economy", "atomic spend, rollback, double-tap guard"),
        UnitTestArea("Save", "write/read, corruption fallback, migration chain"),
        UnitTestArea("Mission FSM", "valid/invalid transitions, objective completion, fail conditions, watchdog"),
        UnitTestArea("Enemy registry", "counts correct after kill/despawn/fall")
    )

    val PLAY_MODE_TESTS: List<String> = listOf(
        "Range scene: fire at dummies through wall, in open, at ally, at dead body",
        "Explosion through wall; same entity double-hit dedupe",
        "Enemy sees player only with LOS; AI miss rate within target band",
        "Mission complete bot; random-order kills",
        "Full flow: menu -> mission -> result -> rank-up -> shop -> new mission"
    )

    val MANUAL_QA: List<String> = listOf(
        "Touch controls feel good on small and large phones",
        "Aim assist Off/Low/High behave per 5.1",
        "Hitmarker only on real damage",
        "Friendly fire behaves per difficulty",
        "No forced ads in gameplay",
        "Save survives force-close during a purchase",
        "Rotation and interruptions (call, notification) pause properly",
        "Low-battery and low-memory devices"
    )

    val REGRESSIONS: List<RegressionRow> = listOf(
        RegressionRow(
            "Round freezes after killing 1 enemy",
            "Kill-all objective with enemies falling off map or dying simultaneously"
        ),
        RegressionRow("First tournament impossible", "Pass-rate playtest >= 80%, timer scaling test"),
        RegressionRow("Hard to focus on targets", "Aim assist tests, reticle visibility on all backgrounds"),
        RegressionRow("Frequent lag", "Profiler budget test on a low-end device"),
        RegressionRow("Ads during gameplay", "Automated check: no ad call in the Playing state")
    )

    fun unitArea(name: String): UnitTestArea? = UNIT_AREAS.firstOrNull { it.area == name }

    fun areaNames(): List<String> = UNIT_AREAS.map { it.area }

    /** Every area must name at least one concrete check, or the table is decoration. */
    fun allAreasNameTests(): Boolean = UNIT_AREAS.all { it.tests.isNotBlank() }

    fun regressionFor(bug: String): RegressionRow? = REGRESSIONS.firstOrNull { it.originalBug == bug }
}

/**
 * Section 12: a task is done only when all nine conditions hold. [checked] holds
 * the indices that were actually verified for the change under review.
 */
object DefinitionOfDone {

    val ITEMS: List<String> = listOf(
        "Existing working features were not replaced or broken",
        "Logic rules in Section 5 for this feature are implemented and tested",
        "Damage, XP and currency go through their single services only",
        "No hard-coded balance numbers (data-driven)",
        "Zero compile errors or warnings that matter; unit and PlayMode tests pass",
        "Main Menu -> Mission -> Result flow still works",
        "Performance still within budget (Section 8)",
        "BUILD_STATUS.md and CHANGELOG.md updated",
        "Debug-only code is excluded from release builds"
    )

    fun isDone(checked: Set<Int>): Boolean = checked.containsAll(ITEMS.indices)

    fun pending(checked: Set<Int>): List<String> = ITEMS.filterIndexed { index, _ -> index !in checked }

    fun indexOf(item: String): Int = ITEMS.indexOf(item)
}
