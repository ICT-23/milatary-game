package com.example.game.tactical.progression

/**
 * Section 3, "Medals & Honors System".
 *
 * Every ribbon is a pure predicate over one mission record, so the end-of-mission
 * screen can never award a medal the mission did not earn.
 */
enum class CampaignAct(val number: Int, val title: String) {
    ACT_I(1, "The Crucible"),
    ACT_II(2, "Baptism by Fire"),
    ACT_III(3, "Modern Warfare & Drone Command"),
    ACT_IV(4, "The Medal of Honor")
}

enum class MissionApproach { ASSAULT, STEALTH, DRONE_STRIKE }

data class MissionRecord(
    val missionId: String,
    val act: CampaignAct,
    val survived: Boolean,
    val accuracyPercent: Float,
    val headshots: Int,
    val lowestHealthPercent: Int,
    val traumaKitsApplied: Int,
    val approach: MissionApproach,
    val alarmTriggered: Boolean,
    val squadmatesSaved: Int,
    val squadmatesTotal: Int
)

enum class Medal(val displayName: String, val citation: String) {
    SHARPSHOOTER(
        "Sharpshooter Ribbon",
        "Mission completed with >= 85% accuracy and >= 5 headshots"
    ),
    PURPLE_HEART(
        "Purple Heart",
        "Survived after dropping below 15% health and applying a trauma kit"
    ),
    TACTICAL_GENIUS(
        "Tactical Genius Medal",
        "Outpost cleared by stealth or drone strike without triggering the alarm"
    ),
    MEDAL_OF_HONOR(
        "Medal of Honor",
        "All squadmates saved during a high-risk Act III or Act IV event"
    )
}

data class MedalAward(val medal: Medal, val reason: String)

object MedalEvaluator {

    const val SHARPSHOOTER_ACCURACY = 85f
    const val SHARPSHOOTER_HEADSHOTS = 5
    const val PURPLE_HEART_HEALTH = 15
    const val PURPLE_HEART_KITS = 1

    private fun sharpshooter(record: MissionRecord): MedalAward? {
        if (!record.survived) return null
        if (record.accuracyPercent < SHARPSHOOTER_ACCURACY) return null
        if (record.headshots < SHARPSHOOTER_HEADSHOTS) return null
        return MedalAward(
            Medal.SHARPSHOOTER,
            "%.1f%% accuracy with %d headshots".format(record.accuracyPercent, record.headshots)
        )
    }

    private fun purpleHeart(record: MissionRecord): MedalAward? {
        if (!record.survived) return null
        if (record.lowestHealthPercent >= PURPLE_HEART_HEALTH) return null
        if (record.traumaKitsApplied < PURPLE_HEART_KITS) return null
        return MedalAward(
            Medal.PURPLE_HEART,
            "Survived at ${record.lowestHealthPercent}% health with " +
                "${record.traumaKitsApplied} trauma kit(s)"
        )
    }

    private fun tacticalGenius(record: MissionRecord): MedalAward? {
        val quiet = record.approach == MissionApproach.STEALTH ||
            record.approach == MissionApproach.DRONE_STRIKE
        if (!quiet || record.alarmTriggered) return null
        return MedalAward(Medal.TACTICAL_GENIUS, "Cleared via ${record.approach.name.lowercase()}, no alarm")
    }

    private fun medalOfHonor(record: MissionRecord): MedalAward? {
        val highRiskAct = record.act == CampaignAct.ACT_III || record.act == CampaignAct.ACT_IV
        if (!highRiskAct) return null
        if (record.squadmatesTotal <= 0) return null
        if (record.squadmatesSaved < record.squadmatesTotal) return null
        return MedalAward(
            Medal.MEDAL_OF_HONOR,
            "All ${record.squadmatesTotal} squadmates extracted in ${record.act.title}"
        )
    }

    /** Awards in presentation order: lowest ribbon first, Medal of Honor last. */
    fun evaluate(record: MissionRecord): List<MedalAward> = listOfNotNull(
        sharpshooter(record),
        purpleHeart(record),
        tacticalGenius(record),
        medalOfHonor(record)
    )

    fun medals(record: MissionRecord): List<Medal> = evaluate(record).map { it.medal }

    fun has(record: MissionRecord, medal: Medal): Boolean = medals(record).contains(medal)

    /** The end-of-mission ribbon wall: earned medals plus the ones still owed. */
    fun ribbonWall(record: MissionRecord): List<Pair<Medal, Boolean>> =
        Medal.entries.map { medal -> medal to has(record, medal) }
}
