package com.example.game.valor.balance

/** Section 7 legend: the thirteen mission types and their three-letter codes. */
enum class MissionType(val code: String, val displayName: String) {
    TUT("TUT", "Tutorial"),
    ASL("ASL", "Assault"),
    DEF("DEF", "Defend"),
    SNP("SNP", "Sniper overwatch"),
    STL("STL", "Stealth"),
    ESC("ESC", "Escort"),
    RES("RES", "Rescue"),
    VEH("VEH", "Vehicle"),
    RAIL("RAIL", "Rail shooter"),
    DEM("DEM", "Demolition"),
    BOSS("BOSS", "Boss"),
    DRN("DRN", "Drone command"),
    MIX("MIX", "Mixed")
}

/**
 * Section 7: one mission row verbatim. [baseXp] is what the mission pays before
 * kills and bonuses; kills add the share documented by [MissionCatalog] and the
 * star multiplier from section 5 is applied on top.
 */
data class MissionSpec(
    val number: Int,
    val act: Int,
    val name: String,
    val type: MissionType,
    val location: String,
    val objectives: String,
    val baseXp: Int,
    val cash: Int,
    val unlock: String
) {
    val id: String get() = "m" + number.toString().padStart(2, '0')
}

/** A row of the "Side content (always available from Academy hub)" table. */
data class SideContentSpec(val name: String, val rewards: String)

private val TUT = MissionType.TUT
private val ASL = MissionType.ASL
private val DEF = MissionType.DEF
private val SNP = MissionType.SNP
private val STL = MissionType.STL
private val ESC = MissionType.ESC
private val RES = MissionType.RES
private val VEH = MissionType.VEH
private val RAIL = MissionType.RAIL
private val DEM = MissionType.DEM
private val BOSS = MissionType.BOSS
private val DRN = MissionType.DRN
private val MIX = MissionType.MIX

private fun m(
    number: Int,
    act: Int,
    name: String,
    type: MissionType,
    location: String,
    objectives: String,
    baseXp: Int,
    cash: Int,
    unlock: String
) = MissionSpec(number, act, name, type, location, objectives, baseXp, cash, unlock)

object MissionCatalog {

    /** "Base XP is before kills/bonus (kills add ≈ 25-35%)." */
    const val KILL_XP_MIN_SHARE = 0.25f
    const val KILL_XP_MAX_SHARE = 0.35f

    const val TOTAL_MISSIONS = 60
    const val ACT_COUNT = 6

    val MISSIONS: List<MissionSpec> = listOf(
        // ACT 1 - BOOT CAMP (Fort Valor Academy) - Ranks 1-3 - approx 900 XP
        m(1, 1, "Arrival Day", TUT, "Barracks yard", "Move, look, interact; meet Brooks, Lena, Vance", 30, 80, "Character creator saved"),
        m(2, 1, "Obstacle Course", TUT, "Training grounds", "Sprint, vault, crawl; finish in time (generous)", 40, 90, "Stamina stat"),
        m(3, 1, "Rifle Range", TUT, "Range 1", "Hit 10 targets, reload, swap to pistol", 60, 100, "Armory opened"),
        m(4, 1, "Draw & Fire", TUT, "Range 2", "Pistol quick-draw drill, headshot targets", 70, 110, "Revolver blueprint piece"),
        m(5, 1, "Grenade Basics", TUT, "Range 3", "Throw frag at 3 zones; smoke drill", 80, 120, "Frag grenade"),
        m(6, 1, "Cover & Move", ASL, "Live-fire course", "Use cover, clear 8 pop-up targets", 90, 140, "Cover tutorial done"),
        m(7, 1, "Medic Drill", TUT, "Field hospital", "Heal self, revive dummy, carry", 90, 150, "Medkit"),
        m(8, 1, "Night Exercise", STL, "Forest course", "Reach checkpoint unseen; silent takedown drill", 120, 170, "Stealth tutorial"),
        m(9, 1, "Squad Exercise", DEF, "Mock village", "Defend flag vs practice enemies (non-lethal, paint rounds)", 150, 200, "Squad concept"),
        m(10, 1, "Graduation Day", MIX, "Academy grounds", "Exam: range + course + defend; Vance cheats -> choice", 200, 250, "Rank -> PFC; Act 2 opens"),

        // ACT 2 - THE GREAT WAR (trenches) - Ranks 4-6 - approx 5,600 XP
        m(11, 2, "Mud Road", ASL, "Supply road", "Clear ambush, reach trench", 300, 320, "Trench SMG"),
        m(12, 2, "Over the Top", ASL, "No-man's-land", "Cross field, take first trench; smoke use", 400, 380, "Smoke grenade"),
        m(13, 2, "Wire Cutters", DEM, "Barbed-wire line", "Cut 3 gaps under fire", 450, 420, "Gas threat introduced"),
        m(14, 2, "Gas Alarm", DEF, "Command dugout", "Defend while gas cloud rolls in (mask timer)", 500, 470, "Gas mask"),
        m(15, 2, "Machine-Gun Nest", ASL, "Hill 204", "Flank & destroy 2 MG nests", 550, 520, "LMG blueprint"),
        m(16, 2, "Night Raid", STL, "Enemy trench", "Silent entry, steal maps, escape", 600, 580, "Suppressor blueprint"),
        m(17, 2, "Farmhouse Defense", DEF, "Ruined farm", "Hold 4 waves", 650, 640, "Shotgun"),
        m(18, 2, "Brooks Down", RES, "Trench hospital", "Rescue wounded Brooks under fire", 700, 700, "Story: Brooks wounded"),
        m(19, 2, "Bunker Sweep", ASL, "Fortified bunkers", "Clear 3 bunkers, capture orders", 800, 800, "Staff Sergeant rank"),
        m(20, 2, "Trench Butcher", BOSS, "Main trench line", "Defeat armored heavy; use grenades", 1_000, 900, "Boss 1 - Kessler seen"),

        // ACT 3 - INTERWAR STORM - Ranks 7-10 - approx 20,000 XP
        m(21, 3, "Officer Trials", MIX, "Officer school", "Leadership exam: command 2 AI", 1_200, 900, "Officer path"),
        m(22, 3, "Old Town Patrol", ASL, "European town", "Clear patrol, capture radio", 1_400, 1_000, "Semi-auto rifle"),
        m(23, 3, "Train Station Chase", RAIL, "Train roof", "Gunner on moving train, protect carriage", 1_600, 1_100, "First rail mission"),
        m(24, 3, "Snow Pass Ambush", DEF, "Mountain pass", "Hold pass 5 waves, sniper threat", 1_800, 1_200, "Sniper Longwatch"),
        m(25, 3, "Docks Stealth", STL, "Harbor", "Sabotage 3 cranes unseen", 2_000, 1_400, "Silent takedown skill"),
        m(26, 3, "Informant Escort", ESC, "City alleys", "Escort informant to safehouse", 2_000, 1_500, "Choice: trust informant"),
        m(27, 3, "Rooftop Overwatch", SNP, "City rooftops", "Protect convoy with sniper", 2_200, 1_600, "Spotter mechanic"),
        m(28, 3, "Armored Car Heist", VEH, "Highway", "Chase & stop armored car", 2_400, 1_800, "Vehicle basics"),
        m(29, 3, "Vance Showdown", MIX, "Warehouse", "Vance ally / rival path (choice-dependent)", 2_600, 2_000, "Vance arc resolves"),
        m(30, 3, "Chapel Duel", BOSS, "Ruined chapel", "Duel young Kessler in 3 phases", 3_000, 2_200, "Boss 2 - Lieutenant"),

        // ACT 4 - WORLD AT WAR - Ranks 11-13 - approx 26,500 XP
        m(31, 4, "Beach Landing", ASL, "Coastal beach", "Storm beach, take bunkers", 2_200, 2_200, "Artillery call-in"),
        m(32, 4, "Hedgerow Push", ASL, "Hedgerow fields", "Advance field by field", 2_300, 2_400, "Squad commands"),
        m(33, 4, "Bombed City Street", MIX, "Ruined city", "Clear block, rescue civilians", 2_400, 2_600, "Civilian rules"),
        m(34, 4, "Tank Support", VEH, "City outskirts", "Escort tank; destroy AT guns", 2_500, 2_800, "Tank gunner"),
        m(35, 4, "Airfield Defense", DEF, "Airfield", "Defend against waves + air raid", 2_600, 3_000, "Anti-air gun"),
        m(36, 4, "Bridge Demolition", DEM, "River bridge", "Plant charges; hold; extract", 2_700, 3_200, "Demolition skill"),
        m(37, 4, "Sniper Alley", SNP, "Ruined street", "Eliminate enemy sniper team", 2_800, 3_400, "DMR blueprint"),
        m(38, 4, "Lena's Stand", DEF, "Church tower", "Protect Lena; choice decides her fate", 3_000, 3_600, "Story: Lena"),
        m(39, 4, "Fortress Assault", ASL, "Fortress outer", "Breach multiple gates", 3_200, 3_800, "Launcher"),
        m(40, 4, "Fortress Commander", BOSS, "Fortress bunker", "Multi-wave + rooftop boss", 4_000, 5_000, "Boss 3 - Medal nomination"),

        // ACT 5 - COLD SHADOWS - Ranks 14-15 - approx 24,000 XP
        m(41, 5, "Desert Convoy", VEH, "Desert road", "Protect convoy; drive lead truck", 2_000, 4_500, "Desert gear"),
        m(42, 5, "Canyon Recon", STL, "Canyon", "Mark 5 targets undetected", 2_100, 4_800, "Binoculars use"),
        m(43, 5, "Refinery Infiltration", MIX, "Oil refinery", "Sabotage with fire hazards", 2_200, 5_200, "Explosive barrels"),
        m(44, 5, "Night Vision Raid", ASL, "Night city", "Clear compound in darkness", 2_300, 5_600, "Night vision"),
        m(45, 5, "Helicopter Gunner", RAIL, "Helicopter", "Door gunner support run", 2_400, 6_000, "Air support"),
        m(46, 5, "Arctic Approach", ASL, "Ice shelf", "Cross ice field, blizzard visibility", 2_500, 6_400, "Cold effects"),
        m(47, 5, "Ghost Sniper", BOSS, "Snowstorm", "Duel enemy sniper", 2_800, 7_200, "Boss 4"),
        m(48, 5, "Leak Investigation", MIX, "HQ + safehouse", "Search clues, interrogate, choice", 2_600, 7_600, "Betrayal plot"),
        m(49, 5, "Safehouse Defense", DEF, "Safehouse", "Hold against assault", 2_700, 8_000, "Squad upgrades"),
        m(50, 5, "The Traitor", MIX, "Command base", "Confront traitor (choice: arrest/spare)", 3_000, 9_000, "Colonel"),

        // ACT 6 - DRONE AGE - Ranks 16-19 - approx 63,000 XP total (with replay)
        m(51, 6, "ARIA Awakens", DRN, "Tech base", "Learn drone control", 5_000, 8_000, "Drone strike"),
        m(52, 6, "Skyline Rooftops", SNP, "Megacity", "Overwatch + drone recon", 5_500, 9_000, "Recon drone"),
        m(53, 6, "Swarm Defense", DEF, "Rooftop base", "Counter enemy drones", 6_000, 10_000, "Defense drone"),
        m(54, 6, "Carrier Deck", ASL, "Aircraft carrier", "Board and clear", 6_500, 11_000, "Naval set"),
        m(55, 6, "Comms Blackout", STL, "Radar station", "Silent infiltration, EMP", 7_000, 12_000, "EMP device"),
        m(56, 6, "Drone Factory", MIX, "Factory", "Destroy production lines", 7_500, 13_000, "Swarm drone"),
        m(57, 6, "FPV Strike", DRN, "Canyon fortress", "FPV pilot through tunnels", 8_000, 14_000, "FPV mode"),
        m(58, 6, "Command Bunker", ASL, "Mountain bunker", "Multi-floor assault", 8_500, 15_000, "Titan blueprint"),
        m(59, 6, "Kessler's Gambit", MIX, "Network hub", "Multi-stage: hack, defend, escape", 9_000, 16_000, "Final prep"),
        m(60, 6, "Final Duel: Kessler", BOSS, "Arena", "4-phase boss + drone swarm -> Medal of Honor", 12_000, 20_000, "Endings, New Game+")
    )

    val SIDE_CONTENT: List<SideContentSpec> = listOf(
        SideContentSpec("Tournaments (Bronze/Silver/Gold)", "200-2,000 Cash, mastery XP, medals"),
        SideContentSpec("Survival", "Cash/XP by wave, leaderboard"),
        SideContentSpec("Daily Operation", "Crate + medals"),
        SideContentSpec("Weekly Challenge", "Medals + skin pieces"),
        SideContentSpec("Boss Rush", "Rare crate"),
        SideContentSpec("Mission Mastery (3 stars all)", "Skins + titles")
    )

    fun forAct(act: Int): List<MissionSpec> = MISSIONS.filter { it.act == act }

    fun byNumber(number: Int): MissionSpec? = MISSIONS.firstOrNull { it.number == number }

    fun byId(id: String): MissionSpec? = MISSIONS.firstOrNull { it.id == id }

    fun byType(type: MissionType): List<MissionSpec> = MISSIONS.filter { it.type == type }

    /** Every type in the legend is actually used by at least one mission. */
    fun typeCounts(): Map<MissionType, Int> =
        MissionType.entries.associateWith { type -> MISSIONS.count { it.type == type } }

    /** Base XP a full act pays before kills, stars and first-clear bonus. */
    fun baseXpTotal(act: Int): Int = forAct(act).sumOf { it.baseXp }

    fun cashTotal(act: Int): Int = forAct(act).sumOf { it.cash }

    /**
     * Section 7 base cash against the section 6.1 band. The finale is the one
     * row above its act's ceiling, which this reports instead of hiding.
     */
    fun missionsOutsideIncomeBand(): List<MissionSpec> =
        MISSIONS.filter { !MissionIncome.isBaseInBand(it.act, it.cash) }

    /** Missions are numbered 1..60 with no gaps, which the audit asserts. */
    fun isContiguous(): Boolean = MISSIONS.map { it.number } == (1..MISSIONS.size).toList()

    /** Boss missions must line up with the story beats marked "Boss" in section 7. */
    fun bossMissions(): List<MissionSpec> = byType(MissionType.BOSS)
}
