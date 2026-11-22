package com.example.game.valor.plan

import com.example.game.valor.balance.RankSpec
import com.example.game.valor.balance.RankTable

/** Section 5.16: the XP sources the table names. */
enum class XpReason(val displayName: String) {
    ENEMY_KILL("Enemy kill (by type)"),
    HEADSHOT_BONUS("Headshot bonus"),
    OBJECTIVE_COMPLETE("Objective complete"),
    MISSION_COMPLETE("Mission complete"),
    BONUS_OBJECTIVE("Bonus objective"),
    TRAINING_DRILL("Training drill pass"),
    FIRST_TIME_BONUS("First-time mission bonus")
}

data class XpAward(val amount: Int, val reason: XpReason, val detail: String)

/** Section 5.16: what a kill has to satisfy before it is worth XP. */
data class KillXpContext(
    val killerTeam: Team,
    val missionPlaying: Boolean,
    val debugKill: Boolean = false,
    val fromRespawn: Boolean = false,
    val friendly: Boolean = false
)

/** The outcome of one `AddXP`: the promotions it caused, in order. */
data class XpResult(
    val accepted: Boolean,
    val xpAfter: Int,
    val promotions: List<RankSpec>,
    val reason: String? = null
)

/**
 * Section 5.16: XP enters the game here and nowhere else. Rank is derived from
 * XP, and a single award can cross several rungs.
 */
class ProgressionService(initialXp: Int = 0) {

    var xp: Int = initialXp.coerceAtLeast(0)
        private set

    var prestigeCount: Int = 0
        private set

    private val rewardsGranted = mutableSetOf<Int>()
    private val history = mutableListOf<XpAward>()

    val rank: RankSpec get() = RankTable.rankAt(xp)

    fun awards(): List<XpAward> = history.toList()

    fun rewardsFor(rankIndex: Int): Boolean = rewardsGranted.contains(rankIndex)

    /**
     * "while (xp >= nextRankXP) rankUp()" - the loop is what makes a 10,000 XP
     * award cross several ranks at once, each with its reward granted once.
     */
    fun addXp(amount: Int, reason: XpReason, detail: String = "", log: DebugLog? = null): XpResult {
        if (amount < 0) {
            log?.record(LogCategory.XP, "Negative XP rejected: $amount")
            return XpResult(false, xp, emptyList(), "XP can never be negative")
        }
        val before = xp
        xp += amount
        history += XpAward(amount, reason, detail)
        val promotions = RankTable.RANKS.filter {
            val threshold = it.cumulativeXp ?: return@filter false
            threshold > before && threshold <= xp
        }
        promotions.forEach { rewardsGranted += it.index }
        return XpResult(true, xp, promotions)
    }

    /** Rank is recomputed on load; only XP is persisted. */
    fun rankForXp(totalXp: Int): RankSpec = RankTable.rankAt(totalXp)

    fun xpToNextRank(): Int? = RankTable.nextRank(xp)?.let { (it.cumulativeXp ?: 0) - xp }

    fun progressToNextRank(): Float = RankTable.progressToNext(xp)

    /** "No XP: friendly kills, kills after mission end, debug/god-mode, respawn farming." */
    fun killEarnsXp(context: KillXpContext): Boolean =
        context.killerTeam == Team.PLAYER &&
            context.missionPlaying &&
            !context.debugKill &&
            !context.fromRespawn &&
            !context.friendly

    /** Kill XP is 10-100 by enemy type; the row is data, the band is the rule. */
    fun killXpFor(baseXp: Int): Int = baseXp.coerceIn(ProgressionRules.KILL_XP_RANGE)

    fun headshotBonus(baseXp: Int): Int = baseXp + RankTable.HEADSHOT_BONUS_XP

    fun bonusObjectiveXp(amount: Int): Int = amount.coerceIn(ProgressionRules.BONUS_XP_RANGE)

    /** "Prestige resets XP/rank but keeps cosmetics/mastery." */
    fun prestige(cosmeticIds: Set<String>, mastery: Map<String, Int>): PrestigeResult {
        xp = 0
        prestigeCount += 1
        rewardsGranted.clear()
        return PrestigeResult(cosmeticIds, mastery, prestigeCount)
    }
}

/** Section 5.16: the printed bands and the one-place unlock check. */
object ProgressionRules {

    val KILL_XP_RANGE: IntRange = 10..100
    val BONUS_XP_RANGE: IntRange = 50..200

    /** "Rank-up screen shown at Results, not mid-combat." */
    const val RANK_UP_SHOWN_AT_RESULTS = true
    const val SKILL_POINT_PER_RANK = 1
}

/** Section 5.16: "checked in one place". */
object UnlockService {

    fun isUnlocked(playerRank: Int, unlockRank: Int): Boolean = playerRank >= unlockRank

    fun lockedBy(playerRank: Int, unlockRank: Int): Boolean = !isUnlocked(playerRank, unlockRank)

    /** Locked items show their required rank in the UI. */
    fun requiredRankLabel(unlockRank: Int): String = "Requires rank $unlockRank"
}

/** Section 5.16: New Game+ keeps cosmetics and mastery, resets the ladder. */
data class PrestigeResult(val keptCosmetics: Set<String>, val keptMastery: Map<String, Int>, val prestige: Int)
