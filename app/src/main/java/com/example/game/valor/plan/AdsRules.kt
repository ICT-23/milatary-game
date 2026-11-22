package com.example.game.valor.plan

/** Section 5.22: the three places the player may choose to watch a rewarded ad. */
enum class AdPlacement(val displayName: String) {
    DOUBLE_REWARD_RESULTS("Watch ad for 2x reward (Results)"),
    REVIVE("Revive"),
    FREE_CRATE("Free crate")
}

/**
 * Section 5.22: no ad ever plays during gameplay or a cutscene, interstitials sit
 * between missions only, and nothing sold is required to progress.
 */
object AdsRules {

    const val NEVER_IN_GAMEPLAY = true
    const val NEVER_IN_CUTSCENE = true
    const val NO_DEAD_BUTTONS = true
    const val REMOVE_ADS_DISABLES_INTERSTITIALS = true
    const val REMOVE_ADS_RESTORED_ON_REINSTALL = true
    const val NO_PAY_TO_WIN = true

    /** "No more than once per 3 missions." */
    const val MIN_MISSIONS_BETWEEN = 3

    val ALLOWED_POINTS: List<AdPlacement> = AdPlacement.entries

    /** The states the document calls gameplay: an ad must never be shown here. */
    val GAMEPLAY_STATES: Set<MissionState> = setOf(
        MissionState.PLAYING,
        MissionState.PAUSED,
        MissionState.COMPLETING
    )

    fun isGameplay(state: MissionState): Boolean = state in GAMEPLAY_STATES

    fun isCutscene(state: MissionState): Boolean = state == MissionState.INTRO

    /** A rewarded ad is only offered at one of the three points, outside both. */
    fun rewardedAllowed(placement: AdPlacement, state: MissionState): Boolean =
        placement in ALLOWED_POINTS && !isGameplay(state) && !isCutscene(state)

    /** "If no ad is available -> hide/disable the button (no dead buttons)." */
    fun buttonVisible(adAvailable: Boolean): Boolean = adAvailable

    fun buttonEnabled(adAvailable: Boolean): Boolean = adAvailable

    fun interstitialsEnabled(removeAdsOwned: Boolean): Boolean = !removeAdsOwned

    /** "No pay-to-win: everything purchasable has an in-game earning path." */
    fun hasEarningPath(itemId: String, earnableInGame: Set<String>): Boolean = itemId in earnableInGame

    fun isPayToWin(itemId: String, earnableInGame: Set<String>): Boolean = !hasEarningPath(itemId, earnableInGame)
}

/**
 * Section 5.22: an interstitial may appear only between missions, no more than
 * once every three missions, and never straight after a failure.
 */
class InterstitialScheduler(private var missionsSinceLast: Int = AdsRules.MIN_MISSIONS_BETWEEN) {

    fun missionsSinceLast(): Int = missionsSinceLast

    fun canShow(betweenMissions: Boolean, previousMissionFailed: Boolean, removeAdsOwned: Boolean): Boolean =
        betweenMissions &&
            !previousMissionFailed &&
            !removeAdsOwned &&
            missionsSinceLast >= AdsRules.MIN_MISSIONS_BETWEEN

    fun recordMissionEnd() {
        missionsSinceLast += 1
    }

    fun recordShown() {
        missionsSinceLast = 0
    }

    /** A failure is never followed by an ad, so the counter keeps running. */
    fun recordFailure() {
        missionsSinceLast += 1
    }
}
