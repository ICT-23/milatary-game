package com.example.game.valor.plan

import com.example.game.valor.balance.RegenPolicy

/**
 * Section 5.5: the health component in its pure form. `receiveDamage` is the
 * single mutation entry and is only ever called by `DamageSystem`.
 */
class Combatant(
    override val id: String,
    override var team: Team,
    val maxHealth: Float,
    startHealth: Float = maxHealth,
    var armorPoints: Float = 0f,
    var helmetTier: Int? = null,
    var damageReduction: Float = 0f,
    var storyCritical: Boolean = false,
    val isPlayer: Boolean = false,
    var invulnerableUntil: Float = 0f
) : Damageable {

    var current: Float = startHealth.coerceIn(0f, maxHealth)
        private set

    /** Section 5.5: the guard that makes the death flow run exactly once. */
    var dead: Boolean = false
        private set

    var killReported: Boolean = false
        private set

    var lastDamageAtSeconds: Float = Float.NEGATIVE_INFINITY

    override val isAlive: Boolean get() = !dead && current > 0f

    val healthFraction: Float get() = if (maxHealth <= 0f) 0f else current / maxHealth

    override fun receiveDamage(info: DamageInfo): Boolean {
        if (!isAlive || info.amount <= 0f) return false
        val before = current
        current = (current - info.amount).coerceAtLeast(0f)
        if (current <= 0f) dead = true
        return current < before
    }

    /** Medkits and scripted heals; never above max. */
    fun heal(amount: Float): Float {
        if (amount <= 0f || !isAlive) return 0f
        val before = current
        current = (current + amount).coerceAtMost(maxHealth)
        return current - before
    }

    /** Section 5.5 "Revive restores 50% health + 2 s invulnerability". */
    fun revive(healthFraction: Float, invulnerabilitySeconds: Float, nowSeconds: Float) {
        dead = false
        killReported = false
        current = (maxHealth * healthFraction.coerceIn(0f, 1f)).coerceAtMost(maxHealth)
        invulnerableUntil = nowSeconds + invulnerabilitySeconds
    }

    fun markKillReported(): Boolean {
        if (killReported) return false
        killReported = true
        return true
    }

    fun isInvulnerable(nowSeconds: Float): Boolean = nowSeconds < invulnerableUntil
}

/** Section 5.5: the ordered death flow, run once thanks to [Combatant.dead]. */
enum class DeathStep(val detail: String) {
    MARK_DEAD("isDead = true"),
    DISABLE_RECEPTION("Disable damage reception, AI, weapon, input"),
    PLAY_DEATH("Death animation / ragdoll with last-hit impulse"),
    FIRE_KILLED_EVENT("Fire OnEntityKilled exactly once"),
    DROP_LOOT("Drop ammo / cash for enemies")
}

/** Section 5.5: the health numbers the rules quote. */
object HealthRules {

    const val REGEN_DELAY_SECONDS = 5f
    const val REGEN_CEILING_FRACTION = 0.30f
    const val LOW_HEALTH_FRACTION = 0.25f
    const val SPAWN_INVULNERABILITY_SECONDS = 2f
    const val REVIVE_HEALTH_FRACTION = 0.5f
    const val REVIVE_INVULNERABILITY_SECONDS = 2f
    const val PLAYER_MIN_DAMAGE = 1f

    val DEATH_FLOW: List<DeathStep> = DeathStep.entries

    /** "Slow regen after 5 s without damage up to 30% (Normal); Hard: no regen." */
    fun allowsRegen(policy: RegenPolicy): Boolean =
        policy == RegenPolicy.FAST || policy == RegenPolicy.NORMAL

    fun regenCeiling(maxHealth: Float): Float = maxHealth * REGEN_CEILING_FRACTION

    fun isLowHealth(current: Float, maxHealth: Float): Boolean =
        current < maxHealth * LOW_HEALTH_FRACTION

    fun canRegen(
        current: Float,
        maxHealth: Float,
        lastDamageAtSeconds: Float,
        nowSeconds: Float,
        policy: RegenPolicy
    ): Boolean =
        allowsRegen(policy) &&
            nowSeconds - lastDamageAtSeconds >= REGEN_DELAY_SECONDS &&
            current < regenCeiling(maxHealth)

    fun regenStep(current: Float, maxHealth: Float, amount: Float): Float =
        (current + amount).coerceAtMost(regenCeiling(maxHealth))

    /** True only the first time a corpse is seen, so the flow cannot double-run. */
    fun shouldRunDeathFlow(combatant: Combatant): Boolean = combatant.dead && !combatant.killReported
}

/** Section 5.5 "Kill credit": the source of the last damaging hit, if still valid. */
object KillCredit {

    const val LAST_HIT_WINS = true

    fun creditsKiller(sourceId: String?, killerStillAlive: Boolean): Boolean =
        sourceId != null && killerStillAlive

    /** XP, cash and medals are only for a player-team killer on a live victim. */
    fun grantsPlayerRewards(killerTeam: Team?, missionPlaying: Boolean): Boolean =
        killerTeam == Team.PLAYER && missionPlaying
}

/** Section 5.5 death screen options, in the order the document lists them. */
enum class DeathOption(val displayName: String) {
    REVIVE("Revive (rewarded ad or medkit token)"),
    RETRY_CHECKPOINT("Retry from last checkpoint"),
    QUIT("Quit")
}
