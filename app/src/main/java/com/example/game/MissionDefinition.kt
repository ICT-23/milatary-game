package com.example.game

data class DialogueLine(
    val speaker: String,
    val speakerRole: String,
    val text: String,
    val speakerColorHex: Long = 0xFFE7A83E
)

data class MissionChoice(
    val title: String,
    val description: String,
    val optionA: ChoiceOption,
    val optionB: ChoiceOption
)

data class ChoiceOption(
    val id: String,
    val label: String,
    val summary: String,
    val outcomeDialogue: DialogueLine,
    val vossTrustDelta: Int,
    val idrisRespectDelta: Int,
    val kadeRivalryDelta: Int,
    /** Cipher intelligence this path adds to the campaign profile. */
    val intelDelta: Int = 0
)

data class MissionDossier(
    val id: String,
    val chapterNumber: Int,
    val title: String,
    val subtitle: String,
    val location: String,
    val briefingOverview: String,
    val primaryObjective: String,
    val secondaryObjective: String,
    val estimatedDurationMinutes: Int,
    val recommendedRank: String,
    val rewardXp: Int,
    /** Field time that still earns the speed star. Mirrors parSeconds in levels.js. */
    val parTimeSeconds: Int = 240,
    /**
     * Enemy composition the briefing warns about. Mirrors the `kind` values in
     * levels.js so the profile never promises something the level does not spawn.
     */
    val hostileIntel: List<String> = emptyList(),
    /**
     * Ordered objective chain exactly as the design document lists it. The live
     * chain in GameEngine advances through the same steps one for one.
     */
    val objectiveSequence: List<String> = emptyList()
)

/**
 * Star rule for every chapter. Kept in Kotlin so results are authoritative even
 * if the embedded engine reports something different.
 *
 *  1 star  - chapter completed
 *  2 stars - completed with at least 60% health remaining
 *  3 stars - additionally finished inside the par time
 */
object MissionRating {
    const val HEALTH_THRESHOLD = 60

    fun stars(healthPercent: Int, elapsedSeconds: Int, parTimeSeconds: Int): Int {
        var stars = 1
        if (healthPercent >= HEALTH_THRESHOLD) stars++
        if (elapsedSeconds in 1..parTimeSeconds) stars++
        return stars.coerceIn(1, 3)
    }

    fun titleFor(stars: Int): String = when (stars.coerceIn(1, 3)) {
        3 -> "DISTINGUISHED"
        2 -> "EFFECTIVE"
        else -> "SURVIVED"
    }
}

/**
 * Display copy for the branching decisions the 3D engine reports back by id.
 * The engine owns the gameplay consequences; this table owns the debrief text.
 */
data class ChoiceBrief(
    val id: String,
    val label: String,
    val summary: String,
    val speaker: String,
    val outcome: String
)

object ChoiceCatalog {
    private val ENTRIES = listOf(
        ChoiceBrief(
            id = "SAFEGUARD_CIVILIANS",
            label = "SAFEGUARD CIVILIANS",
            summary = "Vented the superheated coolant away from the shelter. No lives were lost.",
            speaker = "Idris Kane (Medic)",
            outcome = "All forty families are safe. Now proceed to LZ Falcon for extraction!"
        ),
        ChoiceBrief(
            id = "INTERCEPT_DATA",
            label = "INTERCEPT CIPHER DATA",
            summary = "Overclocked the receiver to capture enemy frequency codes. The bunker took heat damage.",
            speaker = "Rowan Kade (Rival)",
            outcome = "You captured the saboteurs' encrypted frequency codes before they wiped the server."
        ),
        ChoiceBrief(
            id = "DETAIN_KADE",
            label = "DETAIN KADE",
            summary = "Placed Rowan under arrest and handed the ledger to command.",
            speaker = "Captain Mara Voss",
            outcome = "Kade is in restraints and the ledger is in my hands. That is how a soldier handles a compromised operative."
        ),
        ChoiceBrief(
            id = "COVER_KADE",
            label = "COVER FOR KADE",
            summary = "Buried his name in the report and kept him in the field as an asset.",
            speaker = "Rowan Kade (Rival)",
            outcome = "You buried my name in that report. I do not forget debts, Spectre."
        ),
        ChoiceBrief(
            id = "BROADCAST_TRUTH",
            label = "BROADCAST THE TRUTH",
            summary = "Sent the architect's full confession coast to coast on every band.",
            speaker = "Captain Mara Voss",
            outcome = "It is going out on every band. Half the coast is listening to a dead man confess."
        ),
        ChoiceBrief(
            id = "DESTROY_ARRAY",
            label = "DESTROY THE ARRAY",
            summary = "Burned the transmission rig with the architect aboard. No EMP, no witnesses.",
            speaker = "Rowan Kade (Rival)",
            outcome = "Array is scrap and the architect went down with it. The EMP can never fire."
        ),
        ChoiceBrief(
            id = "TAKE_PRISONERS",
            label = "TAKE THEM PRISONERS",
            summary = "Bound the estuary survivors and handed them to the tribunal alive.",
            speaker = "Idris Kane (Medic)",
            outcome = "Eleven sets of restraints and one working radio. They will talk under oath before the tribunal sits."
        ),
        ChoiceBrief(
            id = "BURN_HIDE",
            label = "BURN THE HIDE",
            summary = "Torched the south hide with the cell still inside. No testimony, no names.",
            speaker = "Rowan Kade (Rival)",
            outcome = "The hide is ash and the network died with it. Command will not ask how."
        ),
        ChoiceBrief(
            id = "SEAL_ARCHIVE",
            label = "SEAL THE ARCHIVE",
            summary = "Locked the academy archive for fifty years to spare the name.",
            speaker = "Rowan Kade (Rival)",
            outcome = "Archive sealed, cadets safe, and the name stays clean. Someone will open that door eventually."
        )
    )

    fun byId(id: String?): ChoiceBrief? = ENTRIES.firstOrNull { it.id == id }
}

object MissionCatalog {
    val ALL_MISSIONS = listOf(
        MissionDossier(
            id = "M1_SIGNAL_IN_DUST",
            chapterNumber = 1,
            title = "Signal in the Dust",
            subtitle = "Sector 4 Coastal Relay Outpost",
            location = "Cape Oakhaven, Republic of Asterra",
            briefingOverview = "What began as a routine cadet field exercise has abruptly escalated. All regional telemetry nodes went dark 12 minutes ago. Captain Voss has ordered Spectre-7 to link up with Recon Specialist Tomas, scan the damaged tower array, and discover the source of the blackout.",
            primaryObjective = "Inspect Relay Tower Alpha and secure regional communications.",
            secondaryObjective = "Resolve emergency protocol without losing vital intelligence.",
            estimatedDurationMinutes = 8,
            recommendedRank = "Junior Cadet",
            rewardXp = 250,
            parTimeSeconds = 210,
            hostileIntel = listOf(
                "3 x SCOUT DRONE - hovering quad-rotors with light pulse guns"
            ),
            objectiveSequence = listOf(
                "Link up with Recon Specialist",
                "Scan Damaged Relay Tower Alpha",
                "Investigate Overloading Generator",
                "Resolve Override Dilemma",
                "Extract at LZ Falcon",
                "After-Action Report"
            )
        ),
        MissionDossier(
            id = "M2_BROKEN_RELAY",
            chapterNumber = 2,
            title = "Broken Relay",
            subtitle = "Subterranean Cable Vaults",
            location = "Garrison Perimeter, Sector 5",
            briefingOverview = "Sabotage signatures confirm the blackout was deliberate. Engineer Leena Suri has pinpointed spliced fiber lines beneath the old garrison. Infiltrate the maintenance conduits, repair the relays, and expose the tampering.",
            primaryObjective = "Restore subterranean telemetry uplinks.",
            secondaryObjective = "Recover intact optical splicing tools.",
            estimatedDurationMinutes = 12,
            recommendedRank = "Cadet Operative",
            rewardXp = 350,
            parTimeSeconds = 300,
            hostileIntel = listOf(
                "3 x SCOUT DRONE - vault patrol on a tight alert radius",
                "1 x SENTINEL TURRET - fixed emplacement, fast cyclic fire"
            )
        ),
        MissionDossier(
            id = "M3_SILENT_CROSSING",
            chapterNumber = 3,
            title = "Silent Crossing",
            subtitle = "Tidal Salt Flats & Marshlands",
            location = "Oakhaven Estuary",
            briefingOverview = "A hostile reconnaissance patrol has locked down the main highway bridge. Cross the mist-covered salt flats using cover and timing, coordinating with Tomas to avoid triggering automated surveillance beacons.",
            primaryObjective = "Navigate the estuary unseen to the rally point.",
            secondaryObjective = "Disable patrol sensor nodes along the dunes.",
            estimatedDurationMinutes = 14,
            recommendedRank = "Cadet Operative",
            rewardXp = 450,
            parTimeSeconds = 330,
            hostileIntel = listOf(
                "3 x SCOUT DRONE - salt-flat patrol screen",
                "1 x SENTINEL TURRET - dug in beside the causeway",
                "1 x WALKER - armoured biped that closes to short range"
            )
        ),
        MissionDossier(
            id = "M4_THE_LONG_RETURN",
            chapterNumber = 4,
            title = "The Long Return",
            subtitle = "Evacuation Escort",
            location = "North Ridge Highway",
            briefingOverview = "Civilian transport convoys are bottlenecked along the coastal pass. With Medic Idris managing casualties, your squad must repel interceptor probes and guide the convoy across the damaged trestle bridge.",
            primaryObjective = "Escort the evacuation convoy safely to Fort Vane.",
            secondaryObjective = "Prevent critical damage to medical transport vehicles.",
            estimatedDurationMinutes = 15,
            recommendedRank = "Field Operative",
            rewardXp = 550,
            parTimeSeconds = 360,
            hostileIntel = listOf(
                "4 x INTERCEPTOR PROBE - highway hunters with escalating damage",
                "1 x SENTINEL TURRET - overwatch on the eastern verge",
                "1 x WALKER - armoured biped working the median"
            )
        ),
        MissionDossier(
            id = "M5_FAULT_LINES",
            chapterNumber = 5,
            title = "Fault Lines",
            subtitle = "Investigative Infiltration",
            location = "Abandoned Mining District 9",
            briefingOverview = "Conflicting logs reveal military-grade detonators were sourced from within Asterra's own defense reserve. Confront Rowan Kade regarding leaked frequency codes and uncover who issued the unauthorized clearance.",
            primaryObjective = "Access the encrypted terminal inside Mine Shaft 3.",
            secondaryObjective = "Resolve the confrontation with Rowan without squad fracture.",
            estimatedDurationMinutes = 16,
            recommendedRank = "Field Operative",
            rewardXp = 650,
            parTimeSeconds = 390,
            hostileIntel = listOf(
                "4 x SECURITY DRONE - shaft lockdown detail",
                "1 x SENTINEL TURRET - covering the access drift",
                "1 x WALKER - armoured biped in the yard"
            )
        ),
        MissionDossier(
            id = "M6_EMBER_PROTOCOL",
            chapterNumber = 6,
            title = "Ember Protocol",
            subtitle = "Fortress Defense",
            location = "Fort Vane Signal Tower",
            briefingOverview = "The rogue faction launches a coordinated assault to dismantle Asterra's central emergency grid before the ceasefire committee convenes. Hold the perimeter using earlier tactical alliances and defense fortifications.",
            primaryObjective = "Defend the central relay transmitter against 3 assault waves.",
            secondaryObjective = "Keep generator cooling manifolds intact.",
            estimatedDurationMinutes = 18,
            recommendedRank = "Senior Operative",
            rewardXp = 800,
            parTimeSeconds = 420,
            hostileIntel = listOf(
                "8 x SCOUT DRONE - three assault waves on the transmitter",
                "2 x SENTINEL TURRET - pre-sited on the courtyard walls",
                "1 x WALKER - armoured biped leading the final wave"
            )
        ),
        MissionDossier(
            id = "M7_LAST_SIGNAL",
            chapterNumber = 7,
            title = "Last Signal",
            subtitle = "The Climax of Asterra",
            location = "Offshore Radar Platform Chimera",
            briefingOverview = "The mastermind's offshore transmission rig is preparing to trigger a false-flag EMP strike across the coastal republic. Lead the Emberwatch squad on an all-or-nothing boarding mission to transmit the authentic truth to the world.",
            primaryObjective = "Neutralize the transmission array and apprehend the architect.",
            secondaryObjective = "Extract all squad members before platform scuttle.",
            estimatedDurationMinutes = 20,
            recommendedRank = "Emberwatch Vanguard",
            rewardXp = 1000,
            parTimeSeconds = 480,
            hostileIntel = listOf(
                "5 x ELITE GUARD DRONE - fast, accurate, heavy hitters",
                "2 x SENTINEL TURRET - deck emplacements with 24 m reach",
                "1 x WALKER - armoured biped patrolling the transmission deck",
                "1 x ARCHITECT COMMAND FRAME - boss unit, deploys escorts at 66% and 33% hull"
            )
        ),
        MissionDossier(
            id = "M8_ASHES_OF_CHIMERA",
            chapterNumber = 8,
            title = "Ashes of Chimera",
            subtitle = "The Evidence Goes Down With The Rig",
            location = "Offshore Platform Chimera - Lower Decks",
            briefingOverview = "Chimera is scuttling itself, and the flight recorder on the lower deck is the only surviving proof of the false-flag EMP order. Fight down a deck that is already flooding and pull the recorder before it reaches the seabed.",
            primaryObjective = "Recover the flight recorder from the sensor mast.",
            secondaryObjective = "Reach the helipad before the platform goes under.",
            estimatedDurationMinutes = 15,
            recommendedRank = "Emberwatch Vanguard",
            rewardXp = 1100,
            parTimeSeconds = 300,
            hostileIntel = listOf(
                "4 x RETENTION DRONE - pursuit squad holding the stairwell",
                "1 x SENTINEL TURRET - lockdown emplacement on the deck spine",
                "1 x WALKER - armoured biped covering the lower deck"
            )
        ),
        MissionDossier(
            id = "M9_THE_QUIET_COAST",
            chapterNumber = 9,
            title = "The Quiet Coast",
            subtitle = "Two Cells, One Causeway",
            location = "Oakhaven Estuary - Surviving Cells",
            briefingOverview = "Two cells escaped Chimera before it sank and dug into the estuary hides with the names of everyone who signed the EMP order. Take the hides apart, then hold the causeway bridge against the counter-attack.",
            primaryObjective = "Clear both estuary hides and hold the bridge.",
            secondaryObjective = "Recover the signatory list before the cells can burn it.",
            estimatedDurationMinutes = 18,
            recommendedRank = "Emberwatch Vanguard",
            rewardXp = 1200,
            parTimeSeconds = 360,
            hostileIntel = listOf(
                "8 x CELL DRONE - radio cell in the north hide, gun crew in the south",
                "1 x SENTINEL TURRET - covers the north hide approach",
                "1 x WALKER - armoured biped anchoring the south hide",
                "3 x COUNTER-ATTACK WAVES - two, three then four hostiles on the bridge"
            )
        ),
        MissionDossier(
            id = "M10_EMBERWATCH",
            chapterNumber = 10,
            title = "Emberwatch",
            subtitle = "The Academy Stands",
            location = "Fort Vane - Academy Parade Ground",
            briefingOverview = "The enemy is no longer hunting the transmitter. It has come for the academy itself, with cadets still inside the barracks. Hold the parade ground through three assault waves, break the siege, and put the Warden command frame down.",
            primaryObjective = "Hold the parade ground and destroy the Warden command frame.",
            secondaryObjective = "Keep the barracks intact - every cadet inside is a life on your line.",
            estimatedDurationMinutes = 24,
            recommendedRank = "Emberwatch Command",
            rewardXp = 1500,
            parTimeSeconds = 540,
            hostileIntel = listOf(
                "5 x ASSAULT DRONE - first push on the barracks approach",
                "3 x SENTINEL TURRET - siege emplacements ringing the parade ground",
                "2 x WALKER - biped artillery shelling the barracks",
                "1 x THE WARDEN - boss command frame, deploys escorts at 66% and 33% hull",
                "3 x ASSAULT WAVES - two, three then four hostiles through the north gate"
            )
        )
    )

    fun byId(id: String): MissionDossier =
        ALL_MISSIONS.firstOrNull { it.id == id } ?: ALL_MISSIONS.first()

    /** The chapter that unlocks when [id] is cleared, or null for the finale. */
    fun nextAfter(id: String): MissionDossier? {
        val index = ALL_MISSIONS.indexOfFirst { it.id == id }
        if (index < 0) return null
        return ALL_MISSIONS.getOrNull(index + 1)
    }

    val SQUAD_DOSSIERS = listOf(
        SquadMemberDossier(
            key = "voss",
            name = "Captain Mara Voss",
            callsign = "IRON-HEART",
            role = "Squad Mentor & Tactical Officer",
            bio = "Veteran of the Three-Year Border War. Stern, protective, and unflinchingly disciplined. Voss demands excellence from cadets because she knows the cost of unpreparedness in a live firezone.",
            specialty = "Tactical Command & Suppression Fire",
            uniformColor = 0xFF354A40
        ),
        SquadMemberDossier(
            key = "tomas",
            name = "Tomas Vale",
            callsign = "HAWK-EYE",
            role = "Reconnaissance Specialist",
            bio = "Raised on the coastal cliffs of Asterra. Tomas has an instinct for topography and stealth. Quiet and cautious, he favors patient observation before committing squad resources.",
            specialty = "Long-Range Scouting & Sensor Triangulation",
            uniformColor = 0xFF667353
        ),
        SquadMemberDossier(
            key = "leena",
            name = "Leena Suri",
            callsign = "SPARK",
            role = "Combat Engineer & Cryptanalyst",
            bio = "Top graduate in mechanical systems from Asterra Technical Academy. Leena views battlefields as networks of mechanical equations to be diagnosed, hacked, or reinforced.",
            specialty = "Terminal Overrides & Generator Mechanics",
            uniformColor = 0xFF81919E
        ),
        SquadMemberDossier(
            key = "idris",
            name = "Idris Kane",
            callsign = "SOLACE",
            role = "Combat Medic",
            bio = "Dedicated field medic whose commitment to civilian safety frequently clashes with cold military calculus. Idris keeps the squad grounded in human empathy.",
            specialty = "Field Triage & Emergency Stabilization",
            uniformColor = 0xFFD6C29A
        ),
        SquadMemberDossier(
            key = "kade",
            name = "Rowan Kade",
            callsign = "VIPER",
            role = "Rival Operative",
            bio = "Aggressive, relentlessly competitive cadet from a prestigious military family. Rowan believes hesitation is betrayal and frequently challenges your command decisions.",
            specialty = "Direct Action Assault & Rapid Clearing",
            uniformColor = 0xFFC84C4C
        )
    )

    val MISSION_1_CHOICE = MissionChoice(
        title = "CRITICAL SITUATION — AUXILIARY RELAY FAULT",
        description = "The auxiliary generator is venting superheated coolant near the civilian emergency shelter, risking fatal rupture. Simultaneously, a saboteur terminal is transmitting stolen frequency ciphers to an offshore vessel.",
        optionA = ChoiceOption(
            id = "SAFEGUARD_CIVILIANS",
            label = "SAFEGUARD CIVILIANS",
            summary = "Vent power into the drainage sluice to protect 40 sheltering families. The stolen cipher transmissions will complete, but no lives will be lost.",
            outcomeDialogue = DialogueLine(
                speaker = "Idris Kane (Medic)",
                speakerRole = "Combat Medic",
                text = "Thank heavens, Spectre! The shelter pressure dropped to safe levels! You put human lives before cold data. That's real leadership.",
                speakerColorHex = 0xFFD6C29A
            ),
            vossTrustDelta = 15,
            idrisRespectDelta = 20,
            kadeRivalryDelta = 10
        ),
        optionB = ChoiceOption(
            id = "INTERCEPT_DATA",
            label = "INTERCEPT CIPHER DATA",
            summary = "Overclock the scanner to capture the enemy saboteur's stolen frequency ciphers. The bunker will sustain superficial heat damage, but Asterra gains vital military intelligence.",
            outcomeDialogue = DialogueLine(
                speaker = "Rowan Kade (Rival)",
                speakerRole = "Rival Operative",
                text = "Impressive steel, Spectre. You secured the enemy frequency bands before they could wipe the drive. Voss might scowl, but command won't argue with results.",
                speakerColorHex = 0xFFC84C4C
            ),
            vossTrustDelta = 5,
            idrisRespectDelta = -5,
            kadeRivalryDelta = -15,
            intelDelta = 25
        )
    )
}

data class SquadMemberDossier(
    /** Links this dossier to the relationship column on CampaignProfileEntity. */
    val key: String,
    val name: String,
    val callsign: String,
    val role: String,
    val bio: String,
    val specialty: String,
    val uniformColor: Long
)
