package com.example.game.academy

import com.example.game.combat.ArmorProfile
import com.example.game.combat.BallisticsEngine
import com.example.game.combat.DamageableCutout
import com.example.game.combat.HitZone
import com.example.game.combat.MeritRules
import com.example.game.combat.Vec3
import com.example.game.combat.WeaponRuntime
import com.example.game.combat.toDamagePayload
import kotlin.math.abs
import kotlin.random.Random

/**
 * Drill 3: the CQB killhouse.
 *
 * Three rooms of pop-up cutouts. Doors can be picked, kicked or blown with a
 * charge; a flashbang blinds the room for 4 seconds. Hit an unarmed civilian
 * cutout and the qualification fails immediately for zero merit.
 *
 * Every engagement runs the same pipeline as the range: the trigger has to
 * release a round, the aim ray has to strike a zone sphere, the muzzle ray has
 * to strike that same sphere, and only then does the cutout take damage.
 */
class KillhouseDrill(
    private val weapon: WeaponRuntime,
    private val rankIndex: Int = 6,
    private val random: Random = Random(23)
) : PlayableDrill {

    override val drill: DrillId = DrillId.KILLHOUSE

    private data class KillTarget(
        val id: String,
        val hostile: Boolean,
        val x: Float,
        val y: Float,
        var down: Boolean = false,
        val armor: ArmorProfile = ArmorProfile()
    ) {
        /** Screen space is mapped 1:1 into metres so zone spheres stay concentric. */
        val cutout = DamageableCutout(
            transformRef = Vec3(x * 10f, (1f - y) * 10f, 0f),
            maxHealth = 90f,
            effectiveRangeMeters = 300f,
            armor = armor,
            id = id
        )
    }

    private data class Room(val name: String, val targets: List<KillTarget>)

    private val rooms = listOf(
        Room(
            name = "ENTRY HALL",
            targets = listOf(
                KillTarget("A1", hostile = true, x = 0.30f, y = 0.46f),
                KillTarget("A2", hostile = false, x = 0.70f, y = 0.42f)
            )
        ),
        Room(
            name = "LIVING QUARTERS",
            targets = listOf(
                KillTarget("B1", hostile = true, x = 0.24f, y = 0.50f),
                KillTarget("B2", hostile = true, x = 0.50f, y = 0.38f),
                KillTarget("B3", hostile = false, x = 0.76f, y = 0.52f)
            )
        ),
        Room(
            name = "REAR OFFICE",
            targets = listOf(
                KillTarget("C1", hostile = false, x = 0.34f, y = 0.46f),
                KillTarget(
                    "C2",
                    hostile = true,
                    x = 0.66f,
                    y = 0.40f,
                    armor = ArmorProfile(plateRating = 2, plateDurability = 45, helmetTier = 1)
                )
            )
        )
    )

    private val alerts = mutableListOf<String>()

    var elapsedSeconds: Float = 0f
        private set
    var flashStunSeconds: Float = 0f
        private set
    var totalScore: Int = 0
        private set
    var infractions: Int = 0
        private set
    var failed: Boolean = false
        private set

    private var roomIndex = 0
    private var breached = false
    private var engagements = 0
    private var correctEngagements = 0
    private var chargesUsed = 0
    private var finished = false
    private var resultCache: DrillResult? = null
    private var statusLine = "Stack on the entry door. Breach when ready."
    private var lastZone: HitZone? = null

    override val isFinished: Boolean get() = finished
    private val currentRoom: Room? get() = rooms.getOrNull(roomIndex)
    private val flashbangUnlocked: Boolean get() = rankIndex >= 6

    init {
        DrillSupport.pushAlert(alerts, "Shoot armed hostiles only. Civilians are no-shoot.")
    }

    override fun tick(dt: Float) {
        if (finished) return
        val step = dt.coerceIn(0f, 0.25f)
        elapsedSeconds += step
        flashStunSeconds = (flashStunSeconds - step).coerceAtLeast(0f)
        weapon.tick(step)
        if (elapsedSeconds >= drill.timeLimitSeconds) finish()
    }

    override fun handleAction(actionId: String) {
        if (finished) return
        val room = currentRoom ?: return

        when {
            actionId == "PICKLOCK" && !breached -> {
                breached = true
                elapsedSeconds += 2.5f
                statusLine = "Lock picked. ${room.name} open - slow and quiet."
            }
            actionId == "KICK_DOOR" && !breached -> {
                breached = true
                statusLine = "Door kicked. ${room.name} exposed - identify your targets."
            }
            actionId == "BREACH_CHARGE" && !breached -> {
                breached = true
                chargesUsed += 1
                elapsedSeconds += 1.5f
                flashStunSeconds = 1.5f
                totalScore += 25
                statusLine = "Charge detonated. ${room.name} exposed."
            }
            actionId == "FLASHBANG" && breached -> {
                if (!flashbangUnlocked) {
                    statusLine = "Flashbangs require E-7 clearance."
                } else if (flashStunSeconds > 0f) {
                    statusLine = "Room already stunned."
                } else {
                    flashStunSeconds = 4f
                    statusLine = "Flashbang in. Targets blinded for 4 seconds."
                    DrillSupport.pushAlert(alerts, statusLine)
                }
            }
            actionId == "CLEAR" -> {
                val prompt = weapon.clearMalfunctionStep()
                statusLine = prompt ?: "Weapon is not jammed."
                DrillSupport.pushAlert(alerts, statusLine)
            }
            actionId == "RELOAD" -> {
                statusLine = if (weapon.beginReload()) {
                    "Reloading - ${weapon.stats.reloadSeconds.formatOne()} s."
                } else {
                    "Cannot reload now."
                }
                DrillSupport.pushAlert(alerts, statusLine)
            }
            actionId.startsWith("ENGAGE_") -> engage(actionId.removePrefix("ENGAGE_"))
        }
    }

    override fun handlePoint(x: Float, y: Float) {
        if (!breached || finished) return
        val room = currentRoom ?: return
        val target = room.targets.filterNot { it.down }.minByOrNull { candidate ->
            val dx = candidate.x - x
            val dy = candidate.y - y
            dx * dx + dy * dy
        } ?: return
        val dx = x - target.x
        val dy = y - target.y
        if ((dx * dx + dy * dy) <= 0.05f) {
            // Where the operator taps on the cutout decides the hit zone.
            val zone = when {
                abs(dx) > 0.07f && dy > -0.045f -> HitZone.ARM
                dy < -0.045f -> HitZone.HEAD
                dy < 0.02f -> HitZone.UPPER_TORSO
                dy < 0.06f -> HitZone.LOWER_TORSO
                else -> HitZone.LEG
            }
            engage(target.id, zone)
        }
    }

    private fun engage(targetId: String, zone: HitZone = HitZone.UPPER_TORSO) {
        val room = currentRoom ?: return
        if (!breached) {
            statusLine = "Door is sealed."
            return
        }
        val target = room.targets.firstOrNull { it.id == targetId && !it.down } ?: return

        val attempt = weapon.tryExecuteShot(nowSeconds = elapsedSeconds, stamina01 = 1f)
        if (!attempt.fired) {
            statusLine = attempt.block?.displayName ?: "Trigger refused."
            DrillSupport.pushAlert(alerts, statusLine)
            return
        }
        if (attempt.jammedNow) {
            DrillSupport.pushAlert(alerts, "STOPPAGE: tap magazine then rack the handle.")
        }

        engagements += 1
        val zonePoint = target.cutout.zoneCenter(zone)
        val eye = zonePoint + Vec3(0f, 0f, -2f)
        val muzzle = eye + Vec3(0.06f, -0.08f, 0.05f)
        val shot = BallisticsEngine.resolve(
            aimOrigin = eye,
            aimDirection = (zonePoint - eye).normalized(),
            muzzleOrigin = muzzle,
            bodies = target.cutout.hitZones,
            maxRangeMeters = 10f,
            spreadDegrees = attempt.spreadDegrees,
            random = random
        )
        val payload = shot.toDamagePayload(
            rawDamage = weapon.stats.damage,
            armorPenetrationPower = weapon.stats.armorPenetration,
            bulletVelocityMetersPerSecond = weapon.stats.muzzleVelocity,
            attackerId = "OPERATOR"
        )
        if (payload == null) {
            statusLine = "NO HIT - ${shot.rejectReason.name.lowercase().replace('_', ' ')}"
            DrillSupport.pushAlert(alerts, statusLine)
            return
        }

        target.cutout.applyDamage(payload)
        val resolution = target.cutout.lastResolution
        val applied = resolution?.appliedDamage ?: 0
        lastZone = payload.zone

        if (!target.hostile) {
            target.down = true
            failed = true
            infractions += 1
            totalScore = 0
            statusLine = "CIVILIAN HIT - qualification failed. Zero merit."
            DrillSupport.pushAlert(alerts, statusLine)
            finish()
            return
        }

        if (target.cutout.isDestroyedOrDead) {
            target.down = true
            correctEngagements += 1
            val stunBonus = if (flashStunSeconds > 0f) 50 else 0
            totalScore += 150 + stunBonus
            statusLine = "HOSTILE ${target.id} down (${payload.zone.displayName}, -$applied HP) +${150 + stunBonus}"
        } else {
            totalScore += 40
            statusLine = "HIT ${target.id} ${payload.zone.displayName} -$applied HP, still standing"
        }
        DrillSupport.pushAlert(alerts, statusLine)

        if (room.targets.all { it.down }) {
            roomIndex += 1
            breached = false
            flashStunSeconds = 0f
            if (roomIndex >= rooms.size) {
                finish()
            } else {
                statusLine = "Room clear. Stack on the next door."
                DrillSupport.pushAlert(alerts, statusLine)
            }
        }
    }

    private fun finish() {
        if (finished) return
        finished = true
        val cleared = roomIndex >= rooms.size
        val passed = !failed && cleared && elapsedSeconds <= drill.timeLimitSeconds
        val accuracy = DrillSupport.accuracy(correctEngagements, engagements)
        val timeRatio = DrillSupport.timeRatio(elapsedSeconds, drill.timeLimitSeconds)
        val notes = mutableListOf(
            "Rooms cleared: ${roomIndex.coerceAtMost(rooms.size)} of ${rooms.size}",
            "Hostiles neutralised: $correctEngagements of $engagements engagements",
            "Breaching charges used: $chargesUsed",
            "Hostage hits: ${if (failed) 1 else 0}"
        )
        if (failed) notes.add("Qualification failed: unarmed civilian hit.")
        resultCache = DrillResult(
            drill = drill,
            passed = passed,
            score = totalScore,
            meritAwarded = if (failed) 0 else MeritRules.drillMerit(drill.maxMerit, accuracy, timeRatio, infractions),
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
        val room = currentRoom
        val markers = mutableListOf<DrillMarker>()
        if (room != null) {
            room.targets.forEach { target ->
                markers.add(
                    DrillMarker(
                        id = target.id,
                        x = target.x,
                        y = target.y,
                        label = if (breached) "CONTACT ${target.id}" else "SEALED",
                        state = when {
                            target.down -> DrillMarkerState.DOWN
                            !breached -> DrillMarkerState.SEALED
                            flashStunSeconds > 0f -> DrillMarkerState.STUNNED
                            else -> DrillMarkerState.TARGET
                        },
                        sublabel = when {
                            !breached -> ""
                            target.hostile && target.armor.hasPlate -> "ARMOR PLATE · RIFLE"
                            target.hostile -> "RIFLE VISIBLE"
                            else -> "EMPTY HANDS"
                        }
                    )
                )
            }
        }

        val actions = mutableListOf<DrillAction>()
        if (!breached) {
            actions.add(DrillAction("PICKLOCK", "PICK LOCK"))
            actions.add(DrillAction("KICK_DOOR", "KICK DOOR"))
            actions.add(DrillAction("BREACH_CHARGE", "PLACE CHARGE"))
        } else {
            actions.add(DrillAction("FLASHBANG", if (flashbangUnlocked) "FLASHBANG (4 s)" else "FLASHBANG LOCKED", flashbangUnlocked))
            room?.targets?.filterNot { it.down }?.forEach { target ->
                actions.add(DrillAction("ENGAGE_${target.id}", "ENGAGE ${target.id}"))
            }
            if (weapon.isJammed) {
                val prompt = weapon.readout().malfunctionPrompt ?: "CLEAR STOPPAGE"
                actions.add(DrillAction("CLEAR", prompt))
            } else if (weapon.loadedRounds < weapon.stats.magazineCapacity && weapon.reserveRounds > 0) {
                actions.add(DrillAction("RELOAD", "RELOAD"))
            }
        }

        val readout = weapon.readout()
        val weaponMetric = buildString {
            append("${readout.loadedRounds}/${readout.magazineCapacity}")
            if (readout.isJammed) append(" · ${readout.malfunctionPrompt}")
        }
        return DrillSnapshot(
            drill = drill,
            statusLine = statusLine,
            timeRemainingSeconds = DrillSupport.remaining(drill.timeLimitSeconds, elapsedSeconds),
            progress = DrillSupport.ratioOf(roomIndex.toFloat(), rooms.size.toFloat()),
            stamina = 100f,
            markers = markers,
            metrics = listOf(
                DrillMetric("SCORE", totalScore.toString()),
                DrillMetric("ROOM", "${(roomIndex + 1).coerceAtMost(rooms.size)}/${rooms.size}"),
                DrillMetric("AMMO", weaponMetric),
                DrillMetric("ZONE", lastZone?.displayName ?: "--"),
                DrillMetric("STUN", "${"%.1f".format(flashStunSeconds)} s"),
                DrillMetric("ENGAGEMENTS", "$correctEngagements/$engagements")
            ),
            actions = actions,
            alerts = alerts.toList()
        )
    }

    private fun Float.formatOne(): String = "%.1f".format(this)
}
