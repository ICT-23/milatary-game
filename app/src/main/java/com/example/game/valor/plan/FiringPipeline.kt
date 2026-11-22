package com.example.game.valor.plan

/** Section 5.2 check 6: what a trigger press is allowed to do. */
enum class FireMode(val displayName: String) {
    SINGLE("Single"),
    BURST("Burst"),
    AUTO("Auto")
}

/** Section 5.2: the eight checks, and the one that rejected the press. */
enum class FireRejection(val reason: String) {
    NOT_ALIVE("Owner is not alive"),
    SWAPPING("Weapon is being swapped"),
    RELOADING("Weapon is reloading"),
    MOVEMENT_LOCK("Sprinting, vaulting, climbing, cutscene or paused"),
    COOLDOWN("Fire-rate cooldown not finished"),
    FIRE_MODE("Fire mode does not allow the press"),
    EMPTY_MAGAZINE("Magazine is empty"),
    JAMMED("Weapon is jammed")
}

/**
 * Section 5.2: everything the pipeline needs to answer "can I shoot?".
 * Defaults describe a ready weapon so a test only states the one fact it varies.
 */
data class FireContext(
    val ownerAlive: Boolean = true,
    val weaponEquipped: Boolean = true,
    val swapRemainingSeconds: Float = 0f,
    val reloading: Boolean = false,
    val shellReloadInterruptible: Boolean = false,
    val sprinting: Boolean = false,
    val vaulting: Boolean = false,
    val climbing: Boolean = false,
    val inCutscene: Boolean = false,
    val paused: Boolean = false,
    val nowSeconds: Float = 0f,
    val nextFireTime: Float = 0f,
    val fireMode: FireMode = FireMode.AUTO,
    val freshPress: Boolean = true,
    val midBurst: Boolean = false,
    val held: Boolean = true,
    val magazine: Int = 30,
    val rpm: Float = 600f,
    val jammed: Boolean = false
) {
    val movementLocked: Boolean
        get() = sprinting || vaulting || climbing || inCutscene || paused
}

/** Section 5.2 "On success": the state the shot leaves behind. */
data class FireDecision(
    val allowed: Boolean,
    val rejection: FireRejection? = null,
    val dryClick: Boolean = false,
    val autoReload: Boolean = false,
    val magazineAfter: Int = 0,
    val nextFireTime: Float = 0f,
    val roundsConsumed: Int = 0
)

/** Section 5.2 "paused mid-burst (resume correctly)". */
data class BurstState(val bulletsRemaining: Int = 0)

/** Section 5.2: fire and reload pressed on the same frame. */
enum class InputRace(val winner: String) {
    FIRE("Fire"),
    RELOAD("Reload")
}

/**
 * Section 5.2: a shot happens only when every check passes. A rejection never
 * costs ammo and never deals damage, and it always names its reason.
 */
object FiringPipeline {

    const val DRY_CLICK_ONCE = true
    const val SUPPRESSED_NOISE_MULT = 0.3f

    /** `nextFireTime = now + 60/RPM`. */
    fun nextFireTime(nowSeconds: Float, rpm: Float): Float =
        if (rpm <= 0f) nowSeconds else nowSeconds + 60f / rpm

    fun evaluate(
        context: FireContext,
        autoReloadSetting: Boolean = true,
        log: DebugLog? = null
    ): FireDecision {
        val rejection = firstFailure(context)
        if (rejection != null) {
            log?.record(LogCategory.FIRE, rejection.reason, context.nowSeconds)
            val empty = rejection == FireRejection.EMPTY_MAGAZINE
            return FireDecision(
                allowed = false,
                rejection = rejection,
                dryClick = empty && DRY_CLICK_ONCE,
                autoReload = empty && autoReloadSetting,
                magazineAfter = context.magazine,
                nextFireTime = context.nextFireTime
            )
        }
        return FireDecision(
            allowed = true,
            magazineAfter = (context.magazine - 1).coerceAtLeast(0),
            nextFireTime = nextFireTime(context.nowSeconds, context.rpm),
            roundsConsumed = 1
        )
    }

    /** The checks run in the document's order, so the reported reason is the first blocker. */
    private fun firstFailure(context: FireContext): FireRejection? = when {
        !context.ownerAlive -> FireRejection.NOT_ALIVE
        !context.weaponEquipped || context.swapRemainingSeconds > 0f -> FireRejection.SWAPPING
        context.reloading && !context.shellReloadInterruptible -> FireRejection.RELOADING
        context.movementLocked -> FireRejection.MOVEMENT_LOCK
        context.nowSeconds < context.nextFireTime -> FireRejection.COOLDOWN
        !modeAllows(context) -> FireRejection.FIRE_MODE
        context.magazine <= 0 -> FireRejection.EMPTY_MAGAZINE
        context.jammed -> FireRejection.JAMMED
        else -> null
    }

    /** Single needs a fresh press, Burst must not be mid-burst, Auto fires while held. */
    fun modeAllows(context: FireContext): Boolean = when (context.fireMode) {
        FireMode.SINGLE -> context.freshPress
        FireMode.BURST -> !context.midBurst && (context.freshPress || context.held)
        FireMode.AUTO -> context.held
    }

    /** "Reload wins only if mag is empty; otherwise fire wins." */
    fun sameFrameRace(magazine: Int): InputRace =
        if (magazine <= 0) InputRace.RELOAD else InputRace.FIRE

    /** A dead owner stops a burst instead of finishing it. */
    fun stopBurstOnDeath(state: BurstState): BurstState = state.copy(bulletsRemaining = 0)

    fun pauseBurst(state: BurstState): BurstState = state

    fun resumeBurst(state: BurstState): BurstState = state

    /** "Alert nearby enemies (noise radius; suppressor reduces it)." */
    fun noiseRadius(baseRadiusMeters: Float, noiseMultiplier: Float): Float =
        baseRadiusMeters * noiseMultiplier.coerceIn(0f, 1f)

    /** One shotgun shell always costs exactly one magazine round, whatever its pellet count. */
    fun roundsForShell(): Int = 1
}
