package com.example.game.valor.art

/**
 * Section 3.1: every character the art bible names, with its variant set.
 */
enum class CharacterRole(
    val displayName: String,
    val variants: String,
    val notes: String,
    val targetable: Boolean
) {
    PLAYER(
        "Player (customizable)",
        "2 body types x 8 faces x 6 hair x skin tones; 6 uniforms by era; camo skins",
        "Face morph via blend shapes; 4 voice options",
        true
    ),
    BROOKS("Brooks", "Era variants (young, wounded, senior)", "Scars progress by act", true),
    LENA("Lena", "Cadet, field, officer", "Sniper kit", true),
    VANCE("Vance", "Cadet, officer", "Arrogant posture", true),
    WHITFIELD("Whitfield", "Formal, field", "Commanding silhouette", true),
    KESSLER("Kessler", "Young officer, general, final", "Distinct coat/cape silhouette per age", true),
    ENEMY(
        "Enemies",
        "3-4 head/outfit variants per type per era",
        "Color-coded accents so type reads at distance",
        true
    ),
    CIVILIAN("Civilians", "6 variants", "Never targetable", false)
}

/**
 * Section 3.2: triangle budgets and texture allocation per asset class.
 * LOD0 >= LOD1 >= LOD2 is enforced so a swap can never refine upward.
 */
enum class CharacterAsset(
    val displayName: String,
    val lod0Triangles: Int,
    val lod1Triangles: Int,
    val lod2Triangles: Int,
    val textureSpec: String,
    val notes: String
) {
    HERO_PLAYER(
        "Hero player", 18_000, 9_000, 4_000,
        "2048 (body+gear atlas) + 1024 (face)",
        "4 blend-shape sets, cloth-lite on coat"
    ),
    MAJOR_NPC("Major NPC", 15_000, 7_000, 3_000, "2048", "Unique face"),
    STANDARD_ENEMY(
        "Standard enemy", 8_000, 4_000, 2_000,
        "1024 atlas, shared across variants",
        "GPU-instanced where possible"
    ),
    BOSS("Boss", 25_000, 12_000, 6_000, "2048 x2", "Custom shader details"),
    CIVILIAN("Civilian", 5_000, 2_500, 1_200, "512-1024", ""),
    FIRST_PERSON_ARMS("First-person arms", 8_000, 8_000, 8_000, "1024", "Separate rig");

    val hasLods: Boolean get() = lod1Triangles != lod0Triangles

    fun trianglesAt(lod: Int): Int = when (lod.coerceIn(0, 2)) {
        0 -> lod0Triangles
        1 -> lod1Triangles
        else -> lod2Triangles
    }
}

/** Section 3.3: gear tiers are separate meshes, never baked into the body. */
enum class EquipmentLayer(val displayName: String) {
    HELMET("Helmet"),
    VEST("Vest"),
    BACKPACK("Backpack")
}

object CharacterArt {
    const val RIG_MAX_BONES = 65
    const val PLAYER_BODY_TYPES = 2
    const val PLAYER_FACES = 8
    const val PLAYER_HAIR = 6
    const val PLAYER_UNIFORMS = 6
    const val VOICE_OPTIONS = 4
    const val ENEMY_VARIANTS_MIN = 3
    const val ENEMY_VARIANTS_MAX = 4
    const val CIVILIAN_VARIANTS = 6
    const val RANK_BADGES = 20
    const val INSIGNIA_SWAPS_ON_SHOULDER_TEXTURE = true
    const val FACIAL_BONES_ON_HEROES_ONLY = true
    const val IK_FEET_HANDS_HEAD = true

    fun withinBudget(asset: CharacterAsset): Boolean =
        asset.lod0Triangles >= asset.lod1Triangles && asset.lod1Triangles >= asset.lod2Triangles

    /** Every face/hair/body combination the player creator must be able to build. */
    fun playerFaceCombinations(): Int = PLAYER_BODY_TYPES * PLAYER_FACES * PLAYER_HAIR

    fun enemyVariantRange(): IntRange = ENEMY_VARIANTS_MIN..ENEMY_VARIANTS_MAX

    fun isTargetable(role: CharacterRole): Boolean = role.targetable

    fun gearLayers(): List<String> = EquipmentLayer.entries.map { it.displayName }
}

/** Section 4: the three wear states and their era. */
enum class WearState(val displayName: String, val era: String) {
    CLEAN("Clean", "Modern"),
    WORN("Worn", "Cold War"),
    RUSTED_WOOD("Rusted / wood", "WW1")
}

/**
 * Section 4: weapon art budgets, modular parts, attachment sockets and naming.
 */
object WeaponArt {
    const val FIRST_PERSON_MIN_TRIANGLES = 15_000
    const val FIRST_PERSON_MAX_TRIANGLES = 30_000
    const val WORLD_MIN_TRIANGLES = 3_000
    const val WORLD_MAX_TRIANGLES = 6_000
    const val TEXTURE_SIZE = 2048

    val TEXTURE_MAPS: List<String> = listOf("Albedo", "Metallic/Smoothness", "Normal", "AO")

    /** Parts modelled separately so reload, bolt and attachment animations can drive them. */
    val SEPARATE_PARTS: List<String> = listOf(
        "Magazine", "Bolt", "Slide", "Trigger", "Safety", "Optic mount", "Muzzle", "Stock"
    )

    val ATTACHMENT_SOCKETS: List<String> = listOf(
        "Socket_Optic", "Socket_Muzzle", "Socket_Barrel",
        "Socket_Under", "Socket_Mag", "Socket_Stock"
    )

    val ALWAYS_ON_SOCKETS: List<String> = listOf("Socket_Muzzle_FX", "Socket_Eject")

    fun allSockets(): List<String> = ATTACHMENT_SOCKETS + ALWAYS_ON_SOCKETS

    fun isFirstPersonWithinBudget(triangles: Int): Boolean =
        triangles in FIRST_PERSON_MIN_TRIANGLES..FIRST_PERSON_MAX_TRIANGLES

    fun isWorldWithinBudget(triangles: Int): Boolean =
        triangles in WORLD_MIN_TRIANGLES..WORLD_MAX_TRIANGLES

    fun meshName(weaponClass: String, name: String, lod: Int): String =
        "WPN_${weaponClass.uppercase()}_${name}_LOD${lod.coerceAtLeast(0)}"

    fun textureName(name: String, map: String): String = "T_WPN_${name}_$map"

    /** Section 4 legal rule: original designs, generic names, no brand marks. */
    fun isShippingSafe(designName: String): Boolean =
        designName.isNotBlank() && designName.none { it == '\u2122' || it == '\u00A9' || it == '\u00AE' }

    fun camoSkinsTintMasked(): Boolean = true
}
