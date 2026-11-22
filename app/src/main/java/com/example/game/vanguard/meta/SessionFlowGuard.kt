package com.example.game.vanguard.meta

import com.example.game.vanguard.core.EventBus
import com.example.game.vanguard.core.GameErrorCategory
import com.example.game.vanguard.core.GameResult
import com.example.game.vanguard.core.SessionFlowBlocked

/**
 * Section 3.4 item 4: the checkpoint guard. Combat is the one place the game is
 * allowed to be uninterruptible, so every interstitial, store prompt and
 * battle-pass offer asks this class first.
 */
enum class CombatState(val displayName: String) {
    IDLE("No mission running"),
    ACTIVE_MISSION("Active Mission"),
    MISSION_COMPLETED("OnMissionCompleted"),
    MISSION_FAILED("OnMissionFailed");

    val isTerminal: Boolean get() = this == MISSION_COMPLETED || this == MISSION_FAILED
}

enum class PostMissionHook(val displayName: String) {
    INTERSTITIAL_AD("Interstitial Ad"),
    STORE_PROMPT("Store Prompt"),
    BATTLE_PASS_POPUP("Battle Pass Popup"),
    REWARD_SUMMARY("Reward Summary"),
    SAVE_CHECKPOINT("Save Checkpoint"),
    PRODUCTION_CATCH_UP("Production Catch-Up")
}

object SessionFlowGuard {

    /** The single predicate every presentation hook must consult. */
    fun mayRun(state: CombatState): Boolean = state.isTerminal

    fun evaluate(hook: PostMissionHook, state: CombatState): GameResult<Unit> {
        if (state.isTerminal) return GameResult.Ok(Unit)
        val code = if (state == CombatState.ACTIVE_MISSION) "BLOCKED_DURING_ACTIVE_MISSION" else "BLOCKED_NO_BOUNDARY"
        return GameResult.fail(
            GameErrorCategory.SESSION_FLOW,
            code,
            "${hook.displayName} may only run after a mission completes or fails (state: ${state.displayName})"
        )
    }

    /** Same rule, plus the diagnostic event the QA overlay listens to. */
    fun evaluate(hook: PostMissionHook, state: CombatState, bus: EventBus): GameResult<Unit> {
        val result = evaluate(hook, state)
        if (result is GameResult.Err) {
            bus.publish(SessionFlowBlocked(hook.displayName, state.displayName))
        }
        return result
    }
}

// ------------------------------------------------------- meta camera (3.4.2)

enum class MetaZone(val displayName: String, val cameraHeightMeters: Float) {
    ACADEMY_GROUNDS("Academy Grounds", 24f),
    VEHICLE_BAY("Vehicle Customization Bay", 40f),
    NAVAL_DOCKYARD("Naval Production Dockyard", 90f),
    BATTLE_MAP("Tactical Battle Map", 150f)
}

data class MetaCameraState(
    val from: MetaZone,
    val to: MetaZone,
    val blend01: Float
) {
    val cameraHeightMeters: Float
        get() = from.cameraHeightMeters + (to.cameraHeightMeters - from.cameraHeightMeters) * blend01

    val isSettled: Boolean get() = blend01 >= 1f || (from == to)
}

/**
 * The pinch/toggle zoom-out that links the academy grounds, the vehicle bay, the
 * dockyard and the battle map. Blend is a pure function of pinch progress, so a
 * dropped frame cannot desynchronise the camera from the UI.
 */
object MetaCameraController {

    fun zoom(from: MetaZone, to: MetaZone, pinchProgress01: Float): MetaCameraState =
        MetaCameraState(from = from, to = to, blend01 = pinchProgress01.coerceIn(0f, 1f))

    fun nextZone(current: MetaZone): MetaZone = when (current) {
        MetaZone.ACADEMY_GROUNDS -> MetaZone.VEHICLE_BAY
        MetaZone.VEHICLE_BAY -> MetaZone.NAVAL_DOCKYARD
        MetaZone.NAVAL_DOCKYARD -> MetaZone.BATTLE_MAP
        MetaZone.BATTLE_MAP -> MetaZone.BATTLE_MAP
    }

    fun previousZone(current: MetaZone): MetaZone = when (current) {
        MetaZone.ACADEMY_GROUNDS -> MetaZone.ACADEMY_GROUNDS
        MetaZone.VEHICLE_BAY -> MetaZone.ACADEMY_GROUNDS
        MetaZone.NAVAL_DOCKYARD -> MetaZone.VEHICLE_BAY
        MetaZone.BATTLE_MAP -> MetaZone.NAVAL_DOCKYARD
    }
}
