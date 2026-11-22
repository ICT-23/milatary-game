package com.example.game.valor.art

import com.example.game.combat.SurfaceType as LegacySurface
import com.example.game.tactical.render.ImpactEffect
import com.example.game.tactical.render.ImpactSurface
import com.example.game.tactical.render.ImpactVfx

/**
 * Section 5.2: the twelve surfaces that drive decals, particles, impact sound,
 * footstep sound and the penetration rule.
 *
 * [tacticalSurface] reuses the already-shipping impact effect table instead of
 * duplicating particle ids; surfaces the tactical table does not model stay null
 * and use their own entry in [impactFx].
 */
enum class SurfaceMaterial(
    val displayName: String,
    val impactFx: String,
    val footstepId: String,
    val penetrable: Boolean,
    val legacy: LegacySurface,
    val tacticalSurface: ImpactSurface?
) {
    CONCRETE("Concrete", "fx_dust_puff", "step_concrete", false, LegacySurface.CONCRETE, ImpactSurface.CONCRETE),
    METAL("Metal", "fx_sparks", "step_metal", false, LegacySurface.STEEL, ImpactSurface.METAL),
    WOOD("Wood", "fx_splinters", "step_wood", true, LegacySurface.WOOD, ImpactSurface.WOOD),
    DIRT("Dirt", "fx_dirt_spray", "step_dirt", false, LegacySurface.DIRT, ImpactSurface.DIRT),
    MUD("Mud", "fx_mud_splash", "step_mud", false, LegacySurface.DIRT, ImpactSurface.DIRT),
    GRASS("Grass", "fx_grass_flick", "step_grass", false, LegacySurface.GENERIC, null),
    SAND("Sand", "fx_sand_puff", "step_sand", false, LegacySurface.DIRT, ImpactSurface.DIRT),
    SNOW("Snow", "fx_snow_puff", "step_snow", false, LegacySurface.GENERIC, null),
    WATER("Water", "fx_water_splash", "step_water", false, LegacySurface.GENERIC, ImpactSurface.WATER),
    GLASS("Glass", "fx_glass_shards", "step_glass", true, LegacySurface.GENERIC, null),
    FABRIC("Fabric", "fx_fabric_puff", "step_fabric", true, LegacySurface.GENERIC, null),
    FLESH("Flesh", "fx_blood_mist", "step_flesh", true, LegacySurface.FLESH, ImpactSurface.FLESH)
}

object SurfaceLibrary {

    fun byId(id: String): SurfaceMaterial? =
        SurfaceMaterial.entries.firstOrNull { it.name.equals(id.trim(), ignoreCase = true) }

    fun penetrableSurfaces(): List<SurfaceMaterial> = SurfaceMaterial.entries.filter { it.penetrable }

    /** The tactical impact table entry, when the surface has a shared family. */
    fun tacticalEffect(material: SurfaceMaterial): ImpactEffect? =
        material.tacticalSurface?.let { ImpactVfx.effectFor(it) }

    fun displayNames(): List<String> = SurfaceMaterial.entries.map { it.displayName }
}

/**
 * Section 5.1: level kit, scatter, terrain and readability rules.
 */
object EnvironmentRules {
    const val SPLAT_LAYERS_MIN = 4
    const val SPLAT_LAYERS_MAX = 6
    const val MAX_AREA_SQUARE_METERS = 1_000_000f
    const val PROP_SPACING_MIN_METERS = 5f
    const val PROP_SPACING_MAX_METERS = 8f
    const val LANDMARKS_MIN = 1
    const val LANDMARKS_MAX = 2
    const val COVER_LOW_HEIGHT_METERS = 1.0f
    const val COVER_HIGH_HEIGHT_METERS = 2.0f
    const val FOLIAGE_WIND_SHADER = true
    const val DESTRUCTIBLE_DAMAGE_STATE_TEXTURE = true
    const val OBJECTIVE_CUE = "light / flag / sound"

    fun isWithinMissionArea(areaSquareMeters: Float): Boolean =
        areaSquareMeters > 0f && areaSquareMeters <= MAX_AREA_SQUARE_METERS

    fun isSplatCountLegal(layers: Int): Boolean = layers in SPLAT_LAYERS_MIN..SPLAT_LAYERS_MAX

    fun isLandmarkCountLegal(count: Int): Boolean = count in LANDMARKS_MIN..LANDMARKS_MAX

    /** Gameplay-relevant props a walkable run of [walkableMeters] must contain. */
    fun propCountRange(walkableMeters: Float): IntRange {
        val meters = walkableMeters.coerceAtLeast(0f)
        return (meters / PROP_SPACING_MAX_METERS).toInt()..(meters / PROP_SPACING_MIN_METERS).toInt()
    }

    fun coverHeight(label: String): Float? = when (label.trim().lowercase()) {
        "low" -> COVER_LOW_HEIGHT_METERS
        "high" -> COVER_HIGH_HEIGHT_METERS
        else -> null
    }
}

/**
 * Section 5.3: model and texture import rules.
 */
object ImportRules {
    const val MODEL_FORMAT = "FBX"
    const val MODEL_SCALE_METERS = 1f
    const val FORWARD_AXIS = "Z"
    const val PIVOT_AT_BASE = true
    const val NEGATIVE_SCALE_ALLOWED = false
    const val APPLY_TRANSFORMS = true
    const val TEXTURE_POWER_OF_TWO = true
    const val ASTC_COMPRESSION = true
    const val SRGB_ON_COLOR_MAPS_ONLY = true
    const val MIPMAPS = true
    const val STREAMING_MIPMAPS_LARGE_SCENES = true
    const val ATLAS_SMALL_PROPS = true
    const val LIGHTMAP_UV2 = true
    const val STATIC_FLAGS = true
    const val SIMPLE_COLLIDERS_ONLY = true

    fun isPowerOfTwo(size: Int): Boolean = size > 0 && (size and (size - 1)) == 0

    fun isTextureSizeLegal(size: Int): Boolean = isPowerOfTwo(size)

    /** Empty list means the import honours every rule. */
    fun violations(
        scaleMeters: Float,
        negativeScale: Boolean,
        transformsApplied: Boolean,
        textureSizes: List<Int>,
        colliderIsFullMesh: Boolean
    ): List<String> {
        val problems = mutableListOf<String>()
        if (scaleMeters != MODEL_SCALE_METERS) problems += "scale must be 1 = 1 m"
        if (negativeScale) problems += "negative scale is forbidden"
        if (!transformsApplied) problems += "transforms must be applied"
        if (textureSizes.any { !isTextureSizeLegal(it) }) problems += "textures must be power-of-two"
        if (colliderIsFullMesh) problems += "dense props need simplified colliders"
        return problems
    }
}
