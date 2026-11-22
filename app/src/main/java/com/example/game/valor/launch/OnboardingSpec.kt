package com.example.game.valor.launch

/** Section 6.1: one row of the first-ten-minutes flow. */
data class FtueStep(
    val offsetSeconds: Int,
    val label: String,
    val detail: String
) {
    val clock: String
        get() = "%d:%02d".format(offsetSeconds / 60, offsetSeconds % 60)
}

/**
 * Section 6.1: the flow, the timings and the four rules around it.
 *
 * The steps are written with the document's own clock so a screen can say
 * "1:00 - character creator" without re-deriving the minute.
 */
object FtueFlow {

    /** "Cinematic: arrival at Fort Valor (20 s, skippable)". */
    const val CINEMATIC_SECONDS = 20
    const val CINEMATIC_SKIPPABLE = true

    /** "Character creator (quick: 3 taps; 'randomize' button)". */
    const val CHARACTER_CREATOR_TAPS = 3
    const val RANDOMIZE_BUTTON = true

    /** "0:00 Splash -> short consent/age -> language". */
    const val LANGUAGE_PICKER_IN_FTUE = true

    val STEPS: List<FtueStep> = listOf(
        FtueStep(0, "Splash", "Short consent/age, then language"),
        FtueStep(30, "Cinematic", "Arrival at Fort Valor ($CINEMATIC_SECONDS s, skippable)"),
        FtueStep(60, "Character creator", "Quick: $CHARACTER_CREATOR_TAPS taps plus a randomize button"),
        FtueStep(120, "Mission 1", "Movement and look, with contextual prompts and big arrows"),
        FtueStep(240, "Missions 2/3", "First shots, headshot hint, reload prompt"),
        FtueStep(
            420,
            "First reward",
            "Rank-up to Private, celebratory popup, Armory intro (equip a free item)"
        ),
        FtueStep(540, "Mission 4", "Begins; a small \"Daily reward\" popup appears")
    )

    fun stepAt(seconds: Int): FtueStep? =
        STEPS.lastOrNull { it.offsetSeconds <= seconds }

    fun stepFor(label: String): FtueStep? =
        STEPS.firstOrNull { it.label.equals(label.trim(), ignoreCase = true) }

    fun durationSeconds(): Int = STEPS.last().offsetSeconds

    fun clockLabels(): List<String> = STEPS.map { it.clock }
}

/** Section 6.1: the four rules that bound the whole first session. */
object FtueRules {

    /** "never show an ad in the first 20 minutes". */
    const val NO_AD_MINUTES = AdPlacements.FIRST_SESSION_MINUTES

    const val TUTORIAL_PROMPTS_DISMISSIBLE = true
    const val TUTORIAL_PROMPTS_SKIPPABLE = true
    const val CONTROLS_CUSTOMIZABLE_FROM_FIRST_GAMEPLAY_SCREEN = true
    const val FIRST_TOURNAMENT_OPTIONAL = true
    const val FIRST_TOURNAMENT_PASSABLE = true

    fun adsAllowed(elapsedSeconds: Int): Boolean = elapsedSeconds >= NO_AD_MINUTES * 60

    /** A prompt the player dismissed must never come back in the same run. */
    fun showsAgainAfterDismissal(): Boolean = false

    /** The controls screen is reachable from the first gameplay frame. */
    fun controlsScreenReachableAtFirstFrame(): Boolean =
        CONTROLS_CUSTOMIZABLE_FROM_FIRST_GAMEPLAY_SCREEN

    /** The tournament the story hands the player early never blocks the campaign. */
    fun tournamentBlocksStory(): Boolean = !FIRST_TOURNAMENT_OPTIONAL
}

/** Section 6.2: one retention hook and where it lives. */
data class RetentionHook(val hook: String, val surface: String)

/** Section 6.2: the seven hooks, including the push policy. */
object RetentionHooks {

    val ALL: List<RetentionHook> = listOf(
        RetentionHook("Daily login reward", "7-day cycle"),
        RetentionHook("Daily Operation", "Operations tab"),
        RetentionHook("Weekly Challenge", "Challenge tab"),
        RetentionHook("Rank goals", "Results screen"),
        RetentionHook("Next unlock teaser", "Results and Armory"),
        RetentionHook("Medal collection", "Collection screen"),
        RetentionHook("Push notifications", "Opt-in, max 1/day")
    )

    const val PUSH_MAX_PER_DAY = 1
    const val PUSH_MESSAGE = "Your daily op is ready"

    fun hookNamed(name: String): RetentionHook? =
        ALL.firstOrNull { it.hook.equals(name.trim(), ignoreCase = true) }

    /** "Rank goals shown on Results" is a Results-screen promise, not a HUD one. */
    fun rankGoalsOnResults(): Boolean =
        ALL.firstOrNull { it.hook == "Rank goals" }?.surface?.contains("Results") == true
}

/**
 * Section 6.2: the seven-day login cycle.
 *
 * [claim] returns the day that was just paid (1..7). The eighth claim closes the
 * cycle and starts the next one, so the parade never stalls on day 7.
 */
class DailyLoginCycle(val days: Int = DAYS) {

    var claimedDays: Int = 0
        private set

    var completedCycles: Int = 0
        private set

    val nextDay: Int get() = if (claimedDays >= days) 1 else claimedDays + 1

    val cycleComplete: Boolean get() = claimedDays >= days

    fun claim(): Int {
        if (claimedDays >= days) {
            completedCycles += 1
            claimedDays = 0
        }
        claimedDays += 1
        return claimedDays
    }

    /** The reward itself is a reward-table entry, not a number invented here. */
    fun rewardId(day: Int = nextDay): String = "daily_login_day_$day"

    companion object {
        const val DAYS = 7
    }
}

/** Section 6.2: "push notifications (opt-in, max 1/day)". */
class PushNotificationPolicy(optIn: Boolean = false) {

    var optIn: Boolean = optIn
        private set

    private var sentToday: Int = 0

    fun enabled(enabled: Boolean) {
        optIn = enabled
        if (!enabled) sentToday = 0
    }

    fun canSend(): Boolean = optIn && sentToday < RetentionHooks.PUSH_MAX_PER_DAY

    fun recordSent(): Boolean {
        if (!canSend()) return false
        sentToday += 1
        return true
    }

    fun sentCountToday(): Int = sentToday

    fun newDay() {
        sentToday = 0
    }

    fun message(): String = RetentionHooks.PUSH_MESSAGE
}
