package com.example.game.vanguard.narrative

import com.example.game.vanguard.progression.VanguardRank

/**
 * Section 2 Narrative: StoryArcManager.
 *
 * The five acts follow the rank ladder rather than a chapter counter, so a save
 * that earned a rank early can enter the act it belongs to and a save that was
 * reset cannot skip ahead.
 */
enum class StoryAct(
    val number: Int,
    val title: String,
    val firstChapter: Int,
    val lastChapter: Int,
    val minRankIndex: Int
) {
    ACT_I(1, "Act I - Induction", 1, 2, 0),
    ACT_II(2, "Act II - Specialist", 3, 4, 2),
    ACT_III(3, "Act III - Command", 5, 6, 4),
    ACT_IV(4, "Act IV - Strategy", 7, 8, 6),
    ACT_V(5, "Act V - The Nation", 9, 9, 8);

    fun contains(chapterNumber: Int): Boolean = chapterNumber in firstChapter..lastChapter

    val requiredRank: VanguardRank get() = VanguardRank.fromIndex(minRankIndex) ?: VanguardRank.E1_PVT
}

data class ChapterGate(
    val chapterNumber: Int,
    val chapterId: String,
    val title: String,
    val requiresChapters: List<Int> = emptyList()
)

class StoryArcManager {

    fun actForChapter(chapterNumber: Int): StoryAct? =
        StoryAct.entries.firstOrNull { it.contains(chapterNumber) }

    /** Both conditions must hold: story prerequisites and the rank the act expects. */
    fun isUnlocked(gate: ChapterGate, completedChapters: Set<Int>, rank: VanguardRank): Boolean {
        if (!gate.requiresChapters.all { it in completedChapters }) return false
        val act = actForChapter(gate.chapterNumber) ?: return false
        return rank.index >= act.minRankIndex
    }

    fun nextUnlockedChapter(
        gates: List<ChapterGate>,
        completedChapters: Set<Int>,
        rank: VanguardRank
    ): ChapterGate? = gates
        .sortedBy { it.chapterNumber }
        .firstOrNull { it.chapterNumber !in completedChapters && isUnlocked(it, completedChapters, rank) }

    /** The blocker a UI should show, or null when the chapter is available. */
    fun blockerFor(gate: ChapterGate, completedChapters: Set<Int>, rank: VanguardRank): String? {
        val missing = gate.requiresChapters.filterNot { it in completedChapters }
        if (missing.isNotEmpty()) {
            return "Requires chapter ${missing.sorted().joinToString()}"
        }
        val act = actForChapter(gate.chapterNumber) ?: return "Chapter is outside the story arc"
        if (rank.index < act.minRankIndex) {
            return "Requires ${act.requiredRank.code} to enter ${act.title}"
        }
        return null
    }

    val chapterCount: Int get() = StoryAct.entries.sumOf { it.lastChapter - it.firstChapter + 1 }
}
