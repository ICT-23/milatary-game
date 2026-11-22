package com.example.game.valor.pipeline

/**
 * Section 13.1: the naming prefixes every imported asset must carry.
 */
enum class AssetPrefix(val code: String, val usage: String) {
    SKELETAL("SK_", "skeletal mesh"),
    STATIC_MESH("SM_", "static mesh"),
    TEXTURE("T_", "texture"),
    MATERIAL("M_", "material"),
    MATERIAL_INSTANCE("MI_", "material instance"),
    ANIMATION("A_", "animation"),
    ANIMATOR_CONTROLLER("AC_", "animator controller"),
    VFX("FX_", "VFX"),
    PREFAB("PF_", "prefab"),
    UI("UI_", "UI sprite"),
    SFX("SFX_", "sound effect"),
    MUSIC("MUS_", "music"),
    VOICE("VO_", "voice over")
}

enum class LicenseKind(val displayName: String) {
    CC0("CC0"),
    CC_BY("CC-BY"),
    ASSET_STORE_EULA("Asset Store EULA"),
    AI_GENERATED("AI-generated"),
    PROPRIETARY("Proprietary")
}

/**
 * Section 13.2: one row of `ASSET_LICENSES.md`.
 */
data class LicenseRecord(
    val name: String,
    val sourceUrl: String,
    val author: String,
    val kind: LicenseKind,
    val date: String,
    val commercialUse: Boolean?,
    val attributionRequired: Boolean?
) {
    /** A record only clears the gate when every field the bible demands is known. */
    val isComplete: Boolean
        get() = name.isNotBlank() && sourceUrl.isNotBlank() && author.isNotBlank() &&
            date.isNotBlank() && commercialUse != null && attributionRequired != null

    val isShippable: Boolean get() = isComplete && commercialUse == true
}

object AssetPipeline {

    val FOLDERS: List<String> = listOf(
        "Art/Characters", "Art/Weapons", "Art/Environment/<Act>", "Art/Props",
        "Art/VFX", "Art/Animations", "Art/UI", "Art/Audio/SFX", "Art/Audio/Music", "Art/Audio/VO"
    )

    /** Section 13.3 import checklist, in order. */
    val IMPORT_CHECKLIST: List<String> = listOf(
        "Scale/pivot correct, no negative scale",
        "Triangles within budget, LODs set",
        "Textures compressed, mipmaps, correct color space",
        "Materials use project shaders (URP Lit / custom mobile shader)",
        "Colliders simple; layer set; static flags set",
        "Tested on low-end preset",
        "License logged"
    )

    /** Section 15 art QA checklist, run before shipping an act. */
    val ART_QA_CHECKLIST: List<String> = listOf(
        "Consistent color grade per act",
        "No texture stretching/z-fighting/light leaks",
        "Enemies readable against every background (squint test)",
        "Cover heights consistent",
        "VFX within particle budgets, pooled",
        "Audio levels balanced, no clipping",
        "UI readable on 5\" and 10\" screens; safe areas respected",
        "Low preset still looks acceptable",
        "Localization strings fit"
    )

    const val PLACEHOLDER_SAME_NAME_AND_PIVOT = true
    const val PLACEHOLDER_PREFAB_VARIANTS = true
    const val PLACEHOLDER_SAME_SOCKET_NAMES = true
    const val LICENSE_LOG_FILE = "ASSET_LICENSES.md"

    fun prefixFor(assetName: String): AssetPrefix? =
        AssetPrefix.entries.firstOrNull { assetName.startsWith(it.code) }

    /** Empty string means the name is legal; otherwise the reason it is not. */
    fun namingViolation(assetName: String): String = when {
        assetName.isBlank() -> "asset name is empty"
        prefixFor(assetName) == null ->
            "missing prefix, expected one of ${AssetPrefix.entries.joinToString { it.code }}"
        else -> ""
    }

    fun checklistComplete(checkedItems: Collection<String>): Boolean =
        checkedItems.containsAll(IMPORT_CHECKLIST)

    fun qaComplete(checkedItems: Collection<String>): Boolean =
        checkedItems.containsAll(ART_QA_CHECKLIST)

    fun unloggedAssets(shipped: Collection<String>, logged: Collection<LicenseRecord>): List<String> =
        shipped.filter { asset -> logged.none { it.name == asset && it.isComplete } }
}

/**
 * Section 14: concept and texture prompt ideas. Idea generation only - licensing
 * must be checked before anything is shipped.
 */
object AiArtPrompts {
    val PROMPTS: List<String> = listOf(
        "Gritty realistic WW1 trench in fog at dawn, mud, wooden planks, barbed wire, " +
            "desaturated, cinematic lighting, game environment concept art.",
        "Tileable PBR texture: worn olive-drab canvas fabric with stitching, 2048, seamless.",
        "Rank insignia set, 20 designs, realistic embroidered patches, flat lay, transparent background.",
        "Futuristic drone control HUD overlay, thin cyan lines, military tactical, 16:9, transparent.",
        "Character concept: female sniper cadet, field uniform, 1940s style, realistic proportions, " +
            "turnaround sheet."
    )

    const val CONCEPT_ONLY = true
    const val LICENSE_CHECK_BEFORE_SHIPPING = true
}
