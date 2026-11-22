package com.example.game.valor.plan

import com.example.game.valor.balance.WeaponCatalog
import com.example.game.vanguard.CaseRecorder
import com.example.game.vanguard.ContractCase
import com.example.game.vanguard.approxEquals
import com.example.game.vanguard.observed

/** Section 5.21: the HUD reads live state and every popup waits for a safe moment. */
object PlanHudCases {

    fun run(): List<ContractCase> {
        val r = CaseRecorder(ValorPlanAudit.GROUP_HUD)
        readoutCases(r)
        feedbackCases(r)
        popupCases(r)
        return r.result()
    }

    private fun readoutCases(r: CaseRecorder) {
        val player = Combatant("player", Team.PLAYER, maxHealth = 100f, isPlayer = true)
        val ammo = AmmoState(magazine = 18, magSize = 30, reserve = 90, reserveCap = 120)
        val readout = HudRules.readout(
            player, ammo, WeaponCatalog.WEAPONS.first(), FireMode.AUTO, 2,
            listOf("Kill all enemies"), "enemy_2", HitMarkerKind.HEADSHOT
        )
        r.check(
            "HUD reads live state", "health and ammo are the simulation's numbers",
            approxEquals(readout.health, player.current) &&
                readout.magazine == ammo.magazine && readout.reserve == ammo.reserve &&
                readout.ammoLabel() == "18 / 90" &&
                approxEquals(readout.healthFraction, 1f) &&
                HudRules.SHOWS_ONLY_TRUTH,
            observed(readout.ammoLabel(), "18 / 90")
        )
        r.check(
            "Buttons grey out", "reload is disabled when the magazine is full",
            !HudRules.reloadEnabled(AmmoState(30, 30, 0, 120), reloading = false) &&
                HudRules.reloadEnabled(AmmoState(0, 30, 90, 120), reloading = false) &&
                !HudRules.reloadEnabled(AmmoState(0, 30, 90, 120), reloading = true) &&
                HudRules.isGreyed(false),
            observed(HudRules.reloadEnabled(AmmoState(30, 30, 0, 120), false), false)
        )
        r.check(
            "Centre stays clear", "only the crosshair may sit at the centre of the screen",
            HudRules.centerWidgets().map { it.id }.contains("crosshair") &&
                HudRules.centerBlockingWidgets().isEmpty() &&
                HudRules.noWidgetBlocksCenter(),
            observed(HudRules.centerBlockingWidgets().size, 0)
        )
        r.check(
            "Layout autosaves", "a moved widget goes through the settings trigger",
            HudRules.LAYOUT_EDITABLE && HudRules.LAYOUT_SAVED &&
                HudRules.layoutAutosaveTrigger() == AutosaveTrigger.SETTINGS_CHANGE &&
                HudRules.layoutKey() == "settings.hudLayout",
            observed(HudRules.layoutKey(), "settings.hudLayout")
        )
    }

    private fun feedbackCases(r: CaseRecorder) {
        r.check(
            "Damage direction on real damage", "an indicator appears only when health fell",
            HudRules.damageDirection("enemy_2", damageApplied = true) == "enemy_2" &&
                HudRules.damageDirection("enemy_2", damageApplied = false) == null &&
                HudRules.damageDirection(null, damageApplied = true) == null,
            observed(HudRules.damageDirection("enemy_2", false), null)
        )
        r.check(
            "Four hit markers", "miss, body, headshot and kill read differently",
            HudRules.hitMarker(false, false, false) == HitMarkerKind.NONE &&
                HudRules.hitMarker(true, false, false) == HitMarkerKind.BODY &&
                HudRules.hitMarker(true, true, false) == HitMarkerKind.HEADSHOT &&
                HudRules.hitMarker(true, true, true) == HitMarkerKind.KILL,
            observed(HudRules.hitMarker(true, true, false), HitMarkerKind.HEADSHOT)
        )
        r.check(
            "Localization table", "every HUD label has a key and a miss falls back to the key",
            Localization.NO_HARD_CODED_STRINGS && Localization.isTranslated("hud.health") &&
                Localization.text("hud.health") == "Health" &&
                Localization.text("missing.key") == "missing.key" &&
                !Localization.isTranslated("missing.key"),
            observed(Localization.text("missing.key"), "missing.key")
        )
    }

    private fun popupCases(r: CaseRecorder) {
        val queue = PopupQueue()
        queue.enqueue(PopupKind.RANK_UP)
        queue.enqueue(PopupKind.MEDAL)
        val during = queue.next(MissionState.PLAYING)
        val afterPlaying = queue.next(MissionState.RESULTS)
        r.check(
            "Popups wait for a safe state", "a rank-up never interrupts a firefight",
            during == null && afterPlaying == PopupKind.RANK_UP && queue.pendingCount() == 1 &&
                HudRules.SAFE_POPUP_STATES == setOf(
                    MissionState.SUCCESS, MissionState.RESULTS, MissionState.BRIEFING
                ),
            observed(during, null)
        )
        r.check(
            "Popup order is kept", "the oldest popup surfaces first",
            PopupQueue().apply { enqueue(PopupKind.MEDAL); enqueue(PopupKind.LEVEL_END) }
                .next(MissionState.RESULTS) == PopupKind.MEDAL,
            observed(PopupKind.MEDAL, "oldest first")
        )
    }
}

/** Section 5.22: rewarded ads are opt-in, interstitials sit between missions only. */
object PlanAdCases {

    fun run(): List<ContractCase> {
        val r = CaseRecorder(ValorPlanAudit.GROUP_ADS)
        placementCases(r)
        cadenceCases(r)
        return r.result()
    }

    private fun placementCases(r: CaseRecorder) {
        r.check(
            "Three optional points", "2x reward, revive and free crate are the only placements",
            AdPlacement.entries.size == 3 && AdsRules.ALLOWED_POINTS.size == 3,
            observed(AdPlacement.entries.size, 3)
        )
        r.check(
            "Never during gameplay", "no placement is offered in Playing, Paused or Completing",
            AdsRules.NEVER_IN_GAMEPLAY &&
                AdsRules.GAMEPLAY_STATES == setOf(
                    MissionState.PLAYING, MissionState.PAUSED, MissionState.COMPLETING
                ) &&
                AdPlacement.entries.none { placement ->
                    AdsRules.GAMEPLAY_STATES.any { state -> AdsRules.rewardedAllowed(placement, state) }
                },
            observed(AdsRules.GAMEPLAY_STATES.size, 3)
        )
        r.check(
            "Never during a cutscene", "an ad cannot interrupt the intro",
            AdsRules.NEVER_IN_CUTSCENE && !AdsRules.isGameplay(MissionState.INTRO) &&
                AdPlacement.entries.none { AdsRules.rewardedAllowed(it, MissionState.INTRO) } &&
                AdsRules.rewardedAllowed(AdPlacement.DOUBLE_REWARD_RESULTS, MissionState.RESULTS),
            observed(AdsRules.rewardedAllowed(AdPlacement.REVIVE, MissionState.INTRO), false)
        )
        r.check(
            "No dead buttons", "an unavailable ad hides its button instead of doing nothing",
            AdsRules.NO_DEAD_BUTTONS && !AdsRules.buttonVisible(adAvailable = false) &&
                AdsRules.buttonVisible(adAvailable = true) &&
                !AdsRules.buttonEnabled(adAvailable = false),
            observed(AdsRules.buttonVisible(false), false)
        )
        r.check(
            "Reward on completion only", "a closed ad grants nothing",
            RewardedAdRules.GRANTS_ON_COMPLETION_ONLY &&
                !RewardedAdRules.grant(completed = false) { Unit } &&
                RewardedAdRules.grant(completed = true) { Unit },
            observed(RewardedAdRules.grant(false) { Unit }, false)
        )
    }

    private fun cadenceCases(r: CaseRecorder) {
        val scheduler = InterstitialScheduler()
        val allowedNow = scheduler.canShow(true, previousMissionFailed = false, removeAdsOwned = false)
        val afterFailure = scheduler.canShow(true, previousMissionFailed = true, removeAdsOwned = false)
        scheduler.recordShown()
        val tooSoon = scheduler.canShow(true, previousMissionFailed = false, removeAdsOwned = false)
        repeat(AdsRules.MIN_MISSIONS_BETWEEN) { scheduler.recordMissionEnd() }
        r.check(
            "Interstitial cadence", "once per three missions, never straight after a failure",
            allowedNow && !afterFailure && !tooSoon &&
                scheduler.missionsSinceLast() == AdsRules.MIN_MISSIONS_BETWEEN &&
                scheduler.canShow(true, false, false),
            observed(scheduler.missionsSinceLast(), AdsRules.MIN_MISSIONS_BETWEEN)
        )
        r.check(
            "Remove ads is honoured", "owning the removal disables interstitials permanently",
            !AdsRules.interstitialsEnabled(removeAdsOwned = true) &&
                AdsRules.interstitialsEnabled(removeAdsOwned = false) &&
                !scheduler.canShow(true, false, removeAdsOwned = true),
            observed(AdsRules.interstitialsEnabled(true), false)
        )
        r.check(
            "No pay to win", "every purchasable item also has an in-game earning path",
            AdsRules.NO_PAY_TO_WIN &&
                !AdsRules.isPayToWin("rifle", earnableInGame = setOf("rifle")) &&
                AdsRules.isPayToWin("rifle", earnableInGame = emptySet()),
            observed(AdsRules.isPayToWin("rifle", emptySet()), true)
        )
    }
}
