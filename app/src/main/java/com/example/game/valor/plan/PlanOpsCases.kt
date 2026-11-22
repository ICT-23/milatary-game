package com.example.game.valor.plan

import com.example.game.valor.gdd.DroneType
import com.example.game.vanguard.CaseRecorder
import com.example.game.vanguard.ContractCase
import com.example.game.vanguard.approxEquals
import com.example.game.vanguard.observed

/** Sections 5.13 to 5.15: drones, the mission state machine and tournaments. */
object PlanOpsCases {

    fun run(): List<ContractCase> {
        val r = CaseRecorder(ValorPlanAudit.GROUP_OPS)
        droneCases(r)
        missionCases(r)
        tournamentCases(r)
        return r.result()
    }

    private fun droneCases(r: CaseRecorder) {
        val drone = DroneType.entries.first()
        r.check(
            "Rank gate", "a drone unlocks at its own rank, not before",
            !DroneRules.unlockedAt(drone.unlockRank - 1, drone) &&
                DroneRules.unlockedAt(drone.unlockRank, drone),
            observed(drone.unlockRank, drone.displayName)
        )
        val cooled = DroneType.entries.first { it.cooldownSeconds != null }
        val cooldown = cooled.cooldownSeconds ?: 0
        r.check(
            "Charge and cooldown", "a call needs a charge and an expired cooldown",
            DroneRules.canCall(20, cooled, chargesLeft = 1, secondsSinceLastUse = cooldown.toFloat()) &&
                !DroneRules.canCall(20, cooled, chargesLeft = 0, secondsSinceLastUse = cooldown.toFloat()) &&
                !DroneRules.canCall(20, cooled, chargesLeft = 1, secondsSinceLastUse = cooldown - 1f),
            observed(cooldown, cooled.cooldownSeconds)
        )
        val loadout = DroneLoadout(mapOf(cooled to 1))
        val first = loadout.consume(cooled, 0f)
        r.check(
            "Consume once", "one call spends exactly one charge",
            first && loadout.chargesLeft(cooled) == 0 && !loadout.consume(cooled, 1f),
            observed(loadout.chargesLeft(cooled), 0)
        )
        val calls = ActiveCalls()
        r.check(
            "One active call per type", "a second call of the same type is refused",
            calls.begin(cooled, "call1") && !calls.begin(cooled, "call2") &&
                calls.activeCount() == 1,
            observed(calls.active(cooled), "call1")
        )
        calls.finish(cooled, "call1")
        r.check(
            "Finishing frees the slot", "the same type can be called again afterwards",
            calls.active(cooled) == null && calls.begin(cooled, "call3"),
            observed(calls.activeCount(), 1)
        )
        val marking = DroneType.entries.first { it.durationSeconds != null }
        val markSeconds = marking.durationSeconds ?: 0
        r.check(
            "Recon is visual only", "marks last their printed duration and never deal damage",
            !DroneRules.reconDealsDamage() &&
                DroneRules.marksVisible(marking, markSeconds.toFloat()) &&
                DroneRules.marksAfterDeaths(listOf("a", "b"), setOf("a")) == listOf("b"),
            observed(DroneRules.marksAfterDeaths(listOf("a", "b"), setOf("a")), listOf("b"))
        )
        r.check(
            "Strike safety", "a two second delay and no strike inside a no-strike zone",
            approxEquals(DroneRules.strikeDelaySeconds(), 2f) &&
                !DroneRules.canTarget(noStrikeZone = true, alliesInRadius = false, civiliansInRadius = false) &&
                !DroneRules.canTarget(noStrikeZone = false, alliesInRadius = true, civiliansInRadius = false) &&
                DroneRules.canTarget(noStrikeZone = false, alliesInRadius = false, civiliansInRadius = false),
            observed(DroneRules.strikeDelaySeconds(), 2f)
        )
        r.check(
            "Swarm window", "three seconds of small hits at the given interval",
            approxEquals(DroneRules.SWARM_DURATION_SECONDS, 3f) &&
                DroneRules.swarmHits(0.5f) == 6 &&
                DroneRules.swarmHits(1f) == 3,
            observed(DroneRules.swarmHits(0.5f), 6)
        )
        val fpv = DroneRules.enterFpv(30f, "pose-2")
        val flown = DroneRules.tickFpv(fpv, 12f)
        r.check(
            "FPV sortie", "the battery drains, the body stays exposed, the pose is restored",
            approxEquals(flown.batterySeconds, 18f) &&
                DroneRules.bodyVulnerable(flown) &&
                !DroneRules.fpvExpired(flown) &&
                DroneRules.exitFpv(flown) == "pose-2" &&
                DroneRules.fpvExpired(DroneRules.tickFpv(fpv, 30f)),
            observed(flown.batterySeconds, 18f)
        )
    }

    private fun missionCases(r: CaseRecorder) {
        r.check(
            "Ten states", "Loading through Exit in document order",
            MissionState.entries.map { it.displayName } == listOf(
                "Loading", "Briefing", "Intro/Cutscene", "Playing", "Paused",
                "Completing", "Success", "Failed", "Results", "Exit"
            ),
            observed(MissionState.entries.size, 10)
        )
        r.check(
            "Ten objective types", "KillAll through Stealth",
            ObjectiveType.entries.size == 10 &&
                ObjectiveType.entries.first() == ObjectiveType.KILL_ALL &&
                ObjectiveType.entries.last() == ObjectiveType.STEALTH,
            observed(ObjectiveType.entries.size, 10)
        )
        r.check(
            "Transition table", "twelve legal edges, no shortcuts",
            MissionFsm.transitionCount() == 12 &&
                MissionFsm.TRANSITIONS[MissionState.LOADING] == setOf(MissionState.BRIEFING) &&
                MissionFsm.TRANSITIONS[MissionState.EXIT].orEmpty().isEmpty(),
            observed(MissionFsm.transitionCount(), 12)
        )
        val log = DebugLog()
        val run = MissionRun(listOf(Objective("obj1", ObjectiveType.KILL_ALL)))
        val illegal = run.transition(MissionState.RESULTS, log)
        r.check(
            "Illegal transition refused", "Loading cannot jump to Results, and the log says so",
            !illegal && run.state == MissionState.LOADING &&
                log.last(LogCategory.MISSION)?.reason?.contains("Invalid transition") == true,
            observed(run.state, MissionState.LOADING)
        )
        r.check(
            "Legal path", "Loading -> Briefing -> Intro -> Playing",
            run.transition(MissionState.BRIEFING) &&
                run.transition(MissionState.INTRO) &&
                run.transition(MissionState.PLAYING),
            observed(run.state, MissionState.PLAYING)
        )
        val objective = Objective("obj1", ObjectiveType.KILL_ALL)
        r.check(
            "Objective lifecycle", "locked, then active, then complete exactly once",
            !objective.complete() && objective.activate() && !objective.activate() &&
                objective.complete() && !objective.complete() && objective.isComplete,
            observed(objective.status, ObjectiveStatus.COMPLETED)
        )
        val successful = MissionRun(listOf(Objective("a", ObjectiveType.KILL_COUNT)))
        successful.objectives.forEach { it.activate() }
        successful.objectives.forEach { it.complete() }
        val failed = MissionRun(listOf(Objective("b", ObjectiveType.DEFEND_TIMER)))
        failed.objectives.forEach { it.fail() }
        r.check(
            "Completion and failure", "all required complete -> Completing, any failed -> Failed",
            successful.evaluate() == MissionState.COMPLETING &&
                failed.evaluate() == MissionState.FAILED &&
                failed.anyRequiredFailed,
            observed(successful.evaluate(), MissionState.COMPLETING)
        )
        r.check(
            "Watchdog", "a hint at 60 s, an auto-complete at 120 s with nothing left",
            approxEquals(MissionFsm.WATCHDOG_HINT_SECONDS, 60f) &&
                Watchdog(60f, 2).shouldHint() &&
                !Watchdog(59f, 0).shouldHint() &&
                Watchdog(120f, 0).shouldAutoComplete() &&
                !Watchdog(120f, 1).shouldAutoComplete(),
            observed(Watchdog(120f, 0).shouldAutoComplete(), true)
        )
        val registry = EnemyRegistry()
        registry.spawn("e1")
        registry.spawn("e2")
        r.check(
            "Enemy registry", "kills, despawns and falls all remove a living body",
            registry.livingCount() == 2 &&
                registry.kill("e1") && !registry.kill("e1") &&
                registry.livingCount() == 1 &&
                registry.fallOffMap("e2") && registry.isEmpty() &&
                registry.killPlaneCrossed(-5f, -2f) &&
                !registry.killPlaneCrossed(1f, -2f),
            observed(registry.livingCount(), 0)
        )
        val grant = RewardGrant()
        var paid = 0
        r.check(
            "Single grant", "the reward lands once and only once",
            grant.grant(100, 50) { xp, cash -> paid = xp + cash } &&
                !grant.grant(100, 50) { _, _ -> paid = 0 } &&
                paid == 150,
            observed(paid, 150)
        )
        val checkpoint = Checkpoint("obj1", 55f, 42, listOf("obj0"))
        val store = CheckpointStore()
        store.save(checkpoint)
        r.check(
            "Checkpoint", "saved at objective completion and cleared on mission end",
            store.last == checkpoint && store.also { it.clear() }.last == null,
            observed(store.last, null)
        )
        r.check(
            "Accuracy", "hits over shots as a percentage",
            MissionResults.accuracyPercent(5, 10) == 50 &&
                MissionResults.accuracyPercent(0, 0) == 0,
            observed(MissionResults.accuracyPercent(5, 10), 50)
        )
    }

    private fun tournamentCases(r: CaseRecorder) {
        val thresholds = TournamentThresholds(bronze = 20, silver = 40, gold = 60)
        val run = TournamentRun(baseTimeSeconds = 60f, rankTimeScale = 0.9f, thresholds = thresholds)
        r.check(
            "Timer scaling", "baseTime x difficultyScale[rank]",
            approxEquals(run.timeLimitSeconds, 54f),
            observed(run.timeLimitSeconds, 54f)
        )
        run.tick(1f)
        r.check(
            "Countdown first", "the clock only starts when 3-2-1 finishes",
            !run.running && approxEquals(run.elapsedSeconds, 0f) && approxEquals(run.countdownRemaining, 2f),
            observed(run.elapsedSeconds, 0f)
        )
        run.tick(2f)
        run.tick(10f)
        val beforePause = run.elapsedSeconds
        run.pause()
        run.tick(3f)
        r.check(
            "Pause stops the timer", "a paused run does not advance",
            approxEquals(beforePause, 10f) && approxEquals(run.elapsedSeconds, 10f) &&
                TournamentRules.pauseStopsTimer(),
            observed(run.elapsedSeconds, 10f)
        )
        run.resume()
        run.tick(2f)
        r.check(
            "Resume", "the clock picks up where it stopped",
            approxEquals(run.elapsedSeconds, 12f),
            observed(run.elapsedSeconds, 12f)
        )
        run.registerTarget(TargetScore(headshot = true, centerMass = false), 2f, 1f)
        r.check(
            "Scoring", "a headshot scores its multiplier, a body hit scores one",
            run.score == 2 &&
                TournamentRules.score(listOf(TargetScore(false, true)), 2f, 1.5f) == 1,
            observed(run.score, 2)
        )
        r.check(
            "Tiers", "Bronze, Silver and Gold resolve from the threshold table",
            thresholds.tierFor(60) == TournamentTier.GOLD &&
                thresholds.tierFor(40) == TournamentTier.SILVER &&
                thresholds.tierFor(20) == TournamentTier.BRONZE &&
                thresholds.tierFor(19) == null,
            observed(thresholds.tierFor(40), TournamentTier.SILVER)
        )
        r.check(
            "Never blocks the story", "an 80% pass rate, retry allowed, no story gate",
            TournamentRules.meetsPassTarget(8, 10) &&
                !TournamentRules.meetsPassTarget(7, 10) &&
                TournamentRules.retryImmediately() &&
                !TournamentRules.blocksStory() &&
                TournamentRules.validSpawn(onScreen = true, obstructed = false) &&
                !TournamentRules.validSpawn(onScreen = false, obstructed = false),
            observed(TournamentRules.meetsPassTarget(7, 10), false)
        )
    }
}
