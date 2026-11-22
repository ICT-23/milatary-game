package com.example.game.vanguard.progression

import com.example.game.vanguard.core.EventBus
import com.example.game.vanguard.core.GameErrorCategory
import com.example.game.vanguard.core.GameResult
import com.example.game.vanguard.core.RankChanged

/**
 * Section 3.3 item 1: the strict monotonic enumeration. The index is the only
 * ordering the game trusts, so a label can never be used to compare two ranks.
 */
enum class VanguardRank(
    val index: Int,
    val code: String,
    val title: String,
    val tier: Int
) {
    E1_PVT(0, "E1_PVT", "Recruit / Private", 1),
    E4_CPL(1, "E4_CPL", "Corporal", 1),
    E6_SSG(2, "E6_SSG", "Staff Sergeant", 2),
    O1_LT(3, "O1_LT", "Lieutenant", 3),
    O3_CPT(4, "O3_CPT", "Captain", 3),
    O5_LTC(5, "O5_LTC", "Lieutenant Colonel", 4),
    O7_BG(6, "O7_BG", "Brigadier General", 4),
    O10_GEN(7, "O10_GEN", "General / Army Commander", 4),
    CIV_PRES(8, "CIV_PRES", "Commander-in-Chief / President", 5);

    val display: String get() = "$code - $title"

    /** Next rung, or null at the constitutional ceiling. */
    val next: VanguardRank? get() = entries.getOrNull(index + 1)

    companion object {
        fun fromIndex(index: Int): VanguardRank? = entries.firstOrNull { it.index == index }

        fun fromCode(code: String?): VanguardRank? =
            entries.firstOrNull { it.code.equals(code?.trim(), ignoreCase = true) }

        /** The legacy failure: a chapter loader resetting the save to E1_PVT. */
        val DEFAULT_FALLBACK: VanguardRank = E1_PVT
    }
}

/** One append-only promotion record. Nothing in this file ever edits history. */
data class RankEntry(
    val rank: VanguardRank,
    val recordedAtEpochMs: Long,
    val source: String
)

/**
 * Event-sourced ledger. [record] returns a new ledger, so a caller that forgets
 * to save simply loses its promotion instead of corrupting the stored one.
 */
class RankLedger private constructor(val history: List<RankEntry>) {

    val current: VanguardRank get() = history.lastOrNull()?.rank ?: VanguardRank.E1_PVT

    fun record(entry: RankEntry): RankLedger = RankLedger(history + entry)

    /** True when this rank was ever held, which a regression bug would erase. */
    fun everHeld(rank: VanguardRank): Boolean = history.any { it.rank == rank }

    companion object {
        fun initial(rank: VanguardRank = VanguardRank.E1_PVT, atEpochMs: Long = 0L): RankLedger =
            RankLedger(listOf(RankEntry(rank, atEpochMs, "ENLISTMENT")))

        fun of(vararg entries: RankEntry): RankLedger = RankLedger(entries.toList())
    }
}

/**
 * Section 3.3 item 2. Promotion is the only way the rank moves forward, and it
 * publishes [RankChanged] so the uniform binder and the HUD react without being
 * told what to do.
 */
class RankProgressionService(private val bus: EventBus) {

    fun promoteTo(ledger: RankLedger, target: VanguardRank, atEpochMs: Long): GameResult<RankLedger> {
        if (target.index <= ledger.current.index) {
            return GameResult.fail(
                GameErrorCategory.PROGRESSION,
                "RANK_REGRESSION_BLOCKED",
                "Cannot move from ${ledger.current.code} to ${target.code}"
            )
        }
        val previous = ledger.current
        val updated = ledger.record(RankEntry(target, atEpochMs, "PROMOTION"))
        bus.publish(
            RankChanged(
                oldRankIndex = previous.index,
                newRankIndex = target.index,
                rankCode = target.code,
                rankTitle = target.title
            )
        )
        return GameResult.Ok(updated)
    }

    /**
     * Chapter loaders call this instead of assigning a rank. Whatever the scene
     * believes, the ledger wins, which is exactly the Chapter 8 regression fix.
     */
    fun resolveLoadedRank(ledger: RankLedger, chapterDefault: VanguardRank?): VanguardRank {
        val saved = ledger.current
        if (chapterDefault == null) return saved
        return if (chapterDefault.index > saved.index) chapterDefault else saved
    }

    /** HUD badges bind here, never to a scene-local variable. */
    fun hudRank(ledger: RankLedger): String = ledger.current.display
}
