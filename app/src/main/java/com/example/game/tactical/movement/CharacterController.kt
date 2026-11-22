package com.example.game.tactical.movement

import com.example.game.combat.Vec3
import kotlin.math.sqrt

/**
 * Phase 2 deliverable: the 3D character controller.
 *
 * Movement is stance-driven and stamina-gated, and vaulting is only allowed
 * against a measured obstacle of the right height, so the controller never
 * teleports the character through geometry it has not been told about.
 */
enum class Stance(
    val displayName: String,
    val eyeHeightMeters: Float,
    val speedMultiplier: Float,
    val hitboxHeightMeters: Float
) {
    STANDING("Standing", 1.70f, 1.00f, 1.80f),
    CROUCHING("Crouching", 1.10f, 0.58f, 1.20f),
    PRONE("Prone", 0.35f, 0.24f, 0.45f)
}

data class MoveInput(
    val forward: Float = 0f,
    val strafe: Float = 0f,
    val sprint: Boolean = false,
    val crouch: Boolean = false,
    val prone: Boolean = false,
    val vault: Boolean = false
) {
    val isMoving: Boolean get() = forward != 0f || strafe != 0f
}

enum class MoveDenial(val code: String, val message: String) {
    NONE("", ""),
    VAULT_NO_OBSTACLE("VAULT_NO_OBSTACLE", "No vaultable surface within reach"),
    VAULT_TOO_LOW("VAULT_TOO_LOW", "Surface is too low to vault"),
    VAULT_TOO_HIGH("VAULT_TOO_HIGH", "Surface is too high to vault"),
    EXHAUSTED("EXHAUSTED", "Not enough stamina to sprint or vault")
}

data class LocomotionState(
    val stance: Stance = Stance.STANDING,
    val position: Vec3 = Vec3.Zero,
    val facing: Vec3 = Vec3(0f, 0f, -1f),
    val speedMetersPerSecond: Float = 0f,
    val stamina: Float = 100f,
    val isSprinting: Boolean = false,
    val vaultRemainingSeconds: Float = 0f
) {
    val isVaulting: Boolean get() = vaultRemainingSeconds > 0f
    /** Movement noise in metres an enemy can hear; sprinting is loudest. */
    val noiseRadiusMeters: Float
        get() = when {
            speedMetersPerSecond <= 0.05f -> 0f
            stance == Stance.PRONE -> 2f
            stance == Stance.CROUCHING -> 6f
            isSprinting -> 30f
            else -> 14f
        }
}

data class StepResult(val state: LocomotionState, val denial: MoveDenial)

object CharacterController {

    const val JOG_SPEED_METERS_PER_SECOND = 4.5f
    const val SPRINT_SPEED_METERS_PER_SECOND = 7.0f
    const val SPRINT_STAMINA_PER_SECOND = 18f
    const val STAMINA_REGEN_PER_SECOND = 12f
    const val MIN_SPRINT_STAMINA = 10f
    const val VAULT_STAMINA_COST = 15f
    const val VAULT_DURATION_SECONDS = 0.65f
    const val VAULT_MIN_HEIGHT_METERS = 0.6f
    const val VAULT_MAX_HEIGHT_METERS = 1.4f
    const val ACCELERATION_METERS_PER_SECOND_SQUARED = 14f
    const val MAX_STAMINA = 100f

    fun stanceFor(input: MoveInput): Stance = when {
        input.prone -> Stance.PRONE
        input.crouch -> Stance.CROUCHING
        else -> Stance.STANDING
    }

    fun canSprint(state: LocomotionState, input: MoveInput): Boolean =
        input.sprint &&
            input.isMoving &&
            state.stamina > MIN_SPRINT_STAMINA &&
            state.stance == Stance.STANDING &&
            !state.isVaulting

    /**
     * Advances one simulation frame. A refusal (bad vault, no stamina) is
     * reported alongside the state instead of cancelling movement outright.
     */
    fun step(
        state: LocomotionState,
        input: MoveInput,
        deltaSeconds: Float,
        obstacleAheadMeters: Float? = null
    ): StepResult {
        val dt = deltaSeconds.coerceAtLeast(0f)
        var denial = MoveDenial.NONE
        var stamina = state.stamina
        var vaultRemaining = (state.vaultRemainingSeconds - dt).coerceAtLeast(0f)

        if (input.vault && !state.isVaulting) {
            denial = when {
                obstacleAheadMeters == null -> MoveDenial.VAULT_NO_OBSTACLE
                obstacleAheadMeters < VAULT_MIN_HEIGHT_METERS -> MoveDenial.VAULT_TOO_LOW
                obstacleAheadMeters > VAULT_MAX_HEIGHT_METERS -> MoveDenial.VAULT_TOO_HIGH
                stamina < VAULT_STAMINA_COST -> MoveDenial.EXHAUSTED
                else -> {
                    stamina -= VAULT_STAMINA_COST
                    vaultRemaining = VAULT_DURATION_SECONDS
                    MoveDenial.NONE
                }
            }
        }

        val stance = stanceFor(input)
        val wantsSprint = canSprint(state.copy(stamina = stamina), input)

        val desiredSpeed = when {
            vaultRemaining > 0f -> JOG_SPEED_METERS_PER_SECOND * 1.35f
            !input.isMoving -> 0f
            wantsSprint -> SPRINT_SPEED_METERS_PER_SECOND
            else -> JOG_SPEED_METERS_PER_SECOND * stance.speedMultiplier
        }
        val speed = approach(state.speedMetersPerSecond, desiredSpeed, ACCELERATION_METERS_PER_SECOND_SQUARED * dt)

        stamina = if (wantsSprint && speed > 0.1f) {
            (stamina - SPRINT_STAMINA_PER_SECOND * dt).coerceIn(0f, MAX_STAMINA)
        } else {
            (stamina + STAMINA_REGEN_PER_SECOND * dt).coerceIn(0f, MAX_STAMINA)
        }

        return StepResult(
            state = state.copy(
                stance = stance,
                position = state.position + heading(input, state.facing) * (speed * dt),
                speedMetersPerSecond = speed,
                stamina = stamina,
                isSprinting = wantsSprint && speed > 0.1f,
                vaultRemainingSeconds = vaultRemaining
            ),
            denial = denial
        )
    }

    /** Unit heading in world space; the character faces the direction it travels. */
    fun heading(input: MoveInput, facing: Vec3): Vec3 {
        val length = sqrt(input.forward * input.forward + input.strafe * input.strafe)
        if (length <= 0.0001f) return Vec3.Zero
        val normalizedForward = Vec3(facing.x, 0f, facing.z).normalized()
        val right = Vec3(-normalizedForward.z, 0f, normalizedForward.x)
        return (normalizedForward * input.forward + right * input.strafe).normalized()
    }

    private fun approach(current: Float, target: Float, maxDelta: Float): Float = when {
        target > current -> (current + maxDelta).coerceAtMost(target)
        target < current -> (current - maxDelta).coerceAtLeast(target)
        else -> current
    }
}
