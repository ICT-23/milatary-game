package com.example.game.tactical.movement

/**
 * Phase 2 deliverable: the dynamic camera.
 *
 * Three rigs - Idle, Combat Shoulder and ADS Zoom - are blended, never swapped,
 * so raising the weapon tilts and tightens the view instead of cutting to a new
 * camera. The optic's magnification feeds the ADS field of view, which is what
 * makes a 4x ACOG look different from a 1.5x red dot.
 */
enum class CameraMode(val displayName: String, val fovDegrees: Float, val shoulderOffsetMeters: Float) {
    IDLE("Idle", 70f, 0.35f),
    COMBAT_SHOULDER("Combat Shoulder", 60f, 0.55f),
    ADS_ZOOM("ADS Zoom", 45f, 0.22f)
}

data class CameraState(
    val mode: CameraMode = CameraMode.IDLE,
    val fovDegrees: Float = CameraMode.IDLE.fovDegrees,
    val shoulderOffsetMeters: Float = CameraMode.IDLE.shoulderOffsetMeters,
    val magnification: Float = 1f,
    val isFullyAimed: Boolean = false
)

object TacticalCamera {

    const val ADS_TRANSITION_SECONDS = 0.18f
    const val COMBAT_TRANSITION_SECONDS = 0.30f
    const val MIN_ADS_FOV_DEGREES = 6f

    /** The rig the current input is asking for. ADS always wins over the shoulder. */
    fun targetMode(aimingDownSights: Boolean, inCombat: Boolean): CameraMode = when {
        aimingDownSights -> CameraMode.ADS_ZOOM
        inCombat -> CameraMode.COMBAT_SHOULDER
        else -> CameraMode.IDLE
    }

    /**
     * Field of view while aimed, scaled by optic magnification. A 12x variable
     * scope bottoms out at [MIN_ADS_FOV_DEGREES] rather than going to zero.
     */
    fun adsFovDegrees(magnification: Float): Float =
        (CameraMode.ADS_ZOOM.fovDegrees / magnification.coerceAtLeast(1f)).coerceAtLeast(MIN_ADS_FOV_DEGREES)

    fun targetState(
        aimingDownSights: Boolean,
        inCombat: Boolean,
        magnification: Float = 1f
    ): CameraState {
        val mode = targetMode(aimingDownSights, inCombat)
        val fov = if (mode == CameraMode.ADS_ZOOM) adsFovDegrees(magnification) else mode.fovDegrees
        return CameraState(
            mode = mode,
            fovDegrees = fov,
            shoulderOffsetMeters = mode.shoulderOffsetMeters,
            magnification = magnification,
            isFullyAimed = mode == CameraMode.ADS_ZOOM
        )
    }

    /** Eased one-frame blend toward [target]; the shortest transition gate wins. */
    fun step(state: CameraState, target: CameraState, deltaSeconds: Float): CameraState {
        val dt = deltaSeconds.coerceAtLeast(0f)
        val duration = if (target.mode == CameraMode.ADS_ZOOM || state.mode == CameraMode.ADS_ZOOM) {
            ADS_TRANSITION_SECONDS
        } else {
            COMBAT_TRANSITION_SECONDS
        }
        val alpha = (dt / duration).coerceIn(0f, 1f)
        val fov = state.fovDegrees + (target.fovDegrees - state.fovDegrees) * alpha
        val offset = state.shoulderOffsetMeters +
            (target.shoulderOffsetMeters - state.shoulderOffsetMeters) * alpha
        val settled = kotlin.math.abs(fov - target.fovDegrees) < 0.05f &&
            kotlin.math.abs(offset - target.shoulderOffsetMeters) < 0.005f
        return CameraState(
            mode = if (settled) target.mode else state.mode,
            fovDegrees = fov,
            shoulderOffsetMeters = offset,
            magnification = target.magnification,
            isFullyAimed = target.mode == CameraMode.ADS_ZOOM &&
                kotlin.math.abs(fov - target.fovDegrees) < 0.5f
        )
    }

    /**
     * Depth of field, per Section 6: on while aiming or inspecting a weapon on
     * the workbench, off while running.
     */
    fun depthOfFieldEnabled(state: CameraState, atWorkbench: Boolean = false): Boolean =
        atWorkbench || state.mode == CameraMode.ADS_ZOOM
}
