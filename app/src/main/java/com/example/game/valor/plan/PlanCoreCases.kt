package com.example.game.valor.plan

import com.example.game.vanguard.CaseRecorder
import com.example.game.vanguard.ContractCase
import com.example.game.vanguard.approxEquals
import com.example.game.vanguard.observed

/** Section 4: layers, teams, damage payload, event bus and frame rules. */
object PlanCoreCases {

    fun run(): List<ContractCase> {
        val r = CaseRecorder(ValorPlanAudit.GROUP_CORE)

        r.check(
            "Physics layers", "9 layers named in the table",
            PhysicsLayer.entries.size == 9 &&
                PhysicsLayer.entries.map { it.displayName } == listOf(
                    "Player", "Enemy", "Ally", "Environment", "Cover",
                    "Glass", "Destructible", "Projectile", "Trigger"
                ),
            observed(PhysicsLayer.entries.size, 9)
        )
        r.check(
            "Bullet mask", "Enemy, Ally, Player, Environment, Cover, Glass, Destructible",
            BulletMask.INCLUDED == setOf(
                PhysicsLayer.ENEMY, PhysicsLayer.ALLY, PhysicsLayer.PLAYER,
                PhysicsLayer.ENVIRONMENT, PhysicsLayer.COVER,
                PhysicsLayer.GLASS, PhysicsLayer.DESTRUCTIBLE
            ),
            observed(BulletMask.INCLUDED.size, 7)
        )
        r.check(
            "Mask exclusions", "Projectile and Trigger never receive a bullet",
            !BulletMask.accepts(PhysicsLayer.PROJECTILE) && !BulletMask.accepts(PhysicsLayer.TRIGGER),
            observed(BulletMask.EXCLUDED, setOf(PhysicsLayer.PROJECTILE, PhysicsLayer.TRIGGER))
        )
        r.check(
            "Shooter excluded", "the same layer is refused only for the shooter",
            BulletMask.accepts(PhysicsLayer.ENEMY, "alpha", "alpha") &&
                BulletMask.accepts(PhysicsLayer.ENEMY, "bravo", "alpha"),
            observed(BulletMask.accepts(PhysicsLayer.ENEMY, "alpha", "alpha"), false)
        )
        r.check(
            "Teams", "Player and Ally share a side, Enemy only with Enemy",
            Team.entries.size == 4 &&
                Team.PLAYER.playerSide && Team.ALLY.playerSide &&
                !Team.ENEMY.playerSide &&
                Team.sameSide(Team.PLAYER, Team.ALLY) &&
                Team.sameSide(Team.ENEMY, Team.ENEMY) &&
                !Team.sameSide(Team.PLAYER, Team.ENEMY),
            observed(Team.entries.size, 4)
        )
        r.check(
            "Hit zone multipliers", "Head 2.5, Torso 1.0, Limb 0.7",
            HitZone.HEAD.multiplier == 2.5f &&
                HitZone.TORSO.multiplier == 1.0f &&
                HitZone.LIMB.multiplier == 0.7f,
            observed(
                listOf(HitZone.HEAD.multiplier, HitZone.TORSO.multiplier, HitZone.LIMB.multiplier),
                listOf(2.5f, 1.0f, 0.7f)
            )
        )
        r.check(
            "Damage types", "Bullet, Explosion, Melee, Drone, Fall",
            DamageType.entries.map { it.displayName } ==
                listOf("Bullet", "Explosion", "Melee", "Drone", "Fall"),
            observed(DamageType.entries.size, 5)
        )
        r.check(
            "DamageInfo payload", "amount, zone, type, source, team in one value",
            DamageInfo(25f, HitZone.HEAD, DamageType.BULLET, "alpha", Team.PLAYER).amount == 25f,
            observed(DamageInfo(25f, HitZone.HEAD, DamageType.BULLET, "alpha", Team.PLAYER).zone, HitZone.HEAD)
        )
        r.check(
            "Damageable contract", "a live combatant accepts a hit and reports health loss",
            Combatant("alpha", Team.ENEMY, 100f)
                .receiveDamage(DamageInfo(10f, HitZone.TORSO, DamageType.BULLET, "bravo", Team.PLAYER)),
            observed(Combatant("alpha", Team.ENEMY, 100f).current, 100f)
        )
        r.check(
            "Event bus names", "the twelve documented events plus player death",
            PlanEventType.entries.map { it.displayName } == listOf(
                "OnShotFired", "OnHit", "OnDamageDealt", "OnEntityKilled",
                "OnReloadStarted", "OnReloadFinished", "OnWeaponSwapped",
                "OnObjectiveCompleted", "OnMissionStateChanged", "OnXPGained",
                "OnRankUp", "OnCurrencyChanged", "OnPlayerDied"
            ),
            observed(PlanEventType.entries.size, 13)
        )
        val bus = EventBus()
        bus.emit(PlanEventType.SHOT_FIRED, "alpha")
        bus.emit(PlanEventType.ENTITY_KILLED, "bravo")
        r.check(
            "Event bus dispatch", "a listener sees one event of each published type",
            bus.count(PlanEventType.SHOT_FIRED) == 1 &&
                bus.count(PlanEventType.ENTITY_KILLED) == 1 &&
                bus.all().size == 2,
            observed(bus.all().size, 2)
        )
        val log = DebugLog()
        LogCategory.entries.forEach { log.record(it, "probe") }
        r.check(
            "Log categories", "nine [Tag] categories each keep their reason",
            LogCategory.entries.size == 9 &&
                LogCategory.entries.all { log.last(it)?.reason == "probe" } &&
                DebugTools.LOG_CATEGORIES.size == 9,
            observed(LogCategory.entries.map { it.tag }, DebugTools.LOG_CATEGORIES)
        )
        r.check(
            "Log disabled", "a release log records nothing",
            DebugLog(enabled = false).also { it.record(LogCategory.FIRE, "x") }.all().isEmpty(),
            observed(DebugLog(enabled = false).all().size, 0)
        )
        r.check(
            "Frame rules", "input in Update, physics in FixedUpdate, camera in LateUpdate",
            FrameRules.INPUT_IN_UPDATE &&
                FrameRules.PHYSICS_IN_FIXED_UPDATE &&
                FrameRules.CAMERA_IN_LATE_UPDATE &&
                FrameRules.NO_FIND_IN_UPDATE &&
                FrameRules.POOLED_KINDS.size == 4,
            observed(FrameRules.POOLED_KINDS, 4)
        )
        r.check(
            "Vector angle", "a 45 degree offset reads as 45 degrees",
            approxEquals(Vec3(1f, 0f, 1f).angleDegreesTo(Vec3.FORWARD), 45f, 0.01f),
            observed(Vec3(1f, 0f, 1f).angleDegreesTo(Vec3.FORWARD), 45f)
        )
        r.check(
            "Normalise zero", "a zero vector normalises to zero instead of NaN",
            Vec3.ZERO.normalized() == Vec3.ZERO &&
                approxEquals(Vec3(3f, 4f, 0f).length, 5f),
            observed(Vec3(3f, 4f, 0f).length, 5f)
        )
        return r.result()
    }
}
