package com.example.game.valor.gdd

/** Section 3.1 role column. */
enum class StoryRole(val displayName: String) {
    PROTAGONIST("Protagonist"),
    MENTOR("Mentor"),
    SQUADMATE("Squadmate / sniper"),
    RIVAL("Rival"),
    COMMAND("Commanding officer"),
    ANTAGONIST("Antagonist"),
    AI_SYSTEM("AI drone system")
}

/**
 * Section 3.1: one cast row. [notes] keeps the sentence the document prints, so
 * the dossier screen and the story beats cannot describe a character differently.
 */
data class StoryCharacter(
    val id: String,
    val name: String,
    val role: StoryRole,
    val notes: String,
    val playable: Boolean = false
)

object StoryCast {

    const val MERCER = "mercer"
    const val BROOKS = "brooks"
    const val HARTWELL = "hartwell"
    const val VANCE = "vance"
    const val WHITFIELD = "whitfield"
    const val KESSLER = "kessler"
    const val ARIA = "aria"

    val CHARACTERS: List<StoryCharacter> = listOf(
        StoryCharacter(
            MERCER, "Cadet Jack \"Ace\" Mercer", StoryRole.PROTAGONIST,
            "Farm-town kid, joins the academy. Player picks face, skin tone, hair, voice.",
            playable = true
        ),
        StoryCharacter(
            BROOKS, "Drill Sergeant Walter \"Iron\" Brooks", StoryRole.MENTOR,
            "Tough, fair, trains you. His fate is a major emotional beat."
        ),
        StoryCharacter(
            HARTWELL, "Cadet Lena Hartwell", StoryRole.SQUADMATE,
            "Calm, precise; best friend."
        ),
        StoryCharacter(
            VANCE, "Cadet Marcus Vance", StoryRole.RIVAL,
            "Arrogant, wealthy family. Starts as rival, becomes ally or enemy based on player choices."
        ),
        StoryCharacter(
            WHITFIELD, "General Eleanor Whitfield", StoryRole.COMMAND,
            "Strategist, gives high-level missions."
        ),
        StoryCharacter(
            KESSLER, "\"The Warden\" (Col. Dmitri Kessler)", StoryRole.ANTAGONIST,
            "Ruthless enemy commander who appears in every era (different ages / allies)."
        ),
        StoryCharacter(
            ARIA, "ARIA", StoryRole.AI_SYSTEM,
            "AI drone system (modern era). Voice of the drone/tech interface."
        )
    )

    fun byId(id: String): StoryCharacter? = CHARACTERS.firstOrNull { it.id == id }

    fun ofRole(role: StoryRole): List<StoryCharacter> = CHARACTERS.filter { it.role == role }

    /** The only character the player controls, per Section 3.1. */
    fun playable(): List<StoryCharacter> = CHARACTERS.filter { it.playable }
}

/**
 * Section 3.2: one act of the campaign. [tutorials] is only filled for Act 1,
 * [choices] names the decisions the document calls out, [boss] the boss mission.
 */
data class StoryActSpec(
    val number: Int,
    val title: String,
    val openingRank: String,
    val closingRank: String,
    val beats: List<String>,
    val tutorials: List<String> = emptyList(),
    val choices: List<String> = emptyList(),
    val boss: String? = null
)

object Campaign {

    const val ACT_COUNT = 6

    val ACTS: List<StoryActSpec> = listOf(
        StoryActSpec(
            1, "Boot Camp", "Recruit", "Private",
            listOf(
                "Jack arrives at Fort Valor Academy; meets Brooks, Lena, Vance",
                "Graduation exercise where Vance cheats"
            ),
            tutorials = listOf(
                "Obstacle course", "Rifle range", "Grenade range", "First aid", "Radio"
            ),
            choices = listOf("Report Vance for cheating or stay silent (affects the rivalry arc)")
        ),
        StoryActSpec(
            2, "The Great War", "Private", "Sergeant",
            listOf(
                "Trench warfare: mud, fog, gas-alert sequences, bolt-action rifles, early machine guns",
                "Squad deployed to a front-line sector",
                "Brooks is wounded saving the squad",
                "First meeting with Kessler as a young officer of the opposing force"
            ),
            boss = "Breakthrough of a fortified trench line"
        ),
        StoryActSpec(
            3, "Interwar & Rising Storm", "Sergeant", "Lieutenant",
            listOf(
                "Jack is selected for Officer Candidate School; learns leadership, commands small units",
                "Spy / stealth missions, escort missions, a train-heist chase",
                "Vance decision pays off (ally helps / rival sabotages)"
            )
        ),
        StoryActSpec(
            4, "World War", "Lieutenant", "Major",
            listOf(
                "Beach landing, forest ambush, city street fighting, tank support, air raid defense",
                "Lena makes a sacrifice play OR survives based on the player's choices",
                "Kessler returns as a seasoned general"
            ),
            boss = "Assault on a fortress bunker; Medal of Honor nomination event"
        ),
        StoryActSpec(
            5, "Cold Shadows", "Major", "Colonel",
            listOf(
                "Desert and snow missions, helicopters, night vision, thermal scopes",
                "Betrayal: someone inside the command leaks plans",
                "Player investigates through a light detective / choice sequence"
            )
        ),
        StoryActSpec(
            6, "Drone Age & Final Stand", "Colonel", "General / Commander",
            listOf(
                "ARIA introduced; the player calls drone strikes and manages soldiers plus drones",
                "Kessler's final plan: seize a drone network",
                "Final mission = multi-stage infiltration + boss duel",
                "Ending: the player receives the Medal of Honor"
            ),
            boss = "Kessler final: drone swarm + arena fight"
        )
    )

    /** Section 3.2: the three documented endings and what selects them. */
    val ENDINGS: List<Ending> = Ending.entries

    fun actFor(number: Int): StoryActSpec? = ACTS.firstOrNull { it.number == number }

    fun bossActs(): List<StoryActSpec> = ACTS.filter { it.boss != null }

    /** Every tutorial named in Act 1, in the order the act lists them. */
    fun tutorials(): List<String> = ACTS.flatMap { it.tutorials }
}

/** Section 3.3: how the story reaches the player. */
enum class DeliveryChannel(val displayName: String, val detail: String) {
    CUTSCENE("In-engine cutscene", "30-60 s each, voiced dialogue, subtitles"),
    BRIEFING("Mission briefing", "3D map table with objectives"),
    LETTER("Letter from home", "Emotional, short, between acts"),
    DIALOGUE("Dialogue choice", "2-3 options that move the alignment meters")
}

object StoryDelivery {
    const val CUTSCENE_MIN_SECONDS = 30
    const val CUTSCENE_MAX_SECONDS = 60
    const val CHOICE_MIN_OPTIONS = 2
    const val CHOICE_MAX_OPTIONS = 3
    val CHANNELS: List<DeliveryChannel> = DeliveryChannel.entries
}

/** Section 3.3: the three meters a dialogue choice moves. */
enum class AlignmentMeter(val displayName: String) {
    LOYALTY("Loyalty"),
    DIPLOMACY("Diplomacy"),
    AGGRESSION("Aggression")
}

/**
 * Section 3.3: the running totals behind the endings. Choices only ever add, and
 * the ending reads the strongest meter, so a run can be reasoned about after the
 * fact instead of being decided by hidden state.
 */
data class AlignmentState(
    val loyalty: Int = 0,
    val diplomacy: Int = 0,
    val aggression: Int = 0
) {
    val total: Int get() = loyalty + diplomacy + aggression

    val dominant: AlignmentMeter
        get() = when {
            loyalty >= diplomacy && loyalty >= aggression -> AlignmentMeter.LOYALTY
            diplomacy >= aggression -> AlignmentMeter.DIPLOMACY
            else -> AlignmentMeter.AGGRESSION
        }

    fun score(meter: AlignmentMeter): Int = when (meter) {
        AlignmentMeter.LOYALTY -> loyalty
        AlignmentMeter.DIPLOMACY -> diplomacy
        AlignmentMeter.AGGRESSION -> aggression
    }

    fun shift(meter: AlignmentMeter, amount: Int): AlignmentState = when (meter) {
        AlignmentMeter.LOYALTY -> copy(loyalty = loyalty + amount)
        AlignmentMeter.DIPLOMACY -> copy(diplomacy = diplomacy + amount)
        AlignmentMeter.AGGRESSION -> copy(aggression = aggression + amount)
    }
}

/** Section 3.2 ending list, keyed to the meter that selects it. */
enum class Ending(
    val displayName: String,
    val trigger: String,
    val epilogue: String,
    val meter: AlignmentMeter
) {
    SOLDIERS_HONOR(
        "Soldier's Honor", "High loyalty",
        "Stays in command and becomes Chief of Staff",
        AlignmentMeter.LOYALTY
    ),
    STATESMAN(
        "Statesman", "High diplomacy",
        "Enters public life, with an optional road-to-President epilogue",
        AlignmentMeter.DIPLOMACY
    ),
    LONE_WOLF(
        "Lone Wolf", "High risk choices",
        "Retires as a legend; secret mission after the credits",
        AlignmentMeter.AGGRESSION
    );

    companion object {
        /** The ending the strongest meter selects; ties fall to the doc's first row. */
        fun resolve(state: AlignmentState): Ending =
            entries.first { it.meter == state.dominant }

        fun forMeter(meter: AlignmentMeter): Ending = entries.first { it.meter == meter }
    }
}

/** Section 3.2: a decision the document names, with only its printed options. */
data class StoryChoice(val act: Int, val scene: String, val options: List<String>)

object StoryChoices {

    val CHOICES: List<StoryChoice> = listOf(
        StoryChoice(
            1, "Graduation exercise",
            listOf("Report Vance for cheating", "Stay silent")
        )
    )

    fun forAct(act: Int): List<StoryChoice> = CHOICES.filter { it.act == act }

    /** Section 3.3 caps every choice at 2-3 options. */
    fun optionsInRange(choice: StoryChoice): Boolean =
        choice.options.size in StoryDelivery.CHOICE_MIN_OPTIONS..StoryDelivery.CHOICE_MAX_OPTIONS
}
