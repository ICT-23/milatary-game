package com.example.game.valor.launch

import com.example.game.vanguard.CaseRecorder
import com.example.game.vanguard.ContractCase
import com.example.game.vanguard.observed

/** Sections 4 to 7: the money, the privacy, the first ten minutes and the listing. */
object LaunchMonetizationCases {

    fun run(): List<ContractCase> {
        val r = CaseRecorder(LaunchOpsAudit.GROUP_MONETIZATION)
        checkPrinciples(r)
        checkProducts(r)
        checkPlacements(r)
        checkInterstitialGate(r)
        checkRewards(r)
        checkRemoteConfig(r)
        checkOdds(r)
        return r.result()
    }

    private fun checkPrinciples(r: CaseRecorder) {
        r.check(
            "Four fair-play promises",
            "no forced ads, no pay-to-win, transparent odds, parental-friendly",
            MonetizationPrinciples.RULES.size == 4 &&
                !MonetizationPrinciples.FORCED_ADS_DURING_GAMEPLAY &&
                MonetizationPrinciples.EVERY_ITEM_EARNABLE &&
                !MonetizationPrinciples.PAY_TO_WIN &&
                MonetizationPrinciples.ODDS_TRANSPARENT &&
                MonetizationPrinciples.PITY_TIMERS &&
                !MonetizationPrinciples.MANIPULATIVE_COUNTDOWN_PRESSURE,
            observed(MonetizationPrinciples.RULES.size, 4)
        )
        r.check(
            "A purchase must be earnable and honest",
            "hidden odds or a purchase-only item is refused",
            MonetizationPrinciples.acceptsPurchase(earnableInGame = true, randomWithHiddenOdds = false) &&
                !MonetizationPrinciples.acceptsPurchase(earnableInGame = false, randomWithHiddenOdds = false) &&
                !MonetizationPrinciples.acceptsPurchase(earnableInGame = true, randomWithHiddenOdds = true),
            observed(MonetizationPrinciples.acceptsPurchase(true, true), false)
        )
    }

    private fun checkProducts(r: CaseRecorder) {
        val starter = StoreCatalogue.product(StoreCatalogue.STARTER_PACK_ID)
        r.check(
            "Seven store products",
            "Remove Ads, Starter Pack, three medal packs, cosmetics and the optional pass",
            StoreCatalogue.PRODUCTS.size == 7 && StoreCatalogue.consumables().size == 3,
            observed(StoreCatalogue.PRODUCTS.size, 7)
        )
        r.check(
            "Starter pack contents",
            "Remove Ads, 2 weapons, 500 Medals and Steel as one-time low-mid tier",
            starter?.type == ProductType.ONE_TIME &&
                starter.priceTier == "Low-mid" &&
                starter.contents.contains("500 Medals") &&
                starter.contents.contains("2 weapons") &&
                starter.contents.any { it == "Remove Ads" },
            observed(starter?.contents, listOf("Remove Ads", "2 weapons", "500 Medals", "Steel"))
        )
        r.check(
            "Play Billing only, local tiers for India",
            "UPI-friendly checkout and a cheaper starter pack for emerging markets",
            StoreCatalogue.billingIsPlayOnly() &&
                StoreCatalogue.LOCAL_PRICE_TIERS &&
                StoreCatalogue.UPI_FRIENDLY_CHECKOUT &&
                StoreCatalogue.EMERGING_MARKET_STARTER_PACK,
            observed(StoreCatalogue.UPI_FRIENDLY_CHECKOUT, true)
        )
        r.check(
            "Remove Ads covers the starter pack too",
            "buying either product stops interstitials",
            StoreCatalogue.removesInterstitials(StoreCatalogue.REMOVE_ADS_ID) &&
                StoreCatalogue.removesInterstitials(StoreCatalogue.STARTER_PACK_ID) &&
                !StoreCatalogue.removesInterstitials("medal_pack_s"),
            observed(StoreCatalogue.removesInterstitials("medal_pack_s"), false)
        )
    }

    private fun checkPlacements(r: CaseRecorder) {
        r.check(
            "Six placements, four rewarded",
            "double rewards, revive, free crate and extra daily medals are the rewarded pair",
            AdPlacements.ROWS.size == 6 && AdPlacements.rewarded().size == 4,
            observed(AdPlacements.rewarded().size, 4)
        )
        r.check(
            "Banners are not used",
            "the banner row ships switched off so the UI stays clean",
            !AdPlacements.bannersUsed() && !AdPlacements.isUsed("Banner") &&
                AdPlacements.isUsed("Revive"),
            observed(AdPlacements.isUsed("Banner"), false)
        )
        r.check(
            "The plan's three placements are still present",
            "double reward, revive and free crate survive from Section 5.22",
            AdPlacements.isUsed("Double rewards") &&
                AdPlacements.isUsed("Revive") &&
                AdPlacements.isUsed("Free crate") &&
                AdPlacements.MIN_MISSIONS_BETWEEN ==
                com.example.game.valor.plan.AdsRules.MIN_MISSIONS_BETWEEN,
            observed(AdPlacements.MIN_MISSIONS_BETWEEN, 3)
        )
        r.check(
            "Interstitial copy carries all four conditions",
            "between missions, three apart, never after failing, never in the first twenty",
            AdPlacements.interstitial()?.trigger?.contains("Between missions") == true &&
                AdPlacements.interstitial()?.trigger?.contains("3 missions apart") == true &&
                AdPlacements.interstitial()?.trigger?.contains("never after failing") == true &&
                AdPlacements.interstitial()?.trigger?.contains("first 20 minutes") == true,
            observed(AdPlacements.interstitial()?.used, true)
        )
    }

    private fun checkInterstitialGate(r: CaseRecorder) {
        val gate = InterstitialGate()
        val beforeTwenty = gate.canShow(betweenMissions = true, playedMinutes = 19)
        val atTwenty = gate.canShow(betweenMissions = true, playedMinutes = 20)
        val midMission = gate.canShow(betweenMissions = false, playedMinutes = 20)

        gate.recordMissionEnd(failed = true)
        val afterFailure = gate.canShow(betweenMissions = true, playedMinutes = 40)
        gate.recordMissionEnd(failed = false)
        val afterSuccess = gate.canShow(betweenMissions = true, playedMinutes = 40)

        gate.recordShown(playedMinutes = 40)
        val twoMinutesLater = gate.canShow(betweenMissions = true, playedMinutes = 42)
        val fiveMinutesLater = gate.canShow(betweenMissions = true, playedMinutes = 45)

        repeat(AdPlacements.MIN_MISSIONS_BETWEEN) { gate.recordMissionEnd(failed = false) }
        val afterThreeMoreMissions = gate.canShow(betweenMissions = true, playedMinutes = 45)

        gate.removeAdsOwned = true
        val afterPurchase = gate.canShow(betweenMissions = true, playedMinutes = 60)

        r.check(
            "The first twenty minutes are ad-free",
            "19 minutes refused, 20 minutes allowed between missions",
            !beforeTwenty && atTwenty && !midMission,
            observed(atTwenty, true)
        )
        r.check(
            "A failure is never followed by an interstitial",
            "the fail counter holds the ad, the next success releases it",
            !afterFailure && afterSuccess,
            observed(afterFailure, false)
        )
        r.check(
            "One interstitial per five minutes and three missions",
            "two minutes later and five minutes later both wait for the mission counter",
            !twoMinutesLater && !fiveMinutesLater && afterThreeMoreMissions,
            observed(afterThreeMoreMissions, true)
        )
        r.check(
            "Remove Ads closes the gate for good",
            "the purchase wins over every other condition",
            !afterPurchase && gate.interstitialCount() == 1,
            observed(afterPurchase, false)
        )
    }

    private fun checkRewards(r: CaseRecorder) {
        r.check(
            "Free crate cools down for four hours",
            "14,400 seconds, and the button hides until it is ready",
            AdPlacements.FREE_CRATE_COOLDOWN_SECONDS == 14_400 &&
                CrateCooldown.ready(lastClaimSeconds = 0, nowSeconds = 10) &&
                CrateCooldown.ready(lastClaimSeconds = 1_000, nowSeconds = 15_400) &&
                !CrateCooldown.ready(lastClaimSeconds = 1_000, nowSeconds = 15_399) &&
                CrateCooldown.remainingSeconds(1_000, 15_399) == 1,
            observed(CrateCooldown.remainingSeconds(1_000, 15_399), 1)
        )
        r.check(
            "Extra daily medals stop at three",
            "two medals each, nothing paid past the cap",
            DailyMedalAds.canWatch(0) && DailyMedalAds.canWatch(2) && !DailyMedalAds.canWatch(3) &&
                DailyMedalAds.medalsFor(0) == 2 && DailyMedalAds.medalsFor(3) == 0 &&
                DailyMedalAds.remainingToday(1) == 2,
            observed(DailyMedalAds.medalsFor(3), 0)
        )
        r.check(
            "Revive is once per mission at half health",
            "a second offer is refused and 100 HP comes back as 50",
            RevivePolicy.canOffer(usedThisMission = false) &&
                !RevivePolicy.canOffer(usedThisMission = true) &&
                RevivePolicy.hpAfterRevive(100) == 50 &&
                RevivePolicy.hpAfterRevive(75) == 38,
            observed(RevivePolicy.hpAfterRevive(100), 50)
        )
        r.check(
            "Double rewards double Cash and XP",
            "x2 on both, and a negative base never pays out",
            DoubleRewardPolicy.cash(120) == 240 &&
                DoubleRewardPolicy.xp(7) == 14 &&
                DoubleRewardPolicy.cash(-40) == 0,
            observed(DoubleRewardPolicy.cash(120), 240)
        )
    }

    private fun checkRemoteConfig(r: CaseRecorder) {
        val shipped = RemoteConfig()
        val tuned = RemoteConfig(mapOf(RemoteConfig.AD_MIN_MISSIONS to "5"))
        r.check(
            "Six remote groups carry safe defaults",
            "ad frequency, rewards, seeds, event dates, hotfix multipliers and odds",
            RemoteConfig.GROUPS.size == 6 && shipped.isSafe(),
            observed(RemoteConfig.GROUPS.size, 6)
        )
        r.check(
            "A remote value can change the ad schedule",
            "the override wins and an unknown key is ignored",
            tuned.interstitialMissionsBetween() == 5 &&
                shipped.interstitialMissionsBetween() == 3 &&
                RemoteConfig(mapOf("nonsense" to "1")).raw("nonsense") == null,
            observed(tuned.interstitialMissionsBetween(), 5)
        )
        r.check(
            "The shipped defaults match the shipped rules",
            "as-finished numbers are the same in both places",
            shipped.interstitialMinutesBetween() == AdPlacements.MIN_MINUTES_BETWEEN &&
                shipped.interstitialFirstSessionMinutes() == AdPlacements.FIRST_SESSION_MINUTES &&
                shipped.pityAfter() == RemoteConfig.DEFAULT_PITY_AFTER,
            observed(shipped.interstitialMinutesBetween(), 5)
        )
        r.check(
            "Seeds download or derive",
            "the default is 'auto', so the build never needs a seed list to run",
            !shipped.hasDownloadedSeeds() &&
                RemoteConfig(mapOf(RemoteConfig.DAILY_OP_SEED_LIST to "7,8,9"))
                    .seedList() == listOf(7L, 8L, 9L),
            observed(shipped.seedList(), emptyList<Long>())
        )
        r.check(
            "Event dates and hotfix multipliers parse",
            "two named keys survive the round trip",
            RemoteConfig(
                mapOf(
                    RemoteConfig.EVENT_DATES to "winter_event=2026-12-20",
                    RemoteConfig.DIFFICULTY_HOTFIX to "TOURNAMENT=0.9"
                )
            ).eventDates()["winter_event"] == "2026-12-20" &&
                RemoteConfig(mapOf(RemoteConfig.DIFFICULTY_HOTFIX to "TOURNAMENT=0.9"))
                    .hotfixMultipliers()["TOURNAMENT"] == 0.9,
            observed(shipped.eventDates().size, 0)
        )
    }

    private fun checkOdds(r: CaseRecorder) {
        val shipped = RemoteConfig().odds()
        r.check(
            "The shipped crate publishes whole odds",
            "the default weights add up to 1.0",
            shipped != null && shipped.isTransparent && shipped.tiers() == listOf("standard"),
            observed(shipped?.total, 1.0)
        )
        r.check(
            "Partial odds are refused",
            "0.5 alone and 0.9 combined both fail the transparency rule",
            CrateOdds.parse("common=0.5")?.isTransparent == false &&
                CrateOdds.parse("common=0.6,rare=0.3")?.isTransparent == false &&
                CrateOdds.parse("common=0.6,rare=0.3,epic=0.1")?.isTransparent == true,
            observed(CrateOdds.parse("common=0.5")?.isTransparent, false)
        )
        val pity = PityCounter(threshold = 3)
        val afterOne = pity.record(miss = true)
        val afterTwo = pity.record(miss = true)
        val afterThree = pity.record(miss = true)
        pity.reset()
        r.check(
            "Pity timer fires on the threshold",
            "three misses guarantee the next roll, and a reset clears it",
            !afterOne && !afterTwo && afterThree && !pity.guaranteed && pity.misses == 0,
            observed(afterThree, true)
        )
    }
}

/** Section 5: documents, the Play checklist, the consent flow and the IP rules. */
object LaunchPrivacyCases {

    fun run(): List<ContractCase> {
        val r = CaseRecorder(LaunchOpsAudit.GROUP_PRIVACY)
        checkDocuments(r)
        checkChecklist(r)
        checkConsent(r)
        checkIp(r)
        return r.result()
    }

    private fun checkDocuments(r: CaseRecorder) {
        r.check(
            "Five legal documents",
            "privacy policy, terms, deletion instructions, support email and website",
            LegalDocuments.ALL.size == 5 && LegalDocuments.missingFrom(emptyList()).size == 5,
            observed(LegalDocuments.ALL.size, 5)
        )
    }

    private fun checkChecklist(r: CaseRecorder) {
        val ids = PlayChecklist.ITEMS.map { it.id }
        r.check(
            "Ten Play requirements",
            "account through store assets, each with a verification step",
            PlayChecklist.ITEMS.size == 10 &&
                PlayChecklist.ITEMS.all { it.verification.isNotBlank() } &&
                !PlayChecklist.isComplete(ids.dropLast(1)),
            observed(PlayChecklist.ITEMS.size, 10)
        )
        r.check(
            "Only two permissions are allowed",
            "INTERNET and ACCESS_NETWORK_STATE; anything else is flagged",
            PlayChecklist.ALLOWED_PERMISSIONS.size == 2 &&
                PlayChecklist.unnecessaryPermissions(
                    listOf("android.permission.INTERNET", "android.permission.CAMERA")
                ) == listOf("android.permission.CAMERA"),
            observed(PlayChecklist.ALLOWED_PERMISSIONS.size, 2)
        )
        r.check(
            "Ads declared, 64-bit and Teen rated",
            "the three store answers the checklist depends on",
            PlayChecklist.DECLARES_ADS && PlayChecklist.SIXTY_FOUR_BIT &&
                PlayChecklist.TARGET_RATING == "Teen / 12+",
            observed(PlayChecklist.TARGET_RATING, "Teen / 12+")
        )
    }

    private fun checkConsent(r: CaseRecorder) {
        val gate = ConsentGate()
        val openedAt = gate.step
        val afterAge = gate.chooseAge(15)
        val adult = gate.submit(personalizedAds = true, analytics = true)

        r.check(
            "Age gate then consent dialog then done",
            "three steps in the documented order",
            openedAt == ConsentStep.AGE_GATE &&
                afterAge == ConsentStep.CONSENT_DIALOG &&
                gate.step == ConsentStep.COMPLETE &&
                adult.analyticsAllowed && adult.personalizedAdsAllowed && adult.decided,
            observed(afterAge, ConsentStep.CONSENT_DIALOG)
        )

        val under = ConsentGate()
        under.chooseAge(11)
        val child = under.submit(personalizedAds = true, analytics = true)
        val unknown = ConsentGate()
        unknown.chooseAge(null)
        val unknownState = unknown.submit(personalizedAds = true, analytics = true)

        r.check(
            "Under 13 gets non-personalized ads and no analytics",
            "both answers are forced down whatever the dialog said",
            child.ageBand == AgeBand.UNDER_13 &&
                !child.personalizedAdsAllowed && !child.analyticsAllowed &&
                child.nonPersonalizedAdsOnly && child.decided,
            observed(child.describe(), "Under 13 - non-personalized")
        )
        r.check(
            "An unknown age is treated like under 13",
            "no answer never upgrades to personalized ads",
            unknownState.ageBand == AgeBand.UNKNOWN &&
                !unknownState.personalizedAdsAllowed && !unknownState.analyticsAllowed &&
                ConsentGate.requiresNonPersonalizedAds(AgeBand.UNKNOWN),
            observed(unknownState.ageBand, AgeBand.UNKNOWN)
        )
        r.check(
            "Privacy settings stay reachable and re-apply the age rule",
            "editing later cannot lift the under-13 restriction",
            adult.privacySettingsEntryVisible &&
                under.editInSettings(personalizedAds = true, analytics = true) == child,
            observed(under.editInSettings(true, true), child)
        )
    }

    private fun checkIp(r: CaseRecorder) {
        r.check(
            "Original IP with the alternate award name",
            "Medal of Valor is the shipped string and the real award is never a brand",
            IpSafety.safeToShip() &&
                !IpSafety.safeToShip(showsLogos = true) &&
                !IpSafety.safeToShip(usesAwardAsBrand = true) &&
                IpSafety.awardName() == "Medal of Valor" &&
                IpSafety.awardName("Cross of Valour") == "Cross of Valour",
            observed(IpSafety.awardName(), "Medal of Valor")
        )
        r.check(
            "Licenses file and credits screen",
            "ASSET_LICENSES.md is the licence log the section requires",
            IpSafety.LICENSES_FILE == "ASSET_LICENSES.md" && IpSafety.CREDITS_SCREEN,
            observed(IpSafety.LICENSES_FILE, "ASSET_LICENSES.md")
        )
        r.check(
            "Fictional modern factions only",
            "Northern Coalition and Red Meridian, no real actor in the text",
            ContentNotes.FICTIONAL_MODERN_FACTIONS.size == 2 &&
                ContentNotes.usesFictionalFaction("Red Meridian") &&
                ContentNotes.mentionsRealActor("the Nazi party marches") &&
                !ContentNotes.mentionsRealActor("Northern Coalition patrols the ridge") &&
                !ContentNotes.REAL_LIVING_POLITICAL_FIGURES,
            observed(ContentNotes.mentionsRealActor("Northern Coalition"), false)
        )
    }
}

/** Section 6: the first ten minutes and the retention hooks. */
object LaunchFtueCases {

    fun run(): List<ContractCase> {
        val r = CaseRecorder(LaunchOpsAudit.GROUP_FTUE)
        checkTimeline(r)
        checkRules(r)
        checkRetention(r)
        return r.result()
    }

    private fun checkTimeline(r: CaseRecorder) {
        r.check(
            "Seven steps on the document's clock",
            "0:00, 0:30, 1:00, 2:00, 4:00, 7:00 and 9:00",
            FtueFlow.STEPS.size == 7 &&
                FtueFlow.clockLabels() ==
                listOf("0:00", "0:30", "1:00", "2:00", "4:00", "7:00", "9:00"),
            observed(FtueFlow.clockLabels(), "0:00 ... 9:00")
        )
        r.check(
            "Steps resolve by elapsed time",
            "45 seconds is the cinematic, ten minutes is Mission 4",
            FtueFlow.stepAt(45)?.label == "Cinematic" &&
                FtueFlow.stepAt(600)?.label == "Mission 4" &&
                FtueFlow.stepAt(0)?.label == "Splash",
            observed(FtueFlow.stepAt(600)?.label, "Mission 4")
        )
        r.check(
            "Cinematic and character creator limits",
            "20 skippable seconds and a three-tap creator with randomize",
            FtueFlow.CINEMATIC_SECONDS == 20 && FtueFlow.CINEMATIC_SKIPPABLE &&
                FtueFlow.CHARACTER_CREATOR_TAPS == 3 && FtueFlow.RANDOMIZE_BUTTON &&
                FtueFlow.LANGUAGE_PICKER_IN_FTUE,
            observed(FtueFlow.CHARACTER_CREATOR_TAPS, 3)
        )
    }

    private fun checkRules(r: CaseRecorder) {
        r.check(
            "No ad in the first twenty minutes",
            "1,199 seconds refused, 1,200 allowed - the same number as Section 4.3",
            !FtueRules.adsAllowed(1_199) && FtueRules.adsAllowed(1_200) &&
                FtueRules.NO_AD_MINUTES == AdPlacements.FIRST_SESSION_MINUTES,
            observed(FtueRules.NO_AD_MINUTES, 20)
        )
        r.check(
            "Prompts dismissible, controls editable, tournament optional",
            "a dismissed prompt stays dismissed and the tournament never blocks",
            FtueRules.TUTORIAL_PROMPTS_DISMISSIBLE && FtueRules.TUTORIAL_PROMPTS_SKIPPABLE &&
                !FtueRules.showsAgainAfterDismissal() &&
                FtueRules.controlsScreenReachableAtFirstFrame() &&
                !FtueRules.tournamentBlocksStory() &&
                !com.example.game.valor.plan.TournamentRules.BLOCKS_STORY_PROGRESSION,
            observed(FtueRules.tournamentBlocksStory(), false)
        )
    }

    private fun checkRetention(r: CaseRecorder) {
        r.check(
            "Seven retention hooks",
            "login cycle, Daily Operation, weekly challenge, rank goals, teaser, medals, push",
            RetentionHooks.ALL.size == 7 && RetentionHooks.rankGoalsOnResults() &&
                RetentionHooks.hookNamed("Push notifications") != null,
            observed(RetentionHooks.ALL.size, 7)
        )
        val cycle = DailyLoginCycle()
        val claimed = (1..DailyLoginCycle.DAYS).map { cycle.claim() }
        val wrapped = cycle.claim()
        r.check(
            "Seven-day login cycle wraps",
            "days 1-7 paid once, the eighth claim starts a new cycle at day 1",
            claimed == (1..7).toList() && wrapped == 1 && cycle.completedCycles == 1 &&
                cycle.rewardId(3) == "daily_login_day_3",
            observed(wrapped, 1)
        )
        val push = PushNotificationPolicy()
        val blockedOptOut = push.canSend()
        push.enabled(true)
        val first = push.recordSent()
        val second = push.recordSent()
        push.newDay()
        val nextDay = push.canSend()
        r.check(
            "Push needs opt-in and sends once a day",
            "no opt-in is silent, one message per day at most",
            !blockedOptOut && first && !second && nextDay &&
                push.message() == "Your daily op is ready",
            observed(second, false)
        )
    }
}

/** Section 7: the store listing specs and the copy limits. */
object LaunchStoreCases {

    fun run(): List<ContractCase> {
        val r = CaseRecorder(LaunchOpsAudit.GROUP_STORE)
        checkAssets(r)
        checkCopy(r)
        checkPlan(r)
        return r.result()
    }

    private fun checkAssets(r: CaseRecorder) {
        r.check(
            "Seven asset specs",
            "icon, feature graphic, phone and tablet shots, trailer and two descriptions",
            StoreListing.ASSETS.size == 7 &&
                StoreListing.assetNamed("App icon")?.spec?.contains("512x512") == true &&
                StoreListing.assetNamed("Feature graphic")?.spec == "1024x500",
            observed(StoreListing.ASSETS.size, 7)
        )
        r.check(
            "Icon and feature graphic dimensions",
            "512 square, 1024 by 500",
            StoreListing.iconValid(512, 512) && !StoreListing.iconValid(256, 256) &&
                StoreListing.featureGraphicValid(1024, 500) &&
                !StoreListing.featureGraphicValid(1024, 512),
            observed(StoreListing.iconValid(256, 256), false)
        )
    }

    private fun checkCopy(r: CaseRecorder) {
        r.check(
            "Short description fits 80 characters",
            "66 characters for the documented line",
            ListingCopy.SHORT_DESCRIPTION.length == 66 &&
                ListingCopy.shortDescriptionValid() &&
                !ListingCopy.shortDescriptionValid("x".repeat(81)),
            observed(ListingCopy.SHORT_DESCRIPTION.length, 66)
        )
        r.check(
            "Five-part full description inside 4,000 characters",
            "hook, features, replay, fair play, call to action",
            ListingCopy.FULL_DESCRIPTION_OUTLINE.size == 5 &&
                ListingCopy.fullDescriptionValid() &&
                ListingCopy.FULL_MAX_CHARS == 4_000,
            observed(ListingCopy.FULL_DESCRIPTION_OUTLINE.size, 5)
        )
        r.check(
            "Nine ASO keywords, no competitor brands",
            "the documented keyword list, filtered for brand names",
            ListingCopy.keywordCount() == 9 &&
                ListingCopy.KEYWORDS.all { ListingCopy.keywordAllowed(it) } &&
                !ListingCopy.keywordAllowed("call of duty clone"),
            observed(ListingCopy.keywordCount(), 9)
        )
    }

    private fun checkPlan(r: CaseRecorder) {
        r.check(
            "Eight planned screenshots",
            "hero shot through weather showcase",
            StoreListing.SCREENSHOT_PLAN.size == 8 &&
                StoreListing.SCREENSHOT_PLAN.first() == "Cinematic hero shot with logo",
            observed(StoreListing.SCREENSHOT_PLAN.size, 8)
        )
        r.check(
            "Phone shots 6-8, tablets 4, landscape at 1920x1080",
            "the counts and the resolution the section prints",
            StoreListing.phoneScreenshotCountValid(6) &&
                StoreListing.phoneScreenshotCountValid(8) &&
                !StoreListing.phoneScreenshotCountValid(5) &&
                StoreListing.screenshotResolutionValid(1920, 1080) &&
                !StoreListing.screenshotResolutionValid(1080, 1920),
            observed(StoreListing.phoneScreenshotCountValid(5), false)
        )
        r.check(
            "Trailer runs 30-45 seconds with four beats",
            "hook, variety, progression, logo",
            StoreListing.TRAILER_BEATS.size == 4 &&
                StoreListing.trailerLengthValid(30) &&
                StoreListing.trailerLengthValid(45) &&
                !StoreListing.trailerLengthValid(29) &&
                !StoreListing.trailerLengthValid(46),
            observed(StoreListing.trailerLengthValid(46), false)
        )
        r.check(
            "The listing is ready only when every asset passes",
            "a five-shot phone pack blocks the upload",
            StoreListing.listingReady(7, 4, 40) &&
                !StoreListing.listingReady(5, 4, 40) &&
                !StoreListing.listingReady(7, 3, 40) &&
                !StoreListing.listingReady(7, 4, 60),
            observed(StoreListing.listingReady(5, 4, 40), false)
        )
    }
}
