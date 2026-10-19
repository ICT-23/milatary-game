package com.example.game

import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class GameEngine {
    val worldWidth = 1200f
    val worldHeight = 900f

    // Player State
    var playerPos = Vector2(160f, 320f)
    var playerVel = Vector2(0f, 0f)
    var playerFacingAngleRad = 0f
    var playerHealth = 100
    var playerMorale = 100
    val playerRadius = 18f
    var isWalking = false
    var moveSpeed = 160f // px per sec

    // Camera offset (top-left of viewport in world coords)
    var cameraPos = Vector2(0f, 0f)

    // Obstacles in Sector 4
    val obstacles = listOf(
        ObstacleRect(60f, 60f, 240f, 220f, "Command Outpost"),
        ObstacleRect(520f, 100f, 680f, 220f, "Relay Array Alpha"),
        ObstacleRect(760f, 340f, 940f, 480f, "Aux Generator Vault"),
        ObstacleRect(920f, 600f, 1140f, 780f, "Civilian Shelter"),
        ObstacleRect(340f, 420f, 480f, 450f, "Sandbag Emplacement"),
        ObstacleRect(0f, 760f, 280f, 900f, "Coastal Cliff Ridge")
    )

    // Interactive Targets
    val interactiveTargets = mutableListOf(
        InteractiveTarget(
            id = "TARGET_TOMAS",
            title = "Tomas Vale (Recon)",
            prompt = "REPORT TO TOMAS",
            position = Vector2(380f, 300f),
            interactionRadius = 70f,
            type = InteractiveType.SQUAD_MEMBER
        ),
        InteractiveTarget(
            id = "TARGET_RELAY",
            title = "Relay Tower Alpha",
            prompt = "SCAN TELEMETRY DISH",
            position = Vector2(600f, 240f),
            interactionRadius = 75f,
            type = InteractiveType.RELAY_TERMINAL,
            isEnabled = false
        ),
        InteractiveTarget(
            id = "TARGET_GENERATOR",
            title = "Auxiliary Generator",
            prompt = "INSPECT VENTING SYSTEM",
            position = Vector2(850f, 490f),
            interactionRadius = 75f,
            type = InteractiveType.GENERATOR,
            isEnabled = false
        ),
        InteractiveTarget(
            id = "TARGET_CONSOLE",
            title = "Manual Override Console",
            prompt = "MAKE EMERGENCY DECISION",
            position = Vector2(980f, 560f),
            interactionRadius = 75f,
            type = InteractiveType.INTEL_CACHE,
            isEnabled = false
        ),
        InteractiveTarget(
            id = "TARGET_EXTRACTION",
            title = "LZ Falcon Extraction Beacon",
            prompt = "TRANSMIT EXTRACTION FLARE",
            position = Vector2(220f, 660f),
            interactionRadius = 80f,
            type = InteractiveType.EXTRACTION_BEACON,
            isEnabled = false
        )
    )

    // Dynamic Visual FX
    val particles = mutableListOf<Particle>()
    val scannerWaves = mutableListOf<ScannerWave>()

    // Mission Flow Tracker
    var currentObjectiveIndex = 0
    val objectives = listOf(
        TacticalObjective(
            id = "OBJ_RECON",
            title = "Link up with Recon Specialist",
            detail = "Proceed east past the perimeter gate to meet Tomas Vale.",
            targetPosition = Vector2(380f, 300f)
        ),
        TacticalObjective(
            id = "OBJ_SCAN_RELAY",
            title = "Scan Damaged Relay Tower Alpha",
            detail = "Use your Field Scanner to inspect the damaged transmitter dishes.",
            targetPosition = Vector2(600f, 240f)
        ),
        TacticalObjective(
            id = "OBJ_GENERATOR",
            title = "Investigate Overloading Generator",
            detail = "Follow the power cables southeast to the smoking auxiliary generator vault.",
            targetPosition = Vector2(850f, 490f)
        ),
        TacticalObjective(
            id = "OBJ_DECISION",
            title = "Resolve Override Dilemma",
            detail = "Decide whether to vent power into the civilian bunker or capture the saboteur drive.",
            targetPosition = Vector2(980f, 560f)
        ),
        TacticalObjective(
            id = "OBJ_EXTRACTION",
            title = "Extract at LZ Falcon",
            detail = "Regroup at the coastal landing zone and deploy the extraction signal beacon.",
            targetPosition = Vector2(220f, 660f)
        )
    )

    var nearestTarget: InteractiveTarget? = null
    var activeScannerCooldown = 0f
    var timeElapsedSeconds = 0f

    init {
        // Seed ambient dust particles
        for (i in 0..35) {
            particles.add(
                Particle(
                    x = Random.nextFloat() * worldWidth,
                    y = Random.nextFloat() * worldHeight,
                    vx = (Random.nextFloat() - 0.5f) * 15f,
                    vy = (Random.nextFloat() - 0.5f) * 15f,
                    life = Random.nextFloat() * 4f,
                    maxLife = 4f,
                    color = 0x33D6C29A,
                    size = 2f + Random.nextFloat() * 3f
                )
            )
        }
    }

    fun update(
        dt: Float,
        joystickVector: Vector2,
        viewportWidth: Float,
        viewportHeight: Float
    ) {
        val clampedDt = min(dt, 0.05f)
        timeElapsedSeconds += clampedDt

        if (activeScannerCooldown > 0f) {
            activeScannerCooldown = max(0f, activeScannerCooldown - clampedDt)
        }

        // Movement Simulation
        val inputMagnitude = joystickVector.length()
        if (inputMagnitude > 0.05f) {
            isWalking = true
            val normalizedInput = joystickVector.normalized()
            playerFacingAngleRad = atan2(normalizedInput.y, normalizedInput.x)

            val moveDelta = normalizedInput * (moveSpeed * clampedDt * min(inputMagnitude, 1.0f))
            val potentialPos = playerPos + moveDelta

            // Collision check against world boundaries and obstacles
            var resolvedX = potentialPos.x
            var resolvedY = potentialPos.y

            // Bound clamping
            resolvedX = resolvedX.coerceIn(playerRadius, worldWidth - playerRadius)
            resolvedY = resolvedY.coerceIn(playerRadius, worldHeight - playerRadius)

            // Obstacle resolution
            val testPos = Vector2(resolvedX, resolvedY)
            var collides = false
            for (obs in obstacles) {
                if (obs.contains(testPos, playerRadius)) {
                    collides = true
                    break
                }
            }

            if (!collides) {
                playerPos = testPos
            } else {
                // Try sliding along X only
                val slideX = Vector2(resolvedX, playerPos.y)
                var collidesX = false
                for (obs in obstacles) {
                    if (obs.contains(slideX, playerRadius)) {
                        collidesX = true
                        break
                    }
                }
                if (!collidesX) {
                    playerPos = slideX
                } else {
                    // Try sliding along Y only
                    val slideY = Vector2(playerPos.x, resolvedY)
                    var collidesY = false
                    for (obs in obstacles) {
                        if (obs.contains(slideY, playerRadius)) {
                            collidesY = true
                            break
                        }
                    }
                    if (!collidesY) playerPos = slideY
                }
            }
        } else {
            isWalking = false
        }

        // Camera smoothly tracks player centered on viewport
        val targetCamX = (playerPos.x - (viewportWidth / 2f)).coerceIn(0f, max(0f, worldWidth - viewportWidth))
        val targetCamY = (playerPos.y - (viewportHeight / 2f)).coerceIn(0f, max(0f, worldHeight - viewportHeight))
        cameraPos = Vector2(
            cameraPos.x + (targetCamX - cameraPos.x) * (clampedDt * 8f),
            cameraPos.y + (targetCamY - cameraPos.y) * (clampedDt * 8f)
        )

        // Update Nearest Interactive Target
        nearestTarget = interactiveTargets.firstOrNull { target ->
            target.isEnabled && !target.isInteracted && playerPos.distanceTo(target.position) <= target.interactionRadius
        }

        // Update Particle Simulation
        val iter = particles.iterator()
        while (iter.hasNext()) {
            val p = iter.next()
            p.x += p.vx * clampedDt
            p.y += p.vy * clampedDt
            p.life += clampedDt
            if (p.life >= p.maxLife) {
                p.x = Random.nextFloat() * worldWidth
                p.y = Random.nextFloat() * worldHeight
                p.life = 0f
            }
        }

        // Add electrical sparks near damaged relay tower
        if (currentObjectiveIndex < 3 && Random.nextFloat() < 0.25f) {
            particles.add(
                Particle(
                    x = 600f + (Random.nextFloat() - 0.5f) * 40f,
                    y = 180f + (Random.nextFloat() - 0.5f) * 40f,
                    vx = (Random.nextFloat() - 0.5f) * 60f,
                    vy = (Random.nextFloat() - 0.5f) * 60f - 20f,
                    life = 0f,
                    maxLife = 0.4f,
                    color = 0xFFE7A83E,
                    size = 3.5f
                )
            )
        }

        // Add smoke puffs from generator
        if (currentObjectiveIndex >= 2 && currentObjectiveIndex < 4 && Random.nextFloat() < 0.35f) {
            particles.add(
                Particle(
                    x = 850f + (Random.nextFloat() - 0.5f) * 30f,
                    y = 420f,
                    vx = (Random.nextFloat() - 0.5f) * 20f,
                    vy = -35f - Random.nextFloat() * 25f,
                    life = 0f,
                    maxLife = 1.2f,
                    color = 0x55C84C4C,
                    size = 8f + Random.nextFloat() * 6f
                )
            )
        }

        // Update Scanner Waves
        val waveIter = scannerWaves.iterator()
        while (waveIter.hasNext()) {
            val wave = waveIter.next()
            wave.currentRadius += wave.speed * clampedDt
            if (wave.currentRadius >= wave.maxRadius) {
                wave.isFinished = true
                waveIter.remove()
            }
        }
    }

    fun triggerScanner(): Boolean {
        if (activeScannerCooldown > 0f) return false
        activeScannerCooldown = 2.5f
        scannerWaves.add(
            ScannerWave(
                origin = playerPos,
                currentRadius = 10f,
                maxRadius = 320f
            )
        )
        return true
    }

    fun advanceObjective() {
        if (currentObjectiveIndex < objectives.size - 1) {
            currentObjectiveIndex++
            when (currentObjectiveIndex) {
                1 -> {
                    // Unlocked scanning relay
                    interactiveTargets.find { it.id == "TARGET_RELAY" }?.isEnabled = true
                }
                2 -> {
                    // Unlocked investigating generator
                    interactiveTargets.find { it.id == "TARGET_GENERATOR" }?.isEnabled = true
                }
                3 -> {
                    // Unlocked decision console
                    interactiveTargets.find { it.id == "TARGET_CONSOLE" }?.isEnabled = true
                }
                4 -> {
                    // Unlocked extraction beacon
                    interactiveTargets.find { it.id == "TARGET_EXTRACTION" }?.isEnabled = true
                }
            }
        }
    }
}
