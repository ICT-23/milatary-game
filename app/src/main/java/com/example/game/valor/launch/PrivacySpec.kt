package com.example.game.valor.launch

/** Section 5.1: one document the store checks for. */
data class LegalDocument(val name: String, val requirement: String)

/** Section 5.1: the paperwork that must exist before the first upload. */
object LegalDocuments {

    val ALL: List<LegalDocument> = listOf(
        LegalDocument("Privacy Policy", "Hosted URL"),
        LegalDocument("Terms of Service", "Hosted document"),
        LegalDocument("Data deletion", "Instructions / page"),
        LegalDocument("Support email", "Monitored inbox"),
        LegalDocument("Website", "Support website")
    )

    fun documentNamed(name: String): LegalDocument? =
        ALL.firstOrNull { it.name.equals(name.trim(), ignoreCase = true) }

    /** Every document is required; the store form rejects an empty one. */
    fun missingFrom(configured: Iterable<String>): List<String> {
        val present = configured.map { it.trim().lowercase() }.toSet()
        return ALL.map { it.name }.filterNot { it.lowercase() in present }
    }
}

/** Section 5.2: one line of the Google Play checklist. */
data class PlayChecklistItem(
    val id: String,
    val requirement: String,
    val verification: String
)

/** Section 5.2: the checklist to re-verify against current Play policy before release. */
object PlayChecklist {

    val ITEMS: List<PlayChecklistItem> = listOf(
        PlayChecklistItem(
            "developer_account",
            "Developer account (one-time fee) verified",
            "Play Console account status"
        ),
        PlayChecklistItem(
            "app_content",
            "App content: privacy policy URL, ads declaration (contains ads: Yes), " +
                "target audience & content, data safety form (ad ID, analytics, purchases, crash logs)",
            "App content section complete"
        ),
        PlayChecklistItem(
            "content_rating",
            "Content rating questionnaire (IARC) - expect Teen/12+ (violence); answer honestly",
            "IARC certificate issued"
        ),
        PlayChecklistItem(
            "target_api",
            "Target API level meets Play's current requirement",
            "Play Console warning-free"
        ),
        PlayChecklistItem(
            "artifact",
            "64-bit builds, AAB format",
            "Upload accepted"
        ),
        PlayChecklistItem(
            "permissions",
            "Permissions minimal (INTERNET, ACCESS_NETWORK_STATE; avoid unnecessary ones)",
            "Manifest diff reviewed"
        ),
        PlayChecklistItem(
            "ads_consent",
            "Ads SDK compliance + consent flow (UMP for EEA/UK; India/US state privacy rules)",
            "Consent dialog shown on first launch"
        ),
        PlayChecklistItem(
            "billing",
            "Billing via Google Play Billing only",
            "No external payment path"
        ),
        PlayChecklistItem(
            "testing_track",
            "Testing track requirement for new personal developer accounts " +
                "(closed testing for the required period)",
            "Closed test completed"
        ),
        PlayChecklistItem(
            "store_assets",
            "Store listing assets ready (Section 7)",
            "Listing preview approved"
        )
    )

    /** Section 5.2: the only two permissions the game is allowed to request. */
    val ALLOWED_PERMISSIONS: List<String> = listOf(
        "android.permission.INTERNET",
        "android.permission.ACCESS_NETWORK_STATE"
    )

    const val DECLARES_ADS = true
    const val TARGET_RATING = "Teen / 12+"
    const val SIXTY_FOUR_BIT = true

    fun item(id: String): PlayChecklistItem? = ITEMS.firstOrNull { it.id == id }

    fun outstanding(verified: Iterable<String>): List<PlayChecklistItem> {
        val done = verified.toSet()
        return ITEMS.filterNot { it.id in done }
    }

    /** Anything outside the two named permissions is a policy risk. */
    fun unnecessaryPermissions(declared: Iterable<String>): List<String> =
        declared.filterNot { it in ALLOWED_PERMISSIONS }

    fun isComplete(verified: Iterable<String>): Boolean = outstanding(verified).isEmpty()
}

/**
 * Section 5.3: what the age gate learned.
 *
 * [UNKNOWN] is the state the document calls "age unknown"; it is treated exactly
 * like an under-13 answer, never better.
 */
enum class AgeBand(val displayName: String) {
    UNDER_13("Under 13"),
    THIRTEEN_OR_OLDER("13 or older"),
    UNKNOWN("Not stated")
}

/** Section 5.3: the three screens the first launch walks through. */
enum class ConsentStep { AGE_GATE, CONSENT_DIALOG, COMPLETE }

/**
 * Section 5.3: the recorded answer. The two booleans are the *effective* values,
 * already forced down when the age band does not allow them.
 */
data class ConsentState(
    val ageBand: AgeBand = AgeBand.UNKNOWN,
    val analyticsAllowed: Boolean = false,
    val personalizedAdsAllowed: Boolean = false,
    val decided: Boolean = false
) {
    /** "If a player is under 13 or age unknown -> non-personalized ads only". */
    val nonPersonalizedAdsOnly: Boolean get() = !personalizedAdsAllowed

    /** "persistent 'Privacy settings' entry in Settings" - always present. */
    val privacySettingsEntryVisible: Boolean get() = true

    /** Nothing beyond essentials may carry an identifier. */
    val identifiersAllowed: Boolean get() = analyticsAllowed

    fun describe(): String =
        "${ageBand.displayName} - ads: " +
            (if (personalizedAdsAllowed) "personalized" else "non-personalized") +
            ", analytics: " + (if (analyticsAllowed) "on" else "essential only")
}

/**
 * Section 5.3: age gate -> consent dialog -> persistent privacy entry.
 *
 * The gate never guesses upward: a missing answer is [AgeBand.UNKNOWN] and an
 * under-13 answer overrides whatever the dialog asked for.
 */
class ConsentGate {

    var step: ConsentStep = ConsentStep.AGE_GATE
        private set

    var state: ConsentState = ConsentState()
        private set

    /** Neutral gate: "prefer not to say" is a plain answer, not a nudge. */
    fun chooseAge(ageYears: Int?): ConsentStep {
        state = state.copy(ageBand = bandFor(ageYears))
        step = ConsentStep.CONSENT_DIALOG
        return step
    }

    /** The dialog's two switches; the age rule is applied here, once. */
    fun submit(personalizedAds: Boolean, analytics: Boolean): ConsentState {
        state = stateFor(state.ageBand, personalizedAds, analytics)
        step = ConsentStep.COMPLETE
        return state
    }

    /** Re-opening from Settings edits the answer but never the age band. */
    fun editInSettings(personalizedAds: Boolean, analytics: Boolean): ConsentState {
        state = stateFor(state.ageBand, personalizedAds, analytics)
        return state
    }

    fun reset() {
        step = ConsentStep.AGE_GATE
        state = ConsentState()
    }

    companion object {

        fun bandFor(ageYears: Int?): AgeBand = when {
            ageYears == null -> AgeBand.UNKNOWN
            ageYears < 13 -> AgeBand.UNDER_13
            else -> AgeBand.THIRTEEN_OR_OLDER
        }

        /** The one place the under-13 / unknown override is applied. */
        fun stateFor(ageBand: AgeBand, personalizedAds: Boolean, analytics: Boolean): ConsentState {
            val allowed = ageBand == AgeBand.THIRTEEN_OR_OLDER
            return ConsentState(
                ageBand = ageBand,
                analyticsAllowed = allowed && analytics,
                personalizedAdsAllowed = allowed && personalizedAds,
                decided = true
            )
        }

        fun requiresNonPersonalizedAds(ageBand: AgeBand): Boolean =
            ageBand != AgeBand.THIRTEEN_OR_OLDER

        /** What a screen shows while the player has not decided yet. */
        fun undecided(): ConsentState = ConsentState()
    }
}

/** Section 5.4: the intellectual-property rules the content has to satisfy. */
object IpSafety {

    const val ORIGINAL_TITLE = true
    const val ORIGINAL_CHARACTERS = true
    const val ORIGINAL_STORY = true
    const val ORIGINAL_ART_MUSIC_VOICE = true
    const val REUSES_ORIGINAL_GAME_ASSETS = false
    const val SHOWS_REAL_BRAND_LOGOS = false
    const val GENERIC_WEAPON_NAMES = true

    /** "Consider an in-game alternate name like 'Medal of Valor' ... (editable string)". */
    const val AWARD_NAME = "Medal of Valor"
    const val REAL_AWARD_NAME_USED_AS_BRAND = false
    const val REAL_AWARD_REFERENCED_RESPECTFULLY_IN_STORY = true

    const val LICENSES_FILE = "ASSET_LICENSES.md"
    const val CREDITS_SCREEN = true

    fun safeToShip(
        reusesAssets: Boolean = REUSES_ORIGINAL_GAME_ASSETS,
        showsLogos: Boolean = SHOWS_REAL_BRAND_LOGOS,
        usesAwardAsBrand: Boolean = REAL_AWARD_NAME_USED_AS_BRAND
    ): Boolean = !reusesAssets && !showsLogos && !usesAwardAsBrand

    /** The award name is a string the writer may change without a code change. */
    fun awardName(custom: String? = null): String =
        custom?.takeIf { it.isNotBlank() } ?: AWARD_NAME
}

/** Section 5.5: how the war is depicted and which factions are fictional. */
object ContentNotes {

    val FICTIONAL_MODERN_FACTIONS: List<String> = listOf("Northern Coalition", "Red Meridian")

    const val REAL_HATE_GROUPS = false
    const val REAL_TERROR_OR_ATROCITY_RECREATION = false
    const val REAL_LIVING_POLITICAL_FIGURES = false
    const val DEPICTS_WAR_WITH_RESPECT = true

    /** No real organisation may appear in the enemy roster. */
    fun mentionsRealActor(text: String): Boolean {
        val lower = text.lowercase()
        return REAL_ACTOR_MARKERS.any { marker -> lower.contains(marker) }
    }

    private val REAL_ACTOR_MARKERS = listOf(
        "taliban",
        "isis",
        "al-qaeda",
        "nazi party",
        "ss division",
        "putin",
        "biden",
        "modi"
    )

    fun usesFictionalFaction(name: String): Boolean =
        FICTIONAL_MODERN_FACTIONS.any { it.equals(name.trim(), ignoreCase = true) }
}
