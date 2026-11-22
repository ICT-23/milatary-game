package com.example.game.vanguard.combat

/**
 * Section 3.1 gate 5, first half: three handgun/carbine stances and three
 * postures. The numbers are stability multipliers, not damage bonuses, because
 * gunfights are decided by placement and recovery rather than by stance DPS.
 */
enum class Stance(
    val displayName: String,
    /** Fraction of the operator's frontal profile the stance presents. */
    val silhouette: Float,
    /** Multiplier applied to the weapon's own dispersion. */
    val stability: Float,
    /** Multiplier on how fast the sight picture settles after recoil. */
    val verticalRecovery: Float,
    val mountsFlashlight: Boolean
) {
    ISOSCELES("Isosceles", silhouette = 1.0f, stability = 1.0f, verticalRecovery = 1.0f, mountsFlashlight = false),
    WEAVER("Weaver", silhouette = 0.85f, stability = 1.05f, verticalRecovery = 1.35f, mountsFlashlight = false),
    HARRIES("Harries", silhouette = 0.95f, stability = 0.9f, verticalRecovery = 1.1f, mountsFlashlight = true)
}

enum class Posture(
    val displayName: String,
    val recoilMultiplier: Float,
    val stability: Float,
    val movementMultiplier: Float
) {
    STANDING("Standing", recoilMultiplier = 1.0f, stability = 1.0f, movementMultiplier = 1.0f),
    CROUCHED("Crouched", recoilMultiplier = 0.75f, stability = 1.15f, movementMultiplier = 0.6f),
    PRONE("Prone", recoilMultiplier = 0.55f, stability = 1.4f, movementMultiplier = 0.25f)
}

/**
 * Spring state for one weapon. `cumulativeImpulse` only ever grows while shots
 * are being fired, so holding the trigger can never produce a free shot.
 */
data class RecoilState(
    val offsetDegrees: Float = 0f,
    val velocityDegreesPerSecond: Float = 0f,
    val cumulativeImpulse: Float = 0f,
    val consecutiveShots: Int = 0
) {
    val isSettled: Boolean
        get() = kotlin.math.abs(offsetDegrees) < 0.01f &&
            kotlin.math.abs(velocityDegreesPerSecond) < 0.01f
}

object RecoilModel {

    const val STIFFNESS = 42f
    const val DAMPING = 11f
    private const val IMPULSE_TO_DEGREES = 0.45f
    private const val IMPULSE_TO_MILLIMETRES = 3.2f

    /**
     * One shot's impulse. Kick-off hydraulics remove up to 70% of it, stance and
     * posture scale the remainder, and the result is added - never averaged - so
     * a fast trigger cannot outrun the spring.
     */
    fun impulsePerShot(
        baseImpulse: Float,
        stance: Stance,
        posture: Posture,
        kickOffReduction: Float
    ): Float {
        val spring = baseImpulse.coerceAtLeast(0f) *
            stance.stability *
            posture.recoilMultiplier
        return spring * (1f - kickOffReduction.coerceIn(0f, 0.70f))
    }

    fun weaponKickMillimetres(impulse: Float): Float = impulse * IMPULSE_TO_MILLIMETRES

    fun apply(state: RecoilState, impulse: Float): RecoilState = state.copy(
        offsetDegrees = state.offsetDegrees + impulse * IMPULSE_TO_DEGREES,
        velocityDegreesPerSecond = state.velocityDegreesPerSecond + impulse * 4.5f,
        cumulativeImpulse = state.cumulativeImpulse + impulse,
        consecutiveShots = state.consecutiveShots + 1
    )

    /** Critically damped spring step; recovery is asymptotic, never instant. */
    fun step(state: RecoilState, deltaSeconds: Float): RecoilState {
        val dt = deltaSeconds.coerceIn(0f, 0.05f)
        if (dt == 0f) return state
        val acceleration = -STIFFNESS * state.offsetDegrees - DAMPING * state.velocityDegreesPerSecond
        val velocity = state.velocityDegreesPerSecond + acceleration * dt
        val offset = state.offsetDegrees + velocity * dt
        return state.copy(offsetDegrees = offset, velocityDegreesPerSecond = velocity)
    }

    /** Time since the last shot, expressed as the spread the operator is carrying. */
    fun swayDegrees(state: RecoilState, stance: Stance): Float {
        val carried = kotlin.math.abs(state.offsetDegrees) / stance.verticalRecovery.coerceAtLeast(0.1f)
        return carried.coerceAtMost(6f)
    }

    /**
     * Harries: the off-hand flashlight removes the darkness penalty inside the
     * beam cone and does nothing outside it.
     */
    fun darknessPenaltyDegrees(stance: Stance, isDark: Boolean, insideBeamCone: Boolean): Float {
        if (!isDark) return 0f
        return if (stance.mountsFlashlight && insideBeamCone) 0f else 2.5f
    }
}
