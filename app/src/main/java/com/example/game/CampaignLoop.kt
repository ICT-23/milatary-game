package com.example.game

/**
 * The six-step core loop every chapter is built from:
 *
 *   Strategic Briefing -> Tactical Top-Down Deployment -> Environmental Recon &
 *   Scanning -> Consequential Story Dilemma -> Tactical Extraction ->
 *   After-Action Report (AAR) & Operative Promotion.
 *
 * The stages are declared here rather than inferred from the screens so the HUD
 * and the debrief can name the step the player is currently standing in.
 */
enum class CampaignLoopStage(
    val displayName: String,
    val detail: String
) {
    STRATEGIC_BRIEFING(
        displayName = "Strategic Briefing",
        detail = "Read the dossier and accept the operation."
    ),
    TACTICAL_DEPLOYMENT(
        displayName = "Tactical Top-Down Deployment",
        detail = "Move the operator across the outpost with the virtual joystick."
    ),
    ENVIRONMENTAL_RECON(
        displayName = "Environmental Recon & Scanning",
        detail = "Sweep structures with the Field Scanner before committing."
    ),
    STORY_DILEMMA(
        displayName = "Consequential Story Dilemma",
        detail = "Pick an override path; the choice is written into the save."
    ),
    TACTICAL_EXTRACTION(
        displayName = "Tactical Extraction",
        detail = "Deploy the flare at the landing zone and pull out."
    ),
    AFTER_ACTION(
        displayName = "After-Action Report & Operative Promotion",
        detail = "File the AAR, bank the XP and take the promotion."
    );

    /** One-based position in the loop, for "step 3 of 6" readouts. */
    val step: Int get() = ordinal + 1

    companion object {
        /** Total number of stages in the core loop. */
        val TOTAL_STEPS: Int get() = CampaignLoopStage.values().size
    }
}

/**
 * Maps Mission 1's objective chain onto the core loop. Objective ids are stable,
 * so the mapping survives copy changes to the objective titles.
 */
object CampaignLoop {

    /** The stage the player is in for the given objective id. */
    fun stageForObjectiveId(objectiveId: String?): CampaignLoopStage = when (objectiveId) {
        "OBJ_RECON" -> CampaignLoopStage.TACTICAL_DEPLOYMENT
        "OBJ_SCAN_RELAY" -> CampaignLoopStage.ENVIRONMENTAL_RECON
        "OBJ_GENERATOR" -> CampaignLoopStage.ENVIRONMENTAL_RECON
        "OBJ_DECISION" -> CampaignLoopStage.STORY_DILEMMA
        "OBJ_EXTRACTION" -> CampaignLoopStage.TACTICAL_EXTRACTION
        else -> CampaignLoopStage.TACTICAL_DEPLOYMENT
    }

    fun stageForObjectiveIndex(index: Int): CampaignLoopStage =
        stageForObjectiveId(GameEngine.OBJECTIVE_IDS.getOrNull(index))

    fun stageForObjective(objective: TacticalObjective?): CampaignLoopStage =
        stageForObjectiveId(objective?.id)

    /** Stage that follows [stage], or null once the loop has reported. */
    fun next(stage: CampaignLoopStage): CampaignLoopStage? =
        CampaignLoopStage.values().getOrNull(stage.ordinal + 1)
}
