package com.example.game.vanguard

/**
 * A deterministic contract check. Every case runs pure domain code in-process,
 * so the result is identical on every launch and on every device.
 */
data class ContractCase(
    val group: String,
    val index: Int,
    val title: String,
    val expectation: String,
    val passed: Boolean,
    val detail: String
)

data class ContractAudit(val cases: List<ContractCase>) {
    val total: Int get() = cases.size
    val passed: Int get() = cases.count { it.passed }
    val allPassed: Boolean get() = total > 0 && passed == total
    val failures: List<ContractCase> get() = cases.filter { !it.passed }

    fun byGroup(): Map<String, List<ContractCase>> = cases.groupBy { it.group }
}

/**
 * Collects the cases of one group. The file only states expectation and the
 * value it observed; numbering and grouping are handled here.
 */
internal class CaseRecorder(private val group: String) {

    private val collected = mutableListOf<ContractCase>()

    fun check(title: String, expectation: String, passed: Boolean, detail: String) {
        collected += ContractCase(group, collected.size + 1, title, expectation, passed, detail)
    }

    fun result(): List<ContractCase> = collected.toList()
}

/** Uniform detail formatting so every failure line explains itself. */
internal fun observed(actual: Any?, expected: Any?): String = "observed=$actual expected=$expected"

/**
 * The gate described in Section 1.2 steps 3 and 6: tests are defined first, and
 * the same assertions run inside the shipping app so a regression cannot hide
 * behind "it looked fine on the device".
 */
object VanguardContractAudit {

    const val GROUP_CORE = "CORE"
    const val GROUP_COMBAT = "COMBAT"
    const val GROUP_GUNSMITH = "GUNSMITH"
    const val GROUP_CAREER = "CAREER"
    const val GROUP_NARRATIVE = "NARRATIVE"
    const val GROUP_META = "META"
    const val GROUP_SYSTEMS = "SYSTEMS"

    /** Display order used by the audit screen, so groups never reshuffle. */
    val GROUP_ORDER: List<String> = listOf(
        GROUP_CORE,
        GROUP_COMBAT,
        GROUP_GUNSMITH,
        GROUP_CAREER,
        GROUP_NARRATIVE,
        GROUP_META,
        GROUP_SYSTEMS
    )

    fun audit(): ContractAudit = ContractAudit(run())

    fun run(): List<ContractCase> =
        CoreCases.run() +
            CombatContractCases.run() +
            GunsmithContractCases.run() +
            CareerContractCases.run() +
            NarrativeContractCases.run() +
            MetaContractCases.run() +
            SystemsCoreContractCases.run() +
            SystemsGamespaceContractCases.run() +
            SystemsMetaUiContractCases.run()
}

/** Shared float comparison so a contract case never fails on representation noise. */
internal fun approxEquals(actual: Float, expected: Float, epsilon: Float = 0.01f): Boolean =
    kotlin.math.abs(actual - expected) <= epsilon
