package com.example.game.valor.gdd

import com.example.game.valor.art.ArtAct

/**
 * Section 10: the environments of one act. The act title is read from the Art,
 * Audio & UI bible instead of being restated here.
 */
data class WorldActSpec(val act: Int, val environments: List<String>) {
    val title: String get() = ArtAct.entries.firstOrNull { it.number == act }?.title ?: "Act $act"
}

object WorldMap {

    /** Section 10 environment table, row for row. */
    val ACTS: List<WorldActSpec> = listOf(
        WorldActSpec(
            1, listOf("Barracks", "Obstacle course", "Range", "Mess hall", "Parade ground")
        ),
        WorldActSpec(
            2, listOf("Muddy trenches", "No-man's-land", "Ruined farm", "Foggy forest")
        ),
        WorldActSpec(
            3, listOf("European old town", "Train station", "Snowy mountain pass", "Docks")
        ),
        WorldActSpec(
            4, listOf("Beach landing", "Bocage hedgerows", "Bombed city", "Fortress bunker", "Airfield")
        ),
        WorldActSpec(
            5, listOf("Desert canyon", "Oil refinery", "Arctic base", "Night city")
        ),
        WorldActSpec(
            6, listOf("High-tech compound", "Rooftop skyline", "Ocean carrier", "Drone factory")
        )
    )

    fun forAct(act: Int): WorldActSpec? = ACTS.firstOrNull { it.act == act }

    fun environmentsFor(act: Int): List<String> = forAct(act)?.environments ?: emptyList()

    fun totalEnvironments(): Int = ACTS.sumOf { it.environments.size }

    fun hasEnvironment(act: Int, environment: String): Boolean =
        environmentsFor(act).any { it.equals(environment.trim(), ignoreCase = true) }

    /** Every act carries at least one named environment. */
    fun allActsPopulated(): Boolean = ACTS.all { it.environments.isNotEmpty() }
}

/** Section 10 "Dynamic weather". */
enum class Weather(val displayName: String) {
    RAIN("Rain"),
    FOG("Fog"),
    SNOW("Snow"),
    SANDSTORM("Sandstorm"),
    DAY_NIGHT_CYCLE("Day/night cycle")
}

/** Section 10 "Destruction (lite)". */
enum class DestructionTarget(val displayName: String) {
    BREAKABLE_CRATES("Breakable crates"),
    WOODEN_WALLS("Wooden walls"),
    SANDBAG_DAMAGE("Sandbag damage"),
    GLASS("Glass")
}

object WorldSystems {

    val WEATHER: List<Weather> = Weather.entries

    val DESTRUCTION: List<DestructionTarget> = DestructionTarget.entries

    fun hasWeather(name: String): Boolean =
        WEATHER.any { it.displayName.equals(name.trim(), ignoreCase = true) }

    /** Destruction is deliberately lite: four target classes, nothing structural. */
    fun destructionIsLite(): Boolean = DESTRUCTION.size == 4
}
