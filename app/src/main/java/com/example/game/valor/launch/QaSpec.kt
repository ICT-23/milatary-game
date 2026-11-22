package com.example.game.valor.launch

/** Section 8.1: one device tier and the performance it must hold. */
data class DeviceTierSpec(
    val tier: String,
    val specs: String,
    val target: String,
    val preset: String?
)

/** Section 8.1: the matrix, the brands to cover and the classifier. */
object DeviceMatrix {

    val LOW = DeviceTierSpec("Low", "3 GB RAM, entry GPU, Android 9-11", "30 FPS", "Low")
    val MID = DeviceTierSpec("Mid", "4-6 GB RAM, mid GPU", "45-60 FPS", "Medium")
    val HIGH = DeviceTierSpec("High", "8 GB+ flagship", "60 FPS", "High")
    val TABLET = DeviceTierSpec("Tablet", "10\"", "UI scaling check", null)

    val TIERS: List<DeviceTierSpec> = listOf(LOW, MID, HIGH, TABLET)

    /** "Test at least 6 real devices". */
    const val MIN_REAL_DEVICES = 6

    val BRANDS: List<String> = listOf(
        "Samsung",
        "Xiaomi/Redmi",
        "Realme/Oppo/Vivo",
        "Motorola",
        "Pixel"
    )

    /** 3 GB and below is the Low tier; 4-6 GB is Mid; 8 GB and up is High. */
    fun classify(ramGb: Int): DeviceTierSpec = when {
        ramGb >= 8 -> HIGH
        ramGb >= 4 -> MID
        else -> LOW
    }

    fun tier(name: String): DeviceTierSpec? =
        TIERS.firstOrNull { it.tier.equals(name.trim(), ignoreCase = true) }

    fun fpsTargetFor(preset: String): String = when (preset.uppercase()) {
        "LOW" -> LOW.target
        "HIGH" -> HIGH.target
        else -> MID.target
    }

    fun tabletsNeedScalingCheck(): Boolean = TABLET.target.contains("scaling", ignoreCase = true)
}

/** Section 8.2: one test type and what it must cover. */
data class TestTypeSpec(val type: String, val coverage: String)

/** Section 8.2: the nine test types every build cycle runs. */
object TestPlan {

    val TYPES: List<TestTypeSpec> = listOf(
        TestTypeSpec(
            "Smoke test (every build)",
            "menu -> mission -> shoot -> win -> result -> shop -> quit/resume"
        ),
        TestTypeSpec("Regression suite", "From the Implementation Plan Section 7"),
        TestTypeSpec("Soak test", "2 hours continuous play, memory stable, no crash"),
        TestTypeSpec(
            "Interruptions",
            "phone call, notification, app switch, screen lock, low battery, airplane mode, storage full"
        ),
        TestTypeSpec("Save tests", "force-close during save, update from older version, reinstall"),
        TestTypeSpec("Network tests", "offline play works; ads/IAP fail gracefully"),
        TestTypeSpec("Localization", "pseudo-locale and RTL pass"),
        TestTypeSpec("Balance playtests", "10+ testers, record fail points; tweak via data"),
        TestTypeSpec(
            "Security basics",
            "obfuscate/strip, no secrets in build, server-validate purchases if online economy is added"
        )
    )

    const val SOAK_HOURS = 2
    const val MIN_BALANCE_TESTERS = 10
    const val OFFLINE_PLAY_WORKS = true
    const val ADS_FAIL_GRACEFULLY = true
    const val SECRETS_IN_BUILD = false

    fun typeNamed(type: String): TestTypeSpec? =
        TYPES.firstOrNull { it.type.startsWith(type.trim(), ignoreCase = true) }

    fun soakStable(memoryGrowthMb: Double, crashed: Boolean): Boolean =
        !crashed && memoryGrowthMb <= MEMORY_GROWTH_LIMIT_MB

    /** A slow, steady leak still fails the soak even without a crash. */
    const val MEMORY_GROWTH_LIMIT_MB = 32.0

    fun typeCount(): Int = TYPES.size
}

/** Section 8.3: one bug severity and the rule that governs it. */
enum class Severity(
    val code: String,
    val meaning: String,
    val rule: String
) {
    S0("S0", "Crash / progress loss / blocker", "Fix before any release"),
    S1("S1", "Major feature broken", "Fix before release"),
    S2("S2", "Annoying/visual", "Schedule"),
    S3("S3", "Minor polish", "Backlog");

    val blocksRelease: Boolean get() = this == S0 || this == S1
}

/** Section 8.4: the three gates a build walks through. */
enum class ReleaseStage(val displayName: String, val requirement: String) {
    ALPHA("Alpha", "All core systems playable, placeholders OK"),
    BETA("Beta", "All content in, 0 S0, <= 5 S1, performance targets met"),
    RC("RC", "0 S0/S1, store assets done, privacy/data forms done")
}

/** The verdict of one gate, with every blocker named. */
data class GateReport(val stage: ReleaseStage, val blockers: List<String>) {
    val passed: Boolean get() = blockers.isEmpty()

    fun summary(): String =
        if (passed) "PASS - ${stage.displayName}" else "BLOCKED - ${blockers.joinToString("; ")}"
}

/** Section 8.4: the gate arithmetic, kept out of the release checklists. */
object ReleaseGate {

    const val BETA_MAX_S1 = 5

    fun evaluate(
        stage: ReleaseStage,
        s0: Int,
        s1: Int,
        coreSystemsPlayable: Boolean,
        allContentIn: Boolean,
        performanceTargetsMet: Boolean,
        storeAssetsDone: Boolean,
        privacyFormsDone: Boolean
    ): GateReport {
        val blockers = mutableListOf<String>()
        when (stage) {
            ReleaseStage.ALPHA -> {
                if (!coreSystemsPlayable) blockers += "core systems not playable"
            }

            ReleaseStage.BETA -> {
                if (!coreSystemsPlayable) blockers += "core systems not playable"
                if (!allContentIn) blockers += "content missing"
                if (s0 > 0) blockers += "$s0 x S0 open"
                if (s1 > BETA_MAX_S1) blockers += "$s1 x S1 open (limit $BETA_MAX_S1)"
                if (!performanceTargetsMet) blockers += "performance targets missed"
            }

            ReleaseStage.RC -> {
                if (s0 > 0) blockers += "$s0 x S0 open"
                if (s1 > 0) blockers += "$s1 x S1 open"
                if (!storeAssetsDone) blockers += "store assets missing"
                if (!privacyFormsDone) blockers += "privacy/data forms missing"
            }
        }
        return GateReport(stage, blockers)
    }

    /** Nobody ships a build with an open blocker, whatever the stage claims. */
    fun shipBlocked(s0: Int, s1: Int): Boolean = s0 > 0 || s1 > 0
}
