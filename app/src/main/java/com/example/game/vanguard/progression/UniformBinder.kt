package com.example.game.vanguard.progression

import com.example.game.vanguard.core.EventBus
import com.example.game.vanguard.core.RankChanged
import com.example.game.vanguard.core.subscribeTo

/**
 * Section 3.3 item 4 / Section 2's rank table: one uniform preset per rank, so
 * a promotion changes what the character is wearing without a scene reload.
 *
 * Customization (face, hair, personal gear) is carried across untouched - the
 * binder replaces the garment layer only.
 */
data class CharacterCustomization(
    val faceId: String = "FACE_A",
    val hairId: String = "HAIR_SHORT",
    val gearIds: List<String> = emptyList()
)

data class UniformPreset(
    val rank: VanguardRank,
    val garrisonGarment: String,
    val fieldGarment: String,
    val epaulets: String,
    val collarInsignia: String,
    val headgear: String,
    val ribbonRack: Boolean,
    val detail: String
)

data class CharacterAppearance(
    val rank: VanguardRank,
    val preset: UniformPreset,
    val customization: CharacterCustomization
) {
    val isOfficer: Boolean get() = rank.tier >= 3
    val isCommanderInChief: Boolean get() = rank == VanguardRank.CIV_PRES
}

object UniformTable {

    private val TIER_ONE = UniformPreset(
        rank = VanguardRank.E1_PVT,
        garrisonGarment = "MESH_UTILITY_FATIGUES",
        fieldGarment = "MESH_UTILITY_FATIGUES",
        epaulets = "EPAU_LIGHT_BLANK",
        collarInsignia = "INSIGNIA_UNIT",
        headgear = "HELMET_KEVLAR_BASIC",
        ribbonRack = false,
        detail = "Standard utility fatigues, basic Kevlar helmet, unadorned collar"
    )

    private val TIER_TWO = UniformPreset(
        rank = VanguardRank.E6_SSG,
        garrisonGarment = "MESH_NCO_GARRISON",
        fieldGarment = "MESH_PLATE_CARRIER",
        epaulets = "EPAU_CHEVRONS_NCO",
        collarInsignia = "INSIGNIA_NCO_CHEVRON",
        headgear = "HELMET_NVG_MOUNTED",
        ribbonRack = false,
        detail = "Tactical plate carrier, NCO chevrons, squad comms rig, night-vision mount"
    )

    private val TIER_THREE_SILVER = UniformPreset(
        rank = VanguardRank.O1_LT,
        garrisonGarment = "MESH_OFFICER_SERVICE_DRESS",
        fieldGarment = "MESH_MODULAR_CHEST_RIG_SILVER",
        epaulets = "EPAU_OFFICER_SILVER_BAR",
        collarInsignia = "INSIGNIA_LT",
        headgear = "CAP_SERVICE_DRESS",
        ribbonRack = false,
        detail = "Officer service dress in garrison; chest rig with silver bars in field"
    )

    private val TIER_THREE_GOLD = TIER_THREE_SILVER.copy(
        rank = VanguardRank.O3_CPT,
        fieldGarment = "MESH_MODULAR_CHEST_RIG_GOLD",
        epaulets = "EPAU_OFFICER_GOLD_BAR",
        collarInsignia = "INSIGNIA_CPT"
    )

    private val TIER_FOUR = UniformPreset(
        rank = VanguardRank.O5_LTC,
        garrisonGarment = "MESH_GO_DRESS_RIBBON_RACK",
        fieldGarment = "MESH_COMMAND_OPS_SUIT",
        epaulets = "EPAU_GO_STAR",
        collarInsignia = "INSIGNIA_GO",
        headgear = "CAP_GO_SERVICE",
        ribbonRack = true,
        detail = "General officer dress uniform with ribbon rack; Command Ops tactical suit"
    )

    private val TIER_FOUR_STAR = TIER_FOUR.copy(
        rank = VanguardRank.O10_GEN,
        epaulets = "EPAU_FOUR_STAR",
        collarInsignia = "INSIGNIA_ARMY_COMMANDER"
    )

    private val TIER_FIVE = UniformPreset(
        rank = VanguardRank.CIV_PRES,
        garrisonGarment = "MESH_CIVILIAN_EXECUTIVE_SUIT",
        fieldGarment = "MESH_CIVILIAN_EXECUTIVE_SUIT",
        epaulets = "EPAU_NONE",
        collarInsignia = "PIN_COMMANDER_IN_CHIEF",
        headgear = "NONE",
        ribbonRack = false,
        detail = "Tailored executive suit with Commander-in-Chief lapel pin and detail"
    )

    fun forRank(rank: VanguardRank): UniformPreset = when (rank) {
        VanguardRank.E1_PVT, VanguardRank.E4_CPL -> TIER_ONE.copy(rank = rank)
        VanguardRank.E6_SSG -> TIER_TWO
        VanguardRank.O1_LT -> TIER_THREE_SILVER
        VanguardRank.O3_CPT -> TIER_THREE_GOLD
        VanguardRank.O5_LTC, VanguardRank.O7_BG -> TIER_FOUR.copy(rank = rank)
        VanguardRank.O10_GEN -> TIER_FOUR_STAR
        VanguardRank.CIV_PRES -> TIER_FIVE
    }
}

/**
 * Listens for [RankChanged] and swaps the garment layer. The binder never edits
 * the character's face, hair or personal gear, which was the "static uniform"
 * complaint in the legacy build.
 */
class UniformMeshBinder(
    private val bus: EventBus,
    private val customization: CharacterCustomization = CharacterCustomization()
) {
    private var slot: EventBus.Subscription? = null

    var appearance: CharacterAppearance = previewFor(VanguardRank.E1_PVT, customization)
        private set

    var swapCount: Int = 0
        private set

    fun attach(): UniformMeshBinder {
        slot?.unsubscribe()
        slot = bus.subscribeTo<RankChanged> { event ->
            val rank = VanguardRank.fromIndex(event.newRankIndex)
            if (rank != null) apply(rank)
        }
        return this
    }

    fun detach() {
        slot?.unsubscribe()
        slot = null
    }

    fun previewFor(rank: VanguardRank, customization: CharacterCustomization): CharacterAppearance =
        CharacterAppearance(rank = rank, preset = UniformTable.forRank(rank), customization = customization)

    private fun apply(rank: VanguardRank) {
        appearance = previewFor(rank, appearance.customization)
        swapCount += 1
    }
}
