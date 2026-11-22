package com.example.game.progression

/** Transparent S / A / B / C / D rating from section 6.4. */
enum class MissionGrade(val letter: String, val label: String) {
    S("S", "EXEMPLARY"),
    A("A", "DISTINGUISHED"),
    B("B", "EFFECTIVE"),
    C("C", "ADEQUATE"),
    D("D", "DEFICIENT");

    companion object {
        fun forScore(score: Int): MissionGrade = when {
            score >= 90 -> S
            score >= 78 -> A
            score >= 62 -> B
            score >= 45 -> C
            else -> D
        }
    }
}

/**
 * Recorded mission events the grade is derived from. Every field is a number the
 * build already tracks, so the grade can always be explained to the player.
 */
data class GradeEvidence(
    val primaryCompleted: Int,
    val primaryTotal: Int,
    val optionalCompleted: Int = 0,
    val optionalTotal: Int = 0,
    val accuracyPercent: Int = 100,
    val squadConditionPercent: Int = 100,
    /** Civilian safety, only meaningful when the mission protects people. */
    val civilianSafetyPercent: Int = 100,
    val civiliansAtRisk: Boolean = false,
    /** Speed is only scored when the briefing says speed is part of the design. */
    val speedMatters: Boolean = false,
    val withinParTime: Boolean = true,
    /** Avoidable mission violations, e.g. hitting a protected target. */
    val violations: Int = 0
)

/** Grade plus the reason for it, as the debrief screen is required to show. */
data class MissionGradeResult(
    val grade: MissionGrade,
    val score: Int,
    val reasons: List<String>
)

/**
 * Weighted, deterministic grading. A careful success can never lose to a fast but
 * careless run, because speed carries the smallest weight and is opt-in.
 */
object MissionGrader {

    private const val WEIGHT_PRIMARY = 40
    private const val WEIGHT_OPTIONAL = 15
    private const val WEIGHT_ACCURACY = 15
    private const val WEIGHT_SQUAD = 10
    private const val WEIGHT_CIVILIAN = 15
    private const val WEIGHT_SPEED = 5
    private const val VIOLATION_PENALTY = 10

    fun evaluate(evidence: GradeEvidence): MissionGradeResult {
        val reasons = mutableListOf<String>()

        val primaryRatio = if (evidence.primaryTotal <= 0) 1f else {
            evidence.primaryCompleted.toFloat() / evidence.primaryTotal
        }.coerceIn(0f, 1f)
        val optionalRatio = if (evidence.optionalTotal <= 0) 1f else {
            evidence.optionalCompleted.toFloat() / evidence.optionalTotal
        }.coerceIn(0f, 1f)

        var score = primaryRatio * WEIGHT_PRIMARY
        score += optionalRatio * WEIGHT_OPTIONAL
        score += evidence.accuracyPercent.coerceIn(0, 100) / 100f * WEIGHT_ACCURACY
        score += evidence.squadConditionPercent.coerceIn(0, 100) / 100f * WEIGHT_SQUAD
        if (evidence.civiliansAtRisk) {
            score += evidence.civilianSafetyPercent.coerceIn(0, 100) / 100f * WEIGHT_CIVILIAN
        } else {
            // No protected people in the area: redistribute rather than punish.
            score += WEIGHT_CIVILIAN * primaryRatio
        }
        if (evidence.speedMatters && evidence.withinParTime) {
            score += WEIGHT_SPEED
        }
        score -= evidence.violations.coerceAtLeast(0) * VIOLATION_PENALTY

        reasons.add("Primary objectives ${evidence.primaryCompleted}/${evidence.primaryTotal.coerceAtLeast(1)}")
        if (evidence.optionalTotal > 0) {
            reasons.add("Optional objectives ${evidence.optionalCompleted}/${evidence.optionalTotal}")
        }
        reasons.add("Accuracy ${evidence.accuracyPercent.coerceIn(0, 100)}%")
        reasons.add("Squad condition ${evidence.squadConditionPercent.coerceIn(0, 100)}%")
        if (evidence.civiliansAtRisk) {
            reasons.add("Civilian safety ${evidence.civilianSafetyPercent.coerceIn(0, 100)}%")
        }
        if (evidence.speedMatters && evidence.withinParTime) {
            reasons.add("Par time met")
        }
        if (evidence.violations > 0) {
            reasons.add("${evidence.violations} avoidable violation(s), -${evidence.violations * VIOLATION_PENALTY}")
        }

        val clamped = score.coerceIn(0f, 100f)
        var grade = MissionGrade.forScore(clamped.toInt())

        // An incomplete mission can never be rated exemplary, whatever the maths says.
        if (primaryRatio < 1f && (grade == MissionGrade.S || grade == MissionGrade.A)) {
            grade = MissionGrade.B
            reasons.add("Incomplete primary objectives cap the grade at B")
        }
        // Civilian casualties are a mission-rule failure, not a bonus.
        if (evidence.violations > 0 && grade == MissionGrade.S) {
            grade = MissionGrade.A
            reasons.add("Violations prevent an S rating")
        }

        return MissionGradeResult(grade = grade, score = clamped.toInt(), reasons = reasons)
    }
}
