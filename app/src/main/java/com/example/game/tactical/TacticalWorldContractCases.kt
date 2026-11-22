package com.example.game.tactical

import com.example.game.combat.Vec3
import com.example.game.tactical.audio.AudioMix
import com.example.game.tactical.audio.SpatialAudio
import com.example.game.tactical.campaign.BranchingDialogue
import com.example.game.tactical.campaign.IronVanguardCampaign
import com.example.game.tactical.combat.HitboxComponent
import com.example.game.tactical.gunsmith.ForgeMaterial
import com.example.game.tactical.integration.LegacyBridge
import com.example.game.tactical.progression.CampaignAct
import com.example.game.tactical.progression.RankRequirement
import com.example.game.tactical.render.ImpactSurface
import com.example.game.tactical.render.ImpactVfx
import com.example.game.tactical.render.PbrMaterials
import com.example.game.tactical.render.QualityPreset
import com.example.game.tactical.render.RenderPipeline
import com.example.game.tactical.state.GameStateManager
import com.example.game.vanguard.CaseRecorder
import com.example.game.vanguard.ContractCase
import com.example.game.vanguard.approxEquals
import com.example.game.vanguard.core.errorCodeOrNull
import com.example.game.vanguard.core.isOk
import com.example.game.vanguard.core.valueOrNull
import com.example.game.vanguard.observed

/** Sections 2 and 6 plus Phases 1 and 6: campaign, dialogue, presentation and the legacy bridge. */
internal object TacticalWorldContractCases {

    fun run(): List<ContractCase> = campaignCases() + dialogueCases() + presentationCases() + bridgeCases()

    private fun campaignCases(): List<ContractCase> {
        val recorder = CaseRecorder(TacticalContractAudit.GROUP_CAMPAIGN)

        recorder.check(
            "Campaign shape", "12 missions over four acts",
            IronVanguardCampaign.MISSIONS.size == 12 &&
                IronVanguardCampaign.MISSIONS.map { it.id }.distinct().size == 12 &&
                CampaignAct.entries.size == 4,
            observed(IronVanguardCampaign.MISSIONS.size, 12)
        )
        recorder.check(
            "Campaign economy", "rewards total the 35,000 XP General rung",
            IronVanguardCampaign.MISSIONS.sumOf { it.xpReward } == 35_000,
            observed(IronVanguardCampaign.MISSIONS.sumOf { it.xpReward }, 35_000)
        )
        recorder.check(
            "Act I milestone", "three missions award 500 XP",
            IronVanguardCampaign.forAct(CampaignAct.ACT_I).sumOf { it.xpReward } == 500,
            observed(IronVanguardCampaign.forAct(CampaignAct.ACT_I).sumOf { it.xpReward }, 500)
        )
        recorder.check(
            "First mission opens", "A1-01 at Recruit",
            IronVanguardCampaign.open("A1-01", 0, emptySet()).isOk,
            observed(true, true)
        )
        recorder.check(
            "Act IV rank gate", "RANK_LOCKED for a Recruit",
            IronVanguardCampaign.open("A4-01", 0, emptySet()).errorCodeOrNull() == "RANK_LOCKED",
            observed(IronVanguardCampaign.open("A4-01", 0, emptySet()).errorCodeOrNull(), "RANK_LOCKED")
        )
        recorder.check(
            "Sequential missions", "PRIOR_MISSION_PENDING before Act I clears",
            IronVanguardCampaign.open("A2-01", 1, emptySet()).errorCodeOrNull() ==
                "PRIOR_MISSION_PENDING",
            observed(
                IronVanguardCampaign.open("A2-01", 1, emptySet()).errorCodeOrNull(),
                "PRIOR_MISSION_PENDING"
            )
        )
        recorder.check(
            "Unknown mission", "MISSION_NOT_FOUND",
            IronVanguardCampaign.open("A9-99", 0, emptySet()).errorCodeOrNull() == "MISSION_NOT_FOUND",
            observed(
                IronVanguardCampaign.open("A9-99", 0, emptySet()).errorCodeOrNull(),
                "MISSION_NOT_FOUND"
            )
        )
        val allIds = IronVanguardCampaign.MISSIONS.map { it.id }.toSet()
        recorder.check(
            "Campaign completion", "all twelve missions close the campaign",
            IronVanguardCampaign.isComplete(allIds) && approxEquals(IronVanguardCampaign.progress(allIds), 1f),
            observed(IronVanguardCampaign.progress(allIds), 1f)
        )
        recorder.check(
            "Act II completion gate", "Captain requires ACT_II_COMPLETE",
            IronVanguardCampaign.completionRequirement(CampaignAct.ACT_II) ==
                RankRequirement.ACT_II_COMPLETE,
            observed(IronVanguardCampaign.completionRequirement(CampaignAct.ACT_II), "ACT_II_COMPLETE")
        )
        return recorder.result()
    }

    private fun dialogueCases(): List<ContractCase> {
        val recorder = CaseRecorder(TacticalContractAudit.GROUP_CAMPAIGN)

        val opened = BranchingDialogue.start("act1_informant", rankIndex = 0).valueOrNull()
        recorder.check(
            "Dialogue opens", "the Sergeant speaks first",
            opened?.nodeId == "vance_intro",
            observed(opened?.nodeId, "vance_intro")
        )
        val suspicious = opened?.let { BranchingDialogue.choose(it, "question_roster").valueOrNull() }
        recorder.check(
            "Branch on suspicion", "the roster question sets a flag and a different node",
            suspicious?.state?.flags?.contains(BranchingDialogue.FLAG_SUSPECTED_ROSTER) == true &&
                suspicious.node?.id == "vance_warns",
            observed(suspicious?.node?.id, "vance_warns")
        )
        val bonded = opened?.let {
            BranchingDialogue.choose(it, "volunteer").valueOrNull()?.state
        }?.let { state ->
            BranchingDialogue.advance(state).valueOrNull()?.state
        }?.let { state ->
            BranchingDialogue.choose(state, "share_intel").valueOrNull()
        }
        recorder.check(
            "Morale accumulates", "volunteer +5 then briefing +8 reaches 63",
            bonded?.state?.morale == 63,
            observed(bonded?.state?.morale, 63)
        )
        recorder.check(
            "Act IV rank gate", "DIALOGUE_RANK_LOCKED below Lieutenant",
            BranchingDialogue.start("act4_comrades", rankIndex = 0).errorCodeOrNull() ==
                "DIALOGUE_RANK_LOCKED",
            observed(
                BranchingDialogue.start("act4_comrades", rankIndex = 0).errorCodeOrNull(),
                "DIALOGUE_RANK_LOCKED"
            )
        )
        val briefing = BranchingDialogue.start("act4_comrades", rankIndex = 4).valueOrNull()
        val rescue = briefing?.let { BranchingDialogue.choose(it, "divert_rescue").valueOrNull() }
        recorder.check(
            "Saving the squad", "flag set, morale 70, tempo spent",
            rescue?.state?.squadSaved == true && rescue.state.morale == 70 &&
                rescue.state.advantage == 0 && rescue.node?.id == "rescue_success",
            observed(rescue?.state?.morale, 70)
        )
        val spire = briefing?.let { BranchingDialogue.choose(it, "push_spire").valueOrNull() }
        recorder.check(
            "Taking the shortcut", "advantage +25, morale 35, squad abandoned",
            spire?.state?.abandonedSquad == true && spire.state.advantage == 25 &&
                spire.state.morale == 35,
            observed(spire?.state?.morale, 35)
        )
        recorder.check(
            "Scene discipline", "choices and advances are not interchangeable",
            rescue?.state?.let { BranchingDialogue.choose(it, "divert_rescue").errorCodeOrNull() } ==
                "NOT_A_DECISION" &&
                briefing?.let { BranchingDialogue.advance(it).errorCodeOrNull() } == "DECISION_REQUIRED",
            observed("NOT_A_DECISION / DECISION_REQUIRED", "NOT_A_DECISION / DECISION_REQUIRED")
        )
        recorder.check(
            "Scene ends cleanly", "the rescue node has no follow-up",
            rescue?.state?.let { BranchingDialogue.advance(it).valueOrNull()?.node } == null,
            observed(rescue?.state?.let { BranchingDialogue.advance(it).valueOrNull()?.node }, null)
        )
        return recorder.result()
    }

    private fun presentationCases(): List<ContractCase> {
        val recorder = CaseRecorder(TacticalContractAudit.GROUP_PRESENTATION)

        recorder.check(
            "Quality presets", "low-end gets LOW, mid gets HIGH, top gets ULTRA",
            RenderPipeline.recommend(1_024, 1) == QualityPreset.LOW &&
                RenderPipeline.recommend(4_096, 2) == QualityPreset.HIGH &&
                RenderPipeline.recommend(8_192, 3) == QualityPreset.ULTRA,
            observed(RenderPipeline.recommend(4_096, 2), QualityPreset.HIGH)
        )
        recorder.check(
            "Low preset drops SSAO", "bloom stays, AO and DoF go",
            !RenderPipeline.post(QualityPreset.LOW).ssao &&
                RenderPipeline.post(QualityPreset.LOW).bloom &&
                !RenderPipeline.post(QualityPreset.LOW).depthOfField,
            observed(RenderPipeline.post(QualityPreset.LOW).ssao, false)
        )
        recorder.check(
            "PBR material mapping", "polymer stock resolves to the polymer material",
            PbrMaterials.forForgeMaterial(ForgeMaterial.POLYMER) == PbrMaterials.POLYMER,
            observed(PbrMaterials.forForgeMaterial(ForgeMaterial.POLYMER).id, "polymer")
        )
        recorder.check(
            "Speed of sound", "343 m report delay is one second",
            approxEquals(SpatialAudio.reportDelaySeconds(343f), 1f),
            observed(SpatialAudio.reportDelaySeconds(343f), 1f)
        )
        recorder.check(
            "Supersonic crack", "an 880 m round cracks before the report",
            approxEquals(SpatialAudio.crackDelaySeconds(880f), 1f) &&
                SpatialAudio.shotReport(100f, false).cracksBeforeReport,
            observed(SpatialAudio.shotReport(100f, false).cracksBeforeReport, true)
        )
        recorder.check(
            "Suppressor audibility", "50 m is silent, 50 m unsuppressed is heard",
            !SpatialAudio.isAudible(50f, true) && SpatialAudio.isAudible(50f, false),
            observed(SpatialAudio.isAudible(50f, true), false)
        )
        recorder.check(
            "Peak level", "128 dB at 1 m reads 108 dB at 10 m",
            approxEquals(SpatialAudio.shotReport(10f, true).peakDb, 108f),
            observed(SpatialAudio.shotReport(10f, true).peakDb, 108f)
        )
        recorder.check(
            "Stereo pan", "right is +1, left is -1",
            approxEquals(SpatialAudio.panStereo(Vec3(1f, 0f, 0f), Vec3.Zero, Vec3(10f, 0f, 0f)), 1f) &&
                approxEquals(SpatialAudio.panStereo(Vec3(1f, 0f, 0f), Vec3.Zero, Vec3(-10f, 0f, 0f)), -1f),
            observed(SpatialAudio.panStereo(Vec3(1f, 0f, 0f), Vec3.Zero, Vec3(10f, 0f, 0f)), 1f)
        )
        recorder.check(
            "Mixer clamp", "a 2.0 master is clipped to unity",
            approxEquals(AudioMix(master = 2f).clamped().master, 1f),
            observed(AudioMix(master = 2f).clamped().master, 1f)
        )
        recorder.check(
            "Hit marker", "a lethal headshot reads KILL / HEADSHOT",
            ImpactVfx.hitMarker(HitboxComponent.HEAD, true) == "KILL / HEADSHOT",
            observed(ImpactVfx.hitMarker(HitboxComponent.HEAD, true), "KILL / HEADSHOT")
        )
        recorder.check(
            "Impact VFX", "metal throws twelve sparks",
            ImpactVfx.effectFor(ImpactSurface.METAL).sparkCount == 12 &&
                ImpactVfx.surfaceFor(PbrMaterials.CONCRETE.id) == ImpactSurface.CONCRETE,
            observed(ImpactVfx.effectFor(ImpactSurface.METAL).sparkCount, 12)
        )
        return recorder.result()
    }

    private fun bridgeCases(): List<ContractCase> {
        val recorder = CaseRecorder(TacticalContractAudit.GROUP_STATE)

        recorder.check(
            "Legacy rank mapping", "legacy Sergeant maps to tactical Sergeant",
            LegacyBridge.toTacticalRankIndex(4, "SGT") == 3 &&
                LegacyBridge.toLegacyClearance(4) == 9,
            observed(LegacyBridge.toTacticalRankIndex(4, "SGT"), 3)
        )
        recorder.check(
            "Merit to XP", "90,000 MP is the full 35,000 XP ladder",
            LegacyBridge.xpFromMerit(90_000) == 35_000 &&
                LegacyBridge.meritFromXp(35_000) == 90_000,
            observed(LegacyBridge.xpFromMerit(90_000), 35_000)
        )
        val migrated = LegacyBridge.seed("VANCE", 4_200, 4, "SGT")
        recorder.check(
            "Profile migration", "keeps Sergeant, floors XP at 3,500 and preserves gates",
            migrated.rankIndex == 3 && migrated.xp == 3_500 &&
                RankRequirement.SQUAD_LEADER_TRIAL in migrated.satisfied &&
                migrated == GameStateManager
                    .load(GameStateManager.save(migrated))
                    .valueOrNull(),
            observed(migrated.xp, 3_500)
        )
        return recorder.result()
    }
}
