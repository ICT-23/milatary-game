package com.example.game.valor.balance

/**
 * Section 8: one character row.
 *
 * [hp] and [weapon] are null for the briefing-only commander, and [special]
 * keeps the table's own wording ("StoryCritical", "3 phases") rather than
 * inventing flags the bible does not state.
 */
data class NpcSpec(
    val id: String,
    val name: String,
    val hp: Int?,
    val weapon: String?,
    val role: String,
    val special: String
) {
    val isCombatant: Boolean get() = hp != null
    val isStoryCritical: Boolean get() = special.startsWith("StoryCritical")
    val isBoss: Boolean get() = role.startsWith("Boss") || role == "Final boss"
}

object NpcCatalog {

    const val BROOKS_ID = "brooks"
    const val LENA_ID = "lena"
    const val VANCE_ID = "vance"
    const val WHITFIELD_ID = "whitfield"
    const val KESSLER_YOUNG_ID = "kessler_young"
    const val KESSLER_FINAL_ID = "kessler_final"

    val NPCS: List<NpcSpec> = listOf(
        NpcSpec(BROOKS_ID, "Sgt. Brooks", 200, "Rifle/Pistol",
            "Ally (Acts 1-2), then support voice", "StoryCritical"),
        NpcSpec(LENA_ID, "Lena Hartwell", 120, "Sniper",
            "Overwatch ally; marks targets", "StoryCritical (Act 4 choice)"),
        NpcSpec(VANCE_ID, "Vance", 150, "Rifle",
            "Ally or rival boss (Act 3)", "StoryCritical"),
        NpcSpec(WHITFIELD_ID, "General Whitfield", null, null,
            "Briefing only", "-"),
        NpcSpec(KESSLER_YOUNG_ID, "Kessler (young)", 1_000, "Pistol + saber",
            "Boss 2", "3 phases"),
        NpcSpec(KESSLER_FINAL_ID, "Kessler (final)", 3_000, "Rifle + drones",
            "Final boss", "4 phases")
    )

    fun byId(id: String): NpcSpec? = NPCS.firstOrNull { it.id == id }

    fun combatants(): List<NpcSpec> = NPCS.filter { it.isCombatant }

    fun storyCritical(): List<NpcSpec> = NPCS.filter { it.isStoryCritical }

    fun bosses(): List<NpcSpec> = NPCS.filter { it.isBoss }

    /**
     * The two Kessler rows are the same character at two points in the story;
     * the final duel must always field the tougher of the two.
     */
    fun kesslerEscalation(): Boolean {
        val young = byId(KESSLER_YOUNG_ID)?.hp ?: return false
        val final = byId(KESSLER_FINAL_ID)?.hp ?: return false
        return final > young
    }
}
