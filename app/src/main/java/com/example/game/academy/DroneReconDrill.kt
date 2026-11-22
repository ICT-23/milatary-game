package com.example.game.academy

import com.example.game.combat.MeritRules
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Drill 4: drone reconnaissance and artillery support.
 *
 * A ruggedized tablet shows an overhead FLIR feed (white-hot / black-hot) with a
 * 6x4 grid. Altitude and sensor mode decide whether the convoy can be identified.
 * Designate a grid cell, pick a munition, and lead the moving convoy: the 81 mm
 * mortar flies for 4 seconds, the guided munition for 5.6 seconds.
 */
class DroneReconDrill(
    private val random: Random = Random(9)
) : PlayableDrill {

    override val drill: DrillId = DrillId.DRONE

    enum class ThermalMode(val label: String) {
        WHITE_HOT("WHITE-HOT"),
        BLACK_HOT("BLACK-HOT")
    }

    enum class Munition(val label: String, val flightSeconds: Float, val radiusCells: Int) {
        MORTAR_81("81 mm MORTAR", 4f, 0),
        GUIDED("GUIDED MUNITION", 5.6f, 0)
    }

    val gridColumns = 6
    val gridRows = 4

    var thermalMode: ThermalMode = ThermalMode.WHITE_HOT
        private set
    var altitudeMeters: Int = 220
        private set
    var sensorToggled: Boolean = false
        private set

    var strikesRemaining: Int = 3
        private set
    var hits: Int = 0
        private set
    var strikesLaunched: Int = 0
        private set
    var totalScore: Int = 0
        private set
    var infractions: Int = 0
        private set

    var designatedColumn: Int = -1
        private set
    var designatedRow: Int = -1
        private set
    var selectedMunition: Munition = Munition.MORTAR_81
        private set

    var elapsedSeconds: Float = 0f
        private set
    var convoyColumn: Float = 0f
        private set
    var flightRemainingSeconds: Float = -1f
        private set

    private var impactColumn = -1
    private var impactRow = -1
    private var finished = false
    private var resultCache: DrillResult? = null
    private var statusLine = "Toggle FLIR, set altitude, then designate the convoy."
    private val alerts = mutableListOf<String>()

    override val isFinished: Boolean get() = finished

    companion object {
        /** A slow crawl so a 4 s mortar flight still lands inside the convoy's cell. */
        const val CONVOY_CELLS_PER_SECOND = 0.06f
    }

    val convoyRow: Int get() = 1 + (convoyColumn.toInt() % 2).coerceIn(0, gridRows - 1)
    val isIdentified: Boolean get() = sensorToggled && altitudeMeters in 150..350
    val timeToImpactSeconds: Float get() = flightRemainingSeconds.coerceAtLeast(0f)

    init {
        DrillSupport.pushAlert(alerts, "Convoy moving east across the valley. Three munitions authorised.")
    }

    override fun tick(dt: Float) {
        if (finished) return
        val step = dt.coerceIn(0f, 0.25f)
        elapsedSeconds += step
        convoyColumn = (convoyColumn + CONVOY_CELLS_PER_SECOND * step).coerceAtMost(gridColumns - 0.01f)

        if (flightRemainingSeconds > 0f) {
            flightRemainingSeconds -= step
            if (flightRemainingSeconds <= 0f) resolveImpact()
        }

        if (convoyColumn >= gridColumns - 1.01f) {
            statusLine = "Convoy left the valley."
            finish()
        }
        if (elapsedSeconds >= drill.timeLimitSeconds) finish()
    }

    override fun handleAction(actionId: String) {
        if (finished) return
        when (actionId) {
            "THERMAL" -> {
                thermalMode = if (thermalMode == ThermalMode.WHITE_HOT) ThermalMode.BLACK_HOT else ThermalMode.WHITE_HOT
                sensorToggled = true
                statusLine = "FLIR set to ${thermalMode.label}."
            }
            "ALT_UP" -> {
                altitudeMeters = (altitudeMeters + 20).coerceAtMost(400)
                statusLine = "Altitude ${altitudeMeters} m."
            }
            "ALT_DOWN" -> {
                altitudeMeters = (altitudeMeters - 20).coerceAtLeast(80)
                statusLine = "Altitude ${altitudeMeters} m."
            }
            "MORTAR" -> {
                selectedMunition = Munition.MORTAR_81
                statusLine = "81 mm mortar selected. 4 s time of flight."
            }
            "GUIDED" -> {
                selectedMunition = Munition.GUIDED
                statusLine = "Guided munition selected. 5.6 s time of flight."
            }
            "STRIKE" -> launchStrike()
        }
    }

    override fun handlePoint(x: Float, y: Float) {
        if (finished || flightRemainingSeconds > 0f) return
        val column = (x * gridColumns).toInt().coerceIn(0, gridColumns - 1)
        val row = (y * gridRows).toInt().coerceIn(0, gridRows - 1)
        designatedColumn = column
        designatedRow = row
        statusLine = "GRID ${row + 1}-${column + 1} designated. Confirm STRIKE."
        DrillSupport.pushAlert(alerts, statusLine)
    }

    private fun launchStrike() {
        if (strikesRemaining <= 0) {
            statusLine = "No munitions remaining."
            return
        }
        if (flightRemainingSeconds > 0f) {
            statusLine = "Munition already in flight."
            return
        }
        if (designatedColumn < 0 || designatedRow < 0) {
            statusLine = "Designate a grid cell first."
            return
        }

        var impactCol = designatedColumn
        var impactRow = designatedRow
        if (!isIdentified) {
            // No valid FLIR solution: the coordinates drift off the intended cell.
            impactCol = (impactCol + if (random.nextBoolean()) 1 else -1).coerceIn(0, gridColumns - 1)
            impactRow = (impactRow + if (random.nextBoolean()) 1 else -1).coerceIn(0, gridRows - 1)
            DrillSupport.pushAlert(alerts, "No firm identification - coordinates degraded.")
        }

        impactColumn = impactCol
        impactRow = impactRow
        strikesRemaining -= 1
        strikesLaunched += 1
        flightRemainingSeconds = selectedMunition.flightSeconds
        statusLine = "${selectedMunition.label} away. Impact in ${selectedMunition.flightSeconds.toInt()} s."
    }

    private fun resolveImpact() {
        flightRemainingSeconds = -1f
        val convoyCellColumn = convoyColumn.roundToInt().coerceIn(0, gridColumns - 1)
        val convoyCellRow = convoyRow
        val onTarget = impactColumn == convoyCellColumn && impactRow == convoyCellRow

        if (onTarget) {
            hits += 1
            totalScore += 300
            statusLine = "DIRECT HIT on the convoy +300"
        } else {
            totalScore = (totalScore - 25).coerceAtLeast(0)
            statusLine = "Impact at grid ${impactRow + 1}-${impactColumn + 1} - convoy at ${convoyCellRow + 1}-${convoyCellColumn + 1}. MISS."
        }
        DrillSupport.pushAlert(alerts, statusLine)

        if (strikesRemaining <= 0) finish()
    }

    private fun finish() {
        if (finished) return
        finished = true
        val accuracy = DrillSupport.accuracy(hits, strikesLaunched)
        val timeRatio = DrillSupport.timeRatio(elapsedSeconds, drill.timeLimitSeconds)
        val passed = hits >= 2
        val notes = mutableListOf(
            "Munitions expended: $strikesLaunched",
            "Direct hits: $hits",
            "FLIR mode: ${thermalMode.label}",
            "Altitude: $altitudeMeters m",
            "Sensor identification: ${if (isIdentified) "FIRM" else "DEGRADED"}"
        )
        resultCache = DrillResult(
            drill = drill,
            passed = passed,
            score = totalScore,
            meritAwarded = MeritRules.drillMerit(drill.maxMerit, accuracy, timeRatio, infractions),
            accuracyPercent = accuracy,
            durationSeconds = elapsedSeconds,
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
            durationSeconds = elapsedSeconds,
            infractions = infractions,
            notes = emptyList()
        )
    }

    override fun snapshot(): DrillSnapshot {
        val markers = mutableListOf<DrillMarker>()
        for (row in 0 until gridRows) {
            for (column in 0 until gridColumns) {
                val isConvoy = column == convoyColumn.roundToInt() && row == convoyRow
                val isDesignated = column == designatedColumn && row == designatedRow
                val isImpact = column == impactColumn && row == impactRow
                markers.add(
                    DrillMarker(
                        id = "CELL_${row}_$column",
                        x = (column + 0.5f) / gridColumns,
                        y = (row + 0.5f) / gridRows,
                        label = if (isConvoy) "CONVOY" else "${row + 1}-${column + 1}",
                        state = when {
                            isImpact -> DrillMarkerState.IMPACT
                            isConvoy -> DrillMarkerState.CONVOY
                            isDesignated -> DrillMarkerState.ACTIVE
                            else -> DrillMarkerState.TARGET
                        },
                        sublabel = when {
                            isConvoy && !isIdentified -> "SENSOR UNRESOLVED"
                            isConvoy -> "TRACK LOCK"
                            else -> ""
                        }
                    )
                )
            }
        }

        return DrillSnapshot(
            drill = drill,
            statusLine = statusLine,
            timeRemainingSeconds = DrillSupport.remaining(drill.timeLimitSeconds, elapsedSeconds),
            progress = DrillSupport.ratioOf((3 - strikesRemaining).toFloat(), 3f),
            stamina = 100f,
            markers = markers,
            metrics = listOf(
                DrillMetric("SCORE", totalScore.toString()),
                DrillMetric("STRIKES", "$strikesRemaining left"),
                DrillMetric("HITS", "$hits"),
                DrillMetric("FLIR", thermalMode.label),
                DrillMetric("ALT", "$altitudeMeters m"),
                DrillMetric("MUNITION", if (selectedMunition == Munition.MORTAR_81) "81mm" else "GUIDED"),
                DrillMetric("TTI", if (flightRemainingSeconds > 0f) "${"%.1f".format(flightRemainingSeconds)} s" else "--")
            ),
            actions = listOf(
                DrillAction("THERMAL", "FLIR ${thermalMode.label}"),
                DrillAction("ALT_DOWN", "ALT -20"),
                DrillAction("ALT_UP", "ALT +20"),
                DrillAction("MORTAR", "81 mm"),
                DrillAction("GUIDED", "GUIDED"),
                DrillAction("STRIKE", "STRIKE", strikesRemaining > 0 && flightRemainingSeconds <= 0f)
            ),
            alerts = alerts.toList()
        )
    }
}
