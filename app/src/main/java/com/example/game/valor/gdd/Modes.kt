package com.example.game.valor.gdd

/** Section 7.1: one of the eleven campaign mission types. */
data class MissionTypeRow(val code: String, val name: String, val objective: String)

object MissionTypes {

    /** Section 7.1, in the order the document numbers them. */
    val TYPES: List<MissionTypeRow> = listOf(
        MissionTypeRow("ASL", "Assault", "Clear an enemy position"),
        MissionTypeRow("DEF", "Defend", "Hold a point against waves"),
        MissionTypeRow("SNP", "Sniper overwatch", "Protect allies from a rooftop"),
        MissionTypeRow("STL", "Stealth infiltration", "Silent takedowns, avoid alarms"),
        MissionTypeRow("ESC", "Escort / Convoy", "Protect a VIP or vehicle"),
        MissionTypeRow("RES", "Rescue", "Extract hostages / wounded"),
        MissionTypeRow("VEH", "Vehicle", "Drive jeep/tank/boat sequences"),
        MissionTypeRow("RAIL", "Rail shooter", "Gunner seat on truck/boat/helicopter"),
        MissionTypeRow("DEM", "Demolition", "Plant charges, defuse"),
        MissionTypeRow("BOSS", "Boss fight", "Enemy commander with unique mechanics"),
        MissionTypeRow("DRN", "Drone command", "Direct strikes from the map (late game)")
    )

    fun byCode(code: String): MissionTypeRow? =
        TYPES.firstOrNull { it.code.equals(code.trim(), ignoreCase = true) }

    fun byName(name: String): MissionTypeRow? =
        TYPES.firstOrNull { it.name.equals(name.trim(), ignoreCase = true) }

    fun count(): Int = TYPES.size
}

/** Section 7.1: "~60 missions over 6 Acts, 5-12 minutes each." */
object CampaignShape {

    const val APPROX_MISSIONS = 60
    const val ACTS = 6
    const val MIN_MINUTES = 5
    const val MAX_MINUTES = 12

    fun missionsPerAct(): Int = APPROX_MISSIONS / ACTS

    fun inTimeWindow(minutes: Int): Boolean = minutes in MIN_MINUTES..MAX_MINUTES
}

/**
 * Section 7.2: one Academy activity. [timed] and [scalesWithRank] carry the two
 * rules the document states about timers; [firstAttemptGenerous] only applies to
 * the tournament row, which the doc calls out by name.
 */
data class AcademyActivity(
    val name: String,
    val detail: String,
    val timed: Boolean,
    val scalesWithRank: Boolean = false,
    val firstAttemptGenerous: Boolean = false
)

object Academy {

    val ACTIVITIES: List<AcademyActivity> = listOf(
        AcademyActivity("Shooting range", "Accuracy and speed drills", timed = true),
        AcademyActivity(
            "Tournaments", "Target shooting with a fair timer scaled to rank",
            timed = true, scalesWithRank = true, firstAttemptGenerous = true
        ),
        AcademyActivity("Obstacle course", "Timed run", timed = true),
        AcademyActivity("Tactics classroom", "Quiz minigame, gives an intel bonus", timed = false),
        AcademyActivity("Gym", "Stat boosts: stamina, steadiness, strength", timed = false)
    )

    fun byName(name: String): AcademyActivity? =
        ACTIVITIES.firstOrNull { it.name.equals(name.trim(), ignoreCase = true) }

    fun timed(): List<AcademyActivity> = ACTIVITIES.filter { it.timed }

    /** Section 7.2: the first tournament must be generous, never impossible. */
    fun tournament(): AcademyActivity = ACTIVITIES.first { it.name == "Tournaments" }
}

/** Section 7.3: the six replay modes. */
data class ReplayMode(val name: String, val detail: String)

/** Section 7.3 "Weekly Challenge": the three printed modifiers. */
enum class ChallengeModifier(val displayName: String) {
    HEADSHOTS_ONLY("Headshots only"),
    ONE_HIT_KILL("One-hit-kill"),
    LOW_AMMO("Low ammo")
}

object ReplayModes {

    val MODES: List<ReplayMode> = listOf(
        ReplayMode("Survival / Endless waves", "Leaderboards"),
        ReplayMode("Daily Operation", "One special mission per day with a fixed loadout"),
        ReplayMode("Weekly Challenge", "Modifiers: headshots only, one-hit-kill, low ammo"),
        ReplayMode("New Game+ / Prestige", "Keep skins, harder enemies, new dialogue"),
        ReplayMode("Boss Rush", "All bosses back to back"),
        ReplayMode("Mission Mastery", "3 stars per mission: time, accuracy, no damage")
    )

    val MODIFIERS: List<ChallengeModifier> = ChallengeModifier.entries

    fun byName(name: String): ReplayMode? =
        MODES.firstOrNull { it.name.equals(name.trim(), ignoreCase = true) }

    fun count(): Int = MODES.size
}

/**
 * Section 7.3 "Mission Mastery": one star per printed criterion, three in total.
 * Thresholds come from mission data, so the evaluator only takes the verdicts.
 */
object MissionMastery {

    const val STARS_PER_MISSION = 3

    val CRITERIA: List<String> = listOf("Time", "Accuracy", "No damage")

    fun stars(timeMet: Boolean, accuracyMet: Boolean, noDamage: Boolean): Int =
        listOf(timeMet, accuracyMet, noDamage).count { it }

    fun stars(elapsedSeconds: Float, timeTargetSeconds: Float, accuracyPercent: Int, accuracyTargetPercent: Int, tookDamage: Boolean): Int =
        stars(
            timeMet = elapsedSeconds <= timeTargetSeconds,
            accuracyMet = accuracyPercent >= accuracyTargetPercent,
            noDamage = !tookDamage
        )

    fun isPerfect(stars: Int): Boolean = stars == STARS_PER_MISSION
}

/** Section 7.5: the five boss encounters. */
data class BossEncounter(
    val name: String,
    val mechanics: String,
    val act: Int? = null
)

object BossRoster {

    /**
     * Section 7.5 lists five bosses; only the acts the document states are filled
     * in, so the roster never places an encounter on a guess.
     */
    val BOSSES: List<BossEncounter> = listOf(
        BossEncounter("Trench Butcher", "Armored heavy with machine gun", act = 2),
        BossEncounter("Colonel Kessler (young)", "Duel in a ruined chapel"),
        BossEncounter("Fortress Commander", "Multi-wave bunker defense + rooftop sniper", act = 4),
        BossEncounter("Ghost Sniper", "Cat-and-mouse in a snowstorm", act = 5),
        BossEncounter("Kessler (final)", "Drone swarm + arena fight", act = 6)
    )

    fun byName(name: String): BossEncounter? =
        BOSSES.firstOrNull { it.name.equals(name.trim(), ignoreCase = true) }

    fun forAct(act: Int): List<BossEncounter> = BOSSES.filter { it.act == act }

    /** Boss Rush fields every row back to back. */
    fun rushOrder(): List<BossEncounter> = BOSSES
}
