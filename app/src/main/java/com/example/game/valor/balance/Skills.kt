package com.example.game.valor.balance

/** Section 5.1 branches. */
enum class SkillBranch(val displayName: String) {
    MARKSMAN("Marksman"),
    TACTICIAN("Tactician"),
    SURVIVOR("Survivor"),
    STEALTH("Stealth")
}

/** What the effect number is measured in. */
enum class SkillUnit(val displayName: String) {
    PERCENT("%"),
    HEALTH("HP"),
    SECONDS("s"),
    FLAG("")
}

/**
 * Section 5.1: one skill. Skills marked "(1)" in the table cap at one level;
 * every other skill runs to five. [perLevel] carries the table's own value, so
 * the sign is preserved (-4% recoil, +5 HP).
 */
data class SkillSpec(
    val id: String,
    val name: String,
    val branch: SkillBranch,
    val maxLevel: Int,
    val effectText: String,
    val perLevel: Float,
    val unit: SkillUnit
) {
    fun valueAt(level: Int): Float = perLevel * level.coerceIn(0, maxLevel)
}

object SkillTreeData {

    val SKILLS: List<SkillSpec> = listOf(
        // Marksman
        SkillSpec("steady_hands", "Steady Hands", SkillBranch.MARKSMAN, 5, "Recoil -4%", -4f, SkillUnit.PERCENT),
        SkillSpec("quick_draw", "Quick Draw", SkillBranch.MARKSMAN, 5, "ADS time -5%", -5f, SkillUnit.PERCENT),
        SkillSpec("deadeye", "Deadeye", SkillBranch.MARKSMAN, 5, "Headshot damage +3%", 3f, SkillUnit.PERCENT),
        SkillSpec("hold_breath", "Hold Breath", SkillBranch.MARKSMAN, 1, "Sniper breath hold +2 s", 2f, SkillUnit.SECONDS),

        // Tactician
        SkillSpec(
            "radio_discipline", "Radio Discipline", SkillBranch.TACTICIAN, 5,
            "Support cooldown -5%", -5f, SkillUnit.PERCENT
        ),
        SkillSpec("squad_leader", "Squad Leader", SkillBranch.TACTICIAN, 5, "Squad accuracy +4%", 4f, SkillUnit.PERCENT),
        SkillSpec("quartermaster", "Quartermaster", SkillBranch.TACTICIAN, 5, "Ammo pickup +10%", 10f, SkillUnit.PERCENT),
        SkillSpec("eyes_up", "Eyes Up", SkillBranch.TACTICIAN, 1, "Mark time +5 s", 5f, SkillUnit.SECONDS),

        // Survivor
        SkillSpec("tough", "Tough", SkillBranch.SURVIVOR, 5, "Max health +5", 5f, SkillUnit.HEALTH),
        SkillSpec("field_medic", "Field Medic", SkillBranch.SURVIVOR, 5, "Heal item speed +8%", 8f, SkillUnit.PERCENT),
        SkillSpec("plated", "Plated", SkillBranch.SURVIVOR, 5, "Armor absorb +2%", 2f, SkillUnit.PERCENT),
        SkillSpec(
            "second_wind", "Second Wind", SkillBranch.SURVIVOR, 1,
            "Once per mission survive lethal damage with 1 HP", 1f, SkillUnit.FLAG
        ),

        // Stealth
        SkillSpec("light_foot", "Light Foot", SkillBranch.STEALTH, 5, "Footstep noise -8%", -8f, SkillUnit.PERCENT),
        SkillSpec("silent_hand", "Silent Hand", SkillBranch.STEALTH, 5, "Takedown time -8%", -8f, SkillUnit.PERCENT),
        SkillSpec("shadow", "Shadow", SkillBranch.STEALTH, 5, "Detection speed -5%", -5f, SkillUnit.PERCENT),
        SkillSpec("ghost", "Ghost", SkillBranch.STEALTH, 1, "Suppressed noise radius -30% extra", -30f, SkillUnit.PERCENT)
    )

    /** Points the rank curve actually awards, taken from the rank table. */
    val TOTAL_POINTS_AWARDED: Int get() = RankTable.RANKS.sumOf { it.skillPoints }

    val MAX_TOTAL_LEVELS: Int get() = SKILLS.sumOf { it.maxLevel }

    fun byId(id: String): SkillSpec? = SKILLS.firstOrNull { it.id == id }

    fun ofBranch(branch: SkillBranch): List<SkillSpec> = SKILLS.filter { it.branch == branch }

    fun maxHealthBonus(levels: Map<String, Int>): Float =
        byId("tough")?.valueAt(levels["tough"] ?: 0) ?: 0f

    fun recoilReductionPercent(levels: Map<String, Int>): Float =
        byId("steady_hands")?.valueAt(levels["steady_hands"] ?: 0) ?: 0f

    /** Percent deltas convert to a multiplier the same way attachments do. */
    fun percentToFactor(percent: Float): Float = 1f + percent / 100f

    /** Every level spent so far must be backed by an earned point. */
    fun spentPoints(levels: Map<String, Int>): Int =
        levels.entries.sumOf { (id, level) ->
            val max = byId(id)?.maxLevel ?: 0
            level.coerceIn(0, max)
        }

    fun canSpend(levels: Map<String, Int>, points: Int): Boolean =
        spentPoints(levels) <= points && levels.all { (id, level) ->
            val max = byId(id)?.maxLevel ?: 0
            level in 0..max
        }
}
