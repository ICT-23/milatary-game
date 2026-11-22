package com.example.game.academy

import com.example.game.combat.MeritRules

/**
 * Drill 1: the physical obstacle course.
 *
 * Sprint, vault, low crawl, balance beam, cargo net and rope traverse each draw
 * from one stamina pool. Wrong technique and exhausted grips cost time and points;
 * a beam slip costs 15 s, a rope fall into the mud pit costs 20 s.
 */
class ObstacleCourseDrill : PlayableDrill {

    override val drill: DrillId = DrillId.OBSTACLE

    private data class Segment(
        val id: String,
        val label: String,
        val action: String,
        val staminaCost: Float,
        val hazard: Boolean = false
    )

    private val segments = listOf(
        Segment("SPRINT_APPROACH", "Sprint approach", "SPRINT", 10f),
        Segment("HIGH_WALL", "High wooden wall", "VAULT", 26f),
        Segment("WIRE_CRAWL", "Barbed-wire mud crawl", "CRAWL", 16f),
        Segment("BALANCE_BEAM", "Balance beam over mud", "BALANCE", 12f, hazard = true),
        Segment("CARGO_NET", "Cargo net climb", "CLIMB", 30f),
        Segment("ROPE_TRAVERSE", "Rope traverse", "ROPE", 34f, hazard = true)
    )

    private val stamina = StaminaSystem()
    private val alerts = mutableListOf<String>()

    var elapsedSeconds: Float = 0f
        private set
    var penaltySeconds: Float = 0f
        private set
    var infractions: Int = 0
        private set
    var totalScore: Int = 1000
        private set

    private var segmentIndex = 0
    private var resting = false
    private var finished = false
    private var resultCache: DrillResult? = null
    private var statusLine = "Sprint the approach, then clear each obstacle."

    override val isFinished: Boolean get() = finished
    private val currentSegment: Segment? get() = segments.getOrNull(segmentIndex)
    val totalElapsedSeconds: Float get() = elapsedSeconds + penaltySeconds

    init {
        DrillSupport.pushAlert(alerts, "Drill Sergeant Stone: move like your life depends on it!")
    }

    override fun tick(dt: Float) {
        if (finished) return
        val step = dt.coerceIn(0f, 0.25f)
        elapsedSeconds += step
        stamina.recover(if (resting) 20f else 5f, step)
        if (totalElapsedSeconds >= drill.timeLimitSeconds) finish()
    }

    override fun handleAction(actionId: String) {
        if (finished) return
        if (actionId == "REST") {
            resting = true
            statusLine = "Recovering. Stamina ${stamina.stamina.toInt()}%."
            return
        }
        resting = false

        val segment = currentSegment ?: return

        if (actionId != segment.action) {
            infractions += 1
            penaltySeconds += 3f
            totalScore = (totalScore - 10).coerceAtLeast(0)
            statusLine = "WRONG TECHNIQUE at ${segment.label}: +3 s, -10 pts."
            DrillSupport.pushAlert(alerts, statusLine)
            return
        }

        if (!stamina.trySpend(segment.staminaCost)) {
            statusLine = "Insufficient stamina for ${segment.label}. Rest first."
            DrillSupport.pushAlert(alerts, statusLine)
            return
        }

        // Exhausted grip on a hazard sends the recruit into the mud.
        if (segment.hazard && stamina.stamina01 < 0.15f) {
            val penalty = if (segment.action == "BALANCE") 15f else 20f
            penaltySeconds += penalty
            infractions += 1
            totalScore = (totalScore - 50).coerceAtLeast(0)
            statusLine = if (segment.action == "BALANCE") {
                "SLIPPED off the beam into the mud: +15 s."
            } else {
                "GRIP FAILED on the rope: mud pit, +20 s."
            }
            DrillSupport.pushAlert(alerts, statusLine)
        } else {
            statusLine = "Cleared ${segment.label}. Stamina ${stamina.stamina.toInt()}%."
        }

        segmentIndex += 1
        if (segmentIndex >= segments.size) finish()
    }

    private fun finish() {
        if (finished) return
        finished = true
        val timeRatio = DrillSupport.timeRatio(totalElapsedSeconds, drill.timeLimitSeconds)
        val speedBonus = ((drill.timeLimitSeconds - totalElapsedSeconds).coerceAtLeast(0f) * 2f).toInt()
        totalScore = (totalScore + speedBonus).coerceAtLeast(0)
        val passed = segmentIndex >= segments.size && totalElapsedSeconds <= drill.timeLimitSeconds
        val notes = mutableListOf(
            "Obstacles cleared: $segmentIndex of ${segments.size}",
            "Field time: ${"%.1f".format(totalElapsedSeconds)} s (${penaltySeconds.toInt()} s penalties)",
            "Stamina remaining: ${stamina.stamina.toInt()}%",
            "Technique infractions: $infractions"
        )
        if (speedBonus > 0) notes.add("Speed bonus +$speedBonus")
        resultCache = DrillResult(
            drill = drill,
            passed = passed,
            score = totalScore,
            meritAwarded = MeritRules.drillMerit(drill.maxMerit, if (passed) 100 else 40, timeRatio, infractions),
            accuracyPercent = if (passed) 100 else 40,
            durationSeconds = totalElapsedSeconds,
            infractions = infractions,
            notes = notes
        )
    }

    override fun result(): DrillResult {
        if (!finished) finish()
        return resultCache ?: DrillResult(
            drill = drill,
            passed = false,
            score = totalScore,
            meritAwarded = 0,
            accuracyPercent = 0,
            durationSeconds = totalElapsedSeconds,
            infractions = infractions,
            notes = emptyList()
        )
    }

    override fun snapshot(): DrillSnapshot {
        val markers = mutableListOf<DrillMarker>()
        segments.forEachIndexed { index, segment ->
            markers.add(
                DrillMarker(
                    id = segment.id,
                    x = 0.10f + index * 0.15f,
                    y = 0.62f,
                    label = segment.action,
                    state = when {
                        index < segmentIndex -> DrillMarkerState.DOWN
                        index == segmentIndex -> DrillMarkerState.ACTIVE
                        else -> DrillMarkerState.TARGET
                    },
                    sublabel = segment.label
                )
            )
        }
        val playerX = 0.10f + (segmentIndex.coerceAtMost(segments.lastIndex)) * 0.15f
        markers.add(DrillMarker("RECRUIT", playerX, 0.82f, "YOU", DrillMarkerState.PLAYER))

        return DrillSnapshot(
            drill = drill,
            statusLine = statusLine,
            timeRemainingSeconds = DrillSupport.remaining(drill.timeLimitSeconds, totalElapsedSeconds),
            progress = DrillSupport.ratioOf(segmentIndex.toFloat(), segments.size.toFloat()),
            stamina = stamina.stamina,
            markers = markers,
            metrics = listOf(
                DrillMetric("SCORE", totalScore.toString()),
                DrillMetric("STAMINA", "${stamina.stamina.toInt()}%"),
                DrillMetric("PENALTY", "${penaltySeconds.toInt()} s"),
                DrillMetric("SWAY", "${"%.2f".format(stamina.swayAmplitudeDegrees())} deg"),
                DrillMetric("BREATH", "${"%.1f".format(stamina.breathingFrequencyMultiplier())}x")
            ),
            actions = listOf(
                DrillAction("SPRINT", "SPRINT"),
                DrillAction("VAULT", "VAULT"),
                DrillAction("CRAWL", "LOW CRAWL"),
                DrillAction("BALANCE", "BEAM"),
                DrillAction("CLIMB", "CLIMB"),
                DrillAction("ROPE", "ROPE"),
                DrillAction("REST", if (resting) "RESTING..." else "REST")
            ),
            alerts = alerts.toList()
        )
    }
}
