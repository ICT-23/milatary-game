package com.example.game.valor.plan

import com.example.game.valor.balance.AttachmentCatalog
import com.example.game.valor.balance.AttachmentSlot
import com.example.game.valor.balance.AttachmentSpec
import com.example.game.valor.balance.ModKey
import com.example.game.valor.balance.FriendlyFirePolicy
import com.example.game.vanguard.CaseRecorder
import com.example.game.vanguard.ContractCase
import com.example.game.vanguard.approxEquals
import com.example.game.vanguard.observed

/** Sections 5.7 to 5.9: attachments, cover and explosions. */
object PlanFieldCases {

    fun run(): List<ContractCase> {
        val r = CaseRecorder(ValorPlanAudit.GROUP_FIELD)
        attachmentCases(r)
        coverCases(r)
        explosionCases(r)
        return r.result()
    }

    private fun part(slot: AttachmentSlot, name: String, vararg mods: Pair<ModKey, Float>) =
        AttachmentSpec(slot, name, mods.toMap(), steel = 0, parts = 0, cash = 0, unlockRank = 1)

    private val baseStats = WeaponStats(
        damage = 20f, recoil = 10f, rangeMeters = 100f, reloadSeconds = 2.5f,
        accuracy = 50f, adsFov = 60f, adsSpreadMult = 0.4f, magSize = 30, noiseRadiusMeters = 30f
    )

    private fun attachmentCases(r: CaseRecorder) {
        val optic = part(AttachmentSlot.OPTIC, "Trial optic", ModKey.ACCURACY_PCT to 10f)
        val optic2 = part(AttachmentSlot.OPTIC, "Second optic", ModKey.ACCURACY_PCT to 5f)
        val suppressor = part(
            AttachmentSlot.MUZZLE, "Suppressor",
            ModKey.NOISE_MULT to 0.3f, ModKey.DAMAGE_MULT to 0.95f
        )

        r.check(
            "One per slot", "a second optic is refused and the loadout is unchanged",
            !AttachmentRules.canFit(listOf(optic), optic2) &&
                AttachmentRules.equip(listOf(optic), optic2).size == 1 &&
                AttachmentRules.opticsFitted(listOf(optic)) == 1,
            observed(AttachmentRules.equip(listOf(optic), optic2).size, 1)
        )
        val tuned = AttachmentRules.recompute(baseStats, listOf(optic))
        r.check(
            "Recompute from base", "(base + add) x mul, so +10% accuracy is 55",
            approxEquals(tuned.accuracy, 55f) && approxEquals(tuned.damage, 20f) &&
                approxEquals(tuned.adsFov, 60f),
            observed(tuned.accuracy, 55f)
        )
        val clamped = AttachmentRules.recompute(
            baseStats,
            listOf(part(AttachmentSlot.AMMO, "Heavy", ModKey.DAMAGE_MULT to 3f)),
            WeaponStatBounds(damage = StatBounds(0f, 25f))
        )
        r.check(
            "Clamped to bounds", "a 3x damage part still respects the data maximum",
            approxEquals(clamped.damage, 25f),
            observed(clamped.damage, 25f)
        )
        r.check(
            "Round trip is identity", "equipping then removing restores the base numbers",
            AttachmentRules.roundTripIsIdentity(baseStats, optic) &&
                AttachmentRules.recompute(baseStats, emptyList()) == baseStats,
            observed(AttachmentRules.recompute(baseStats, emptyList()), "base stats")
        )
        val suppressed = AttachmentRules.recompute(baseStats, listOf(suppressor))
        r.check(
            "Suppressor", "noise x0.3, damage x0.95, muzzle flash hidden",
            approxEquals(suppressed.noiseRadiusMeters, 9f) &&
                approxEquals(suppressed.damage, 19f) &&
                AttachmentRules.SUPPRESSOR_HIDES_MUZZLE_FLASH,
            observed(suppressed, "noise 9 / damage 19")
        )
        r.check(
            "No-suppressor weapon", "the flag blocks the part even with a free muzzle slot",
            !AttachmentRules.suppressorAllowed(noSuppressorFlag = true) &&
                !AttachmentRules.suppressorFits(emptyList(), noSuppressorFlag = true) &&
                AttachmentRules.suppressorFits(emptyList(), noSuppressorFlag = false),
            observed(AttachmentRules.suppressorAllowed(true), false)
        )
        val book = AttachmentBook()
        book.equip("ar", optic)
        r.check(
            "Attachments persist per weapon", "the same rifle keeps its optic, a second does not",
            book.fitted("ar").size == 1 && book.fitted("ak").isEmpty() &&
                book.remove("ar", AttachmentSlot.OPTIC).isEmpty(),
            observed(book.fitted("ar").size, 1)
        )
        r.check(
            "Catalogue", "the part list is data and starts with an optic",
            AttachmentCatalog.ATTACHMENTS.isNotEmpty() &&
                AttachmentCatalog.ATTACHMENTS.first().slot == AttachmentSlot.OPTIC,
            observed(AttachmentCatalog.ATTACHMENTS.size, "> 0")
        )
    }

    private fun coverCases(r: CaseRecorder) {
        r.check(
            "Snap gate", "within 1.2 m, facing under 70 degrees, auto-snap on",
            CoverRules.canSnap(1.2f, 69f, autoSnapSetting = true) &&
                !CoverRules.canSnap(1.3f, 69f, true) &&
                !CoverRules.canSnap(1f, 71f, true) &&
                !CoverRules.canSnap(1f, 10f, false),
            observed(CoverRules.canSnap(1.3f, 69f, true), false)
        )
        r.check(
            "Cover stance", "low cover crouches, high cover does not",
            CoverRules.crouchesAt(CoverHeight.LOW) && !CoverRules.crouchesAt(CoverHeight.HIGH),
            observed(CoverRules.crouchesAt(CoverHeight.LOW), true)
        )
        r.check(
            "Peek", "peeking exposes half of a low cover and all of a high one",
            approxEquals(CoverRules.peekedHeightFraction(CoverHeight.LOW, peeking = true), 0.5f) &&
                approxEquals(CoverRules.peekedHeightFraction(CoverHeight.HIGH, peeking = true), 1f) &&
                approxEquals(CoverRules.peekedHeightFraction(CoverHeight.LOW, peeking = false), 0f),
            observed(CoverRules.peekedHeightFraction(CoverHeight.LOW, true), 0.5f)
        )
        r.check(
            "Blind fire", "2x spread and 0.6 accuracy, damage untouched",
            approxEquals(CoverRules.blindFireSpread(2f), 4f) &&
                approxEquals(CoverRules.blindFireAccuracy(0.5f), 0.3f) &&
                approxEquals(CoverRules.damageUnchangedByBlindFire(20f), 20f),
            observed(CoverRules.blindFireSpread(2f), 4f)
        )
        val cover = CoverPoint("c1", Vec3(1f, 0f, 0f), CoverHeight.LOW, destructibleHp = 100f)
        r.check(
            "One occupant per slot", "a claimed slot refuses a second body until released",
            cover.claim("alpha") && !cover.claim("bravo") && cover.claim("alpha"),
            observed(cover.claim("bravo"), false)
        )
        cover.release("alpha")
        r.check(
            "Release", "the slot is free once the occupant leaves",
            cover.occupantId == null && cover.claim("bravo"),
            observed(cover.occupantId, "bravo")
        )
        r.check(
            "Destructible auto-exit", "destroying cover clears the occupant and returns Destroyed",
            cover.applyDamage(100f) && cover.destroyed && cover.occupantId == null &&
                CoverRules.exitOnDestroyed(cover) == CoverExit.DESTROYED,
            observed(CoverRules.exitOnDestroyed(cover), CoverExit.DESTROYED)
        )
        r.check(
            "Indestructible cover", "a wall without hit points never reports destruction",
            !CoverPoint("c2", Vec3.ZERO, CoverHeight.HIGH).let {
                it.applyDamage(500f) || it.destroyed
            },
            observed(false, false)
        )
    }

    private fun explosionCases(r: CaseRecorder) {
        r.check(
            "Blast falloff", "maxDamage * (1 - d/r)^1.5, zero outside the sphere",
            approxEquals(ExplosionRules.damage(100f, 0f, 10f), 100f) &&
                approxEquals(ExplosionRules.damage(100f, 5f, 10f), 35.355f) &&
                approxEquals(ExplosionRules.damage(100f, 10f, 10f), 0f),
            observed(ExplosionRules.damage(100f, 5f, 10f), 35.355f)
        )
        r.check(
            "No line of sight", "a target behind a wall takes a quarter",
            approxEquals(ExplosionRules.lineOfSightMultiplier(false), 0.25f) &&
                approxEquals(
                    ExplosionRules.damageFor(100f, 10f, BlastTarget("enemy1", 0f, hasLineOfSight = false)),
                    25f
                ),
            observed(ExplosionRules.lineOfSightMultiplier(false), 0.25f)
        )
        val hits = ExplosionRules.resolve(
            100f, 10f,
            listOf(
                BlastTarget("enemy1", 6f),
                BlastTarget("enemy1", 2f),
                BlastTarget("enemy2", 2f),
                BlastTarget("enemy3", 12f)
            )
        )
        r.check(
            "Dedupe by entity", "a multi-collider body is hit once, using its nearest collider",
            hits.size == 2 && approxEquals(hits.first { it.entityId == "enemy1" }.damage, 71.554f),
            observed(hits.map { it.entityId }, listOf("enemy1", "enemy2"))
        )
        r.check(
            "Fuse", "a thrown grenade waits three seconds",
            approxEquals(ExplosionRules.FUSE_SECONDS, 3f) && ExplosionRules.ARC_PREVIEW,
            observed(ExplosionRules.FUSE_SECONDS, 3f)
        )
        r.check(
            "Smoke", "smoke blocks vision and never blocks bullets",
            ExplosionRules.blocksVision(GrenadeKind.SMOKE) &&
                !ExplosionRules.blocksBullets(GrenadeKind.SMOKE) &&
                !ExplosionRules.blocksVision(GrenadeKind.FRAG),
            observed(ExplosionRules.blocksBullets(GrenadeKind.SMOKE), false)
        )
        r.check(
            "Gas mask", "the mask keeps 80% of the gas damage off",
            approxEquals(ExplosionRules.gasTickDamage(10f, hasGasMask = true), 2f) &&
                approxEquals(ExplosionRules.gasTickDamage(10f, hasGasMask = false), 10f),
            observed(ExplosionRules.gasTickDamage(10f, true), 2f)
        )
        r.check(
            "Rocket", "a direct hit adds full damage to the splash",
            approxEquals(ExplosionRules.rocketTotal(100f, 20f), 120f) &&
                ExplosionRules.blindsInViewCone(GrenadeKind.FLASH),
            observed(ExplosionRules.rocketTotal(100f, 20f), 120f)
        )
        r.check(
            "Friendly blast", "the difficulty policy decides, not the grenade",
            ExplosionRules.friendlyOutcome(FriendlyFirePolicy.OFF, Team.PLAYER) == FriendlyOutcome.NO_DAMAGE &&
                ExplosionRules.friendlyOutcome(FriendlyFirePolicy.ON, Team.PLAYER) == FriendlyOutcome.DAMAGE,
            observed(ExplosionRules.friendlyOutcome(FriendlyFirePolicy.OFF, Team.PLAYER), FriendlyOutcome.NO_DAMAGE)
        )
    }
}
