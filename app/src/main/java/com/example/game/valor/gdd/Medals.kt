package com.example.game.valor.gdd

/** Section 4.1: the four headshot badges are the only repeated threshold. */
enum class GddMedal(val displayName: String, val requirement: String) {
    MARKSMAN("Marksman Badge", "50 headshots"),
    SHARPSHOOTER("Sharpshooter Badge", "200 headshots"),
    EXPERT("Expert Badge", "1,000 headshots"),
    PURPLE_HEART("Purple Heart", "Survive a mission with <10% health"),
    BRONZE_STAR("Bronze Star", "Complete all bonus objectives in an Act"),
    SILVER_STAR("Silver Star", "Complete all missions of an Act on Hard"),
    DISTINGUISHED_SERVICE_CROSS("Distinguished Service Cross", "Beat a boss without taking damage"),
    MEDAL_OF_HONOR("Medal of Honor", "Story finale")
}

/**
 * One snapshot of everything the medal table reads. Fields are counted, not
 * booleans per act, so the gallery can show progress as well as awards.
 */
data class MedalProgress(
    val headshots: Int = 0,
    val lowestHealthPercent: Int = 100,
    val actsWithAllBonusObjectives: Set<Int> = emptySet(),
    val actsClearedOnHard: Set<Int> = emptySet(),
    val bossesBeatenFlawless: Int = 0,
    val storyComplete: Boolean = false
)

object MedalRules {

    const val MARKSMAN_HEADSHOTS = 50
    const val SHARPSHOOTER_HEADSHOTS = 200
    const val EXPERT_HEADSHOTS = 1_000

    /** "Survive a mission with <10% health": strictly below ten percent. */
    const val PURPLE_HEART_BELOW_PERCENT = 10

    val HEADSHOT_MEDALS: List<GddMedal> = listOf(
        GddMedal.MARKSMAN,
        GddMedal.SHARPSHOOTER,
        GddMedal.EXPERT
    )

    fun headshotThreshold(medal: GddMedal): Int? = when (medal) {
        GddMedal.MARKSMAN -> MARKSMAN_HEADSHOTS
        GddMedal.SHARPSHOOTER -> SHARPSHOOTER_HEADSHOTS
        GddMedal.EXPERT -> EXPERT_HEADSHOTS
        else -> null
    }

    /** Every medal the snapshot earns, in the document's order. */
    fun earned(progress: MedalProgress): List<GddMedal> = listOfNotNull(
        GddMedal.MARKSMAN.takeIf { progress.headshots >= MARKSMAN_HEADSHOTS },
        GddMedal.SHARPSHOOTER.takeIf { progress.headshots >= SHARPSHOOTER_HEADSHOTS },
        GddMedal.EXPERT.takeIf { progress.headshots >= EXPERT_HEADSHOTS },
        GddMedal.PURPLE_HEART.takeIf { progress.lowestHealthPercent < PURPLE_HEART_BELOW_PERCENT },
        GddMedal.BRONZE_STAR.takeIf { progress.actsWithAllBonusObjectives.isNotEmpty() },
        GddMedal.SILVER_STAR.takeIf { progress.actsClearedOnHard.isNotEmpty() },
        GddMedal.DISTINGUISHED_SERVICE_CROSS.takeIf { progress.bossesBeatenFlawless > 0 },
        GddMedal.MEDAL_OF_HONOR.takeIf { progress.storyComplete }
    )

    fun missing(progress: MedalProgress): List<GddMedal> =
        GddMedal.entries.filterNot { it in earned(progress) }

    /** The next headshot badge still to earn, or null once Expert is held. */
    fun nextHeadshotMedal(headshots: Int): GddMedal? =
        HEADSHOT_MEDALS.firstOrNull { headshots < (headshotThreshold(it) ?: 0) }

    /** Headshots still needed for a badge; zero when it is already held. */
    fun headshotsRemaining(headshots: Int, medal: GddMedal): Int {
        val threshold = headshotThreshold(medal) ?: return 0
        return (threshold - headshots).coerceAtLeast(0)
    }
}

/** Section 4.1 gallery: the eight awards in table order with their rule text. */
object MedalGallery {

    val MEDALS: List<GddMedal> = GddMedal.entries

    fun count(): Int = MEDALS.size

    fun requirementOf(medal: GddMedal): String = medal.requirement

    /** The story award is the only one that cannot be earned twice. */
    fun isStoryAward(medal: GddMedal): Boolean = medal == GddMedal.MEDAL_OF_HONOR
}
