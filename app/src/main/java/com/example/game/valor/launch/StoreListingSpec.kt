package com.example.game.valor.launch

/** Section 7.1: one store asset and its spec. */
data class StoreAsset(val asset: String, val spec: String)

/** Section 7.2: the listing copy, kept as the document writes it. */
object ListingCopy {

    const val TITLE = "Valor Academy: Rise of the Commander"

    const val SHORT_DESCRIPTION =
        "Train. Fight. Rise through the ranks in realistic 3D war missions."

    /** The five-paragraph outline, in order. */
    val FULL_DESCRIPTION_OUTLINE: List<String> = listOf(
        "Hook paragraph (fantasy: from recruit to commander).",
        "Key features bullets: realistic 3D graphics; 60-mission story campaign across 6 eras; " +
            "build and customize weapons; 20 ranks; drone strikes; squad commands; offline play.",
        "Replay content: Survival, Daily Ops, Weekly Challenges, New Game+.",
        "Fair play promise: no forced ads during missions.",
        "Call to action."
    )

    /** "(avoid competitor brand names)". */
    val KEYWORDS: List<String> = listOf(
        "military game",
        "army shooter",
        "3D war",
        "offline shooting game",
        "commander",
        "sniper",
        "soldier training",
        "WW2 game",
        "tactical shooter"
    )

    const val SHORT_MAX_CHARS = 80
    const val FULL_MAX_CHARS = 4_000

    fun shortDescriptionValid(text: String = SHORT_DESCRIPTION): Boolean =
        text.isNotBlank() && text.length <= SHORT_MAX_CHARS

    /** The full description is an outline of at most 4,000 characters when rendered. */
    fun fullDescription(): String = buildString {
        appendLine(TITLE)
        FULL_DESCRIPTION_OUTLINE.forEach { line -> appendLine(line) }
    }.trim()

    fun fullDescriptionValid(): Boolean = fullDescription().length <= FULL_MAX_CHARS

    fun keywordCount(): Int = KEYWORDS.size

    /** No competitor brand may be used as a keyword. */
    fun keywordAllowed(keyword: String): Boolean =
        COMPETITOR_MARKERS.none { keyword.lowercase().contains(it) }

    private val COMPETITOR_MARKERS = listOf("medal of honor", "call of duty", "battlefield", "pubg")
}

/** Section 7.1 and 7.3: the asset specs and the screenshot plan. */
object StoreListing {

    val ASSETS: List<StoreAsset> = listOf(
        StoreAsset("App icon", "512x512 PNG, original emblem (e.g. shield + star + silhouette)"),
        StoreAsset("Feature graphic", "1024x500"),
        StoreAsset("Phone screenshots", "6-8, landscape, 1920x1080 min, with short captions"),
        StoreAsset("Tablet screenshots", "4 (7\"/10\")"),
        StoreAsset("Trailer", "30-45 s YouTube link"),
        StoreAsset("Short description", "<= 80 chars"),
        StoreAsset("Full description", "<= 4,000 chars")
    )

    val SCREENSHOT_PLAN: List<String> = listOf(
        "Cinematic hero shot with logo",
        "Third-person firefight at golden hour",
        "Gunsmith screen (stats + 3D weapon)",
        "Rank/medal ceremony",
        "Drone strike tactical map",
        "Different eras montage",
        "Squad commands",
        "Weather/graphics showcase"
    )

    /** "0-5 s hook explosion/cinematic, 5-25 s variety, 25-40 s progression, end with logo". */
    val TRAILER_BEATS: List<String> = listOf(
        "0-5 s hook explosion/cinematic",
        "5-25 s variety",
        "25-40 s progression",
        "End with logo"
    )

    const val ICON_PX = 512
    const val FEATURE_GRAPHIC_WIDTH = 1024
    const val FEATURE_GRAPHIC_HEIGHT = 500
    const val MIN_PHONE_SCREENSHOTS = 6
    const val MAX_PHONE_SCREENSHOTS = 8
    const val TABLET_SCREENSHOTS = 4
    const val MIN_SCREENSHOT_WIDTH = 1920
    const val MIN_SCREENSHOT_HEIGHT = 1080
    const val TRAILER_MIN_SECONDS = 30
    const val TRAILER_MAX_SECONDS = 45

    fun assetNamed(asset: String): StoreAsset? =
        ASSETS.firstOrNull { it.asset.equals(asset.trim(), ignoreCase = true) }

    fun phoneScreenshotCountValid(count: Int): Boolean =
        count in MIN_PHONE_SCREENSHOTS..MAX_PHONE_SCREENSHOTS

    fun screenshotResolutionValid(width: Int, height: Int): Boolean =
        width >= MIN_SCREENSHOT_WIDTH && height >= MIN_SCREENSHOT_HEIGHT && width > height

    fun trailerLengthValid(seconds: Int): Boolean =
        seconds in TRAILER_MIN_SECONDS..TRAILER_MAX_SECONDS

    fun iconValid(width: Int, height: Int): Boolean =
        width == ICON_PX && height == ICON_PX

    fun featureGraphicValid(width: Int, height: Int): Boolean =
        width == FEATURE_GRAPHIC_WIDTH && height == FEATURE_GRAPHIC_HEIGHT

    /** The whole listing is ready only when copy and assets both clear their limits. */
    fun listingReady(phoneScreenshots: Int, tabletScreenshots: Int, trailerSeconds: Int): Boolean =
        ListingCopy.shortDescriptionValid() &&
            ListingCopy.fullDescriptionValid() &&
            phoneScreenshotCountValid(phoneScreenshots) &&
            tabletScreenshots == TABLET_SCREENSHOTS &&
            trailerLengthValid(trailerSeconds)
}
