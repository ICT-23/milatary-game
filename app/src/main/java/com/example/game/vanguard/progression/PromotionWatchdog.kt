package com.example.game.vanguard.progression

import com.example.game.vanguard.core.EventBus
import com.example.game.vanguard.core.PromotionAcknowledged
import com.example.game.vanguard.core.subscribeTo

/**
 * Section 3.3 item 3: the promotion modal watchdog.
 *
 * The legacy build froze because the Continue button waited for an animation
 * callback that never arrived. This class owns unscaled time and unlocks the
 * button at 3.0 s whether or not the ceremony event ever fires.
 */
class PromotionWatchdog(
    private val timeoutSeconds: Float = DEFAULT_TIMEOUT_SECONDS
) {
    private var elapsedSeconds = 0f
    private var acknowledged = false
    private var started = false
    private var slot: EventBus.Subscription? = null

    /** True once the button is visible and interactable. */
    val continueEnabled: Boolean get() = started && (acknowledged || elapsedSeconds >= timeoutSeconds)

    /** True when the watchdog, not the animation, released the button. */
    val forced: Boolean get() = continueEnabled && !acknowledged

    val elapsed: Float get() = elapsedSeconds

    /** Idempotent: restarting a ceremony does not reset the elapsed time to zero. */
    fun start() {
        if (started) return
        started = true
        elapsedSeconds = 0f
    }

    /** Attaches to the acknowledgement event so a working ceremony is still honoured. */
    fun attach(bus: EventBus): PromotionWatchdog {
        slot?.unsubscribe()
        slot = bus.subscribeTo<PromotionAcknowledged> { onAcknowledged() }
        return this
    }

    fun detach() {
        slot?.unsubscribe()
        slot = null
    }

    fun onAcknowledged() {
        acknowledged = true
    }

    /** Called with unscaled delta time; returns the new button state. */
    fun update(unscaledDeltaSeconds: Float): Boolean {
        if (!started) return false
        if (unscaledDeltaSeconds > 0f) {
            elapsedSeconds += unscaledDeltaSeconds
        }
        return continueEnabled
    }

    fun reset() {
        elapsedSeconds = 0f
        acknowledged = false
        started = false
        detach()
    }

    companion object {
        /** Exactly the 3.0 s the specification names. */
        const val DEFAULT_TIMEOUT_SECONDS = 3.0f
    }
}
