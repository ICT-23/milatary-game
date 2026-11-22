package com.example.game.valor.plan

import com.example.game.valor.balance.ArmorMath
import com.example.game.valor.balance.RegenPolicy
import com.example.game.vanguard.CaseRecorder
import com.example.game.vanguard.ContractCase
import com.example.game.vanguard.approxEquals
import com.example.game.vanguard.observed

/** Sections 5.4 to 5.6: damage, health/death and ammunition. */
object PlanDamageCases {

    fun run(): List<ContractCase> {
        val r = CaseRecorder(ValorPlanAudit.GROUP_COMBAT)
        damageCases(r)
        healthCases(r)
        ammoCases(r)
        return r.result()
    }

    private fun request(
        base: Float,
        zone: HitZone,
        distance: Float = 0f,
        minMult: Float = 0.5f,
        armorAbsorb: Float = 0.6f
    ) = DamageRequest(
        baseDamage = base,
        zone = zone,
        distanceMeters = distance,
        weaponRangeMeters = 100f,
        minDamageMult = minMult,
        armorAbsorbRatio = armorAbsorb
    )

    private fun damageCases(r: CaseRecorder) {
        val bare = Combatant("enemy1", Team.ENEMY, 500f)
        r.check(
            "Zone multipliers", "head 2.5x, torso 1.0x, limb 0.7x",
            approxEquals(DamageSystem.compute(request(20f, HitZone.HEAD), bare), 50f) &&
                approxEquals(DamageSystem.compute(request(20f, HitZone.TORSO), bare), 20f) &&
                approxEquals(DamageSystem.compute(request(20f, HitZone.LIMB), bare), 14f),
            observed(DamageSystem.compute(request(20f, HitZone.HEAD), bare), 50f)
        )
        r.check(
            "Falloff", "at full range the shot keeps minDamageMult",
            approxEquals(DamageSystem.compute(request(20f, HitZone.TORSO, distance = 100f), bare), 10f) &&
                approxEquals(DamageSystem.falloffMultiplier(50f, 0f, 100f, 0.5f), 0.75f),
            observed(DamageSystem.falloffMultiplier(50f, 0f, 100f, 0.5f), 0.75f)
        )
        r.check(
            "Head beats torso", "a headshot is never weaker than a torso shot",
            DamageSystem.headAlwaysBeatsTorso(20f, 100f, 100f, 0.5f) &&
                DamageSystem.headAlwaysBeatsTorso(20f, 0f, 100f, 0.5f, helmetTier = 3),
            observed(DamageSystem.headAlwaysBeatsTorso(20f, 100f, 100f, 0.5f), true)
        )
        val armored = Combatant("enemy2", Team.ENEMY, 100f, armorPoints = 50f)
        val computed = DamageSystem.computeDetailed(request(20f, HitZone.TORSO), armored)
        r.check(
            "Armor absorbs 0.6", "60% of the hit comes off the armour pool",
            approxEquals(computed.armorAbsorbed, 12f) && approxEquals(computed.finalDamage, 8f),
            observed(computed, "absorbed=12 final=8")
        )
        r.check(
            "Helmet tiers", "2.5 baseline then 2.0 / 1.5 / 1.2, and tier 3 is the divergence",
            ArmorMath.headMultiplier(1) == 2.0f &&
                ArmorMath.headMultiplier(2) == 1.5f &&
                ArmorMath.headMultiplier(3) == 1.2f &&
                HelmetAdapter.DIVERGENT_TIERS == listOf(3),
            observed(HelmetAdapter.DIVERGENT_TIERS, listOf(3))
        )
        val spawn = Combatant("enemy3", Team.ENEMY, 100f, invulnerableUntil = 2f)
        r.check(
            "Spawn invulnerability", "no damage lands inside the 2 s spawn window",
            !DamageSystem.apply(request(20f, HitZone.TORSO), spawn, "alpha", Team.PLAYER, nowSeconds = 1f).applied &&
                DamageSystem.apply(request(20f, HitZone.TORSO), spawn, "alpha", Team.PLAYER, nowSeconds = 3f).applied,
            observed(spawn.current, 80f)
        )
        val victim = Combatant("enemy4", Team.ENEMY, 10f)
        val bus = EventBus()
        val killing = DamageSystem.apply(request(100f, HitZone.TORSO), victim, "alpha", Team.PLAYER, bus = bus)
        val again = DamageSystem.apply(request(100f, HitZone.TORSO), victim, "alpha", Team.PLAYER, bus = bus)
        r.check(
            "Exactly one kill", "a dead target refuses damage and the kill fires once",
            killing.killed && !again.applied &&
                bus.count(PlanEventType.ENTITY_KILLED) == 1 &&
                bus.count(PlanEventType.DAMAGE_DEALT) == 1,
            observed(bus.count(PlanEventType.ENTITY_KILLED), 1)
        )
        r.check(
            "Clamp at zero", "a negative multiplier still resolves to zero damage",
            DamageSystem.compute(
                request(20f, HitZone.TORSO).copy(penetrationMultiplier = -4f), bare
            ) == 0f,
            observed(DamageSystem.compute(request(20f, HitZone.TORSO).copy(penetrationMultiplier = -4f), bare), 0f)
        )
        val player = Combatant("player", Team.PLAYER, 100f, isPlayer = true)
        val chip = DamageSystem.apply(
            DamageRequest(0.2f, HitZone.TORSO, 0f, 100f, 1f), player, "enemy1", Team.ENEMY
        )
        r.check(
            "Player minimum damage", "a player never takes less than one point",
            chip.applied && approxEquals(chip.healthLost, 1f),
            observed(chip.healthLost, 1f)
        )
        val ally = Combatant("brooks", Team.ALLY, 100f, startHealth = 50f, storyCritical = true)
        val stray = DamageSystem.apply(request(500f, HitZone.TORSO), ally, "alpha", Team.PLAYER)
        r.check(
            "Story-critical ally", "a stray bullet leaves a critical ally standing",
            stray.applied && ally.isAlive && approxEquals(ally.current, 1f),
            observed(ally.current, 1f)
        )
        val scripted = Combatant("brooks2", Team.ALLY, 100f, startHealth = 50f)
        val scriptedHit = DamageSystem.apply(
            request(500f, HitZone.TORSO), scripted, "alpha", Team.PLAYER, scriptedBeat = true
        )
        r.check(
            "Scripted beat", "the same shot kills a body with no story-critical flag",
            scriptedHit.killed && !scripted.isAlive,
            observed(scripted.isAlive, false)
        )
    }

    private fun healthCases(r: CaseRecorder) {
        r.check(
            "Regen policy", "Normal and Fast regenerate, Hard and Elite do not",
            HealthRules.allowsRegen(RegenPolicy.NORMAL) &&
                HealthRules.allowsRegen(RegenPolicy.FAST) &&
                !HealthRules.allowsRegen(RegenPolicy.NONE) &&
                !HealthRules.allowsRegen(RegenPolicy.NONE_ONE_MEDKIT),
            observed(HealthRules.allowsRegen(RegenPolicy.NONE), false)
        )
        r.check(
            "Regen after 5 s", "nothing regenerates before the delay elapses",
            !HealthRules.canRegen(10f, 100f, 0f, 4.9f, RegenPolicy.NORMAL) &&
                HealthRules.canRegen(10f, 100f, 0f, 5f, RegenPolicy.NORMAL),
            observed(HealthRules.canRegen(10f, 100f, 0f, 4.9f, RegenPolicy.NORMAL), false)
        )
        r.check(
            "Regen ceiling", "regeneration stops at 30% of maximum health",
            approxEquals(HealthRules.regenCeiling(100f), 30f) &&
                approxEquals(HealthRules.regenStep(29f, 100f, 5f), 30f),
            observed(HealthRules.regenCeiling(100f), 30f)
        )
        r.check(
            "Low health", "the vignette starts below 25%",
            HealthRules.isLowHealth(24f, 100f) && !HealthRules.isLowHealth(25f, 100f),
            observed(HealthRules.isLowHealth(25f, 100f), false)
        )
        val downed = Combatant("ally1", Team.ALLY, 100f, startHealth = 10f)
        DamageSystem.apply(request(50f, HitZone.TORSO), downed, "enemy1", Team.ENEMY)
        val dead = downed.dead
        downed.revive(HealthRules.REVIVE_HEALTH_FRACTION, HealthRules.REVIVE_INVULNERABILITY_SECONDS, 10f)
        r.check(
            "Revive", "revive restores 50% health and 2 s of invulnerability",
            dead && approxEquals(downed.current, 50f) && downed.isInvulnerable(11.9f) &&
                !downed.isInvulnerable(12f),
            observed(downed.current, 50f)
        )
        val unmarked = Combatant("ally3", Team.ALLY, 100f, startHealth = 1f)
        unmarked.receiveDamage(DamageInfo(5f, HitZone.TORSO, DamageType.BULLET, "enemy1", Team.ENEMY))
        r.check(
            "Death flow once", "the flow is reported once per death",
            HealthRules.DEATH_FLOW.size == 5 &&
                HealthRules.shouldRunDeathFlow(unmarked) &&
                unmarked.markKillReported() &&
                !HealthRules.shouldRunDeathFlow(unmarked),
            observed(unmarked.killReported, true)
        )
        r.check(
            "Kill credit", "rewards only for a player-team killer during play",
            KillCredit.grantsPlayerRewards(Team.PLAYER, true) &&
                !KillCredit.grantsPlayerRewards(Team.ALLY, true) &&
                !KillCredit.grantsPlayerRewards(Team.PLAYER, false),
            observed(KillCredit.grantsPlayerRewards(Team.ALLY, true), false)
        )
        val full = Combatant("ally2", Team.ALLY, 100f)
        r.check(
            "Healing at full HP", "a medkit on a full bar heals nothing",
            approxEquals(full.heal(50f), 0f) && approxEquals(full.current, 100f),
            observed(full.heal(50f), 0f)
        )
    }

    private fun ammoCases(r: CaseRecorder) {
        r.check(
            "Reload start", "needs a partial magazine, a reserve and no reload",
            AmmoRules.canStartReload(AmmoState(10, 30, 60, 120), alreadyReloading = false) &&
                !AmmoRules.canStartReload(AmmoState(30, 30, 60, 120), alreadyReloading = false) &&
                !AmmoRules.canStartReload(AmmoState(10, 30, 0, 120), alreadyReloading = false) &&
                !AmmoRules.canStartReload(AmmoState(10, 30, 60, 120), alreadyReloading = true),
            observed(AmmoRules.canStartReload(AmmoState(30, 30, 60, 120), false), false)
        )
        r.check(
            "Tactical vs empty", "an empty magazine uses the longer reload",
            approxEquals(AmmoRules.reloadDuration(2.5f, 3.4f, 0), 3.4f) &&
                approxEquals(AmmoRules.reloadDuration(2.5f, 3.4f, 12), 2.5f),
            observed(AmmoRules.reloadDuration(2.5f, 3.4f, 0), 3.4f)
        )
        val start = AmmoState(5, 30, 60, 120)
        val finished = AmmoRules.finish(start)
        r.check(
            "Ammo moves, never appears", "the total is identical after a reload",
            finished.magazine == 30 && finished.reserve == 35 && finished.total == start.total,
            observed(finished, "30 / 35")
        )
        val reload = ReloadProgress(0f, 2f, start)
        r.check(
            "Reload is atomic", "mid-reload and cancelled reloads keep the old values",
            reload.midReloadAmmo() == start && !reload.isComplete(1.9f) &&
                reload.cancel() == start && reload.isComplete(2.1f),
            observed(reload.progress(1f), 0.5f)
        )
        r.check(
            "Shell by shell", "each shell adds one round and spends one reserve",
            AmmoRules.loadOneShell(AmmoState(0, 6, 10, 30)).let {
                it.magazine == 1 && it.reserve == 9
            } && AmmoRules.interruptibleByFire(reloadPerShell = true),
            observed(AmmoRules.loadOneShell(AmmoState(0, 6, 10, 30)).magazine, 1)
        )
        val picked = AmmoRules.pickup(AmmoState(10, 30, 115, 120), 20)
        r.check(
            "Pickup caps", "the reserve stops at the cap and the excess is wasted",
            picked.first.reserve == 120 && picked.second == 15,
            observed(picked.second, 15)
        )
        r.check(
            "Infinite only in training", "combat never grants infinite ammo",
            AmmoRules.infiniteAllowed(AmmoContext.TRAINING_RANGE) &&
                AmmoRules.infiniteAllowed(AmmoContext.DEBUG) &&
                !AmmoRules.infiniteAllowed(AmmoContext.COMBAT),
            observed(AmmoRules.infiniteAllowed(AmmoContext.COMBAT), false)
        )
        val primary = WeaponInstance("ar", AmmoState(12, 30, 40, 120))
        val sidearm = WeaponInstance("pistol", AmmoState(7, 15, 21, 60))
        val swapped = AmmoRules.swap(primary, sidearm)
        r.check(
            "Swap keeps state", "both weapons keep their own magazine and reserve",
            swapped.first === primary && swapped.second === sidearm &&
                swapped.first.ammo.magazine == 12 && swapped.second.ammo.magazine == 7 &&
                !AmmoRules.reloadSurvivesSwap(),
            observed(primary.ammo.magazine, 12)
        )
        r.check(
            "No negative total", "a reload with an empty reserve changes nothing",
            AmmoRules.finish(AmmoState(0, 30, 0, 120)).total == 0 &&
                AmmoRules.loadOneShell(AmmoState(0, 6, 0, 30)).total == 0,
            observed(AmmoRules.finish(AmmoState(0, 30, 0, 120)).total, 0)
        )
    }
}
