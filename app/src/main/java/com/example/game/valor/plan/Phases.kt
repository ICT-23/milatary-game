package com.example.game.valor.plan

/**
 * Section 6: one milestone. [files] are the new files the phase introduces,
 * [logicSections] the Section 5 rule groups it must obey.
 */
data class PlanPhase(
    val number: Int,
    val milestone: String?,
    val title: String,
    val files: List<String>,
    val tasks: List<String>,
    val acceptance: List<String>,
    val logicSections: List<String>
)

/** Section 6: the fifteen phases, in build order, with their acceptance gates. */
object PlanPhases {

    val PHASES: List<PlanPhase> = listOf(
        PlanPhase(
            0, null, "Project audit & setup",
            emptyList(),
            listOf(
                "Scan the project; fill BUILD_STATUS.md with what exists",
                "Create missing folders, layers/tags and assembly definitions",
                "Create core interfaces and the event bus if missing",
                "Set up version control, commit baseline"
            ),
            listOf(
                "Project compiles", "Status file accurate",
                "Nothing existing modified except additive setup"
            ),
            listOf("2", "4")
        ),
        PlanPhase(
            1, "M1", "Player & camera",
            listOf("PlayerController", "PlayerInput (mobile + gamepad)", "ThirdPersonCamera", "PlayerAnimator"),
            listOf(
                "Move, sprint, crouch, jump, vault",
                "Camera follow with collision",
                "ADS FOV change",
                "Touch UI: joystick plus look drag"
            ),
            listOf("Smooth 60 FPS movement", "No camera clipping", "Controls remappable"),
            listOf("5.1", "5.2")
        ),
        PlanPhase(
            2, "M2", "Weapons & damage core",
            listOf(
                "WeaponData", "Weapon", "WeaponController", "RecoilSystem", "SpreadSystem",
                "Projectile", "HitDetection", "DamageSystem", "Health", "HitboxZone",
                "DamageInfo", "AmmoSystem"
            ),
            listOf(
                "Weapon data with all stats",
                "Firing pipeline checks",
                "Hitscan and projectile hit detection with muzzle/camera convergence",
                "Hitbox zones on characters",
                "Damage formula, health and death",
                "Ammo, reload and swap",
                "Test dummies and a shooting range"
            ),
            listOf(
                "Damage only on real hit; wall blocks; miss = 0 damage",
                "Headshot > torso > limb",
                "Empty mag does nothing; reload math correct; cancel safe",
                "Dead target cannot be damaged; exactly one kill event",
                "Unit tests for the damage table and reload pass"
            ),
            listOf("5.2", "5.3", "5.4", "5.5", "5.6")
        ),
        PlanPhase(
            3, "M2b", "Enemy AI",
            listOf("EnemyAI", "EnemyStateMachine", "Perception", "CoverManager", "EnemyData", "EnemyRegistry"),
            listOf(
                "States and perception",
                "Cover claiming",
                "Accuracy-based shooting through the same hit detection",
                "Alert propagation",
                "Types: Rifleman, Rusher, Sniper, Heavy"
            ),
            listOf(
                "Enemies cannot see through walls", "AI misses sometimes",
                "Reaction delay works", "No AI stuck; 8 active AIs at 45 FPS or better"
            ),
            listOf("5.10", "5.8")
        ),
        PlanPhase(
            4, "M2c", "Mission system & HUD",
            listOf(
                "MissionManager", "MissionStateMachine", "Objective classes", "MissionData",
                "ResultScreen", "HUD", "PauseMenu", "CheckpointSystem"
            ),
            listOf("State machine", "Objectives", "Stars", "Results", "Watchdog", "HUD"),
            listOf(
                "Bot completes test missions", "No stuck states",
                "XP and cash granted once"
            ),
            listOf("5.14", "5.21")
        ),
        PlanPhase(
            5, "M3", "Boot Camp / Act 1",
            emptyList(),
            listOf(
                "Academy hub scene", "Obstacle course", "Shooting range drills",
                "Tournament", "Tutorial prompts", "Cover system", "Grenades",
                "First story beats", "6-8 missions"
            ),
            listOf(
                "A new player can finish Act 1 without help",
                "First tournament passable", "Tutorial skippable"
            ),
            listOf("5.15", "5.8", "5.9", "5.20")
        ),
        PlanPhase(
            6, "M4", "Progression & economy",
            listOf(
                "ProgressionService", "RankData (20 ranks)", "UnlockService",
                "EconomyService", "SkillTree", "MedalService", "SaveSystem"
            ),
            listOf("Rank and XP", "Economy", "Save system", "Rank-up UI", "Medals", "Skill points"),
            listOf(
                "Rank math tests", "Atomic purchase tests",
                "Corrupt-save recovery", "Migration test"
            ),
            listOf("5.16", "5.17", "5.19")
        ),
        PlanPhase(
            7, "M5", "Gunsmith",
            listOf("AttachmentData", "WeaponStatCalculator", "GunsmithUI", "CraftingService", "BlueprintService"),
            listOf("Attachment modifiers", "Craft flow", "3D rotating preview", "Stat bars", "Camo skins"),
            listOf(
                "Stat recompute never drifts", "Crafting costs correct",
                "Preview does not change live stats"
            ),
            listOf("5.7", "5.18")
        ),
        PlanPhase(
            8, "M6", "Acts 2-3",
            emptyList(),
            listOf(
                "WW1 trench environment", "Bolt-action and jam mechanic", "Gas grenades and gas mask",
                "Squad AI", "Stealth", "Escort and train missions", "Boss 1 and 2", "Officer rank content"
            ),
            listOf(
                "15-20 missions playable", "Bosses beatable on all difficulties"
            ),
            listOf("5.11", "5.12")
        ),
        PlanPhase(
            9, "M7", "Acts 4-5",
            emptyList(),
            listOf(
                "Beach landing", "City fights", "Vehicles (jeep, tank gunner)",
                "Night vision and thermal", "Snow and desert", "Sniper boss", "Betrayal sequence"
            ),
            listOf("Vehicle controls stable", "Night vision performance OK"),
            listOf("5.10")
        ),
        PlanPhase(
            10, "M8", "Act 6 & drones",
            emptyList(),
            listOf(
                "ARIA", "Drones", "Tactical map", "Final boss",
                "Three endings", "Medal ceremony cutscene"
            ),
            listOf(
                "All endings reachable", "Drone rules enforced (charges, cooldown, no-strike)"
            ),
            listOf("5.13", "5.20")
        ),
        PlanPhase(
            11, "M9", "Replay modes",
            emptyList(),
            listOf(
                "Survival waves", "Daily Operation (seed = date)", "Weekly Challenge modifiers",
                "New Game+ / Prestige", "Boss Rush", "Leaderboards (local first)"
            ),
            listOf(
                "Deterministic daily seed", "Prestige does not break the save",
                "Respawn-farm protection works"
            ),
            listOf("5.16", "5.19")
        ),
        PlanPhase(
            12, "M10", "Graphics & audio polish",
            emptyList(),
            listOf(
                "PBR materials", "Lighting bake", "Post-processing", "VFX", "Weather",
                "Animations", "Audio layering", "Quality presets"
            ),
            listOf("Visual target met on High", "Low preset stable at 30 FPS"),
            listOf("8")
        ),
        PlanPhase(
            13, "M11", "Optimization & QA",
            emptyList(),
            listOf(
                "Budget profiling", "Memory leak checks", "Crash reporting",
                "Localization", "Accessibility", "Full regression run"
            ),
            listOf("All tests green", "2-hour soak test with no crash"),
            listOf("7", "8", "9")
        ),
        PlanPhase(
            14, "M12", "Release prep",
            emptyList(),
            listOf(
                "Play Store assets", "Privacy policy", "Data safety form", "Age rating",
                "Ads consent", "Release build (AAB)", "Signing"
            ),
            listOf("Internal testing track build passes the pre-launch report"),
            listOf("12")
        )
    )

    fun phase(number: Int): PlanPhase? = PHASES.firstOrNull { it.number == number }

    fun count(): Int = PHASES.size

    fun allHaveAcceptance(): Boolean = PHASES.all { it.acceptance.isNotEmpty() }

    fun numbered(): List<Int> = PHASES.map { it.number }

    /** The phases that touch one Section 5 group, e.g. "5.4". */
    fun phasesFor(logicSection: String): List<PlanPhase> =
        PHASES.filter { logicSection in it.logicSections }
}

/** Section 11.2: the task prompts the plan ships, kept as named entries. */
data class PromptTemplate(val name: String, val aim: String)

object BuildPrompts {

    /** Section 11.1: paste before every task. */
    const val UNIVERSAL_PREFIX =
        "Read the design doc, the implementation plan, BUILD_STATUS.md and CHANGELOG.md. " +
            "Do not replace or rewrite any existing working file. Scan first, then add."

    val TEMPLATES: List<PromptTemplate> = listOf(
        PromptTemplate("Weapons & damage", "Phase 2 - 5.2/5.3/5.4/5.5/5.6, range scene, unit tests"),
        PromptTemplate("Enemy AI", "Phase 3 - line of sight only, same hit detection, reaction delay"),
        PromptTemplate("Missions", "Phase 4 - state machine, kill-plane, watchdog, checkpoints, single grant"),
        PromptTemplate("Progression & economy & save", "Phase 6 - multi-rank-up, atomic purchase, corrupt save, migration"),
        PromptTemplate("Gunsmith", "Phase 7 - recompute from base, preview never alters live stats"),
        PromptTemplate("Bug-fix", "Root cause first, smallest fix, add a regression test, log it"),
        PromptTemplate("Review", "Check the last change against Sections 1, 2 and 5")
    )

    fun names(): List<String> = TEMPLATES.map { it.name }

    fun template(name: String): PromptTemplate? = TEMPLATES.firstOrNull { it.name == name }
}
