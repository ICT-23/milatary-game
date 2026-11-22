package com.example.game.valor.launch

/** Section 2.2: the three parts of the version name. */
enum class VersionBump { MAJOR, MINOR, PATCH }

/**
 * Section 2.2: `MAJOR.MINOR.PATCH` plus the incrementing Android version code.
 *
 * The name follows the document; the code is bumped on *every* upload, including
 * one that only changes the name, so Play never sees a repeated code.
 */
data class AppVersion(
    val major: Int,
    val minor: Int,
    val patch: Int,
    val versionCode: Int
) {

    val name: String get() = "$major.$minor.$patch"

    /** "Show version in Settings -> About". */
    fun aboutLine(): String = "Version $name (build $versionCode)"

    /** The same release, re-uploaded: the code moves, the name does not. */
    fun nextUpload(): AppVersion = copy(versionCode = versionCode + 1)

    /** A real release bump: the name moves and the code still increments. */
    fun bump(kind: VersionBump): AppVersion = when (kind) {
        VersionBump.MAJOR -> AppVersion(major + 1, 0, 0, versionCode + 1)
        VersionBump.MINOR -> AppVersion(major, minor + 1, 0, versionCode + 1)
        VersionBump.PATCH -> AppVersion(major, minor, patch + 1, versionCode + 1)
    }

    fun isNewerThan(other: AppVersion): Boolean = when {
        major != other.major -> major > other.major
        minor != other.minor -> minor > other.minor
        patch != other.patch -> patch > other.patch
        else -> versionCode > other.versionCode
    }

    companion object {

        /** Mirrors `versionName` / `versionCode` in `app/build.gradle.kts`. */
        val CURRENT = AppVersion(major = 1, minor = 0, patch = 0, versionCode = 1)

        const val NAME_FORMAT = "MAJOR.MINOR.PATCH"
        const val CODE_INCREMENTS_ON_EVERY_UPLOAD = true
        const val SHOWN_IN_SETTINGS_ABOUT = true

        /** Parses "0.3.2" and, for a save that stored both, "0.3.2+41". */
        fun parse(text: String): AppVersion? {
            val trimmed = text.trim()
            if (trimmed.isEmpty()) return null
            val namePart = trimmed.substringBefore("+").substringBefore(" (")
            val parts = namePart.split(".")
            if (parts.size != 3) return null
            val numbers = parts.map { it.toIntOrNull() ?: return null }
            val code = trimmed.substringAfter("+", "").toIntOrNull() ?: 0
            return AppVersion(numbers[0], numbers[1], numbers[2], code)
        }
    }
}

/** Section 2.1: one of the three build flavours and the settings it carries. */
enum class BuildType(
    val displayName: String,
    val format: String,
    val purpose: String,
    val settings: String,
    val developmentFlags: Boolean,
    val signed: Boolean
) {
    DEBUG_APK(
        displayName = "Debug",
        format = "APK",
        purpose = "Daily testing",
        settings = "Development build, profiler, debug panel, cheats",
        developmentFlags = true,
        signed = false
    ),
    RELEASE_CANDIDATE_AAB(
        displayName = "Release Candidate",
        format = "AAB",
        purpose = "Play internal testing",
        settings = "IL2CPP, no dev flags, minify/strip, signed",
        developmentFlags = false,
        signed = true
    ),
    RELEASE_AAB(
        displayName = "Release",
        format = "AAB",
        purpose = "Production",
        settings = "Same as RC + final version code",
        developmentFlags = false,
        signed = true
    );

    val shipsToPlayers: Boolean get() = this == RELEASE_AAB
}

/**
 * Section 2.3 to 2.5: signing, the editor build entry point and the CI jobs.
 *
 * Nothing here holds a secret. The keystore path, store password and key password
 * are read from the environment at build time, which is why the repository can
 * carry the *rules* without carrying the key.
 */
object BuildPipeline {

    // 2.3 Signing
    const val SIGNING_MODE = "Play App Signing"
    const val KEYSTORE_STORED_IN_GIT = false
    const val KEYSTORE_BACKUP_COPIES = 2
    const val KEYSTORE_SECRETS_FROM_ENV = true
    val KEYSTORE_ENV_VARS: List<String> = listOf("KEYSTORE_PATH", "STORE_PASSWORD", "KEY_PASSWORD")
    const val KEYSTORE_ALIAS_RECORDED_IN_PASSWORD_MANAGER = true

    // 2.4 Build script
    const val SCRIPT_FILE = "BuildScript.cs"
    const val BUILD_METHOD = "BuildScript.BuildAndroidAab"
    const val OUTPUT_DIRECTORY = "Builds/"
    const val BATCH_COMMAND =
        "Unity -batchmode -quit -projectPath . -executeMethod BuildScript.BuildAndroidAab -logFile build.log"

    // 2.5 CI
    const val CI_PROVIDER = "GitHub Actions + GameCI"
    val CI_JOBS: List<String> = listOf(
        "EditMode and PlayMode tests on each PR",
        "APK artifact on develop",
        "AAB artifact on release tags"
    )

    /** A release build must never carry the debug surface. */
    fun allowsDebugFlags(type: BuildType): Boolean = type.developmentFlags

    fun mustBeMinified(type: BuildType): Boolean = type != BuildType.DEBUG_APK

    fun requiresSignature(type: BuildType): Boolean = type.signed

    fun uploadFormatIsAab(): Boolean = BuildType.RELEASE_AAB.format == "AAB"

    fun batchCommandFor(method: String = BUILD_METHOD): String =
        BATCH_COMMAND.replace(BUILD_METHOD, method)

    /** The About readout, in one place so Settings cannot drift from the build file. */
    fun aboutLine(version: AppVersion = AppVersion.CURRENT): String = version.aboutLine()
}
