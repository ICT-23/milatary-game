package com.example.game.valor.balance

/**
 * Section 5: one rung of the revised (v2) XP curve.
 *
 * [cumulativeXp] is null only for the Medal of Honor holder, whose gate is the
 * completed story rather than a number; [act] is null for that same rung.
 */
data class RankSpec(
    val index: Int,
    val name: String,
    val cumulativeXp: Int?,
    val skillPoints: Int,
    val unlocks: List<String>,
    val act: Int?,
    val storyGated: Boolean = false
)

object RankTable {

    val RANKS: List<RankSpec> = listOf(
        RankSpec(1, "Recruit", 0, 0,
            listOf("Pistol Mk1", "Pioneer Bolt Rifle", "Trench Knife"), act = 1),
        RankSpec(2, "Private", 300, 1, listOf("Frag grenade", "medkit"), act = 1),
        RankSpec(3, "Private First Class", 900, 1,
            listOf("Red-dot", "helmet T1", "vest T1", "Revolver"), act = 1),
        RankSpec(4, "Corporal", 2_000, 1,
            listOf("Trench SMG", "Pioneer Mk2", "gas grenade/mask"), act = 2),
        RankSpec(5, "Sergeant", 3_800, 1,
            listOf("Shotgun", "smoke grenade", "1 AI squadmate"), act = 2),
        RankSpec(6, "Staff Sergeant", 6_500, 1,
            listOf("Semi-auto rifle", "4x scope", "compensator", "foregrip"), act = 2),
        RankSpec(7, "Sergeant First Class", 10_000, 1,
            listOf("LMG", "SMG Hornet", "tracer ammo"), act = 3),
        RankSpec(8, "Master Sergeant", 14_500, 1,
            listOf("Sniper Longwatch", "suppressor", "camo set 1"), act = 3),
        RankSpec(9, "Sergeant Major", 20_000, 2,
            listOf("2 AI squadmates", "T2 gear", "flash grenade"), act = 3),
        RankSpec(10, "Second Lieutenant", 26_500, 1,
            listOf("Mission planning table", "8x scope", "mine"), act = 3),
        RankSpec(11, "First Lieutenant", 34_000, 1,
            listOf("Artillery call-in", "AR Vanguard"), act = 4),
        RankSpec(12, "Captain", 43_000, 1,
            listOf("Vehicle missions", "launcher", "carbine"), act = 4),
        RankSpec(13, "Major", 53_000, 2,
            listOf("Night vision", "machine pistol"), act = 4),
        RankSpec(14, "Lieutenant Colonel", 64_500, 1,
            listOf("3 AI squadmates", "T3 gear", "AR Sentinel"), act = 5),
        RankSpec(15, "Colonel", 77_000, 2,
            listOf("Drone strike (ARIA)", "thermal", "DMR"), act = 5),
        RankSpec(16, "Brigadier General", 91_000, 1,
            listOf("Drone swarm", "Bulwark LMG"), act = 6),
        RankSpec(17, "Major General", 106_000, 1,
            listOf("Titan anti-material", "legendary skins"), act = 6),
        RankSpec(18, "Lieutenant General", 122_000, 2,
            listOf("Battle command mini-missions"), act = 6),
        RankSpec(19, "General (Commander)", 140_000, 2,
            listOf("Final campaign missions"), act = 6),
        RankSpec(20, "Medal of Honor Holder", null, 0,
            listOf("New Game+", "Prestige"), act = null, storyGated = true)
    )

    /** Section 5 "XP sources (Normal)". */
    const val HEADSHOT_BONUS_XP = 5
    const val FIRST_CLEAR_BONUS = 0.25f

    /** "objective 50-500" XP. */
    val OBJECTIVE_XP_RANGE: IntRange = 50..500

    val STAR_MULTIPLIERS: List<Float> = listOf(1.0f, 1.2f, 1.5f)

    fun byIndex(index: Int): RankSpec? = RANKS.firstOrNull { it.index == index }

    fun forAct(act: Int): List<RankSpec> = RANKS.filter { it.act == act }

    /** The rung held at a cumulative XP total; story-gated rungs need the ending. */
    fun rankAt(xp: Int): RankSpec =
        RANKS.lastOrNull { it.cumulativeXp != null && it.cumulativeXp <= xp } ?: RANKS.first()

    fun nextRank(xp: Int): RankSpec? =
        RANKS.firstOrNull { it.cumulativeXp != null && it.cumulativeXp > xp }

    fun xpIntoRank(xp: Int): Int {
        val held = rankAt(xp).cumulativeXp ?: 0
        return (xp - held).coerceAtLeast(0)
    }

    fun progressToNext(xp: Int): Float {
        val held = rankAt(xp).cumulativeXp ?: return 1f
        val next = nextRank(xp)?.cumulativeXp ?: return 1f
        val span = next - held
        return if (span <= 0) 1f else (xp - held).toFloat() / span
    }

    fun skillPointsEarned(xp: Int): Int =
        RANKS.filter { it.cumulativeXp != null && it.cumulativeXp <= xp }.sumOf { it.skillPoints }

    /**
     * Section 5 headers quote an XP figure per act; that figure is exactly the
     * cumulative span of the act's rank band, which is what this returns.
     */
    fun actXpSpan(act: Int): Int {
        val band = forAct(act)
        if (band.isEmpty()) return 0
        val last = band.last().cumulativeXp ?: return 0
        val firstIndex = band.first().index
        val before = byIndex(firstIndex - 1)?.cumulativeXp ?: 0
        return last - before
    }

    /** "1 star x1.0, 2 stars x1.2, 3 stars x1.5". */
    fun starMultiplier(stars: Int): Float = STAR_MULTIPLIERS[stars.coerceIn(1, 3) - 1]

    /** Mission completion XP: base x stars, plus 25% on a first clear. */
    fun missionXp(baseXp: Int, stars: Int, firstClear: Boolean): Int {
        val base = baseXp * starMultiplier(stars)
        return (if (firstClear) base * (1f + FIRST_CLEAR_BONUS) else base).toInt()
    }
}
