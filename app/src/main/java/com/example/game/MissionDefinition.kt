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
    val kadeRivalryDelta: Int
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
    val rewardXp: Int
)

object MissionCatalog {
    val ALL_MISSIONS = listOf(
        MissionDossier(
            id = "M1_SIGNAL_IN_DUST",
            chapterNumber = 1,
            title = "Signal in the Dust",
            subtitle = "Sector 4 Coastal Relay Outpost",
            location = "Cape Oakhaven, Asterra",
            briefingOverview = "What began as a routine cadet field exercise has abruptly escalated. All regional telemetry nodes went dark 12 minutes ago. Captain Voss has ordered Spectre-7 to link up with Recon Specialist Tomas, scan the damaged tower array, and discover the source of the blackout.",
            primaryObjective = "Inspect Relay Tower Alpha and secure regional communications.",
            secondaryObjective = "Resolve emergency protocol without losing vital intelligence.",
            estimatedDurationMinutes = 8,
            recommendedRank = "Junior Cadet",
            rewardXp = 250
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
            rewardXp = 350
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
            rewardXp = 450
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
            rewardXp = 550
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
            rewardXp = 650
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
            rewardXp = 800
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
            rewardXp = 1000
        )
    )

    val SQUAD_DOSSIERS = listOf(
        SquadMemberDossier(
            name = "Captain Mara Voss",
            callsign = "IRON-HEART",
            role = "Squad Mentor & Tactical Officer",
            bio = "Veteran of the Three-Year Border War. Stern, protective, and unflinchingly disciplined. Voss demands excellence from cadets because she knows the cost of unpreparedness in a live firezone.",
            specialty = "Tactical Command & Suppression Fire",
            uniformColor = 0xFF354A40
        ),
        SquadMemberDossier(
            name = "Tomas Vale",
            callsign = "HAWK-EYE",
            role = "Reconnaissance Specialist",
            bio = "Raised on the coastal cliffs of Asterra. Tomas has an instinct for topography and stealth. Quiet and cautious, he favors patient observation before committing squad resources.",
            specialty = "Long-Range Scouting & Sensor Triangulation",
            uniformColor = 0xFF667353
        ),
        SquadMemberDossier(
            name = "Leena Suri",
            callsign = "SPARK",
            role = "Combat Engineer & Cryptanalyst",
            bio = "Top graduate in mechanical systems from Asterra Technical Academy. Leena views battlefields as networks of mechanical equations to be diagnosed, hacked, or reinforced.",
            specialty = "Terminal Overrides & Generator Mechanics",
            uniformColor = 0xFF81919E
        ),
        SquadMemberDossier(
            name = "Idris Kane",
            callsign = "SOLACE",
            role = "Combat Medic",
            bio = "Dedicated field medic whose commitment to civilian safety frequently clashes with cold military calculus. Idris keeps the squad grounded in human empathy.",
            specialty = "Field Triage & Emergency Stabilization",
            uniformColor = 0xFFD6C29A
        ),
        SquadMemberDossier(
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
            summary = "Vent coolant into the drainage sluice to protect 40 sheltering families. The stolen cipher transmissions will complete, but no lives will be lost.",
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
            summary = "Overclock the scanner to capture the encrypted saboteur drive. The bunker will sustain superficial heat damage, but Asterra gains vital military intelligence.",
            outcomeDialogue = DialogueLine(
                speaker = "Rowan Kade (Rival)",
                speakerRole = "Rival Operative",
                text = "Impressive steel, Spectre. You secured the enemy frequency bands before they could wipe the drive. Voss might scowl, but command won't argue with results.",
                speakerColorHex = 0xFFC84C4C
            ),
            vossTrustDelta = 5,
            idrisRespectDelta = -5,
            kadeRivalryDelta = -15
        )
    )
}

data class SquadMemberDossier(
    val name: String,
    val callsign: String,
    val role: String,
    val bio: String,
    val specialty: String,
    val uniformColor: Long
)
