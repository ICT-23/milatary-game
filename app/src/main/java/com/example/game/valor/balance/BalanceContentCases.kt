package com.example.game.valor.balance

import com.example.game.vanguard.CaseRecorder
import com.example.game.vanguard.ContractCase
import com.example.game.vanguard.approxEquals
import com.example.game.vanguard.observed

/** Section 7's 60 missions and the section 8 character table. */
object BalanceContentCases {

    fun run(): List<ContractCase> {
        val missions = CaseRecorder(BalanceAudit.GROUP_MISSIONS)
        missionCases(missions)
        val npcs = CaseRecorder(BalanceAudit.GROUP_NPCS)
        npcCases(npcs)
        return missions.result() + npcs.result()
    }

    private fun missionCases(r: CaseRecorder) {
        r.check(
            "Mission count", "60 missions across 6 acts",
            MissionCatalog.MISSIONS.size == 60 && MissionCatalog.TOTAL_MISSIONS == 60,
            observed(MissionCatalog.MISSIONS.size, 60)
        )
        r.check(
            "Numbering", "1..60 with no gap and no repeat",
            MissionCatalog.isContiguous() && MissionCatalog.MISSIONS.first().id == "m01" &&
                MissionCatalog.MISSIONS.last().id == "m60",
            observed(
                "${MissionCatalog.MISSIONS.first().id}..${MissionCatalog.MISSIONS.last().id}",
                "m01..m60"
            )
        )
        r.check(
            "Ten per act", "every act carries exactly ten missions",
            (1..6).all { MissionCatalog.forAct(it).size == 10 },
            observed((1..6).map { MissionCatalog.forAct(it).size }, "10 x 6")
        )
        r.check(
            "Mission types", "all 13 legend types are used",
            MissionType.entries.size == 13 &&
                MissionCatalog.typeCounts().values.none { it == 0 },
            observed(MissionCatalog.typeCounts().filterValues { it == 0 }.keys, "[]")
        )
        r.check(
            "Boss missions", "5 BOSS rows at 20, 30, 40, 47 and 60",
            MissionCatalog.bossMissions().map { it.number } == listOf(20, 30, 40, 47, 60),
            observed(MissionCatalog.bossMissions().map { it.number }, "[20, 30, 40, 47, 60]")
        )
        val first = MissionCatalog.MISSIONS.first()
        val last = MissionCatalog.MISSIONS.last()
        r.check(
            "First and last row", "Arrival Day 30 XP / 80 cash; Final Duel 12,000 XP / 20,000 cash",
            first.name == "Arrival Day" && first.baseXp == 30 && first.cash == 80 &&
                last.name == "Final Duel: Kessler" && last.baseXp == 12_000 && last.cash == 20_000,
            observed("${first.baseXp}/${last.baseXp}", "30/12000")
        )
        r.check(
            "Act XP matches the rank band", "each act's mission XP is within 25% of its rank span",
            (1..6).all { act ->
                val span = RankTable.actXpSpan(act)
                val total = MissionCatalog.baseXpTotal(act)
                total in (span * 3 / 4)..(span * 5 / 4)
            },
            observed(
                (1..6).map { MissionCatalog.baseXpTotal(it) },
                (1..6).map { RankTable.actXpSpan(it) }
            )
        )
        r.check(
            "Income band fit", "only the finale pays above its act's cash band",
            MissionCatalog.missionsOutsideIncomeBand().map { it.id } == listOf("m60"),
            observed(MissionCatalog.missionsOutsideIncomeBand().map { it.id }, listOf("m60"))
        )
        r.check(
            "Story anchors", "graduation, Brooks, Lena and the ending keep their beats",
            MissionCatalog.byNumber(10)?.unlock?.contains("PFC") == true &&
                MissionCatalog.byNumber(18)?.unlock?.contains("Brooks") == true &&
                MissionCatalog.byNumber(38)?.unlock?.contains("Lena") == true &&
                MissionCatalog.byNumber(60)?.unlock?.contains("New Game+") == true,
            observed(MissionCatalog.byNumber(60)?.unlock, "Endings, New Game+")
        )
        r.check(
            "Kill XP share", "kills add 25-35% on top of base XP",
            approxEquals(MissionCatalog.KILL_XP_MIN_SHARE, 0.25f) &&
                approxEquals(MissionCatalog.KILL_XP_MAX_SHARE, 0.35f),
            observed(
                "${MissionCatalog.KILL_XP_MIN_SHARE}-${MissionCatalog.KILL_XP_MAX_SHARE}",
                "0.25-0.35"
            )
        )
        r.check(
            "Side content", "6 always-available activities",
            MissionCatalog.SIDE_CONTENT.size == 6,
            observed(MissionCatalog.SIDE_CONTENT.map { it.name }, 6)
        )
    }

    private fun npcCases(r: CaseRecorder) {
        r.check(
            "NPC count", "6 rows in section 8",
            NpcCatalog.NPCS.size == 6, observed(NpcCatalog.NPCS.size, 6)
        )
        r.check(
            "Sgt. Brooks", "200 HP, rifle/pistol, StoryCritical",
            NpcCatalog.byId(NpcCatalog.BROOKS_ID)?.hp == 200 &&
                NpcCatalog.byId(NpcCatalog.BROOKS_ID)?.weapon == "Rifle/Pistol" &&
                NpcCatalog.byId(NpcCatalog.BROOKS_ID)?.isStoryCritical == true,
            observed(NpcCatalog.byId(NpcCatalog.BROOKS_ID)?.hp, 200)
        )
        r.check(
            "Lena Hartwell", "120 HP sniper; her fate is the Act 4 choice",
            NpcCatalog.byId(NpcCatalog.LENA_ID)?.hp == 120 &&
                NpcCatalog.byId(NpcCatalog.LENA_ID)?.weapon == "Sniper" &&
                NpcCatalog.byId(NpcCatalog.LENA_ID)?.special?.contains("Act 4") == true,
            observed(NpcCatalog.byId(NpcCatalog.LENA_ID)?.special, "StoryCritical (Act 4 choice)")
        )
        r.check(
            "Vance", "150 HP rifle, ally or rival boss",
            NpcCatalog.byId(NpcCatalog.VANCE_ID)?.hp == 150 &&
                NpcCatalog.byId(NpcCatalog.VANCE_ID)?.weapon == "Rifle",
            observed(NpcCatalog.byId(NpcCatalog.VANCE_ID)?.hp, 150)
        )
        r.check(
            "General Whitfield", "briefing only, no combat stats",
            NpcCatalog.byId(NpcCatalog.WHITFIELD_ID)?.isCombatant == false &&
                NpcCatalog.byId(NpcCatalog.WHITFIELD_ID)?.role == "Briefing only",
            observed(NpcCatalog.byId(NpcCatalog.WHITFIELD_ID)?.hp, "null")
        )
        r.check(
            "Kessler escalation", "1,000 HP boss 2, 3,000 HP final boss",
            NpcCatalog.byId(NpcCatalog.KESSLER_YOUNG_ID)?.hp == 1_000 &&
                NpcCatalog.byId(NpcCatalog.KESSLER_FINAL_ID)?.hp == 3_000 &&
                NpcCatalog.kesslerEscalation(),
            observed(NpcCatalog.byId(NpcCatalog.KESSLER_FINAL_ID)?.hp, 3_000)
        )
        r.check(
            "Story-critical count", "Brooks, Lena and Vance",
            NpcCatalog.storyCritical().map { it.id } ==
                listOf(NpcCatalog.BROOKS_ID, NpcCatalog.LENA_ID, NpcCatalog.VANCE_ID),
            observed(NpcCatalog.storyCritical().size, 3)
        )
        r.check(
            "Combatants", "5 of the 6 rows can fight",
            NpcCatalog.combatants().size == 5 && NpcCatalog.bosses().size == 2,
            observed("${NpcCatalog.combatants().size}/${NpcCatalog.bosses().size}", "5/2")
        )
    }
}
