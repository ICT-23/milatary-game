package com.example.game.vanguard.meta

import com.example.game.vanguard.core.GameErrorCategory
import com.example.game.vanguard.core.GameResult

/**
 * Section 2 Meta: VehicleCustomizationBay.
 *
 * Vehicles are assembled from four slots exactly like a weapon is assembled
 * from seven sockets. Armour buys survivability with mass, the powertrain buys
 * speed back, and a slot that is not filled is a typed failure.
 */
data class VehicleBase(
    val chassisId: String,
    val name: String,
    val baseMassKg: Float,
    val baseTopSpeedKph: Float,
    val baseArmorRating: Int
)

enum class ArmorPackage(
    val id: String,
    val massKg: Float,
    val rating: Int,
    val speedPenaltyKph: Float
) {
    ARMOR_LIGHT("ARMOR_LIGHT", 180f, 2, 4f),
    ARMOR_MEDIUM("ARMOR_MEDIUM", 520f, 5, 12f),
    ARMOR_HEAVY("ARMOR_HEAVY", 980f, 8, 26f)
}

enum class PowertrainPackage(
    val id: String,
    val speedBonusKph: Float,
    val massKg: Float
) {
    POWERTRAIN_STANDARD("POWERTRAIN_STANDARD", 0f, 0f),
    POWERTRAIN_UPGRADED("POWERTRAIN_UPGRADED", 14f, 120f),
    POWERTRAIN_HYBRID("POWERTRAIN_HYBRID", 22f, 260f)
}

data class VehicleConfig(
    val chassisId: String,
    val paintId: String,
    val armor: ArmorPackage,
    val powertrain: PowertrainPackage,
    val weaponStationId: String
)

data class VehicleStats(
    val massKg: Float,
    val topSpeedKph: Float,
    val armorRating: Int,
    val firepowerRating: Int
)

object VehicleCustomizationBay {

    val WEAPON_STATIONS: Map<String, Int> = mapOf(
        "WEAPON_HMG" to 6,
        "WEAPON_AUTOCANNON" to 9,
        "WEAPON_MORTAR" to 7
    )

    val PAINTS: List<String> = listOf("PAINT_OLIVE", "PAINT_DESERT", "PAINT_ARCTIC", "PAINT_URBAN")

    fun fit(base: VehicleBase, config: VehicleConfig): GameResult<VehicleStats> {
        if (!config.chassisId.equals(base.chassisId, ignoreCase = true)) {
            return GameResult.fail(
                GameErrorCategory.INCOMPATIBLE,
                "CHASSIS_MISMATCH",
                "Configuration targets ${config.chassisId}, bay holds ${base.chassisId}"
            )
        }
        if (config.paintId.isBlank() || config.weaponStationId.isBlank()) {
            return GameResult.fail(
                GameErrorCategory.INVALID_STATE,
                "INCOMPLETE_SLOTS",
                "Paint and weapon station must both be filled"
            )
        }
        if (config.paintId !in PAINTS) {
            return GameResult.fail(
                GameErrorCategory.INCOMPATIBLE,
                "UNKNOWN_PAINT",
                "No paint with id ${config.paintId}"
            )
        }
        val firepower = WEAPON_STATIONS[config.weaponStationId]
            ?: return GameResult.fail(
                GameErrorCategory.INCOMPATIBLE,
                "UNKNOWN_WEAPON_STATION",
                "No weapon station with id ${config.weaponStationId}"
            )

        val mass = base.baseMassKg + config.armor.massKg + config.powertrain.massKg
        val speed = (base.baseTopSpeedKph - config.armor.speedPenaltyKph + config.powertrain.speedBonusKph)
            .coerceAtLeast(0f)

        return GameResult.Ok(
            VehicleStats(
                massKg = mass,
                topSpeedKph = speed,
                armorRating = base.baseArmorRating + config.armor.rating,
                firepowerRating = firepower
            )
        )
    }
}
