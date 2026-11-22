package com.example.game.tactical

import com.example.game.combat.Vec3
import com.example.game.tactical.ai.AiBrain
import com.example.game.tactical.ai.AiContext
import com.example.game.tactical.ai.AiEvent
import com.example.game.tactical.ai.AiState
import com.example.game.tactical.ai.EnemyAi
import com.example.game.tactical.ai.SquadAvailability
import com.example.game.tactical.ai.SquadCommandWheel
import com.example.game.tactical.ai.SquadMate
import com.example.game.tactical.ai.SquadOrder
import com.example.game.tactical.support.BlastUnit
import com.example.game.tactical.support.DroneMode
import com.example.game.tactical.support.DroneStrikeSystem
import com.example.game.tactical.support.StrikeRequest
import com.example.game.vanguard.CaseRecorder
import com.example.game.vanguard.ContractCase
import com.example.game.vanguard.core.errorCodeOrNull
import com.example.game.vanguard.core.valueOrNull
import com.example.game.vanguard.observed

/** Section 5.2 / 5.3 and Phase 5: the enemy FSM, the squad wheel and the UAV strike. */
internal object TacticalAiContractCases {

    private val engaged = AiBrain(state = AiState.ENGAGE)

    private val context = AiContext(
        playerInVisionCone = true,
        hasClearLineOfSight = true,
        isFacingPlayer = true,
        muzzleAngleToPlayerDegrees = 20f,
        isReloading = false,
        isSuppressed = false,
        coverAvailable = true,
        distanceToPlayerMeters = 50f
    )

    private val mate = SquadMate(id = "m1", callsign = "Kowalski")

    fun run(): List<ContractCase> = aiCases() + squadCases() + droneCases()

    private fun aiCases(): List<ContractCase> {
        val recorder = CaseRecorder(TacticalContractAudit.GROUP_AI)

        recorder.check(
            "Patrol hears gunfire", "PATROL to ALERT",
            EnemyAi.transition(AiBrain(), AiEvent.HEARS_GUNFIRE, context).state == AiState.ALERT,
            observed(EnemyAi.transition(AiBrain(), AiEvent.HEARS_GUNFIRE, context).state, AiState.ALERT)
        )
        recorder.check(
            "Suppressed gunfire is silent", "a suppressed shot leaves the patrol alone",
            EnemyAi.eventForGunshot(true) == AiEvent.HEARS_SUPPRESSED_GUNFIRE &&
                EnemyAi.transition(AiBrain(), AiEvent.HEARS_SUPPRESSED_GUNFIRE, context).state == AiState.PATROL,
            observed(EnemyAi.transition(AiBrain(), AiEvent.HEARS_SUPPRESSED_GUNFIRE, context).state, AiState.PATROL)
        )
        recorder.check(
            "Alert seeks cover", "vision cone plus available cover",
            EnemyAi.transition(AiBrain(state = AiState.ALERT), AiEvent.SEES_PLAYER, context).state ==
                AiState.SEEK_COVER,
            observed(
                EnemyAi.transition(AiBrain(state = AiState.ALERT), AiEvent.SEES_PLAYER, context).state,
                AiState.SEEK_COVER
            )
        )
        recorder.check(
            "Lost sight suppresses", "ENGAGE to SUPPRESS",
            EnemyAi.transition(engaged, AiEvent.LOST_SIGHT, context).state == AiState.SUPPRESS,
            observed(EnemyAi.transition(engaged, AiEvent.LOST_SIGHT, context).state, AiState.SUPPRESS)
        )
        recorder.check(
            "Physical muzzle aim", "20 deg inside the 35 deg arc may damage",
            EnemyAi.mayDamagePlayer(engaged, context),
            observed(EnemyAi.mayDamagePlayer(engaged, context), true)
        )
        val offAim = context.copy(muzzleAngleToPlayerDegrees = 40f)
        recorder.check(
            "Facing away is harmless", "40 deg cannot damage",
            !EnemyAi.mayDamagePlayer(engaged, offAim) &&
                EnemyAi.denialReason(engaged, offAim)?.contains("off target") == true,
            observed(EnemyAi.denialReason(engaged, offAim), "muzzle off target")
        )
        recorder.check(
            "No damage while reloading", "the AI must pass the same gate as the player",
            !EnemyAi.mayDamagePlayer(engaged, context.copy(isReloading = true)) &&
                !EnemyAi.mayDamagePlayer(AiBrain(), context),
            observed(EnemyAi.mayDamagePlayer(engaged, context.copy(isReloading = true)), false)
        )
        return recorder.result()
    }

    private fun squadCases(): List<ContractCase> {
        val recorder = CaseRecorder(TacticalContractAudit.GROUP_AI)

        recorder.check(
            "Command slots", "0 / 1 / 4 by rank",
            SquadCommandWheel.commandSlots(0) == 0 &&
                SquadCommandWheel.commandSlots(2) == 1 &&
                SquadCommandWheel.commandSlots(3) == 4,
            observed(SquadCommandWheel.commandSlots(3), 4)
        )
        recorder.check(
            "Wheel locked at Recruit", "REFUSED_RANK",
            SquadCommandWheel.issue(SquadOrder.FOLLOW, mate, 0, false).availability ==
                SquadAvailability.REFUSED_RANK,
            observed(SquadCommandWheel.issue(SquadOrder.FOLLOW, mate, 0, false).availability, "REFUSED_RANK")
        )
        recorder.check(
            "Follow order", "accepted by a Corporal",
            SquadCommandWheel.issue(SquadOrder.FOLLOW, mate, 2, false).accepted,
            observed(SquadCommandWheel.issue(SquadOrder.FOLLOW, mate, 2, false).accepted, true)
        )
        recorder.check(
            "Suppress needs a target", "REFUSED_NO_TARGET without a designation",
            SquadCommandWheel.issue(SquadOrder.SUPPRESS_TARGET, mate, 3, false).availability ==
                SquadAvailability.REFUSED_NO_TARGET,
            observed(
                SquadCommandWheel.issue(SquadOrder.SUPPRESS_TARGET, mate, 3, false).availability,
                "REFUSED_NO_TARGET"
            )
        )
        recorder.check(
            "Downed mate cannot obey", "REFUSED_DOWNED",
            SquadCommandWheel.issue(
                SquadOrder.FLANK,
                mate.copy(healthPercent = 0, isDowned = true),
                3,
                true
            ).availability == SquadAvailability.REFUSED_DOWNED,
            observed(
                SquadCommandWheel.issue(
                    SquadOrder.FLANK,
                    mate.copy(healthPercent = 0, isDowned = true),
                    3,
                    true
                ).availability,
                "REFUSED_DOWNED"
            )
        )
        recorder.check(
            "Wheel contents", "four orders at Sergeant, none at Recruit",
            SquadCommandWheel.wheelFor(3).size == 4 && SquadCommandWheel.wheelFor(0).isEmpty(),
            observed(SquadCommandWheel.wheelFor(3).size, 4)
        )
        val squad = listOf(mate, SquadMate(id = "m2", callsign = "Reyes", isDowned = true))
        recorder.check(
            "Ordered squad", "downed mates are dropped from the wheel",
            SquadCommandWheel.orderedSquad(3, squad).map { it.id } == listOf("m1"),
            observed(SquadCommandWheel.orderedSquad(3, squad).map { it.id }, listOf("m1"))
        )
        return recorder.result()
    }

    private fun droneCases(): List<ContractCase> {
        val recorder = CaseRecorder(TacticalContractAudit.GROUP_AI)

        recorder.check(
            "Drone rank gate", "locked at Sergeant, open at Lieutenant",
            !DroneStrikeSystem.isUnlocked(3) && DroneStrikeSystem.isUnlocked(4) &&
                DroneStrikeSystem.recon(3).errorCodeOrNull() == "DRONE_LOCKED",
            observed(DroneStrikeSystem.recon(3).errorCodeOrNull(), "DRONE_LOCKED")
        )
        val oversized = StrikeRequest(Vec3.Zero, 100f, DroneMode.GUIDED_STRIKE)
        recorder.check(
            "Blast radius bounds", "100 m is outside 5..60 m",
            DroneStrikeSystem.resolve(oversized, emptyList(), 4).errorCodeOrNull() ==
                "BLAST_RADIUS_OUT_OF_RANGE",
            observed(
                DroneStrikeSystem.resolve(oversized, emptyList(), 4).errorCodeOrNull(),
                "BLAST_RADIUS_OUT_OF_RANGE"
            )
        )
        val units = listOf(
            BlastUnit("h1", Vec3(5f, 0f, 0f), isHostile = true),
            BlastUnit("h2", Vec3(8f, 0f, 0f), isHostile = true, isInsideStructure = true),
            BlastUnit("f1", Vec3(3f, 0f, 0f), isHostile = false)
        )
        val strike = DroneStrikeSystem.resolve(
            StrikeRequest(Vec3.Zero, 20f, DroneMode.GUIDED_STRIKE),
            units,
            4
        ).valueOrNull()
        recorder.check(
            "Blast shielding", "the roof protects the unit underneath",
            strike != null && strike.shieldedUnitIds == listOf("h2") &&
                strike.affectedUnitIds.size == 2,
            observed(strike?.shieldedUnitIds, listOf("h2"))
        )
        recorder.check(
            "Casualty count", "one hostile in the open is lost",
            strike?.casualties == 1,
            observed(strike?.casualties, 1)
        )
        val recon = DroneStrikeSystem.resolve(
            StrikeRequest(Vec3.Zero, 20f, DroneMode.RECON),
            units,
            4
        ).valueOrNull()
        recorder.check(
            "Recon deals no damage", "thermal feed only",
            recon != null && recon.casualties == 0 && recon.affectedUnitIds.isEmpty(),
            observed(recon?.casualties, 0)
        )
        return recorder.result()
    }
}
