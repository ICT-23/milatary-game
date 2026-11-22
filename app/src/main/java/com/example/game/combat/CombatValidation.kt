package com.example.game.combat

/**
 * Section 8.11: the twelve mandatory combat acceptance cases, executed against the
 * real pipeline ([WeaponRuntime] -> [CombatService] -> [BallisticsEngine] ->
 * [DamageResolver]). Nothing here is a stub: every case fires through the same code
 * a live mission uses, so a regression in any stage fails the audit.
 */
data class CombatCaseResult(
    val index: Int,
    val title: String,
    val expectation: String,
    val passed: Boolean,
    val detail: String
)

/** Aggregate view the HUD/armoury renders. */
data class CombatAudit(
    val results: List<CombatCaseResult>
) {
    val total: Int get() = results.size
    val passed: Int get() = results.count { it.passed }
    val allPassed: Boolean get() = total > 0 && passed == total
    val failures: List<CombatCaseResult> get() = results.filter { !it.passed }
}

object CombatValidation {

    private const val BASE_DAMAGE = 28f
    private const val EFFECTIVE_RANGE = 300f

    fun audit(): CombatAudit = CombatAudit(run())

    fun run(): List<CombatCaseResult> = listOf(
        blockedMuzzleRayCase(),
        clearTorsoCase(),
        marginalMissCase(),
        emptyMagazineCase(),
        reloadingCase(),
        protectedEntityCase(),
        teammateCase(),
        distantTargetCase(),
        duplicateEventCase(),
        alreadyDisabledCase(),
        coverBetweenAimAndMuzzleCase(),
        checkpointRestoreCase()
    )

    // 1. Crosshair sits on the enemy, but a wall blocks the muzzle ray.
    private fun blockedMuzzleRayCase(): CombatCaseResult {
        val target = cutout(Vec3(0f, 0f, -30f), id = "BLOCKED")
        val wall = BallisticBody.wall("WALL_MUZZLE", Vec3(0f, 0f, -20f), 1.4f)
        // The eye looks down over the wall; the rifle sits behind it.
        val eye = Vec3(0f, 12f, -10f)
        val muzzle = Vec3(0f, 0f, -5f)
        val event = CombatService.fire(
            request = request(
                shotId = "CASE_01",
                target = target,
                aimOrigin = eye,
                muzzleOrigin = muzzle,
                extraBodies = listOf(wall)
            ),
            weapon = null,
            nowSeconds = 0f
        )
        val passed = event.outcome == ShotOutcome.BLOCKED &&
            event.appliedDamage == 0 &&
            target.health == target.maxHealth
        return case(
            1,
            "Wall blocks the muzzle ray",
            "No enemy damage",
            passed,
            "outcome=${event.outcome} damage=${event.appliedDamage} blockedBy=${event.shot?.blockedByBodyId}"
        )
    }

    // 2. Clear line of sight onto an enemy torso.
    private fun clearTorsoCase(): CombatCaseResult {
        val target = cutout(Vec3(0f, 0f, -20f), id = "TORSO")
        val event = CombatService.fire(
            request = request(shotId = "CASE_02", target = target),
            weapon = null,
            nowSeconds = 0f
        )
        val lost = target.maxHealth - target.health
        val passed = event.outcome == ShotOutcome.APPLIED &&
            event.shot?.hitZone == HitZone.UPPER_TORSO &&
            event.appliedDamage > 0 &&
            lost == event.appliedDamage.toFloat()
        return case(
            2,
            "Clear torso hit",
            "Torso damage applied exactly once",
            passed,
            "outcome=${event.outcome} zone=${event.shot?.hitZone} damage=${event.appliedDamage} healthLost=$lost"
        )
    }

    // 3. The ray misses by a small margin.
    private fun marginalMissCase(): CombatCaseResult {
        val target = cutout(Vec3(0f, 0f, -20f), id = "MISS")
        val almost = Vec3(0.6f, 0.12f, -20f)
        val event = CombatService.fire(
            request = request(
                shotId = "CASE_03",
                target = target,
                direction = (almost - ORIGIN).normalized()
            ),
            weapon = null,
            nowSeconds = 0f
        )
        val passed = event.outcome == ShotOutcome.MISSED && target.health == target.maxHealth
        return case(
            3,
            "Marginal miss",
            "No damage",
            passed,
            "outcome=${event.outcome} reason=${event.shot?.rejectReason}"
        )
    }

    // 4. Magazine empty.
    private fun emptyMagazineCase(): CombatCaseResult {
        val target = cutout(Vec3(0f, 0f, -20f), health = 1_000_000f, id = "EMPTY")
        val weapon = weapon()
        val ledger = ShotLedger()
        var outcome: CombatEvent? = null
        var firedBefore = 0
        var healthWhileLoaded = target.health
        var tick = 0
        while (tick < 200) {
            val now = tick * 0.5f
            val event = CombatService.fire(
                request = request(shotId = "CASE_04_$tick", target = target),
                weapon = weapon,
                nowSeconds = now,
                ledger = ledger
            )
            if (event.outcome == ShotOutcome.NO_ROUND) {
                outcome = event
                break
            }
            if (event.outcome == ShotOutcome.APPLIED) {
                firedBefore += 1
                healthWhileLoaded = target.health
            }
            tick += 1
        }
        val last = outcome
        val passed = last != null &&
            last.appliedDamage == 0 &&
            target.health == healthWhileLoaded &&
            firedBefore == weapon.stats.magazineCapacity
        return case(
            4,
            "Empty magazine",
            "No shot and no damage event",
            passed,
            "roundsFired=$firedBefore capacity=${weapon.stats.magazineCapacity} outcome=${last?.outcome} note=${last?.note}"
        )
    }

    // 5. Weapon is reloading.
    private fun reloadingCase(): CombatCaseResult {
        val target = cutout(Vec3(0f, 0f, -20f), health = 1_000_000f, id = "RELOAD")
        val weapon = weapon()
        val ledger = ShotLedger()
        CombatService.fire(
            request = request(shotId = "CASE_05_A", target = target),
            weapon = weapon,
            nowSeconds = 0f,
            ledger = ledger
        )
        val started = weapon.beginReload()
        val healthBefore = target.health
        val event = CombatService.fire(
            request = request(shotId = "CASE_05_B", target = target),
            weapon = weapon,
            nowSeconds = 2f,
            ledger = ledger
        )
        val passed = started &&
            event.outcome == ShotOutcome.RELOADING &&
            event.appliedDamage == 0 &&
            target.health == healthBefore
        return case(
            5,
            "Weapon reloading",
            "No shot while the reload is in progress",
            passed,
            "reloadStarted=$started outcome=${event.outcome} note=${event.note}"
        )
    }

    // 6. Protected entity.
    private fun protectedEntityCase(): CombatCaseResult {
        val target = cutout(Vec3(0f, 0f, -20f), id = "CIVILIAN")
        val protectedBody = BallisticBody(
            bodyId = "CIVILIAN_CHEST",
            center = Vec3(0f, 0.05f, -20f),
            radius = 0.4f,
            zone = HitZone.PROTECTED,
            surface = SurfaceType.FLESH
        )
        val event = CombatService.fire(
            request = ShotRequest(
                shotId = "CASE_06",
                aimOrigin = ORIGIN,
                aimDirection = Vec3(0f, 0f, -1f),
                muzzleOrigin = ORIGIN,
                bodies = listOf(protectedBody),
                target = target,
                baseDamage = BASE_DAMAGE,
                effectiveRangeMeters = EFFECTIVE_RANGE,
                targetFaction = Faction.NON_COMBATANT
            ),
            weapon = null,
            nowSeconds = 0f
        )
        val passed = event.outcome == ShotOutcome.PROTECTED_VIOLATION &&
            event.appliedDamage == 0 &&
            event.damage?.protectedViolation == true &&
            target.health == target.maxHealth
        return case(
            6,
            "Protected entity",
            "Zero combat damage plus a mission-rule response",
            passed,
            "outcome=${event.outcome} violation=${event.damage?.protectedViolation} response=${event.note}"
        )
    }

    // 7. Teammate in the line of fire, default campaign mode.
    private fun teammateCase(): CombatCaseResult {
        val target = cutout(Vec3(0f, 0f, -20f), id = "ALLY")
        val event = CombatService.fire(
            request = request(shotId = "CASE_07", target = target, faction = Faction.ALLY),
            weapon = null,
            nowSeconds = 0f
        )
        val passed = event.outcome == ShotOutcome.FRIENDLY_FIRE_DENIED &&
            target.health == target.maxHealth &&
            event.note.isNotBlank()
        return case(
            7,
            "Teammate hit",
            "No teammate health loss, warning recorded",
            passed,
            "outcome=${event.outcome} health=${target.health.toInt()} note=${event.note}"
        )
    }

    // 8. Target beyond the configured falloff range.
    private fun distantTargetCase(): CombatCaseResult {
        val near = cutout(Vec3(0f, 0f, -20f), effectiveRange = 100f, id = "NEAR")
        val far = cutout(Vec3(0f, 0f, -200f), effectiveRange = 100f, id = "FAR")
        val nearEvent = CombatService.fire(
            request = request(shotId = "CASE_08_NEAR", target = near, effectiveRange = 100f),
            weapon = null,
            nowSeconds = 0f
        )
        val farEvent = CombatService.fire(
            request = request(shotId = "CASE_08_FAR", target = far, effectiveRange = 100f),
            weapon = null,
            nowSeconds = 0f
        )
        val passed = farEvent.outcome == ShotOutcome.APPLIED &&
            farEvent.appliedDamage > 0 &&
            farEvent.appliedDamage < nearEvent.appliedDamage
        return case(
            8,
            "Target past falloff range",
            "Hit registers with the lower configured damage",
            passed,
            "near=${nearEvent.appliedDamage} far=${farEvent.appliedDamage} falloff=${farEvent.damage?.falloff}"
        )
    }

    // 9. The same bullet event processed twice by two callbacks.
    private fun duplicateEventCase(): CombatCaseResult {
        val target = cutout(Vec3(0f, 0f, -20f), health = 1_000_000f, id = "DUP")
        val ledger = ShotLedger()
        val first = CombatService.fire(
            request = request(shotId = "CASE_09", target = target),
            weapon = null,
            nowSeconds = 0f,
            ledger = ledger
        )
        val healthAfterFirst = target.health
        val second = CombatService.fire(
            request = request(shotId = "CASE_09", target = target),
            weapon = null,
            nowSeconds = 0f,
            ledger = ledger
        )
        val passed = first.outcome == ShotOutcome.APPLIED &&
            first.appliedDamage > 0 &&
            second.outcome == ShotOutcome.DUPLICATE_EVENT &&
            second.appliedDamage == 0 &&
            target.health == healthAfterFirst
        return case(
            9,
            "Duplicate collision event",
            "Damage applied only once",
            passed,
            "first=${first.appliedDamage} second=${second.outcome} healthHeld=${target.health == healthAfterFirst}"
        )
    }

    // 10. Target already disabled.
    private fun alreadyDisabledCase(): CombatCaseResult {
        val target = cutout(Vec3(0f, 0f, -20f), health = 40f, id = "DOWN")
        val ledger = ShotLedger()
        var guard = 0
        while (!target.isDestroyedOrDead && guard < 20) {
            CombatService.fire(
                request = request(shotId = "CASE_10_$guard", target = target),
                weapon = null,
                nowSeconds = 0f,
                ledger = ledger
            )
            guard += 1
        }
        val healthAtDown = target.health
        val event = CombatService.fire(
            request = request(shotId = "CASE_10_FINAL", target = target),
            weapon = null,
            nowSeconds = 0f,
            ledger = ledger
        )
        val passed = target.isDestroyedOrDead &&
            event.outcome == ShotOutcome.TARGET_ALREADY_DOWN &&
            !event.awardsScore &&
            target.health == healthAtDown
        return case(
            10,
            "Target already disabled",
            "No additional score or duplicate reward",
            passed,
            "outcome=${event.outcome} awardsScore=${event.awardsScore} health=${target.health.toInt()}"
        )
    }

    // 11. Target steps behind cover between the aim ray and the muzzle check.
    private fun coverBetweenAimAndMuzzleCase(): CombatCaseResult {
        val target = cutout(Vec3(5f, 0f, -25f), id = "MOVED")
        val cover = BallisticBody.wall("COVER", Vec3(0f, 0f, -20f), 1.4f)
        // The player aimed at where the target used to be, so the ray now meets the cover.
        val event = CombatService.fire(
            request = request(
                shotId = "CASE_11",
                target = target,
                direction = Vec3(0f, 0f, -1f),
                extraBodies = listOf(cover)
            ),
            weapon = null,
            nowSeconds = 0f
        )
        val passed = event.outcome == ShotOutcome.BLOCKED &&
            event.appliedDamage == 0 &&
            target.health == target.maxHealth
        return case(
            11,
            "Target moves behind cover",
            "The obstruction blocks the shot",
            passed,
            "outcome=${event.outcome} blockedBy=${event.shot?.blockedByBodyId}"
        )
    }

    // 12. Checkpoint retry restores ammo, health and weapon state.
    private fun checkpointRestoreCase(): CombatCaseResult {
        val target = cutout(Vec3(0f, 0f, -20f), health = 1_000f, id = "CHECKPOINT")
        val weapon = weapon()
        val ledger = ShotLedger()
        var tick = 0
        repeat(3) {
            CombatService.fire(
                request = request(shotId = "CASE_12_PRE_$tick", target = target),
                weapon = weapon,
                nowSeconds = tick * 0.5f,
                ledger = ledger
            )
            tick += 1
        }
        val weaponAtCheckpoint = weapon.snapshot()
        val healthAtCheckpoint = target.health

        repeat(3) {
            CombatService.fire(
                request = request(shotId = "CASE_12_POST_$tick", target = target),
                weapon = weapon,
                nowSeconds = tick * 0.5f,
                ledger = ledger
            )
            tick += 1
        }
        val drifted = weapon.snapshot()

        // Retry from the checkpoint.
        weapon.restore(weaponAtCheckpoint)
        target.restoreHealth(healthAtCheckpoint)
        val restored = weapon.snapshot()

        val passed = restored == weaponAtCheckpoint &&
            drifted != weaponAtCheckpoint &&
            target.health == healthAtCheckpoint &&
            restored.loadedRounds < weapon.stats.magazineCapacity
        return case(
            12,
            "Checkpoint restore",
            "Ammo, health and weapon state come back exactly",
            passed,
            "restoredRounds=${restored.loadedRounds} driftedRounds=${drifted.loadedRounds} " +
                "checkpointRounds=${weaponAtCheckpoint.loadedRounds} health=${target.health.toInt()}"
        )
    }

    // ------------------------------------------------------------- scaffolding
    private val ORIGIN = Vec3(0f, 0f, 0f)

    private fun cutout(
        position: Vec3,
        health: Float = 1_000f,
        effectiveRange: Float = EFFECTIVE_RANGE,
        id: String = "TARGET"
    ): DamageableCutout = DamageableCutout(
        transformRef = position,
        maxHealth = health,
        effectiveRangeMeters = effectiveRange,
        id = id
    )

    private fun weapon(): WeaponRuntime =
        WeaponRuntime(WeaponBuild(GunsmithCatalog.weapon("AR4_SENTINEL"), emptyMap()))

    private fun request(
        shotId: String,
        target: DamageableCutout,
        aimOrigin: Vec3 = ORIGIN,
        muzzleOrigin: Vec3 = aimOrigin,
        direction: Vec3 = (target.zoneCenter(HitZone.UPPER_TORSO) - aimOrigin).normalized(),
        extraBodies: List<BallisticBody> = emptyList(),
        effectiveRange: Float = EFFECTIVE_RANGE,
        faction: Faction = Faction.HOSTILE
    ): ShotRequest = ShotRequest(
        shotId = shotId,
        aimOrigin = aimOrigin,
        aimDirection = direction,
        muzzleOrigin = muzzleOrigin,
        bodies = target.hitZones + extraBodies,
        target = target,
        baseDamage = BASE_DAMAGE,
        effectiveRangeMeters = effectiveRange,
        targetFaction = faction
    )

    private fun case(
        index: Int,
        title: String,
        expectation: String,
        passed: Boolean,
        detail: String
    ): CombatCaseResult = CombatCaseResult(index, title, expectation, passed, detail)
}
