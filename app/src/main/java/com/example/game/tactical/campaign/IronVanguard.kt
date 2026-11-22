package com.example.game.tactical.campaign

import com.example.game.tactical.progression.CampaignAct
import com.example.game.tactical.progression.RankRequirement
import com.example.game.vanguard.core.GameErrorCategory
import com.example.game.vanguard.core.GameResult

/**
 * Section 2: "Operation Iron Vanguard", the four-act campaign at Fort Valor.
 *
 * The XP rewards ladder onto the Section 3 thresholds exactly: Act I pays 500,
 * so a candidate who clears bootcamp lands on Private. Act II pays 3,000
 * (Sergeant), Act III pays 8,500 (Captain) and Act IV pays 23,000, finishing on
 * the 35,000 General rung.
 */
data class MissionBrief(
    val id: String,
    val act: CampaignAct,
    val indexInAct: Int,
    val title: String,
    val setting: String,
    val objective: String,
    val xpReward: Int,
    val creditReward: Int,
    val requiresRankIndex: Int = 0,
    val grantsRequirement: RankRequirement? = null
)

object IronVanguardCampaign {

    const val CODENAME = "Operation Iron Vanguard"
    const val ACADEMY = "Fort Valor"
    const val ENEMY_FACTION = "The Chimera Syndicate"

    private val ACT_I = listOf(
        MissionBrief(
            "A1-01", CampaignAct.ACT_I, 1, "Intake at Fort Valor",
            "Fort Valor parade ground and obstacle course",
            "Pass the timed obstacle course under Drill Sergeant Vance",
            xpReward = 120, creditReward = 150
        ),
        MissionBrief(
            "A1-02", CampaignAct.ACT_I, 2, "Parts and Powder",
            "Academy Armory and pop-up firing range",
            "Assemble a service rifle from the bench and pass Basic Rifle Qualification",
            xpReward = 180, creditReward = 200,
            grantsRequirement = RankRequirement.BASIC_RIFLE_QUAL
        ),
        MissionBrief(
            "A1-03", CampaignAct.ACT_I, 3, "The Informant",
            "Fort Valor killhouse and barracks",
            "Clear the killhouse and identify the Syndicate informant inside the academy",
            xpReward = 200, creditReward = 250
        )
    )

    private val ACT_II = listOf(
        MissionBrief(
            "A2-01", CampaignAct.ACT_II, 1, "Baptism by Fire",
            "Bombarded Eastern European streets",
            "Survive your first live deployment and hold the forward operating base",
            xpReward = 900, creditReward = 900,
            requiresRankIndex = 1,
            grantsRequirement = RankRequirement.FIRST_COMBAT_MISSION
        ),
        MissionBrief(
            "A2-02", CampaignAct.ACT_II, 2, "Supply Line",
            "Highway 12 and the ruined rail depot",
            "Defend the supply convoy using cover and squad commands",
            xpReward = 1_000, creditReward = 1_000,
            requiresRankIndex = 2
        ),
        MissionBrief(
            "A2-03", CampaignAct.ACT_II, 3, "Pinned Platoon",
            "Forward operating base Kilo, breached wall",
            "Breach the strongpoint and extract the pinned allied platoon",
            xpReward = 1_100, creditReward = 1_200,
            requiresRankIndex = 3,
            grantsRequirement = RankRequirement.SQUAD_LEADER_TRIAL
        )
    )

    private val ACT_III = listOf(
        MissionBrief(
            "A3-01", CampaignAct.ACT_III, 1, "Recon Indoctrination",
            "Arid mountain stronghold approach",
            "Pass the Officer Academy board and infiltrate the mountain stronghold",
            xpReward = 2_500, creditReward = 2_000,
            requiresRankIndex = 3,
            grantsRequirement = RankRequirement.OFFICER_ACADEMY_EXAM
        ),
        MissionBrief(
            "A3-02", CampaignAct.ACT_III, 2, "Oil Rig",
            "Naval oil rig, night-vision stealth compound",
            "Dismantle the Syndicate cell on the rig without triggering the alarm",
            xpReward = 3_000, creditReward = 2_400,
            requiresRankIndex = 4
        ),
        MissionBrief(
            "A3-03", CampaignAct.ACT_III, 3, "Drone Command",
            "Special Tactical Reconnaissance forward post",
            "Coordinate UAV strikes and neutralise the Syndicate leadership cell",
            xpReward = 3_000, creditReward = 3_000,
            requiresRankIndex = 4
        )
    )

    private val ACT_IV = listOf(
        MissionBrief(
            "A4-01", CampaignAct.ACT_IV, 1, "Combined Arms",
            "Chimera citadel outer defences",
            "Lead the battalion assault and break the island citadel perimeter",
            xpReward = 7_000, creditReward = 5_000,
            requiresRankIndex = 4
        ),
        MissionBrief(
            "A4-02", CampaignAct.ACT_IV, 2, "No One Left",
            "Citadel collapse zone",
            "Refuse the tactical shortcut and extract the trapped squad without losses",
            xpReward = 8_000, creditReward = 6_000,
            requiresRankIndex = 5,
            grantsRequirement = RankRequirement.ZERO_CASUALTY_OP
        ),
        MissionBrief(
            "A4-03", CampaignAct.ACT_IV, 3, "The Medal of Honor",
            "Chimera citadel command spire",
            "End the Syndicate command and secure campaign victory",
            xpReward = 8_000, creditReward = 8_000,
            requiresRankIndex = 5,
            grantsRequirement = RankRequirement.CAMPAIGN_VICTORY
        )
    )

    val MISSIONS: List<MissionBrief> = ACT_I + ACT_II + ACT_III + ACT_IV

    /** Reaching Captain requires Act II completion, exactly as the table states. */
    val ACT_COMPLETION_REQUIREMENTS: Map<CampaignAct, RankRequirement> = mapOf(
        CampaignAct.ACT_II to RankRequirement.ACT_II_COMPLETE
    )

    fun forAct(act: CampaignAct): List<MissionBrief> = MISSIONS.filter { it.act == act }

    fun missionAt(id: String): MissionBrief? = MISSIONS.firstOrNull { it.id == id }

    fun firstOf(act: CampaignAct): MissionBrief? = forAct(act).minByOrNull { it.indexInAct }

    /**
     * A mission opens only when the rank is held and every earlier mission in
     * the campaign has been completed, so Act IV cannot be rushed from bootcamp.
     */
    fun open(missionId: String, rankIndex: Int, completed: Set<String>): GameResult<MissionBrief> {
        val mission = missionAt(missionId)
            ?: return GameResult.fail(
                GameErrorCategory.NARRATIVE,
                "MISSION_NOT_FOUND",
                "No mission '$missionId' exists in $CODENAME"
            )
        if (rankIndex < mission.requiresRankIndex) {
            return GameResult.fail(
                GameErrorCategory.PROGRESSION,
                "RANK_LOCKED",
                "${mission.title} requires rank index ${mission.requiresRankIndex}"
            )
        }
        val pending = MISSIONS
            .filter { it.id < mission.id && it.id !in completed }
            .firstOrNull()
        if (pending != null) {
            return GameResult.fail(
                GameErrorCategory.NARRATIVE,
                "PRIOR_MISSION_PENDING",
                "Complete ${pending.id} ${pending.title} first"
            )
        }
        return GameResult.Ok(mission)
    }

    /** The requirement earned for finishing a mission's Act, if it has one. */
    fun completionRequirement(act: CampaignAct): RankRequirement? = ACT_COMPLETION_REQUIREMENTS[act]

    fun progress(completed: Set<String>): Float =
        if (MISSIONS.isEmpty()) 0f else completed.count { id -> missionAt(id) != null }.toFloat() / MISSIONS.size

    fun isComplete(completed: Set<String>): Boolean = MISSIONS.all { it.id in completed }
}
