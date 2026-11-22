package com.example.game.valor.plan

import com.example.game.valor.balance.FriendlyFirePolicy
import kotlin.math.pow

/** Section 5.9: the grenade families and what each one does to vision or bullets. */
enum class GrenadeKind(val displayName: String) {
    FRAG("Frag"),
    SMOKE("Smoke"),
    FLASH("Flash"),
    GAS("Gas"),
    ROCKET("Launcher rocket")
}

/**
 * Section 5.9: one candidate inside the blast sphere. A body with several
 * colliders appears once per collider; the dedupe below keeps the nearest.
 */
data class BlastTarget(
    val entityId: String,
    val distanceMeters: Float,
    val hasLineOfSight: Boolean = true
)

data class BlastHit(val entityId: String, val damage: Float, val hadLineOfSight: Boolean)

/** Section 5.9: the throw, fuse, falloff and dedupe rules. */
object ExplosionRules {

    const val FUSE_SECONDS = 3f
    const val FALLOFF_EXPONENT = 1.5
    const val NO_LOS_MULTIPLIER = 0.25f
    const val DEDUPE_BY_ENTITY = true
    const val ARC_PREVIEW = true
    const val BOUNCES_OFF_SURFACES = true

    /** Gas mask note in the balance table: 80% of the damage is negated. */
    const val GAS_MASK_REDUCTION = 0.8f

    /** `maxDamage * (1 - dist/radius)^1.5`, zero outside the sphere. */
    fun damage(maxDamage: Float, distanceMeters: Float, radiusMeters: Float): Float {
        if (radiusMeters <= 0f || distanceMeters >= radiusMeters) return 0f
        val ratio = (1f - distanceMeters / radiusMeters).coerceIn(0f, 1f)
        return maxDamage * ratio.toDouble().pow(FALLOFF_EXPONENT).toFloat()
    }

    /** "No line of sight → damage x0.25 (or 0 if fully shielded)." */
    fun lineOfSightMultiplier(hasLineOfSight: Boolean): Float =
        if (hasLineOfSight) 1f else NO_LOS_MULTIPLIER

    fun damageFor(maxDamage: Float, radiusMeters: Float, target: BlastTarget): Float =
        damage(maxDamage, target.distanceMeters, radiusMeters) *
            lineOfSightMultiplier(target.hasLineOfSight)

    /** "Each entity takes damage once per explosion (dedupe by entity)." */
    fun resolve(maxDamage: Float, radiusMeters: Float, targets: List<BlastTarget>): List<BlastHit> =
        targets
            .groupBy { it.entityId }
            .map { (entityId, colliders) ->
                val nearest = colliders.minByOrNull { it.distanceMeters } ?: colliders.first()
                BlastHit(entityId, damageFor(maxDamage, radiusMeters, nearest), nearest.hasLineOfSight)
            }
            .filter { it.damage > 0f }

    /** "Smoke blocks AI vision (not bullets)." */
    fun blocksVision(kind: GrenadeKind): Boolean = kind == GrenadeKind.SMOKE

    fun blocksBullets(kind: GrenadeKind): Boolean = false

    fun blindsInViewCone(kind: GrenadeKind): Boolean = kind == GrenadeKind.FLASH

    /** Gas does damage over time; a mask keeps [GAS_MASK_REDUCTION] off. */
    fun gasTickDamage(tickDamage: Float, hasGasMask: Boolean): Float =
        if (hasGasMask) tickDamage * (1f - GAS_MASK_REDUCTION) else tickDamage

    /** "Thrower takes self-damage at reduced rate on Hard." */
    fun selfDamage(blastDamage: Float, reducedOnHard: Boolean, reducedFraction: Float): Float =
        if (reducedOnHard) blastDamage * reducedFraction.coerceIn(0f, 1f) else blastDamage

    /** "Launcher rockets: direct hit = full damage, plus splash." */
    fun rocketTotal(directHitDamage: Float, splashDamage: Float): Float =
        directHitDamage + splashDamage

    /** Friendly fire still follows 5.3, so the difficulty policy decides. */
    fun friendlyOutcome(policy: FriendlyFirePolicy, shooterTeam: Team) =
        HitDetection.friendlyRule(policy, shooterTeam)
}
