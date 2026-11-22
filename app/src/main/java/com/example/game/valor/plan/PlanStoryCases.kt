package com.example.game.valor.plan

import com.example.game.valor.gdd.AlignmentMeter
import com.example.game.valor.gdd.AlignmentState
import com.example.game.valor.gdd.Ending
import com.example.game.valor.gdd.StoryCast
import com.example.game.valor.gdd.StoryChoices
import com.example.game.vanguard.CaseRecorder
import com.example.game.vanguard.ContractCase
import com.example.game.vanguard.approxEquals
import com.example.game.vanguard.observed

/** Section 5.20: story beats, choices, meters, endings and the protected cast. */
object PlanStoryCases {

    fun run(): List<ContractCase> {
        val r = CaseRecorder(ValorPlanAudit.GROUP_STORY)
        beatCases(r)
        choiceCases(r)
        endingCases(r)
        return r.result()
    }

    private fun beatCases(r: CaseRecorder) {
        val state = StoryState()
        r.check(
            "A beat plays once", "replaying a completed beat is refused",
            state.playBeat("act1.graduation") && !state.playBeat("act1.graduation") &&
                state.completedBeatCount() == 1,
            observed(state.completedBeatCount(), 1)
        )
        val firstVisit = state.canSkip("cutscene.1")
        state.markViewed("cutscene.1")
        r.check(
            "Skip after first view", "a cutscene is skippable only on a repeat visit",
            !firstVisit && state.canSkip("cutscene.1"),
            observed(firstVisit, false)
        )
        r.check(
            "Forward only", "the campaign never rewinds to an earlier act",
            !StoryState().advanceTo(1) && StoryState().advanceTo(2) &&
                !StoryState(initialAct = 2).advanceTo(1),
            observed(StoryState(initialAct = 2).advanceTo(1), false)
        )
        r.check(
            "Authored beats respect the cap", "every shipped choice offers two or three options",
            StoryChoicesRules.authoredBeats().isNotEmpty() &&
                StoryChoicesRules.authoredBeats().all { StoryChoicesRules.withinOptionCap(it) } &&
                StoryChoices.CHOICES.all { it.options.size in 2..3 },
            observed(StoryChoicesRules.authoredBeats().size, StoryChoices.CHOICES.size)
        )
    }

    private fun choiceCases(r: CaseRecorder) {
        val state = StoryState()
        val beat = StoryBeat(
            "act1.choice", 1, "Graduation exercise",
            listOf(
                StoryOption("Stay silent"),
                StoryOption("Report Vance", AlignmentMeter.LOYALTY, 2)
            )
        )
        val unconfirmed = state.choose(beat, 0, confirmed = false)
        r.check(
            "Choice lands on confirm", "an unconfirmed tap records nothing",
            !unconfirmed.recorded && state.choiceCount() == 0 &&
                unconfirmed.reason == "Not confirmed",
            observed(state.choiceCount(), 0)
        )
        val confirmed = state.choose(beat, 1, confirmed = true)
        r.check(
            "Meter moves", "a confirmed choice shifts exactly the meter it names",
            confirmed.recorded && state.meters.loyalty == 2 && state.meters.diplomacy == 0 &&
                state.recordedChoice("act1.choice") == "Report Vance",
            observed(state.meters.loyalty, 2)
        )
        val timed = StoryState().choose(beat, null, confirmed = false, timedOut = true)
        r.check(
            "Timeout falls to neutral", "a timed-out decision takes the meterless option",
            timed.recorded && timed.defaulted && timed.option?.neutral == true,
            observed(timed.option?.text, "Stay silent")
        )
        r.check(
            "Dialogue history", "every recorded line lands in the log in order",
            state.dialogueHistory().size == 1 &&
                state.dialogueHistory().first().startsWith("Graduation exercise:"),
            observed(state.dialogueHistory().size, 1)
        )
        val snapshot = state.snapshot()
        r.check(
            "Snapshot for the save", "the story state writes its choices and its meters",
            snapshot.value(SaveKeys.STORY_CHOICES)?.contains("Report Vance") == true &&
                snapshot.value(SaveKeys.METERS)?.isNotBlank() == true,
            observed(snapshot.value(SaveKeys.METERS), "three meter values")
        )
    }

    private fun endingCases(r: CaseRecorder) {
        r.check(
            "All endings reachable", "the three meter presets reach the three endings",
            EndingRules.allReachable() && EndingRules.reachable().size == Ending.entries.size &&
                EndingRules.forMeters(AlignmentState(aggression = 1)) == Ending.LONE_WOLF &&
                EndingRules.forMeters(AlignmentState(loyalty = 1)) == Ending.SOLDIERS_HONOR,
            observed(EndingRules.reachable().size, Ending.entries.size)
        )
        r.check(
            "Ending label", "an ending always reads as name plus epilogue",
            EndingRules.label(Ending.STATESMAN).startsWith(Ending.STATESMAN.displayName),
            observed(EndingRules.label(Ending.STATESMAN), "name - epilogue")
        )
        r.check(
            "Story-critical cast", "three characters can only die on a scripted beat",
            StoryCritical.IDS.size == 3 && StoryCritical.names().size == 3 &&
                StoryCritical.isCritical(StoryCast.BROOKS) &&
                StoryCritical.blocksLethalDamage(StoryCast.VANCE, scriptedBeat = false) &&
                !StoryCritical.blocksLethalDamage(StoryCast.VANCE, scriptedBeat = true),
            observed(StoryCritical.IDS.size, 3)
        )
        val ally = Combatant(StoryCast.BROOKS, Team.ALLY, maxHealth = 100f, storyCritical = true)
        val stray = StoryProtection.clampLethal(500f, ally, scriptedBeat = false)
        val scripted = StoryProtection.clampLethal(500f, ally, scriptedBeat = true)
        r.check(
            "Stray bullet clamped", "a critical ally is left with one hit point, never killed",
            approxEquals(stray, 99f) && approxEquals(scripted, 500f) &&
                StoryProtection.isProtected(ally, scriptedBeat = false) &&
                !StoryProtection.isProtected(ally, scriptedBeat = true) &&
                StoryProtection.survivesStrayBullet(ally),
            observed(stray, 99f)
        )
    }
}
