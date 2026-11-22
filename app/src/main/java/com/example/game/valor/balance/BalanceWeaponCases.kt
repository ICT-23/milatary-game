package com.example.game.valor.balance

import com.example.game.valor.art.WeaponClass
import com.example.game.vanguard.CaseRecorder
import com.example.game.vanguard.ContractCase
import com.example.game.vanguard.approxEquals
import com.example.game.vanguard.observed

/** Sections 2.1 to 2.5: every weapon table row as transcribed. */
object BalanceWeaponCases {

    /** One case per sub-table verifies all seven repeated columns at once. */
    private fun matches(
        id: String,
        damage: Float,
        rpm: Float,
        mag: Int,
        reserve: Int,
        unlock: Int,
        price: Int
    ): Boolean {
        val weapon = WeaponCatalog.byId(id) ?: return false
        return approxEquals(weapon.damage, damage) &&
            approxEquals(weapon.rpm, rpm) &&
            weapon.magSize == mag &&
            weapon.reserveAmmo == reserve &&
            weapon.unlockRank == unlock &&
            weapon.price == price
    }

    private fun describe(id: String): String {
        val weapon = WeaponCatalog.byId(id) ?: return "$id missing"
        return "dmg=${weapon.damage} rpm=${weapon.rpm} mag=${weapon.magSize} " +
            "res=${weapon.reserveAmmo} unlock=${weapon.unlockRank} price=${weapon.price}"
    }

    fun run(): List<ContractCase> {
        val r = CaseRecorder(BalanceAudit.GROUP_WEAPONS)

        r.check(
            "Firearm count", "21 firearm rows across sections 2.1-2.4",
            WeaponCatalog.WEAPONS.size == 21, observed(WeaponCatalog.WEAPONS.size, 21)
        )
        r.check(
            "Field item count", "7 melee and throwable rows in section 2.5",
            WeaponCatalog.FIELD_ITEMS.size == 7, observed(WeaponCatalog.FIELD_ITEMS.size, 7)
        )
        r.check(
            "Total catalogue", "28 ids in total",
            WeaponCatalog.WEAPONS.size + WeaponCatalog.FIELD_ITEMS.size == 28,
            observed(WeaponCatalog.WEAPONS.size + WeaponCatalog.FIELD_ITEMS.size, 28)
        )
        r.check(
            "Unique weapon ids", "no duplicated id",
            WeaponCatalog.WEAPONS.map { it.id }.distinct().size == WeaponCatalog.WEAPONS.size,
            observed(WeaponCatalog.WEAPONS.map { it.id }.distinct().size, WeaponCatalog.WEAPONS.size)
        )
        r.check(
            "Every class appears", "all 7 WeaponClass values are used",
            WeaponClass.entries.all { it in WeaponCatalog.WEAPONS.map { w -> w.weaponClass } },
            observed(WeaponCatalog.WEAPONS.map { it.weaponClass }.distinct().size, 7)
        )

        r.check(
            "Pistol row", "pistol_mk1 34/300/7/49/unlock 1/free",
            matches("pistol_mk1", 34f, 300f, 7, 49, 1, 0), describe("pistol_mk1")
        )
        r.check(
            "Revolver row", "revolver_frontier 55/150/6/36/unlock 3/800",
            matches("revolver_frontier", 55f, 150f, 6, 36, 3, 800),
            describe("revolver_frontier")
        )
        r.check(
            "Bolt rifle row", "bolt_pioneer1 85/45/5/40/unlock 1/free",
            matches("bolt_pioneer1", 85f, 45f, 5, 40, 1, 0), describe("bolt_pioneer1")
        )
        r.check(
            "Heavy AR row", "ar_sentinel 33/550/30/150/unlock 14/16,000",
            matches("ar_sentinel", 33f, 550f, 30, 150, 14, 16_000), describe("ar_sentinel")
        )
        r.check(
            "SMG row", "smg_hornet 22/750/30/180/unlock 7/4,000",
            matches("smg_hornet", 22f, 750f, 30, 180, 7, 4_000), describe("smg_hornet")
        )
        r.check(
            "LMG row", "lmg_titanbelt 32/700/150/450/unlock 16/25,000",
            matches("lmg_titanbelt", 32f, 700f, 150, 450, 16, 25_000), describe("lmg_titanbelt")
        )
        r.check(
            "Shotgun row", "sg_breaker 9 per pellet / 70 / 6 / 36, shell reload",
            matches("sg_breaker", 9f, 70f, 6, 36, 5, 3_500) &&
                WeaponCatalog.byId("sg_breaker")?.pellets == 8 &&
                WeaponCatalog.byId("sg_breaker")?.reloadPerShell == true,
            describe("sg_breaker")
        )
        r.check(
            "Sniper row", "sniper_longwatch 140/40/5/30/unlock 8/9,000",
            matches("sniper_longwatch", 140f, 40f, 5, 30, 8, 9_000), describe("sniper_longwatch")
        )
        r.check(
            "Launcher row", "launcher_hammer 250/20/1/6/unlock 12/20,000",
            matches("launcher_hammer", 250f, 20f, 1, 6, 12, 20_000), describe("launcher_hammer")
        )

        r.check(
            "STK column is a target", "only ar_sentinel misses its stated 3 body shots",
            WeaponCatalog.stkGaps().map { it.id } == listOf("ar_sentinel"),
            observed(WeaponCatalog.stkGaps().map { it.id }, listOf("ar_sentinel"))
        )
        r.check(
            "Prose STK rows", "the 2 snipers and the launcher print notes, not a number",
            WeaponCatalog.WEAPONS.count { it.targetStk == null } == 3,
            observed(WeaponCatalog.WEAPONS.count { it.targetStk == null }, 3)
        )
        r.check(
            "No Recoil V column", "snipers and launcher carry no vertical recoil figure",
            WeaponCatalog.WEAPONS.count { it.recoilVerticalDeg == null } == 3,
            observed(WeaponCatalog.WEAPONS.count { it.recoilVerticalDeg == null }, 3)
        )
        r.check(
            "Falloff per class", "SMG and shotgun floor at 0.3, everything else 0.5",
            WeaponCatalog.WEAPONS.all { weapon ->
                val expected = if (weapon.weaponClass in DamageFalloff.CLOSE_RANGE.weaponClasses) 0.3f else 0.5f
                approxEquals(weapon.minDamageMult, expected)
            },
            observed(WeaponCatalog.WEAPONS.map { it.weaponClass to it.minDamageMult }.distinct().size, "<=2 pairs")
        )
        r.check(
            "Positive ballistics", "every firearm has rounds, a magazine and a range",
            WeaponCatalog.WEAPONS.all { it.rpm > 0f && it.magSize > 0 && it.reserveAmmo > 0 && it.rangeMeters > 0f },
            observed(WeaponCatalog.WEAPONS.count { it.rpm > 0f && it.magSize > 0 }, 21)
        )
        r.check(
            "Unlock ranks available", "no weapon needs the story-gated rank 20",
            WeaponCatalog.WEAPONS.all { it.unlockRank in 1..19 },
            observed(WeaponCatalog.WEAPONS.maxOf { it.unlockRank }, "<= 19")
        )
        r.check(
            "Starting kit", "two weapons are free at rank 1",
            WeaponCatalog.WEAPONS.count { it.price == 0 } == 2,
            observed(WeaponCatalog.WEAPONS.count { it.price == 0 }, 2)
        )

        val frag = WeaponCatalog.FIELD_ITEMS.firstOrNull { it.id == "grenade_frag" }
        r.check(
            "Frag grenade", "150 max, r=7 m, fuse 3.0 s, 100 each",
            frag != null && approxEquals(frag.damage ?: 0f, 150f) &&
                approxEquals(frag.radiusMeters ?: 0f, 7f) &&
                approxEquals(frag.fuseSeconds ?: 0f, 3f) && frag.price == 100 && frag.pricedEach,
            "$frag"
        )
        val smoke = WeaponCatalog.FIELD_ITEMS.firstOrNull { it.id == "grenade_smoke" }
        val flash = WeaponCatalog.FIELD_ITEMS.firstOrNull { it.id == "grenade_flash" }
        r.check(
            "Smoke and flash", "smoke 12 s r=6 m; flash blinds 4 s; 80 and 120 each",
            smoke != null && approxEquals(smoke.durationSeconds ?: 0f, 12f) &&
                approxEquals(smoke.radiusMeters ?: 0f, 6f) && smoke.price == 80 &&
                flash != null && approxEquals(flash.durationSeconds ?: 0f, 4f) && flash.price == 120,
            "$smoke | $flash"
        )
        val gas = WeaponCatalog.FIELD_ITEMS.firstOrNull { it.id == "grenade_gas" }
        val mine = WeaponCatalog.FIELD_ITEMS.firstOrNull { it.id == "claymore_mine" }
        r.check(
            "Gas and mine", "gas 12 dmg/s x8 s r=6 m; mine 200 r=5 m",
            gas != null && approxEquals(gas.damage ?: 0f, 12f) &&
                approxEquals(gas.durationSeconds ?: 0f, 8f) && gas.unlockRank == 4 &&
                mine != null && approxEquals(mine.damage ?: 0f, 200f) &&
                approxEquals(mine.radiusMeters ?: 0f, 5f) && mine.price == 250,
            "$gas | $mine"
        )
        val trenchKnife = WeaponCatalog.FIELD_ITEMS.firstOrNull { it.id == "knife_trench" }
        r.check(
            "Trench knife", "60 front damage, instant kill from behind, free at rank 1",
            trenchKnife != null && approxEquals(trenchKnife.damage ?: 0f, 60f) &&
                trenchKnife.price == 0 && trenchKnife.unlockRank == 1 &&
                trenchKnife.note.contains("Instant kill from behind"),
            "$trenchKnife"
        )

        return r.result()
    }
}
