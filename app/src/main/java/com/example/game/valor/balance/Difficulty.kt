package com.example.game.valor.balance

/** Section 4.2 "Player regen" column. */
enum class RegenPolicy(val displayName: String) {
    FAST("Fast"),
    NORMAL("Normal"),
    NONE("None"),
    NONE_ONE_MEDKIT("None, 1 medkit max")
}

/** Section 4.2 "Friendly fire" column. */
enum class FriendlyFirePolicy(val displayName: String) {
    OFF("Off"),
    OFF_FOR_PLAYER_TO_ALLY("Off for player->ally"),
    ON("On"),
    ON_ALLY_DEATH_FAILS("On, ally death fails mission")
}

/** Section 4.2: the four settings and the seven multipliers each one applies. */
enum class Difficulty(
    val displayName: String,
    val enemyHpMult: Float,
    val enemyDamageMult: Float,
    val enemyAccuracyMult: Float,
    val enemyReactionTimeMult: Float,
    val regen: RegenPolicy,
    val rewardMult: Float,
    val friendlyFire: FriendlyFirePolicy
) {
    RECRUIT("Recruit (Easy)", 0.8f, 0.6f, 0.8f, 1.4f, RegenPolicy.FAST, 0.8f, FriendlyFirePolicy.OFF),
    SOLDIER(
        "Soldier (Normal)", 1.0f, 1.0f, 1.0f, 1.0f, RegenPolicy.NORMAL, 1.0f,
        FriendlyFirePolicy.OFF_FOR_PLAYER_TO_ALLY
    ),
    VETERAN("Veteran (Hard)", 1.2f, 1.4f, 1.15f, 0.8f, RegenPolicy.NONE, 1.25f, FriendlyFirePolicy.ON),
    ELITE(
        "Elite (Insane)", 1.5f, 1.8f, 1.3f, 0.65f, RegenPolicy.NONE_ONE_MEDKIT, 1.6f,
        FriendlyFirePolicy.ON_ALLY_DEATH_FAILS
    );

    fun enemyHp(baseHp: Float): Float = baseHp * enemyHpMult

    fun enemyDamage(baseDamage: Float): Float = baseDamage * enemyDamageMult

    fun enemyAccuracy(baseAccuracy: Float): Float = baseAccuracy * enemyAccuracyMult

    /** A slower reaction is easier, so the multiplier scales the wait time. */
    fun enemyReaction(baseSeconds: Float): Float = baseSeconds * enemyReactionTimeMult

    fun xp(baseXp: Int): Int = (baseXp * rewardMult).toInt()

    fun cash(baseCash: Int): Int = (baseCash * rewardMult).toInt()

    /** Section 6.3: Elite is the only setting that charges a failure fee. */
    val failureFeeFraction: Float get() = if (this == ELITE) 0.05f else 0f

    fun failureFee(cashOnHand: Int): Int = (cashOnHand * failureFeeFraction).toInt()

    companion object {
        fun byName(name: String): Difficulty =
            entries.firstOrNull { it.displayName.equals(name.trim(), ignoreCase = true) } ?: SOLDIER
    }
}
