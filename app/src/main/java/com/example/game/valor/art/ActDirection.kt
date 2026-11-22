package com.example.game.valor.art

/**
 * Section 1: the five pillars every asset, act and screen is graded against.
 */
enum class VisualPillar(val displayName: String, val statement: String) {
    GROUNDED_REALISM(
        "Grounded realism",
        "Believable materials: worn metal, wet mud, scratched wood, fabric with weave."
    ),
    CINEMATIC_LIGHT(
        "Cinematic light",
        "Golden-hour shafts, volumetric-looking fog, strong silhouettes, motivated light."
    ),
    READABLE_COMBAT(
        "Readable combat",
        "Enemies and objectives must be readable at distance; clutter must never hide gameplay."
    ),
    ERA_IDENTITY(
        "Era identity",
        "Each act has its own color grade, props and weapon silhouettes."
    ),
    PERFORMANCE_AWARE(
        "Performance-aware beauty",
        "Baked lighting, smart LODs, texture atlases; looks premium on a mid-range phone."
    )
}

/** Section 2: one named color of an act palette. */
data class ActSwatch(val name: String, val argb: Long)

enum class SplitTone { NONE, WARM, TEAL_ORANGE, BLEACH_BYPASS, DAY_NIGHT, CLEAN_HIGH_CONTRAST }

/**
 * Section 2 "Post-process grade" column, expressed as numbers the render pipeline
 * can consume. Where the bible states a percentage it is stored as the resulting
 * factor (desaturated 25% -> saturation 0.75, contrast +10% -> contrast 1.10).
 */
data class GradeSpec(
    val saturation: Float = 1f,
    val contrast: Float = 1f,
    val sepia: Float = 0f,
    val grain: Float = 0f,
    val bleachBypass: Boolean = false,
    val splitTone: SplitTone = SplitTone.NONE,
    val bloom: Float = 0f,
    val chromaticAberration: Float = 0f,
    val warmShift: Float = 0f
)

/**
 * Section 2: the six acts with palette, motivated light and post-process grade.
 */
enum class ArtAct(
    val number: Int,
    val title: String,
    val setting: String,
    val palette: List<ActSwatch>,
    val light: String,
    val grade: GradeSpec
) {
    BOOT_CAMP(
        number = 1,
        title = "Boot Camp",
        setting = "Academy, grass, brick",
        palette = listOf(
            ActSwatch("Olive", 0xFF667353),
            ActSwatch("Tan", 0xFFD6C29A),
            ActSwatch("Red brick", 0xFF8C4A3C),
            ActSwatch("Blue sky", 0xFF6FA8D1)
        ),
        light = "Morning sun, soft shadows",
        grade = GradeSpec(contrast = 1.05f, splitTone = SplitTone.WARM, warmShift = 0.10f)
    ),
    GREAT_WAR(
        number = 2,
        title = "Great War",
        setting = "Mud trenches, fog",
        palette = listOf(
            ActSwatch("Brown", 0xFF5A4632),
            ActSwatch("Grey", 0xFF6F7375),
            ActSwatch("Sickly green", 0xFF6B6F45)
        ),
        light = "Overcast, low sun, fog volume",
        grade = GradeSpec(saturation = 0.75f, sepia = 0.25f, grain = 0.20f)
    ),
    INTERWAR(
        number = 3,
        title = "Interwar",
        setting = "Old town, snow, docks",
        palette = listOf(
            ActSwatch("Warm stone", 0xFFB9A588),
            ActSwatch("Steel blue", 0xFF5E7C92),
            ActSwatch("Amber lamp", 0xFFD9973F)
        ),
        light = "Dusk, street lamps, snowfall",
        grade = GradeSpec(splitTone = SplitTone.TEAL_ORANGE, warmShift = 0.05f)
    ),
    WORLD_AT_WAR(
        number = 4,
        title = "World at War",
        setting = "Beach, hedgerow, ruins",
        palette = listOf(
            ActSwatch("Grey-green", 0xFF6E7462),
            ActSwatch("Ash", 0xFF8A8B86),
            ActSwatch("Fire orange", 0xFFC8612A)
        ),
        light = "Smoke-filled, explosions as light",
        grade = GradeSpec(contrast = 1.10f, splitTone = SplitTone.BLEACH_BYPASS, bleachBypass = true)
    ),
    COLD_SHADOWS(
        number = 5,
        title = "Cold Shadows",
        setting = "Desert, refinery, arctic",
        palette = listOf(
            ActSwatch("Orange sand", 0xFFC98F52),
            ActSwatch("Night cyan", 0xFF2E7E8C),
            ActSwatch("White ice", 0xFFE6EEF2)
        ),
        light = "Harsh noon / blue night / blizzard",
        grade = GradeSpec(splitTone = SplitTone.DAY_NIGHT, bloom = 0.80f)
    ),
    DRONE_AGE(
        number = 6,
        title = "Drone Age",
        setting = "Megacity, carrier, tech base",
        palette = listOf(
            ActSwatch("Cool blue", 0xFF3B5C86),
            ActSwatch("Neon cyan", 0xFF38D39F),
            ActSwatch("White", 0xFFF0F4F6)
        ),
        light = "Night city lights, HUD glow",
        grade = GradeSpec(
            contrast = 1.10f,
            splitTone = SplitTone.CLEAN_HIGH_CONTRAST,
            bloom = 0.50f,
            chromaticAberration = 0.05f
        )
    );

    /** True when the grade pulls saturation below unity. */
    val isDesaturated: Boolean get() = grade.saturation < 1f
}

object ActDirection {

    fun actFor(actNumber: Int): ArtAct =
        ArtAct.entries.firstOrNull { it.number == actNumber } ?: ArtAct.BOOT_CAMP

    fun paletteNames(act: ArtAct): List<String> = act.palette.map { it.name }

    fun swatch(act: ArtAct, name: String): ActSwatch? =
        act.palette.firstOrNull { it.name.equals(name, ignoreCase = true) }

    /** Acts whose grade carries film grain, used by the post-process stack. */
    fun grainedActs(): List<ArtAct> = ArtAct.entries.filter { it.grade.grain > 0f }

    fun bleachedActs(): List<ArtAct> = ArtAct.entries.filter { it.grade.bleachBypass }
}

/**
 * Section 2 "Lighting setup (mobile)".
 */
object LightingRules {
    const val DIRECTIONAL_REALTIME_LIGHTS = 1
    const val MAX_DYNAMIC_LIGHTS_VISIBLE = 4
    const val MUZZLE_FLASH_PULSE_SECONDS = 0.05f
    const val BAKED_GI = true
    const val LIGHT_PROBE_GROUPS = true
    const val REFLECTION_PROBES = true
    const val SKY_SOURCE = "Procedural / HDRI per act"

    fun clampDynamicLights(requested: Int): Int = requested.coerceIn(0, MAX_DYNAMIC_LIGHTS_VISIBLE)

    fun muzzlePulseFrames(frameSeconds: Float): Int =
        (MUZZLE_FLASH_PULSE_SECONDS / frameSeconds.coerceAtLeast(0.001f)).toInt().coerceAtLeast(1)
}

enum class ShakeEvent(val baseStrength: Float) {
    EXPLOSION(1.0f),
    HEAVY_FIRE(0.6f),
    LANDING(0.35f)
}

/**
 * Section 8: third-person camera, ADS, cover, shake, cinematics and the kill cam.
 */
object CameraBible {
    const val SHOULDER_OFFSET_METERS = 0.6f
    const val THIRD_PERSON_DISTANCE_METERS = 2.6f
    const val DEFAULT_FOV = 60f
    const val MIN_ADS_FOV = 5f
    const val COVER_LOWER_METERS = 0.35f
    const val COVER_OFFSET_METERS = 0.4f
    const val PEEK_RAISE_METERS = 0.30f
    const val SPRING_ARM_RAYCAST = true
    const val LETTERBOX = true
    const val CINEMATICS_SKIPPABLE = true
    const val KILL_CAM_SECONDS = 0.6f
    const val KILL_CAM_TIME_SCALE = 0.3f
    const val KILL_CAM_OPTIONAL = true
    const val SCOPE_VIGNETTE = 0.60f

    fun swapShoulder(offsetMeters: Float): Float = -offsetMeters

    /** The optic sets the FOV; a 1x sight keeps the third-person 60 degrees. */
    fun adsFov(magnification: Float): Float =
        (DEFAULT_FOV / magnification.coerceAtLeast(1f)).coerceAtLeast(MIN_ADS_FOV)

    /** Sniper scopes use a FOV change plus a vignette overlay, never a render texture. */
    fun scopeVignette(magnification: Float): Float = when {
        magnification >= 8f -> SCOPE_VIGNETTE
        magnification >= 4f -> 0.35f
        else -> 0f
    }

    fun shakeStrength(event: ShakeEvent, shakeEnabled: Boolean, scale: Float = 1f): Float =
        if (!shakeEnabled) 0f else (event.baseStrength * scale).coerceIn(0f, 1f)
}
