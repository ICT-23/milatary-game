package com.example.game.valor.balance

import com.example.game.valor.art.WeaponClass
import kotlin.math.ceil
import kotlin.math.min

/**
 * Section 1: the reading rules every table in the Content & Balance file assumes.
 *
 * Damage is quoted per bullet or pellet against the torso, so the multipliers
 * below are the only conversion between a table cell and a health bar.
 */
enum class HitRegion(val displayName: String, val multiplier: Float) {
    TORSO("Torso", 1.0f),
    HEAD("Head", 2.5f),
    LIMB("Limb", 0.7f)
}

/** Section 1: the distance factor a weapon reaches at the end of its range. */
enum class DamageFalloff(val weaponClasses: List<WeaponClass>, val minDamageMult: Float) {
    /** "default 0.5 for rifles" - and every class the line does not call out. */
    RIFLE_LIKE(
        listOf(
            WeaponClass.RIFLE, WeaponClass.PISTOL, WeaponClass.LMG,
            WeaponClass.SNIPER, WeaponClass.LAUNCHER
        ),
        0.5f
    ),
    /** "0.3 for shotguns/SMG". */
    CLOSE_RANGE(listOf(WeaponClass.SHOTGUN, WeaponClass.SMG), 0.3f);

    companion object {
        fun of(weaponClass: WeaponClass): DamageFalloff =
            entries.first { weaponClass in it.weaponClasses }
    }
}

/** Section 4.3: the sanity targets the tables are tuned against. */
object TimeToKillTargets {
    const val REFERENCE_ENEMY_HP = 100f
    const val AR_VS_RIFLEMAN_SECONDS = 0.6f
    const val RIFLEMAN_VS_PLAYER_SECONDS_MIN = 4f
    const val RIFLEMAN_VS_PLAYER_SECONDS_MAX = 6f
    const val COVER_MUST_EXCEED_SECONDS = 10f
    const val HEAVY_FOCUSED_SECONDS = 3f
    const val HEAVY_ARMOR_PIERCING_SECONDS = 1.5f
    const val PLAYER_BASE_HEALTH = 100f
}

object DamageMath {

    /**
     * Section 1: range is "the distance where damage reaches minDamageMult", so
     * the falloff between the muzzle and that distance is linear.
     */
    fun damageAtRange(
        baseDamage: Float,
        distanceMeters: Float,
        rangeMeters: Float,
        minDamageMult: Float
    ): Float {
        if (rangeMeters <= 0f) return baseDamage * minDamageMult
        if (distanceMeters <= 0f) return baseDamage
        if (distanceMeters >= rangeMeters) return baseDamage * minDamageMult
        val t = distanceMeters / rangeMeters
        return baseDamage * (1f - (1f - minDamageMult) * t)
    }

    fun minDamageMultFor(weaponClass: WeaponClass): Float = DamageFalloff.of(weaponClass).minDamageMult

    fun damageAtRange(baseDamage: Float, distanceMeters: Float, rangeMeters: Float, weaponClass: WeaponClass): Float =
        damageAtRange(baseDamage, distanceMeters, rangeMeters, minDamageMultFor(weaponClass))

    /** One trigger pull: pellets are fired simultaneously, so their damage adds. */
    fun shotDamage(damagePerPellet: Float, pellets: Int): Float =
        damagePerPellet * pellets.coerceAtLeast(1)

    fun regionDamage(shotDamage: Float, region: HitRegion): Float = shotDamage * region.multiplier

    /** Body shots needed to drop the target, rounded up because the last shot still fires. */
    fun bodyShotsToKill(shotDamage: Float, targetHp: Float = TimeToKillTargets.REFERENCE_ENEMY_HP): Int {
        if (shotDamage <= 0f) return Int.MAX_VALUE
        return ceil(targetHp / shotDamage).toInt()
    }

    fun shotsToKill(
        shotDamage: Float,
        region: HitRegion,
        targetHp: Float = TimeToKillTargets.REFERENCE_ENEMY_HP
    ): Int = bodyShotsToKill(regionDamage(shotDamage, region), targetHp)

    /** Seconds of held fire for n rounds at the given cyclic rate. */
    fun secondsForShots(shots: Int, rpm: Float): Float =
        if (rpm <= 0f) 0f else (shots - 1).coerceAtLeast(0) * 60f / rpm
}

/**
 * Section 3: armour is a pool that absorbs a share of every hit, and a helmet
 * lowers the head multiplier from the 2.5 baseline.
 */
data class ArmorState(val points: Float, val absorbed: Float, val remaining: Float)

object ArmorMath {

    const val BASELINE_HEAD_MULTIPLIER = 2.5f

    fun absorbedDamage(damage: Float, points: Float, absorbFraction: Float): Float =
        min(damage * absorbFraction, points.coerceAtLeast(0f))

    fun healthDamage(damage: Float, points: Float, absorbFraction: Float): Float =
        damage - absorbedDamage(damage, points, absorbFraction)

    fun applyHit(damage: Float, points: Float, absorbFraction: Float): Pair<ArmorState, Float> {
        val absorbed = absorbedDamage(damage, points, absorbFraction)
        val state = ArmorState(points = points, absorbed = absorbed, remaining = points - absorbed)
        return state to (damage - absorbed)
    }

    /** Section 3 helmet tiers: 2.5 baseline, then 2.0 / 1.5 / 1.2. */
    fun headMultiplier(helmetTier: Int?): Float = when (helmetTier) {
        null, 0 -> BASELINE_HEAD_MULTIPLIER
        1 -> 2.0f
        2 -> 1.5f
        else -> 1.2f
    }

    fun headDamage(shotDamage: Float, helmetTier: Int?): Float =
        shotDamage * headMultiplier(helmetTier)
}
