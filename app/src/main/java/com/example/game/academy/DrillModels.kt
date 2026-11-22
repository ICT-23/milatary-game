package com.example.game.academy

/** The four academy training modules from the master specification. */
enum class DrillId(
    val title: String,
    val subtitle: String,
    val maxMerit: Int,
    val requiredRankIndex: Int,
    val timeLimitSeconds: Float
) {
    RANGE(
        title = "FIRING RANGE",
        subtitle = "Pop-up steel, wind and turret zeroing",
        maxMerit = 200,
        requiredRankIndex = 0,
        timeLimitSeconds = 180f
    ),
    OBSTACLE(
        title = "OBSTACLE COURSE",
        subtitle = "Wall vault, wire crawl, beam and cargo net",
        maxMerit = 180,
        requiredRankIndex = 1,
        timeLimitSeconds = 150f
    ),
    KILLHOUSE(
        title = "CQB KILLHOUSE",
        subtitle = "Breach, discriminate, clear",
        maxMerit = 260,
        requiredRankIndex = 5,
        timeLimitSeconds = 90f
    ),
    DRONE(
        title = "DRONE & ARTILLERY",
        subtitle = "FLIR reconnaissance and fire support",
        maxMerit = 240,
        requiredRankIndex = 8,
        timeLimitSeconds = 120f
    )
}

enum class DrillMarkerState {
    PLAYER,
    TARGET,
    HOSTILE,
    FRIENDLY,
    STUNNED,
    DOWN,
    ACTIVE,
    SEALED,
    CONVOY,
    IMPACT
}

/** Engine-agnostic marker so one Compose canvas can draw every drill. */
data class DrillMarker(
    val id: String,
    val x: Float,
    val y: Float,
    val label: String,
    val state: DrillMarkerState,
    val sublabel: String = ""
)

data class DrillAction(
    val id: String,
    val label: String,
    val enabled: Boolean = true
)

data class DrillMetric(val label: String, val value: String)

data class DrillSnapshot(
    val drill: DrillId,
    val statusLine: String,
    val timeRemainingSeconds: Float,
    val progress: Float,
    val stamina: Float,
    val markers: List<DrillMarker>,
    val metrics: List<DrillMetric>,
    val actions: List<DrillAction>,
    val alerts: List<String>
)

/** Evaluation output. Merit is already resolved through MeritRules. */
data class DrillResult(
    val drill: DrillId,
    val passed: Boolean,
    val score: Int,
    val meritAwarded: Int,
    val accuracyPercent: Int,
    val durationSeconds: Float,
    val infractions: Int,
    val notes: List<String>
)

/**
 * Every drill exposes the same surface so UI and persistence never branch on type.
 */
interface PlayableDrill {
    val drill: DrillId
    val isFinished: Boolean
    fun tick(dt: Float)
    fun handleAction(actionId: String)
    /** Canvas taps in normalized 0..1 space; unused drills ignore them. */
    fun handlePoint(x: Float, y: Float) {}
    fun snapshot(): DrillSnapshot
    fun result(): DrillResult
}

/**
 * Stamina pool driving both sway and the obstacle course.
 * At 100% stamina sway is +/-0.05 degrees; at 0% it is 600% higher (+/-0.35).
 */
class StaminaSystem(initial: Float = 100f) {

    var stamina: Float = initial.coerceIn(0f, 100f)
        private set

    val stamina01: Float get() = stamina / 100f
    val isExhausted: Boolean get() = stamina < 5f

    fun drain(amount: Float) {
        stamina = (stamina - amount.coerceAtLeast(0f)).coerceIn(0f, 100f)
    }

    fun recover(ratePerSecond: Float, dt: Float) {
        stamina = (stamina + ratePerSecond * dt.coerceAtLeast(0f)).coerceIn(0f, 100f)
    }

    /** Returns false and leaves stamina untouched when the action is unaffordable. */
    fun trySpend(cost: Float): Boolean {
        if (stamina < cost) return false
        drain(cost)
        return true
    }

    fun swayAmplitudeDegrees(): Float = 0.05f + (1f - stamina01) * 0.30f

    fun breathingFrequencyMultiplier(): Float = 1f + (1f - stamina01) * 2f

    fun cameraBobPercent(): Float = 1f + (1f - stamina01) * 2.5f

    fun setTimeout(percent: Float) {
        stamina = (percent.coerceIn(0f, 100f))
    }

    companion object {
        fun clamp(value: Float): Float = value.coerceIn(0f, 100f)
    }
}

/** Shared helpers for the drill implementations. */
internal object DrillSupport {

    fun timeRatio(elapsed: Float, limit: Float): Float =
        if (limit <= 0f) 1f else (elapsed / limit).coerceIn(0f, 1f)

    fun accuracy(hits: Int, attempts: Int): Int =
        if (attempts <= 0) 0 else ((hits.toFloat() / attempts) * 100f).toInt().coerceIn(0, 100)

    fun pushAlert(alerts: MutableList<String>, message: String) {
        alerts.add(0, message)
        while (alerts.size > 4) alerts.removeAt(alerts.size - 1)
    }

    fun remaining(limit: Float, elapsed: Float): Float = (limit - elapsed).coerceAtLeast(0f)

    fun ratioOf(value: Float, max: Float): Float =
        if (max <= 0f) 0f else (value / max).coerceIn(0f, 1f)
}
