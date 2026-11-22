package com.example.game.vanguard.narrative

import com.example.game.vanguard.core.GameErrorCategory
import com.example.game.vanguard.core.GameResult

/**
 * Section 3.4 item 1: the three persistent officer attributes. They are signed
 * scores in [-100, +100] and they feed squad responsiveness and budget
 * allocation, so a dialogue choice is never purely cosmetic.
 */
data class OfficerAttributes(
    val tacticalDoctrine: Int = 0,
    val unitMorale: Int = 0,
    val politicalCapital: Int = 0
) {
    fun adjusted(doctrine: Int, morale: Int, capital: Int): OfficerAttributes = OfficerAttributes(
        tacticalDoctrine = clamp(tacticalDoctrine + doctrine),
        unitMorale = clamp(unitMorale + morale),
        politicalCapital = clamp(politicalCapital + capital)
    )

    /** 0.80 - 1.20 multiplier handed to squad AI reaction checks. */
    val squadResponsiveness: Float get() = 1f + unitMorale / 500f

    /** 0.75 - 1.25 multiplier handed to the base budget allocator. */
    val budgetMultiplier: Float get() = 1f + politicalCapital / 400f

    companion object {
        const val FLOOR = -100
        const val CEILING = 100

        fun clamp(value: Int): Int = value.coerceIn(FLOOR, CEILING)
    }
}

enum class DilemmaContext(val displayName: String) {
    ACADEMY_BOARD("Academy Evaluation Board"),
    FIELD_INTERROGATION("Field Interrogation"),
    TV_INTERVIEW("Televised Interview"),
    PUBLIC_RALLY("Public Rally")
}

/**
 * One option always buys something and costs something else, which is what
 * makes the choice a leadership decision instead of a quiz.
 */
data class DilemmaOption(
    val id: String,
    val label: String,
    val doctrineDelta: Int,
    val moraleDelta: Int,
    val capitalDelta: Int,
    val consequence: String
) {
    val deltas: List<Int> get() = listOf(doctrineDelta, moraleDelta, capitalDelta)
}

data class Dilemma(
    val id: String,
    val context: DilemmaContext,
    val prompt: String,
    val options: List<DilemmaOption>
)

data class DilemmaOutcome(
    val dilemmaId: String,
    val optionId: String,
    val attributes: OfficerAttributes,
    val consequence: String,
    val squadResponsiveness: Float,
    val budgetMultiplier: Float
)

/**
 * Validation and resolution. Three options are mandatory, identifiers must be
 * unique, and no single option may dominate on all three axes - that check is
 * what "not trivially obvious" means in code.
 */
object DilemmaEngine {

    const val REQUIRED_OPTIONS = 3

    fun validate(dilemma: Dilemma): GameResult<Dilemma> {
        if (dilemma.options.size != REQUIRED_OPTIONS) {
            return GameResult.fail(
                GameErrorCategory.NARRATIVE,
                "WRONG_OPTION_COUNT",
                "A leadership dilemma needs exactly three options"
            )
        }
        if (dilemma.options.map { it.id }.toSet().size != dilemma.options.size) {
            return GameResult.fail(
                GameErrorCategory.NARRATIVE,
                "DUPLICATE_OPTION_ID",
                "Option identifiers must be unique"
            )
        }
        if (dominatingOption(dilemma) != null) {
            return GameResult.fail(
                GameErrorCategory.NARRATIVE,
                "TRIVIAL_DILEMMA",
                "One option dominates all three attributes; the choice is not a decision"
            )
        }
        return GameResult.Ok(dilemma)
    }

    fun choose(
        dilemma: Dilemma,
        optionId: String,
        current: OfficerAttributes
    ): GameResult<DilemmaOutcome> {
        validate(dilemma).let { if (it is GameResult.Err) return it }
        val option = dilemma.options.firstOrNull { it.id == optionId }
            ?: return GameResult.fail(
                GameErrorCategory.NARRATIVE,
                "UNKNOWN_OPTION",
                "No option with id $optionId in ${dilemma.id}"
            )
        val updated = current.adjusted(option.doctrineDelta, option.moraleDelta, option.capitalDelta)
        return GameResult.Ok(
            DilemmaOutcome(
                dilemmaId = dilemma.id,
                optionId = option.id,
                attributes = updated,
                consequence = option.consequence,
                squadResponsiveness = updated.squadResponsiveness,
                budgetMultiplier = updated.budgetMultiplier
            )
        )
    }

    /** Returns the id of an option that is >= every rival on every axis. */
    fun dominatingOption(dilemma: Dilemma): String? {
        if (dilemma.options.size < 2) return null
        for (candidate in dilemma.options) {
            val dominates = dilemma.options
                .filter { it.id != candidate.id }
                .all { rival ->
                    candidate.doctrineDelta >= rival.doctrineDelta &&
                        candidate.moraleDelta >= rival.moraleDelta &&
                        candidate.capitalDelta >= rival.capitalDelta
                }
            if (dominates) return candidate.id
        }
        return null
    }
}

/** Sample content, kept beside the engine so the audit can drive a real choice. */
object DilemmaLibrary {

    val ACADEMY_BOARD_LIVE_FIRE = Dilemma(
        id = "DIL_ACADEMY_LIVE_FIRE",
        context = DilemmaContext.ACADEMY_BOARD,
        prompt = "A cadet broke the surface-danger rule to pull a teammate off the range. Do you endorse the instinct?",
        options = listOf(
            DilemmaOption(
                id = "ENDORSE",
                label = "Publicly endorse the instinct",
                doctrineDelta = -8,
                moraleDelta = 14,
                capitalDelta = -5,
                consequence = "The squad trusts you; the board questions your discipline."
            ),
            DilemmaOption(
                id = "CORRECT",
                label = "Correct the breach, commend the intent privately",
                doctrineDelta = 10,
                moraleDelta = 3,
                capitalDelta = 4,
                consequence = "Doctrine is upheld without humiliating the cadet."
            ),
            DilemmaOption(
                id = "DEFER",
                label = "Defer the ruling to the senior NCO",
                doctrineDelta = -2,
                moraleDelta = -6,
                capitalDelta = 7,
                consequence = "The board respects the chain of command; the squad notices you stepped back."
            )
        )
    )

    val FIELD_INTERROGATION_TIMER = Dilemma(
        id = "DIL_FIELD_INTERROGATION",
        context = DilemmaContext.FIELD_INTERROGATION,
        prompt = "The detainee offers the convoy route, but only if the medic treats his brother first.",
        options = listOf(
            DilemmaOption(
                id = "TREAT_FIRST",
                label = "Treat the brother, then talk",
                doctrineDelta = -6,
                moraleDelta = 12,
                capitalDelta = -4,
                consequence = "The route arrives late; the platoon sees who you are."
            ),
            DilemmaOption(
                id = "PRESS_HARD",
                label = "Press for the route now",
                doctrineDelta = 9,
                moraleDelta = -11,
                capitalDelta = 5,
                consequence = "The convoy moves on time and the district remembers."
            ),
            DilemmaOption(
                id = "SPLIT_TEAM",
                label = "Split the medic from the convoy",
                doctrineDelta = 3,
                moraleDelta = 2,
                capitalDelta = -2,
                consequence = "Both tasks run thin, and nobody is fully covered."
            )
        )
    )

    val ALL: List<Dilemma> = listOf(ACADEMY_BOARD_LIVE_FIRE, FIELD_INTERROGATION_TIMER)
}
