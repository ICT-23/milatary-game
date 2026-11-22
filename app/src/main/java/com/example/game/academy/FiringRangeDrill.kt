package com.example.game.academy

import com.example.game.combat.BallisticBody
import com.example.game.combat.BallisticMath
import com.example.game.combat.BallisticsEngine
import com.example.game.combat.HitZone
import com.example.game.combat.MeritRules
import com.example.game.combat.SurfaceType
import com.example.game.combat.Vec3
import com.example.game.combat.WeaponRuntime
import kotlin.math.hypot
import kotlin.random.Random

/**
 * Drill 2: precision marksmanship.
 *
 * Pop-up steel at 50 m to 500 m, variable crosswind, moving rails and turret
 * zeroing. Scoring: bulls-eye 100, center mass 75, peripheral 30, miss -25.
 * Every shot still runs the muzzle-validated ballistic pipeline before scoring.
 */
class FiringRangeDrill(
    private val weapon: WeaponRuntime,
    private val random: Random = Random(11)
) : PlayableDrill {

    override val drill: DrillId = DrillId.RANGE

    private val distances = listOf(50, 100, 200, 300, 400, 500)
    private val movingTargets = setOf(2, 3, 5)
    private val alerts = mutableListOf<String>()

    var elapsedSeconds: Float = 0f
        private set
    var zeroingMeters: Int = 100
        private set
    var windageMils: Float = 0f
        private set
    var elevationMils: Float = 0f
        private set
    val windKnots: Float = random.nextInt(21).toFloat()
    val windFromLeft: Boolean = random.nextBoolean()

    private var targetIndex = 0
    private var shots = 0
    private var hits = 0
    private var bullseyes = 0
    private var centers = 0
    private var peripheral = 0
    private var misses = 0
    private var infractions = 0
    private var finished = false
    private var resultCache: DrillResult? = null
    private var statusLine = "Dial zero and windage, then engage the active lane."

    override val isFinished: Boolean get() = finished

    private val currentDistance: Int get() = distances[targetIndex.coerceIn(0, distances.lastIndex)]

    init {
        DrillSupport.pushAlert(
            alerts,
            "Wind ${windKnots.toInt()} kt from the ${if (windFromLeft) "left" else "right"}. Zero dial at ${zeroingMeters} m."
        )
    }

    override fun tick(dt: Float) {
        if (finished) return
        elapsedSeconds += dt.coerceIn(0f, 0.25f)
        weapon.tick(dt)
        if (elapsedSeconds >= drill.timeLimitSeconds) finish()
    }

    override fun handleAction(actionId: String) {
        if (finished) return
        when (actionId) {
            "ZERO_UP" -> changeZero(100)
            "ZERO_DOWN" -> changeZero(-100)
            "WINDAGE_LEFT" -> windageMils = (windageMils - 1f).coerceIn(-10f, 10f)
            "WINDAGE_RIGHT" -> windageMils = (windageMils + 1f).coerceIn(-10f, 10f)
            "ELEVATION_UP" -> elevationMils = (elevationMils + 1f).coerceIn(-10f, 15f)
            "ELEVATION_DOWN" -> elevationMils = (elevationMils - 1f).coerceIn(-10f, 15f)
            "FIRE" -> fire()
            "RELOAD" -> if (!weapon.beginReload()) {
                statusLine = "Reload not required or no reserve rounds."
            } else {
                statusLine = "Reloading..."
            }
            "CLEAR_MALFUNCTION" -> {
                val prompt = weapon.clearMalfunctionStep()
                if (prompt != null) {
                    DrillSupport.pushAlert(alerts, prompt)
                    statusLine = prompt
                }
            }
        }
    }

    override fun handlePoint(x: Float, y: Float) {
        // The lane strip is drawn on the lower third of the canvas.
        if (y > 0.55f) fire()
    }

    private fun changeZero(delta: Int) {
        zeroingMeters = (zeroingMeters + delta).coerceIn(100, 500)
        DrillSupport.pushAlert(alerts, "Zero set to $zeroingMeters m")
    }

    private fun fire() {
        val distance = currentDistance.toFloat()
        val velocity = weapon.stats.muzzleVelocity.coerceAtLeast(100f)
        val attempt = weapon.tryExecuteShot(
            nowSeconds = elapsedSeconds,
            stamina01 = 1f,
            movement01 = 0f
        )

        if (!attempt.fired) {
            statusLine = attempt.block?.displayName ?: "Trigger refused."
            DrillSupport.pushAlert(alerts, statusLine)
            return
        }
        if (attempt.jammedNow) {
            DrillSupport.pushAlert(alerts, "STOPPAGE: barrel over 85% heat.")
        }

        val zeroDistance = zeroingMeters.toFloat()
        val drop = BallisticMath.gravityDropMeters(distance, velocity) -
            BallisticMath.gravityDropMeters(zeroDistance, velocity)
        val drift = BallisticMath.crosswindDriftMeters(distance, velocity, windKnots) *
            (if (windFromLeft) 1f else -1f)
        val elevationCorrection = BallisticMath.milCorrectionMeters(elevationMils, distance)
        val windCorrection = BallisticMath.milCorrectionMeters(windageMils, distance)
        val railError = if (distances[targetIndex] in movingTargets) {
            (random.nextFloat() - 0.5f) * 0.30f
        } else {
            0f
        }

        val verticalError = drop - elevationCorrection
        val lateralError = drift - windCorrection + railError
        val radialError = hypot(verticalError, lateralError)

        // Physical verification: the round must actually strike the plate.
        val eye = Vec3(0f, 1.6f, 0f)
        val targetCenter = Vec3(0f, 1.4f, -distance)
        val aimPoint = targetCenter + Vec3(lateralError, verticalError, 0f)
        val direction = (aimPoint - eye).normalized()
        val muzzle = eye + Vec3(0.12f, -0.16f, -0.25f)
        val plate = BallisticBody(
            bodyId = "STEEL_$distance",
            center = targetCenter,
            radius = 0.55f,
            zone = HitZone.UPPER_TORSO,
            surface = SurfaceType.STEEL
        )
        val shot = BallisticsEngine.resolve(
            aimOrigin = eye,
            aimDirection = direction,
            muzzleOrigin = muzzle,
            bodies = listOf(plate),
            maxRangeMeters = 900f,
            spreadDegrees = attempt.spreadDegrees,
            random = random
        )

        shots += 1
        if (!shot.canApplyDamage) {
            misses += 1
            scoreDelta(-25)
            statusLine = "MISS at ${distance.toInt()} m - ${shot.rejectReason.name.lowercase().replace('_', ' ')}"
            DrillSupport.pushAlert(alerts, statusLine)
        } else {
            when {
                radialError <= 0.12f -> {
                    bullseyes += 1
                    hits += 1
                    scoreDelta(100)
                    statusLine = "BULLS-EYE at ${distance.toInt()} m  +100"
                }
                radialError <= 0.30f -> {
                    centers += 1
                    hits += 1
                    scoreDelta(75)
                    statusLine = "CENTER MASS at ${distance.toInt()} m  +75"
                }
                radialError <= 0.55f -> {
                    peripheral += 1
                    hits += 1
                    scoreDelta(30)
                    statusLine = "PERIPHERAL at ${distance.toInt()} m  +30"
                }
                else -> {
                    misses += 1
                    scoreDelta(-25)
                    statusLine = "MISS at ${distance.toInt()} m  -25"
                }
            }
            DrillSupport.pushAlert(alerts, statusLine)
        }

        targetIndex += 1
        if (targetIndex >= distances.size) finish()
    }

    private var totalScore = 0
    val score: Int get() = totalScore

    private fun scoreDelta(delta: Int) {
        totalScore = (totalScore + delta).coerceAtLeast(0)
    }

    private fun finish() {
        if (finished) return
        finished = true
        val accuracy = DrillSupport.accuracy(hits, shots)
        val timeRatio = DrillSupport.timeRatio(elapsedSeconds, drill.timeLimitSeconds)
        val merit = MeritRules.drillMerit(drill.maxMerit, accuracy, timeRatio, infractions)
        val speedBonus = if (timeRatio <= 0.6f) 50 else 0
        totalScore += speedBonus
        val notes = mutableListOf(
            "Bulls-eyes: $bullseyes",
            "Center mass: $centers",
            "Peripheral: $peripheral",
            "Misses: $misses",
            "Wind: ${windKnots.toInt()} knots from the ${if (windFromLeft) "left" else "right"}",
            "Final zero: $zeroingMeters m"
        )
        if (speedBonus > 0) notes.add("Speed bonus +$speedBonus")
        resultCache = DrillResult(
            drill = drill,
            passed = totalScore >= 400 && hits >= 4,
            score = totalScore,
            meritAwarded = merit,
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
        val readout = weapon.readout()
        val markers = distances.mapIndexed { index, distance ->
            DrillMarker(
                id = "LANE_$distance",
                x = 0.10f + index * 0.15f,
                y = 0.34f,
                label = "${distance}m",
                state = when {
                    index < targetIndex -> DrillMarkerState.DOWN
                    index == targetIndex -> DrillMarkerState.ACTIVE
                    else -> DrillMarkerState.TARGET
                },
                sublabel = if (distance in movingTargets) "RAIL" else ""
            )
        }

        val actions = mutableListOf<DrillAction>()
        actions.add(DrillAction("FIRE", if (readout.isReloading) "RELOADING" else "FIRE", !readout.isReloading))
        actions.add(DrillAction("RELOAD", "RELOAD"))

        val malfunctionPrompt = readout.malfunctionPrompt
        if (malfunctionPrompt != null) {
            actions.add(DrillAction("CLEAR_MALFUNCTION", malfunctionPrompt))
        }

        actions.add(DrillAction("ZERO_DOWN", "ZERO -100"))
        actions.add(DrillAction("ZERO_UP", "ZERO +100"))
        actions.add(DrillAction("WINDAGE_LEFT", "WIND -1 mil"))
        actions.add(DrillAction("WINDAGE_RIGHT", "WIND +1 mil"))
        actions.add(DrillAction("ELEVATION_DOWN", "ELEV -1 mil"))
        actions.add(DrillAction("ELEVATION_UP", "ELEV +1 mil"))

        return DrillSnapshot(
            drill = drill,
            statusLine = statusLine,
            timeRemainingSeconds = DrillSupport.remaining(drill.timeLimitSeconds, elapsedSeconds),
            progress = DrillSupport.ratioOf(targetIndex.toFloat(), distances.size.toFloat()),
            stamina = 100f,
            markers = markers,
            metrics = listOf(
                DrillMetric("SCORE", totalScore.toString()),
                DrillMetric("AMMO", "${readout.loadedRounds}/${readout.reserveRounds}"),
                DrillMetric("HEAT", "${readout.heatPercent}%"),
                DrillMetric("ZERO", "$zeroingMeters m"),
                DrillMetric("WIND", "${windKnots.toInt()} kt ${if (windFromLeft) "<" else ">"} / ${"%.1f".format(windageMils)} mil"),
                DrillMetric("ELEV", "${"%.1f".format(elevationMils)} mil"),
                DrillMetric("SPREAD", "${"%.2f".format(readout.spreadDegrees)} deg")
            ),
            actions = actions,
            alerts = alerts.toList()
        )
    }
}
