package com.example.game.vanguard

import com.example.game.vanguard.combat.AcousticEnvironment
import com.example.game.vanguard.combat.AcousticReverbSystem
import com.example.game.vanguard.core.GameResult
import com.example.game.vanguard.core.errorCodeOrNull
import com.example.game.vanguard.core.valueOrNull
import com.example.game.vanguard.gunsmith.VanguardArmory
import com.example.game.vanguard.progression.SkillBranch
import com.example.game.vanguard.progression.SkillState
import com.example.game.vanguard.progression.SkillTreeService
import com.example.game.vanguard.narrative.VoiceCue
import com.example.game.vanguard.narrative.VoiceOverSync

/**
 * Section 2 Combat / Progression / Narrative coverage.
 *
 * The three "felt" systems: how far a shot carries, what a rank point buys and
 * whether a subtitle keeps up with its line.
 */
object SystemsGamespaceContractCases {

    fun run(): List<ContractCase> {
        val cases = CaseRecorder(VanguardContractAudit.GROUP_SYSTEMS)
        checkAcoustics(cases)
        checkSkills(cases)
        checkVoice(cases)
        return cases.result()
    }

    private fun checkAcoustics(cases: CaseRecorder) {
        val bare = AcousticReverbSystem.profile(
            muzzle = VanguardArmory.MUZZLE_THREAD_PROTECTOR,
            environment = AcousticEnvironment.OPEN_FIELD
        ).valueOrNull()
        cases.check(
            title = "Bare muzzle carries the base radius",
            expectation = "120 m, 160 dB, audible at 120 m but not 130 m",
            passed = bare != null &&
                approxEquals(bare.radiusMeters, 120f) &&
                approxEquals(bare.peakDecibels, 160f) &&
                bare.isAudibleAt(120f) &&
                !bare.isAudibleAt(130f),
            detail = observed(bare?.radiusMeters, 120f)
        )
        val suppressed = AcousticReverbSystem.profile(
            muzzle = VanguardArmory.MUZZLE_SUPPRESSOR,
            environment = AcousticEnvironment.OPEN_FIELD,
            firstShot = true
        ).valueOrNull()
        cases.check(
            title = "Suppressor cuts radius, decibels and tail",
            expectation = "54 m, 136 dB, 0.36 s tail, 6 dB first-shot pop",
            passed = suppressed != null &&
                approxEquals(suppressed.radiusMeters, 54f) &&
                approxEquals(suppressed.peakDecibels, 136f) &&
                approxEquals(suppressed.reverbTailSeconds, 0.36f) &&
                approxEquals(suppressed.firstShotPopDecibels, 6f) &&
                suppressed.isSuppressed,
            detail = observed(suppressed?.radiusMeters, 54f)
        )
        val occluded = AcousticReverbSystem.profile(
            muzzle = VanguardArmory.MUZZLE_THREAD_PROTECTOR,
            environment = AcousticEnvironment.URBAN_STREET,
            shooterOccluded = true
        ).valueOrNull()
        cases.check(
            title = "Urban reverb widens while occlusion masks",
            expectation = "120 x 1.15 x 0.55 = 75.9 m at 148 dB and 1.4 s tail",
            passed = occluded != null &&
                approxEquals(occluded.radiusMeters, 75.9f) &&
                approxEquals(occluded.peakDecibels, 148f) &&
                approxEquals(occluded.reverbTailSeconds, 1.4f),
            detail = observed(occluded?.radiusMeters, 75.9f)
        )
        val broken = AcousticReverbSystem.profile(
            muzzle = VanguardArmory.MUZZLE_THREAD_PROTECTOR.copy(acousticRadiusPercent = 0f),
            environment = AcousticEnvironment.OPEN_FIELD
        )
        cases.check(
            title = "Zero acoustic radius is a typed failure",
            expectation = "INVALID_MUZZLE_ACOUSTICS",
            passed = broken.errorCodeOrNull() == "INVALID_MUZZLE_ACOUSTICS",
            detail = observed(broken.errorCodeOrNull(), "INVALID_MUZZLE_ACOUSTICS")
        )
    }

    private fun unlocked(state: SkillState, nodeId: String): SkillState =
        when (val result = SkillTreeService.unlock(state, nodeId)) {
            is GameResult.Ok -> result.value
            is GameResult.Err -> state
        }

    private fun checkSkills(cases: CaseRecorder) {
        var skill = SkillState(points = 5)
        val gated = SkillTreeService.unlock(skill, "SKILL_TRIGGER_DISCIPLINE").errorCodeOrNull()
        cases.check(
            title = "Prerequisite gates the trigger upgrade",
            expectation = "PREREQUISITE_MISSING before Steady Aim",
            passed = gated == "PREREQUISITE_MISSING",
            detail = observed(gated, "PREREQUISITE_MISSING")
        )
        repeat(3) { skill = unlocked(skill, "SKILL_STEADY_AIM") }
        cases.check(
            title = "A skill stops at its max rank",
            expectation = "rank 3, 2 points left, next purchase MAX_RANK_REACHED",
            passed = skill.rankOf("SKILL_STEADY_AIM") == 3 &&
                skill.points == 2 &&
                SkillTreeService.unlock(skill, "SKILL_STEADY_AIM")
                    .errorCodeOrNull() == "MAX_RANK_REACHED",
            detail = observed("${skill.rankOf("SKILL_STEADY_AIM")}/${skill.points}", "3/2")
        )
        skill = unlocked(skill, "SKILL_TRIGGER_DISCIPLINE")
        cases.check(
            title = "Unlock spends points once the gate opens",
            expectation = "Trigger Discipline at rank 1 with 0 points left",
            passed = skill.rankOf("SKILL_TRIGGER_DISCIPLINE") == 1 && skill.points == 0,
            detail = observed("${skill.rankOf("SKILL_TRIGGER_DISCIPLINE")}/${skill.points}", "1/0")
        )
        cases.check(
            title = "Spending without points fails safely",
            expectation = "NO_SKILL_POINTS and UNKNOWN_SKILL leave the tree untouched",
            passed = SkillTreeService.unlock(skill, "SKILL_FIELD_CRAFT")
                .errorCodeOrNull() == "NO_SKILL_POINTS" &&
                SkillTreeService.unlock(skill, "SKILL_NOPE").errorCodeOrNull() == "UNKNOWN_SKILL" &&
                skill.totalRanks == 4,
            detail = observed(skill.totalRanks, 4)
        )
        cases.check(
            title = "Branch bonus is linear in ranks",
            expectation = "4 marksmanship ranks at 5% = 0.20",
            passed = approxEquals(SkillTreeService.branchBonus(skill, SkillBranch.MARKSMANSHIP), 0.2f),
            detail = observed(SkillTreeService.branchBonus(skill, SkillBranch.MARKSMANSHIP), 0.2f)
        )
    }

    private fun checkVoice(cases: CaseRecorder) {
        val cues = listOf(
            VoiceCue("VC1", "L1", "Sgt Hale", "Line one", 0f, 2f),
            VoiceCue("VC2", "L2", "Sgt Hale", "Line two", 2.4f, 1.6f)
        )
        cases.check(
            title = "VoiceOverSync owns each moment once",
            expectation = "cue 1 at 1.0 s, silence at 2.2 s, gapless timeline",
            passed = VoiceOverSync.activeCue(cues, 1f)?.id == "VC1" &&
                VoiceOverSync.activeCue(cues, 2.2f) == null &&
                VoiceOverSync.subtitleAt(cues, 1f) == "Line one" &&
                VoiceOverSync.isGapless(cues),
            detail = observed(VoiceOverSync.activeCue(cues, 1f)?.id, "VC1")
        )
        cases.check(
            title = "Playback rate stays inside the mix window",
            expectation = "0.85..1.15 accepted, 3.0 falls back to 1.0x",
            passed = !VoiceOverSync.supportsPlaybackRate(0.8f) &&
                VoiceOverSync.supportsPlaybackRate(1.15f) &&
                approxEquals(VoiceOverSync.playbackRate(3f), 1f) &&
                approxEquals(VoiceOverSync.finishSeconds(cues[0], 1.25f), 1.6f),
            detail = observed(VoiceOverSync.playbackRate(3f), 1f)
        )
        val overlapping = listOf(cues[0], cues[0].copy(id = "VC3", startSeconds = 1f))
        cases.check(
            title = "Skip lock protects the first half second",
            expectation = "0.4 s not skippable, 0.5 s skippable, overlap detected",
            passed = !VoiceOverSync.isSkippable(cues[0], 0.4f) &&
                VoiceOverSync.isSkippable(cues[0], 0.5f) &&
                !VoiceOverSync.isGapless(overlapping),
            detail = observed(VoiceOverSync.isSkippable(cues[0], 0.4f), false)
        )
    }
}
