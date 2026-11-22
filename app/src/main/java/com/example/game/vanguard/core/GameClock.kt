package com.example.game.vanguard.core

/**
 * Section 2 Core: GameClock.
 *
 * Two timelines run side by side. Scaled time drives animation and recoil;
 * unscaled time drives the promotion watchdog, because a frozen or accelerated
 * frame must never be able to strand a modal. Both are pure accumulators, so a
 * replayed input sequence always produces the same reading.
 */
data class ClockReading(
    val scaledSeconds: Float,
    val unscaledSeconds: Float,
    val isPaused: Boolean,
    val timeScale: Float
)

class GameClock(timeScale: Float = 1f) {

    private val scale = timeScale.coerceIn(0f, 4f)

    private var scaled = 0f
    private var unscaled = 0f
    private var paused = false

    val scaledSeconds: Float get() = scaled

    val unscaledSeconds: Float get() = unscaled

    val isPaused: Boolean get() = paused

    val reading: ClockReading get() = ClockReading(scaled, unscaled, paused, scale)

    /**
     * Advances both timelines. Unscaled time always moves; scaled time freezes
     * while paused, which is what a pause menu expects.
     */
    fun advance(unscaledDeltaSeconds: Float): ClockReading {
        if (unscaledDeltaSeconds <= 0f) return reading
        unscaled += unscaledDeltaSeconds
        if (!paused) {
            scaled += unscaledDeltaSeconds * scale
        }
        return reading
    }

    fun pause() {
        paused = true
    }

    fun resume() {
        paused = false
    }

    fun reset() {
        scaled = 0f
        unscaled = 0f
        paused = false
    }

    companion object {
        /** Section 3.3: the watchdog counts this value, never scaled time. */
        const val UNSCALED_WATCHDOG_SOURCE = "GameClock.unscaledSeconds"
    }
}

/**
 * Section 1.2 step 4 / Section 2 Core: the service locator.
 *
 * Registration is one-way per type and freezes once the app is running, so a
 * later feature cannot swap out a locked service by re-registering it.
 */
class DependencyContainer {

    private val factories = LinkedHashMap<Class<*>, () -> Any>()
    private val instances = LinkedHashMap<Class<*>, Any>()
    private var frozen = false

    val isFrozen: Boolean get() = frozen

    val registeredCount: Int get() = factories.size

    /** False when the container is frozen or the type is already registered. */
    fun <T : Any> register(type: Class<T>, factory: () -> T): Boolean {
        if (frozen || factories.containsKey(type)) return false
        factories[type] = factory
        return true
    }

    /** Resolves once, then returns the same instance for the process lifetime. */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> resolve(type: Class<T>): T? {
        val existing = instances[type]
        if (existing != null) return existing as T
        val factory = factories[type] ?: return null
        val created = factory()
        instances[type] = created
        return created
    }

    fun isRegistered(type: Class<*>): Boolean = factories.containsKey(type)

    fun freeze() {
        frozen = true
    }

    fun clear() {
        factories.clear()
        instances.clear()
        frozen = false
    }
}

inline fun <reified T : Any> DependencyContainer.register(noinline factory: () -> T): Boolean =
    register(T::class.java, factory)

inline fun <reified T : Any> DependencyContainer.resolve(): T? = resolve(T::class.java)
