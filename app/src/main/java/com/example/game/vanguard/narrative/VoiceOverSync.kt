package com.example.game.vanguard.narrative

/**
 * Section 2 Narrative: VoiceOverSync.
 *
 * Subtitles are driven by the same clock as the audio, so a line cannot drift
 * away from its caption. Playback rate is bounded because pitch-shifting a
 * performance too far breaks the scene, and a line cannot be skipped before
 * the first half second or players would cut every sentence in half.
 */
data class VoiceCue(
    val id: String,
    val lineId: String,
    val speaker: String,
    val subtitle: String,
    val startSeconds: Float,
    val durationSeconds: Float
) {
    val endSeconds: Float get() = startSeconds + durationSeconds
}

object VoiceOverSync {

    const val MIN_PLAYBACK_RATE = 0.85f
    const val MAX_PLAYBACK_RATE = 1.15f
    const val SKIP_LOCK_SECONDS = 0.5f

    /** The cue that owns a given moment, or null between cues. */
    fun activeCue(cues: List<VoiceCue>, elapsedSeconds: Float): VoiceCue? =
        cues.firstOrNull { elapsedSeconds >= it.startSeconds && elapsedSeconds < it.endSeconds }

    fun subtitleAt(cues: List<VoiceCue>, elapsedSeconds: Float): String =
        activeCue(cues, elapsedSeconds)?.subtitle ?: ""

    fun supportsPlaybackRate(rate: Float): Boolean = rate in MIN_PLAYBACK_RATE..MAX_PLAYBACK_RATE

    /** Unsupported rates fall back to 1.0x instead of desynchronising the mix. */
    fun playbackRate(rate: Float): Float = if (supportsPlaybackRate(rate)) rate else 1f

    fun finishSeconds(cue: VoiceCue, rate: Float): Float = cue.durationSeconds / playbackRate(rate)

    fun isSkippable(cue: VoiceCue, elapsedSeconds: Float): Boolean =
        elapsedSeconds - cue.startSeconds >= SKIP_LOCK_SECONDS

    /**
     * True when no two cues claim the same instant, which is the condition a
     * mixer needs before it can play dialogue without dropping a line.
     */
    fun isGapless(cues: List<VoiceCue>): Boolean {
        val ordered = cues.sortedBy { it.startSeconds }
        return ordered.zipWithNext().all { (first, second) -> second.startSeconds >= first.endSeconds }
    }
}
