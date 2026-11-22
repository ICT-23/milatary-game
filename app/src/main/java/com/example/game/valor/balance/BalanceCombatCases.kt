package com.example.game.valor.balance

import com.example.game.valor.art.WeaponClass
import com.example.game.vanguard.CaseRecorder
import com.example.game.vanguard.ContractCase
import com.example.game.vanguard.approxEquals
import com.example.game.vanguard.observed

/** Section 1 reading rules plus the section 4.3 time-to-kill sanity targets. */
object BalanceCombatCases {

    fun run(): List<ContractCase> {
        val r = CaseRecorder(BalanceAudit.GROUP_COMBAT)

        r.check("Head multiplier", "Head x2.5", approxEquals(HitRegion.HEAD.multiplier, 2.5f),
            observed(HitRegion.HEAD.multiplier, 2.5f))
        r.check("Limb multiplier", "Limb x0.7", approxEquals(HitRegion.LIMB.multiplier, 0.7f),
            observed(HitRegion.LIMB.multiplier, 0.7f))
        r.check("Torso multiplier", "Torso x1.0", approxEquals(HitRegion.TORSO.multiplier, 1f),
            observed(HitRegion.TORSO.multiplier, 1f))
        r.check(
            "Rifle falloff floor", "default 0.5 for rifles",
            approxEquals(DamageMath.minDamageMultFor(WeaponClass.RIFLE), 0.5f),
            observed(DamageMath.minDamageMultFor(WeaponClass.RIFLE), 0.5f)
        )
        r.check(
            "Shotgun and SMG falloff floor", "0.3 for shotguns/SMG",
            approxEquals(DamageMath.minDamageMultFor(WeaponClass.SHOTGUN), 0.3f) &&
                approxEquals(DamageMath.minDamageMultFor(WeaponClass.SMG), 0.3f),
            observed(
                "${DamageMath.minDamageMultFor(WeaponClass.SHOTGUN)}/" +
                    DamageMath.minDamageMultFor(WeaponClass.SMG),
                "0.3/0.3"
            )
        )
        r.check(
            "Point blank damage", "no falloff before the muzzle",
            approxEquals(DamageMath.damageAtRange(28f, 0f, 60f, 0.5f), 28f),
            observed(DamageMath.damageAtRange(28f, 0f, 60f, 0.5f), 28f)
        )
        r.check(
            "Falloff is linear", "half range = halfway to the floor",
            approxEquals(DamageMath.damageAtRange(28f, 30f, 60f, 0.5f), 21f),
            observed(DamageMath.damageAtRange(28f, 30f, 60f, 0.5f), 21f)
        )
        r.check(
            "Range floor reached", "at the listed range damage equals minDamageMult",
            approxEquals(DamageMath.damageAtRange(28f, 60f, 60f, 0.5f), 14f),
            observed(DamageMath.damageAtRange(28f, 60f, 60f, 0.5f), 14f)
        )
        r.check(
            "Beyond range clamp", "damage never drops below the floor",
            approxEquals(DamageMath.damageAtRange(28f, 400f, 60f, 0.5f), 14f),
            observed(DamageMath.damageAtRange(28f, 400f, 60f, 0.5f), 14f)
        )
        r.check(
            "Pellet damage adds", "9 x 8 pellets = 72 per trigger pull",
            approxEquals(DamageMath.shotDamage(9f, 8), 72f),
            observed(DamageMath.shotDamage(9f, 8), 72f)
        )
        r.check(
            "Lv1 AR needs four body shots", "the 0.6 s sanity target quotes 4 body shots",
            DamageMath.bodyShotsToKill(28f) == 4, observed(DamageMath.bodyShotsToKill(28f), 4)
        )
        r.check(
            "Lv1 AR cyclic rate", "650 RPM",
            approxEquals(WeaponCatalog.byId("ar_vanguard")?.rpm ?: 0f, 650f),
            observed(WeaponCatalog.byId("ar_vanguard")?.rpm, 650f)
        )
        r.check(
            "Burst fits the target window", "4 rounds at 650 RPM finish inside 0.6 s",
            DamageMath.secondsForShots(4, 650f) < TimeToKillTargets.AR_VS_RIFLEMAN_SECONDS,
            observed(DamageMath.secondsForShots(4, 650f), "< 0.6")
        )
        r.check(
            "Head damage converts", "28 damage deals 70 to the head",
            approxEquals(DamageMath.regionDamage(28f, HitRegion.HEAD), 70f),
            observed(DamageMath.regionDamage(28f, HitRegion.HEAD), 70f)
        )
        r.check(
            "Headshot on a rifleman", "two head hits drop a 100 HP enemy",
            DamageMath.shotsToKill(28f, HitRegion.HEAD) == 2,
            observed(DamageMath.shotsToKill(28f, HitRegion.HEAD), 2)
        )
        r.check(
            "Sniper one-shot headshot", "140 on the head surpasses 100 HP",
            DamageMath.shotsToKill(140f, HitRegion.HEAD) == 1,
            observed(DamageMath.shotsToKill(140f, HitRegion.HEAD), 1)
        )
        r.check(
            "Heavy focused fire", "focused rifle fire beats the 3 s target",
            EnemyCatalog.heavyTimeToKill(28f, 650f) < TimeToKillTargets.HEAVY_FOCUSED_SECONDS,
            observed(EnemyCatalog.heavyTimeToKill(28f, 650f), "< 3.0")
        )
        r.check(
            "Heavy with AP rounds", "1.5 s with armour-piercing",
            approxEquals(
                EnemyCatalog.heavyTimeToKill(28f, 650f, armorPiercingSeconds = true),
                TimeToKillTargets.HEAVY_ARMOR_PIERCING_SECONDS
            ),
            observed(
                EnemyCatalog.heavyTimeToKill(28f, 650f, armorPiercingSeconds = true),
                TimeToKillTargets.HEAVY_ARMOR_PIERCING_SECONDS
            )
        )
        r.check(
            "Cover beats open ground", "cover must reduce damage to over 10 s",
            TimeToKillTargets.COVER_MUST_EXCEED_SECONDS > TimeToKillTargets.RIFLEMAN_VS_PLAYER_SECONDS_MAX,
            observed(
                TimeToKillTargets.COVER_MUST_EXCEED_SECONDS,
                "> ${TimeToKillTargets.RIFLEMAN_VS_PLAYER_SECONDS_MAX}"
            )
        )
        r.check(
            "Player base health", "100 HP before gear",
            approxEquals(TimeToKillTargets.PLAYER_BASE_HEALTH, 100f),
            observed(TimeToKillTargets.PLAYER_BASE_HEALTH, 100f)
        )
        r.check(
            "Armour absorbs a share", "a T1 vest takes 40 of a 100 damage hit",
            approxEquals(ArmorMath.absorbedDamage(100f, 50f, 0.40f), 40f),
            observed(ArmorMath.absorbedDamage(100f, 50f, 0.40f), 40f)
        )

        return r.result()
    }
}
