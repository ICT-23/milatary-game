package com.example.game.combat

/** One rung of the merit ladder. Clearance gates the armory and drill facilities. */
data class RankTier(
    val index: Int,
    val code: String,
    val insignia: String,
    val title: String,
    val meritRequired: Int,
    val unlocks: List<String>
) {
    val display: String get() = "$insignia · $title"
}

/**
 * The single merit ladder from the master spec. Fourteen promotions take the
 * operator from unranked recruit to General of the Army at 90,000 MP.
 */
object RankLadder {

    val TIERS: List<RankTier> = listOf(
        RankTier(
            index = 0, code = "E-1", insignia = "PV1", title = "Recruit", meritRequired = 0,
            unlocks = listOf("Standard PT gear", "Iron-sight V-9 service pistol", "Track course")
        ),
        RankTier(
            index = 1, code = "E-2", insignia = "PV2", title = "Private Second Class", meritRequired = 600,
            unlocks = listOf("AR-4 Sentinel standard issue", "Obstacle course tier 1")
        ),
        RankTier(
            index = 2, code = "E-3", insignia = "PFC", title = "Private First Class", meritRequired = 1_400,
            unlocks = listOf("Red dot reflex optics", "Flash suppressors", "Sandbag barricades")
        ),
        RankTier(
            index = 3, code = "E-4", insignia = "SPC", title = "Specialist / Corporal", meritRequired = 2_500,
            unlocks = listOf("Armory workbench", "Tactical foregrips", "100 m range")
        ),
        RankTier(
            index = 4, code = "E-5", insignia = "SGT", title = "Sergeant", meritRequired = 4_200,
            unlocks = listOf("LR-12 Horizon precision rifle", "Match grade ammunition", "300 m range")
        ),
        RankTier(
            index = 5, code = "E-6", insignia = "SSG", title = "Staff Sergeant", meritRequired = 6_500,
            unlocks = listOf("CQB killhouse tier 1", "B-12 Breacher shotgun")
        ),
        RankTier(
            index = 6, code = "E-7", insignia = "SFC", title = "Sergeant First Class", meritRequired = 9_800,
            unlocks = listOf("Flashbangs", "Tactical suppressors", "NVG Gen 3")
        ),
        RankTier(
            index = 7, code = "W-1", insignia = "WO1", title = "Warrant Officer 1", meritRequired = 14_000,
            unlocks = listOf("Custom gunsmithing", "Gas blocks, triggers and calibers")
        ),
        RankTier(
            index = 8, code = "O-1", insignia = "2LT", title = "Second Lieutenant", meritRequired = 19_500,
            unlocks = listOf("Tactical tablet", "UAV reconnaissance drone controls")
        ),
        RankTier(
            index = 9, code = "O-2", insignia = "1LT", title = "First Lieutenant", meritRequired = 26_000,
            unlocks = listOf("Laser designator", "81 mm mortar fire support")
        ),
        RankTier(
            index = 10, code = "O-3", insignia = "CPT", title = "Captain", meritRequired = 34_000,
            unlocks = listOf("Fireteam command", "Custom camo finishes", "Killhouse tier 3")
        ),
        RankTier(
            index = 11, code = "O-4", insignia = "MAJ", title = "Major", meritRequired = 44_000,
            unlocks = listOf("Heavy weapons", "H-6 Anchor support weapon", "Anti-materiel precision system")
        ),
        RankTier(
            index = 12, code = "O-5", insignia = "LTC", title = "Lieutenant Colonel", meritRequired = 56_000,
            unlocks = listOf("Close air support call-in simulator", "Night killhouse")
        ),
        RankTier(
            index = 13, code = "O-6", insignia = "COL", title = "Colonel", meritRequired = 70_000,
            unlocks = listOf("Black ops armory clearance", "Classified spec-ops campaign")
        ),
        RankTier(
            index = 14, code = "O-10", insignia = "GEN", title = "General of the Army", meritRequired = 90_000,
            unlocks = listOf("Commandant uniform", "Medal of Honor", "Supreme war room access")
        )
    )

    val maxIndex: Int get() = TIERS.lastIndex

    fun tierAt(index: Int): RankTier = TIERS[index.coerceIn(0, maxIndex)]

    fun indexFor(meritPoints: Int): Int =
        TIERS.indexOfLast { meritPoints >= it.meritRequired }.coerceAtLeast(0)

    fun nextTier(index: Int): RankTier? = TIERS.getOrNull(index.coerceIn(0, maxIndex) + 1)

    /** 0..1 progress from the current rung toward the next one. */
    fun progress(index: Int, meritPoints: Int): Float {
        val current = tierAt(index)
        val next = nextTier(index) ?: return 1f
        val span = (next.meritRequired - current.meritRequired).coerceAtLeast(1)
        return ((meritPoints - current.meritRequired).toFloat() / span).coerceIn(0f, 1f)
    }

    fun promotionsBetween(fromIndex: Int, toIndex: Int): List<RankTier> {
        if (toIndex <= fromIndex) return emptyList()
        return TIERS.subList((fromIndex + 1).coerceAtLeast(0), (toIndex + 1).coerceAtMost(TIERS.size))
    }

    fun hasClearance(currentRankIndex: Int, requiredRankIndex: Int): Boolean =
        currentRankIndex >= requiredRankIndex

    /** Promotion ceremonies are only owed once per rung. */
    fun newlyUnlocked(fromIndex: Int, toIndex: Int): List<String> =
        promotionsBetween(fromIndex, toIndex).flatMap { it.unlocks }
}

/** Merit Point award formulas. Accuracy, speed, discipline, zero safety infractions. */
object MeritRules {

    fun missionMerit(score: Int, stars: Int, healthPercent: Int): Int {
        val base = 35 + stars.coerceIn(0, 3) * 25
        val scoreShare = (score / 80).coerceIn(0, 250)
        val healthShare = healthPercent.coerceIn(0, 100) / 5
        return (base + scoreShare + healthShare).coerceIn(0, 800)
    }

    /**
     * @param baseMerit maximum the drill can pay before modifiers
     * @param accuracyPercent correct engagements out of all engagements
     * @param timeRatio elapsed / limit (lower is faster)
     * @param infractions safety or discipline violations, 50 MP each
     */
    fun drillMerit(baseMerit: Int, accuracyPercent: Int, timeRatio: Float, infractions: Int): Int {
        val accuracy = accuracyPercent.coerceIn(0, 100) / 100f
        val speed = 1f - timeRatio.coerceIn(0f, 1f)
        val raw = baseMerit * (0.45f + 0.35f * accuracy + 0.20f * speed) - infractions.coerceAtLeast(0) * 50
        return raw.toInt().coerceIn(0, baseMerit * 2)
    }
}
