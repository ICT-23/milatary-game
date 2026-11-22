package com.example.game.valor

import com.example.game.tactical.render.ImpactSurface
import com.example.game.valor.art.ActDirection
import com.example.game.valor.art.ArtAct
import com.example.game.valor.art.CameraBible
import com.example.game.valor.art.CharacterArt
import com.example.game.valor.art.CharacterAsset
import com.example.game.valor.art.CharacterRole
import com.example.game.valor.art.EnvironmentRules
import com.example.game.valor.art.ImportRules
import com.example.game.valor.art.LightingRules
import com.example.game.valor.art.ShakeEvent
import com.example.game.valor.art.SplitTone
import com.example.game.valor.art.SurfaceLibrary
import com.example.game.valor.art.SurfaceMaterial
import com.example.game.valor.art.VisualPillar
import com.example.game.valor.art.WeaponArt
import com.example.game.valor.art.WearState
import com.example.game.vanguard.CaseRecorder
import com.example.game.vanguard.ContractCase
import com.example.game.vanguard.approxEquals
import com.example.game.vanguard.observed

/** Sections 1 - 5 and 8: pillars, act grades, camera, characters, weapons and environment. */
internal object ValorArtContractCases {

    fun run(): List<ContractCase> =
        actCases() + characterCases() + weaponCases() + environmentCases()

    private fun actCases(): List<ContractCase> {
        val recorder = CaseRecorder(ValorBibleAudit.GROUP_PILLARS)

        recorder.check(
            "Visual pillars", "five pillars define the look",
            VisualPillar.entries.size == 5, observed(VisualPillar.entries.size, 5)
        )
        recorder.check(
            "Act count", "six acts numbered 1 to 6",
            ArtAct.entries.size == 6 && ArtAct.entries.map { it.number } == (1..6).toList(),
            observed(ArtAct.entries.map { it.number }, (1..6).toList())
        )
        recorder.check(
            "Act 2 grade", "desaturated 25% with sepia tint and film grain",
            approxEquals(ArtAct.GREAT_WAR.grade.saturation, 0.75f) &&
                ArtAct.GREAT_WAR.grade.sepia > 0f && ArtAct.GREAT_WAR.grade.grain > 0f,
            observed(ArtAct.GREAT_WAR.grade.saturation, 0.75f)
        )
        recorder.check(
            "Act 4 grade", "contrast +10% and slight bleach bypass",
            approxEquals(ArtAct.WORLD_AT_WAR.grade.contrast, 1.10f) && ArtAct.WORLD_AT_WAR.grade.bleachBypass,
            observed(ArtAct.WORLD_AT_WAR.grade.contrast, 1.10f)
        )
        recorder.check(
            "Act 5 grade", "strong day-night split with bloom on lights",
            ArtAct.COLD_SHADOWS.grade.bloom > 0f && ArtAct.COLD_SHADOWS.grade.splitTone == SplitTone.DAY_NIGHT,
            observed(ArtAct.COLD_SHADOWS.grade.bloom, "> 0")
        )
        recorder.check(
            "Act 6 grade", "clean high contrast with subtle chromatic aberration",
            approxEquals(ArtAct.DRONE_AGE.grade.contrast, 1.10f) && ArtAct.DRONE_AGE.grade.chromaticAberration > 0f,
            observed(ArtAct.DRONE_AGE.grade.chromaticAberration, "> 0")
        )
        recorder.check(
            "Era palettes", "every act names three or more colors and Olive resolves",
            ArtAct.entries.all { it.palette.size >= 3 } &&
                ActDirection.swatch(ArtAct.BOOT_CAMP, "Olive")?.name == "Olive",
            observed(ArtAct.entries.minOf { it.palette.size }, ">= 3")
        )
        recorder.check(
            "Mobile lighting", "one directional light, four dynamic, 0.05 s flash pulse",
            LightingRules.DIRECTIONAL_REALTIME_LIGHTS == 1 && LightingRules.clampDynamicLights(9) == 4 &&
                LightingRules.muzzlePulseFrames(0.01f) == 5,
            observed(LightingRules.clampDynamicLights(9), 4)
        )
        recorder.check(
            "Third-person camera", "0.6 m shoulder, 2.6 m distance, 60 degree FOV, mirrored swap",
            approxEquals(CameraBible.SHOULDER_OFFSET_METERS, 0.6f) &&
                approxEquals(CameraBible.THIRD_PERSON_DISTANCE_METERS, 2.6f) &&
                approxEquals(CameraBible.DEFAULT_FOV, 60f) &&
                approxEquals(CameraBible.swapShoulder(0.6f), -0.6f),
            observed(CameraBible.swapShoulder(0.6f), -0.6f)
        )
        recorder.check(
            "ADS FOV by optic", "1x keeps 60, 4x gives 15, 12x floors at 5",
            approxEquals(CameraBible.adsFov(1f), 60f) && approxEquals(CameraBible.adsFov(4f), 15f) &&
                approxEquals(CameraBible.adsFov(12f), 5f),
            observed(CameraBible.adsFov(12f), 5f)
        )
        recorder.check(
            "Scope vignette", "4x and 8x scopes add the overlay, 1x does not",
            approxEquals(CameraBible.scopeVignette(4f), 0.35f) &&
                approxEquals(CameraBible.scopeVignette(8f), 0.6f) &&
                approxEquals(CameraBible.scopeVignette(1f), 0f),
            observed(CameraBible.scopeVignette(8f), 0.6f)
        )
        recorder.check(
            "Kill cam and shake", "0.6 s at 0.3x, shake disabled zeroes every event",
            approxEquals(CameraBible.KILL_CAM_SECONDS, 0.6f) &&
                approxEquals(CameraBible.KILL_CAM_TIME_SCALE, 0.3f) &&
                approxEquals(CameraBible.shakeStrength(ShakeEvent.EXPLOSION, false), 0f) &&
                approxEquals(CameraBible.shakeStrength(ShakeEvent.EXPLOSION, true), 1f) &&
                approxEquals(CameraBible.shakeStrength(ShakeEvent.LANDING, true), 0.35f),
            observed(CameraBible.shakeStrength(ShakeEvent.EXPLOSION, false), 0f)
        )
        return recorder.result()
    }

    private fun characterCases(): List<ContractCase> {
        val recorder = CaseRecorder(ValorBibleAudit.GROUP_CHARACTERS)

        recorder.check(
            "Character roster", "eight roles and civilians are never targetable",
            CharacterRole.entries.size == 8 && !CharacterRole.CIVILIAN.targetable &&
                CharacterRole.entries.count { it.targetable } == 7,
            observed(CharacterRole.entries.size, 8)
        )
        recorder.check(
            "Player creator", "2 body types x 8 faces x 6 hair = 96 combinations",
            CharacterArt.playerFaceCombinations() == 96, observed(CharacterArt.playerFaceCombinations(), 96)
        )
        recorder.check(
            "Rig and insignia", "65 bones maximum and 20 rank badges",
            CharacterArt.RIG_MAX_BONES == 65 && CharacterArt.RANK_BADGES == 20,
            observed(CharacterArt.RIG_MAX_BONES, 65)
        )
        recorder.check(
            "LOD budgets", "every character asset loses triangles down the chain",
            CharacterAsset.entries.all { CharacterArt.withinBudget(it) },
            observed(CharacterAsset.entries.count { CharacterArt.withinBudget(it) }, CharacterAsset.entries.size)
        )
        recorder.check(
            "Hero and boss budgets", "hero 18k/9k/4k, boss 25k/12k/6k",
            CharacterAsset.HERO_PLAYER.lod0Triangles == 18_000 &&
                CharacterAsset.HERO_PLAYER.trianglesAt(2) == 4_000 &&
                CharacterAsset.BOSS.lod0Triangles == 25_000 &&
                CharacterAsset.BOSS.trianglesAt(1) == 12_000,
            observed(CharacterAsset.HERO_PLAYER.trianglesAt(2), 4_000)
        )
        recorder.check(
            "First-person arms", "8k, separate rig, single LOD",
            CharacterAsset.FIRST_PERSON_ARMS.lod0Triangles == 8_000 &&
                !CharacterAsset.FIRST_PERSON_ARMS.hasLods,
            observed(CharacterAsset.FIRST_PERSON_ARMS.hasLods, false)
        )
        recorder.check(
            "Gear visibility", "helmet, vest and backpack are separate meshes",
            CharacterArt.gearLayers() == listOf("Helmet", "Vest", "Backpack"),
            observed(CharacterArt.gearLayers().size, 3)
        )
        recorder.check(
            "Enemy and civilian variants", "3-4 enemy variants and 6 civilians",
            CharacterArt.enemyVariantRange() == 3..4 && CharacterArt.CIVILIAN_VARIANTS == 6,
            observed(CharacterArt.enemyVariantRange(), 3..4)
        )
        return recorder.result()
    }

    private fun weaponCases(): List<ContractCase> {
        val recorder = CaseRecorder(ValorBibleAudit.GROUP_WEAPONS)

        recorder.check(
            "First-person budget", "15k-30k inclusive, outside refused",
            WeaponArt.isFirstPersonWithinBudget(15_000) && WeaponArt.isFirstPersonWithinBudget(30_000) &&
                !WeaponArt.isFirstPersonWithinBudget(14_999) && !WeaponArt.isFirstPersonWithinBudget(30_001),
            observed(WeaponArt.isFirstPersonWithinBudget(30_001), false)
        )
        recorder.check(
            "World weapon budget", "3k-6k inclusive, outside refused",
            WeaponArt.isWorldWithinBudget(3_000) && WeaponArt.isWorldWithinBudget(6_000) &&
                !WeaponArt.isWorldWithinBudget(2_999) && !WeaponArt.isWorldWithinBudget(6_001),
            observed(WeaponArt.isWorldWithinBudget(6_001), false)
        )
        recorder.check(
            "Modular parts", "eight separate weapon parts",
            WeaponArt.SEPARATE_PARTS.size == 8, observed(WeaponArt.SEPARATE_PARTS.size, 8)
        )
        recorder.check(
            "Attachment sockets", "six sockets plus muzzle FX and eject on every weapon",
            WeaponArt.ATTACHMENT_SOCKETS.size == 6 && WeaponArt.allSockets().size == 8 &&
                "Socket_Muzzle_FX" in WeaponArt.allSockets() && "Socket_Eject" in WeaponArt.allSockets(),
            observed(WeaponArt.allSockets().size, 8)
        )
        recorder.check(
            "Asset naming", "WPN_<Class>_<Name>_LOD<n> and T_WPN_<Name>_<Map>",
            WeaponArt.meshName("rifle", "M4A1", 0) == "WPN_RIFLE_M4A1_LOD0" &&
                WeaponArt.textureName("M4A1", "Albedo") == "T_WPN_M4A1_Albedo",
            observed(WeaponArt.meshName("rifle", "M4A1", 0), "WPN_RIFLE_M4A1_LOD0")
        )
        recorder.check(
            "Wear states", "clean, worn and rusted map to modern, cold war and WW1",
            WearState.entries.size == 3 &&
                WearState.entries.map { it.era } == listOf("Modern", "Cold War", "WW1"),
            observed(WearState.entries.map { it.era }, listOf("Modern", "Cold War", "WW1"))
        )
        recorder.check(
            "Legal designs", "brand marks are refused",
            WeaponArt.isShippingSafe("M4A1") && !WeaponArt.isShippingSafe("M4A1\u00AE"),
            observed(WeaponArt.isShippingSafe("M4A1"), true)
        )
        return recorder.result()
    }

    private fun environmentCases(): List<ContractCase> {
        val recorder = CaseRecorder(ValorBibleAudit.GROUP_ENVIRONMENT)

        recorder.check(
            "Surface table", "twelve surfaces drive FX, audio and penetration",
            SurfaceMaterial.entries.size == 12, observed(SurfaceMaterial.entries.size, 12)
        )
        recorder.check(
            "Penetration rule", "wood, glass, fabric and flesh are penetrable",
            SurfaceLibrary.penetrableSurfaces() == listOf(
                SurfaceMaterial.WOOD, SurfaceMaterial.GLASS,
                SurfaceMaterial.FABRIC, SurfaceMaterial.FLESH
            ) && !SurfaceMaterial.CONCRETE.penetrable,
            observed(SurfaceLibrary.penetrableSurfaces().size, 4)
        )
        recorder.check(
            "Tactical FX reuse", "metal and flesh reuse the tactical table, grass does not",
            SurfaceLibrary.tacticalEffect(SurfaceMaterial.METAL)?.surface == ImpactSurface.METAL &&
                SurfaceLibrary.tacticalEffect(SurfaceMaterial.FLESH)?.surface == ImpactSurface.FLESH &&
                SurfaceLibrary.tacticalEffect(SurfaceMaterial.GRASS) == null,
            observed(SurfaceLibrary.tacticalEffect(SurfaceMaterial.METAL)?.surface, ImpactSurface.METAL)
        )
        recorder.check(
            "Terrain layers", "4 to 6 splat layers, never 3 or 7",
            EnvironmentRules.isSplatCountLegal(4) && EnvironmentRules.isSplatCountLegal(6) &&
                !EnvironmentRules.isSplatCountLegal(3) && !EnvironmentRules.isSplatCountLegal(7),
            observed(EnvironmentRules.SPLAT_LAYERS_MAX, 6)
        )
        recorder.check(
            "Mission area", "1 km2 ceiling inclusive",
            EnvironmentRules.isWithinMissionArea(1_000_000f) &&
                !EnvironmentRules.isWithinMissionArea(1_000_001f),
            observed(EnvironmentRules.isWithinMissionArea(1_000_001f), false)
        )
        recorder.check(
            "Landmarks and cover", "1-2 landmarks, low 1.0 m, high 2.0 m",
            EnvironmentRules.isLandmarkCountLegal(1) && EnvironmentRules.isLandmarkCountLegal(2) &&
                !EnvironmentRules.isLandmarkCountLegal(3) &&
                approxEquals(EnvironmentRules.coverHeight("low") ?: -1f, 1f) &&
                approxEquals(EnvironmentRules.coverHeight("high") ?: -1f, 2f),
            observed(EnvironmentRules.coverHeight("high"), 2f)
        )
        recorder.check(
            "Prop density", "one prop every 5-8 m: 80 m must hold 10 to 16 props",
            EnvironmentRules.propCountRange(80f) == 10..16,
            observed(EnvironmentRules.propCountRange(80f), 10..16)
        )
        recorder.check(
            "Texture power of two", "2048 and 1024 legal, 1500 refused",
            ImportRules.isTextureSizeLegal(2048) && ImportRules.isTextureSizeLegal(1024) &&
                !ImportRules.isTextureSizeLegal(1500),
            observed(ImportRules.isTextureSizeLegal(1500), false)
        )
        recorder.check(
            "Import gate", "a clean import reports nothing and a bad one reports five faults",
            ImportRules.violations(1f, false, true, listOf(2048, 1024), false).isEmpty() &&
                ImportRules.violations(0.01f, true, false, listOf(1500), true).size == 5,
            observed(ImportRules.violations(0.01f, true, false, listOf(1500), true).size, 5)
        )
        return recorder.result()
    }
}
