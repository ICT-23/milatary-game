package com.example.game.settings

/**
 * Section 21.3 quality presets.
 *
 * Low, Medium and High each describe the same seven axes (resolution scale, shadows,
 * textures, effects, foliage, post-processing, crowd/AI count) so the host and the
 * Three.js engine agree on what a preset means. Nothing here can lock the player out
 * of the settings screen: [recommend] only offers advice and the manual choice wins.
 */
enum class QualityPreset(
    val id: String,
    val label: String,
    val resolutionScale: Float,
    val shadowDistanceMeters: Float,
    val textureTier: Int,
    val particleBudget: Float,
    val foliageDensity: Float,
    val postProcessingTier: Int,
    val aiBudget: Float
) {
    /** Compressed low-memory textures, minimal shadows, reduced particles and foliage. */
    LOW(
        id = "LOW",
        label = "LOW",
        resolutionScale = 0.75f,
        shadowDistanceMeters = 18f,
        textureTier = 0,
        particleBudget = 0.45f,
        foliageDensity = 0.55f,
        postProcessingTier = 0,
        aiBudget = 0.7f
    ),

    /** Native resolution, medium shadow distance, standard textures and effects. */
    MEDIUM(
        id = "MEDIUM",
        label = "MEDIUM",
        resolutionScale = 1.0f,
        shadowDistanceMeters = 34f,
        textureTier = 1,
        particleBudget = 0.8f,
        foliageDensity = 1.0f,
        postProcessingTier = 1,
        aiBudget = 1.0f
    ),

    /** Native resolution with the higher-quality texture set and the full restrained stack. */
    HIGH(
        id = "HIGH",
        label = "HIGH",
        resolutionScale = 1.0f,
        shadowDistanceMeters = 52f,
        textureTier = 2,
        particleBudget = 1.15f,
        foliageDensity = 1.3f,
        postProcessingTier = 2,
        aiBudget = 1.2f
    );

    companion object {
        private const val LOW_HEAP_MB = 192
        private const val MEDIUM_HEAP_MB = 384

        fun fromId(id: String?): QualityPreset =
            entries.firstOrNull { it.id.equals(id?.trim(), ignoreCase = true) } ?: MEDIUM

        /**
         * Advisory only. A small heap or fewer than four cores cannot sustain the high
         * preset, so the screen offers that advice without ever applying it automatically.
         */
        fun recommend(heapMegabytes: Int, cores: Int): QualityPreset = when {
            heapMegabytes < LOW_HEAP_MB || cores <= 2 -> LOW
            heapMegabytes < MEDIUM_HEAP_MB || cores <= 4 -> MEDIUM
            else -> HIGH
        }

        fun recommendationNote(recommended: QualityPreset): String = when (recommended) {
            LOW -> "This device class favours the low preset. Manual selection stays open."
            MEDIUM -> "This device class favours the medium preset. Manual selection stays open."
            HIGH -> "This device class can sustain the high preset. Manual selection stays open."
        }
    }
}
