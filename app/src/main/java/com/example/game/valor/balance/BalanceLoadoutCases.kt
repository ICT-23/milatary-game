package com.example.game.valor.balance

import com.example.game.vanguard.CaseRecorder
import com.example.game.vanguard.ContractCase
import com.example.game.vanguard.approxEquals
import com.example.game.vanguard.observed

/** Section 2.6 gunsmith maths, section 2.7 attachments and the section 3 gear table. */
object BalanceLoadoutCases {

    fun run(): List<ContractCase> {
        val r = CaseRecorder(BalanceAudit.GROUP_GUNSMITH)
        gunsmith(r)
        attachments(r)
        val gear = CaseRecorder(BalanceAudit.GROUP_GEAR)
        gearCases(gear)
        return r.result() + gear.result()
    }

    private fun gunsmith(r: CaseRecorder) {
        r.check(
            "Upgrade levels", "levels 1-10",
            GunsmithProgression.MIN_LEVEL == 1 && GunsmithProgression.MAX_LEVEL == 10,
            observed("${GunsmithProgression.MIN_LEVEL}-${GunsmithProgression.MAX_LEVEL}", "1-10")
        )
        r.check(
            "Level 10 damage", "approx +30% damage",
            approxEquals(GunsmithProgression.damageMultiplier(10), 1.30f, 0.05f),
            observed(GunsmithProgression.damageMultiplier(10), 1.30f)
        )
        r.check(
            "Level 10 handling", "approx +20% handling",
            approxEquals(GunsmithProgression.handlingMultiplier(10), 1.20f, 0.05f),
            observed(GunsmithProgression.handlingMultiplier(10), 1.20f)
        )
        r.check(
            "Level 10 accuracy", "approx +10% accuracy",
            approxEquals(GunsmithProgression.accuracyMultiplier(10), 1.10f, 0.05f),
            observed(GunsmithProgression.accuracyMultiplier(10), 1.10f)
        )
        r.check(
            "Level 1 has no bonus", "a freshly bought weapon is unchanged",
            approxEquals(GunsmithProgression.damageMultiplier(1), 1f) &&
                approxEquals(GunsmithProgression.accuracyMultiplier(1), 1f),
            observed(GunsmithProgression.damageMultiplier(1), 1f)
        )
        r.check(
            "Base cost is 10%", "a 9,000 weapon costs 900 per step at level 1",
            approxEquals(GunsmithProgression.baseCost(9_000), 900f),
            observed(GunsmithProgression.baseCost(9_000), 900f)
        )
        r.check(
            "Base cost floor", "never below 150",
            approxEquals(GunsmithProgression.baseCost(1_000), 150f),
            observed(GunsmithProgression.baseCost(1_000), 150f)
        )
        r.check(
            "Cost growth", "baseCost x 1.35^(level-1)",
            approxEquals(GunsmithProgression.upgradeCost(9_000, 2), 1_215f),
            observed(GunsmithProgression.upgradeCost(9_000, 2), 1_215f)
        )
        r.check(
            "Max level has no step", "level 10 cannot be upgraded further",
            approxEquals(GunsmithProgression.upgradeCost(9_000, 10), 0f) &&
                GunsmithProgression.steelCost(10) == 0,
            observed(GunsmithProgression.steelCost(10), 0)
        )
        r.check(
            "Steel cost per level", "2 x level",
            GunsmithProgression.steelCost(4) == 8, observed(GunsmithProgression.steelCost(4), 8)
        )
        r.check(
            "Steel to max", "2 x (1..9) = 90 steel",
            GunsmithProgression.totalSteelTo(10) == 90,
            observed(GunsmithProgression.totalSteelTo(10), 90)
        )
        r.check(
            "Mastery tiers", "50 / 150 / 400 / 1,000 / 2,500 kills",
            GunsmithProgression.MASTERY_TIERS.map { it.kills } ==
                listOf(50, 150, 400, 1_000, 2_500),
            observed(GunsmithProgression.MASTERY_TIERS.map { it.kills }, "[50, 150, 400, 1000, 2500]")
        )
        r.check(
            "Mastery ladder", "nothing below 50 kills, last tier at 2,500",
            GunsmithProgression.tierForKills(49) == null &&
                GunsmithProgression.tierForKills(2_500)?.kills == 2_500,
            observed(GunsmithProgression.tierForKills(2_500)?.kills, 2_500)
        )
    }

    private fun attachments(r: CaseRecorder) {
        r.check(
            "Attachment count", "18 rows in section 2.7",
            AttachmentCatalog.ATTACHMENTS.size == 18,
            observed(AttachmentCatalog.ATTACHMENTS.size, 18)
        )
        r.check(
            "Every slot used", "7 slots incl. Ammo",
            AttachmentSlot.entries.all { slot -> AttachmentCatalog.forSlot(slot).isNotEmpty() },
            observed(AttachmentCatalog.ATTACHMENTS.map { it.slot }.distinct().size, 7)
        )
        val suppressor = AttachmentCatalog.byName("Suppressor")
        r.check(
            "Cost triple", "Suppressor 15 steel / 8 parts / 1,800 cash",
            suppressor != null &&
                AttachmentCatalog.craftCost(suppressor) == CraftCost(15, 8, 1_800),
            observed(suppressor?.let { AttachmentCatalog.craftCost(it) }, "15/8/1800")
        )
        val ap = AttachmentCatalog.byName("Armor-piercing")
        val suppressed = ModifierStacking.stack(listOfNotNull(suppressor, ap))
        r.check(
            "Multiply stage", "0.95 x 0.95 = 0.9025 damage",
            approxEquals(ModifierStacking.damageMultiplier(suppressed), 0.9025f),
            observed(ModifierStacking.damageMultiplier(suppressed), 0.9025f)
        )
        val foregrip = AttachmentCatalog.byName("Foregrip")
        val compensator = AttachmentCatalog.byName("Compensator")
        val recoil = ModifierStacking.stack(listOfNotNull(foregrip, compensator))
        r.check(
            "Add stage", "-8 and -15 recoil sum to -23",
            approxEquals(ModifierStacking.recoilMultiplier(recoil), 0.77f),
            observed(ModifierStacking.recoilMultiplier(recoil), 0.77f)
        )
        val fastMag = AttachmentCatalog.byName("Fast mag")
        val extMag = AttachmentCatalog.byName("Extended mag")
        val reload = ModifierStacking.stack(listOfNotNull(fastMag, extMag))
        r.check(
            "Reload stacking", "-25% and +8% sum to -17%",
            approxEquals(ModifierStacking.reloadMultiplier(reload), 0.83f),
            observed(ModifierStacking.reloadMultiplier(reload), 0.83f)
        )
        val redDot = AttachmentCatalog.byName("Red-dot")
        val scope4x = AttachmentCatalog.byName("4x scope")
        val optics = ModifierStacking.stack(listOfNotNull(redDot, scope4x))
        r.check(
            "One optic at a time", "FOV is taken from the fitted optic, not summed",
            approxEquals(ModifierStacking.factorFor(ModKey.ADS_FOV, optics), 50f),
            observed(ModifierStacking.factorFor(ModKey.ADS_FOV, optics), 50f)
        )
        val longBarrel = AttachmentCatalog.byName("Long barrel")
        val shortBarrel = AttachmentCatalog.byName("Short barrel")
        val barrels = ModifierStacking.stack(listOfNotNull(longBarrel, shortBarrel))
        r.check(
            "Range stacking", "+20% and -15% sum to +5%",
            approxEquals(ModifierStacking.rangeMultiplier(barrels), 1.05f),
            observed(ModifierStacking.rangeMultiplier(barrels), 1.05f)
        )
        r.check(
            "Clamp bounds", "a factor never leaves 0..3",
            ModifierStacking.MIN_FACTOR == 0f && ModifierStacking.MAX_FACTOR == 3f,
            observed("${ModifierStacking.MIN_FACTOR}..${ModifierStacking.MAX_FACTOR}", "0..3")
        )
    }

    private fun gearCases(r: CaseRecorder) {
        r.check(
            "Helmet tiers", "2.5 becomes 2.0 / 1.5 / 1.2 at 500 / 3,000 / 12,000",
            GearCatalog.HELMETS.map { it.headMultiplier } == listOf(2.0f, 1.5f, 1.2f) &&
                GearCatalog.HELMETS.map { it.price } == listOf(500, 3_000, 12_000) &&
                GearCatalog.HELMETS.map { it.unlockRank } == listOf(3, 9, 14),
            observed(GearCatalog.HELMETS.map { it.headMultiplier }, "[2.0, 1.5, 1.2]")
        )
        r.check(
            "Helmet at rank", "bare head 2.5, T1 at 3, T2 at 9, T3 at 14",
            approxEquals(GearCatalog.headMultiplierAt(2), 2.5f) &&
                approxEquals(GearCatalog.headMultiplierAt(3), 2.0f) &&
                approxEquals(GearCatalog.headMultiplierAt(9), 1.5f) &&
                approxEquals(GearCatalog.headMultiplierAt(14), 1.2f),
            observed(GearCatalog.headMultiplierAt(14), 1.2f)
        )
        r.check(
            "Vest pool", "50 / 100 / 150 points at 40 / 50 / 60%",
            GearCatalog.VESTS.map { it.armorPoints } == listOf(50f, 100f, 150f) &&
                GearCatalog.VESTS.map { it.armorAbsorb } == listOf(0.40f, 0.50f, 0.60f),
            observed(GearCatalog.VESTS.map { it.armorPoints }, "[50, 100, 150]")
        )
        val hit = ArmorMath.applyHit(100f, 50f, 0.40f)
        r.check(
            "Armour depletion", "a 100 hit spends 20 points and deals 80",
            approxEquals(hit.first.remaining, 30f) && approxEquals(hit.second, 80f),
            observed("remaining=${hit.first.remaining} health=${hit.second}", "30/80")
        )
        r.check(
            "Medical kit", "medkit 100% in 3.0 s, bandage 30% in 1.5 s",
            approxEquals(GearCatalog.byId("medkit")?.healFraction ?: 0f, 1f) &&
                approxEquals(GearCatalog.byId("medkit")?.useSeconds ?: 0f, 3f) &&
                approxEquals(GearCatalog.byId("bandage")?.healFraction ?: 0f, 0.30f) &&
                approxEquals(GearCatalog.byId("bandage")?.useSeconds ?: 0f, 1.5f),
            observed(GearCatalog.byId("medkit")?.useSeconds, 3f)
        )
        r.check(
            "Gas mask", "gas damage -80%: 12 dmg/s becomes 2.4",
            approxEquals(12f * (1f - (GearCatalog.byId("gas_mask")?.gasReduction ?: 0f)), 2.4f),
            observed(12f * (1f - (GearCatalog.byId("gas_mask")?.gasReduction ?: 0f)), 2.4f)
        )
        r.check(
            "Binoculars", "marks enemies for 20 s",
            approxEquals(GearCatalog.BINOCULAR_MARK_SECONDS, 20f),
            observed(GearCatalog.BINOCULAR_MARK_SECONDS, 20f)
        )
        r.check(
            "Backpack bonus", "+15 / +30 / +50% reserve ammo",
            GearCatalog.BACKPACKS.map { it.reserveBonusPercent } == listOf(15, 30, 50) &&
                GearCatalog.reserveWithBackpack(180, GearCatalog.byId("backpack_1")) == 207,
            observed(GearCatalog.reserveWithBackpack(180, GearCatalog.byId("backpack_1")), 207)
        )
        r.check(
            "Gear rows", "12 table rows; the backpack row expands to three tiers",
            GearCatalog.HELMETS.size + GearCatalog.VESTS.size + GearCatalog.EQUIPMENT.size == 11 &&
                GearCatalog.BACKPACKS.size == 3 && GearCatalog.ALL.size == 14,
            observed(GearCatalog.ALL.size, 14)
        )
    }
}
