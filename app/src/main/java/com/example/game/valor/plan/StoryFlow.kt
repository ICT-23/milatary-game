package com.example.game.valor.plan

import com.example.game.valor.gdd.AlignmentMeter
import com.example.game.valor.gdd.AlignmentState
import com.example.game.valor.gdd.Ending
import com.example.game.valor.gdd.StoryCast
import com.example.game.valor.gdd.StoryChoice
import com.example.game.valor.gdd.StoryChoices
import com.example.game.valor.gdd.StoryDelivery

/**
 * Section 5.20: one option of a story choice. An option with no meter moves
 * nothing, which is what "the neutral option" means for a timed decision.
 */
data class StoryOption(
    val text: String,
    val meter: AlignmentMeter? = null,
    val amount: Int = 1
) {
    val neutral: Boolean get() = meter == null
}

/** Section 5.20: a story beat with only the lines the content file authors. */
data class StoryBeat(val id: String, val act: Int, val scene: String, val options: List<StoryOption>)

/** The outcome of one decision: what was recorded and why. */
data class ChoiceResult(
    val recorded: Boolean,
    val option: StoryOption? = null,
    val defaulted: Boolean = false,
    val reason: String? = null
)

object StoryChoicesRules {

    /** The authored choices, read from the design data instead of re-typed here. */
    fun authoredBeats(): List<StoryBeat> = StoryChoices.CHOICES.map { beatFor(it) }

    fun beatFor(choice: StoryChoice): StoryBeat = StoryBeat(
        id = "act${choice.act}.${choice.scene}",
        act = choice.act,
        scene = choice.scene,
        options = choice.options.map { StoryOption(it) }
    )

    /** The option a timed-out decision falls back to. */
    fun neutral(options: List<StoryOption>): StoryOption? = options.firstOrNull { it.neutral }

    /** Section 3.3's 2-3 option cap, reused by the gate. */
    fun withinOptionCap(beat: StoryBeat): Boolean =
        beat.options.size in StoryDelivery.CHOICE_MIN_OPTIONS..StoryDelivery.CHOICE_MAX_OPTIONS
}

/**
 * Section 5.20: `currentAct`, the completed beats, the recorded choices and the
 * three meters. A beat can only play once and a choice only lands on confirm.
 */
class StoryState(initialAct: Int = 1) {

    var currentAct: Int = initialAct
        private set

    var meters: AlignmentState = AlignmentState()
        private set

    private val completedBeats = linkedSetOf<String>()
    private val viewedBeats = linkedSetOf<String>()
    private val recorded = linkedMapOf<String, String>()
    private val history = mutableListOf<String>()

    fun completedBeatIds(): List<String> = completedBeats.toList()

    fun completedBeatCount(): Int = completedBeats.size

    /** Returns true only the first time the beat plays. */
    fun playBeat(id: String): Boolean {
        if (id in completedBeats) return false
        completedBeats += id
        return true
    }

    /** "Skip allowed for cutscenes after first view." */
    fun canSkip(id: String): Boolean = id in viewedBeats

    fun markViewed(id: String) {
        viewedBeats += id
    }

    /**
     * A confirmed selection is recorded; an unconfirmed one records nothing; a
     * timed-out one records the neutral option.
     */
    fun choose(
        beat: StoryBeat,
        selectedIndex: Int?,
        confirmed: Boolean,
        timedOut: Boolean = false
    ): ChoiceResult {
        if (timedOut) {
            val option = StoryChoicesRules.neutral(beat.options)
                ?: return ChoiceResult(false, null, true, "No neutral option authored")
            return record(beat, option, defaulted = true)
        }
        if (!confirmed) return ChoiceResult(false, null, false, "Not confirmed")
        val option = selectedIndex?.let { beat.options.getOrNull(it) }
            ?: return ChoiceResult(false, null, false, "No valid option selected")
        return record(beat, option, defaulted = false)
    }

    private fun record(beat: StoryBeat, option: StoryOption, defaulted: Boolean): ChoiceResult {
        recorded[beat.id] = option.text
        history += "${beat.scene}: ${option.text}"
        option.meter?.let { meters = meters.shift(it, option.amount) }
        return ChoiceResult(true, option, defaulted)
    }

    fun recordedChoice(beatId: String): String? = recorded[beatId]

    fun choiceCount(): Int = recorded.size

    /** "Dialogue history log." */
    fun dialogueHistory(): List<String> = history.toList()

    /** Only forward, so a mission cannot rewind the campaign. */
    fun advanceTo(act: Int): Boolean {
        if (act <= currentAct) return false
        currentAct = act
        return true
    }

    /** Design doc Section 3.2: the strongest meter picks the ending. */
    fun ending(): Ending = Ending.resolve(meters)

    fun snapshot(): SaveDocument = SaveDocument()
        .set(SaveKeys.STORY_CHOICES, SaveCodec.joinMap(recorded))
        .set(
            SaveKeys.METERS,
            listOf(meters.loyalty, meters.diplomacy, meters.aggression).joinToString(",")
        )
}

/** Section 5.20: "all endings reachable (test with meter presets)". */
object EndingRules {

    val PRESETS: List<AlignmentState> = listOf(
        AlignmentState(loyalty = 1),
        AlignmentState(diplomacy = 1),
        AlignmentState(aggression = 1)
    )

    fun reachable(): Set<Ending> = PRESETS.map { Ending.resolve(it) }.toSet()

    fun allReachable(): Boolean = reachable().size == Ending.entries.size

    fun forMeters(state: AlignmentState): Ending = Ending.resolve(state)

    fun label(ending: Ending): String = "${ending.displayName} - ${ending.epilogue}"
}

/**
 * Section 5.20: Brooks, Lena and Vance can only die through the scripted choice,
 * never through a stray bullet.
 */
object StoryCritical {

    val IDS: List<String> = listOf(StoryCast.BROOKS, StoryCast.HARTWELL, StoryCast.VANCE)

    fun isCritical(entityId: String): Boolean = entityId in IDS

    fun blocksLethalDamage(entityId: String, scriptedBeat: Boolean): Boolean =
        isCritical(entityId) && !scriptedBeat

    fun names(): List<String> = IDS.mapNotNull { StoryCast.byId(it)?.name }
}

/**
 * Section 5.20: a critical ally may only die to the scripted beat, so a stray
 * bullet is clamped to leave the target standing instead of refusing the shot.
 */
object StoryProtection {

    const val LETHAL_BLOCKED = true

    fun isProtected(target: Combatant, scriptedBeat: Boolean): Boolean =
        !scriptedBeat && (target.storyCritical || StoryCritical.isCritical(target.id))

    /** Never returns more than the damage that leaves one hit point alive. */
    fun clampLethal(damage: Float, target: Combatant, scriptedBeat: Boolean): Float {
        if (!isProtected(target, scriptedBeat)) return damage
        val survivable = (target.current - 1f).coerceAtLeast(0f)
        return damage.coerceAtMost(survivable)
    }

    fun survivesStrayBullet(target: Combatant): Boolean = target.isAlive
}
