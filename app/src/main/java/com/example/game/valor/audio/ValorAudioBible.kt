package com.example.game.valor.audio

import com.example.game.valor.art.ArtAct
import kotlin.math.pow

/**
 * Section 9.5: the mix buses and their nominal levels.
 */
enum class MixBus(val displayName: String, val levelDb: Float) {
    MASTER("Master", 0f),
    SFX_WEAPONS("SFX - Weapons", -6f),
    SFX_IMPACTS("SFX - Impacts/Env", -10f),
    VOICE("Voice", -5f),
    MUSIC("Music", -14f),
    AMBIENCE("Ambience", -18f),
    UI("UI", -8f)
}

/**
 * Section 9.2 categories; every entry lists the sounds the bible requires.
 */
enum class SfxCategory(val displayName: String, vararg val sounds: String) {
    WEAPONS(
        "Weapons",
        "close fire", "distant fire", "indoor/outdoor tail", "suppressed",
        "dry-fire", "reload steps (mag out/in/bolt)", "swap", "empty click"
    ),
    IMPACTS(
        "Impacts",
        "per surface (12) x bullet/explosion", "ricochet whizz-by", "flesh hit", "headshot tick"
    ),
    EXPLOSIONS("Explosions", "small", "medium", "large", "debris rain", "ringing tinnitus"),
    MOVEMENT("Movement", "footsteps per surface (walk/run/crouch)", "gear rattle", "vault/climb", "body fall"),
    PLAYER("Player", "breath", "heartbeat low-health", "hit grunts", "heal", "reload effort"),
    ENEMIES("Enemies", "callouts", "hit/death", "grenade warning", "reload", "radio"),
    VEHICLES("Vehicles", "jeep", "tank", "helicopter", "boat", "drone buzz"),
    UI("UI", "click", "confirm", "deny", "rank-up fanfare", "medal jingle", "cash", "craft anvil", "upgrade"),
    AMBIENCE("Ambience", "wind", "distant artillery", "birds", "rain", "city hum", "ocean", "radio static")
}

/** Section 9.3: one act's score, rendered as three interchangeable layers. */
data class MusicTheme(
    val act: ArtAct,
    val style: String,
    val layers: List<String>
)

object ValorAudioBible {

    const val LIMITER_DB = -1f
    const val MUSIC_DUCK_DB = 6f
    const val MAX_VOICES = 32
    const val LOW_END_VOICES = 20
    const val LOGARITHMIC_ROLLOFF = true
    const val OCCLUSION_LOWPASS = true
    const val REVERB_ZONES_INDOORS = true
    const val RADIO_FILTER = true
    const val DISTANCE_MUFFLING = true

    const val VARIATION_MIN = 3
    const val VARIATION_MAX = 5
    const val PITCH_VARIATION = 0.05f

    const val STEMS_MIN = 3
    const val STEMS_MAX = 4
    const val CROSSFADE_SECONDS = 2f
    const val FADE_OUT_ON_CUTSCENE = true

    val MIXER_SETTINGS: List<String> = listOf(
        "master", "music", "SFX", "voice", "mono option", "subtitles", "headphone spatial mode"
    )

    val THEMES: List<MusicTheme> = listOf(
        MusicTheme(ArtAct.BOOT_CAMP, "Orchestral march, hopeful", listOf("Calm", "Training", "Success")),
        MusicTheme(ArtAct.GREAT_WAR, "Low strings, war drums, somber", listOf("Tension", "Combat", "Loss")),
        MusicTheme(ArtAct.INTERWAR, "Noir jazz hints + strings", listOf("Stealth", "Chase", "Duel")),
        MusicTheme(ArtAct.WORLD_AT_WAR, "Epic orchestral, brass", listOf("Approach", "Combat", "Victory")),
        MusicTheme(ArtAct.COLD_SHADOWS, "Ambient synth + percussion", listOf("Recon", "Raid", "Betrayal")),
        MusicTheme(ArtAct.DRONE_AGE, "Electronic orchestral hybrid", listOf("Hacking", "Swarm", "Final boss"))
    )

    fun dbToGain(db: Float): Float = 10f.pow(db / 20f)

    /** Voice ducks the music bus by 6 dB while a line is speaking. */
    fun levelFor(bus: MixBus, voiceActive: Boolean = false): Float =
        if (bus == MixBus.MUSIC && voiceActive) bus.levelDb - MUSIC_DUCK_DB else bus.levelDb

    fun gainFor(bus: MixBus, voiceActive: Boolean = false): Float = dbToGain(levelFor(bus, voiceActive))

    fun clampToLimiter(db: Float): Float = db.coerceAtMost(LIMITER_DB)

    fun maxVoices(lowEndDevice: Boolean): Int = if (lowEndDevice) LOW_END_VOICES else MAX_VOICES

    /**
     * Five deterministic variants spread across the permitted +/-5% pitch window,
     * so a repeat never sounds mechanically identical.
     */
    fun variationPitch(variantIndex: Int): Float {
        val slots = VARIATION_MAX
        val offset = variantIndex.mod(slots) - slots / 2
        return 1f + offset * (PITCH_VARIATION / (slots / 2))
    }

    fun requiredVariantCount(): IntRange = VARIATION_MIN..VARIATION_MAX

    fun themeFor(act: ArtAct): MusicTheme = THEMES.first { it.act == act }

    fun stemsFor(act: ArtAct): List<String> = themeFor(act).layers

    fun voClipName(character: String, act: Int, scene: String, line: String): String =
        "VO_${character}_A${act}_${scene}_${line}"

    fun busCount(): Int = MixBus.entries.size

    fun categoryCount(): Int = SfxCategory.entries.size
}
