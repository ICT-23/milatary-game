package com.example.game.valor.gdd

import com.example.game.vanguard.CaseRecorder
import com.example.game.vanguard.ContractCase
import com.example.game.vanguard.approxEquals
import com.example.game.vanguard.observed

/** Sections 5.1-5.5: the categories, the stats, the workshop, gear and handling. */
object GddArsenalCases {

    fun run(): List<ContractCase> {
        val r = CaseRecorder(ValorGddAudit.GROUP_ARSENAL)

        r.check(
            "Category count", "11 weapon categories",
            WeaponCategories.CATEGORIES.size == 11,
            observed(WeaponCategories.CATEGORIES.size, 11)
        )
        r.check(
            "Pistol row", "Pistol covers every era with a 1911-style example",
            WeaponCategories.byCategory("Pistol")?.era == "All" &&
                WeaponCategories.byCategory("Pistol")?.examples?.contains("1911") == true,
            observed(WeaponCategories.byCategory("Pistol")?.era, "All")
        )
        r.check(
            "Era filter", "WW1 lists the bolt-action rifle and the LMG",
            WeaponCategories.byEra("WW1").map { it.category } ==
                listOf("Bolt-action rifle", "LMG"),
            observed(WeaponCategories.byEra("WW1").map { it.category }, "[2 rows]")
        )
        r.check(
            "Nine stats", "damage, rate, range, accuracy, recoil, reload, mag, mobility, penetration",
            WeaponStat.entries.size == 9 &&
                WeaponStat.entries.first() == WeaponStat.DAMAGE &&
                WeaponStat.entries.last() == WeaponStat.PENETRATION,
            observed(WeaponStat.entries.size, 9)
        )
        r.check(
            "Stat bars", "bars clamp to the maximum",
            approxEquals(StatBars.barFraction(28f, 56f), 0.5f) &&
                approxEquals(StatBars.barFraction(60f, 56f), 1f) &&
                approxEquals(StatBars.barFraction(10f, 0f), 0f),
            observed(StatBars.barFraction(60f, 56f), 1f)
        )
        r.check(
            "Stat readout", "tap shows the number behind the bar",
            StatBars.numeric(28f) == "28" && StatBars.numeric(2.5f) == "2.5",
            observed(StatBars.numeric(28f), "28")
        )
        r.check(
            "Workshop steps", "7 steps ending in mastery",
            Gunsmith.STEPS.size == 7 &&
                Gunsmith.STEPS.map { it.order } == (1..7).toList() &&
                Gunsmith.STEPS.last().name == "Weapon mastery",
            observed(Gunsmith.STEPS.last().name, "Weapon mastery")
        )
        r.check(
            "Slots and options", "7 slots holding 24 attachments",
            Gunsmith.slotCount() == 7 && Gunsmith.optionCount() == 24,
            observed(Gunsmith.optionCount(), 24)
        )
        r.check(
            "Slot rank gates", "optic at 3, barrel and grip at 6, the rest unnamed",
            Gunsmith.slot("Optic")?.unlockRank == 3 &&
                Gunsmith.slot("Barrel")?.unlockRank == 6 &&
                Gunsmith.slot("Underbarrel")?.unlockRank == 6 &&
                Gunsmith.slot("Muzzle")?.unlockRank == null,
            observed(Gunsmith.slot("Underbarrel")?.unlockRank, 6)
        )
        r.check(
            "Slots by rank", "one slot at 3, three at 6, none at 2",
            Gunsmith.unlockedSlots(2).isEmpty() &&
                Gunsmith.unlockedSlots(3).size == 1 &&
                Gunsmith.unlockedSlots(6).size == 3,
            observed(Gunsmith.unlockedSlots(6).size, 3)
        )
        r.check(
            "Advanced attachments", "unlock at Lieutenant Colonel (14)",
            Gunsmith.advancedAttachmentsAt(14) && !Gunsmith.advancedAttachmentsAt(13),
            observed(Gunsmith.advancedAttachmentsAt(13), false)
        )
        r.check(
            "Skins", "five cosmetic camos",
            Gunsmith.SKINS == listOf("Desert", "Woodland", "Arctic", "Gold", "Prestige") &&
                Gunsmith.skinIsCosmetic(),
            observed(Gunsmith.SKINS.size, 5)
        )
        r.check(
            "Level cap", "every gun runs Lv1 to Lv10",
            Gunsmith.MIN_LEVEL == 1 && Gunsmith.MAX_LEVEL == 10,
            observed(Gunsmith.MAX_LEVEL, 10)
        )
        r.check(
            "Craft resources", "Steel, Parts and Cash",
            Gunsmith.CRAFT_RESOURCES == listOf("Steel", "Parts", "Cash"),
            observed(Gunsmith.CRAFT_RESOURCES, "[Steel, Parts, Cash]")
        )
        r.check(
            "Blueprint sources", "missions and bosses drop pieces",
            Blueprints.SOURCES == listOf("Missions", "Bosses") &&
                Blueprints.isSource("bosses"),
            observed(Blueprints.SOURCES, "[Missions, Bosses]")
        )
        r.check(
            "Gear table", "8 rows, helmet and body armour tiered",
            GearTable.count() == 8 &&
                GearTable.tieredRows().map { it.item } ==
                listOf("Helmet (tiers)", "Body armor (tiers)"),
            observed(GearTable.count(), 8)
        )
        r.check(
            "Radio row", "the radio places every support call",
            GearTable.radioRow().function == "Call smoke, artillery, drone",
            observed(GearTable.radioRow().function, "Call smoke, artillery, drone")
        )
        r.check(
            "Hit zones", "head 2.5x, torso 1x, limb 0.7x",
            approxEquals(WeaponHandling.HEAD_MULTIPLIER, 2.5f) &&
                approxEquals(WeaponHandling.TORSO_MULTIPLIER, 1f) &&
                approxEquals(WeaponHandling.LIMB_MULTIPLIER, 0.7f) &&
                WeaponHandling.HIT_ZONES.size == 3,
            observed(WeaponHandling.HEAD_MULTIPLIER, 2.5f)
        )
        r.check(
            "Fire modes", "single, burst, auto",
            WeaponHandling.FIRE_MODES == listOf("Single", "Burst", "Auto"),
            observed(WeaponHandling.FIRE_MODES, "[Single, Burst, Auto]")
        )
        r.check(
            "Projectile rule", "only sniper rounds travel",
            WeaponHandling.usesProjectile(true) && !WeaponHandling.usesProjectile(false),
            observed(WeaponHandling.usesProjectile(false), false)
        )
        r.check(
            "Suppressor rule", "a suppressor shrinks the alert radius",
            WeaponHandling.reducesAlertRadius(true) &&
                !WeaponHandling.reducesAlertRadius(false),
            observed(WeaponHandling.reducesAlertRadius(false), false)
        )
        r.check(
            "Jams", "only WW1 guns jam, and only with the option on",
            WeaponHandling.canJam(eraWorldWarOne = true, jamsEnabled = true) &&
                !WeaponHandling.canJam(eraWorldWarOne = true, jamsEnabled = false) &&
                !WeaponHandling.canJam(eraWorldWarOne = false, jamsEnabled = true),
            observed(WeaponHandling.canJam(false, true), false)
        )

        r.check(
            "Gear lookup", "the radio row resolves by name",
            GearTable.byItem("radio")?.function == GearTable.radioRow().function,
            observed(GearTable.byItem("radio")?.function, "Call smoke, artillery, drone")
        )
        r.check(
            "Drone lookup", "the swarm resolves by its display name",
            DroneSupport.byName("Swarm Drones") == DroneType.SWARM,
            observed(DroneSupport.byName("Swarm Drones"), DroneType.SWARM)
        )

        return r.result()
    }
}

/** Section 6: the five drones and the five radio calls. */
object GddSupportCases {

    fun run(): List<ContractCase> {
        val r = CaseRecorder(ValorGddAudit.GROUP_SUPPORT)

        r.check(
            "Drone count", "5 drones",
            DroneSupport.DRONES.size == 5, observed(DroneSupport.DRONES.size, 5)
        )
        r.check(
            "ARIA marks", "marks hold for 15 s",
            DroneType.ARIA_RECON.durationSeconds == 15 &&
                DroneSupport.marksVisible(DroneType.ARIA_RECON, 15f) &&
                !DroneSupport.marksVisible(DroneType.ARIA_RECON, 15.5f),
            observed(DroneType.ARIA_RECON.durationSeconds, 15)
        )
        r.check(
            "Strike cooldown", "60 s between missiles",
            DroneType.STRIKE.cooldownSeconds == 60 &&
                !DroneSupport.isReady(DroneType.STRIKE, 59f, 1) &&
                DroneSupport.isReady(DroneType.STRIKE, 60f, 1),
            observed(DroneSupport.isReady(DroneType.STRIKE, 59f, 1), false)
        )
        r.check(
            "Limited charges", "a spent charge blocks the drone",
            DroneSupport.LIMITED_CHARGES && !DroneSupport.isReady(DroneType.STRIKE, 120f, 0),
            observed(DroneSupport.isReady(DroneType.STRIKE, 120f, 0), false)
        )
        r.check(
            "Drone ranks", "four at Colonel (15), the swarm at 16",
            DroneSupport.unlockedAt(14).isEmpty() &&
                DroneSupport.unlockedAt(15).size == 4 &&
                DroneSupport.unlockedAt(16).size == 5,
            observed(DroneSupport.unlockedAt(15).size, 4)
        )
        r.check(
            "Support calls", "5 calls on the radio",
            SupportCalls.CALLS.size == 5, observed(SupportCalls.CALLS.size, 5)
        )
        r.check(
            "Smoke rank", "smoke arrives at Sergeant Major (9)",
            SupportCalls.byName("Smoke")?.let { call ->
                call.unlockRank == 9 &&
                    !SupportCalls.availableAt(8, call) &&
                    SupportCalls.availableAt(9, call)
            } == true,
            observed(SupportCalls.byName("Smoke")?.unlockRank, 9)
        )
        r.check(
            "Artillery rank", "artillery arrives at First Lieutenant (11)",
            SupportCalls.byName("Artillery")?.unlockRank == 11 &&
                SupportCalls.rankGated().size == 2,
            observed(SupportCalls.rankGated().size, 2)
        )
        r.check(
            "Era-gated calls", "airstrike follows the era, medic is never rank-gated",
            SupportCalls.byName("Airstrike")?.eraDependent == true &&
                SupportCalls.byName("Medic call")?.let { SupportCalls.availableAt(1, it) } == true &&
                SupportCalls.byName("Mortar")?.let { SupportCalls.availableAt(1, it) } == true,
            observed(SupportCalls.byName("Airstrike")?.eraDependent, true)
        )
        r.check(
            "Calls by rank", "a Colonel holds all five",
            SupportCalls.availableCalls(15).size == 5 &&
                SupportCalls.availableCalls(1).size == 3,
            observed(SupportCalls.availableCalls(1).size, 3)
        )

        return r.result()
    }
}
