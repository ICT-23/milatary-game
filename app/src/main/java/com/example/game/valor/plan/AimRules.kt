package com.example.game.valor.plan

/**
 * Section 5.1.4: the three aim-assist settings. The angle and the nudge are
 * tuning data supplied by the settings asset, never constants here.
 */
enum class AimAssistLevel(val displayName: String) {
    OFF("Off"),
    LOW("Low"),
    HIGH("High")
}

/** Section 5.1.2: the ADS cancel triggers, in the order the document lists them. */
enum class AdsCancel(val displayName: String) {
    RELOAD_START("Reload start"),
    SPRINT("Sprint"),
    WEAPON_SWAP("Weapon swap"),
    DEATH("Death"),
    VAULT("Vault")
}

/** Section 5.1.1: the crosshair tint is feedback only and never damage. */
enum class CrosshairTint(val displayName: String) {
    DEFAULT("Default"),
    ENEMY("Red on enemy hitbox"),
    ALLY("Green on teammate")
}

/** Stance ordering for the sway rule; a lower rank sways less. */
enum class SwayStance(val displayName: String, val rank: Int) {
    PRONE("Prone", 0),
    CROUCHED("Crouched", 1),
    STANDING("Standing", 2)
}

/** Section 5.1.2: the ADS state plus the sprint cancel that starts it. */
data class AdsState(val aiming: Boolean = false, val sprinting: Boolean = false)

/** Section 5.1.4: a sniper's breath hold, limited by stamina. */
data class BreathState(val staminaSeconds: Float, val holding: Boolean = false)

/**
 * Section 5.1: aiming only changes where a shot goes. Nothing in this object
 * can damage, fire, or reach a target that is not visible.
 */
object AimRules {

    const val AIM_ASSIST_DEALS_DAMAGE = false
    const val AIM_ASSIST_AUTO_FIRES = false
    const val AIM_ASSIST_HITS_THROUGH_WALLS = false
    const val AIM_ASSIST_LOCKS_ON_DEAD = false
    const val CROSSHAIR_IS_VISUAL_ONLY = true

    /**
     * "Only nudges the aim direction slightly toward a target already near the
     * crosshair (angle < assistAngle) and only if that target is visible."
     * Every guard is required, which is what makes the behind-a-wall test pass.
     */
    fun canAssist(
        level: AimAssistLevel,
        angleToTargetDegrees: Float,
        assistAngleDegrees: Float,
        targetVisible: Boolean,
        targetAlive: Boolean,
        targetOnScreen: Boolean
    ): Boolean =
        level != AimAssistLevel.OFF &&
            angleToTargetDegrees < assistAngleDegrees &&
            targetVisible &&
            targetAlive &&
            targetOnScreen

    /** "Disabled for snipers at full zoom unless user enables." */
    fun assistsSniper(level: AimAssistLevel, atFullZoom: Boolean, userEnabledForSnipers: Boolean): Boolean =
        level != AimAssistLevel.OFF && (!atFullZoom || userEnabledForSnipers)

    /** The nudge stays inside the caller's budget; assist never turns the player. */
    fun nudgeDegrees(angleToTargetDegrees: Float, maxNudgeDegrees: Float): Float =
        angleToTargetDegrees.coerceIn(0f, maxNudgeDegrees.coerceAtLeast(0f))

    /** "Crosshair turns red when the ray hits an enemy, green on a teammate." */
    fun crosshairTint(hitsEnemy: Boolean, hitsAlly: Boolean): CrosshairTint = when {
        hitsEnemy -> CrosshairTint.ENEMY
        hitsAlly -> CrosshairTint.ALLY
        else -> CrosshairTint.DEFAULT
    }

    /** ADS cancels a sprint the moment it starts. */
    fun beginAds(state: AdsState): AdsState = state.copy(aiming = true, sprinting = false)

    /** "ADS is cancelled by: reload start, sprint, weapon swap, death, vault." */
    fun cancelAds(state: AdsState, reason: AdsCancel): AdsState =
        if (reason in AdsCancel.entries) state.copy(aiming = false) else state

    /** "Spread multiplier drops to adsSpreadMult (e.g. 0.4)". */
    fun spreadMultiplier(state: AdsState, adsSpreadMult: Float): Float =
        if (state.aiming) adsSpreadMult else 1f

    /** "Move speed multiplied by adsMoveMult." */
    fun moveMultiplier(state: AdsState, adsMoveMult: Float): Float =
        if (state.aiming) adsMoveMult else 1f

    /** "FOV lerps to the weapon's adsFov over adsTime." */
    fun adsProgress(elapsedSeconds: Float, adsTimeSeconds: Float): Float =
        if (adsTimeSeconds <= 0f) 1f else (elapsedSeconds / adsTimeSeconds).coerceIn(0f, 1f)

    fun fov(hipFov: Float, adsFov: Float, progress: Float): Float =
        hipFov + (adsFov - hipFov) * progress.coerceIn(0f, 1f)

    /** "Small idle sway, reduced crouched/prone, increased when moving." */
    fun swayRank(stance: SwayStance, moving: Boolean): Int = stance.rank + if (moving) 3 else 0

    fun swaysLess(a: Int, b: Int): Boolean = a < b

    /** Breath hold drains stamina only while held and only above zero. */
    fun holdBreath(state: BreathState, deltaSeconds: Float): BreathState {
        if (state.staminaSeconds <= 0f) return state.copy(holding = false)
        return BreathState((state.staminaSeconds - deltaSeconds).coerceAtLeast(0f), holding = true)
    }

    fun releaseBreath(state: BreathState): BreathState = state.copy(holding = false)

    fun regainBreath(state: BreathState, deltaSeconds: Float, maxSeconds: Float): BreathState =
        state.copy(staminaSeconds = (state.staminaSeconds + deltaSeconds).coerceAtMost(maxSeconds))
}
