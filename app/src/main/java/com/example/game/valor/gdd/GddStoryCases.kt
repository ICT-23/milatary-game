package com.example.game.valor.gdd

import com.example.game.vanguard.CaseRecorder
import com.example.game.vanguard.ContractCase
import com.example.game.vanguard.observed

/** Section 3: the cast, the act structure, the delivery channels and the endings. */
object GddStoryCases {

    fun run(): List<ContractCase> {
        val r = CaseRecorder(ValorGddAudit.GROUP_STORY)

        r.check(
            "Cast size", "7 characters",
            StoryCast.CHARACTERS.size == 7, observed(StoryCast.CHARACTERS.size, 7)
        )
        r.check(
            "Playable character", "Mercer is the only playable lead",
            StoryCast.playable().map { it.id } == listOf(StoryCast.MERCER),
            observed(StoryCast.playable().map { it.id }, "[mercer]")
        )
        r.check(
            "Cast roles", "one character per role",
            StoryRole.entries.all { role -> StoryCast.ofRole(role).size == 1 },
            observed(StoryRole.entries.map { StoryCast.ofRole(it).size }, "[1, 1, 1, 1, 1, 1, 1]")
        )
        r.check(
            "Act count", "6 acts numbered 1-6",
            Campaign.ACTS.size == 6 && Campaign.ACTS.map { it.number } == (1..6).toList(),
            observed(Campaign.ACTS.map { it.number }, "[1..6]")
        )
        r.check(
            "Act 1 tutorials", "obstacle course, rifle range, grenade range, first aid, radio",
            Campaign.tutorials() == listOf(
                "Obstacle course", "Rifle range", "Grenade range", "First aid", "Radio"
            ),
            observed(Campaign.tutorials(), "[5 tutorial rows]")
        )
        r.check(
            "Act rank bands", "Recruit->Private ... Colonel->General",
            Campaign.ACTS.map { it.openingRank to it.closingRank } == listOf(
                "Recruit" to "Private",
                "Private" to "Sergeant",
                "Sergeant" to "Lieutenant",
                "Lieutenant" to "Major",
                "Major" to "Colonel",
                "Colonel" to "General / Commander"
            ),
            observed(Campaign.ACTS.map { it.closingRank }, "[Private..General]")
        )
        r.check(
            "Boss acts", "Acts 2, 4 and 6 close on a boss",
            Campaign.bossActs().map { it.number } == listOf(2, 4, 6),
            observed(Campaign.bossActs().map { it.number }, "[2, 4, 6]")
        )
        r.check(
            "Trench line boss", "breakthrough of a fortified trench line",
            Campaign.actFor(2)?.boss == "Breakthrough of a fortified trench line",
            observed(Campaign.actFor(2)?.boss, "Breakthrough of a fortified trench line")
        )
        r.check(
            "Bunker boss", "fortress bunker assault with a Medal of Honor nomination",
            Campaign.actFor(4)?.boss?.contains("Medal of Honor nomination") == true,
            observed(Campaign.actFor(4)?.boss, "contains Medal of Honor nomination")
        )
        r.check(
            "Final boss", "drone swarm plus arena fight",
            Campaign.actFor(6)?.boss == "Kessler final: drone swarm + arena fight",
            observed(Campaign.actFor(6)?.boss, "Kessler final: drone swarm + arena fight")
        )
        r.check(
            "Story delivery", "4 channels, 30-60 s cutscenes",
            StoryDelivery.CHANNELS.size == 4 &&
                StoryDelivery.CUTSCENE_MIN_SECONDS == 30 &&
                StoryDelivery.CUTSCENE_MAX_SECONDS == 60,
            observed(StoryDelivery.CHANNELS.size, 4)
        )
        r.check(
            "Act 1 choice", "report Vance or stay silent",
            StoryChoices.CHOICES.size == 1 &&
                StoryChoices.optionsInRange(StoryChoices.CHOICES.first()) &&
                StoryChoices.CHOICES.first().options ==
                listOf("Report Vance for cheating", "Stay silent"),
            observed(StoryChoices.CHOICES.first().options, "[2 options]")
        )
        r.check(
            "Act 3 payoff", "the Vance decision pays off",
            Campaign.actFor(3)?.beats?.any { it.contains("Vance decision pays off") } == true,
            observed(Campaign.actFor(3)?.beats?.size, "contains Vance payoff")
        )
        r.check(
            "Act 5 betrayal", "someone inside command leaks the plans",
            Campaign.actFor(5)?.beats?.any { it.contains("Betrayal") } == true,
            observed(Campaign.actFor(5)?.beats?.size, "contains Betrayal")
        )
        r.check(
            "Endings", "3 endings selected by loyalty, diplomacy and risk",
            Ending.entries.size == 3 &&
                Ending.entries.map { it.trigger } ==
                listOf("High loyalty", "High diplomacy", "High risk choices"),
            observed(Ending.entries.map { it.trigger }, "[3 triggers]")
        )
        r.check(
            "Loyalty ending", "high loyalty is Soldier's Honor",
            Ending.resolve(AlignmentState(loyalty = 10)) == Ending.SOLDIERS_HONOR,
            observed(Ending.resolve(AlignmentState(loyalty = 10)), Ending.SOLDIERS_HONOR)
        )
        r.check(
            "Diplomacy ending", "high diplomacy is Statesman",
            Ending.resolve(AlignmentState(diplomacy = 10)) == Ending.STATESMAN,
            observed(Ending.resolve(AlignmentState(diplomacy = 10)), Ending.STATESMAN)
        )
        r.check(
            "Risk ending", "high risk is Lone Wolf",
            Ending.resolve(AlignmentState(aggression = 10)) == Ending.LONE_WOLF,
            observed(Ending.resolve(AlignmentState(aggression = 10)), Ending.LONE_WOLF)
        )
        r.check(
            "Tie break", "a tie falls to the first documented ending",
            Ending.resolve(AlignmentState(loyalty = 5, diplomacy = 5, aggression = 5)) ==
                Ending.SOLDIERS_HONOR,
            observed(Ending.resolve(AlignmentState(5, 5, 5)), Ending.SOLDIERS_HONOR)
        )
        r.check(
            "Meter shift", "a choice moves one meter only",
            AlignmentState().shift(AlignmentMeter.LOYALTY, 3) == AlignmentState(loyalty = 3),
            observed(AlignmentState().shift(AlignmentMeter.LOYALTY, 3), AlignmentState(3))
        )
        r.check(
            "Meter total", "the meters add up for the ending screen",
            AlignmentState(loyalty = 2, diplomacy = 3, aggression = 5).total == 10,
            observed(AlignmentState(2, 3, 5).total, 10)
        )

        return r.result()
    }
}
