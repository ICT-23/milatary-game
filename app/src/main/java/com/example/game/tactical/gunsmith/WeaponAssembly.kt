package com.example.game.tactical.gunsmith

import com.example.game.vanguard.core.GameErrorCategory
import com.example.game.vanguard.core.GameResult

/**
 * Section 4.1: the 3D workbench. A weapon is eight physical parts plus the six
 * customisation slots, and the two stat formulas from Section 4.2 are the only
 * way those choices reach the WeaponController.
 */
enum class AssemblyPart(val displayName: String) {
    LOWER_RECEIVER("Lower Receiver"),
    UPPER_RECEIVER("Upper Receiver"),
    BOLT_CARRIER_GROUP("Bolt Carrier Group"),
    BARREL("Barrel"),
    HANDGUARD("Handguard"),
    STOCK("Stock"),
    MAGAZINE("Magazine"),
    OPTIC("Optic")
}

enum class ForgeMaterial(val displayName: String) {
    WEAPON_ALLOY("Weapon Alloy"),
    POLYMER("Polymer"),
    HIGH_GRADE_STEEL("High-Grade Steel")
}

data class MaterialStock(
    val alloy: Int,
    val polymer: Int,
    val steel: Int,
    val credits: Int
) {
    fun canAfford(cost: CraftCost): Boolean =
        alloy >= cost.alloy && polymer >= cost.polymer &&
            steel >= cost.steel && credits >= cost.credits

    fun spend(cost: CraftCost): MaterialStock = MaterialStock(
        alloy = alloy - cost.alloy,
        polymer = polymer - cost.polymer,
        steel = steel - cost.steel,
        credits = credits - cost.credits
    )
}

data class CraftCost(val alloy: Int, val polymer: Int, val steel: Int, val credits: Int)

enum class BlueprintSource(val displayName: String) {
    RANK_UP("Rank-up reward"),
    MISSION("Mission objective"),
    SALVAGE("Enemy weapon salvage")
}

data class WeaponBlueprint(
    val id: String,
    val name: String,
    val unlockRankIndex: Int,
    val source: BlueprintSource,
    val cost: CraftCost,
    val parts: Set<AssemblyPart> = AssemblyPart.entries.toSet()
)

data class StockFit(val id: String, val name: String, val recoilStockModifier: Float)

object StockRack {
    val STANDARD = StockFit("STOCK_STANDARD", "Standard Collapsible", 0f)
    val HEAVY = StockFit("STOCK_HEAVY", "Heavy Match Stock", 0.15f)
    val LIGHT = StockFit("STOCK_LIGHT", "Lightweight Skeleton", 0.05f)
    val ALL: List<StockFit> = listOf(STANDARD, HEAVY, LIGHT)
}

/** The unmodified receiver before any slot is filled. */
data class WeaponBase(
    val id: String,
    val name: String,
    val baseRecoil: Float,
    val baseAdsMs: Float,
    val baseDamage: Float,
    val effectiveRangeMeters: Float,
    val maxRangeMeters: Float,
    val fireRateRpm: Float,
    val mobility: Float,
    val baseMagazine: Int = 30
)

data class WeaponStats(
    val effectiveRecoil: Float,
    val adsSpeedMs: Float,
    val damage: Float,
    val effectiveRangeMeters: Float,
    val maxRangeMeters: Float,
    val fireRateRpm: Float,
    val mobility: Float,
    val magazineSize: Int,
    val zoomMin: Float,
    val zoomMax: Float,
    val armorIgnorePercent: Int,
    val unarmoredDamageBonusPercent: Int,
    val isSuppressed: Boolean,
    val hidesFlash: Boolean,
    val muzzleVelocityMps: Float
)

data class AssembledWeapon(
    val blueprintId: String,
    val name: String,
    val base: WeaponBase,
    val parts: Set<AssemblyPart>,
    val stock: StockFit,
    val attachments: Map<AttachmentSlot, Attachment>,
    val stats: WeaponStats
)

object WeaponStatCalculator {

    /** Section 4.2: EffectiveRecoil = Base x (1 - grip - stock - muzzle). */
    fun effectiveRecoil(
        baseRecoil: Float,
        gripModifier: Float,
        stockModifier: Float,
        muzzleModifier: Float
    ): Float = (baseRecoil * (1f - gripModifier - stockModifier - muzzleModifier)).coerceAtLeast(0f)

    /** Section 4.2: ADS(ms) = BaseADS + sum(attachment weight penalties). */
    fun adsSpeedMs(baseAdsMs: Float, attachments: Collection<Attachment>): Float =
        (baseAdsMs + attachments.sumOf { it.adsWeightMs.toDouble() }.toFloat()).coerceAtLeast(40f)

    fun stats(base: WeaponBase, stock: StockFit, attachments: Map<AttachmentSlot, Attachment>): WeaponStats {
        val devices = attachments.values
        val grip = attachments[AttachmentSlot.UNDERBARREL]?.recoilGripModifier ?: 0f
        val muzzle = attachments[AttachmentSlot.MUZZLE]?.recoilMuzzleModifier ?: 0f

        val damageMultiplier = devices.fold(1f) { acc, item -> acc * item.damageMultiplier }
        val rangeMultiplier = devices.fold(1f) { acc, item -> acc * item.rangeMultiplier }
        val mobilityMultiplier = devices.fold(1f) { acc, item -> acc * item.mobilityMultiplier }

        val optic = attachments[AttachmentSlot.OPTIC]
        val magazine = attachments[AttachmentSlot.MAGAZINE]
        val ammo = attachments[AttachmentSlot.AMMUNITION]
        val muzzleDevice = attachments[AttachmentSlot.MUZZLE]

        val velocityPenalty = muzzleDevice?.muzzleVelocityPenaltyMps ?: 0f

        return WeaponStats(
            effectiveRecoil = effectiveRecoil(base.baseRecoil, grip, stock.recoilStockModifier, muzzle),
            adsSpeedMs = adsSpeedMs(base.baseAdsMs, devices),
            damage = base.baseDamage * damageMultiplier,
            effectiveRangeMeters = base.effectiveRangeMeters * rangeMultiplier,
            maxRangeMeters = base.maxRangeMeters * rangeMultiplier,
            fireRateRpm = base.fireRateRpm,
            mobility = (base.mobility * mobilityMultiplier).coerceIn(0.1f, 3f),
            magazineSize = magazine?.magazineSize?.takeIf { it > 0 } ?: base.baseMagazine,
            zoomMin = optic?.zoomMin ?: 1f,
            zoomMax = optic?.zoomMax ?: 1f,
            armorIgnorePercent = ammo?.armorIgnorePercent ?: 0,
            unarmoredDamageBonusPercent = ammo?.unarmoredDamageBonusPercent ?: 0,
            isSuppressed = muzzleDevice?.suppressed ?: false,
            hidesFlash = muzzleDevice?.hidesFlash ?: false,
            muzzleVelocityMps = BASE_MUZZLE_VELOCITY_MPS - velocityPenalty
        )
    }

    const val BASE_MUZZLE_VELOCITY_MPS = 880f
}

object Workbench {

    fun assemble(
        blueprint: WeaponBlueprint,
        base: WeaponBase,
        fittedParts: Set<AssemblyPart>,
        stock: StockFit,
        attachments: Map<AttachmentSlot, Attachment>
    ): GameResult<AssembledWeapon> {
        val missing = AssemblyPart.entries.filter { it !in fittedParts }
        if (missing.isNotEmpty()) {
            return GameResult.fail(
                GameErrorCategory.INCOMPATIBLE,
                "MISSING_PART",
                "${blueprint.name} cannot fire without ${missing.joinToString { it.displayName }}"
            )
        }
        val mismatched = attachments.entries.firstOrNull { (slot, item) -> slot != item.slot }
        if (mismatched != null) {
            return GameResult.fail(
                GameErrorCategory.INCOMPATIBLE,
                "WRONG_SLOT",
                "${mismatched.value.name} is a ${mismatched.value.slot.displayName} part, " +
                    "fitted as ${mismatched.key.displayName}"
            )
        }
        return GameResult.Ok(
            AssembledWeapon(
                blueprintId = blueprint.id,
                name = blueprint.name,
                base = base,
                parts = fittedParts.toSet(),
                stock = stock,
                attachments = attachments.toMap(),
                stats = WeaponStatCalculator.stats(base, stock, attachments)
            )
        )
    }

    /** Swaps one slotted attachment and recomputes every stat immediately. */
    fun equip(weapon: AssembledWeapon, attachment: Attachment): GameResult<AssembledWeapon> {
        val next = weapon.attachments + (attachment.slot to attachment)
        return GameResult.Ok(
            weapon.copy(
                attachments = next,
                stats = WeaponStatCalculator.stats(weapon.base, weapon.stock, next)
            )
        )
    }

    fun fitStock(weapon: AssembledWeapon, stock: StockFit): AssembledWeapon = weapon.copy(
        stock = stock,
        stats = WeaponStatCalculator.stats(weapon.base, stock, weapon.attachments)
    )

    /** Section 4.1 step 1-2: blueprint gate, then material spend, then craft. */
    fun craft(
        blueprint: WeaponBlueprint,
        stock: MaterialStock,
        rankIndex: Int
    ): GameResult<MaterialStock> {
        if (!rankGate(blueprint, rankIndex)) {
            return GameResult.fail(
                GameErrorCategory.PROGRESSION,
                "BLUEPRINT_LOCKED",
                "${blueprint.name} unlocks at rank index ${blueprint.unlockRankIndex}"
            )
        }
        if (!stock.canAfford(blueprint.cost)) {
            return GameResult.fail(
                GameErrorCategory.ECONOMY,
                "INSUFFICIENT_MATERIALS",
                "Need ${blueprint.cost}, holding $stock"
            )
        }
        return GameResult.Ok(stock.spend(blueprint.cost))
    }

    fun rankGate(blueprint: WeaponBlueprint, rankIndex: Int): Boolean = rankIndex >= blueprint.unlockRankIndex
}
