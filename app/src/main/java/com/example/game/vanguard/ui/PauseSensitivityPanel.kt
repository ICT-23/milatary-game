package com.example.game.vanguard.ui

import com.example.game.vanguard.core.GameErrorCategory
import com.example.game.vanguard.core.GameResult

/**
 * Section 2 UI: PauseSensitivityPanel.
 *
 * The pause menu is the only surface allowed to touch aim settings during a
 * mission, so the mapping lives here as pure data. A preset is a fixed triple of
 * hip multiplier, ADS multiplier and deadzone; free values must stay inside the
 * published bounds or the panel refuses to apply them.
 */
enum class SensitivityPreset(
    val displayName: String,
    val hipMultiplier: Float,
    val adsMultiplier: Float,
    val deadzone: Float
) {
    LOW("Low", 0.70f, 0.55f, 0.12f),
    MEDIUM("Medium", 1.00f, 0.80f, 0.08f),
    HIGH("High", 1.35f, 1.05f, 0.05f)
}

data class SensitivityProfile(
    val hipMultiplier: Float,
    val adsMultiplier: Float,
    val deadzone: Float
)

object PauseSensitivityPanel {

    /** Free-form values are clamped to these, per the pause-menu contract. */
    const val MIN_MULTIPLIER = 0.25f
    const val MAX_MULTIPLIER = 3.00f
    const val MIN_DEADZONE = 0f
    const val MAX_DEADZONE = 0.35f

    val PRESETS: List<SensitivityPreset> = SensitivityPreset.entries

    fun profileFor(preset: SensitivityPreset): SensitivityProfile =
        SensitivityProfile(preset.hipMultiplier, preset.adsMultiplier, preset.deadzone)

    /**
     * Applies a raw slider value. Out-of-range input is a typed failure rather
     * than a silent clamp, so a corrupted save cannot quietly double the aim.
     */
    fun apply(profile: SensitivityProfile): GameResult<SensitivityProfile> {
        if (profile.hipMultiplier !in MIN_MULTIPLIER..MAX_MULTIPLIER) {
            return GameResult.fail(
                GameErrorCategory.INVALID_STATE,
                "SENSITIVITY_OUT_OF_RANGE",
                "Hip multiplier ${profile.hipMultiplier} outside $MIN_MULTIPLIER..$MAX_MULTIPLIER"
            )
        }
        if (profile.adsMultiplier !in MIN_MULTIPLIER..MAX_MULTIPLIER) {
            return GameResult.fail(
                GameErrorCategory.INVALID_STATE,
                "SENSITIVITY_OUT_OF_RANGE",
                "ADS multiplier ${profile.adsMultiplier} outside $MIN_MULTIPLIER..$MAX_MULTIPLIER"
            )
        }
        if (profile.deadzone !in MIN_DEADZONE..MAX_DEADZONE) {
            return GameResult.fail(
                GameErrorCategory.INVALID_STATE,
                "DEADZONE_OUT_OF_RANGE",
                "Deadzone ${profile.deadzone} outside $MIN_DEADZONE..$MAX_DEADZONE"
            )
        }
        return GameResult.Ok(profile)
    }

    /** ADS reads its own multiplier; hip reads the hip multiplier. No shared ratio. */
    fun effectiveLookSpeed(profile: SensitivityProfile, baseSpeed: Float, isAimingDownSight: Boolean): Float {
        val multiplier = if (isAimingDownSight) profile.adsMultiplier else profile.hipMultiplier
        return baseSpeed * multiplier
    }
}
