package com.example.game.valor.gdd

/**
 * Section 4: one rung of the 20-rank ladder.
 *
 * [cumulativeXp] is null only for the Medal of Honor holder, whose requirement is
 * completing the story rather than a number. [unlocks] keeps the doc's cell.
 */
data class GddRank(
    val index: Int,
    val title: String,
    val cumulativeXp: Int?,
    val unlocks: List<String>
) {
    val storyGated: Boolean get() = cumulativeXp == null
}

object RankCurve {

    const val MAX_RANK = 20
    const val COMMANDER_RANK = 19
    const val HONOR_RANK = 20
    const val FINAL_XP = 900_000

    /** Section 4 rank table, transcribed row for row. */
    val RANKS: List<GddRank> = listOf(
        GddRank(1, "Recruit", 0, listOf("Basic rifle", "Boot-camp training")),
        GddRank(2, "Private", 500, listOf("Sidearm slot", "Grenades")),
        GddRank(3, "Private First Class", 1_500, listOf("First gun attachment slot (optic)")),
        GddRank(4, "Corporal", 3_500, listOf("Armor vest upgrade", "SMG")),
        GddRank(5, "Sergeant", 7_000, listOf("Squad command (1 AI teammate)")),
        GddRank(6, "Staff Sergeant", 12_000, listOf("Second attachment slot (grip/barrel)")),
        GddRank(7, "Sergeant First Class", 20_000, listOf("Machine gun", "Medic kit")),
        GddRank(8, "Master Sergeant", 32_000, listOf("Sniper rifle", "Camo skins")),
        GddRank(9, "Sergeant Major", 48_000, listOf("Squad of 2 AI", "Radio support (smoke)")),
        GddRank(10, "Second Lieutenant", 70_000, listOf("Officer uniform", "Mission planning table")),
        GddRank(11, "First Lieutenant", 100_000, listOf("Artillery strike call-in")),
        GddRank(12, "Captain", 140_000, listOf("Vehicle driving missions")),
        GddRank(13, "Major", 190_000, listOf("Night vision / thermal gear")),
        GddRank(14, "Lieutenant Colonel", 250_000, listOf("Squad of 3 AI", "Advanced attachments")),
        GddRank(15, "Colonel", 330_000, listOf("Drone strike unlocked (ARIA)")),
        GddRank(16, "Brigadier General", 430_000, listOf("Multi-drone swarm")),
        GddRank(17, "Major General", 550_000, listOf("Elite gear", "Legendary skins")),
        GddRank(18, "Lieutenant General", 700_000, listOf("Command whole battle (RTS-lite mini-missions)")),
        GddRank(19, "General (Commander)", 900_000, listOf("Final campaign missions")),
        GddRank(20, "Medal of Honor Holder", null, listOf("New Game+", "Prestige mode"))
    )

    fun byIndex(index: Int): GddRank? = RANKS.firstOrNull { it.index == index }

    fun byTitle(title: String): GddRank? =
        RANKS.firstOrNull { it.title.equals(title.trim(), ignoreCase = true) }

    /** The rung held at a cumulative XP total; the story rung needs the ending. */
    fun rankAt(xp: Int): GddRank =
        RANKS.lastOrNull { it.cumulativeXp != null && it.cumulativeXp <= xp } ?: RANKS.first()

    fun nextRank(xp: Int): GddRank? =
        RANKS.firstOrNull { it.cumulativeXp != null && it.cumulativeXp > xp }

    fun xpToNext(xp: Int): Int? = nextRank(xp)?.let { (it.cumulativeXp ?: 0) - xp }

    fun progressToNext(xp: Int): Float {
        val held = rankAt(xp).cumulativeXp ?: return 1f
        val next = nextRank(xp)?.cumulativeXp ?: return 1f
        val span = next - held
        return if (span <= 0) 1f else (xp - held).toFloat() / span
    }

    /** Everything unlocked at or below a rung, in ladder order. */
    fun unlocksThrough(index: Int): List<String> =
        RANKS.filter { it.index <= index }.flatMap { it.unlocks }

    fun unlockRankOf(unlock: String): Int? =
        RANKS.firstOrNull { rank -> rank.unlocks.any { it.equals(unlock, true) } }?.index

    fun isUnlocked(index: Int, unlock: String): Boolean =
        unlockRankOf(unlock)?.let { index >= it } ?: false

    /** The ladder must climb strictly, or the rank-up screen cannot animate it. */
    fun isStrictlyIncreasing(): Boolean =
        RANKS.mapNotNull { it.cumulativeXp }.zipWithNext().all { (a, b) -> b > a }

    fun isContiguous(): Boolean = RANKS.map { it.index } == (1..MAX_RANK).toList()

    /** Rank-ups between two totals, used for the promotion animation. */
    fun promotionsBetween(fromXp: Int, toXp: Int): List<GddRank> {
        val from = rankAt(fromXp).index
        return RANKS.filter {
            it.cumulativeXp != null && it.cumulativeXp > fromXp &&
                it.cumulativeXp <= toXp && it.index > from
        }
    }
}

/**
 * Section 4 "Prestige": once the story is finished the player may re-enlist.
 * The three effects are stated, so they are kept as rules rather than invented
 * multipliers.
 */
object Prestige {

    const val REQUIRED_RANK = RankCurve.HONOR_RANK

    val RULES: List<String> = listOf(
        "Re-enlist (New Game+) with a Prestige star",
        "Keeps cosmetic unlocks",
        "Enemies are tougher"
    )

    fun availableAt(index: Int): Boolean = index >= REQUIRED_RANK

    fun starCount(newGamePlusRuns: Int): Int = newGamePlusRuns.coerceAtLeast(0)

    /** Cosmetics survive a re-enlist; progression does not. */
    fun keptAfterReenlist(cosmeticIds: Set<String>): Set<String> = cosmeticIds
}
