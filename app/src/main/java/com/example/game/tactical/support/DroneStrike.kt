package com.example.game.tactical.support

import com.example.game.combat.Vec3
import com.example.game.tactical.progression.RankLadder
import com.example.game.vanguard.core.GameErrorCategory
import com.example.game.vanguard.core.GameResult

/**
 * Section 5.3: UAV recon and guided drone strike.
 *
 * Unlocked at Lieutenant (tier 5). The blast is an overlap-sphere query plus a
 * line-of-sight test, so a bunker roof protects the soldiers underneath an
 * anti-personnel munition - the same obstruction rule the bullets obey.
 */
enum class DroneMode(val displayName: String) {
    RECON("Thermal Recon Feed"),
    GUIDED_STRIKE("Guided Munition")
}

data class BlastUnit(
    val id: String,
    val position: Vec3,
    val isHostile: Boolean,
    val isInsideStructure: Boolean = false
)

data class StrikeRequest(
    val target: Vec3,
    val blastRadiusMeters: Float,
    val mode: DroneMode,
    val isAntiPersonnel: Boolean = true
)

data class StrikeResult(
    val mode: DroneMode,
    val radiusMeters: Float,
    val affectedUnitIds: List<String>,
    val shieldedUnitIds: List<String>,
    val casualties: Int
)

object DroneStrikeSystem {

    const val UNLOCK_RANK_INDEX = RankLadder.DRONE_UNLOCK_INDEX
    const val MIN_BLAST_RADIUS_METERS = 5f
    const val MAX_BLAST_RADIUS_METERS = 60f

    fun isUnlocked(rankIndex: Int): Boolean = RankLadder.hasClearance(rankIndex, UNLOCK_RANK_INDEX)

    /** Flips the camera into the overhead FLIR feed; no damage is possible here. */
    fun recon(rankIndex: Int): GameResult<DroneMode> {
        if (!isUnlocked(rankIndex)) {
            return GameResult.fail(
                GameErrorCategory.PROGRESSION,
                "DRONE_LOCKED",
                "UAV feed unlocks at ${RankLadder.tierAt(UNLOCK_RANK_INDEX).display}"
            )
        }
        return GameResult.Ok(DroneMode.RECON)
    }

    /**
     * OverlapSphere + line of sight. A unit inside a structure is shielded from
     * an anti-personnel munition; a shaped/anti-materiel munition is not stopped
     * by the roof.
     */
    fun resolve(request: StrikeRequest, units: List<BlastUnit>, rankIndex: Int): GameResult<StrikeResult> {
        if (!isUnlocked(rankIndex)) {
            return GameResult.fail(
                GameErrorCategory.PROGRESSION,
                "DRONE_LOCKED",
                "Guided munitions unlock at ${RankLadder.tierAt(UNLOCK_RANK_INDEX).display}"
            )
        }
        if (request.blastRadiusMeters !in MIN_BLAST_RADIUS_METERS..MAX_BLAST_RADIUS_METERS) {
            return GameResult.fail(
                GameErrorCategory.INVALID_STATE,
                "BLAST_RADIUS_OUT_OF_RANGE",
                "Blast radius ${request.blastRadiusMeters} m outside " +
                    "$MIN_BLAST_RADIUS_METERS..$MAX_BLAST_RADIUS_METERS m"
            )
        }
        if (request.mode == DroneMode.RECON) {
            return GameResult.Ok(
                StrikeResult(
                    mode = DroneMode.RECON,
                    radiusMeters = request.blastRadiusMeters,
                    affectedUnitIds = emptyList(),
                    shieldedUnitIds = emptyList(),
                    casualties = 0
                )
            )
        }

        val inRadius = units.filter { it.position.distanceTo(request.target) <= request.blastRadiusMeters }
        val shielded = inRadius.filter { request.isAntiPersonnel && it.isInsideStructure }
        val affected = inRadius - shielded.toSet()

        return GameResult.Ok(
            StrikeResult(
                mode = request.mode,
                radiusMeters = request.blastRadiusMeters,
                affectedUnitIds = affected.map { it.id },
                shieldedUnitIds = shielded.map { it.id },
                casualties = affected.count { it.isHostile }
            )
        )
    }
}
