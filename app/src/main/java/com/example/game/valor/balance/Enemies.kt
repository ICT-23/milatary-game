package com.example.game.valor.balance

/**
 * Section 4.1: one base (Normal) enemy row.
 *
 * HP and armour are ranges only for the mini boss; every other row has a single
 * value, so [hpMax] and [armorMax] default to it. [accuracy] is the AI cone
 * width factor - higher is tighter - and never bypasses hit detection.
 */
data class EnemySpec(
    val id: String,
    val type: String,
    val hp: Float,
    val hpMax: Float,
    val armor: Float,
    val armorMax: Float,
    val weapon: String,
    val accuracy: Float,
    val reactionSeconds: Float,
    val laserWarningSeconds: Float,
    val speedMetersPerSecond: Float,
    val xp: Int,
    val cashDropMin: Int,
    val cashDropMax: Int
) {
    val cashIsRange: Boolean get() = cashDropMax > cashDropMin
    val averageCashDrop: Int get() = (cashDropMin + cashDropMax) / 2
    val effectiveHitPoints: Float get() = hp + armor
}

/** Section 4.4: Survival wave scaling, written exactly as the block states it. */
object WaveScaling {

    const val BASE_ENEMIES = 4f
    const val PER_WAVE = 1.5f
    const val TOTAL_CAP = 30f
    const val MAX_SIMULTANEOUS = 8
    const val HP_MULT_PER_WAVE = 0.03f
    const val ACCURACY_PER_WAVE = 0.005f
    const val ACCURACY_CAP_BONUS = 0.2f
    const val ELITE_EVERY = 5
    const val MINI_BOSS_EVERY = 10
    const val CASH_PER_WAVE = 50
    const val XP_PER_WAVE = 30

    /** "waveEnemyCount = 4 + wave * 1.5 (cap 30 total, 8 simultaneous)". */
    fun enemyCount(wave: Int): Float =
        (BASE_ENEMIES + wave * PER_WAVE).coerceIn(0f, TOTAL_CAP)

    fun hpMultiplier(wave: Int): Float = 1f + wave * HP_MULT_PER_WAVE

    /** "enemyAccuracy = base + wave * 0.005 (cap +0.2)". */
    fun accuracy(wave: Int, baseAccuracy: Float): Float =
        (baseAccuracy + wave * ACCURACY_PER_WAVE).coerceAtMost(baseAccuracy + ACCURACY_CAP_BONUS)

    fun isEliteWave(wave: Int): Boolean = wave > 0 && wave % ELITE_EVERY == 0

    fun isMiniBossWave(wave: Int): Boolean = wave > 0 && wave % MINI_BOSS_EVERY == 0

    fun cashReward(wave: Int): Int = CASH_PER_WAVE * wave

    fun xpReward(wave: Int): Int = XP_PER_WAVE * wave

    /** Elite waves field an Officer plus a Heavy; every tenth wave adds a mini boss. */
    fun eliteRoster(): List<String> = listOf(EnemyCatalog.OFFICER_ID, EnemyCatalog.HEAVY_ID)
}

object EnemyCatalog {

    const val RIFLEMAN_ID = "rifleman"
    const val RUSHER_ID = "rusher"
    const val SNIPER_ID = "sniper"
    const val HEAVY_ID = "heavy_gunner"
    const val GRENADIER_ID = "grenadier"
    const val OFFICER_ID = "officer"
    const val MEDIC_ID = "medic"
    const val VEHICLE_ID = "armored_vehicle"
    const val TANK_ID = "tank"
    const val DRONE_ID = "enemy_drone"
    const val MINI_BOSS_ID = "boss_mini"
    const val FINAL_BOSS_ID = "boss_final"

    private fun enemy(
        id: String,
        type: String,
        hp: Float,
        armor: Float,
        weapon: String,
        accuracy: Float,
        reaction: Float,
        speed: Float,
        xp: Int,
        cashMin: Int,
        cashMax: Int = cashMin,
        hpMax: Float = hp,
        armorMax: Float = armor,
        laserWarning: Float = 0f
    ) = EnemySpec(
        id, type, hp, hpMax, armor, armorMax, weapon, accuracy, reaction,
        laserWarning, speed, xp, cashMin, cashMax
    )

    val ENEMIES: List<EnemySpec> = listOf(
        enemy(RIFLEMAN_ID, "Rifleman", 100f, 0f, "Rifle", 0.35f, 0.6f, 3.5f, 20, 10, 30),
        enemy(RUSHER_ID, "Rusher", 70f, 0f, "Knife / SMG", 0.25f, 0.4f, 5.5f, 15, 5, 20),
        enemy(SNIPER_ID, "Sniper", 80f, 0f, "Sniper", 0.7f, 1.0f, 2.8f, 40, 30, 60,
            laserWarning = 1.0f),
        enemy(HEAVY_ID, "Heavy gunner", 250f, 100f, "LMG", 0.30f, 0.8f, 2.2f, 60, 40, 80),
        enemy(GRENADIER_ID, "Grenadier", 100f, 0f, "Rifle + grenades", 0.35f, 0.6f, 3.5f, 35, 20, 40),
        enemy(OFFICER_ID, "Officer", 120f, 40f, "Pistol", 0.45f, 0.5f, 3.2f, 80, 60, 120),
        enemy(MEDIC_ID, "Medic", 80f, 0f, "Pistol", 0.2f, 0.8f, 3.4f, 30, 20, 40),
        enemy(VEHICLE_ID, "Armored vehicle", 1_200f, 400f, "Mounted MG", 0.5f, 1.0f, 4.0f, 150, 150),
        enemy(TANK_ID, "Tank", 2_500f, 800f, "Cannon + MG", 0.55f, 1.2f, 3.0f, 300, 300),
        enemy(DRONE_ID, "Enemy drone", 120f, 0f, "Mini gun", 0.4f, 0.5f, 8.0f, 50, 40),
        enemy(MINI_BOSS_ID, "Boss (mini)", 800f, 100f, "Varies", 0.5f, 0.4f, 3.0f, 500, 500,
            hpMax = 1_500f, armorMax = 200f),
        enemy(FINAL_BOSS_ID, "Boss (final)", 3_000f, 300f, "Multi-phase", 0.6f, 0.3f, 3.5f, 2_000, 2_000)
    )

    fun byId(id: String): EnemySpec? = ENEMIES.firstOrNull { it.id == id }

    fun killXp(id: String): Int = byId(id)?.xp ?: 0

    /** Rows whose armour pool is non-zero, i.e. where AP ammunition pays off. */
    fun armoredEnemies(): List<EnemySpec> = ENEMIES.filter { it.armor > 0f }

    fun bosses(): List<EnemySpec> = listOfNotNull(byId(MINI_BOSS_ID), byId(FINAL_BOSS_ID))

    /** Section 4.3: heavy gunner time-to-kill check against focused rifle fire. */
    fun heavyTimeToKill(shotDamage: Float, rpm: Float, armorPiercingSeconds: Boolean = false): Float {
        val heavy = byId(HEAVY_ID) ?: return 0f
        if (armorPiercingSeconds) return TimeToKillTargets.HEAVY_ARMOR_PIERCING_SECONDS
        val shots = DamageMath.bodyShotsToKill(shotDamage, heavy.effectiveHitPoints)
        return DamageMath.secondsForShots(shots, rpm)
    }
}
