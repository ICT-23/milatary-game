package com.example.game.tactical.render

import com.example.game.tactical.gunsmith.ForgeMaterial

/**
 * Section 6: the rendering pipeline.
 *
 * Three.js WebGL2/WebGPU on the web build, URP/HDRP or Unreal 5 for the
 * packaged builds - all using the same metallic-roughness PBR workflow, so a
 * material authored once reads the same everywhere.
 */
enum class RenderBackend(val displayName: String) {
    WEBGL2("Three.js WebGL2"),
    WEBGPU("Three.js WebGPU"),
    UNITY_URP("Unity URP"),
    UNITY_HDRP("Unity HDRP"),
    UNREAL_5("Unreal Engine 5")
}

/** Metallic-roughness material, matching the gunsmith's forgeable stock. */
data class PbrMaterial(
    val id: String,
    val metallic: Float,
    val roughness: Float,
    val baseColorHex: String,
    val normalStrength: Float
)

object PbrMaterials {

    val WEAPON_ALLOY = PbrMaterial("weapon_alloy", 0.85f, 0.35f, "#3A3F44", 0.60f)
    val POLYMER = PbrMaterial("polymer", 0.05f, 0.65f, "#1E1F22", 0.35f)
    val HIGH_GRADE_STEEL = PbrMaterial("high_grade_steel", 0.95f, 0.22f, "#8A9199", 0.45f)
    val CONCRETE = PbrMaterial("concrete", 0.00f, 0.90f, "#9A9A94", 0.80f)
    val TERRAIN_DIRT = PbrMaterial("terrain_dirt", 0.00f, 0.95f, "#6B5A45", 0.90f)

    val ALL: List<PbrMaterial> = listOf(WEAPON_ALLOY, POLYMER, HIGH_GRADE_STEEL, CONCRETE, TERRAIN_DIRT)

    fun forForgeMaterial(material: ForgeMaterial): PbrMaterial = when (material) {
        ForgeMaterial.WEAPON_ALLOY -> WEAPON_ALLOY
        ForgeMaterial.POLYMER -> POLYMER
        ForgeMaterial.HIGH_GRADE_STEEL -> HIGH_GRADE_STEEL
    }

    fun byId(id: String): PbrMaterial? = ALL.firstOrNull { it.id == id }
}

/** Directional sun with cascaded shadow maps (CSM). */
data class LightingSettings(
    val sunElevationDegrees: Float = 38f,
    val sunAzimuthDegrees: Float = 135f,
    val colorTemperatureKelvin: Int = 5600,
    val cascadeCount: Int = 4,
    val shadowDistanceMeters: Float = 180f,
    val shadowResolution: Int = 2048
)

/** Screen-space AO, bloom for muzzle flashes, and DoF while aiming or at the bench. */
data class PostProcessing(
    val ssao: Boolean = true,
    val ssaoRadiusMeters: Float = 0.85f,
    val bloom: Boolean = true,
    val bloomIntensity: Float = 0.65f,
    val depthOfField: Boolean = true,
    val motionBlur: Boolean = false
)

enum class QualityPreset(val displayName: String) {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High"),
    ULTRA("Ultra")
}

data class RenderSettings(
    val backend: RenderBackend,
    val preset: QualityPreset,
    val resolutionScale: Float,
    val lighting: LightingSettings,
    val post: PostProcessing
)

object RenderPipeline {

    const val MIN_RESOLUTION_SCALE = 0.5f
    const val DEFAULT_BACKEND_TIER = 3

    fun settings(preset: QualityPreset, backend: RenderBackend = RenderBackend.WEBGL2): RenderSettings =
        RenderSettings(
            backend = backend,
            preset = preset,
            resolutionScale = resolutionScale(preset),
            lighting = lighting(preset),
            post = post(preset)
        )

    fun resolutionScale(preset: QualityPreset): Float = when (preset) {
        QualityPreset.LOW -> 0.60f
        QualityPreset.MEDIUM -> 0.80f
        QualityPreset.HIGH -> 1.00f
        QualityPreset.ULTRA -> 1.00f
    }.coerceAtLeast(MIN_RESOLUTION_SCALE)

    fun lighting(preset: QualityPreset): LightingSettings = when (preset) {
        QualityPreset.LOW -> LightingSettings(cascadeCount = 1, shadowDistanceMeters = 60f, shadowResolution = 512)
        QualityPreset.MEDIUM -> LightingSettings(cascadeCount = 2, shadowDistanceMeters = 110f, shadowResolution = 1024)
        QualityPreset.HIGH -> LightingSettings(cascadeCount = 3, shadowDistanceMeters = 150f, shadowResolution = 2048)
        QualityPreset.ULTRA -> LightingSettings(cascadeCount = 4, shadowDistanceMeters = 220f, shadowResolution = 4096)
    }

    fun post(preset: QualityPreset): PostProcessing = when (preset) {
        QualityPreset.LOW -> PostProcessing(ssao = false, bloom = true, bloomIntensity = 0.45f, depthOfField = false)
        QualityPreset.MEDIUM -> PostProcessing(ssao = true, bloom = true, bloomIntensity = 0.55f, depthOfField = true)
        QualityPreset.HIGH -> PostProcessing()
        QualityPreset.ULTRA -> PostProcessing(motionBlur = true)
    }

    /**
     * Picks a preset from the device budget rather than assuming the flagship
     * case, so the same APK runs on a low-end phone and a desktop.
     */
    fun recommend(totalRamMegabytes: Int, gpuTier: Int): QualityPreset = when {
        gpuTier >= DEFAULT_BACKEND_TIER && totalRamMegabytes >= 8_192 -> QualityPreset.ULTRA
        gpuTier >= 2 && totalRamMegabytes >= 4_096 -> QualityPreset.HIGH
        totalRamMegabytes >= 2_048 -> QualityPreset.MEDIUM
        else -> QualityPreset.LOW
    }
}
