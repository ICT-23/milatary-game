package com.example.game.combat

/** Receiver fire modes. Safe blocks the trigger entirely. */
enum class FireMode(val displayName: String) {
    SAFE("Safe"),
    SEMI("Semi-Automatic"),
    BURST("3-Round Burst"),
    AUTO("Fully Automatic")
}

/** The physical socket tree of the workbench. */
enum class AttachmentSocket(val displayName: String) {
    BARREL("Barrel Assembly"),
    MUZZLE("Muzzle Device"),
    OPTIC("Optic / Sight"),
    MAGAZINE("Magazine"),
    STOCK("Stock"),
    UNDERBARREL("Handguard / Underbarrel"),
    TRIGGER("Trigger Group")
}

/**
 * A single interchangeable component. Percent deltas are additive across parts,
 * then applied to the base weapon once, so swapping parts never compounds drift.
 */
data class Attachment(
    val id: String,
    val socket: AttachmentSocket,
    val name: String,
    val detail: String,
    val requiredRankIndex: Int = 0,
    val velocityPercent: Float = 0f,
    val dropPercent: Float = 0f,
    val adsSpeedPercent: Float = 0f,
    val weightDeltaKg: Float = 0f,
    val verticalRecoilPercent: Float = 0f,
    val horizontalRecoilPercent: Float = 0f,
    val audioSignaturePercent: Float = 0f,
    val magazineDelta: Int = 0,
    val reloadPercent: Float = 0f,
    val semiRatePercent: Float = 0f,
    val spreadPercent: Float = 0f,
    val opticZoom: Float = 1f,
    val hidesMuzzleFlash: Boolean = false
)

/** Weapon categories from section 7.2. Every entry is fictional game data. */
enum class WeaponCategory(val displayName: String, val gameRole: String) {
    SERVICE_PISTOL("Service pistol", "Backup / academy qualification"),
    COMPACT_SMG("Compact SMG", "Close-range fictional combat role"),
    CARBINE("General-purpose carbine", "Main campaign weapon"),
    BATTLE_RIFLE("Battle rifle", "Controlled medium-range use"),
    MARKSMAN_RIFLE("Designated marksman rifle", "Precision support"),
    PRECISION_RIFLE("Precision rifle", "Long-range challenge role"),
    SHOTGUN("Shotgun", "Close-range breaching-style game role"),
    SUPPORT_WEAPON("Support machine gun", "Squad support role"),
    UTILITY_LAUNCHER("Utility launcher", "Limited special-objective tool"),
    NON_LETHAL_TOOL("Non-lethal training tool", "Target / qualification activity")
}

/**
 * Base receiver statistics before any part is fitted. Fields mirror the weapon
 * attribute list in section 7.4; gameplay data stays separate from display text.
 */
data class WeaponBase(
    val id: String,
    val name: String,
    val caliber: String,
    val baseDamage: Float,
    val rpm: Float,
    val magazineCapacity: Int,
    val reserveRounds: Int,
    val muzzleVelocity: Float,
    val effectiveRangeMeters: Float,
    val armorPenetration: Float,
    val baseWeightKg: Float,
    val fireMode: FireMode,
    val baseSpreadDegrees: Float = 0.35f,
    val baseRecoil: Float = 1f,
    val baseReloadSeconds: Float = 2.1f,
    val baseAdsSeconds: Float = 0.32f,
    val requiredRankIndex: Int = 0,
    val description: String = "",
    // ---------------------------------------------------------- section 7.4
    val category: WeaponCategory = WeaponCategory.CARBINE,
    val effectiveRangeMinMeters: Float = 0f,
    val falloffCurveId: String = "STANDARD",
    /** Aimed spread. Zero means "derive it from the hip value". */
    val aimSpreadDegrees: Float = 0f,
    val recoilProfileId: String = "BALANCED",
    val weaponSway: Float = 1f,
    val movementPenaltyPercent: Float = 0f,
    val cameraFovWhenAiming: Float = 55f,
    val supportedFireModes: List<FireMode>? = null,
    val muzzleEffectId: String = "MUZZLE_STANDARD",
    val soundProfileId: String = "SND_STANDARD",
    val animationSetId: String = "ANIM_CARBINE",
    val unlockRequirement: String = "Academy issue",
    val allowedAttachments: List<AttachmentSocket> = AttachmentSocket.values().toList(),
    /** Shotgun-style weapons resolve this many pellet-equivalents per trigger pull. */
    val pelletCount: Int = 1
) {
    /** Fire modes the receiver actually supports, defaulting to its primary mode. */
    val fireModes: List<FireMode> get() = supportedFireModes ?: listOf(fireMode)

    /** Spread actually used while aiming down sights. */
    val aimedSpread: Float
        get() = if (aimSpreadDegrees > 0f) aimSpreadDegrees else baseSpreadDegrees * 0.35f

    /** Damage of one full trigger pull; identical to base damage for single-shot guns. */
    val volleyDamage: Float get() = baseDamage * pelletCount.coerceAtLeast(1)
}

/** Final aggregated numbers the runtime and HUD read. */
data class GunsmithStats(
    val damage: Float,
    val rpm: Float,
    val magazineCapacity: Int,
    val reserveRounds: Int,
    val muzzleVelocity: Float,
    val effectiveRangeMeters: Float,
    val armorPenetration: Float,
    val weightKg: Float,
    val adsSeconds: Float,
    val reloadSeconds: Float,
    val spreadDegrees: Float,
    val verticalRecoil: Float,
    val horizontalRecoil: Float,
    val audioSignaturePercent: Float,
    val opticZoom: Float,
    val dropFactor: Float,
    val fireMode: FireMode,
    val suppressed: Boolean
)

/** A receiver plus its fitted parts and the stats they produce. */
class WeaponBuild(
    val base: WeaponBase,
    val attachments: Map<AttachmentSocket, Attachment>
) {
    private val parts: List<Attachment> = attachments.values.toList()

    private fun sum(selector: (Attachment) -> Float): Float = parts.fold(0f) { acc, part -> acc + selector(part) }

    val stats: GunsmithStats

    init {
        val velocityPercent = sum { it.velocityPercent }
        val semiRatePercent = if (base.fireMode == FireMode.SEMI) sum { it.semiRatePercent } else 0f
        val adsPercent = sum { it.adsSpeedPercent }
        val opticZoom = attachments[AttachmentSocket.OPTIC]?.opticZoom ?: 1f

        stats = GunsmithStats(
            damage = base.baseDamage,
            rpm = base.rpm * (1f + semiRatePercent / 100f),
            magazineCapacity = (base.magazineCapacity + parts.sumOf { it.magazineDelta }).coerceAtLeast(1),
            reserveRounds = base.reserveRounds,
            muzzleVelocity = base.muzzleVelocity * (1f + velocityPercent / 100f),
            effectiveRangeMeters = base.effectiveRangeMeters,
            armorPenetration = base.armorPenetration,
            weightKg = (base.baseWeightKg + sum { it.weightDeltaKg }).coerceAtLeast(1f),
            adsSeconds = (base.baseAdsSeconds / (1f + adsPercent / 100f)).coerceIn(0.08f, 1.4f),
            reloadSeconds = (base.baseReloadSeconds * (1f + sum { it.reloadPercent } / 100f)).coerceAtLeast(0.6f),
            spreadDegrees = (base.baseSpreadDegrees * (1f + sum { it.spreadPercent } / 100f)).coerceAtLeast(0.02f),
            verticalRecoil = (base.baseRecoil * (1f + sum { it.verticalRecoilPercent } / 100f)).coerceAtLeast(0.05f),
            horizontalRecoil = (base.baseRecoil * (1f + sum { it.horizontalRecoilPercent } / 100f)).coerceAtLeast(0.05f),
            audioSignaturePercent = (100f + sum { it.audioSignaturePercent }).coerceIn(0f, 150f),
            opticZoom = opticZoom,
            dropFactor = (1f + sum { it.dropPercent } / 100f).coerceAtLeast(0.3f),
            fireMode = base.fireMode,
            suppressed = parts.any { it.hidesMuzzleFlash }
        )
    }

    val name: String get() = base.name
    val fireMode: FireMode get() = base.fireMode
    val magazineCapacity: Int get() = stats.magazineCapacity
    val reloadSeconds: Float get() = stats.reloadSeconds
    val adsSeconds: Float get() = stats.adsSeconds
    val rpm: Float get() = stats.rpm
}

/** Every receiver and component the armory offers, with rank clearances. */
object GunsmithCatalog {

    val WEAPONS: List<WeaponBase> = listOf(
        WeaponBase(
            id = "V9_SERVICE",
            name = "V-9 Service",
            caliber = "Fictional 9-series service cartridge",
            baseDamage = 22f,
            rpm = 420f,
            magazineCapacity = 15,
            reserveRounds = 60,
            muzzleVelocity = 360f,
            effectiveRangeMeters = 40f,
            armorPenetration = 0.30f,
            baseWeightKg = 1.1f,
            fireMode = FireMode.SEMI,
            baseSpreadDegrees = 0.55f,
            baseRecoil = 0.75f,
            baseReloadSeconds = 1.8f,
            baseAdsSeconds = 0.24f,
            requiredRankIndex = 0,
            description = "Sidearm qualification platform. Fast to equip, compact, moderate spread.",
            category = WeaponCategory.SERVICE_PISTOL,
            effectiveRangeMinMeters = 5f,
            falloffCurveId = "PISTOL",
            aimSpreadDegrees = 0.22f,
            recoilProfileId = "SIDEARM",
            weaponSway = 0.8f,
            cameraFovWhenAiming = 60f,
            muzzleEffectId = "MUZZLE_PISTOL",
            soundProfileId = "SND_PISTOL_SERVICE",
            animationSetId = "ANIM_PISTOL",
            unlockRequirement = "Academy issue",
            allowedAttachments = listOf(
                AttachmentSocket.OPTIC, AttachmentSocket.MUZZLE,
                AttachmentSocket.MAGAZINE, AttachmentSocket.TRIGGER
            )
        ),
        WeaponBase(
            id = "AR4_SENTINEL",
            name = "AR-4 Sentinel",
            caliber = "Fictional 5-series compact cartridge",
            baseDamage = 28f,
            rpm = 750f,
            magazineCapacity = 30,
            reserveRounds = 180,
            muzzleVelocity = 870f,
            effectiveRangeMeters = 500f,
            armorPenetration = 0.75f,
            baseWeightKg = 3.4f,
            fireMode = FireMode.AUTO,
            baseSpreadDegrees = 0.35f,
            baseRecoil = 1f,
            baseReloadSeconds = 2.1f,
            baseAdsSeconds = 0.32f,
            requiredRankIndex = 1,
            description = "Default campaign rifle. Versatile handling and range, no extreme specialisation.",
            category = WeaponCategory.CARBINE,
            effectiveRangeMinMeters = 10f,
            falloffCurveId = "CARBINE",
            aimSpreadDegrees = 0.12f,
            recoilProfileId = "BALANCED",
            weaponSway = 1f,
            cameraFovWhenAiming = 55f,
            muzzleEffectId = "MUZZLE_CARBINE",
            soundProfileId = "SND_CARBINE_SENTINEL",
            animationSetId = "ANIM_CARBINE",
            unlockRequirement = "Complete arrival and safety orientation",
            supportedFireModes = listOf(FireMode.SAFE, FireMode.SEMI, FireMode.AUTO)
        ),
        WeaponBase(
            id = "KESTREL5",
            name = "Kestrel-5",
            caliber = "Fictional 5-series subsonic load",
            baseDamage = 15f,
            rpm = 900f,
            magazineCapacity = 30,
            reserveRounds = 150,
            muzzleVelocity = 380f,
            effectiveRangeMeters = 120f,
            armorPenetration = 0.35f,
            baseWeightKg = 2.6f,
            fireMode = FireMode.AUTO,
            baseSpreadDegrees = 0.75f,
            baseRecoil = 0.9f,
            baseReloadSeconds = 2f,
            baseAdsSeconds = 0.22f,
            requiredRankIndex = 2,
            description = "Close-range mobility. Fast movement into the fight, higher spread at distance.",
            category = WeaponCategory.COMPACT_SMG,
            effectiveRangeMinMeters = 3f,
            falloffCurveId = "SMG",
            aimSpreadDegrees = 0.3f,
            recoilProfileId = "COMPACT",
            weaponSway = 0.9f,
            cameraFovWhenAiming = 62f,
            muzzleEffectId = "MUZZLE_SMG",
            soundProfileId = "SND_SMG_KESTREL",
            animationSetId = "ANIM_SMG",
            unlockRequirement = "Private rank clearance"
        ),
        WeaponBase(
            id = "BR8_NORTHSTAR",
            name = "BR-8 Northstar",
            caliber = "Fictional 8-series heavy cartridge",
            baseDamage = 38f,
            rpm = 480f,
            magazineCapacity = 20,
            reserveRounds = 120,
            muzzleVelocity = 830f,
            effectiveRangeMeters = 550f,
            armorPenetration = 0.85f,
            baseWeightKg = 4.2f,
            fireMode = FireMode.SEMI,
            baseSpreadDegrees = 0.3f,
            baseRecoil = 1.5f,
            baseReloadSeconds = 2.4f,
            baseAdsSeconds = 0.38f,
            requiredRankIndex = 4,
            description = "Deliberate medium-range play. Higher per-shot impact, slower follow-up.",
            category = WeaponCategory.BATTLE_RIFLE,
            effectiveRangeMinMeters = 40f,
            falloffCurveId = "BATTLE_RIFLE",
            aimSpreadDegrees = 0.11f,
            recoilProfileId = "HEAVY",
            weaponSway = 1.2f,
            cameraFovWhenAiming = 50f,
            muzzleEffectId = "MUZZLE_BATTLE",
            soundProfileId = "SND_BATTLE_NORTHSTAR",
            animationSetId = "ANIM_BATTLE_RIFLE",
            unlockRequirement = "Lance Corporal clearance"
        ),
        WeaponBase(
            id = "M7_LONGVIEW",
            name = "M7 Longview",
            caliber = "Fictional 7-series match cartridge",
            baseDamage = 48f,
            rpm = 180f,
            magazineCapacity = 10,
            reserveRounds = 80,
            muzzleVelocity = 840f,
            effectiveRangeMeters = 700f,
            armorPenetration = 0.90f,
            baseWeightKg = 4.6f,
            fireMode = FireMode.SEMI,
            baseSpreadDegrees = 0.2f,
            baseRecoil = 1.2f,
            baseReloadSeconds = 2.6f,
            baseAdsSeconds = 0.48f,
            requiredRankIndex = 4,
            description = "Precision support. Stronger stability and zoom, slower close-range handling.",
            category = WeaponCategory.MARKSMAN_RIFLE,
            effectiveRangeMinMeters = 60f,
            falloffCurveId = "MARKSMAN",
            aimSpreadDegrees = 0.06f,
            recoilProfileId = "PRECISION",
            weaponSway = 1.1f,
            cameraFovWhenAiming = 40f,
            muzzleEffectId = "MUZZLE_DMR",
            soundProfileId = "SND_DMR_LONGVIEW",
            animationSetId = "ANIM_DMR",
            unlockRequirement = "Lance Corporal clearance"
        ),
        WeaponBase(
            id = "LR12_HORIZON",
            name = "LR-12 Horizon",
            caliber = "Fictional 12-series long cartridge",
            baseDamage = 72f,
            rpm = 45f,
            magazineCapacity = 5,
            reserveRounds = 40,
            muzzleVelocity = 900f,
            effectiveRangeMeters = 1000f,
            armorPenetration = 0.95f,
            baseWeightKg = 6f,
            fireMode = FireMode.SEMI,
            baseSpreadDegrees = 0.1f,
            baseRecoil = 1.5f,
            baseReloadSeconds = 3.2f,
            baseAdsSeconds = 0.6f,
            requiredRankIndex = 6,
            description = "Long-range challenge role. Accurate at distance, slow aim and recovery.",
            category = WeaponCategory.PRECISION_RIFLE,
            effectiveRangeMinMeters = 100f,
            falloffCurveId = "PRECISION",
            aimSpreadDegrees = 0.03f,
            recoilProfileId = "PRECISION_HEAVY",
            weaponSway = 1.3f,
            cameraFovWhenAiming = 32f,
            muzzleEffectId = "MUZZLE_PRECISION",
            soundProfileId = "SND_PRECISION_HORIZON",
            animationSetId = "ANIM_PRECISION",
            unlockRequirement = "Advanced specialist qualification",
            allowedAttachments = listOf(
                AttachmentSocket.BARREL, AttachmentSocket.MUZZLE,
                AttachmentSocket.OPTIC, AttachmentSocket.MAGAZINE,
                AttachmentSocket.STOCK, AttachmentSocket.TRIGGER
            )
        ),
        WeaponBase(
            id = "B12_BREACHER",
            name = "B-12 Breacher",
            caliber = "Fictional 12-series breaching load",
            baseDamage = 12f,
            rpm = 70f,
            magazineCapacity = 6,
            reserveRounds = 36,
            muzzleVelocity = 400f,
            effectiveRangeMeters = 35f,
            armorPenetration = 0.2f,
            baseWeightKg = 3.6f,
            fireMode = FireMode.SEMI,
            baseSpreadDegrees = 1.4f,
            baseRecoil = 1.7f,
            baseReloadSeconds = 3f,
            baseAdsSeconds = 0.3f,
            requiredRankIndex = 5,
            description = "Short-range mission utility. 12 per pellet-equivalent unit, rapid falloff with distance.",
            category = WeaponCategory.SHOTGUN,
            effectiveRangeMinMeters = 0f,
            falloffCurveId = "SHOTGUN",
            aimSpreadDegrees = 1f,
            recoilProfileId = "BREACH",
            weaponSway = 1.1f,
            movementPenaltyPercent = 10f,
            cameraFovWhenAiming = 64f,
            muzzleEffectId = "MUZZLE_SHOTGUN",
            soundProfileId = "SND_SHOTGUN_BREACHER",
            animationSetId = "ANIM_SHOTGUN",
            unlockRequirement = "Staff Sergeant clearance",
            pelletCount = 6
        ),
        WeaponBase(
            id = "H6_ANCHOR",
            name = "H-6 Anchor",
            caliber = "Fictional 6-series support load",
            baseDamage = 18f,
            rpm = 700f,
            magazineCapacity = 100,
            reserveRounds = 300,
            muzzleVelocity = 820f,
            effectiveRangeMeters = 400f,
            armorPenetration = 0.7f,
            baseWeightKg = 8.5f,
            fireMode = FireMode.AUTO,
            baseSpreadDegrees = 0.9f,
            baseRecoil = 1.3f,
            baseReloadSeconds = 5.5f,
            baseAdsSeconds = 0.55f,
            requiredRankIndex = 8,
            description = "Squad support role. Sustained area coverage, heavy movement penalty and longer setup.",
            category = WeaponCategory.SUPPORT_WEAPON,
            effectiveRangeMinMeters = 20f,
            falloffCurveId = "SUPPORT",
            aimSpreadDegrees = 0.35f,
            recoilProfileId = "SUSTAINED",
            weaponSway = 1.5f,
            movementPenaltyPercent = 35f,
            cameraFovWhenAiming = 58f,
            muzzleEffectId = "MUZZLE_SUPPORT",
            soundProfileId = "SND_SUPPORT_ANCHOR",
            animationSetId = "ANIM_SUPPORT",
            unlockRequirement = "Warrant Officer clearance",
            allowedAttachments = listOf(
                AttachmentSocket.BARREL, AttachmentSocket.MUZZLE,
                AttachmentSocket.OPTIC, AttachmentSocket.UNDERBARREL,
                AttachmentSocket.TRIGGER
            )
        ),
        WeaponBase(
            id = "T2_MARKER",
            name = "T-2 Marker",
            caliber = "Fictional training tag load",
            baseDamage = 0f,
            rpm = 120f,
            magazineCapacity = 8,
            reserveRounds = 40,
            muzzleVelocity = 200f,
            effectiveRangeMeters = 60f,
            armorPenetration = 0f,
            baseWeightKg = 1.4f,
            fireMode = FireMode.SEMI,
            baseSpreadDegrees = 0.1f,
            baseRecoil = 0.2f,
            baseReloadSeconds = 1.6f,
            baseAdsSeconds = 0.2f,
            requiredRankIndex = 0,
            description = "Non-lethal training tool. Accurate tagging for identification tasks, zero combat damage.",
            category = WeaponCategory.NON_LETHAL_TOOL,
            effectiveRangeMinMeters = 0f,
            falloffCurveId = "NONE",
            aimSpreadDegrees = 0.04f,
            recoilProfileId = "TRAINING",
            weaponSway = 0.4f,
            cameraFovWhenAiming = 65f,
            muzzleEffectId = "MUZZLE_MARKER",
            soundProfileId = "SND_MARKER_T2",
            animationSetId = "ANIM_TRAINING_TOOL",
            unlockRequirement = "Academy issue"
        )
    )

    val ATTACHMENTS: List<Attachment> = listOf(
        // Barrels
        Attachment(
            id = "BARREL_SHORT",
            socket = AttachmentSocket.BARREL,
            name = "10.5\" Short Barrel",
            detail = "+25% ADS speed, -15% muzzle velocity, +20% bullet drop.",
            requiredRankIndex = 3,
            velocityPercent = -15f,
            dropPercent = 20f,
            adsSpeedPercent = 25f,
            weightDeltaKg = -0.3f
        ),
        Attachment(
            id = "BARREL_STANDARD",
            socket = AttachmentSocket.BARREL,
            name = "14.5\" Standard Barrel",
            detail = "Balanced velocity and baseline maneuverability.",
            requiredRankIndex = 0
        ),
        Attachment(
            id = "BARREL_LONG_MATCH",
            socket = AttachmentSocket.BARREL,
            name = "20\" Long Match Barrel",
            detail = "+20% velocity, -30% bullet drop, -20% ADS speed, +15% weight.",
            requiredRankIndex = 3,
            velocityPercent = 20f,
            dropPercent = -30f,
            adsSpeedPercent = -20f,
            weightDeltaKg = 0.6f
        ),
        // Muzzle devices
        Attachment(
            id = "MUZZLE_A2",
            socket = AttachmentSocket.MUZZLE,
            name = "A2 Flash Hider",
            detail = "Baseline flash reduction.",
            requiredRankIndex = 0,
            audioSignaturePercent = -20f,
            verticalRecoilPercent = -5f
        ),
        Attachment(
            id = "MUZZLE_BRAKE",
            socket = AttachmentSocket.MUZZLE,
            name = "Muzzle Brake",
            detail = "Reduces vertical recoil 35%, increases side blast and sound.",
            requiredRankIndex = 3,
            verticalRecoilPercent = -35f,
            audioSignaturePercent = 12f
        ),
        Attachment(
            id = "MUZZLE_COMPENSATOR",
            socket = AttachmentSocket.MUZZLE,
            name = "Compensator",
            detail = "Reduces horizontal drift 40%.",
            requiredRankIndex = 3,
            horizontalRecoilPercent = -40f,
            audioSignaturePercent = 8f
        ),
        Attachment(
            id = "MUZZLE_SUPPRESSOR",
            socket = AttachmentSocket.MUZZLE,
            name = "Tactical Suppressor",
            detail = "Audio signature -70%, hides muzzle flash, adds 0.4 kg.",
            requiredRankIndex = 6,
            audioSignaturePercent = -70f,
            velocityPercent = -4f,
            weightDeltaKg = 0.4f,
            verticalRecoilPercent = -8f,
            hidesMuzzleFlash = true
        ),
        // Optics
        Attachment(
            id = "OPTIC_IRON",
            socket = AttachmentSocket.OPTIC,
            name = "Iron Sights",
            detail = "Zero ADS penalty, narrow field of view.",
            requiredRankIndex = 0,
            opticZoom = 1f
        ),
        Attachment(
            id = "OPTIC_REFLEX",
            socket = AttachmentSocket.OPTIC,
            name = "Reflex / Holographic",
            detail = "1x quick acquisition with a clean reticle.",
            requiredRankIndex = 2,
            opticZoom = 1f,
            adsSpeedPercent = 6f,
            spreadPercent = -4f
        ),
        Attachment(
            id = "OPTIC_ACOG",
            socket = AttachmentSocket.OPTIC,
            name = "Prism Optic 4x",
            detail = "Ranging reticle for 200 m to 600 m engagements.",
            requiredRankIndex = 6,
            opticZoom = 4f,
            adsSpeedPercent = -12f,
            spreadPercent = -8f
        ),
        Attachment(
            id = "OPTIC_SNIPER",
            socket = AttachmentSocket.OPTIC,
            name = "Variable Sniper Scope 6x-24x",
            detail = "Manual elevation and windage turrets, 100 m to 1000 m zeroing.",
            requiredRankIndex = 4,
            opticZoom = 12f,
            adsSpeedPercent = -22f,
            spreadPercent = -12f
        ),
        // Magazines
        Attachment(
            id = "MAG_STANDARD",
            socket = AttachmentSocket.MAGAZINE,
            name = "Standard 30-Round",
            detail = "30-round standard fictional service magazine.",
            requiredRankIndex = 0
        ),
        Attachment(
            id = "MAG_EXTENDED",
            socket = AttachmentSocket.MAGAZINE,
            name = "40-Round Extended",
            detail = "+10 rounds, slower reload, added ADS latency.",
            requiredRankIndex = 3,
            magazineDelta = 10,
            reloadPercent = 12f,
            adsSpeedPercent = -6f
        ),
        Attachment(
            id = "MAG_DRUM",
            socket = AttachmentSocket.MAGAZINE,
            name = "60-Round Drum",
            detail = "+30 rounds, heavy, increases weapon sway during fatigue.",
            requiredRankIndex = 5,
            magazineDelta = 30,
            reloadPercent = 35f,
            adsSpeedPercent = -18f,
            weightDeltaKg = 0.9f,
            spreadPercent = 10f
        ),
        // Stocks
        Attachment(
            id = "STOCK_SOLID",
            socket = AttachmentSocket.STOCK,
            name = "Solid Stock",
            detail = "Stability: -12% vertical and -8% horizontal recoil.",
            requiredRankIndex = 0,
            verticalRecoilPercent = -12f,
            horizontalRecoilPercent = -8f,
            adsSpeedPercent = -6f
        ),
        Attachment(
            id = "STOCK_COLLAPSIBLE",
            socket = AttachmentSocket.STOCK,
            name = "Collapsible Stock",
            detail = "Mobility and weapon swap speed, +18% ADS speed.",
            requiredRankIndex = 1,
            adsSpeedPercent = 18f,
            verticalRecoilPercent = 5f
        ),
        // Handguard / underbarrel
        Attachment(
            id = "GRIP_ANGLED",
            socket = AttachmentSocket.UNDERBARREL,
            name = "Angled Foregrip",
            detail = "Recoil stabilization: -18% vertical recoil.",
            requiredRankIndex = 3,
            verticalRecoilPercent = -18f,
            adsSpeedPercent = -4f
        ),
        Attachment(
            id = "GRIP_VERTICAL",
            socket = AttachmentSocket.UNDERBARREL,
            name = "Vertical Grip",
            detail = "Faster ADS entry (+18%) and -12% horizontal drift.",
            requiredRankIndex = 3,
            adsSpeedPercent = 18f,
            horizontalRecoilPercent = -12f
        ),
        Attachment(
            id = "LASER_LIGHT",
            socket = AttachmentSocket.UNDERBARREL,
            name = "Tactical Laser / Light",
            detail = "Tighter hip spread (-8%).",
            requiredRankIndex = 2,
            spreadPercent = -8f
        ),
        // Trigger groups
        Attachment(
            id = "TRIGGER_MILSPEC",
            socket = AttachmentSocket.TRIGGER,
            name = "Mil-Spec Trigger",
            detail = "Heavy combat trigger, 6.5 lb pull.",
            requiredRankIndex = 0
        ),
        Attachment(
            id = "TRIGGER_TWO_STAGE",
            socket = AttachmentSocket.TRIGGER,
            name = "Two-Stage Match Trigger",
            detail = "2.5 lb crisp pull, +15% semi-auto fire rate.",
            requiredRankIndex = 7,
            semiRatePercent = 15f,
            spreadPercent = -5f
        )
    )

    val DEFAULT_SOCKETS: List<AttachmentSocket> = listOf(
        AttachmentSocket.BARREL,
        AttachmentSocket.MUZZLE,
        AttachmentSocket.OPTIC,
        AttachmentSocket.MAGAZINE,
        AttachmentSocket.STOCK,
        AttachmentSocket.UNDERBARREL,
        AttachmentSocket.TRIGGER
    )

    /**
     * Receiver ids saved before the fictional roster replaced the reference
     * names. Persisted loadouts are mapped on read, never dropped, so an
     * existing save keeps the weapon it was armed with.
     */
    private val LEGACY_WEAPON_IDS: Map<String, String> = mapOf(
        "M9_SERVICE" to "V9_SERVICE",
        "M4A1" to "AR4_SENTINEL",
        "M24_SNIPER" to "M7_LONGVIEW",
        "M870_BREACH" to "B12_BREACHER"
    )

    fun migrateWeaponId(id: String?): String = LEGACY_WEAPON_IDS[id] ?: (id ?: "")

    fun weapon(id: String?): WeaponBase =
        WEAPONS.firstOrNull { it.id == migrateWeaponId(id) } ?: WEAPONS[1]

    fun weaponsForRank(rankIndex: Int): List<WeaponBase> =
        WEAPONS.filter { rankIndex >= it.requiredRankIndex }

    fun partsFor(socket: AttachmentSocket): List<Attachment> =
        ATTACHMENTS.filter { it.socket == socket }

    fun find(partId: String?): Attachment? = ATTACHMENTS.firstOrNull { it.id == partId }

    fun defaultSelection(): Map<AttachmentSocket, String> = mapOf(
        AttachmentSocket.BARREL to "BARREL_STANDARD",
        AttachmentSocket.MUZZLE to "MUZZLE_A2",
        AttachmentSocket.OPTIC to "OPTIC_IRON",
        AttachmentSocket.MAGAZINE to "MAG_STANDARD",
        AttachmentSocket.STOCK to "STOCK_SOLID",
        AttachmentSocket.UNDERBARREL to "",
        AttachmentSocket.TRIGGER to "TRIGGER_MILSPEC"
    )

    fun isLocked(part: Attachment, rankIndex: Int): Boolean = rankIndex < part.requiredRankIndex

    /** Fits [selection] onto [weaponId], ignoring parts that do not fit or are locked. */
    fun build(weaponId: String?, selection: Map<AttachmentSocket, String>, rankIndex: Int): WeaponBuild {
        val requested = weapon(weaponId)
        // A loadout saved at a higher clearance never arms an unqualified operator.
        val base = if (rankIndex >= requested.requiredRankIndex) {
            requested
        } else {
            WEAPONS.first { rankIndex >= it.requiredRankIndex }
        }
        val fitted = LinkedHashMap<AttachmentSocket, Attachment>()
        for (socket in DEFAULT_SOCKETS) {
            val chosen = find(selection[socket])
            val part = when {
                chosen == null -> null
                chosen.socket != socket -> null
                isLocked(chosen, rankIndex) -> null
                else -> chosen
            } ?: find(defaultSelection()[socket])
            if (part != null) fitted[socket] = part
        }
        return WeaponBuild(base, fitted)
    }
}

/** Compact loadout persistence: `weaponId|BARREL=id,MUZZLE=id,...`. */
object LoadoutCodec {

    fun encode(weaponId: String, selection: Map<AttachmentSocket, String>): String {
        val parts = selection.entries
            .filter { it.value.isNotBlank() }
            .joinToString(",") { "${it.key.name}=${it.value}" }
        return if (parts.isEmpty()) weaponId else "$weaponId|$parts"
    }

    fun decode(raw: String?): Pair<String, Map<AttachmentSocket, String>> {
        if (raw.isNullOrBlank()) return GunsmithCatalog.WEAPONS[1].id to GunsmithCatalog.defaultSelection()
        val weaponId = GunsmithCatalog.migrateWeaponId(raw.substringBefore('|'))
            .ifBlank { GunsmithCatalog.WEAPONS[1].id }
        val selection = LinkedHashMap<AttachmentSocket, String>()
        raw.substringAfter('|', "").split(',').filter { it.contains('=') }.forEach { token ->
            val socket = runCatching { AttachmentSocket.valueOf(token.substringBefore('=')) }.getOrNull()
            val partId = token.substringAfter('=', "")
            if (socket != null && partId.isNotBlank()) selection[socket] = partId
        }
        return weaponId to (GunsmithCatalog.defaultSelection() + selection)
    }
}
