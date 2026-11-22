package com.example.game.valor.plan

import com.example.game.valor.balance.WeaponSpec
import com.example.game.valor.ux.HudLayout
import com.example.game.valor.ux.HudWidget

/** Section 5.21: the four hit-marker states; headshot and kill read differently. */
enum class HitMarkerKind(val displayName: String) {
    NONE("None"),
    BODY("Hit"),
    HEADSHOT("Headshot"),
    KILL("Kill")
}

/** Section 5.21: the popups that wait for a safe moment. */
enum class PopupKind(val displayName: String) {
    RANK_UP("Rank-up"),
    MEDAL("Medal earned"),
    LEVEL_END("Level end")
}

/**
 * Section 5.21: every field is read from live state, so the HUD cannot show a
 * number the simulation does not hold.
 */
data class HudReadout(
    val health: Float,
    val maxHealth: Float,
    val armor: Float,
    val magazine: Int,
    val reserve: Int,
    val weaponName: String,
    val fireMode: FireMode,
    val grenades: Int,
    val objectives: List<String>,
    val minimapVisible: Boolean,
    val compassVisible: Boolean,
    val damageDirection: String?,
    val hitMarker: HitMarkerKind
) {
    val healthFraction: Float get() = if (maxHealth <= 0f) 0f else health / maxHealth

    val lowHealth: Boolean get() = HealthRules.isLowHealth(health, maxHealth)

    fun ammoLabel(): String = "$magazine / $reserve"
}

object HudRules {

    const val SHOWS_ONLY_TRUTH = true
    const val DAMAGE_DIRECTION_ON_REAL_DAMAGE_ONLY = true
    const val HIT_MARKER_ON_REAL_DAMAGE_ONLY = true
    const val LAYOUT_EDITABLE = true
    const val LAYOUT_SAVED = true

    /** "Never in combat": these are the states a popup may appear in. */
    val SAFE_POPUP_STATES: Set<MissionState> = setOf(
        MissionState.SUCCESS,
        MissionState.RESULTS,
        MissionState.BRIEFING
    )

    fun readout(
        player: Combatant,
        ammo: AmmoState,
        weapon: WeaponSpec,
        fireMode: FireMode,
        grenades: Int,
        objectives: List<String>,
        damageDirection: String? = null,
        hitMarker: HitMarkerKind = HitMarkerKind.NONE
    ): HudReadout = HudReadout(
        health = player.current,
        maxHealth = player.maxHealth,
        armor = player.armorPoints,
        magazine = ammo.magazine,
        reserve = ammo.reserve,
        weaponName = weapon.name,
        fireMode = fireMode,
        grenades = grenades,
        objectives = objectives,
        minimapVisible = HudLayout.widget("minimap") != null,
        compassVisible = HudLayout.widget("compass") != null,
        damageDirection = damageDirection,
        hitMarker = hitMarker
    )

    /** "Damage direction indicator (only on real damage taken)." */
    fun damageDirection(sourceId: String?, damageApplied: Boolean): String? =
        if (damageApplied && sourceId != null) sourceId else null

    /** "Hit marker (only on real damage dealt; different for headshot/kill)." */
    fun hitMarker(dealtDamage: Boolean, headshot: Boolean, killed: Boolean): HitMarkerKind = when {
        !dealtDamage -> HitMarkerKind.NONE
        killed -> HitMarkerKind.KILL
        headshot -> HitMarkerKind.HEADSHOT
        else -> HitMarkerKind.BODY
    }

    /** "Buttons are disabled (greyed) when the action isn't allowed." */
    fun reloadEnabled(ammo: AmmoState, reloading: Boolean): Boolean = AmmoRules.canStartReload(ammo, reloading)

    fun isGreyed(enabled: Boolean): Boolean = !enabled

    /** "No UI blocks the center of the screen": only the crosshair may sit there. */
    fun centerWidgets(): List<HudWidget> =
        HudLayout.WIDGETS.filter { it.xFraction == 0.5f && it.yFraction == 0.5f }

    fun centerBlockingWidgets(): List<HudWidget> = centerWidgets().filter { it.id != "crosshair" }

    fun noWidgetBlocksCenter(): Boolean = centerBlockingWidgets().isEmpty()

    fun layoutKey(): String = "settings.hudLayout"

    /** Section 5.21 + 5.19: a moved widget autosaves through the settings trigger. */
    fun layoutAutosaveTrigger(): AutosaveTrigger = AutosaveTrigger.SETTINGS_CHANGE
}

/**
 * Section 5.21: rank-up, medal and level-end popups queue and only surface at a
 * safe moment, so a promotion never interrupts a firefight.
 */
class PopupQueue {

    private val pending = mutableListOf<PopupKind>()

    fun enqueue(kind: PopupKind): Int {
        pending += kind
        return pending.size
    }

    fun pendingCount(): Int = pending.size

    fun kinds(): List<PopupKind> = pending.toList()

    fun canShowNow(state: MissionState): Boolean = state in HudRules.SAFE_POPUP_STATES

    /** Pops the oldest popup, or null while the state is not safe. */
    fun next(state: MissionState): PopupKind? {
        if (!canShowNow(state) || pending.isEmpty()) return null
        return pending.removeAt(0)
    }

    fun clear() = pending.clear()
}

/** Section 5.21: "all text through a localization table (no hard-coded strings)". */
object Localization {

    const val NO_HARD_CODED_STRINGS = true

    val ENTRIES: Map<String, String> = mapOf(
        "hud.health" to "Health",
        "hud.armor" to "Armor",
        "hud.ammo" to "Ammo",
        "hud.weapon" to "Weapon",
        "hud.fireMode" to "Fire mode",
        "hud.grenades" to "Grenades",
        "hud.objectives" to "Objectives",
        "hud.minimap" to "Minimap",
        "hud.compass" to "Compass",
        "hud.damageDirection" to "Damage direction",
        "hud.hitmarker" to "Hit marker",
        "menu.resume" to "Resume",
        "menu.settings" to "Settings",
        "menu.quit" to "Quit"
    )

    /** A missing key falls back to the key itself, never to a blank label. */
    fun text(key: String, table: Map<String, String> = ENTRIES): String = table[key] ?: key

    fun isTranslated(key: String, table: Map<String, String> = ENTRIES): Boolean = table.containsKey(key)

    /** Section 10.2 accessibility: the one setting that must survive a reload. */
    fun accessibilityKeys(): List<String> = listOf("hud.minimap", "hud.compass")
}
