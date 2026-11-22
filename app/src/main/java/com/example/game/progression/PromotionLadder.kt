package com.example.game.progression

/**
 * Academy qualifications referenced by the promotion ladder. Each one is earned
 * by an activity that actually exists in the build, never by a button press.
 */
enum class Qualification(val displayName: String, val earnedBy: String) {
    SAFETY_ORIENTATION("Safety Orientation", "Complete the arrival and orientation sequence"),
    PHYSICAL_STANDARD("Physical Standard", "Obstacle course inside the par time"),
    MARKSMANSHIP("Basic Marksmanship", "Firing range qualification"),
    NAVIGATION("Navigation", "Field route and landmark exercise"),
    SQUAD_EVALUATION("Squad Evaluation", "Timed team exercise with the squad"),
    FIELD_DEPLOYMENT("Field Deployment", "First field mission with acceptable conduct"),
    SPECIALIST("Advanced Specialist", "Armory workbench or drone qualification"),
    LEADERSHIP_ASSESSMENT("Leadership Assessment", "Command exercise with squad orders"),
    MULTI_OBJECTIVE("Multi-Objective Command", "Lead a mission with several primary objectives"),
    URBAN_OPERATION("Urban Campaign", "Complete the urban operation");
}

/**
 * Evidence a promotion review demands. Section 6.1 is explicit: a rank is a
 * career milestone, so experience alone is never sufficient.
 */
data class PromotionRequirement(
    val serviceXp: Int,
    val completedMissions: Int = 0,
    val qualification: Qualification? = null,
    val minLeadership: Int = 0
)

/** One rung of the nineteen-rank ladder from section 6.2. */
data class RankRung(
    val index: Int,
    val title: String,
    val code: String,
    val evidence: PromotionRequirement,
    val evidenceText: String
) {
    val display: String get() = "$code · $title"
}

/**
 * Everything the review needs to know about the operative. Leadership is the
 * mentor and command score already tracked on the campaign profile.
 */
data class PromotionEvidence(
    val serviceXp: Int,
    val completedMissions: Int,
    val qualifications: Set<Qualification> = emptySet(),
    val leadershipScore: Int = 0
)

/**
 * Result of a promotion review. [outstanding] always explains what is missing so
 * the UI can show the player exactly what remains incomplete.
 */
data class PromotionReview(
    val current: RankRung,
    val next: RankRung?,
    val outstanding: List<String>,
    val progress: Float
) {
    val eligible: Boolean get() = next != null && outstanding.isEmpty()
    val isFinalRank: Boolean get() = next == null
}

/**
 * The transparent career ladder. A rung is only held when every piece of its
 * evidence is satisfied, so experience cannot fast-forward a promotion.
 */
object PromotionLadder {

    val RANKS: List<RankRung> = listOf(
        RankRung(1, "Cadet", "CDT", PromotionRequirement(serviceXp = 0), "Start of campaign"),
        RankRung(
            2, "Recruit", "RCT",
            PromotionRequirement(serviceXp = 200, completedMissions = 1, qualification = Qualification.SAFETY_ORIENTATION),
            "Complete arrival and safety orientation"
        ),
        RankRung(
            3, "Private", "PVT",
            PromotionRequirement(serviceXp = 500, completedMissions = 1, qualification = Qualification.PHYSICAL_STANDARD),
            "Pass basic physical and movement tests"
        ),
        RankRung(
            4, "Lance Corporal", "LCPL",
            PromotionRequirement(
                serviceXp = 900, completedMissions = 2,
                qualification = Qualification.MARKSMANSHIP
            ),
            "Pass basic marksmanship and navigation"
        ),
        RankRung(
            5, "Corporal", "CPL",
            PromotionRequirement(serviceXp = 1_400, completedMissions = 2, qualification = Qualification.SQUAD_EVALUATION),
            "Complete first squad evaluation"
        ),
        RankRung(
            6, "Sergeant", "SGT",
            PromotionRequirement(
                serviceXp = 2_000, completedMissions = 3,
                qualification = Qualification.FIELD_DEPLOYMENT, minLeadership = 55
            ),
            "Complete first deployment with acceptable conduct"
        ),
        RankRung(
            7, "Staff Sergeant", "SSG",
            PromotionRequirement(serviceXp = 2_700, completedMissions = 4, minLeadership = 60),
            "Demonstrate squad coordination in two operations"
        ),
        RankRung(
            8, "Warrant Officer", "WO1",
            PromotionRequirement(
                serviceXp = 3_500, completedMissions = 4,
                qualification = Qualification.SPECIALIST
            ),
            "Complete advanced specialist qualification"
        ),
        RankRung(
            9, "Second Lieutenant", "2LT",
            PromotionRequirement(
                serviceXp = 4_400, completedMissions = 5,
                qualification = Qualification.LEADERSHIP_ASSESSMENT, minLeadership = 65
            ),
            "Complete leadership assessment and command exercise"
        ),
        RankRung(
            10, "First Lieutenant", "1LT",
            PromotionRequirement(
                serviceXp = 5_400, completedMissions = 5,
                qualification = Qualification.MULTI_OBJECTIVE, minLeadership = 65
            ),
            "Successfully lead a multi-objective mission"
        ),
        RankRung(
            11, "Captain", "CPT",
            PromotionRequirement(
                serviceXp = 6_500, completedMissions = 6,
                qualification = Qualification.URBAN_OPERATION, minLeadership = 70
            ),
            "Complete the urban campaign and command review"
        ),
        RankRung(
            12, "Major", "MAJ",
            PromotionRequirement(serviceXp = 7_800, completedMissions = 7, minLeadership = 70),
            "Coordinate multiple squad roles and support assets"
        ),
        RankRung(
            13, "Lieutenant Colonel", "LTC",
            PromotionRequirement(serviceXp = 9_200, completedMissions = 8, minLeadership = 75),
            "Complete a regional operation and strategic briefing"
        ),
        RankRung(
            14, "Colonel", "COL",
            PromotionRequirement(serviceXp = 10_800, completedMissions = 8, minLeadership = 80),
            "Earn a high command evaluation and campaign milestone"
        ),
        RankRung(
            15, "Brigadier", "BDR",
            PromotionRequirement(serviceXp = 12_500, completedMissions = 9, minLeadership = 80),
            "Lead a multi-region operation"
        ),
        RankRung(
            16, "Major General", "MGEN",
            PromotionRequirement(serviceXp = 14_400, completedMissions = 9, minLeadership = 85),
            "Complete a major campaign review"
        ),
        RankRung(
            17, "Lieutenant General", "LTGEN",
            PromotionRequirement(serviceXp = 16_500, completedMissions = 10, minLeadership = 88),
            "Demonstrate consistent strategic leadership"
        ),
        RankRung(
            18, "General", "GEN",
            PromotionRequirement(serviceXp = 18_800, completedMissions = 10, minLeadership = 90),
            "Complete the main campaign with a qualifying record"
        ),
        RankRung(
            19, "Chief of Defence", "CDS",
            PromotionRequirement(serviceXp = 21_500, completedMissions = 10, minLeadership = 95),
            "Optional final appointment based on the overall campaign ending"
        )
    )

    val maxIndex: Int get() = RANKS.lastIndex

    fun rung(index: Int): RankRung = RANKS[index.coerceIn(0, maxIndex)]

    /** Every unmet requirement, phrased the way the rank screen shows it. */
    fun outstanding(requirement: PromotionRequirement, evidence: PromotionEvidence): List<String> {
        val missing = mutableListOf<String>()
        if (evidence.serviceXp < requirement.serviceXp) {
            missing.add("Service XP ${evidence.serviceXp} / ${requirement.serviceXp}")
        }
        if (evidence.completedMissions < requirement.completedMissions) {
            missing.add("Missions ${evidence.completedMissions} / ${requirement.completedMissions}")
        }
        val qualification = requirement.qualification
        if (qualification != null && qualification !in evidence.qualifications) {
            missing.add("Qualification: ${qualification.displayName}")
        }
        if (evidence.leadershipScore < requirement.minLeadership) {
            missing.add("Leadership ${evidence.leadershipScore} / ${requirement.minLeadership}")
        }
        return missing
    }

    private fun holds(requirement: PromotionRequirement, evidence: PromotionEvidence): Boolean =
        outstanding(requirement, evidence).isEmpty()

    /** Highest rung whose evidence is fully satisfied. */
    fun rankFor(evidence: PromotionEvidence): RankRung =
        RANKS.lastOrNull { holds(it.evidence, evidence) } ?: RANKS.first()

    fun review(evidence: PromotionEvidence): PromotionReview {
        val current = rankFor(evidence)
        val next = RANKS.getOrNull(current.index)
        if (next == null) {
            return PromotionReview(current, null, emptyList(), 1f)
        }
        val outstanding = outstanding(next.evidence, evidence)
        val currentRequirement = current.evidence.serviceXp
        val span = (next.evidence.serviceXp - currentRequirement).coerceAtLeast(1)
        val progress = ((evidence.serviceXp - currentRequirement).toFloat() / span).coerceIn(0f, 1f)
        return PromotionReview(current, next, outstanding, progress)
    }

    /** Rungs crossed between two reviews, so the AAR can announce the jump. */
    fun promotionsBetween(previous: PromotionEvidence, updated: PromotionEvidence): List<RankRung> {
        val from = rankFor(previous).index
        val to = rankFor(updated).index
        if (to <= from) return emptyList()
        return RANKS.filter { it.index > from && it.index <= to }
    }
}
