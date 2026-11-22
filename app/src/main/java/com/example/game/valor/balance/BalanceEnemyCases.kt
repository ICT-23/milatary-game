package com.example.game.valor.balance

import com.example.game.vanguard.CaseRecorder
import com.example.game.vanguard.ContractCase
import com.example.game.vanguard.approxEquals
import com.example.game.vanguard.observed

/** Sections 4.1 and 4.4 enemy rows and Survival wave scaling, plus 4.2 difficulty. */
object BalanceEnemyCases {

    private fun row(
        id: String,
        hp: Float,
        armor: Float,
        accuracy: Float,
        reaction: Float,
        speed: Float,
        xp: Int,
        cashMin: Int,
        cashMax: Int
    ): Boolean {
        val enemy = EnemyCatalog.byId(id) ?: return false
        return approxEquals(enemy.hp, hp) &&
            approxEquals(enemy.armor, armor) &&
            approxEquals(enemy.accuracy, accuracy) &&
            approxEquals(enemy.reactionSeconds, reaction) &&
            approxEquals(enemy.speedMetersPerSecond, speed) &&
            enemy.xp == xp &&
            enemy.cashDropMin == cashMin &&
            enemy.cashDropMax == cashMax
    }

    private fun actual(id: String): String {
        val enemy = EnemyCatalog.byId(id) ?: return "$id missing"
        return "hp=${enemy.hp} armor=${enemy.armor} acc=${enemy.accuracy} " +
            "react=${enemy.reactionSeconds} spd=${enemy.speedMetersPerSecond} " +
            "xp=${enemy.xp} cash=${enemy.cashDropMin}-${enemy.cashDropMax}"
    }

    fun run(): List<ContractCase> {
        val r = CaseRecorder(BalanceAudit.GROUP_ENEMIES)

        r.check(
            "Enemy rows", "12 rows in section 4.1",
            EnemyCatalog.ENEMIES.size == 12, observed(EnemyCatalog.ENEMIES.size, 12)
        )
        r.check(
            "Rifleman", "100 HP, 0 armour, 0.35 acc, 0.6 s, 3.5 m/s, 20 XP, 10-30",
            row(EnemyCatalog.RIFLEMAN_ID, 100f, 0f, 0.35f, 0.6f, 3.5f, 20, 10, 30),
            actual(EnemyCatalog.RIFLEMAN_ID)
        )
        r.check(
            "Rusher", "70 HP, 0.25 acc, 0.4 s, 5.5 m/s, 15 XP, 5-20",
            row(EnemyCatalog.RUSHER_ID, 70f, 0f, 0.25f, 0.4f, 5.5f, 15, 5, 20),
            actual(EnemyCatalog.RUSHER_ID)
        )
        r.check(
            "Sniper", "80 HP, 0.7 acc, 1.0 s + 1.0 s laser warning, 40 XP",
            row(EnemyCatalog.SNIPER_ID, 80f, 0f, 0.70f, 1.0f, 2.8f, 40, 30, 60) &&
                approxEquals(EnemyCatalog.byId(EnemyCatalog.SNIPER_ID)?.laserWarningSeconds ?: 0f, 1f),
            actual(EnemyCatalog.SNIPER_ID)
        )
        r.check(
            "Heavy gunner", "250 HP, 100 armour, 60 XP, 40-80",
            row(EnemyCatalog.HEAVY_ID, 250f, 100f, 0.30f, 0.8f, 2.2f, 60, 40, 80),
            actual(EnemyCatalog.HEAVY_ID)
        )
        r.check(
            "Grenadier", "100 HP, rifle + grenades, 35 XP",
            row(EnemyCatalog.GRENADIER_ID, 100f, 0f, 0.35f, 0.6f, 3.5f, 35, 20, 40),
            actual(EnemyCatalog.GRENADIER_ID)
        )
        r.check(
            "Officer", "120 HP, 40 armour, 80 XP, 60-120",
            row(EnemyCatalog.OFFICER_ID, 120f, 40f, 0.45f, 0.5f, 3.2f, 80, 60, 120),
            actual(EnemyCatalog.OFFICER_ID)
        )
        r.check(
            "Medic", "80 HP, 0.2 acc, 30 XP",
            row(EnemyCatalog.MEDIC_ID, 80f, 0f, 0.20f, 0.8f, 3.4f, 30, 20, 40),
            actual(EnemyCatalog.MEDIC_ID)
        )
        r.check(
            "Armored vehicle", "1,200 HP, 400 armour, flat 150 cash",
            row(EnemyCatalog.VEHICLE_ID, 1_200f, 400f, 0.50f, 1.0f, 4.0f, 150, 150, 150),
            actual(EnemyCatalog.VEHICLE_ID)
        )
        r.check(
            "Tank", "2,500 HP, 800 armour, 300 XP, flat 300 cash",
            row(EnemyCatalog.TANK_ID, 2_500f, 800f, 0.55f, 1.2f, 3.0f, 300, 300, 300),
            actual(EnemyCatalog.TANK_ID)
        )
        r.check(
            "Enemy drone", "120 HP, 8.0 m/s, 50 XP",
            row(EnemyCatalog.DRONE_ID, 120f, 0f, 0.40f, 0.5f, 8.0f, 50, 40, 40),
            actual(EnemyCatalog.DRONE_ID)
        )
        val mini = EnemyCatalog.byId(EnemyCatalog.MINI_BOSS_ID)
        r.check(
            "Mini boss", "800-1,500 HP, 100-200 armour, 500 XP",
            mini != null && approxEquals(mini.hp, 800f) && approxEquals(mini.hpMax, 1_500f) &&
                approxEquals(mini.armor, 100f) && approxEquals(mini.armorMax, 200f) && mini.xp == 500,
            actual(EnemyCatalog.MINI_BOSS_ID)
        )
        r.check(
            "Final boss", "3,000 HP, 300 armour, 2,000 XP",
            row(EnemyCatalog.FINAL_BOSS_ID, 3_000f, 300f, 0.60f, 0.3f, 3.5f, 2_000, 2_000, 2_000),
            actual(EnemyCatalog.FINAL_BOSS_ID)
        )
        r.check(
            "Armoured enemies", "6 rows carry an armour pool",
            EnemyCatalog.armoredEnemies().size == 6,
            observed(EnemyCatalog.armoredEnemies().map { it.id }, 6)
        )

        r.check(
            "Wave count", "4 + wave x 1.5",
            approxEquals(WaveScaling.enemyCount(0), 4f) && approxEquals(WaveScaling.enemyCount(10), 19f),
            observed(
                "${WaveScaling.enemyCount(0)}/${WaveScaling.enemyCount(10)}",
                "4/19"
            )
        )
        r.check(
            "Wave cap", "30 total enemies",
            approxEquals(WaveScaling.enemyCount(20), 30f) && approxEquals(WaveScaling.enemyCount(90), 30f),
            observed(WaveScaling.enemyCount(90), 30f)
        )
        r.check(
            "Wave HP scaling", "1 + wave x 0.03",
            approxEquals(WaveScaling.hpMultiplier(10), 1.30f),
            observed(WaveScaling.hpMultiplier(10), 1.30f)
        )
        r.check(
            "Wave accuracy", "base + wave x 0.005",
            approxEquals(WaveScaling.accuracy(10, 0.35f), 0.40f),
            observed(WaveScaling.accuracy(10, 0.35f), 0.40f)
        )
        r.check(
            "Wave accuracy cap", "never more than +0.2",
            approxEquals(WaveScaling.accuracy(100, 0.35f), 0.55f),
            observed(WaveScaling.accuracy(100, 0.35f), 0.55f)
        )
        r.check(
            "Elite and mini-boss cadence", "every 5th wave elite, every 10th adds a mini boss",
            WaveScaling.isEliteWave(5) && !WaveScaling.isEliteWave(6) &&
                WaveScaling.isEliteWave(10) && WaveScaling.isMiniBossWave(10) &&
                !WaveScaling.isMiniBossWave(5),
            observed(
                "${WaveScaling.isEliteWave(5)}/${WaveScaling.isMiniBossWave(10)}",
                "true/true"
            )
        )
        r.check(
            "Wave rewards", "cash = 50 x wave, XP = 30 x wave",
            WaveScaling.cashReward(7) == 350 && WaveScaling.xpReward(7) == 210,
            observed("${WaveScaling.cashReward(7)}/${WaveScaling.xpReward(7)}", "350/210")
        )

        val d = CaseRecorder(BalanceAudit.GROUP_DIFFICULTY)
        difficulty(d)
        return r.result() + d.result()
    }

    private fun difficulty(r: CaseRecorder) {
        r.check(
            "Four settings", "Recruit, Soldier, Veteran, Elite",
            Difficulty.entries.map { it.displayName } == listOf(
                "Recruit (Easy)", "Soldier (Normal)", "Veteran (Hard)", "Elite (Insane)"
            ),
            observed(Difficulty.entries.map { it.displayName }, 4)
        )
        r.check(
            "Recruit row", "0.8 / 0.6 / 0.8 / 1.4 / fast / 0.8 / off",
            approxEquals(Difficulty.RECRUIT.enemyHpMult, 0.8f) &&
                approxEquals(Difficulty.RECRUIT.enemyDamageMult, 0.6f) &&
                approxEquals(Difficulty.RECRUIT.enemyAccuracyMult, 0.8f) &&
                approxEquals(Difficulty.RECRUIT.enemyReactionTimeMult, 1.4f) &&
                Difficulty.RECRUIT.regen == RegenPolicy.FAST &&
                approxEquals(Difficulty.RECRUIT.rewardMult, 0.8f) &&
                Difficulty.RECRUIT.friendlyFire == FriendlyFirePolicy.OFF,
            "${Difficulty.RECRUIT}"
        )
        r.check(
            "Soldier row", "identity multipliers, friendly fire off for player->ally",
            approxEquals(Difficulty.SOLDIER.enemyHpMult, 1f) &&
                approxEquals(Difficulty.SOLDIER.enemyDamageMult, 1f) &&
                approxEquals(Difficulty.SOLDIER.enemyAccuracyMult, 1f) &&
                approxEquals(Difficulty.SOLDIER.enemyReactionTimeMult, 1f) &&
                Difficulty.SOLDIER.regen == RegenPolicy.NORMAL &&
                approxEquals(Difficulty.SOLDIER.rewardMult, 1f) &&
                Difficulty.SOLDIER.friendlyFire == FriendlyFirePolicy.OFF_FOR_PLAYER_TO_ALLY,
            "${Difficulty.SOLDIER}"
        )
        r.check(
            "Veteran row", "1.2 / 1.4 / 1.15 / 0.8 / none / 1.25 / on",
            approxEquals(Difficulty.VETERAN.enemyHpMult, 1.2f) &&
                approxEquals(Difficulty.VETERAN.enemyDamageMult, 1.4f) &&
                approxEquals(Difficulty.VETERAN.enemyAccuracyMult, 1.15f) &&
                approxEquals(Difficulty.VETERAN.enemyReactionTimeMult, 0.8f) &&
                Difficulty.VETERAN.regen == RegenPolicy.NONE &&
                approxEquals(Difficulty.VETERAN.rewardMult, 1.25f) &&
                Difficulty.VETERAN.friendlyFire == FriendlyFirePolicy.ON,
            "${Difficulty.VETERAN}"
        )
        r.check(
            "Elite row", "1.5 / 1.8 / 1.3 / 0.65 / one medkit / 1.6 / ally death fails",
            approxEquals(Difficulty.ELITE.enemyHpMult, 1.5f) &&
                approxEquals(Difficulty.ELITE.enemyDamageMult, 1.8f) &&
                approxEquals(Difficulty.ELITE.enemyAccuracyMult, 1.3f) &&
                approxEquals(Difficulty.ELITE.enemyReactionTimeMult, 0.65f) &&
                Difficulty.ELITE.regen == RegenPolicy.NONE_ONE_MEDKIT &&
                approxEquals(Difficulty.ELITE.rewardMult, 1.6f) &&
                Difficulty.ELITE.friendlyFire == FriendlyFirePolicy.ON_ALLY_DEATH_FAILS,
            "${Difficulty.ELITE}"
        )
        r.check(
            "Multipliers applied", "Elite turns 100 HP into 150 and 0.6 s into 0.39 s",
            approxEquals(Difficulty.ELITE.enemyHp(100f), 150f) &&
                approxEquals(Difficulty.ELITE.enemyReaction(0.6f), 0.39f),
            observed("${Difficulty.ELITE.enemyHp(100f)}/${Difficulty.ELITE.enemyReaction(0.6f)}", "150/0.39")
        )
        r.check(
            "Reward scaling", "Recruit pays 0.8x, Elite 1.6x",
            Difficulty.RECRUIT.xp(100) == 80 && Difficulty.ELITE.xp(100) == 160 &&
                Difficulty.ELITE.cash(1_000) == 1_600,
            observed("${Difficulty.RECRUIT.xp(100)}/${Difficulty.ELITE.xp(100)}", "80/160")
        )
        r.check(
            "Repair fee", "only Elite charges 5% of cash on failure",
            approxEquals(Difficulty.ELITE.failureFeeFraction, 0.05f) &&
                Difficulty.ELITE.failureFee(2_000) == 100 &&
                Difficulty.SOLDIER.failureFee(2_000) == 0,
            observed(Difficulty.ELITE.failureFee(2_000), 100)
        )
    }
}
