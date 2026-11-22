package com.example.game.valor.launch

/** Section 4.1: the four fair-play promises every product must obey. */
object MonetizationPrinciples {

    const val FORCED_ADS_DURING_GAMEPLAY = false
    const val ADS_DURING_CUTSCENES = false
    const val PAY_TO_WIN = false
    const val EVERY_ITEM_EARNABLE = true
    const val ODDS_TRANSPARENT = true
    const val PITY_TIMERS = true
    const val MANIPULATIVE_COUNTDOWN_PRESSURE = false
    const val AGE_APPROPRIATE_STORE_INFO = true

    val RULES: List<String> = listOf(
        "No forced ads during gameplay or cutscenes.",
        "No pay-to-win; every gameplay item earnable.",
        "Transparent odds for any random reward; pity timers.",
        "Parental-friendly: age-appropriate store info; no manipulative countdown pressure."
    )

    fun acceptsPurchase(earnableInGame: Boolean, randomWithHiddenOdds: Boolean): Boolean =
        earnableInGame && !randomWithHiddenOdds

    fun ruleCount(): Int = RULES.size
}

/** Section 4.2: how a product is charged for. */
enum class ProductType(val displayName: String) {
    ONE_TIME("One-time"),
    CONSUMABLE("Consumable"),
    NON_CONSUMABLE("Non-consumable"),
    TIME_LIMITED("Time-limited")
}

/**
 * Section 4.2: one store row. There is no price number here on purpose - the
 * document says "adjust per region - use Play's local price tiers" - so only the
 * tier name ships and Play holds the actual money.
 */
data class StoreProduct(
    val id: String,
    val name: String,
    val type: ProductType,
    val priceTier: String,
    val contents: List<String> = emptyList()
)

/** Section 4.2: the five products plus the India-friendly pricing stance. */
object StoreCatalogue {

    const val REMOVE_ADS_ID = "remove_ads"
    const val STARTER_PACK_ID = "starter_pack"

    /** "Starter Pack (Remove Ads + 2 weapons + 500 Medals + Steel)". */
    const val STARTER_PACK_MEDALS = 500
    const val STARTER_PACK_WEAPONS = 2

    val PRODUCTS: List<StoreProduct> = listOf(
        StoreProduct(
            id = REMOVE_ADS_ID,
            name = "Remove Ads (interstitials)",
            type = ProductType.ONE_TIME,
            priceTier = "Low",
            contents = listOf("Interstitials removed")
        ),
        StoreProduct(
            id = STARTER_PACK_ID,
            name = "Starter Pack",
            type = ProductType.ONE_TIME,
            priceTier = "Low-mid",
            contents = listOf(
                "Remove Ads",
                "$STARTER_PACK_WEAPONS weapons",
                "$STARTER_PACK_MEDALS Medals",
                "Steel"
            )
        ),
        StoreProduct("medal_pack_s", "Medal pack S", ProductType.CONSUMABLE, "Tiered"),
        StoreProduct("medal_pack_m", "Medal pack M", ProductType.CONSUMABLE, "Tiered"),
        StoreProduct("medal_pack_l", "Medal pack L", ProductType.CONSUMABLE, "Tiered"),
        StoreProduct("cosmetic_bundle", "Cosmetic skin bundles", ProductType.NON_CONSUMABLE, "Tiered"),
        StoreProduct("season_pass", "Season / Battle pass", ProductType.TIME_LIMITED, "Mid")
    )

    const val LOCAL_PRICE_TIERS = true
    const val UPI_FRIENDLY_CHECKOUT = true
    const val EMERGING_MARKET_STARTER_PACK = true

    fun product(id: String): StoreProduct? = PRODUCTS.firstOrNull { it.id == id }

    /** "Billing via Google Play Billing only" (Section 5.2). */
    fun billingIsPlayOnly(): Boolean = true

    fun productCount(): Int = PRODUCTS.size

    fun removesInterstitials(id: String): Boolean = id == REMOVE_ADS_ID || id == STARTER_PACK_ID

    /** Every consumable must be spendable in play; none of them may gate progress. */
    fun consumables(): List<StoreProduct> = PRODUCTS.filter { it.type == ProductType.CONSUMABLE }
}

/** Section 4.3: the three ad shapes, one of which is explicitly not used. */
enum class AdKind(val displayName: String) {
    REWARDED("Rewarded"),
    INTERSTITIAL("Interstitial"),
    BANNER("Banner")
}

/** Section 4.3: one placement row: what it is, when it may appear, what it pays. */
data class LiveAdPlacement(
    val placement: String,
    val kind: AdKind,
    val trigger: String,
    val reward: String,
    val used: Boolean = true
)

/** Section 4.3 and the caps that keep the placements from becoming pressure. */
object AdPlacements {

    /** ">= 3 missions apart" - the same number the Implementation Plan uses. */
    const val MIN_MISSIONS_BETWEEN = 3

    /** "never in first 20 minutes". */
    const val FIRST_SESSION_MINUTES = 20

    /** "<= 1 interstitial per 5 minutes". */
    const val MIN_MINUTES_BETWEEN = 5

    const val FREE_CRATE_COOLDOWN_SECONDS = 4 * 60 * 60
    const val DAILY_MEDAL_AD_CAP = 3
    const val DAILY_MEDAL_AD_MEDALS = 2
    const val REVIVE_HP_FRACTION = 0.5f
    const val DOUBLE_REWARD_MULTIPLIER = 2

    val ROWS: List<LiveAdPlacement> = listOf(
        LiveAdPlacement(
            "Double rewards",
            AdKind.REWARDED,
            "Results screen button",
            "x2 Cash/XP of that mission"
        ),
        LiveAdPlacement(
            "Revive",
            AdKind.REWARDED,
            "Fail screen button",
            "50% HP revive once per mission"
        ),
        LiveAdPlacement(
            "Free crate",
            AdKind.REWARDED,
            "Armory shop (cooldown 4 h)",
            "Supply crate"
        ),
        LiveAdPlacement(
            "Extra daily medals",
            AdKind.REWARDED,
            "Daily tab (max $DAILY_MEDAL_AD_CAP)",
            "$DAILY_MEDAL_AD_MEDALS Medals each"
        ),
        LiveAdPlacement(
            "Interstitial",
            AdKind.INTERSTITIAL,
            "Between missions only, >= $MIN_MISSIONS_BETWEEN missions apart, " +
                "never after failing, never in first $FIRST_SESSION_MINUTES minutes",
            "-"
        ),
        LiveAdPlacement("Banner", AdKind.BANNER, "Not used (keeps UI clean)", "-", used = false)
    )

    fun rewarded(): List<LiveAdPlacement> = ROWS.filter { it.kind == AdKind.REWARDED }

    fun interstitial(): LiveAdPlacement? = ROWS.firstOrNull { it.kind == AdKind.INTERSTITIAL }

    /** A placement is legal only when the document marks it as used. */
    fun isUsed(placement: String): Boolean =
        ROWS.firstOrNull { it.placement.equals(placement.trim(), ignoreCase = true) }?.used == true

    fun bannersUsed(): Boolean = ROWS.any { it.kind == AdKind.BANNER && it.used }

    fun rowsFor(kind: AdKind): List<LiveAdPlacement> = ROWS.filter { it.kind == kind }
}

/**
 * Section 4.3: the interstitial gate.
 *
 * Every input is explicit so the rule can be exercised without a clock: the
 * caller passes the minutes played in the session and says whether the ad would
 * sit between missions.
 */
class InterstitialGate {

    var removeAdsOwned: Boolean = false
    private var missionsSinceAd: Int = AdPlacements.MIN_MISSIONS_BETWEEN
    private var lastAdMinute: Int = -AdPlacements.MIN_MINUTES_BETWEEN
    private var lastMissionFailed: Boolean = false
    private var shownCount: Int = 0

    fun missionCounter(): Int = missionsSinceAd

    fun interstitialCount(): Int = shownCount

    fun lastMissionWasFailure(): Boolean = lastMissionFailed

    fun recordMissionEnd(failed: Boolean) {
        missionsSinceAd += 1
        lastMissionFailed = failed
    }

    fun canShow(betweenMissions: Boolean, playedMinutes: Int): Boolean =
        betweenMissions &&
            !removeAdsOwned &&
            !lastMissionFailed &&
            missionsSinceAd >= AdPlacements.MIN_MISSIONS_BETWEEN &&
            playedMinutes >= AdPlacements.FIRST_SESSION_MINUTES &&
            playedMinutes - lastAdMinute >= AdPlacements.MIN_MINUTES_BETWEEN

    fun recordShown(playedMinutes: Int) {
        lastAdMinute = playedMinutes
        missionsSinceAd = 0
        shownCount += 1
    }
}

/** Section 4.3: "Free crate - Armory shop (cooldown 4 h)". */
object CrateCooldown {

    fun ready(lastClaimSeconds: Long, nowSeconds: Long): Boolean =
        lastClaimSeconds <= 0L || nowSeconds - lastClaimSeconds >= AdPlacements.FREE_CRATE_COOLDOWN_SECONDS

    fun remainingSeconds(lastClaimSeconds: Long, nowSeconds: Long): Long =
        (AdPlacements.FREE_CRATE_COOLDOWN_SECONDS - (nowSeconds - lastClaimSeconds)).coerceAtLeast(0L)

    /** "If no ad is available -> hide/disable the button" (no dead buttons). */
    fun buttonVisible(lastClaimSeconds: Long, nowSeconds: Long): Boolean =
        ready(lastClaimSeconds, nowSeconds)
}

/** Section 4.3: "Extra daily medals - Daily tab (max 3) - 2 Medals each". */
object DailyMedalAds {

    fun canWatch(watchedToday: Int): Boolean =
        watchedToday in 0 until AdPlacements.DAILY_MEDAL_AD_CAP

    fun remainingToday(watchedToday: Int): Int =
        (AdPlacements.DAILY_MEDAL_AD_CAP - watchedToday).coerceAtLeast(0)

    /** Watching never pays past the cap, even if the button is tapped twice. */
    fun medalsFor(watchedToday: Int): Int =
        if (canWatch(watchedToday)) AdPlacements.DAILY_MEDAL_AD_MEDALS else 0
}

/** Section 4.3: "Revive - 50% HP revive once per mission". */
object RevivePolicy {

    fun canOffer(usedThisMission: Boolean): Boolean = !usedThisMission

    fun hpAfterRevive(maxHp: Int): Int =
        kotlin.math.ceil(maxHp * AdPlacements.REVIVE_HP_FRACTION).toInt().coerceAtMost(maxHp)
}

/** Section 4.3: "Double rewards - x2 Cash/XP of that mission". */
object DoubleRewardPolicy {

    fun cash(base: Int): Int = base.coerceAtLeast(0) * AdPlacements.DOUBLE_REWARD_MULTIPLIER

    fun xp(base: Int): Int = base.coerceAtLeast(0) * AdPlacements.DOUBLE_REWARD_MULTIPLIER
}

/** Section 4.1: the odds any random reward must publish. */
data class CrateOdds(val weights: Map<String, Double>) {

    val total: Double get() = weights.values.sum()

    /** "Transparent odds" means the table adds up to a whole crate. */
    val isTransparent: Boolean
        get() = weights.isNotEmpty() && kotlin.math.abs(total - 1.0) <= 0.0001

    fun chance(tier: String): Double? = weights[tier]

    fun tiers(): List<String> = weights.keys.toList()

    companion object {
        /** "standard=1.0" or "common=0.6,rare=0.3,epic=0.1". */
        fun parse(text: String): CrateOdds? {
            val entries = text.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            if (entries.isEmpty()) return null
            val weights = linkedMapOf<String, Double>()
            entries.forEach { entry ->
                val name = entry.substringBefore("=").trim()
                val value = entry.substringAfter("=", "").trim().toDoubleOrNull() ?: return null
                if (name.isEmpty() || value < 0.0 || value > 1.0) return null
                weights[name] = value
            }
            return CrateOdds(weights)
        }
    }
}

/**
 * Section 4.1: the pity timer that sits beside the odds.
 *
 * [record] returns true once the next roll must be a win; [reset] is called when
 * the player actually receives the top tier.
 */
class PityCounter(val threshold: Int = RemoteConfig.DEFAULT_PITY_AFTER) {

    var misses: Int = 0
        private set

    val guaranteed: Boolean get() = threshold > 0 && misses >= threshold

    fun record(miss: Boolean): Boolean {
        if (miss) misses += 1
        return guaranteed
    }

    fun reset() {
        misses = 0
    }
}

/**
 * Section 4.4: the remote knobs, with safe defaults compiled into the build.
 *
 * The document names six kinds of value. Each one keeps at least one default here,
 * and [isSafe] fails the gate if a group is ever shipped empty.
 */
class RemoteConfig(private val overrides: Map<String, String> = emptyMap()) {

    private val values: Map<String, String> =
        DEFAULTS + overrides.filterKeys { key -> key in DEFAULTS }

    fun raw(key: String): String? = values[key]

    fun int(key: String, fallback: Int = 0): Int = values[key]?.toIntOrNull() ?: fallback

    fun double(key: String, fallback: Double = 0.0): Double = values[key]?.toDoubleOrNull() ?: fallback

    fun isDefault(key: String): Boolean = overrides[key] == null

    /** "auto" ships as the default: the day seed is derived, not downloaded. */
    fun seedList(): List<Long> =
        (values[DAILY_OP_SEED_LIST] ?: "auto")
            .split(",")
            .mapNotNull { it.trim().toLongOrNull() }

    fun hasDownloadedSeeds(): Boolean = seedList().isNotEmpty()

    fun eventDates(): Map<String, String> = parsePairs(values[EVENT_DATES].orEmpty())

    fun hotfixMultipliers(): Map<String, Double> =
        parsePairs(values[DIFFICULTY_HOTFIX].orEmpty()).mapNotNull { (key, value) ->
            value.toDoubleOrNull()?.let { key to it }
        }.toMap()

    fun odds(): CrateOdds? = values[CRATE_ODDS]?.let { CrateOdds.parse(it) }

    fun pityAfter(): Int = int(PITY_AFTER, DEFAULT_PITY_AFTER)

    fun interstitialMissionsBetween(): Int = int(AD_MIN_MISSIONS, AdPlacements.MIN_MISSIONS_BETWEEN)

    fun interstitialMinutesBetween(): Int = int(AD_MIN_MINUTES, AdPlacements.MIN_MINUTES_BETWEEN)

    fun interstitialFirstSessionMinutes(): Int = int(AD_FIRST_MINUTES, AdPlacements.FIRST_SESSION_MINUTES)

    /** Every group the document lists must carry at least one shipped default. */
    fun isSafe(): Boolean =
        GROUPS.all { group -> values.keys.any { it == group || it.startsWith("$group.") } } &&
            odds()?.isTransparent == true

    companion object {

        // The six groups named by Section 4.4.
        const val AD_FREQUENCY = "ad_frequency"
        const val REWARD_AMOUNTS = "reward_amounts"
        const val DAILY_OP_SEED_LIST = "daily_op_seed_list"
        const val EVENT_DATES = "event_dates"
        const val DIFFICULTY_HOTFIX = "difficulty_hotfix_multipliers"
        const val CRATE_ODDS = "crate_odds"

        val GROUPS: List<String> = listOf(
            AD_FREQUENCY,
            REWARD_AMOUNTS,
            DAILY_OP_SEED_LIST,
            EVENT_DATES,
            DIFFICULTY_HOTFIX,
            CRATE_ODDS
        )

        const val AD_MIN_MISSIONS = "ad_frequency.min_missions_between"
        const val AD_MIN_MINUTES = "ad_frequency.min_minutes_between"
        const val AD_FIRST_MINUTES = "ad_frequency.first_session_minutes"
        const val AD_CAP_PER_FIVE_MINUTES = "ad_frequency.cap_per_five_minutes"
        const val REWARD_DOUBLE = "reward_amounts.double_reward_multiplier"
        const val REWARD_REVIVE_PERCENT = "reward_amounts.revive_hp_percent"
        const val REWARD_DAILY_MEDALS = "reward_amounts.daily_medal_ad_medals"
        const val REWARD_DAILY_CAP = "reward_amounts.daily_medal_ad_cap"
        const val PITY_AFTER = "crate_odds.pity_after"

        const val DEFAULT_PITY_AFTER = 10

        /**
         * Safe defaults: the conservative crate is a single guaranteed tier, so a
         * build that never reaches the network still publishes honest odds.
         */
        val DEFAULTS: Map<String, String> = linkedMapOf(
            AD_MIN_MISSIONS to AdPlacements.MIN_MISSIONS_BETWEEN.toString(),
            AD_MIN_MINUTES to AdPlacements.MIN_MINUTES_BETWEEN.toString(),
            AD_FIRST_MINUTES to AdPlacements.FIRST_SESSION_MINUTES.toString(),
            AD_CAP_PER_FIVE_MINUTES to "1",
            REWARD_DOUBLE to AdPlacements.DOUBLE_REWARD_MULTIPLIER.toString(),
            REWARD_REVIVE_PERCENT to (AdPlacements.REVIVE_HP_FRACTION * 100).toInt().toString(),
            REWARD_DAILY_MEDALS to AdPlacements.DAILY_MEDAL_AD_MEDALS.toString(),
            REWARD_DAILY_CAP to AdPlacements.DAILY_MEDAL_AD_CAP.toString(),
            DAILY_OP_SEED_LIST to "auto",
            EVENT_DATES to "",
            DIFFICULTY_HOTFIX to "",
            CRATE_ODDS to "standard=1.0",
            PITY_AFTER to DEFAULT_PITY_AFTER.toString()
        )

        private fun parsePairs(text: String): Map<String, String> =
            text.split(",")
                .map { it.trim() }
                .filter { it.contains("=") }
                .associate { it.substringBefore("=").trim() to it.substringAfter("=").trim() }
    }
}
