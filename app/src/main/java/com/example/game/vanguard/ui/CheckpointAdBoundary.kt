package com.example.game.vanguard.ui

import com.example.game.vanguard.core.EventBus
import com.example.game.vanguard.core.GameResult
import com.example.game.vanguard.meta.CombatState
import com.example.game.vanguard.meta.PostMissionHook
import com.example.game.vanguard.meta.SessionFlowGuard

/**
 * Section 2 UI: CheckpointAdBoundary.
 *
 * Section 3.4 item 4: no ad, store prompt or battle pass may appear while a
 * mission is running. This boundary is the only place the UI asks, so the guard
 * is impossible to bypass by calling a dialog directly.
 */
enum class CheckpointSurface(val hook: PostMissionHook, val displayName: String) {
    INTERSTITIAL(PostMissionHook.INTERSTITIAL_AD, "Interstitial Ad"),
    STORE(PostMissionHook.STORE_PROMPT, "Store Prompt"),
    BATTLE_PASS(PostMissionHook.BATTLE_PASS_POPUP, "Battle Pass Popup"),
    REWARD_SUMMARY(PostMissionHook.REWARD_SUMMARY, "Reward Summary"),
    SAVE(PostMissionHook.SAVE_CHECKPOINT, "Save Checkpoint")
}

object CheckpointAdBoundary {

    /** Everything a boundary decision needs, computed once. */
    data class Decision(
        val surface: CheckpointSurface,
        val allowed: Boolean,
        val reasonCode: String
    )

    fun mayPresent(state: CombatState): Boolean = SessionFlowGuard.mayRun(state)

    /** Presenting outside a terminal state publishes the diagnostic event. */
    fun request(surface: CheckpointSurface, state: CombatState, bus: EventBus): GameResult<Unit> =
        SessionFlowGuard.evaluate(surface.hook, state, bus)

    fun decide(surface: CheckpointSurface, state: CombatState): Decision {
        val result = SessionFlowGuard.evaluate(surface.hook, state)
        val reason = when (result) {
            is GameResult.Ok -> "ALLOWED"
            is GameResult.Err -> result.error.code
        }
        return Decision(surface = surface, allowed = result is GameResult.Ok, reasonCode = reason)
    }

    /** The status banner the mission HUD shows instead of an interrupted ad. */
    fun statusLine(state: CombatState): String = when (state) {
        CombatState.ACTIVE_MISSION -> "Mission in progress - interstitials locked"
        CombatState.IDLE -> "No mission boundary - interstitials locked"
        CombatState.MISSION_COMPLETED -> "Mission complete - rewards available"
        CombatState.MISSION_FAILED -> "Mission failed - retry available"
    }
}
