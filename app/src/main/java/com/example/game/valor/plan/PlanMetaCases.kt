package com.example.game.valor.plan

import com.example.game.vanguard.CaseRecorder
import com.example.game.vanguard.ContractCase
import com.example.game.vanguard.observed

/** Sections 2, 3, 6 and 11: the status file, the phase order and the prompt library. */
object PlanMetaCases {

    fun run(): List<ContractCase> {
        val r = CaseRecorder(ValorPlanAudit.GROUP_META)
        statusCases(r)
        phaseCases(r)
        promptCases(r)
        return r.result()
    }

    private fun statusCases(r: CaseRecorder) {
        r.check(
            "Four statuses", "EXISTS-WORKING, EXISTS-PARTIAL, EXISTS-BROKEN and MISSING",
            SystemStatus.entries.map { it.displayName } ==
                listOf("EXISTS-WORKING", "EXISTS-PARTIAL", "EXISTS-BROKEN", "MISSING"),
            observed(SystemStatus.entries.size, 4)
        )
        r.check(
            "Status file name", "the inventory lives in BUILD_STATUS.md at the project root",
            BuildStatus.FILE_NAME == "BUILD_STATUS.md" &&
                ChangelogSpec.FILE_NAME == "CHANGELOG.md",
            observed(BuildStatus.FILE_NAME, "BUILD_STATUS.md")
        )
        r.check(
            "Every working row names its files", "a status without a file is a guess",
            BuildStatus.SYSTEMS.size == 15 && BuildStatus.allHaveFiles() &&
                BuildStatus.counts().values.sum() == BuildStatus.SYSTEMS.size,
            observed(BuildStatus.SYSTEMS.size, 15)
        )
        r.check(
            "Gaps are stated, not hidden", "VFX is missing and the save binding is partial",
            BuildStatus.status("VFX") == SystemStatus.MISSING &&
                BuildStatus.status("Save/Load") == SystemStatus.EXISTS_PARTIAL &&
                BuildStatus.status("Damage system") == SystemStatus.EXISTS_WORKING,
            observed(BuildStatus.status("VFX"), SystemStatus.MISSING)
        )
        r.check(
            "Protected assets listed", "six files may be extended but never overwritten",
            BuildStatus.PROTECTED.size == 6 &&
                BuildStatus.isProtected("balance/BattleMath.kt") &&
                BuildStatus.isProtected("MainActivity.kt") &&
                !BuildStatus.isProtected("plan/PlanCore.kt"),
            observed(BuildStatus.protectedPaths().size, 6)
        )
        r.check(
            "Open questions recorded", "each conflict is written down with its resolution",
            BuildStatus.OPEN_QUESTIONS.size == 5 &&
                BuildStatus.OPEN_QUESTIONS.all { it.isNotBlank() } &&
                BuildStatus.OPEN_QUESTIONS.any { it.contains("Helmet") },
            observed(BuildStatus.OPEN_QUESTIONS.size, 5)
        )
        r.check(
            "Next task recorded", "the status file always ends with what comes next",
            BuildStatus.NEXT_TASK.size == 2 && BuildStatus.NEXT_TASK.all { it.isNotBlank() },
            observed(BuildStatus.NEXT_TASK.size, 2)
        )
        r.check(
            "Changelog headings", "Added, Modified, Removed and Tests, in that order",
            ChangelogSpec.sections() == listOf("Added", "Modified", "Removed", "Tests") &&
                ChangelogSpec.hasSection("added") &&
                !ChangelogSpec.hasSection("Deleted"),
            observed(ChangelogSpec.sections(), "4 headings")
        )
    }

    private fun phaseCases(r: CaseRecorder) {
        r.check(
            "Fifteen phases", "phase 0 through phase 14, in build order",
            PlanPhases.count() == 15 && PlanPhases.numbered() == (0..14).toList(),
            observed(PlanPhases.numbered(), "0..14")
        )
        r.check(
            "Every phase has a gate", "no phase is done without its acceptance bullets",
            PlanPhases.allHaveAcceptance() && PlanPhases.PHASES.all { it.tasks.isNotEmpty() },
            observed(PlanPhases.PHASES.count { it.acceptance.isEmpty() }, 0)
        )
        r.check(
            "Milestones M1 to M12", "fourteen milestones across fifteen phases",
            PlanPhases.PHASES.count { it.milestone != null } == 14 &&
                PlanPhases.phase(0)?.milestone == null &&
                PlanPhases.phase(1)?.milestone == "M1" &&
                PlanPhases.phase(14)?.milestone == "M12",
            observed(PlanPhases.PHASES.count { it.milestone != null }, 14)
        )
        r.check(
            "Phase 2 owns the combat core", "5.2 through 5.6 land in the weapons phase",
            PlanPhases.phase(2)?.logicSections == listOf("5.2", "5.3", "5.4", "5.5", "5.6"),
            observed(PlanPhases.phase(2)?.logicSections, "5.2-5.6")
        )
        r.check(
            "Phase lookup by section", "5.19 is delivered by phase 6 and revisited in phase 11",
            PlanPhases.phasesFor("5.19").map { it.number } == listOf(6, 11) &&
                PlanPhases.phasesFor("5.4").map { it.number } == listOf(2),
            observed(PlanPhases.phasesFor("5.19").map { it.number }, listOf(6, 11))
        )
    }

    private fun promptCases(r: CaseRecorder) {
        r.check(
            "Universal prefix", "every task starts by forbidding a rewrite",
            BuildPrompts.UNIVERSAL_PREFIX.contains("Do not replace or rewrite") &&
                BuildPrompts.UNIVERSAL_PREFIX.contains("Scan first"),
            observed(BuildPrompts.UNIVERSAL_PREFIX, "scan then add")
        )
        r.check(
            "Seven task templates", "each build task has its own prompt",
            BuildPrompts.TEMPLATES.size == 7 &&
                BuildPrompts.names().contains("Bug-fix") &&
                BuildPrompts.template("Review")?.aim?.contains("Sections 1, 2 and 5") == true,
            observed(BuildPrompts.TEMPLATES.size, 7)
        )
    }
}
