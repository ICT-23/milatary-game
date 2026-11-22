package com.example.game.tactical.render

import com.example.game.tactical.combat.HitboxComponent

/**
 * Section 6, VFX: material-specific impact decals, particle emitters and hit
 * confirmation markers.
 *
 * The effect is chosen from the surface the ray actually struck, and the marker
 * from the hitbox segment the bullet actually intersected - nothing is guessed.
 */
enum class ImpactSurface(
    val displayName: String,
    val decalId: String,
    val particleId: String,
    val sfxId: String,
    val sparkCount: Int,
    val decalLifetimeSeconds: Float
) {
    METAL("Metal", "decal_metal_pit", "fx_sparks", "sfx_ricochet_metal", 12, 25f),
    CONCRETE("Concrete", "decal_concrete_dust", "fx_dust_puff", "sfx_impact_concrete", 3, 18f),
    DIRT("Terrain", "decal_dirt_plume", "fx_dirt_spray", "sfx_impact_dirt", 0, 14f),
    WOOD("Wood", "decal_wood_split", "fx_splinters", "sfx_impact_wood", 2, 20f),
    FLESH("Flesh", "decal_blood_spray", "fx_blood_mist", "sfx_hit_flesh", 0, 10f),
    WATER("Water", "decal_water_ring", "fx_water_splash", "sfx_impact_water", 0, 3f)
}

data class ImpactEffect(
    val surface: ImpactSurface,
    val decalId: String,
    val particleId: String,
    val sfxId: String,
    val sparkCount: Int,
    val lifetimeSeconds: Float
)

object ImpactVfx {

    const val HEADSHOT_MARKER = "HEADSHOT"
    const val BODY_MARKER = "HIT"
    const val LIMB_MARKER = "LIMB HIT"
    const val KILL_MARKER = "KILL"
    const val MARKER_SECONDS = 0.35f

    fun effectFor(surface: ImpactSurface): ImpactEffect = ImpactEffect(
        surface = surface,
        decalId = surface.decalId,
        particleId = surface.particleId,
        sfxId = surface.sfxId,
        sparkCount = surface.sparkCount,
        lifetimeSeconds = surface.decalLifetimeSeconds
    )

    /** Maps a PBR material id onto the impact family it should spawn. */
    fun surfaceFor(materialId: String): ImpactSurface = when (materialId) {
        PbrMaterials.WEAPON_ALLOY.id, PbrMaterials.HIGH_GRADE_STEEL.id -> ImpactSurface.METAL
        PbrMaterials.CONCRETE.id -> ImpactSurface.CONCRETE
        PbrMaterials.TERRAIN_DIRT.id -> ImpactSurface.DIRT
        else -> ImpactSurface.CONCRETE
    }

    /** The floating confirmation marker, per Section 6. */
    fun hitMarker(component: HitboxComponent, isKill: Boolean): String = when {
        isKill && component == HitboxComponent.HEAD -> "$KILL_MARKER / $HEADSHOT_MARKER"
        isKill -> KILL_MARKER
        component == HitboxComponent.HEAD -> HEADSHOT_MARKER
        component == HitboxComponent.LIMBS -> LIMB_MARKER
        else -> BODY_MARKER
    }

    /**
     * Directional hit reaction: the ragdoll blend that plays on the victim, in
     * degrees away from the shot vector.
     */
    fun hitReactionDegrees(component: HitboxComponent): Float = when (component) {
        HitboxComponent.HEAD -> 35f
        HitboxComponent.THORAX -> 22f
        HitboxComponent.STOMACH -> 15f
        HitboxComponent.LIMBS -> 10f
    }
}
