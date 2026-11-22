package com.example.game.valor.launch

/** Section 1.1: one row of the tool table. */
data class ToolRow(val tool: String, val purpose: String)

/** Section 1.2: one Package Manager entry. [required] is false when the document says "optional". */
data class PackageRow(val name: String, val required: Boolean, val note: String = "")

/**
 * Sections 1.1 and 1.2: the development environment.
 *
 * The original plan targets Unity; this build runs the same rules on Kotlin and
 * Compose, so every entry is kept as the rule it encodes rather than a tool the
 * repository has to install. [runsOnCurrentStack] names the translation so a
 * reader can see what the Kotlin build actually uses.
 */
object DevEnvironment {

    const val TEMPLATE = "3D (URP)"
    const val PROJECT_NAME = "ValorAcademy"

    val TOOLS: List<ToolRow> = listOf(
        ToolRow("Unity Hub + latest Unity LTS", "Engine"),
        ToolRow("Android Build Support (SDK, NDK, OpenJDK)", "Android builds"),
        ToolRow("Visual Studio / VS Code / Rider", "C# editing"),
        ToolRow("Git + Git LFS", "Version control for large binary assets"),
        ToolRow("Blender", "Model fixes, LODs"),
        ToolRow("Audacity / Reaper", "Audio editing"),
        ToolRow("Android Studio (for adb, Logcat)", "Device debugging"),
        ToolRow("Claude Code / AI coding tool", "Vibe-coding")
    )

    val PACKAGES: List<PackageRow> = listOf(
        PackageRow("Universal RP", required = true),
        PackageRow("Input System", required = true),
        PackageRow("Cinemachine", required = true),
        PackageRow("Timeline", required = true),
        PackageRow("AI Navigation", required = true),
        PackageRow("TextMeshPro", required = true),
        PackageRow("Addressables", required = true),
        PackageRow("Localization", required = true),
        PackageRow("Test Framework", required = true),
        PackageRow("Animation Rigging", required = true),
        PackageRow("Post Processing (via URP)", required = true, note = "via URP"),
        PackageRow("Unity IAP (In-App Purchasing)", required = true),
        PackageRow("Mobile Notifications", required = false, note = "optional"),
        PackageRow("Unity Analytics / Firebase", required = true, note = "choose one pipeline"),
        PackageRow("Burst / Mathematics", required = false, note = "optional")
    )

    const val SCRIPTING_BACKEND = "IL2CPP"
    const val TARGET_ARCH = "ARM64"
    const val LEGACY_ARCH = "ARMv7 - only if old phones must be supported"
    const val MANAGED_STRIPPING = "Medium"
    const val GRAPHICS_API = "Vulkan"
    const val GRAPHICS_FALLBACK = "OpenGLES3"
    const val COLOR_SPACE = "Linear"

    /** "choose per Google Play's current requirements ... check before release". */
    const val MIN_ANDROID_API = "per the current Play requirement, checked before release"

    val QUALITY_LEVELS: List<String> = listOf("Low", "Medium", "High")

    const val INPUT_SYSTEM = "new Input System with on-screen controls"
    val ACTION_MAPS: List<String> = listOf("On-screen controls", "Gamepad")

    val ASSEMBLY_DEFINITIONS: List<String> = listOf(
        "Valor.Core",
        "Valor.Gameplay",
        "Valor.UI",
        "Valor.Tests.EditMode",
        "Valor.Tests.PlayMode"
    )

    /** What the shared rule means in the Kotlin + Compose build that ships here. */
    fun runsOnCurrentStack(rule: String): String = when (rule) {
        "Engine" -> "Kotlin 2.2.10 + Jetpack Compose host, Three.js viewport"
        "Android Build Support (SDK, NDK, OpenJDK)" -> "Gradle Android plugin, compileSdk 36, minSdk 24"
        "Unity IAP (In-App Purchasing)" -> "Play Billing product catalogue in launch.StoreCatalogue"
        else -> rule
    }

    fun requiredPackages(): List<PackageRow> = PACKAGES.filter { it.required }

    fun packageNamed(name: String): PackageRow? =
        PACKAGES.firstOrNull { it.name.equals(name.trim(), ignoreCase = true) }

    fun qualityLevelNamed(name: String): String? =
        QUALITY_LEVELS.firstOrNull { it.equals(name.trim(), ignoreCase = true) }

    /** Two graphics APIs are named so both get a device pass. */
    fun graphicsApiVariants(): List<String> = listOf(GRAPHICS_API, GRAPHICS_FALLBACK)
}

/**
 * Section 1.3 and 1.4: repository layout, branch model and the documents that
 * must sit in the repository root.
 */
object RepositorySpec {

    val GITIGNORE_ENTRIES: List<String> = listOf(
        "Library/",
        "Temp/",
        "Obj/",
        "Logs/",
        "UserSettings/",
        "*.csproj",
        "*.sln",
        "Builds/"
    )

    val LFS_PATTERNS: List<String> = listOf(
        "*.png", "*.jpg", "*.psd", "*.fbx", "*.blend", "*.wav",
        "*.mp3", "*.ogg", "*.mp4", "*.exr", "*.hdr", "*.unitypackage"
    )

    const val ASSET_SERIALIZATION = "Force Text"
    const val VERSION_CONTROL_MODE = "Visible Meta Files"

    const val MAIN_BRANCH = "main"
    const val MAIN_ALWAYS_PLAYABLE = true

    val BRANCHES: List<String> = listOf(
        "main (always playable)",
        "develop",
        "feature/<name>",
        "fix/<name>",
        "release/<version>"
    )

    val COMMIT_EXAMPLES: List<String> = listOf(
        "feat(weapons): add recoil curve",
        "fix(mission): kill-plane counts as dead"
    )

    val TAGS: List<String> = listOf(
        "v0.1.0-prototype",
        "v0.5.0-alpha",
        "v0.9.0-beta",
        "v1.0.0"
    )

    val ROOT_DOCUMENTS: List<String> = listOf(
        "Valor_Academy_Game_Design.md",
        "Valor_Academy_Implementation_Plan.md",
        "Valor_Academy_Content_and_Balance_Data.md",
        "Valor_Academy_Art_Audio_UI_Bible.md",
        "Valor_Academy_Launch_and_LiveOps.md",
        "BUILD_STATUS.md",
        "CHANGELOG.md",
        "ASSET_LICENSES.md",
        "README.md"
    )

    /** "never commit directly to main; merge via PR/review; run tests before merge". */
    const val COMMITS_TO_MAIN = false
    const val MERGES_VIA_PULL_REQUEST = true
    const val TESTS_BEFORE_MERGE = true

    fun isProtectedBranch(branch: String): Boolean =
        branch.trim().equals(MAIN_BRANCH, ignoreCase = true)

    fun isDirectCommitAllowed(branch: String): Boolean = !isProtectedBranch(branch)

    /** "small, one system each" - the subject names the subsystem in parentheses. */
    fun isConventionalCommit(subject: String): Boolean {
        val head = subject.substringBefore(":", missingDelimiterValue = "")
        if (head.isEmpty()) return false
        val type = head.substringBefore("(")
        val scope = head.substringAfter("(", "").substringBefore(")")
        return type in COMMIT_TYPES && scope.isNotBlank() && subject.contains(": ")
    }

    private val COMMIT_TYPES = setOf("feat", "fix", "chore", "docs", "test", "refactor", "perf", "build")

    fun rootDocumentsPresent(names: Iterable<String>): List<String> {
        val present = names.map { it.trim().lowercase() }.toSet()
        return ROOT_DOCUMENTS.filterNot { it.lowercase() in present }
    }
}
