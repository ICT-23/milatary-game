package com.example.game.vanguard.core

/**
 * Section 2 Core / Section 3.3:
 * the decoupled EventBus every presentation layer subscribes to. Nothing here
 * knows about Compose, the database or a 3D scene, so a module can be replaced
 * without touching a publisher.
 *
 * Delivery is synchronous and ordered; handlers added during a publish do not
 * receive the event they were added for.
 */
class EventBus {

    private data class Binding(val type: Class<*>, val handler: (Any) -> Unit)

    private val bindings = mutableListOf<Binding>()

    val subscriberCount: Int get() = bindings.size

    var publishedCount: Int = 0
        private set

    fun <T : Any> subscribe(type: Class<T>, handler: (T) -> Unit): Subscription {
        val binding = Binding(type) { event -> if (type.isInstance(event)) handler(type.cast(event)) }
        bindings += binding
        return Subscription { bindings.remove(binding) }
    }

    /** Publishes to every subscriber whose declared type matches, including supertypes. */
    fun publish(event: Any) {
        publishedCount += 1
        bindings.toList().forEach { binding ->
            if (binding.type.isInstance(event)) binding.handler(event)
        }
    }

    fun clear() {
        bindings.clear()
    }

    /** Handle returned by [subscribe]; calling it twice is a no-op. */
    fun interface Subscription {
        fun unsubscribe()
    }
}

/**
 * A typed publisher kept beside the bus so callers do not repeat the class
 * literal. Both call sites the spec requires (rank changes, promotion
 * acknowledgement) go through this helper.
 */
inline fun <reified T : Any> EventBus.subscribeTo(noinline handler: (T) -> Unit): EventBus.Subscription =
    subscribe(T::class.java, handler)

// ---------------------------------------------------------------- events

/** Section 3.3 item 4: every promotion publishes this exactly once. */
data class RankChanged(val oldRankIndex: Int, val newRankIndex: Int, val rankCode: String, val rankTitle: String)

/** Fired by the ceremony animation; the watchdog covers the case where it never fires. */
data class PromotionAcknowledged(val rankIndex: Int, val acknowledgedAtSeconds: Float)

/** A mission reached a terminal state, so post-mission hooks are now legal. */
data class MissionStateChanged(val missionId: String, val state: String)

/** Section 3.4 item 4: raised when a UI element tries to interrupt an active mission. */
data class SessionFlowBlocked(val attemptedHook: String, val combatState: String)
