package com.example.game.tactical.integration

import com.example.game.tactical.progression.RankLadder
import com.example.game.tactical.progression.RankRequirement
import com.example.game.tactical.state.PlayerProfile

/**
 * Phase 1 deliverable 3: the non-destructive integration layer.
 *
 * The legacy tour (`com.example.game.combat`) still owns its own 15-rung merit
 * ladder and its own save shape. Nothing here edits those files - this bridge
 * only translates their state into the Tactical Evolution contracts, so an
 * existing operator keeps their progress when the new campaign opens.
 */
object LegacyBridge {

    const val LEGACY_MAX_MERIT = 90_000
    const val LEGACY_MAX_RANK_INDEX = 14
    const val TACTICAL_MAX_XP = 35_000

    /** Legacy insignia to the eight-tier Tactical Evolution ladder. */
    val INSIGNIA_TO_TACTICAL_INDEX: Map<String, Int> = mapOf(
        "PV1" to 0, "PV2" to 1, "PFC" to 1,
        "SPC" to 2, "SGT" to 3, "SSG" to 3,
        "SFC" to 4, "WO1" to 4, "2LT" to 4, "1LT" to 4,
        "CPT" to 5, "MAJ" to 6, "LTC" to 6, "COL" to 7, "GEN" to 7
    )

    /** The highest legacy rung an operator at each tactical tier should keep. */
    val TACTICAL_TO_LEGACY_CLEARANCE: Map<Int, Int> = mapOf(
        0 to 0, 1 to 2, 2 to 3, 3 to 5, 4 to 9, 5 to 10, 6 to 12, 7 to LEGACY_MAX_RANK_INDEX
    )

    fun toTacticalRankIndex(legacyRankIndex: Int, legacyInsignia: String): Int =
        INSIGNIA_TO_TACTICAL_INDEX[legacyInsignia]
            ?: (legacyRankIndex.coerceIn(0, LEGACY_MAX_RANK_INDEX) / 2)

    fun toLegacyClearance(tacticalRankIndex: Int): Int =
        TACTICAL_TO_LEGACY_CLEARANCE[tacticalRankIndex.coerceIn(0, RankLadder.maxIndex)] ?: 0

    /** Merit Points and XP measure different economies; this keeps the ratio. */
    fun xpFromMerit(meritPoints: Int): Int =
        (meritPoints.coerceIn(0, LEGACY_MAX_MERIT).toLong() * TACTICAL_MAX_XP / LEGACY_MAX_MERIT).toInt()

    fun meritFromXp(xp: Int): Int =
        (xp.coerceAtLeast(0).toLong() * LEGACY_MAX_MERIT / TACTICAL_MAX_XP).toInt()

    /**
     * Builds a tactical profile from legacy state. Every gate below the migrated
     * tier is marked satisfied, because the legacy ladder already cleared it.
     */
    fun seed(
        callsign: String,
        legacyMeritPoints: Int,
        legacyRankIndex: Int,
        legacyInsignia: String,
        unlockedItems: List<String> = emptyList(),
        credits: Int = 0
    ): PlayerProfile {
        val rankIndex = toTacticalRankIndex(legacyRankIndex, legacyInsignia)
        val satisfied = RankLadder.TIERS
            .filter { it.index <= rankIndex }
            .map { it.requirement }
            .toSet()
            .ifEmpty { setOf(RankRequirement.ENLISTMENT) }
        return PlayerProfile(
            callsign = callsign,
            // A migrated operator must hold the XP floor of the tier they kept.
            xp = maxOf(xpFromMerit(legacyMeritPoints), RankLadder.tierAt(rankIndex).xpRequired),
            credits = credits.coerceAtLeast(0),
            rankIndex = rankIndex,
            inventory = unlockedItems,
            satisfied = satisfied
        )
    }
}
