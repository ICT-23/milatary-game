package com.example.game.valor.gdd

import com.example.game.valor.ux.AimAssistLevel

/** Section 8: one row of the control table. */
data class ControlBinding(val control: String, val action: String)

/**
 * Section 8: the mobile control scheme plus the accessibility switches the
 * section lists. Aim assist reuses the Art/Audio/UI bible's level enum so the
 * two sheets can never disagree about the options.
 */
object ControlScheme {

    val BINDINGS: List<ControlBinding> = listOf(
        ControlBinding("Left virtual stick", "Move"),
        ControlBinding("Right-side drag", "Look/aim"),
        ControlBinding("Fire button (right thumb)", "Shoot (hold for auto)"),
        ControlBinding("ADS button", "Aim down sights / scope"),
        ControlBinding(
            "Reload, Grenade, Swap, Crouch, Jump/Vault, Melee",
            "On-screen buttons (layout editable)"
        ),
        ControlBinding("Cover", "Auto-snap when near cover, tap to peek"),
        ControlBinding("Gyro aiming", "Optional"),
        ControlBinding("Aim assist", "Adjustable (Off / Low / High)"),
        ControlBinding("Controller support", "Bluetooth gamepad")
    )

    /** Section 8: the action-button row the layout editor can rearrange. */
    val ACTION_BUTTONS: List<String> =
        listOf("Reload", "Grenade", "Swap", "Crouch", "Jump/Vault", "Melee")

    /**
     * Section 8 prints three settings - Off / Low / High. The Art, Audio & UI
     * bible carries an extra Medium step; it is not one of the three this sheet
     * documents, so the GDD list names only these.
     */
    val AIM_ASSIST_LEVELS: List<AimAssistLevel> = listOf(
        AimAssistLevel.OFF,
        AimAssistLevel.LOW,
        AimAssistLevel.HIGH
    )

    fun aimAssistLabels(): List<String> = AIM_ASSIST_LEVELS.map { it.displayName }

    fun byControl(control: String): ControlBinding? =
        BINDINGS.firstOrNull { it.control.equals(control.trim(), ignoreCase = true) }

    /** Every on-screen button can be moved, per the layout-editable rule. */
    fun layoutEditable(): Boolean = true

    fun count(): Int = BINDINGS.size
}

/** Section 8 accessibility block. */
enum class AccessOption(val displayName: String, val detail: String) {
    LEFT_HAND_MODE("Left-hand mode", "Mirrors the touch layout"),
    SUBTITLE_SIZE("Subtitle size", "Scales subtitle text"),
    COLOR_BLIND_OUTLINES("Color-blind enemy outlines", "Outlines enemies without relying on color"),
    VIBRATION_TOGGLE("Vibration toggle", "Turns haptics off")
}

object AccessibilityOptions {

    val OPTIONS: List<AccessOption> = AccessOption.entries

    fun byName(name: String): AccessOption? =
        AccessOption.entries.firstOrNull { it.displayName.equals(name.trim(), ignoreCase = true) }

    fun count(): Int = OPTIONS.size
}
