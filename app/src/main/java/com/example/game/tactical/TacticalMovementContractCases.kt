package com.example.game.tactical

import com.example.game.combat.Vec3
import com.example.game.tactical.combat.CoverKind
import com.example.game.tactical.combat.CoverProbe
import com.example.game.tactical.combat.CoverState
import com.example.game.tactical.combat.CoverSystem
import com.example.game.tactical.combat.HitboxComponent
import com.example.game.tactical.movement.CameraMode
import com.example.game.tactical.movement.CameraState
import com.example.game.tactical.movement.CharacterController
import com.example.game.tactical.movement.LocomotionState
import com.example.game.tactical.movement.MoveDenial
import com.example.game.tactical.movement.MoveInput
import com.example.game.tactical.movement.Stance
import com.example.game.tactical.movement.TacticalCamera
import com.example.game.vanguard.CaseRecorder
import com.example.game.vanguard.ContractCase
import com.example.game.vanguard.approxEquals
import com.example.game.vanguard.observed

/** Phase 2: stance movement, vaulting, the blended camera and contextual cover. */
internal object TacticalMovementContractCases {

    fun run(): List<ContractCase> = movementCases() + coverCases() + cameraCases()

    private fun movementCases(): List<ContractCase> {
        val recorder = CaseRecorder(TacticalContractAudit.GROUP_MOVEMENT)

        val walk = CharacterController.step(LocomotionState(), MoveInput(forward = 1f), 0.1f)
        recorder.check(
            "Acceleration to walk", "1.4 m/s after 0.1 s, 0.14 m travelled",
            approxEquals(walk.state.speedMetersPerSecond, 1.4f) &&
                approxEquals(walk.state.position.distanceTo(Vec3.Zero), 0.14f),
            observed(walk.state.position.distanceTo(Vec3.Zero), 0.14f)
        )
        val sprint = CharacterController.step(LocomotionState(), MoveInput(forward = 1f, sprint = true), 0.1f)
        recorder.check(
            "Sprint", "flag set, 18 stamina/s drained",
            sprint.state.isSprinting && approxEquals(sprint.state.stamina, 98.2f),
            observed(sprint.state.stamina, 98.2f)
        )
        val sprinting = LocomotionState(isSprinting = true, speedMetersPerSecond = 7f)
        recorder.check(
            "Sprint noise", "sprinting carries 30 m",
            approxEquals(sprinting.noiseRadiusMeters, 30f),
            observed(sprinting.noiseRadiusMeters, 30f)
        )
        val vaultHigh = CharacterController.step(
            LocomotionState(), MoveInput(forward = 1f, vault = true), 0.016f, 2f
        )
        recorder.check(
            "Vault height gate", "2 m wall is too high",
            vaultHigh.denial == MoveDenial.VAULT_TOO_HIGH,
            observed(vaultHigh.denial, MoveDenial.VAULT_TOO_HIGH)
        )
        val vault = CharacterController.step(
            LocomotionState(), MoveInput(forward = 1f, vault = true), 0.016f, 1f
        )
        recorder.check(
            "Vault", "1 m wall clears for 15 stamina",
            vault.denial == MoveDenial.NONE && vault.state.isVaulting &&
                approxEquals(vault.state.stamina, 85f),
            observed(vault.state.stamina, 85f)
        )
        recorder.check(
            "Stance selection", "prone wins over crouch",
            CharacterController.stanceFor(MoveInput(crouch = true, prone = true)) == Stance.PRONE,
            observed(CharacterController.stanceFor(MoveInput(crouch = true, prone = true)), Stance.PRONE)
        )
        return recorder.result()
    }

    private fun coverCases(): List<ContractCase> {
        val recorder = CaseRecorder(TacticalContractAudit.GROUP_MOVEMENT)

        val highCover = CoverSystem.detect(listOf(CoverProbe("wall", 0.8f, 1.3f, Vec3(1f, 0f, 0f))))
        recorder.check(
            "Cover detection", "1.3 m surface is high cover",
            highCover.kind == CoverKind.HIGH,
            observed(highCover.kind, CoverKind.HIGH)
        )
        val crouched = CoverState("sandbags", CoverKind.LOW, Vec3(0f, 0f, 1f))
        recorder.check(
            "Low cover concealment", "crouching hides head and thorax",
            CoverSystem.concealed(crouched, Stance.CROUCHING) ==
                setOf(HitboxComponent.HEAD, HitboxComponent.THORAX),
            observed(CoverSystem.concealed(crouched, Stance.CROUCHING), "HEAD+THORAX")
        )
        recorder.check(
            "Prone behind low cover", "stomach is hidden as well",
            CoverSystem.concealed(crouched, Stance.PRONE).size == 3,
            observed(CoverSystem.concealed(crouched, Stance.PRONE).size, 3)
        )
        recorder.check(
            "Peeking exposes the torso", "aiming behind cover exposes everything",
            CoverSystem.concealed(CoverSystem.peek(crouched, true), Stance.CROUCHING).isEmpty(),
            observed(CoverSystem.concealed(CoverSystem.peek(crouched, true), Stance.CROUCHING).size, 0)
        )
        val slid = CoverSystem.lockedDirection(Vec3(1f, 0f, 1f), Vec3(1f, 0f, 0f))
        recorder.check(
            "Cover-plane movement", "normal component removed",
            approxEquals(slid.x, 0f) && approxEquals(slid.z, 1f),
            observed(slid, Vec3(0f, 0f, 1f))
        )
        return recorder.result()
    }

    private fun cameraCases(): List<ContractCase> {
        val recorder = CaseRecorder(TacticalContractAudit.GROUP_MOVEMENT)

        recorder.check(
            "Camera ADS zoom", "4x optic yields 11.25 deg, 12x floors at 6 deg",
            TacticalCamera.targetMode(true, false) == CameraMode.ADS_ZOOM &&
                approxEquals(TacticalCamera.adsFovDegrees(4f), 11.25f) &&
                approxEquals(TacticalCamera.adsFovDegrees(12f), 6f),
            observed(TacticalCamera.adsFovDegrees(4f), 11.25f)
        )
        val settled = TacticalCamera.step(
            CameraState(),
            TacticalCamera.targetState(true, false, 1f),
            TacticalCamera.ADS_TRANSITION_SECONDS
        )
        recorder.check(
            "Camera blend", "ADS settles on 45 deg",
            settled.mode == CameraMode.ADS_ZOOM && settled.isFullyAimed &&
                approxEquals(settled.fovDegrees, 45f),
            observed(settled.fovDegrees, 45f)
        )
        recorder.check(
            "Depth of field", "on while aiming or at the bench only",
            TacticalCamera.depthOfFieldEnabled(settled) &&
                TacticalCamera.depthOfFieldEnabled(CameraState(), atWorkbench = true) &&
                !TacticalCamera.depthOfFieldEnabled(CameraState()),
            observed(TacticalCamera.depthOfFieldEnabled(CameraState()), false)
        )
        return recorder.result()
    }
}
