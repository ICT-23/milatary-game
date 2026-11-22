package com.example.game.tactical

import com.example.game.combat.Vec3
import com.example.game.tactical.combat.AmmoProfile
import com.example.game.tactical.combat.BallisticResolver
import com.example.game.tactical.combat.DamageModel
import com.example.game.tactical.combat.FireDenial
import com.example.game.tactical.combat.HitboxComponent
import com.example.game.tactical.combat.HitboxTarget
import com.example.game.tactical.combat.MuzzleTransform
import com.example.game.tactical.combat.Obstacle
import com.example.game.tactical.combat.ShotRequest
import com.example.game.tactical.combat.TargetProfile
import com.example.game.tactical.combat.WeaponController
import com.example.game.tactical.gunsmith.AssemblyPart
import com.example.game.tactical.gunsmith.AssembledWeapon
import com.example.game.tactical.gunsmith.AttachmentCatalog
import com.example.game.tactical.gunsmith.BlueprintSource
import com.example.game.tactical.gunsmith.CraftCost
import com.example.game.tactical.gunsmith.StockRack
import com.example.game.tactical.gunsmith.WeaponBase
import com.example.game.tactical.gunsmith.WeaponBlueprint
import com.example.game.tactical.gunsmith.Workbench
import com.example.game.vanguard.CaseRecorder
import com.example.game.vanguard.ContractCase
import com.example.game.vanguard.approxEquals
import com.example.game.vanguard.core.valueOrNull
import com.example.game.vanguard.observed
import kotlin.random.Random

/** Sections 5.1 and Phase 3: the damage equation, ballistics and the weapon controller. */
internal object TacticalCombatContractCases {

    private val testBase = WeaponBase(
        id = "AR-4",
        name = "AR-4 Sentinel",
        baseRecoil = 2f,
        baseAdsMs = 300f,
        baseDamage = 34f,
        effectiveRangeMeters = 100f,
        maxRangeMeters = 300f,
        fireRateRpm = 750f,
        mobility = 1f
    )

    private val testBlueprint = WeaponBlueprint(
        id = "BP-AR4",
        name = "AR-4 Sentinel",
        unlockRankIndex = 0,
        source = BlueprintSource.MISSION,
        cost = CraftCost(0, 0, 0, 0)
    )

    private fun rifle(): AssembledWeapon = Workbench.assemble(
        testBlueprint,
        testBase,
        AssemblyPart.entries.toSet(),
        StockRack.STANDARD,
        AttachmentCatalog.DEFAULT
    ).valueOrNull()!!

    fun run(): List<ContractCase> = damageCases() + ballisticsCases() + weaponCases()

    private fun damageCases(): List<ContractCase> {
        val recorder = CaseRecorder(TacticalContractAudit.GROUP_COMBAT)

        val multipliers = HitboxComponent.entries.map { it.multiplier }
        recorder.check(
            "Hitbox multipliers", "Head 2.5 / Thorax 1.2 / Stomach 1.0 / Limbs 0.75",
            multipliers == listOf(2.5f, 1.2f, 1.0f, 0.75f),
            observed(multipliers, "2.5/1.2/1.0/0.75")
        )
        recorder.check(
            "Damage dropoff curve", "1.0 inside range, 0.7 at mid, 0.4 at max",
            DamageModel.dropoff(50f, 100f, 200f) == 1f &&
                approxEquals(DamageModel.dropoff(150f, 100f, 200f), 0.7f) &&
                approxEquals(DamageModel.dropoff(250f, 100f, 200f), 0.4f),
            observed(DamageModel.dropoff(150f, 100f, 200f), 0.7f)
        )
        val thorax = DamageModel.resolve(
            baseDamage = 100f,
            hitbox = HitboxComponent.THORAX,
            distanceMeters = 50f,
            effectiveRangeMeters = 100f,
            maxRangeMeters = 200f,
            target = TargetProfile(),
            ammo = AmmoProfile.FMJ
        )
        recorder.check(
            "D_final equation", "100 x 1.2 x 1.0 x 1.0 = 120",
            approxEquals(thorax.finalDamage, 120f),
            observed(thorax.finalDamage, 120f)
        )
        recorder.check(
            "Head instant kill", "unhelmeted dies, helmeted survives, sniper defeats helmet",
            DamageModel.isInstantKill(HitboxComponent.HEAD, TargetProfile(), false) &&
                !DamageModel.isInstantKill(HitboxComponent.HEAD, TargetProfile(isHelmeted = true), false) &&
                DamageModel.isInstantKill(HitboxComponent.HEAD, TargetProfile(isHelmeted = true), true),
            observed(
                DamageModel.isInstantKill(HitboxComponent.HEAD, TargetProfile(isHelmeted = true), false),
                false
            )
        )
        val apRound = DamageModel.resolve(
            baseDamage = 100f,
            hitbox = HitboxComponent.THORAX,
            distanceMeters = 50f,
            effectiveRangeMeters = 100f,
            maxRangeMeters = 200f,
            target = TargetProfile(isArmored = true, armorReductionPercent = 0.5f),
            ammo = AmmoProfile.ARMOR_PIERCING
        )
        recorder.check(
            "Armor piercing", "ignores half of a 50% plate: 90 damage",
            approxEquals(apRound.finalDamage, 90f) &&
                approxEquals(apRound.effectiveArmorReduction, 0.25f),
            observed(apRound.finalDamage, 90f)
        )
        val hollow = DamageModel.resolve(
            baseDamage = 100f,
            hitbox = HitboxComponent.THORAX,
            distanceMeters = 50f,
            effectiveRangeMeters = 100f,
            maxRangeMeters = 200f,
            target = TargetProfile(),
            ammo = AmmoProfile.HOLLOW_POINT
        )
        recorder.check(
            "Hollow point", "+25% on an unarmored thorax: 150 damage",
            approxEquals(hollow.finalDamage, 150f),
            observed(hollow.finalDamage, 150f)
        )
        recorder.check(
            "Limb penalty", "limbs carry aim-sway and movement penalties",
            approxEquals(thorax.aimSwayPenalty, 0f) &&
                DamageModel.resolve(
                    baseDamage = 100f,
                    hitbox = HitboxComponent.LIMBS,
                    distanceMeters = 50f,
                    effectiveRangeMeters = 100f,
                    maxRangeMeters = 200f,
                    target = TargetProfile(),
                    ammo = AmmoProfile.FMJ
                ).let { limbed ->
                    approxEquals(limbed.finalDamage, 75f) && limbed.aimSwayPenalty > 0f
                },
            observed(DamageModel.LIMB_AIM_SWAY_PENALTY, 0.25f)
        )
        return recorder.result()
    }

    private fun ballisticsCases(): List<ContractCase> {
        val recorder = CaseRecorder(TacticalContractAudit.GROUP_COMBAT)

        recorder.check(
            "Gravity drop", "400 m at 880 m/s drops 1.013 m",
            approxEquals(BallisticResolver.dropMeters(400f, 880f), 1.0134f, 0.001f),
            observed(BallisticResolver.dropMeters(400f, 880f), 1.0134f)
        )
        recorder.check(
            "Hip cone", "30% laser bonus narrows 4 deg to 2.8 deg",
            approxEquals(BallisticResolver.effectiveHipCone(4f, 30), 2.8f),
            observed(BallisticResolver.effectiveHipCone(4f, 30), 2.8f)
        )
        val target = HitboxTarget(
            bodyId = "dummy",
            center = Vec3(0f, 0f, -50f),
            radius = 0.4f,
            component = HitboxComponent.THORAX
        )
        val request = ShotRequest(
            muzzle = MuzzleTransform(Vec3.Zero, Vec3(0f, 0f, -1f)),
            aimPoint = target.center,
            isAimingDownSights = true,
            bulletDropEnabled = false
        )
        val hit = BallisticResolver.resolve(request, listOf(target), emptyList(), Random(7))
        recorder.check(
            "Raycast from the muzzle", "thorax hit at 50 m",
            hit.didHit && hit.hitTarget?.component == HitboxComponent.THORAX,
            observed(hit.hitTarget?.component, HitboxComponent.THORAX)
        )
        val wall = Obstacle("WALL", Vec3(0f, 0f, -20f), 1f)
        val blocked = BallisticResolver.resolve(request, listOf(target), listOf(wall), Random(7))
        recorder.check(
            "Solid cover stops the round", "blocked by WALL, no damage",
            blocked.isBlocked && blocked.blockedBy == "WALL" && !blocked.didHit,
            observed(blocked.blockedBy, "WALL")
        )
        val through = BallisticResolver.resolve(
            request,
            listOf(target),
            listOf(wall.copy(penetrable = true, material = "WOOD")),
            Random(7)
        )
        recorder.check(
            "Penetrable cover", "round passes through and still hits",
            through.penetratedObstacleId == "WALL" && through.didHit,
            observed(through.penetratedObstacleId, "WALL")
        )
        val damage = BallisticResolver.damageOf(hit, 30f, AmmoProfile.FMJ, 100f, 300f)
        recorder.check(
            "Resolved damage", "30 x 1.2 = 36 at 50 m",
            damage != null && approxEquals(damage.finalDamage, 36f),
            observed(damage?.finalDamage, 36f)
        )
        val offAxis = BallisticResolver.resolve(
            request.copy(aimPoint = Vec3(40f, 0f, 0f)),
            listOf(target),
            emptyList(),
            Random(7)
        )
        recorder.check(
            "Muzzle-origin vector", "an off-axis aim cannot hit a 40 cm target",
            !offAxis.didHit,
            observed(offAxis.didHit, false)
        )
        return recorder.result()
    }

    private fun weaponCases(): List<ContractCase> {
        val recorder = CaseRecorder(TacticalContractAudit.GROUP_COMBAT)

        val equipped = WeaponController.equip(rifle(), 90)
        val first = WeaponController.tryFire(equipped)
        recorder.check(
            "Trigger pull", "one round spent, 0.08 s cyclic cooldown",
            first.fired && first.state.loadedRounds == 29 &&
                approxEquals(first.state.cooldownSeconds, 0.08f),
            observed(first.state.loadedRounds, 29)
        )
        recorder.check(
            "Cyclic rate enforced", "second pull inside the cooldown is denied",
            WeaponController.tryFire(first.state).denial == FireDenial.COOLDOWN &&
                WeaponController.tryFire(WeaponController.tick(first.state, 0.08f)).fired,
            observed(WeaponController.tryFire(first.state).denial, FireDenial.COOLDOWN)
        )
        recorder.check(
            "Empty magazine", "no round is spent on a denied pull",
            WeaponController.tryFire(equipped.copy(loadedRounds = 0)).denial == FireDenial.EMPTY,
            observed(WeaponController.tryFire(equipped.copy(loadedRounds = 0)).denial, FireDenial.EMPTY)
        )
        val onSafe = WeaponController.toggleSafety(equipped)
        recorder.check(
            "Receiver safety", "safe blocks the trigger",
            WeaponController.tryFire(onSafe.copy(cooldownSeconds = 0f)).denial == FireDenial.ON_SAFE,
            observed(
                WeaponController.tryFire(onSafe.copy(cooldownSeconds = 0f)).denial,
                FireDenial.ON_SAFE
            )
        )
        val reloading = WeaponController.beginReload(
            equipped.copy(loadedRounds = 10, cooldownSeconds = 0f)
        )
        val reloaded = WeaponController.tick(reloading, WeaponController.BASE_RELOAD_SECONDS)
        recorder.check(
            "Reload state machine", "20 drawn from reserve after 2.2 s",
            reloading.isReloading && reloaded.loadedRounds == 30 && reloaded.reserveRounds == 70,
            observed(reloaded.loadedRounds, 30)
        )
        recorder.check(
            "Hip cone baseline", "no recoil means a 3.5 deg cone",
            approxEquals(equipped.spreadDegrees, WeaponController.BASE_HIP_CONE_DEGREES),
            observed(equipped.spreadDegrees, 3.5f)
        )
        recorder.check(
            "Debug ray toggle", "off by default",
            !WeaponController.DEBUG_RAY_DEFAULT,
            observed(WeaponController.DEBUG_RAY_DEFAULT, false)
        )
        return recorder.result()
    }
}
