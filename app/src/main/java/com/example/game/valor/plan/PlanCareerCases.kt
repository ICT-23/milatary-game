package com.example.game.valor.plan

import com.example.game.valor.balance.RankTable
import com.example.game.vanguard.CaseRecorder
import com.example.game.vanguard.ContractCase
import com.example.game.vanguard.approxEquals
import com.example.game.vanguard.observed

/** Sections 5.16 to 5.18: XP and ranks, the economy, and the gunsmith. */
object PlanCareerCases {

    fun run(): List<ContractCase> {
        val r = CaseRecorder(ValorPlanAudit.GROUP_OPS)
        xpCases(r)
        economyCases(r)
        gunsmithCases(r)
        return r.result()
    }

    private fun xpCases(r: CaseRecorder) {
        val service = ProgressionService()
        val small = service.addXp(100, XpReason.ENEMY_KILL, "rifleman")
        r.check(
            "XP adds", "an award lands and the total follows",
            small.accepted && small.xpAfter == 100 && service.xp == 100,
            observed(small.xpAfter, 100)
        )
        val negative = service.addXp(-50, XpReason.ENEMY_KILL, log = DebugLog())
        r.check(
            "No negative XP", "a negative award is refused and the total is untouched",
            !negative.accepted && service.xp == 100 && negative.reason != null,
            observed(negative.reason, "rejection reason")
        )
        val big = ProgressionService().addXp(150_000, XpReason.MISSION_COMPLETE, "act 3")
        r.check(
            "Multi-rank up", "one large award crosses every rung it passes",
            big.accepted && big.promotions.size >= 2 &&
                big.promotions.first().cumulativeXp!! < big.promotions.last().cumulativeXp!!,
            observed(big.promotions.map { it.index }, ">= 2 promotions")
        )
        val ranked = ProgressionService()
        ranked.addXp(150_000, XpReason.MISSION_COMPLETE)
        r.check(
            "Rank derived from XP", "rank is read from the ladder, never stored separately",
            ranked.rank == RankTable.rankAt(150_000) &&
                ranked.rank.cumulativeXp != null &&
                ranked.rewardsFor(ranked.rank.index),
            observed(ranked.rank.title, RankTable.rankAt(150_000).title)
        )
        val once = ProgressionService()
        val first = once.addXp(1_500, XpReason.MISSION_COMPLETE)
        val second = once.addXp(1, XpReason.ENEMY_KILL)
        r.check(
            "Rewards once", "a crossed rank is rewarded on the crossing only",
            first.promotions.isNotEmpty() && second.promotions.isEmpty() &&
                first.promotions.all { once.rewardsFor(it.index) },
            observed(second.promotions.size, 0)
        )
        r.check(
            "Kill XP band", "10 to 100 by enemy type",
            ProgressionRules.KILL_XP_RANGE == 10..100 &&
                ProgressionService().killXpFor(5) == 10 &&
                ProgressionService().killXpFor(500) == 100,
            observed(ProgressionService().killXpFor(5), 10)
        )
        r.check(
            "Headshot bonus", "plus five on top of the kill award",
            ProgressionService().headshotBonus(20) == 20 + RankTable.HEADSHOT_BONUS_XP,
            observed(ProgressionService().headshotBonus(20), 25)
        )
        r.check(
            "Bonus objective band", "50 to 200",
            ProgressionRules.BONUS_XP_RANGE == 50..200 &&
                ProgressionService().bonusObjectiveXp(10) == 50 &&
                ProgressionService().bonusObjectiveXp(900) == 200,
            observed(ProgressionService().bonusObjectiveXp(900), 200)
        )
        r.check(
            "No XP cases", "friendly kill, debug kill, respawn farm and post-mission kill earn nothing",
            ProgressionService().killEarnsXp(KillXpContext(Team.PLAYER, missionPlaying = true)) &&
                !ProgressionService().killEarnsXp(KillXpContext(Team.ALLY, missionPlaying = true)) &&
                !ProgressionService().killEarnsXp(KillXpContext(Team.PLAYER, true, debugKill = true)) &&
                !ProgressionService().killEarnsXp(KillXpContext(Team.PLAYER, true, fromRespawn = true)) &&
                !ProgressionService().killEarnsXp(KillXpContext(Team.PLAYER, true, friendly = true)) &&
                !ProgressionService().killEarnsXp(KillXpContext(Team.PLAYER, missionPlaying = false)),
            observed(ProgressionService().killEarnsXp(KillXpContext(Team.ALLY, true)), false)
        )
        val prestige = ProgressionService(50_000)
        val kept = prestige.prestige(setOf("camo"), mapOf("ar" to 7))
        r.check(
            "Prestige", "XP resets, cosmetics and mastery survive",
            prestige.xp == 0 && prestige.prestigeCount == 1 &&
                kept.keptCosmetics == setOf("camo") && kept.keptMastery["ar"] == 7,
            observed(prestige.xp, 0)
        )
        r.check(
            "Unlock service", "one place decides whether a rank opens an item",
            UnlockService.isUnlocked(5, 5) &&
                !UnlockService.isUnlocked(4, 5) &&
                UnlockService.lockedBy(4, 5) &&
                UnlockService.requiredRankLabel(5).contains("5"),
            observed(UnlockService.isUnlocked(4, 5), false)
        )
    }

    private fun economyCases(r: CaseRecorder) {
        r.check(
            "No negative balance", "a wallet clamps at zero on both paths",
            Wallet().with(PlanCurrency.CASH, -50).cash == 0 &&
                !Wallet(cash = 50).canAfford(PlanCurrency.CASH, 100) &&
                EconomyService.MAX_BALANCE == Int.MAX_VALUE,
            observed(Wallet().with(PlanCurrency.CASH, -50).cash, 0)
        )
        val wallet = EconomyService(Wallet(cash = 500, steel = 20))
        val bought = wallet.spend(PlanCurrency.CASH, 200, "optic")
        r.check(
            "Atomic spend", "a successful purchase deducts exactly once",
            bought.success && wallet.wallet.cash == 300 && wallet.completedPurchases == 1,
            observed(wallet.wallet.cash, 300)
        )
        val poor = wallet.spend(PlanCurrency.CASH, 10_000, "rifle")
        r.check(
            "Insufficient funds", "nothing is charged when the wallet is short",
            !poor.success && wallet.wallet.cash == 300,
            observed(poor.reason, "Insufficient funds")
        )
        val gated = wallet.spend(
            PlanCurrency.CASH, 10, "owned",
            gate = PurchaseGate(alreadyOwned = true)
        )
        r.check(
            "Purchase gates", "rank lock, ownership and full inventory block before money moves",
            !gated.success && gated.reason == "Already owned" &&
                PurchaseGate(rankLocked = true).blockedReason() == "Rank locked" &&
                PurchaseGate(inventoryFull = true).blockedReason() == "Inventory full" &&
                PurchaseGate(alreadyOwned = true, consumable = true).blockedReason() == null,
            observed(gated.reason, "Already owned")
        )
        val grantFail = EconomyService(Wallet(cash = 500))
        val failedGrant = grantFail.spend(
            PlanCurrency.CASH, 400, "blueprint", grant = { false }
        )
        val saveFail = EconomyService(Wallet(cash = 500))
        val failedSave = saveFail.spend(
            PlanCurrency.CASH, 400, "blueprint", save = { false }
        )
        r.check(
            "Rollback", "a failed grant or save restores the balance",
            !failedGrant.success && grantFail.wallet.cash == 500 &&
                !failedSave.success && saveFail.wallet.cash == 500,
            observed(grantFail.wallet.cash, 500)
        )
        val busy = EconomyService(Wallet(cash = 500))
        var nested: PurchaseResult? = null
        val outer = busy.spend(
            PlanCurrency.CASH, 100, "outer",
            grant = {
                nested = busy.spend(PlanCurrency.CASH, 100, "inner")
                true
            }
        )
        r.check(
            "Duplicate tap guard", "a second tap while processing is ignored",
            outer.success && nested?.success == false && busy.wallet.cash == 400,
            observed(nested?.reason, "Transaction already in progress")
        )
        val rich = EconomyService(Wallet(cash = Int.MAX_VALUE - 5))
        val capped = rich.earn(PlanCurrency.CASH, 100, "cap")
        r.check(
            "Earn is bounded", "a negative earn is refused and the balance never wraps",
            capped.cash == Int.MAX_VALUE &&
                rich.earn(PlanCurrency.CASH, -20, "bad").cash == Int.MAX_VALUE,
            observed(capped.cash, Int.MAX_VALUE)
        )
        var rewarded = 0
        r.check(
            "Rewarded ad completion", "a closed ad grants nothing",
            !RewardedAdRules.grant(completed = false) { rewarded += 1 } &&
                RewardedAdRules.grant(completed = true) { rewarded += 1 } &&
                rewarded == 1,
            observed(rewarded, 1)
        )
        r.check(
            "Dismantle", "half the resources come back, never the fitted parts",
            DismantleRules.refund(1_000) == 500 &&
                !DismantleRules.returnsEquippedParts() &&
                approxEquals(DismantleRules.REFUND_FRACTION, 0.5f),
            observed(DismantleRules.refund(1_000), 500)
        )
    }

    private fun gunsmithCases(r: CaseRecorder) {
        r.check(
            "Levels 1 to 10", "only the next rung is purchasable and 10 is the cap",
            GunsmithRules.MIN_LEVEL == 1 && GunsmithRules.MAX_LEVEL == 10 &&
                GunsmithRules.canReach(3, 4) &&
                !GunsmithRules.canReach(3, 5) &&
                GunsmithRules.canUpgrade(9) &&
                !GunsmithRules.canUpgrade(10),
            observed(
                listOf(GunsmithRules.canReach(3, 4), GunsmithRules.canReach(3, 5)),
                listOf(true, false)
            )
        )
        r.check(
            "Costs come from the table", "an upgrade costs cash and steel, never a constant here",
            GunsmithRules.upgradeCost(1_000, 1) > 0f &&
                GunsmithRules.steelCost(1) == 2 &&
                approxEquals(GunsmithRules.damageAt(20f, 1) >= 20f, true),
            observed(GunsmithRules.steelCost(1), 2)
        )
        val live = WeaponStats(20f, 10f, 100f, 2.5f, 50f, 60f, 0.4f, 30, 30f)
        val preview = WeaponStats(22f, 9f, 100f, 2.5f, 50f, 60f, 0.4f, 30, 30f)
        r.check(
            "Preview only", "the comparison never writes to the live weapon",
            GunsmithRules.preview(live, preview) == preview &&
                GunsmithRules.liveAfterPreview(live) == live &&
                GunsmithRules.confirmPreview(live, preview, confirmed = false) == live &&
                GunsmithRules.confirmPreview(live, preview, confirmed = true) == preview &&
                !GunsmithRules.PREVIEW_CHANGES_REAL_STATS,
            observed(GunsmithRules.liveAfterPreview(live).damage, 20f)
        )
        r.check(
            "Skins are cosmetic", "a camo never moves a stat",
            !GunsmithRules.skinsChangeStats() && GunsmithRules.SKINS_ARE_COSMETIC,
            observed(GunsmithRules.skinsChangeStats(), false)
        )
        r.check(
            "Craft flow", "eight stages in order and no skipping",
            GunsmithRules.CRAFT_STAGES.map { it.order } == (1..8).toList() &&
                CraftStage.entries.first().order == 1 &&
                CraftStage.entries.last().order == 8,
            observed(CraftStage.entries.size, 8)
        )
        val flow = CraftFlow()
        var steps = 0
        while (flow.advance() != null) steps += 1
        r.check(
            "Craft flow runs to the end", "the flow stops at Equip and refuses another step",
            steps == 7 && flow.stage == CraftStage.EQUIP && flow.advance() == null,
            observed(steps, 7)
        )
        val blueprint = BlueprintState(piecesNeeded = 3)
        val halfway = blueprint.collect(2)
        val crafted = blueprint.craft()
        blueprint.collect(1)
        r.check(
            "Blueprint pieces", "N pieces unlock the craft, which happens once",
            !halfway && !crafted && blueprint.unlocked &&
                blueprint.craft() && !blueprint.craft(),
            observed(blueprint.pieces, 3)
        )
        r.check(
            "Dismantle refund", "the gunsmith refunds the same half",
            GunsmithRules.dismantleRefund(2_000) == DismantleRules.refund(2_000),
            observed(GunsmithRules.dismantleRefund(2_000), 1_000)
        )
    }
}
