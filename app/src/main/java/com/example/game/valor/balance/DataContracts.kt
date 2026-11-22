package com.example.game.valor.balance

/**
 * Section 9: one schema checklist and the subset of it this data model already
 * carries.
 *
 * The catalogue files were transcribed from the balance tables only, so a
 * checklist field with no table column behind it (spread, penetration, scene
 * references, checkpoints) is reported as [missing] instead of being invented.
 */
data class DataContract(
    val schemaName: String,
    val fields: List<String>,
    val declared: Set<String>
) {
    val missing: List<String> get() = fields.filterNot { it in declared }

    val coveragePercent: Int
        get() = if (fields.isEmpty()) 100 else (declared.count { it in fields } * 100) / fields.size

    val isComplete: Boolean get() = missing.isEmpty()
}

object FieldChecklists {

    val WEAPON_DATA: List<String> = listOf(
        "id", "name", "class", "era", "damage", "pelletCount", "rpm", "fireModes",
        "range", "minDamageMult", "accuracy", "spreadBase", "adsSpreadMult", "adsFov",
        "adsTime", "recoilCurve", "recoilRecovery", "reloadTime", "emptyReloadTime",
        "magSize", "reserveCap", "penetration", "armorPierce", "noiseRadius", "moveMult",
        "swapTime", "unlockRank", "price", "slots", "mesh/prefab refs", "audio refs"
    )

    val ENEMY_DATA: List<String> = listOf(
        "id", "type", "hp", "armor", "weapon", "accuracy", "reaction", "speed",
        "visionRange/angle", "hearingRadius", "xp", "cashDrop", "behaviors"
    )

    val MISSION_DATA: List<String> = listOf(
        "id", "act", "name", "type", "scene", "objectives", "baseXP", "baseCash",
        "resources", "starRules", "unlocks", "storyBeat", "checkpoints"
    )

    val RANK_DATA: List<String> = listOf("index", "name", "xpRequired", "skillPoints", "unlocks")

    /** Table columns the weapon catalogue actually carries. */
    private val WEAPON_DECLARED: Set<String> = setOf(
        "id", "name", "class", "era", "damage", "pelletCount", "rpm", "range",
        "minDamageMult", "accuracy", "reloadTime", "magSize", "reserveCap", "unlockRank", "price"
    )

    /** `EnemySpec` carries everything the section 4 table defines. */
    private val ENEMY_DECLARED: Set<String> = setOf(
        "id", "type", "hp", "armor", "weapon", "accuracy", "reaction", "speed", "xp", "cashDrop"
    )

    /** `MissionSpec` carries the seven section 7 columns plus the derived id. */
    private val MISSION_DECLARED: Set<String> = setOf(
        "id", "act", "name", "type", "objectives", "baseXP", "baseCash", "unlocks"
    )

    private val RANK_DECLARED: Set<String> = setOf("index", "name", "xpRequired", "skillPoints", "unlocks")

    val WEAPONS: DataContract = DataContract("WeaponData", WEAPON_DATA, WEAPON_DECLARED)
    val ENEMIES: DataContract = DataContract("EnemyData", ENEMY_DATA, ENEMY_DECLARED)
    val MISSIONS: DataContract = DataContract("MissionData", MISSION_DATA, MISSION_DECLARED)
    val RANKS: DataContract = DataContract("RankData", RANK_DATA, RANK_DECLARED)

    val ALL: List<DataContract> = listOf(WEAPONS, ENEMIES, MISSIONS, RANKS)

    fun byName(schemaName: String): DataContract? = ALL.firstOrNull { it.schemaName == schemaName }

    /** A declared field that is not in the checklist would be a typo, not coverage. */
    fun strayFields(contract: DataContract): List<String> = contract.declared.filterNot { it in contract.fields }
}
