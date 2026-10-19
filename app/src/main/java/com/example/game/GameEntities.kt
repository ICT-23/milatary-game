package com.example.game

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Vector2(val x: Float, val y: Float) {
    operator fun plus(other: Vector2) = Vector2(x + other.x, y + other.y)
    operator fun minus(other: Vector2) = Vector2(x - other.x, y - other.y)
    operator fun times(scalar: Float) = Vector2(x * scalar, y * scalar)

    fun length(): Float = sqrt(x * x + y * y)

    fun distanceTo(other: Vector2): Float {
        val dx = x - other.x
        val dy = y - other.y
        return sqrt(dx * dx + dy * dy)
    }

    fun normalized(): Vector2 {
        val len = length()
        return if (len > 0.0001f) Vector2(x / len, y / len) else Vector2(0f, 0f)
    }

    companion object {
        val Zero = Vector2(0f, 0f)
    }
}

data class ObstacleRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val label: String = "Barrier",
    val isTall: Boolean = true // casts shadow
) {
    fun contains(point: Vector2, padding: Float = 0f): Boolean {
        return point.x >= (left - padding) && point.x <= (right + padding) &&
                point.y >= (top - padding) && point.y <= (bottom + padding)
    }
}

data class TacticalNPC(
    val id: String,
    val name: String,
    val callsign: String,
    val role: String,
    var position: Vector2,
    val facingAngleRad: Float = 0f,
    val uniformColor: Long = 0xFF667353,
    val hasPendingDialogue: Boolean = true
)

data class InteractiveTarget(
    val id: String,
    val title: String,
    val prompt: String,
    val position: Vector2,
    val interactionRadius: Float = 60f,
    val type: InteractiveType,
    var isInteracted: Boolean = false,
    var isEnabled: Boolean = true
)

enum class InteractiveType {
    RELAY_TERMINAL,
    GENERATOR,
    SQUAD_MEMBER,
    INTEL_CACHE,
    CIVILIAN_SHELTER,
    EXTRACTION_BEACON
}

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var life: Float,
    val maxLife: Float,
    val color: Long,
    val size: Float
)

data class ScannerWave(
    val origin: Vector2,
    var currentRadius: Float = 0f,
    val maxRadius: Float = 260f,
    val speed: Float = 380f, // px per sec
    var isFinished: Boolean = false
)

data class TacticalObjective(
    val id: String,
    val title: String,
    val detail: String,
    val targetPosition: Vector2?,
    val isCompleted: Boolean = false
)

enum class MissionState {
    BRIEFING,
    ACTIVE_GAMEPLAY,
    DIALOGUE_MODAL,
    DECISION_MODAL,
    MISSION_PAUSED,
    MISSION_SUCCESS,
    MISSION_FAILED
}
