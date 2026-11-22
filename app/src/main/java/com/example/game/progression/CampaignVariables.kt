package com.example.game.progression

/**
 * The seven durable campaign variables from section 3.6. They are a plain data
 * snapshot so the story layer never has to reach into widgets or the database.
 */
data class CampaignVariables(
    /** Per-squadmate relationship score. A squad is built through trust. */
    val squadTrust: Map<String, Int> = emptyMap(),
    /** How senior leadership views the player. */
    val commandReputation: Int = 50,
    /** Outcomes of civilian-protection objectives. */
    val civilianSafetyRecord: Int = 50,
    /** How often objectives are completed without avoidable damage. */
    val missionIntegrity: Int = 50,
    /** Quality of information gathered and verified. */
    val intelligenceConfidence: Int = 0,
    /** Supplies, vehicle condition and support availability. */
    val resourceReadiness: Int = 50,
    /** The mentor's evaluation of discipline and judgement. */
    val mentorRespect: Int = 50
) {
    companion object {
        const val MIN = 0
        const val MAX = 100

        fun clampScore(value: Int): Int = value.coerceIn(MIN, MAX)

        /** Stable, attributable squad trust: unknown squadmates start neutral. */
        fun trustFor(trustMap: Map<String, Int>, squadmate: String): Int =
            clampScore(trustMap[squadmate] ?: 50)
    }

    fun normalised(): CampaignVariables = copy(
        squadTrust = squadTrust.mapValues { clampScore(it.value) },
        commandReputation = clampScore(commandReputation),
        civilianSafetyRecord = clampScore(civilianSafetyRecord),
        missionIntegrity = clampScore(missionIntegrity),
        intelligenceConfidence = clampScore(intelligenceConfidence),
        resourceReadiness = clampScore(resourceReadiness),
        mentorRespect = clampScore(mentorRespect)
    )

    /**
     * Effective leadership score. Officer ranks gate on this, per section 6.1,
     * and it is deliberately an average rather than a sum so no single huge
     * value can carry a promotion on its own.
     */
    val leadershipScore: Int
        get() {
            val trustAverage = if (squadTrust.isEmpty()) 50 else squadTrust.values.average().toInt()
            return ((commandReputation + civilianSafetyRecord + missionIntegrity + mentorRespect + trustAverage) / 5)
                .coerceIn(MIN, MAX)
        }
}

/** The four campaign outcome families from section 3.7. */
enum class CampaignEnding(
    val title: String,
    val description: String,
    val hidden: Boolean = false
) {
    GUARDIAN(
        "The Guardian",
        "A high civilian-safety record, strong squad trust and reliable command decisions. The operative becomes a respected senior leader."
    ),
    STRATEGIST(
        "The Strategist",
        "Strong operational success and high command reputation, at the cost of strained relationships. The operative is appointed to a major strategic role."
    ),
    COST_OF_VICTORY(
        "The Cost of Victory",
        "Missions were completed, but avoidable losses and poor decisions reduced trust. The organisation's confidence has to be rebuilt."
    ),
    SENTINEL(
        "The Sentinel Protocol",
        "Excellence across every recorded system. The operative holds the line the nation never sees.",
        hidden = true
    )
}

/**
 * Ending selection from the campaign variables. The hidden fourth ending needs
 * excellence across all major systems, which is why it is checked first.
 */
object EndingEvaluator {

    const val HIGH = 75
    const val LOW = 40

    fun evaluate(variables: CampaignVariables): CampaignEnding {
        val trustAverage = if (variables.squadTrust.isEmpty()) 50 else variables.squadTrust.values.average().toInt()

        val excellentAcrossSystems = variables.civilianSafetyRecord >= HIGH &&
            trustAverage >= HIGH &&
            variables.commandReputation >= HIGH &&
            variables.missionIntegrity >= HIGH &&
            variables.mentorRespect >= HIGH &&
            variables.intelligenceConfidence >= HIGH
        if (excellentAcrossSystems) return CampaignEnding.SENTINEL

        val guardian = variables.civilianSafetyRecord >= HIGH &&
            trustAverage >= HIGH &&
            variables.commandReputation >= 60
        if (guardian) return CampaignEnding.GUARDIAN

        val strategist = variables.commandReputation >= HIGH &&
            variables.missionIntegrity >= 60 &&
            trustAverage < HIGH
        if (strategist) return CampaignEnding.STRATEGIST

        if (variables.civilianSafetyRecord <= LOW ||
            trustAverage <= LOW ||
            variables.mentorRespect <= LOW
        ) {
            return CampaignEnding.COST_OF_VICTORY
        }

        // A quiet, middling record still resolves through the strategic family.
        return CampaignEnding.STRATEGIST
    }
}
