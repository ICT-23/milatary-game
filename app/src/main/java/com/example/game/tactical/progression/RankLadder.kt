package com.example.game.tactical.progression

/**
 * Section 3 of the Tactical Evolution plan: the eight-tier XP ladder.
 *
 * A tier needs both an XP threshold and a named requirement, so grinding XP
 * alone cannot hand out a rank whose qualification board has not been passed.
 */
enum class RankRequirement(val label: String, val detail: String) {
    ENLISTMENT("Enlistment", "Processed at Fort Valor intake"),
    BASIC_RIFLE_QUAL("Pass Basic Rifle Qualification", "38 of 50 on the pop-up range"),
    FIRST_COMBAT_MISSION("Complete 1st Combat Mission", "Survive the first frontline deployment"),
    SQUAD_LEADER_TRIAL("Pass Squad Leader Trial", "Command a fireteam through the killhouse"),
    OFFICER_ACADEMY_EXAM("Pass Officer Academy Exam", "Tactics and law-of-war boards"),
    ACT_II_COMPLETE("Complete Act II", "Clear the Eastern European deployment"),
    ZERO_CASUALTY_OP("Zero-Casualty Operation", "Finish an operation with the squad intact"),
    CAMPAIGN_VICTORY("Achieve Campaign Victory", "Break the Chimera citadel")
}

data class RankTier(
    val index: Int,
    val code: String,
    val title: String,
    val xpRequired: Int,
    val requirement: RankRequirement,
    val privileges: List<String>
) {
    val display: String get() = "$code - $title"
}

/** Why a promotion is not available yet; empty means the player is cleared. */
data class RankEligibility(
    val rankIndex: Int,
    val tier: RankTier,
    val xpMissing: Int,
    val missingRequirement: RankRequirement?
) {
    val isReady: Boolean get() = xpMissing == 0 && missingRequirement == null
    val next: RankTier? get() = RankLadder.tierAtOrNull(rankIndex + 1)
}

object RankLadder {

    val TIERS: List<RankTier> = listOf(
        RankTier(
            index = 0, code = "RCT", title = "Recruit", xpRequired = 0,
            requirement = RankRequirement.ENLISTMENT,
            privileges = listOf("Basic obstacle course", "Workbench level 1", "Iron-sight pistol")
        ),
        RankTier(
            index = 1, code = "PVT", title = "Private", xpRequired = 500,
            requirement = RankRequirement.BASIC_RIFLE_QUAL,
            privileges = listOf("Assault rifle frame", "Live-fire killhouse", "Basic cover mechanics")
        ),
        RankTier(
            index = 2, code = "CPL", title = "Corporal", xpRequired = 1_500,
            requirement = RankRequirement.FIRST_COMBAT_MISSION,
            privileges = listOf("Gunsmith optics and barrels", "1 AI wingman command")
        ),
        RankTier(
            index = 3, code = "SGT", title = "Sergeant", xpRequired = 3_500,
            requirement = RankRequirement.SQUAD_LEADER_TRIAL,
            privileges = listOf("Full 4-man squad commands", "Sniper frame")
        ),
        RankTier(
            index = 4, code = "LT", title = "Lieutenant", xpRequired = 7_000,
            requirement = RankRequirement.OFFICER_ACADEMY_EXAM,
            privileges = listOf("Tactical recon drone", "Mortar strike call-in", "Advanced ballistics")
        ),
        RankTier(
            index = 5, code = "CPT", title = "Captain", xpRequired = 12_000,
            requirement = RankRequirement.ACT_II_COMPLETE,
            privileges = listOf("Heavy weapons", "Thermal / NVG scopes", "Custom ammunition crafting")
        ),
        RankTier(
            index = 6, code = "MAJ", title = "Major", xpRequired = 20_000,
            requirement = RankRequirement.ZERO_CASUALTY_OP,
            privileges = listOf("AC-130 support", "Guided missile drone", "Elite armour plates")
        ),
        RankTier(
            index = 7, code = "GEN", title = "Commander / General", xpRequired = 35_000,
            requirement = RankRequirement.CAMPAIGN_VICTORY,
            privileges = listOf("Medal of Honor ceremony", "Sandbox war-room mode", "All blueprints")
        )
    )

    /** The Lieutenant rung gates the drone strike system (Section 5.3). */
    const val DRONE_UNLOCK_INDEX = 4

    val maxIndex: Int get() = TIERS.lastIndex

    fun tierAtOrNull(index: Int): RankTier? = TIERS.getOrNull(index)

    fun tierAt(index: Int): RankTier = TIERS[index.coerceIn(0, maxIndex)]

    fun indexForXp(xp: Int): Int = TIERS.indexOfLast { xp >= it.xpRequired }.coerceAtLeast(0)

    /**
     * The tier the player currently holds. Requirements are checked from the top
     * down so a single failed board blocks every rung above it.
     */
    fun heldIndex(xp: Int, satisfied: Set<RankRequirement>): Int {
        var index = 0
        for (tier in TIERS) {
            if (xp >= tier.xpRequired && tier.requirement in satisfied) index = tier.index
        }
        return index
    }

    fun eligibility(xp: Int, satisfied: Set<RankRequirement>): RankEligibility {
        val held = heldIndex(xp, satisfied)
        val next = TIERS.getOrNull(held + 1) ?: TIERS[held]
        if (held == maxIndex) {
            return RankEligibility(held, TIERS[held], 0, null)
        }
        return RankEligibility(
            rankIndex = held,
            tier = next,
            xpMissing = (next.xpRequired - xp).coerceAtLeast(0),
            missingRequirement = next.requirement.takeIf { it !in satisfied }
        )
    }

    fun hasClearance(rankIndex: Int, requiredIndex: Int): Boolean = rankIndex >= requiredIndex

    /** Everything unlocked by reaching [toIndex] from [fromIndex], in ladder order. */
    fun privilegesBetween(fromIndex: Int, toIndex: Int): List<String> {
        if (toIndex <= fromIndex) return emptyList()
        return TIERS.subList(fromIndex + 1, (toIndex + 1).coerceAtMost(TIERS.size)).flatMap { it.privileges }
    }

    /** 0..1 progress toward the next rung, used by the HUD rank bar. */
    fun progressToNext(xp: Int, rankIndex: Int): Float {
        val current = tierAt(rankIndex)
        val next = tierAtOrNull(rankIndex + 1) ?: return 1f
        val span = (next.xpRequired - current.xpRequired).coerceAtLeast(1)
        return ((xp - current.xpRequired).toFloat() / span).coerceIn(0f, 1f)
    }
}
