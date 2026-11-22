package com.example.game.tactical.academy

import com.example.game.combat.Vec3
import com.example.game.tactical.render.LightingSettings
import com.example.game.tactical.render.PbrMaterials
import com.example.game.tactical.render.PostProcessing
import com.example.game.tactical.render.QualityPreset
import com.example.game.tactical.render.RenderPipeline

/**
 * Phase 1 deliverable: the Fort Valor Academy Hub environment loader.
 *
 * Four zones - Barracks, Armory, Firing Range and Tactical Briefing Room - each
 * with a spawn point, a PBR material set and the collision meshes the character
 * controller sweeps against.
 */
data class CollisionMesh(
    val id: String,
    val center: Vec3,
    val sizeMeters: Vec3,
    val isCover: Boolean = false
) {
    val isWalkable: Boolean get() = sizeMeters.y <= 0.35f
}

data class HubZone(
    val id: String,
    val displayName: String,
    val description: String,
    val spawn: Vec3,
    val materialIds: List<String>,
    val collisions: List<CollisionMesh>
)

data class AcademyScene(
    val zones: List<HubZone>,
    val spawn: Vec3,
    val lighting: LightingSettings,
    val post: PostProcessing,
    val quality: QualityPreset
)

object AcademyHub {

    val BARRACKS = HubZone(
        id = "barracks",
        displayName = "Barracks",
        description = "Rookie squad bunks, kit lockers and the duty board",
        spawn = Vec3(0f, 0f, 0f),
        materialIds = listOf(PbrMaterials.CONCRETE.id, PbrMaterials.POLYMER.id),
        collisions = listOf(
            CollisionMesh("bunk_row_a", Vec3(-4f, 0.45f, 2f), Vec3(1.9f, 0.9f, 0.9f)),
            CollisionMesh("kit_locker", Vec3(4.2f, 1.0f, 2.4f), Vec3(0.6f, 2.0f, 1.2f), isCover = true),
            CollisionMesh("barracks_floor", Vec3(0f, -0.1f, 0f), Vec3(14f, 0.2f, 10f))
        )
    )

    val ARMORY = HubZone(
        id = "armory",
        displayName = "Armory",
        description = "Gunsmith workbench, material store and blueprint vault",
        spawn = Vec3(12f, 0f, 0f),
        materialIds = listOf(PbrMaterials.HIGH_GRADE_STEEL.id, PbrMaterials.WEAPON_ALLOY.id),
        collisions = listOf(
            CollisionMesh("workbench", Vec3(12f, 0.5f, 1.6f), Vec3(2.4f, 1.0f, 0.8f), isCover = true),
            CollisionMesh("material_crate", Vec3(10.4f, 0.4f, -1.2f), Vec3(1f, 0.8f, 1f), isCover = true),
            CollisionMesh("armory_floor", Vec3(12f, -0.1f, 0f), Vec3(12f, 0.2f, 10f))
        )
    )

    val FIRING_RANGE = HubZone(
        id = "firing_range",
        displayName = "Firing Range",
        description = "Pop-up target lanes and the qualification booth",
        spawn = Vec3(24f, 0f, 0f),
        materialIds = listOf(PbrMaterials.CONCRETE.id, PbrMaterials.TERRAIN_DIRT.id),
        collisions = listOf(
            CollisionMesh("firing_line", Vec3(24f, 0.6f, 0f), Vec3(0.3f, 1.2f, 8f), isCover = true),
            CollisionMesh("target_butts", Vec3(24f, 1.5f, -18f), Vec3(10f, 3f, 0.6f), isCover = true),
            CollisionMesh("range_floor", Vec3(24f, -0.1f, -9f), Vec3(12f, 0.2f, 20f))
        )
    )

    val BRIEFING_ROOM = HubZone(
        id = "briefing_room",
        displayName = "Tactical Briefing Room",
        description = "Map table, mission board and the campaign dialogue stage",
        spawn = Vec3(36f, 0f, 0f),
        materialIds = listOf(PbrMaterials.CONCRETE.id, PbrMaterials.WEAPON_ALLOY.id),
        collisions = listOf(
            CollisionMesh("map_table", Vec3(36f, 0.45f, 1.8f), Vec3(3f, 0.9f, 1.6f), isCover = true),
            CollisionMesh("briefing_floor", Vec3(36f, -0.1f, 0f), Vec3(10f, 0.2f, 10f))
        )
    )

    val ZONES: List<HubZone> = listOf(BARRACKS, ARMORY, FIRING_RANGE, BRIEFING_ROOM)

    val DEFAULT_SPAWN: Vec3 = BARRACKS.spawn

    fun zoneAt(id: String): HubZone? = ZONES.firstOrNull { it.id == id }

    /** The zone whose walkable floor contains [position], nearest spawn on a tie. */
    fun zoneContaining(position: Vec3): HubZone? = ZONES.minByOrNull { it.spawn.distanceTo(position) }

    /**
     * Solid meshes whose axis-aligned bounds overlap the swept sphere. The
     * character controller treats a non-empty result as a blocked step.
     */
    fun blockages(position: Vec3, radiusMeters: Float): List<CollisionMesh> {
        val zone = zoneContaining(position) ?: return emptyList()
        return zone.collisions
            .filter { !it.isWalkable }
            .filter { mesh ->
                val dx = kotlin.math.abs(position.x - mesh.center.x) <= mesh.sizeMeters.x / 2f + radiusMeters
                val dy = kotlin.math.abs(position.y - mesh.center.y) <= mesh.sizeMeters.y / 2f + radiusMeters
                val dz = kotlin.math.abs(position.z - mesh.center.z) <= mesh.sizeMeters.z / 2f + radiusMeters
                dx && dy && dz
            }
    }

    /** Cover probes the cover system receives when the character is in a zone. */
    fun coverMeshes(zoneId: String): List<CollisionMesh> =
        zoneAt(zoneId)?.collisions?.filter { it.isCover }.orEmpty()

    /** Loads the hub at the requested visual preset. */
    fun load(quality: QualityPreset): AcademyScene = AcademyScene(
        zones = ZONES,
        spawn = DEFAULT_SPAWN,
        lighting = RenderPipeline.lighting(quality),
        post = RenderPipeline.post(quality),
        quality = quality
    )
}
