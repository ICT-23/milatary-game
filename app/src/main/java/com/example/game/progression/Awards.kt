package com.example.game.progression

/**
 * The nine commendations from section 6.5. Every award is earned from recorded
 * gameplay events; none can be granted by a button press alone.
 */
enum class Commendation(
    val awardId: String,
    val title: String,
    val condition: String
) {
    ACADEMY_DISTINCTION(
        "AWARD_ACADEMY_DISTINCTION",
        "Academy Distinction",
        "Complete all core academy qualifications"
    ),
    PRECISION_MARK(
        "AWARD_PRECISION_MARK",
        "Precision Mark",
        "Earn a high marksmanship grade without wasting the qualification allowance"
    ),
    GUARDIAN_RIBBON(
        "AWARD_GUARDIAN_RIBBON",
        "Guardian Ribbon",
        "Complete a mission with all protected-person objectives safe"
    ),
    SQUAD_FIRST(
        "AWARD_SQUAD_FIRST",
        "Squad First",
        "Extract with every squadmate in a healthy state"
    ),
    FIELD_ENGINEER(
        "AWARD_FIELD_ENGINEER",
        "Field Engineer",
        "Complete a repair or communications objective efficiently"
    ),
    RECON_MERIT(
        "AWARD_RECON_MERIT",
        "Recon Merit",
        "Discover and verify all key intelligence points in a mission"
    ),
    STORM_SERVICE(
        "AWARD_STORM_SERVICE",
        "Storm Service",
        "Complete a weather-affected operation with no lost objective"
    ),
    COMMAND_EXCELLENCE(
        "AWARD_COMMAND_EXCELLENCE",
        "Command Excellence",
        "Earn a high grade in a mission with multiple squad orders"
    ),
    SENTINEL_MEDAL(
        "AWARD_SENTINEL_MEDAL",
        "Sentinel Medal",
        "Complete the final campaign under the highest leadership conditions"
    )
}

/** Recorded events a debrief hands to the award ledger. */
data class AwardEvidence(
    val coreQualificationsComplete: Boolean = false,
    val marksmanshipGrade: MissionGrade? = null,
    val qualificationAllowanceWasted: Boolean = false,
    val protectedObjectivesAllSafe: Boolean = false,
    val squadAllHealthy: Boolean = false,
    val repairObjectiveEfficient: Boolean = false,
    val keyIntelVerified: Int = 0,
    val keyIntelTotal: Int = 0,
    val weatherAffected: Boolean = false,
    val objectiveLost: Boolean = false,
    val squadOrdersIssued: Int = 0,
    val missionGrade: MissionGrade? = null,
    val campaignComplete: Boolean = false,
    val leadershipScore: Int = 0
)

/**
 * Idempotent award evaluation. The caller passes the awards already recorded in
 * the save, so replaying a mission can never mint the same medal twice.
 */
object AwardLedger {

    /** Leadership score required for the Sentinel Medal's "highest conditions". */
    const val SENTINEL_LEADERSHIP_THRESHOLD = 90

    /** Squad-order count that makes a mission count as command-level. */
    const val COMMAND_ORDER_THRESHOLD = 3

    fun earned(evidence: AwardEvidence): List<Commendation> {
        val result = mutableListOf<Commendation>()

        if (evidence.coreQualificationsComplete) result.add(Commendation.ACADEMY_DISTINCTION)

        val marksmanship = evidence.marksmanshipGrade
        if ((marksmanship == MissionGrade.S || marksmanship == MissionGrade.A) &&
            !evidence.qualificationAllowanceWasted
        ) {
            result.add(Commendation.PRECISION_MARK)
        }

        if (evidence.protectedObjectivesAllSafe) result.add(Commendation.GUARDIAN_RIBBON)
        if (evidence.squadAllHealthy) result.add(Commendation.SQUAD_FIRST)
        if (evidence.repairObjectiveEfficient) result.add(Commendation.FIELD_ENGINEER)

        if (evidence.keyIntelTotal > 0 && evidence.keyIntelVerified >= evidence.keyIntelTotal) {
            result.add(Commendation.RECON_MERIT)
        }

        if (evidence.weatherAffected && !evidence.objectiveLost) {
            result.add(Commendation.STORM_SERVICE)
        }

        val grade = evidence.missionGrade
        if (evidence.squadOrdersIssued >= COMMAND_ORDER_THRESHOLD &&
            (grade == MissionGrade.S || grade == MissionGrade.A)
        ) {
            result.add(Commendation.COMMAND_EXCELLENCE)
        }

        if (evidence.campaignComplete &&
            evidence.leadershipScore >= SENTINEL_LEADERSHIP_THRESHOLD
        ) {
            result.add(Commendation.SENTINEL_MEDAL)
        }

        return result
    }

    /** Awards this run added, so the debrief only announces what is genuinely new. */
    fun newlyEarned(evidence: AwardEvidence, alreadyRecorded: Set<String>): List<Commendation> =
        earned(evidence).filter { it.awardId !in alreadyRecorded }
}
