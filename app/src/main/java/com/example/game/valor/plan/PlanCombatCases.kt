package com.example.game.valor.plan

import com.example.game.valor.balance.FriendlyFirePolicy
import com.example.game.vanguard.CaseRecorder
import com.example.game.vanguard.ContractCase
import com.example.game.vanguard.approxEquals
import com.example.game.vanguard.observed

/** Sections 5.1 to 5.3: aiming, the firing pipeline and hit detection. */
object PlanCombatCases {

    fun run(): List<ContractCase> {
        val r = CaseRecorder(ValorPlanAudit.GROUP_COMBAT)
        aimCases(r)
        firingCases(r)
        hitCases(r)
        return r.result()
    }

    private fun aimCases(r: CaseRecorder) {
        r.check(
            "Assist off", "Off never nudges even in an ideal case",
            !AimRules.canAssist(AimAssistLevel.OFF, 1f, 6f, true, true, true),
            observed(AimAssistLevel.OFF, AimAssistLevel.LOW)
        )
        r.check(
            "Assist angle", "only a target inside assistAngle is nudged",
            AimRules.canAssist(AimAssistLevel.LOW, 5f, 6f, true, true, true) &&
                !AimRules.canAssist(AimAssistLevel.LOW, 7f, 6f, true, true, true),
            observed(7f < 6f, false)
        )
        r.check(
            "Assist needs sight", "a target behind a wall is never assisted",
            !AimRules.canAssist(AimAssistLevel.HIGH, 1f, 6f, false, true, true),
            observed("visible=false", false)
        )
        r.check(
            "Assist never damages", "aim assist cannot damage, fire or reach through walls",
            !AimRules.AIM_ASSIST_DEALS_DAMAGE &&
                !AimRules.AIM_ASSIST_AUTO_FIRES &&
                !AimRules.AIM_ASSIST_HITS_THROUGH_WALLS &&
                !AimRules.AIM_ASSIST_LOCKS_ON_DEAD &&
                AimRules.CROSSHAIR_IS_VISUAL_ONLY,
            observed(AimRules.AIM_ASSIST_DEALS_DAMAGE, false)
        )
        r.check(
            "Sniper full zoom", "assist is off at full zoom unless the player opts in",
            !AimRules.assistsSniper(AimAssistLevel.HIGH, true, false) &&
                AimRules.assistsSniper(AimAssistLevel.HIGH, true, true) &&
                AimRules.assistsSniper(AimAssistLevel.HIGH, false, false),
            observed(AimRules.assistsSniper(AimAssistLevel.HIGH, true, false), false)
        )
        r.check(
            "Crosshair tint", "red on an enemy hitbox, green on a teammate",
            AimRules.crosshairTint(true, false) == CrosshairTint.ENEMY &&
                AimRules.crosshairTint(false, true) == CrosshairTint.ALLY,
            observed(AimRules.crosshairTint(true, false), CrosshairTint.ENEMY)
        )
        r.check(
            "ADS cancels sprint", "aiming down sights ends a sprint",
            AimRules.beginAds(AdsState(sprinting = true)).let { it.aiming && !it.sprinting },
            observed(AimRules.beginAds(AdsState(sprinting = true)).sprinting, false)
        )
        r.check(
            "ADS cancels", "reload, sprint, swap, death and vault all drop the aim",
            AdsCancel.entries.size == 5 &&
                AdsCancel.entries.all { !AimRules.cancelAds(AdsState(aiming = true), it).aiming },
            observed(AdsCancel.entries.size, 5)
        )
        r.check(
            "ADS spread and FOV", "spread drops to adsSpreadMult and FOV lerps",
            approxEquals(AimRules.spreadMultiplier(AdsState(aiming = true), 0.4f), 0.4f) &&
                approxEquals(AimRules.fov(90f, 60f, 0.5f), 75f),
            observed(AimRules.fov(90f, 60f, 0.5f), 75f)
        )
        r.check(
            "Sway stance", "crouched sways less than standing, moving sways more",
            AimRules.swaysLess(
                AimRules.swayRank(SwayStance.CROUCHED, moving = false),
                AimRules.swayRank(SwayStance.STANDING, moving = false)
            ) &&
                AimRules.swaysLess(
                    AimRules.swayRank(SwayStance.STANDING, moving = false),
                    AimRules.swayRank(SwayStance.STANDING, moving = true)
                ),
            observed(AimRules.swayRank(SwayStance.CROUCHED, false), 1)
        )
    }

    private fun firingCases(r: CaseRecorder) {
        r.check(
            "Eight checks", "the pipeline runs eight ordered checks",
            FireRejection.entries.size == 8 &&
                FireRejection.entries.first() == FireRejection.NOT_ALIVE &&
                FireRejection.entries.last() == FireRejection.JAMMED,
            observed(FireRejection.entries.size, 8)
        )
        r.check(
            "First blocker wins", "a dead owner is reported before the reload",
            FiringPipeline.evaluate(FireContext(ownerAlive = false, reloading = true)).rejection ==
                FireRejection.NOT_ALIVE,
            observed(
                FiringPipeline.evaluate(FireContext(ownerAlive = false, reloading = true)).rejection,
                FireRejection.NOT_ALIVE
            )
        )
        val empty = FiringPipeline.evaluate(FireContext(magazine = 0))
        r.check(
            "Empty magazine", "a dry click, an auto-reload attempt and no shot",
            empty.rejection == FireRejection.EMPTY_MAGAZINE &&
                empty.dryClick && empty.autoReload && empty.magazineAfter == 0 &&
                empty.roundsConsumed == 0,
            observed(empty, FireRejection.EMPTY_MAGAZINE)
        )
        r.check(
            "Fire rate", "nextFireTime = now + 60/RPM",
            approxEquals(FiringPipeline.nextFireTime(1f, 600f), 1.1f),
            observed(FiringPipeline.nextFireTime(1f, 600f), 1.1f)
        )
        r.check(
            "Single needs a fresh press", "a held trigger does not re-fire a single-shot",
            FiringPipeline.evaluate(
                FireContext(fireMode = FireMode.SINGLE, freshPress = false, held = true)
            ).rejection == FireRejection.FIRE_MODE,
            observed(
                FiringPipeline.evaluate(FireContext(fireMode = FireMode.SINGLE, freshPress = false)).rejection,
                FireRejection.FIRE_MODE
            )
        )
        val fired = FiringPipeline.evaluate(FireContext(magazine = 30, rpm = 600f))
        r.check(
            "Shot consumes one round", "one round leaves the magazine and the cooldown starts",
            fired.allowed && fired.magazineAfter == 29 && fired.roundsConsumed == 1 &&
                approxEquals(fired.nextFireTime, 0.1f),
            observed(fired, "allowed, 29 rounds")
        )
        val blocked = FiringPipeline.evaluate(FireContext(magazine = 17, jammed = true))
        r.check(
            "Rejection costs nothing", "a jammed weapon keeps its magazine",
            !blocked.allowed && blocked.magazineAfter == 17 && blocked.roundsConsumed == 0,
            observed(blocked.magazineAfter, 17)
        )
        r.check(
            "Same frame race", "reload wins only when the magazine is empty",
            FiringPipeline.sameFrameRace(0) == InputRace.RELOAD &&
                FiringPipeline.sameFrameRace(12) == InputRace.FIRE,
            observed(FiringPipeline.sameFrameRace(0), InputRace.RELOAD)
        )
        r.check(
            "Shell reload interruptible", "firing interrupts a shell-by-shell reload",
            FiringPipeline.evaluate(FireContext(reloading = true, shellReloadInterruptible = true)).allowed &&
                !FiringPipeline.evaluate(FireContext(reloading = true)).allowed,
            observed(FiringPipeline.evaluate(FireContext(reloading = true)).rejection, FireRejection.RELOADING)
        )
        r.check(
            "Suppressed noise", "a suppressor cuts the noise radius to 0.3",
            approxEquals(FiringPipeline.noiseRadius(30f, FiringPipeline.SUPPRESSED_NOISE_MULT), 9f),
            observed(FiringPipeline.noiseRadius(30f, 0.3f), 9f)
        )
        r.check(
            "Burst stops on death", "a paused burst resumes and a dead owner cancels it",
            FiringPipeline.stopBurst(BurstState(2)).bulletsRemaining == 0 &&
                FiringPipeline.pauseBurst(BurstState(2)).bulletsRemaining == 2,
            observed(FiringPipeline.stopBurst(BurstState(2)).bulletsRemaining, 0)
        )
    }

    private fun hitCases(r: CaseRecorder) {
        val rifle = RayWeapon(maxRangeMeters = 100f, penetrationPower = 0f, penetrationFalloff = 1f)

        r.check(
            "Wall blocks", "the first material stops the bullet",
            HitDetection.resolve(
                "alpha", Team.PLAYER,
                listOf(RaySample(5f, null, PhysicsLayer.ENVIRONMENT)), rifle, FriendlyFirePolicy.OFF
            ).outcome == HitOutcome.BLOCKED,
            observed(
                HitDetection.resolve(
                    "alpha", Team.PLAYER,
                    listOf(RaySample(5f, null, PhysicsLayer.ENVIRONMENT)), rifle, FriendlyFirePolicy.OFF
                ).outcome,
                HitOutcome.BLOCKED
            )
        )
        r.check(
            "Open air miss", "no sample means no hit and no damage",
            HitDetection.resolve("alpha", Team.PLAYER, emptyList(), rifle, FriendlyFirePolicy.OFF)
                .let { it.outcome == HitOutcome.NO_HIT && !it.dealsDamage },
            observed(HitOutcome.NO_HIT, HitOutcome.NO_HIT)
        )
        r.check(
            "Dead body", "a corpse absorbs nothing and takes nothing",
            HitDetection.resolve(
                "alpha", Team.PLAYER,
                listOf(RaySample(8f, "enemy1", PhysicsLayer.ENEMY, Team.ENEMY, alive = false)),
                rifle, FriendlyFirePolicy.OFF
            ).let { it.outcome == HitOutcome.IGNORED_DEAD && !it.dealsDamage },
            observed(HitOutcome.IGNORED_DEAD, HitOutcome.IGNORED_DEAD)
        )
        r.check(
            "Friendly blocked", "an ally on a normal playthrough takes no damage",
            HitDetection.resolve(
                "alpha", Team.PLAYER,
                listOf(RaySample(4f, "ally1", PhysicsLayer.ALLY, Team.ALLY)),
                rifle, FriendlyFirePolicy.OFF_FOR_PLAYER_TO_ALLY
            ).let { it.outcome == HitOutcome.FRIENDLY_BLOCKED && !it.dealsDamage },
            observed(HitOutcome.FRIENDLY_BLOCKED, HitOutcome.FRIENDLY_BLOCKED)
        )
        r.check(
            "Friendly damage", "a hard playthrough lets the ally take the hit",
            HitDetection.resolve(
                "alpha", Team.PLAYER,
                listOf(RaySample(4f, "ally1", PhysicsLayer.ALLY, Team.ALLY)),
                rifle, FriendlyFirePolicy.ON
            ).let { it.outcome == HitOutcome.FRIENDLY_DAMAGE && it.dealsDamage },
            observed(HitOutcome.FRIENDLY_DAMAGE, HitOutcome.FRIENDLY_DAMAGE)
        )
        r.check(
            "One target per ray", "the nearest hostile is the only one damaged",
            HitDetection.resolve(
                "alpha", Team.PLAYER,
                listOf(
                    RaySample(4f, "enemy1", PhysicsLayer.ENEMY, Team.ENEMY),
                    RaySample(9f, "enemy2", PhysicsLayer.ENEMY, Team.ENEMY)
                ),
                rifle, FriendlyFirePolicy.OFF
            ).let { it.outcome == HitOutcome.DAMAGED && it.targetId == "enemy1" },
            observed("enemy1", "enemy1")
        )
        r.check(
            "Penetration falloff", "a thin wall passes the bullet at the falloff multiplier",
            HitDetection.resolve(
                "alpha", Team.PLAYER,
                listOf(
                    RaySample(3f, null, PhysicsLayer.GLASS, penetrable = true, thicknessMeters = 0.1f),
                    RaySample(6f, "enemy1", PhysicsLayer.ENEMY, Team.ENEMY)
                ),
                RayWeapon(100f, 1f, 0.8f), FriendlyFirePolicy.OFF
            ).let { it.outcome == HitOutcome.DAMAGED && approxEquals(it.damageMultiplier, 0.8f) },
            observed("0.8", "damage multiplier")
        )
        r.check(
            "Out of range", "a sample beyond max range is a miss",
            HitDetection.resolve(
                "alpha", Team.PLAYER,
                listOf(RaySample(150f, "enemy1", PhysicsLayer.ENEMY, Team.ENEMY)),
                rifle, FriendlyFirePolicy.OFF
            ).outcome == HitOutcome.NO_HIT,
            observed(HitOutcome.NO_HIT, HitOutcome.NO_HIT)
        )
        val aimPoint = HitDetection.aimPoint(Vec3.ZERO, Vec3.FORWARD, 10f)
        r.check(
            "Convergence", "the bullet leaves the muzzle toward the camera aim point",
            aimPoint == Vec3(0f, 0f, 10f) &&
                HitDetection.convergenceDirection(Vec3.ZERO, aimPoint) == Vec3.FORWARD &&
                approxEquals(HitDetection.aimDistance(Vec3.ZERO, aimPoint), 10f),
            observed(aimPoint, Vec3(0f, 0f, 10f))
        )
        r.check(
            "Muzzle inside wall", "a blocker before the aim point takes the hit",
            HitDetection.blockedBeforeAimPoint(2f, 10f) &&
                !HitDetection.blockedBeforeAimPoint(10f, 10f),
            observed(HitDetection.blockedBeforeAimPoint(2f, 10f), true)
        )
        r.check(
            "Shotgun pellets", "each pellet resolves on its own path",
            HitDetection.resolveVolley(
                "alpha", Team.PLAYER,
                listOf(
                    listOf(RaySample(5f, "enemy1", PhysicsLayer.ENEMY, Team.ENEMY)),
                    listOf(RaySample(60f, "enemy2", PhysicsLayer.ENEMY, Team.ENEMY))
                ),
                RayWeapon(50f, 0f, 1f), FriendlyFirePolicy.OFF
            ).map { it.outcome } == listOf(HitOutcome.DAMAGED, HitOutcome.NO_HIT),
            observed("DAMAGED, NO_HIT", "independent pellets")
        )
    }
}
