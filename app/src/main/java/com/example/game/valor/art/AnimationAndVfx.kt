package com.example.game.valor.art

/** Section 6.1: the player locomotion blend tree. */
enum class LocomotionClip(val displayName: String) {
    IDLE("idle"),
    WALK("walk"),
    JOG("jog"),
    SPRINT("sprint"),
    STRAFE_8_WAY("strafe 8-way"),
    CROUCH("crouch"),
    CROUCH_WALK("crouch-walk"),
    PRONE("prone (optional)"),
    TURN_IN_PLACE("turn-in-place")
}

/** Section 6.1: traversal and cover entry. */
enum class TraversalClip(val displayName: String) {
    JUMP_LAND("jump/land"),
    VAULT_LOW("vault low"),
    VAULT_HIGH("vault high"),
    CLIMB_LADDER("climb ladder"),
    SLIDE_INTO_COVER("slide-into-cover")
}

/** Section 6.1: weapon sets, one full clip set per class. */
enum class WeaponClass(val displayName: String) {
    PISTOL("pistol"),
    RIFLE("rifle"),
    SMG("SMG"),
    LMG("LMG"),
    SHOTGUN("shotgun"),
    SNIPER("sniper"),
    LAUNCHER("launcher")
}

/** Section 6.2: the enemy animation set. */
enum class EnemyClip(val displayName: String) {
    PATROL_WALK("patrol walk"),
    ALERT_LOOK_AROUND("alert look-around"),
    RUN("run"),
    COMBAT_STRAFE("combat strafe"),
    COVER_ENTER("cover enter"),
    COVER_PEEK("cover peek"),
    COVER_BLIND_FIRE("cover blind-fire"),
    COVER_EXIT("cover exit"),
    THROW_GRENADE("throw grenade"),
    RELOAD("reload"),
    MELEE_RUSHER("melee rusher"),
    SNIPER_AIM("sniper aim"),
    SNIPER_IDLE("sniper idle"),
    HEAVY_SLOW_WALK("heavy slow-walk"),
    MEDIC_HEAL("medic heal"),
    HIT_REACTION("hit reactions"),
    DEATH("death variants"),
    SURRENDER("surrender (optional)"),
    EXPLOSION_FLINCH("flinch from explosion")
}

object AnimationLibrary {

    /** idle, aim, fire additive, reload tactical, reload empty, swap, sprint pose, ADS walk. */
    val WEAPON_CLIPS_PER_CLASS: List<String> = listOf(
        "idle",
        "aim",
        "fire (additive)",
        "reload (tactical)",
        "reload (empty)",
        "swap in/out",
        "sprint pose",
        "ADS walk"
    )

    val MELEE_CLIPS: List<String> = listOf("slash", "stab", "stealth takedown (front)", "stealth takedown (back)")
    val GRENADE_CLIPS: List<String> = listOf("throw overhand", "throw underhand", "cook")
    val UTILITY_CLIPS: List<String> = listOf(
        "heal (medkit)", "heal (bandage)", "revive ally", "carry body",
        "interact", "radio call", "salute (rank-up cutscene)"
    )

    const val DEATH_VARIANTS = 6
    const val RAGDOLL_TRANSITION = true
    const val DOWNED_CRAWL = true
    const val CINEMATIC_ACTING = true

    /** Section 6.3 quality rules. */
    const val ROOT_MOTION_GAMEPLAY = false
    val ROOT_MOTION_EXEMPT_CLIPS: Set<String> = setOf("takedown", "vault")
    const val PROCEDURAL_RECOIL_LAYERED = true
    const val FOOT_SLIDE_CLEANUP = true

    val ANIMATION_EVENTS: List<String> = listOf(
        "footstep",
        "reload steps (mag out / mag in / chamber)",
        "shell eject",
        "melee hit frame",
        "throw release"
    )

    val RETARGET_SOURCES: String = "Mixamo (retarget), Asset Store packs, or custom"

    fun weaponClipCount(): Int = WeaponClass.entries.size * WEAPON_CLIPS_PER_CLASS.size

    fun keepsRootMotion(clipName: String): Boolean =
        ROOT_MOTION_EXEMPT_CLIPS.any { clipName.contains(it, ignoreCase = true) }

    fun enemyClipCount(): Int = EnemyClip.entries.size
}

/**
 * Section 7: the effect list with its stated particle budget and pool size.
 * A budget of 0 with a pool of 0 means the effect is UI-only or preset-scaled.
 */
enum class VfxEffect(
    val displayName: String,
    val particleBudget: Int,
    val poolSize: Int,
    val notes: String
) {
    MUZZLE_FLASH("Muzzle flash", 12, 0, "2-3 sprite variants + short light; smaller with suppressor"),
    SHELL_EJECTION("Shell ejection", 1, 30, "Physics-lite brass casing, sound on landing"),
    TRACERS("Tracers", 1, 40, "Stretched line with fade, every 3rd round (or tracer ammo)"),
    BULLET_IMPACTS("Bullet impacts", 20, 60, "Dust, sparks, splinters, mud splash, water; decal pool fade"),
    BLOOD("Blood (toggle)", 15, 0, "Small spray + decal; off in Reduced violence mode"),
    EXPLOSION("Explosion", 120, 0, "Fireball, smoke column, debris, shockwave, camera shake, light flash"),
    SMOKE_GRENADE("Smoke grenade", 100, 0, "Volume-like layered billboards, blocks vision"),
    FIRE("Fire", 60, 0, "Looping flame + embers + light flicker"),
    WEATHER("Weather", 0, 0, "Rain, fog, snow, sandstorm; scaled by preset"),
    DRONE_STRIKE("Drone strike", 150, 0, "Targeting laser, missile trail, impact, swarm sparks"),
    HIT_MARKER("Hit marker / kill confirm", 0, 0, "UI-only"),
    AMBIENT("Dust motes, breath fog, heat haze", 30, 0, "Cheap ambient layers");

    val isUiOnly: Boolean get() = this == HIT_MARKER
    val isPresetScaled: Boolean get() = this == WEATHER
}

object VfxLibrary {

    const val ALL_POOLED = true
    const val LOD_BY_DISTANCE = true
    const val SHADOWS_ON_PARTICLES = false
    const val GPU_INSTANCING = true
    const val HEAVY_REFRACTION_ALLOWED_ON_LOW = false
    const val TRACER_EVERY_NTH_ROUND = 3
    const val DECAL_POOL = 60

    /**
     * Weather is the one entry with no fixed ceiling: its emitter count follows the
     * quality preset, so a preset-scaled effect reports budget 0 until the preset
     * supplies the number.
     */
    fun fixedBudget(effect: VfxEffect): Int = if (effect.isPresetScaled) 0 else effect.particleBudget

    fun bloodEnabled(reducedViolence: Boolean): Boolean = !reducedViolence

    fun muzzleFlashScale(suppressed: Boolean): Float = if (suppressed) 0.5f else 1f
}
