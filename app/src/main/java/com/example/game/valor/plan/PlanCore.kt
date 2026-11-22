package com.example.game.valor.plan

import com.example.game.valor.balance.HitRegion
import kotlin.math.acos
import kotlin.math.sqrt

/**
 * Section 4.1: every physics layer the build defines. A bullet ray only sees the
 * layers in [BulletMask], and the shooter's own hitbox is removed by identity.
 */
enum class PhysicsLayer(val displayName: String) {
    PLAYER("Player"),
    ENEMY("Enemy"),
    ALLY("Ally"),
    ENVIRONMENT("Environment"),
    COVER("Cover"),
    GLASS("Glass"),
    DESTRUCTIBLE("Destructible"),
    PROJECTILE("Projectile"),
    TRIGGER("Trigger")
}

/** Section 4.1: the bullet mask, quoted as a set so it cannot drift per call site. */
object BulletMask {

    val INCLUDED: Set<PhysicsLayer> = setOf(
        PhysicsLayer.ENEMY,
        PhysicsLayer.ALLY,
        PhysicsLayer.PLAYER,
        PhysicsLayer.ENVIRONMENT,
        PhysicsLayer.COVER,
        PhysicsLayer.GLASS,
        PhysicsLayer.DESTRUCTIBLE
    )

    val EXCLUDED: Set<PhysicsLayer> = PhysicsLayer.entries.toSet() - INCLUDED

    fun accepts(layer: PhysicsLayer): Boolean = layer in INCLUDED

    /** "It excludes the shooter itself." */
    fun accepts(layer: PhysicsLayer, entityId: String?, shooterId: String?): Boolean =
        accepts(layer) && (entityId == null || entityId != shooterId)
}

/** Section 4.2: the four teams, with Player and Ally on the same side. */
enum class Team(val displayName: String) {
    PLAYER("Player"),
    ALLY("Ally"),
    ENEMY("Enemy"),
    NEUTRAL("Neutral");

    val playerSide: Boolean get() = this == PLAYER || this == ALLY

    companion object {
        fun sameSide(a: Team, b: Team): Boolean =
            (a.playerSide && b.playerSide) || (a == ENEMY && b == ENEMY)
    }
}

/** Section 4.2/5.4: the three hit zones, mapped to the balance file's multipliers. */
enum class HitZone(val displayName: String, val region: HitRegion) {
    HEAD("Head", HitRegion.HEAD),
    TORSO("Torso", HitRegion.TORSO),
    LIMB("Limb", HitRegion.LIMB);

    /** Head 2.5, Torso 1.0, Limb 0.7 - read from the single source in `balance`. */
    val multiplier: Float get() = region.multiplier
}

/** Section 4.2: the five damage sources. */
enum class DamageType(val displayName: String) {
    BULLET("Bullet"),
    EXPLOSION("Explosion"),
    MELEE("Melee"),
    DRONE("Drone"),
    FALL("Fall")
}

/**
 * Section 4.2: the payload every damage application carries. `sourceId` replaces
 * the C# `GameObject` reference so the rules stay pure and serialisable.
 */
data class DamageInfo(
    val amount: Float,
    val zone: HitZone,
    val type: DamageType,
    val sourceId: String?,
    val sourceTeam: Team,
    val weaponId: String? = null,
    val hitPoint: Vec3 = Vec3.ZERO,
    val hitNormal: Vec3 = Vec3.ZERO
)

/**
 * Section 4.2: the only thing a bullet, an explosion or a melee swing may hit.
 * "ONLY DamageSystem calls this" - see `DamageSystem.apply`.
 */
interface Damageable {
    val id: String
    val team: Team
    val isAlive: Boolean

    /** Returns true when health actually fell, which is what the hitmarker reads. */
    fun receiveDamage(info: DamageInfo): Boolean
}

/**
 * Small vector so aim, convergence and blast falloff can be expressed without a
 * scene dependency. Only the operations the rules use exist.
 */
data class Vec3(val x: Float, val y: Float, val z: Float) {

    operator fun plus(other: Vec3): Vec3 = Vec3(x + other.x, y + other.y, z + other.z)

    operator fun minus(other: Vec3): Vec3 = Vec3(x - other.x, y - other.y, z - other.z)

    operator fun times(scalar: Float): Vec3 = Vec3(x * scalar, y * scalar, z * scalar)

    fun dot(other: Vec3): Float = x * other.x + y * other.y + z * other.z

    val length: Float get() = sqrt(dot(this))

    fun normalized(): Vec3 {
        val magnitude = length
        return if (magnitude <= 0f) ZERO else this * (1f / magnitude)
    }

    fun distanceTo(other: Vec3): Float = (this - other).length

    /** Section 5.1 "angle < assistAngle" and Section 5.8 "facing it (angle < 70)". */
    fun angleDegreesTo(other: Vec3): Float {
        val denominator = length * other.length
        if (denominator <= 0f) return 0f
        val cosine = (dot(other) / denominator).coerceIn(-1f, 1f)
        return Math.toDegrees(acos(cosine).toDouble()).toFloat()
    }

    companion object {
        val ZERO = Vec3(0f, 0f, 0f)
        val FORWARD = Vec3(0f, 0f, 1f)
    }
}

/**
 * Section 4.3: the event bus names. Systems publish through [EventBus] instead of
 * holding references to each other.
 */
enum class PlanEventType(val displayName: String) {
    SHOT_FIRED("OnShotFired"),
    HIT("OnHit"),
    DAMAGE_DEALT("OnDamageDealt"),
    ENTITY_KILLED("OnEntityKilled"),
    RELOAD_STARTED("OnReloadStarted"),
    RELOAD_FINISHED("OnReloadFinished"),
    WEAPON_SWAPPED("OnWeaponSwapped"),
    OBJECTIVE_COMPLETED("OnObjectiveCompleted"),
    MISSION_STATE_CHANGED("OnMissionStateChanged"),
    XP_GAINED("OnXPGained"),
    RANK_UP("OnRankUp"),
    CURRENCY_CHANGED("OnCurrencyChanged"),
    PLAYER_DIED("OnPlayerDied")
}

data class PlanEvent(val type: PlanEventType, val detail: String, val atSeconds: Float)

/** A deterministic in-memory bus: every case can assert on what was published. */
class EventBus {

    private val published = mutableListOf<PlanEvent>()

    fun emit(type: PlanEventType, detail: String = "", atSeconds: Float = 0f) {
        published += PlanEvent(type, detail, atSeconds)
    }

    fun events(type: PlanEventType): List<PlanEvent> = published.filter { it.type == type }

    fun count(type: PlanEventType): Int = events(type).size

    fun all(): List<PlanEvent> = published.toList()

    fun clear() = published.clear()
}

/** Section 10: the nine log categories; every rejected action names one of them. */
enum class LogCategory(val tag: String) {
    FIRE("[Fire]"),
    HIT("[Hit]"),
    DAMAGE("[Damage]"),
    KILL("[Kill]"),
    AI("[AI]"),
    MISSION("[Mission]"),
    XP("[XP]"),
    ECONOMY("[Economy]"),
    SAVE("[Save]")
}

data class DebugLogEntry(val category: LogCategory, val reason: String, val atSeconds: Float)

/**
 * Section 1 rule 8 "no silent failures": debug builds keep the reason of every
 * rejected action, so a missing shot is never a mystery.
 */
class DebugLog(private val enabled: Boolean = true) {

    private val entries = mutableListOf<DebugLogEntry>()

    fun record(category: LogCategory, reason: String, atSeconds: Float = 0f) {
        if (!enabled) return
        entries += DebugLogEntry(category, reason, atSeconds)
    }

    fun last(category: LogCategory): DebugLogEntry? = entries.lastOrNull { it.category == category }

    fun count(category: LogCategory): Int = entries.count { it.category == category }

    fun all(): List<DebugLogEntry> = entries.toList()
}

/** Section 4.5: the frame rules, kept as data so a reviewer can assert them. */
object FrameRules {

    const val INPUT_IN_UPDATE = true
    const val PHYSICS_IN_FIXED_UPDATE = true
    const val CAMERA_IN_LATE_UPDATE = true
    const val CACHE_SCENE_REFERENCES = true
    const val NO_FIND_IN_UPDATE = true
    const val POOL_BULLETS_VFX_DECALS_AUDIO = true

    val POOLED_KINDS: List<String> = listOf("bullets", "VFX", "decals", "audio sources")
}
