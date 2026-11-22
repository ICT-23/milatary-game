package com.example.game.progression

/**
 * Section 17.1 economy rules. Two currencies only: Service Credits earned from
 * missions, and Commendation Tokens earned from awards.
 *
 * Section 17.2: a completion pays its defined reward once, and a replay pays a
 * limited repeat reward instead of creating an infinite currency exploit.
 */
object RewardRules {

    /** Full first-clear payout by grade. */
    fun firstClearCredits(grade: MissionGrade): Int = when (grade) {
        MissionGrade.S -> 120
        MissionGrade.A -> 90
        MissionGrade.B -> 70
        MissionGrade.C -> 50
        MissionGrade.D -> 30
    }

    /** Replays pay a quarter, and never less than a token amount. */
    fun credits(grade: MissionGrade, firstClear: Boolean): Int {
        val base = firstClearCredits(grade)
        return if (firstClear) base else (base / 4).coerceAtLeast(5)
    }

    /** One token per commendation actually earned this clear. */
    fun commendationTokens(awards: Int): Int = awards.coerceAtLeast(0)
}
