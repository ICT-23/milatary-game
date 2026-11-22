package com.example.game.combat

/**
 * Collider proxy for a pop-up cutout, steel silhouette or training dummy.
 *
 * The cutout exposes one sphere per documented hit zone so [BallisticsEngine]
 * can cast against real geometry, and it is the only place a [DamagePayload]
 * is turned into health loss. A payload built from a rejected shot never
 * reaches this class because [toDamagePayload] returns null first.
 */
class DamageableCutout(
    override val transformRef: Vec3,
    val maxHealth: Float = 90f,
    val effectiveRangeMeters: Float = 300f,
    private var armor: ArmorProfile = ArmorProfile(),
    val id: String = "CUTOUT"
) : Damageable {

    var health: Float = maxHealth
        private set

    var lastResolution: DamageResolution? = null
        private set

    override val isDestroyedOrDead: Boolean get() = health <= 0f

    val armorProfile: ArmorProfile get() = armor

    /** Zone spheres in the same coordinate space the caller shoots in. */
    val hitZones: List<BallisticBody> = listOf(
        BallisticBody("${id}_HEAD", transformRef + Vec3(0f, 0.36f, 0f), 0.09f, HitZone.HEAD),
        BallisticBody("${id}_UPPER_TORSO", transformRef + Vec3(0f, 0.12f, 0f), 0.16f, HitZone.UPPER_TORSO),
        BallisticBody("${id}_LOWER_TORSO", transformRef + Vec3(0f, -0.05f, 0f), 0.14f, HitZone.LOWER_TORSO),
        BallisticBody("${id}_LEG", transformRef + Vec3(0.16f, -0.28f, 0f), 0.08f, HitZone.LEG)
    )

    fun zoneCenter(zone: HitZone): Vec3 = (hitZones.firstOrNull { it.zone == zone } ?: hitZones[1]).center

    override fun applyDamage(payload: DamagePayload, modifiers: DamageModifiers) {
        if (isDestroyedOrDead) return
        val resolution = DamageResolver.resolve(
            baseDamage = payload.rawDamage,
            zone = payload.zone,
            distanceMeters = payload.distanceMeters,
            effectiveRangeMeters = effectiveRangeMeters,
            armorPenetration = payload.armorPenetrationPower,
            armor = armor,
            armourGrade = modifiers.armourGrade,
            missionDifficultyMultiplier = modifiers.missionDifficultyMultiplier,
            temporaryStatusMultiplier = modifiers.temporaryStatusMultiplier
        )
        lastResolution = resolution
        health = (health - resolution.appliedDamage).coerceAtLeast(0f)

        // Plate hitpoints are consumed by whatever the plate managed to absorb.
        val plateAfter = (armor.plateDurability - resolution.armorAbsorbed).coerceAtLeast(0)
        armor = armor.copy(plateDurability = plateAfter)
    }

    /** Section 11.4: a checkpoint retry restores the exact health this body held. */
    fun restoreHealth(value: Float) {
        health = value.coerceIn(0f, maxHealth)
        lastResolution = null
    }
}
