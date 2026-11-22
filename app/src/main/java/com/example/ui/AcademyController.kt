package com.example.ui

import com.example.data.AcademyProgressEntity
import com.example.data.EmberwatchRepository
import com.example.data.MeritAward
import com.example.game.academy.DrillId
import com.example.game.academy.DrillResult
import com.example.game.academy.DrillSnapshot
import com.example.game.academy.DroneReconDrill
import com.example.game.academy.FiringRangeDrill
import com.example.game.academy.KillhouseDrill
import com.example.game.academy.ObstacleCourseDrill
import com.example.game.academy.PlayableDrill
import com.example.game.combat.Attachment
import com.example.game.combat.AttachmentSocket
import com.example.game.combat.GunsmithCatalog
import com.example.game.combat.RankLadder
import com.example.game.combat.RankTier
import com.example.game.combat.WeaponBuild
import com.example.game.combat.WeaponRuntime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Owns academy progression, the armory loadout and the live drill session.
 * Single writer for merit and rank state; the screens only read flows.
 */
class AcademyController(
    private val repository: EmberwatchRepository,
    private val scope: CoroutineScope
) {

    val progress: StateFlow<AcademyProgressEntity?> =
        repository.academyProgressFlow.stateIn(scope, SharingStarted.Eagerly, null)

    private val _weaponId = MutableStateFlow("V9_SERVICE")
    val weaponId: StateFlow<String> = _weaponId.asStateFlow()

    private val _selection = MutableStateFlow(GunsmithCatalog.defaultSelection())
    val selection: StateFlow<Map<AttachmentSocket, String>> = _selection.asStateFlow()

    private val _revision = MutableStateFlow(0)
    val revision: StateFlow<Int> = _revision.asStateFlow()

    private val _drillSnapshot = MutableStateFlow<DrillSnapshot?>(null)
    val drillSnapshot: StateFlow<DrillSnapshot?> = _drillSnapshot.asStateFlow()

    private val _lastDrillResult = MutableStateFlow<DrillResult?>(null)
    val lastDrillResult: StateFlow<DrillResult?> = _lastDrillResult.asStateFlow()

    private val _promotion = MutableStateFlow<MeritAward?>(null)
    val promotion: StateFlow<MeritAward?> = _promotion.asStateFlow()

    private var activeDrill: PlayableDrill? = null
    private var drillJob: Job? = null

    val rankIndex: Int get() = progress.value?.rankIndex ?: 0
    val meritPoints: Int get() = progress.value?.meritPoints ?: 0
    val tier: RankTier get() = RankLadder.tierAt(rankIndex)
    val nextTier: RankTier? get() = RankLadder.nextTier(rankIndex)
    val rankProgress: Float get() = RankLadder.progress(rankIndex, meritPoints)

    init {
        scope.launch {
            val entity = repository.getOrCreateAcademyProgress()
            val saved = repository.getOrCreateAcademyLoadout()
            val requested = GunsmithCatalog.weapon(saved.first)
            // Clearance is re-checked on load so a reset ladder cannot arm a locked receiver.
            _weaponId.value = if (entity.rankIndex >= requested.requiredRankIndex) {
                requested.id
            } else {
                "V9_SERVICE"
            }
            _selection.value = GunsmithCatalog.defaultSelection() + saved.second
            _revision.value += 1
        }
        // Promotion ceremonies fire whenever the stored ladder crosses a rung.
        scope.launch {
            var lastRank = -1
            progress.collect { entity ->
                if (entity == null) return@collect
                if (lastRank >= 0 && entity.rankIndex > lastRank) {
                    _promotion.value = MeritAward(
                        pointsAwarded = 0,
                        totalMerit = entity.meritPoints,
                        previousRankIndex = lastRank,
                        newRankIndex = entity.rankIndex,
                        promoted = RankLadder.promotionsBetween(lastRank, entity.rankIndex)
                    )
                }
                lastRank = entity.rankIndex
            }
        }
    }

    // ------------------------------------------------------------- ARMORY

    fun currentBuild(): WeaponBuild =
        GunsmithCatalog.build(_weaponId.value, _selection.value, rankIndex)

    fun weaponsAvailable(): List<com.example.game.combat.WeaponBase> =
        GunsmithCatalog.weaponsForRank(rankIndex)

    fun selectWeapon(id: String): Boolean {
        val weapon = GunsmithCatalog.weapon(id)
        if (!RankLadder.hasClearance(rankIndex, weapon.requiredRankIndex)) return false
        _weaponId.value = weapon.id
        _revision.value += 1
        return true
    }

    fun isLocked(part: Attachment): Boolean = GunsmithCatalog.isLocked(part, rankIndex)

    fun equip(partId: String): Boolean {
        val part = GunsmithCatalog.find(partId) ?: return false
        if (isLocked(part)) return false
        _selection.value = _selection.value + (part.socket to part.id)
        _revision.value += 1
        return true
    }

    fun resetSocket(socket: AttachmentSocket) {
        val fallback = GunsmithCatalog.defaultSelection()[socket] ?: ""
        _selection.value = _selection.value + (socket to fallback)
        _revision.value += 1
    }

    fun saveLoadout() {
        val weapon = _weaponId.value
        val selection = _selection.value
        scope.launch { repository.saveLoadout(weapon, selection) }
    }

    // ------------------------------------------------------------- DRILLS

    fun startDrill(drillId: DrillId): Boolean {
        if (!RankLadder.hasClearance(rankIndex, drillId.requiredRankIndex)) return false
        val weapon = WeaponRuntime(currentBuild())
        val drill: PlayableDrill = when (drillId) {
            DrillId.RANGE -> FiringRangeDrill(weapon)
            DrillId.OBSTACLE -> ObstacleCourseDrill()
            DrillId.KILLHOUSE -> KillhouseDrill(weapon, rankIndex)
            DrillId.DRONE -> DroneReconDrill()
        }
        activeDrill = drill
        _lastDrillResult.value = null
        _drillSnapshot.value = drill.snapshot()
        drillJob?.cancel()
        drillJob = scope.launch {
            var lastNanos = System.nanoTime()
            while (isActive) {
                delay(100)
                val now = System.nanoTime()
                val dt = ((now - lastNanos) / 1_000_000_000f).coerceIn(0f, 0.5f)
                lastNanos = now
                val running = activeDrill ?: break
                if (!running.isFinished) running.tick(dt)
                _drillSnapshot.value = running.snapshot()
                if (running.isFinished) {
                    completeDrill(running)
                    break
                }
            }
        }
        return true
    }

    fun drillAction(actionId: String) {
        val drill = activeDrill ?: return
        if (drill.isFinished) return
        drill.handleAction(actionId)
        _drillSnapshot.value = drill.snapshot()
        if (drill.isFinished) completeDrill(drill)
    }

    fun drillPoint(x: Float, y: Float) {
        val drill = activeDrill ?: return
        if (drill.isFinished) return
        drill.handlePoint(x, y)
        _drillSnapshot.value = drill.snapshot()
    }

    private fun completeDrill(drill: PlayableDrill) {
        drillJob?.cancel()
        drillJob = null
        val result = drill.result()
        _drillSnapshot.value = drill.snapshot()
        _lastDrillResult.value = result
        scope.launch { repository.recordDrillResult(result) }
    }

    fun dismissDrill() {
        drillJob?.cancel()
        drillJob = null
        activeDrill = null
        _drillSnapshot.value = null
        _lastDrillResult.value = null
    }

    fun dismissPromotion() {
        _promotion.value = null
    }

    fun canStart(drillId: DrillId): Boolean =
        RankLadder.hasClearance(rankIndex, drillId.requiredRankIndex)
}
