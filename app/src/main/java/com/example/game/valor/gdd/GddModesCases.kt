package com.example.game.valor.gdd

import com.example.game.vanguard.CaseRecorder
import com.example.game.vanguard.ContractCase
import com.example.game.vanguard.observed

/** Sections 7.1-7.3 and 7.5: mission types, academy, replay and bosses. */
object GddModeCases {

    fun run(): List<ContractCase> {
        val r = CaseRecorder(ValorGddAudit.GROUP_MODES)

        r.check(
            "Mission types", "11 types from Assault to Drone command",
            MissionTypes.count() == 11 &&
                MissionTypes.TYPES.first().code == "ASL" &&
                MissionTypes.TYPES.last().code == "DRN",
            observed(MissionTypes.count(), 11)
        )
        r.check(
            "Defend objective", "hold a point against waves",
            MissionTypes.byCode("DEF")?.objective == "Hold a point against waves",
            observed(MissionTypes.byCode("DEF")?.objective, "Hold a point against waves")
        )
        r.check(
            "Campaign shape", "60 missions over 6 acts of 5-12 minutes",
            CampaignShape.APPROX_MISSIONS == 60 &&
                CampaignShape.ACTS == 6 &&
                CampaignShape.missionsPerAct() == 10,
            observed(CampaignShape.missionsPerAct(), 10)
        )
        r.check(
            "Mission length window", "5 and 12 pass, 4 and 13 do not",
            CampaignShape.inTimeWindow(5) && CampaignShape.inTimeWindow(12) &&
                !CampaignShape.inTimeWindow(4) && !CampaignShape.inTimeWindow(13),
            observed(CampaignShape.inTimeWindow(13), false)
        )
        r.check(
            "Academy activities", "5 activities, 3 of them timed",
            Academy.ACTIVITIES.size == 5 && Academy.timed().size == 3,
            observed(Academy.timed().size, 3)
        )
        r.check(
            "Tournament fairness", "the timer scales and the first try is generous",
            Academy.tournament().scalesWithRank && Academy.tournament().firstAttemptGenerous,
            observed(Academy.tournament().firstAttemptGenerous, true)
        )
        r.check(
            "Replay modes", "6 ways to keep playing",
            ReplayModes.count() == 6 && ReplayModes.MODES.last().name == "Mission Mastery",
            observed(ReplayModes.count(), 6)
        )
        r.check(
            "Challenge modifiers", "headshots only, one-hit-kill, low ammo",
            ReplayModes.MODIFIERS.map { it.displayName } ==
                listOf("Headshots only", "One-hit-kill", "Low ammo"),
            observed(ReplayModes.MODIFIERS.size, 3)
        )
        r.check(
            "Mastery stars", "three criteria, one star each",
            MissionMastery.STARS_PER_MISSION == 3 &&
                MissionMastery.CRITERIA.size == 3 &&
                MissionMastery.stars(true, true, true) == 3 &&
                MissionMastery.stars(true, false, false) == 1,
            observed(MissionMastery.stars(true, true, true), 3)
        )
        r.check(
            "Mastery verdict", "on-target and undamaged is perfect, one over is not",
            MissionMastery.stars(120f, 120f, 80, 80, false) == 3 &&
                MissionMastery.stars(121f, 120f, 79, 80, true) == 0 &&
                MissionMastery.isPerfect(3) && !MissionMastery.isPerfect(2),
            observed(MissionMastery.stars(121f, 120f, 79, 80, true), 0)
        )
        r.check(
            "Boss roster", "5 bosses in the printed order",
            BossRoster.BOSSES.size == 5 &&
                BossRoster.BOSSES.first().name == "Trench Butcher" &&
                BossRoster.BOSSES.last().name == "Kessler (final)",
            observed(BossRoster.BOSSES.size, 5)
        )
        r.check(
            "Boss by act", "Act 4 is the fortress commander, Act 3 has no boss",
            BossRoster.forAct(4).single().name == "Fortress Commander" &&
                BossRoster.forAct(3).isEmpty(),
            observed(BossRoster.forAct(4).map { it.name }, "[Fortress Commander]")
        )
        r.check(
            "Boss Rush order", "every boss back to back",
            BossRoster.rushOrder().size == 5, observed(BossRoster.rushOrder().size, 5)
        )

        r.check(
            "Type lookup", "the Defend row is DEF",
            MissionTypes.byName("defend")?.code == "DEF",
            observed(MissionTypes.byName("defend")?.code, "DEF")
        )
        r.check(
            "Academy lookup", "the gym is an untimed activity",
            Academy.byName("gym")?.timed == false,
            observed(Academy.byName("gym")?.timed, false)
        )
        r.check(
            "Replay lookup", "Boss Rush fields every boss",
            ReplayModes.byName("Boss Rush")?.detail == "All bosses back to back",
            observed(ReplayModes.byName("Boss Rush")?.detail, "All bosses back to back")
        )
        r.check(
            "Boss lookup", "the Ghost Sniper closes Act 5",
            BossRoster.byName("Ghost Sniper")?.act == 5,
            observed(BossRoster.byName("Ghost Sniper")?.act, 5)
        )

        return r.result()
    }
}

/** Section 7.4: the enemy roster and the six-state AI machine. */
object GddAiCases {

    fun run(): List<ContractCase> {
        val r = CaseRecorder(ValorGddAudit.GROUP_AI)

        r.check(
            "Enemy roster", "10 enemy behaviours",
            EnemyRoster.count() == 10 && EnemyRoster.byName("boss") != null,
            observed(EnemyRoster.count(), 10)
        )
        r.check(
            "Officer row", "officers buff the enemies around them",
            EnemyRoster.byName("Officer")?.behavior?.contains("Buffs") == true,
            observed(EnemyRoster.byName("Officer")?.behavior, "Buffs nearby enemies")
        )
        r.check(
            "Six AI states", "patrol, alert, combat, flank, retreat, dead",
            AiState.entries.size == 6 &&
                AiState.entries.map { it.displayName } ==
                listOf("Patrol", "Alert", "Combat", "Flank", "Retreat/Heal", "Dead"),
            observed(AiState.entries.size, 6)
        )
        r.check(
            "Seven stimuli", "gunshot, sight, lost, flank, damage, heal, kill",
            AiEvent.entries.size == 7, observed(AiEvent.entries.size, 7)
        )
        r.check(
            "Hearing a shot", "patrol becomes alert",
            EnemyAi.next(AiState.PATROL, AiEvent.HEARD_GUNSHOT) == AiState.ALERT,
            observed(EnemyAi.next(AiState.PATROL, AiEvent.HEARD_GUNSHOT), AiState.ALERT)
        )
        r.check(
            "Sight chain", "patrol and alert both enter combat on sight",
            EnemyAi.next(AiState.PATROL, AiEvent.SAW_TARGET) == AiState.COMBAT &&
                EnemyAi.next(AiState.ALERT, AiEvent.SAW_TARGET) == AiState.COMBAT,
            observed(EnemyAi.next(AiState.ALERT, AiEvent.SAW_TARGET), AiState.COMBAT)
        )
        r.check(
            "Alert recovery", "losing the target returns to patrol",
            EnemyAi.next(AiState.ALERT, AiEvent.LOST_TARGET) == AiState.PATROL,
            observed(EnemyAi.next(AiState.ALERT, AiEvent.LOST_TARGET), AiState.PATROL)
        )
        r.check(
            "Flank timer", "combat flanks, flanking re-engages",
            EnemyAi.next(AiState.COMBAT, AiEvent.FLANK_TIMER) == AiState.FLANK &&
                EnemyAi.next(AiState.FLANK, AiEvent.SAW_TARGET) == AiState.COMBAT,
            observed(EnemyAi.next(AiState.COMBAT, AiEvent.FLANK_TIMER), AiState.FLANK)
        )
        r.check(
            "Retreat and heal", "heavy damage retreats, healing returns to combat",
            EnemyAi.next(AiState.COMBAT, AiEvent.TOOK_HEAVY_DAMAGE) == AiState.RETREAT &&
                EnemyAi.next(AiState.RETREAT, AiEvent.HEALTH_RESTORED) == AiState.COMBAT,
            observed(EnemyAi.next(AiState.RETREAT, AiEvent.HEALTH_RESTORED), AiState.COMBAT)
        )
        r.check(
            "Unhandled stimulus", "an event with no arrow holds the state",
            EnemyAi.next(AiState.PATROL, AiEvent.LOST_TARGET) == AiState.PATROL &&
                EnemyAi.eventsFor(AiState.PATROL).size == 2,
            observed(EnemyAi.next(AiState.PATROL, AiEvent.LOST_TARGET), AiState.PATROL)
        )
        r.check(
            "Kill ends the machine", "any kill lands in Dead and Dead stays Dead",
            EnemyAi.next(AiState.COMBAT, AiEvent.KILLED) == AiState.DEAD &&
                EnemyAi.next(AiState.DEAD, AiEvent.SAW_TARGET) == AiState.DEAD,
            observed(EnemyAi.next(AiState.DEAD, AiEvent.SAW_TARGET), AiState.DEAD)
        )
        r.check(
            "All states reachable", "the six-state loop has no dead end",
            AiState.entries.all { EnemyAi.isReachable(it) },
            observed(AiState.entries.filterNot { EnemyAi.isReachable(it) }, "[]")
        )
        r.check(
            "Arrow count", "8 documented arrows",
            EnemyAi.TRANSITIONS.size == 8,
            observed(EnemyAi.TRANSITIONS.size, 8)
        )
        r.check(
            "Sensing rules", "cover points, heard gunshots and a vision cone",
            EnemyAi.USES_COVER_POINTS && EnemyAi.HEARS_GUNSHOTS && EnemyAi.USES_VISION_CONE,
            observed(EnemyAi.USES_VISION_CONE, true)
        )
        r.check(
            "Events per state", "combat answers flank and heavy damage",
            EnemyAi.eventsFor(AiState.COMBAT) ==
                listOf(AiEvent.FLANK_TIMER, AiEvent.TOOK_HEAVY_DAMAGE) &&
                EnemyAi.eventsFor(AiState.DEAD).isEmpty(),
            observed(EnemyAi.eventsFor(AiState.COMBAT).size, 2)
        )

        return r.result()
    }
}

/** Section 8: the touch scheme and the accessibility switches. */
object GddControlCases {

    fun run(): List<ContractCase> {
        val r = CaseRecorder(ValorGddAudit.GROUP_CONTROLS)

        r.check(
            "Binding rows", "9 control rows",
            ControlScheme.count() == 9, observed(ControlScheme.count(), 9)
        )
        r.check(
            "Twin sticks", "left stick moves, right drag looks",
            ControlScheme.byControl("Left virtual stick")?.action == "Move" &&
                ControlScheme.byControl("Right-side drag")?.action == "Look/aim",
            observed(ControlScheme.byControl("Right-side drag")?.action, "Look/aim")
        )
        r.check(
            "Action buttons", "6 rearrangeable buttons",
            ControlScheme.ACTION_BUTTONS.size == 6 &&
                ControlScheme.ACTION_BUTTONS.contains("Melee"),
            observed(ControlScheme.ACTION_BUTTONS.size, 6)
        )
        r.check(
            "Cover rule", "cover snaps automatically",
            ControlScheme.byControl("Cover")?.action?.contains("Auto-snap") == true,
            observed(ControlScheme.byControl("Cover")?.action, "Auto-snap when near cover")
        )
        r.check(
            "Aim assist", "Off / Low / High, as Section 8 prints them",
            ControlScheme.aimAssistLabels() == listOf("Off", "Low", "High"),
            observed(ControlScheme.aimAssistLabels(), "[Off, Low, High]")
        )
        r.check(
            "Layout editing", "the button layout can be rearranged",
            ControlScheme.layoutEditable(), observed(ControlScheme.layoutEditable(), true)
        )
        r.check(
            "Accessibility options", "left hand, subtitles, outlines, vibration",
            AccessibilityOptions.count() == 4 &&
                AccessibilityOptions.byName("Left-hand mode") != null,
            observed(AccessibilityOptions.count(), 4)
        )

        return r.result()
    }
}
