package com.example.game.valor

import com.example.game.valor.art.AnimationLibrary
import com.example.game.valor.art.EnemyClip
import com.example.game.valor.art.LocomotionClip
import com.example.game.valor.art.TraversalClip
import com.example.game.valor.art.VfxEffect
import com.example.game.valor.art.VfxLibrary
import com.example.game.valor.art.WeaponClass
import com.example.game.vanguard.CaseRecorder
import com.example.game.vanguard.ContractCase
import com.example.game.vanguard.approxEquals
import com.example.game.vanguard.observed

/** Sections 6 and 7: the animation list and the VFX budgets. */
internal object ValorMotionContractCases {

    fun run(): List<ContractCase> = animationCases() + vfxCases()

    private fun animationCases(): List<ContractCase> {
        val recorder = CaseRecorder(ValorBibleAudit.GROUP_ANIMATION)

        recorder.check(
            "Locomotion tree", "nine locomotion clips including prone and turn-in-place",
            LocomotionClip.entries.size == 9, observed(LocomotionClip.entries.size, 9)
        )
        recorder.check(
            "Traversal", "jump, two vaults, ladder and cover slide",
            TraversalClip.entries.size == 5, observed(TraversalClip.entries.size, 5)
        )
        recorder.check(
            "Weapon sets", "seven classes x eight clips = 56",
            WeaponClass.entries.size == 7 && AnimationLibrary.weaponClipCount() == 56,
            observed(AnimationLibrary.weaponClipCount(), 56)
        )
        recorder.check(
            "Melee, grenade, utility", "4 melee, 3 grenade and 7 utility clips",
            AnimationLibrary.MELEE_CLIPS.size == 4 && AnimationLibrary.GRENADE_CLIPS.size == 3 &&
                AnimationLibrary.UTILITY_CLIPS.size == 7,
            observed(AnimationLibrary.UTILITY_CLIPS.size, 7)
        )
        recorder.check(
            "Death set", "six death variants with ragdoll and downed crawl",
            AnimationLibrary.DEATH_VARIANTS == 6 && AnimationLibrary.RAGDOLL_TRANSITION &&
                AnimationLibrary.DOWNED_CRAWL,
            observed(AnimationLibrary.DEATH_VARIANTS, 6)
        )
        recorder.check(
            "Enemy set", "nineteen enemy clips",
            EnemyClip.entries.size == 19 && AnimationLibrary.enemyClipCount() == 19,
            observed(EnemyClip.entries.size, 19)
        )
        recorder.check(
            "Root motion", "controller-driven except takedowns and vaults",
            !AnimationLibrary.ROOT_MOTION_GAMEPLAY && AnimationLibrary.keepsRootMotion("vault_low") &&
                AnimationLibrary.keepsRootMotion("stealth takedown") &&
                !AnimationLibrary.keepsRootMotion("run"),
            observed(AnimationLibrary.keepsRootMotion("run"), false)
        )
        recorder.check(
            "Animation events", "footstep, reload steps, shell eject, melee hit and throw release",
            AnimationLibrary.ANIMATION_EVENTS.size == 5,
            observed(AnimationLibrary.ANIMATION_EVENTS.size, 5)
        )
        return recorder.result()
    }

    private fun vfxCases(): List<ContractCase> {
        val recorder = CaseRecorder(ValorBibleAudit.GROUP_VFX)

        recorder.check(
            "VFX table", "twelve effects",
            VfxEffect.entries.size == 12, observed(VfxEffect.entries.size, 12)
        )
        recorder.check(
            "Weapon effects", "muzzle 12, explosion 120, drone strike 150",
            VfxEffect.MUZZLE_FLASH.particleBudget == 12 &&
                VfxEffect.EXPLOSION.particleBudget == 120 &&
                VfxEffect.DRONE_STRIKE.particleBudget == 150,
            observed(VfxEffect.DRONE_STRIKE.particleBudget, 150)
        )
        recorder.check(
            "Smoke, fire, ambient, blood", "100, 60, 30 and 15",
            VfxEffect.SMOKE_GRENADE.particleBudget == 100 && VfxEffect.FIRE.particleBudget == 60 &&
                VfxEffect.AMBIENT.particleBudget == 30 && VfxEffect.BLOOD.particleBudget == 15,
            observed(VfxEffect.SMOKE_GRENADE.particleBudget, 100)
        )
        recorder.check(
            "Impacts and decals", "20 impact particles with a 60 decal pool",
            VfxEffect.BULLET_IMPACTS.particleBudget == 20 && VfxLibrary.DECAL_POOL == 60,
            observed(VfxLibrary.DECAL_POOL, 60)
        )
        recorder.check(
            "Shells and tracers", "30 casings, 40 tracers, one tracer every third round",
            VfxEffect.SHELL_EJECTION.poolSize == 30 && VfxEffect.TRACERS.poolSize == 40 &&
                VfxLibrary.TRACER_EVERY_NTH_ROUND == 3,
            observed(VfxEffect.TRACERS.poolSize, 40)
        )
        recorder.check(
            "Pooling and shadows", "all pooled, LOD by distance, no particle shadows",
            VfxLibrary.ALL_POOLED && VfxLibrary.LOD_BY_DISTANCE && !VfxLibrary.SHADOWS_ON_PARTICLES &&
                VfxLibrary.GPU_INSTANCING,
            observed(VfxLibrary.SHADOWS_ON_PARTICLES, false)
        )
        recorder.check(
            "Weather and violence", "preset-scaled weather, blood obeys reduced violence",
            VfxEffect.WEATHER.isPresetScaled && VfxLibrary.fixedBudget(VfxEffect.WEATHER) == 0 &&
                !VfxLibrary.bloodEnabled(true) && VfxLibrary.bloodEnabled(false) &&
                approxEquals(VfxLibrary.muzzleFlashScale(true), 0.5f),
            observed(VfxLibrary.bloodEnabled(true), false)
        )
        recorder.check(
            "Hit marker", "UI-only effect carries no particles",
            VfxEffect.HIT_MARKER.isUiOnly && VfxEffect.HIT_MARKER.particleBudget == 0,
            observed(VfxEffect.HIT_MARKER.particleBudget, 0)
        )
        return recorder.result()
    }
}
