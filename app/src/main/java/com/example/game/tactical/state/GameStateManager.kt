package com.example.game.tactical.state

import com.example.game.tactical.progression.CampaignAct
import com.example.game.tactical.progression.Medal
import com.example.game.tactical.progression.MedalEvaluator
import com.example.game.tactical.progression.MissionRecord
import com.example.game.tactical.progression.RankLadder
import com.example.game.tactical.progression.RankRequirement
import com.example.game.tactical.progression.RankTier
import com.example.game.vanguard.core.GameErrorCategory
import com.example.game.vanguard.core.GameResult
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Phase 1 deliverable: the single source of truth for Rank, XP, Credits,
 * Inventory and Story Progress, plus a JSON save/load round trip.
 *
 * The manager is immutable - every operation returns a new profile - so a save
 * written mid-mission can never be half-applied.
 */
data class StoryProgress(
    val act: CampaignAct = CampaignAct.ACT_I,
    val chapter: Int = 1,
    val completedMissions: Int = 0
)

data class PlayerProfile(
    val callsign: String = "CANDIDATE",
    val xp: Int = 0,
    val credits: Int = 0,
    val rankIndex: Int = 0,
    val inventory: List<String> = emptyList(),
    val blueprints: List<String> = emptyList(),
    val satisfied: Set<RankRequirement> = setOf(RankRequirement.ENLISTMENT),
    val story: StoryProgress = StoryProgress(),
    val medals: List<Medal> = emptyList()
) {
    val rank: RankTier get() = RankLadder.tierAt(rankIndex)
}

data class Promotion(val from: RankTier, val to: RankTier, val privileges: List<String>)

data class MissionOutcome(
    val profile: PlayerProfile,
    val awards: List<Medal>,
    val promotion: Promotion?
)

object GameStateManager {

    const val SAVE_VERSION = 1

    fun fresh(callsign: String = "CANDIDATE"): PlayerProfile = PlayerProfile(callsign = callsign)

    fun awardXp(profile: PlayerProfile, amount: Int): PlayerProfile =
        profile.copy(xp = (profile.xp + amount.coerceAtLeast(0)).coerceAtLeast(0))

    fun awardCredits(profile: PlayerProfile, amount: Int): PlayerProfile =
        profile.copy(credits = (profile.credits + amount.coerceAtLeast(0)).coerceAtLeast(0))

    fun grantItem(profile: PlayerProfile, itemId: String): PlayerProfile =
        if (itemId in profile.inventory) profile
        else profile.copy(inventory = profile.inventory + itemId)

    fun grantBlueprint(profile: PlayerProfile, blueprintId: String): PlayerProfile =
        if (blueprintId in profile.blueprints) profile
        else profile.copy(blueprints = profile.blueprints + blueprintId)

    fun satisfy(profile: PlayerProfile, requirement: RankRequirement): PlayerProfile {
        if (requirement in profile.satisfied) return profile
        return profile.copy(satisfied = profile.satisfied + requirement)
    }

    /**
     * Promotion ceremony (Phase 6): the ladder decides the target rung, this
     * method only records it once per rung so the ceremony cannot replay.
     */
    fun promote(profile: PlayerProfile): Promotion? {
        val target = RankLadder.heldIndex(profile.xp, profile.satisfied)
        if (target <= profile.rankIndex) return null
        return Promotion(
            from = RankLadder.tierAt(profile.rankIndex),
            to = RankLadder.tierAt(target),
            privileges = RankLadder.privilegesBetween(profile.rankIndex, target)
        )
    }

    /**
     * Applies the end-of-mission screen: medals, then story progress, then XP.
     * Medals are recomputed from the record, never taken on trust.
     */
    fun applyMission(
        profile: PlayerProfile,
        record: MissionRecord,
        xpReward: Int,
        creditReward: Int
    ): MissionOutcome {
        val awards = MedalEvaluator.medals(record)
        val progressed = profile.copy(
            story = profile.story.copy(
                act = record.act,
                chapter = profile.story.chapter + 1,
                completedMissions = profile.story.completedMissions + 1
            ),
            medals = (profile.medals + awards).distinct()
        )
        val paid = awardCredits(awardXp(progressed, xpReward), creditReward)
        return MissionOutcome(
            profile = paid.copy(rankIndex = RankLadder.heldIndex(paid.xp, paid.satisfied)),
            awards = awards,
            promotion = promote(paid)
        )
    }

    // ------------------------------------------------------------ persistence

    fun save(profile: PlayerProfile): String = JSONObject().apply {
        put("version", SAVE_VERSION)
        put("callsign", profile.callsign)
        put("xp", profile.xp)
        put("credits", profile.credits)
        put("rankIndex", profile.rankIndex)
        put("inventory", JSONArray(profile.inventory))
        put("blueprints", JSONArray(profile.blueprints))
        put("satisfied", JSONArray(profile.satisfied.map { it.name }))
        put("medals", JSONArray(profile.medals.map { it.name }))
        put("act", profile.story.act.name)
        put("chapter", profile.story.chapter)
        put("completedMissions", profile.story.completedMissions)
    }.toString()

    fun load(json: String): GameResult<PlayerProfile> {
        val root = try {
            JSONObject(json)
        } catch (error: JSONException) {
            return corrupt("Save is not valid JSON: ${error.message}")
        }
        if (root.optInt("version", 0) != SAVE_VERSION) {
            return corrupt("Unsupported save version ${root.optInt("version", 0)}")
        }
        val xp = root.optInt("xp", -1)
        if (xp < 0) return corrupt("XP cannot be negative")
        val rankIndex = root.optInt("rankIndex", 0)
        if (rankIndex !in 0..RankLadder.maxIndex) return corrupt("Rank index $rankIndex out of range")
        val actName = root.optString("act", CampaignAct.ACT_I.name)
        val act = CampaignAct.entries.firstOrNull { it.name == actName } ?: CampaignAct.ACT_I

        return GameResult.Ok(
            PlayerProfile(
                callsign = root.optString("callsign", "CANDIDATE"),
                xp = xp,
                credits = root.optInt("credits", 0),
                rankIndex = rankIndex,
                inventory = strings(root, "inventory"),
                blueprints = strings(root, "blueprints"),
                satisfied = strings(root, "satisfied").mapNotNull { name ->
                    RankRequirement.entries.firstOrNull { it.name == name }
                }.toSet(),
                story = StoryProgress(
                    act = act,
                    chapter = root.optInt("chapter", 1).coerceAtLeast(1),
                    completedMissions = root.optInt("completedMissions", 0).coerceAtLeast(0)
                ),
                medals = strings(root, "medals").mapNotNull { name ->
                    Medal.entries.firstOrNull { it.name == name }
                }
            )
        )
    }

    private fun corrupt(message: String): GameResult<PlayerProfile> =
        GameResult.fail(GameErrorCategory.INVALID_STATE, "SAVE_CORRUPT", message)

    private fun strings(root: JSONObject, key: String): List<String> {
        val array = root.optJSONArray(key) ?: return emptyList()
        return (0 until array.length()).mapNotNull { array.optString(it, null) }
    }
}
