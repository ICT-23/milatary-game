package com.example.ui

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CampaignProfileEntity
import com.example.data.EmberwatchDatabase
import com.example.data.EmberwatchRepository
import com.example.data.MissionProgressEntity
import com.example.data.TacticalSettingsEntity
import com.example.game.AudioFeedback
import com.example.game.ChoiceBrief
import com.example.game.ChoiceCatalog
import com.example.game.ChoiceOption
import com.example.game.DialogueLine
import com.example.game.GameEngine
import com.example.game.MissionCatalog
import com.example.game.MissionChoice
import com.example.game.MissionDossier
import com.example.game.MissionRating
import com.example.game.Vector2
import com.example.game.progression.CampaignEnding
import com.example.game.progression.Commendation
import com.example.game.progression.MissionGrade
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class AppScreen {
    TITLE,
    MISSION_SELECT,
    BRIEFING,
    MISSION_PLAY_3D,
    MISSION_PLAY_2D,
    DEBRIEF,
    ROSTER,
    SETTINGS,
    EXPORT_SPEC,
    ACADEMY,
    ARMORY,
    RANKS,
    DRILL,
    VANGUARD,
    TACTICAL,
    VALOR,
    BALANCE,
    GDD,
    PLAN,
    LAUNCH
}

/** Everything the debrief needs after a chapter is cleared. */
data class MissionResult(
    val missionId: String,
    val missionTitle: String,
    val chapterNumber: Int,
    val score: Int,
    val stars: Int,
    val elapsedSeconds: Int,
    val healthPercent: Int,
    val choice: ChoiceBrief?,
    val awardedXp: Int,
    val rankTitle: String,
    val nextMissionTitle: String?,
    /** Non-null when this clear earned a campaign rank promotion. */
    val promotedRank: String? = null,
    /** Rank the operative held before the clear. */
    val previousRankTitle: String = "Cadet",
    /** Cipher intelligence the override choice added. */
    val intelGained: Int = 0,
    /** Section 6.4 grade and the recorded reason for it. */
    val grade: MissionGrade = MissionGrade.C,
    val gradeScore: Int = 0,
    val gradeReasons: List<String> = emptyList(),
    /** Section 6.5 commendations this clear actually earned. */
    val awardsEarned: List<Commendation> = emptyList(),
    /** Section 17.1 rewards. */
    val serviceCreditsGained: Int = 0,
    val commendationTokensGained: Int = 0,
    /** What still blocks the next promotion, per section 6.1. */
    val promotionOutstanding: List<String> = emptyList(),
    /** Section 3.7: only set on the final chapter clear. */
    val ending: CampaignEnding? = null
)

class EmberwatchViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: EmberwatchRepository
    val audioFeedback: AudioFeedback

    /** Academy merit ladder, armory and training drills. */
    val academy: AcademyController

    private val _currentScreen = MutableStateFlow(AppScreen.TITLE)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    val campaignProfile: StateFlow<CampaignProfileEntity?>
    val tacticalSettings: StateFlow<TacticalSettingsEntity?>
    val allMissions: StateFlow<List<MissionProgressEntity>>

    // Mission flow: the chapter currently selected on the ops board.
    private val _selectedMissionId = MutableStateFlow(MissionCatalog.ALL_MISSIONS.first().id)
    val selectedMissionId: StateFlow<String> = _selectedMissionId.asStateFlow()

    val selectedDossier: MissionDossier
        get() = MissionCatalog.byId(_selectedMissionId.value)

    private val _lastResult = MutableStateFlow<MissionResult?>(null)
    val lastResult: StateFlow<MissionResult?> = _lastResult.asStateFlow()

    /** Incremented by the host activity to ask the WebView engine to pause itself. */
    private val _pauseSignal = MutableStateFlow(0)
    val pauseSignal: StateFlow<Int> = _pauseSignal.asStateFlow()

    // Live Game Engine
    val gameEngine = GameEngine()
    private val _joystickVector = MutableStateFlow(Vector2.Zero)
    val joystickVector: StateFlow<Vector2> = _joystickVector.asStateFlow()

    private val _activeDialogue = MutableStateFlow<DialogueLine?>(null)
    val activeDialogue: StateFlow<DialogueLine?> = _activeDialogue.asStateFlow()

    private val _activeDecision = MutableStateFlow<MissionChoice?>(null)
    val activeDecision: StateFlow<MissionChoice?> = _activeDecision.asStateFlow()

    private val _selectedChoice = MutableStateFlow<ChoiceOption?>(null)
    val selectedChoice: StateFlow<ChoiceOption?> = _selectedChoice.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    // Simulation frame loop job
    private var gameLoopJob: Job? = null

    init {
        val database = EmberwatchDatabase.getDatabase(application)
        repository = EmberwatchRepository(database.dao())
        audioFeedback = AudioFeedback(viewModelScope)
        academy = AcademyController(repository, viewModelScope)

        campaignProfile = repository.campaignProfileFlow.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            null
        )
        tacticalSettings = repository.settingsFlow.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            null
        )
        allMissions = repository.allMissionsFlow.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )

        viewModelScope.launch {
            val prof = repository.getOrCreateProfile()
            val sett = repository.getOrCreateSettings()
            audioFeedback.isEnabled = sett.soundEffectsVolume > 0.05f
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
        if (screen == AppScreen.MISSION_PLAY_2D) {
            startGameLoop()
        } else {
            stopGameLoop()
        }
    }

    fun on3DMissionComplete(score: Int, choice: String, timeSec: Int, healthPct: Int) {
        val dossier = selectedDossier
        val missionId = dossier.id
        val elapsed = timeSec.coerceAtLeast(1)
        val health = healthPct.coerceIn(0, 100)
        val stars = MissionRating.stars(health, elapsed, dossier.parTimeSeconds)
        val choiceId = choice.takeIf { it.isNotBlank() }

        viewModelScope.launch {
            val completion = repository.completeMission(
                missionId = missionId,
                score = score,
                stars = stars,
                civilianChoice = choiceId,
                timeSeconds = elapsed.toLong(),
                healthPercent = health
            )

            val next = MissionCatalog.nextAfter(missionId)
            _lastResult.value = MissionResult(
                missionId = missionId,
                missionTitle = dossier.title,
                chapterNumber = dossier.chapterNumber,
                score = score,
                stars = stars,
                elapsedSeconds = elapsed,
                healthPercent = health,
                choice = ChoiceCatalog.byId(choiceId),
                awardedXp = completion?.awardedXp ?: dossier.rewardXp,
                rankTitle = completion?.rankTitle ?: dossier.recommendedRank,
                nextMissionTitle = next?.title,
                promotedRank = completion?.promotedRank,
                previousRankTitle = completion?.previousRankTitle ?: dossier.recommendedRank,
                intelGained = completion?.intelGained ?: 0,
                grade = completion?.grade ?: MissionGrade.C,
                gradeScore = completion?.gradeScore ?: 0,
                gradeReasons = completion?.gradeReasons ?: emptyList(),
                awardsEarned = completion?.awardsEarned ?: emptyList(),
                serviceCreditsGained = completion?.serviceCreditsGained ?: 0,
                commendationTokensGained = completion?.commendationTokensGained ?: 0,
                promotionOutstanding = completion?.promotionOutstanding ?: emptyList(),
                ending = completion?.ending
            )
            _currentScreen.value = AppScreen.DEBRIEF
        }
    }

    /** Opens the briefing for [missionId]; used by the operations board. */
    fun selectMission(missionId: String) {
        _selectedMissionId.value = MissionCatalog.byId(missionId).id
        _currentScreen.value = AppScreen.BRIEFING
    }

    /** Replays the chapter that was just finished without leaving the debrief. */
    fun replayLastMission() {
        _lastResult.value?.missionId?.let { _selectedMissionId.value = it }
        _currentScreen.value = AppScreen.MISSION_PLAY_3D
    }

    fun requestGamePause() {
        _pauseSignal.value = _pauseSignal.value + 1
    }

    fun abort3DToTitle() {
        _currentScreen.value = AppScreen.MISSION_SELECT
    }

    fun setJoystickVector(vec: Vector2) {
        val sensitivity = tacticalSettings.value?.joystickSensitivity ?: 1.0f
        _joystickVector.value = vec * sensitivity
    }

    fun triggerScanner() {
        val triggered = gameEngine.triggerScanner()
        if (triggered) {
            audioFeedback.playScannerPing()
            vibrate(50)
        }
    }

    fun handleInteractClick() {
        val target = gameEngine.nearestTarget ?: return

        // Objective 2 is a scanner task, not a proximity task: the dish only
        // yields telemetry once a pulse has physically reached the array.
        if (target.id == "TARGET_RELAY" && !gameEngine.isScannerConfirmed(target)) {
            audioFeedback.playAlertAlarm()
            _activeDialogue.value = DialogueLine(
                speaker = "Tomas Vale (Recon)",
                speakerRole = "Forward Scout",
                text = "Not yet, Spectre. The array is still blank on my scope. Sweep it with the Field Scanner, then read the splice signature off the dish.",
                speakerColorHex = 0xFF667353
            )
            return
        }

        audioFeedback.playInteractBeep()
        vibrate(40)

        target.isInteracted = true

        when (target.id) {
            "TARGET_TOMAS" -> {
                _activeDialogue.value = DialogueLine(
                    speaker = "Tomas Vale (Recon)",
                    speakerRole = "Forward Scout",
                    text = "Spectre! Good to see you in one piece. Telemetry Tower Alpha took a severe surge. I detected a fleeing figure heading toward the power vaults. Get over to the dish and scan for frequency signatures!",
                    speakerColorHex = 0xFF667353
                )
                gameEngine.advanceObjective()
                audioFeedback.playObjectiveComplete()
            }
            "TARGET_RELAY" -> {
                _activeDialogue.value = DialogueLine(
                    speaker = "Captain Mara Voss",
                    speakerRole = "Tactical Overwatch",
                    text = "Spectre, your scan telemetry confirmed foreign splicing on the primary dish! The signal repeater was rigged to blow. Follow the auxiliary lines southeast to the generator vault before it ruptures!",
                    speakerColorHex = 0xFFE7A83E
                )
                gameEngine.advanceObjective()
                audioFeedback.playObjectiveComplete()
            }
            "TARGET_GENERATOR" -> {
                _activeDialogue.value = DialogueLine(
                    speaker = "Leena Suri (Engineer)",
                    speakerRole = "Combat Systems",
                    text = "Spectre! The auxiliary generator's safety valves are jammed! It's venting coolant towards the civilian shelter! At the manual console ahead, you have to choose which circuit to prioritize!",
                    speakerColorHex = 0xFF81919E
                )
                gameEngine.advanceObjective()
                audioFeedback.playObjectiveComplete()
            }
            "TARGET_CONSOLE" -> {
                audioFeedback.playAlertAlarm()
                vibrate(100)
                _activeDecision.value = MissionCatalog.MISSION_1_CHOICE
            }
            "TARGET_EXTRACTION" -> {
                audioFeedback.playObjectiveComplete()
                vibrate(150)
                completeCurrentMission()
            }
        }
    }

    fun dismissDialogue() {
        _activeDialogue.value = null
    }

    fun selectDecisionOption(option: ChoiceOption) {
        _selectedChoice.value = option
        _activeDecision.value = null
        audioFeedback.playObjectiveComplete()

        // Show consequence reaction dialogue
        _activeDialogue.value = option.outcomeDialogue
        gameEngine.advanceObjective()
    }

    fun togglePause() {
        _isPaused.value = !_isPaused.value
    }

    fun restartMission() {
        _isPaused.value = false
        _activeDialogue.value = null
        _activeDecision.value = null
        _selectedChoice.value = null
        gameEngine.playerPos = Vector2(160f, 320f)
        gameEngine.currentObjectiveIndex = 0
        gameEngine.timeElapsedSeconds = 0f
        gameEngine.resetScanState()
        for (target in gameEngine.interactiveTargets) {
            target.isInteracted = false
            target.isEnabled = target.id == "TARGET_TOMAS"
        }
        startGameLoop()
    }

    private fun completeCurrentMission() {
        stopGameLoop()
        val dossier = selectedDossier
        val elapsed = gameEngine.timeElapsedSeconds.toInt().coerceAtLeast(1)
        val stars = MissionRating.stars(100, elapsed, dossier.parTimeSeconds)
        viewModelScope.launch {
            val choiceId = _selectedChoice.value?.id ?: "SAFEGUARD_CIVILIANS"
            val completion = repository.completeMission(
                missionId = dossier.id,
                score = 8500,
                stars = stars,
                civilianChoice = choiceId,
                timeSeconds = elapsed.toLong(),
                healthPercent = gameEngine.playerHealth.coerceIn(0, 100)
            )
            _lastResult.value = MissionResult(
                missionId = dossier.id,
                missionTitle = dossier.title,
                chapterNumber = dossier.chapterNumber,
                score = 8500,
                stars = stars,
                elapsedSeconds = elapsed,
                healthPercent = gameEngine.playerHealth.coerceIn(0, 100),
                choice = ChoiceCatalog.byId(choiceId),
                awardedXp = completion?.awardedXp ?: dossier.rewardXp,
                rankTitle = completion?.rankTitle ?: dossier.recommendedRank,
                nextMissionTitle = MissionCatalog.nextAfter(dossier.id)?.title,
                promotedRank = completion?.promotedRank,
                previousRankTitle = completion?.previousRankTitle ?: dossier.recommendedRank,
                intelGained = completion?.intelGained ?: 0,
                grade = completion?.grade ?: MissionGrade.C,
                gradeScore = completion?.gradeScore ?: 0,
                gradeReasons = completion?.gradeReasons ?: emptyList(),
                awardsEarned = completion?.awardsEarned ?: emptyList(),
                serviceCreditsGained = completion?.serviceCreditsGained ?: 0,
                commendationTokensGained = completion?.commendationTokensGained ?: 0,
                promotionOutstanding = completion?.promotionOutstanding ?: emptyList(),
                ending = completion?.ending
            )
            _currentScreen.value = AppScreen.DEBRIEF
        }
    }

    fun saveSettings(settings: TacticalSettingsEntity) {
        viewModelScope.launch {
            repository.updateSettings(settings)
            audioFeedback.isEnabled = settings.soundEffectsVolume > 0.05f
        }
    }

    fun resetCampaign() {
        viewModelScope.launch {
            repository.resetAllCampaignData()
            restartMission()
            _selectedMissionId.value = MissionCatalog.ALL_MISSIONS.first().id
            _lastResult.value = null
            _currentScreen.value = AppScreen.TITLE
        }
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            var lastTimeNanos = System.nanoTime()
            while (isActive) {
                val now = System.nanoTime()
                val dt = (now - lastTimeNanos) / 1_000_000_000f
                lastTimeNanos = now

                if (!_isPaused.value && _activeDialogue.value == null && _activeDecision.value == null) {
                    gameEngine.update(
                        dt = dt,
                        joystickVector = _joystickVector.value,
                        viewportWidth = 800f,
                        viewportHeight = 450f
                    )
                }

                delay(16) // ~60 FPS tick
            }
        }
    }

    private fun stopGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = null
    }

    private fun vibrate(durationMs: Long) {
        if (tacticalSettings.value?.hapticEnabled != true) return
        try {
            val context = getApplication<Application>().applicationContext
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val vibrator = manager?.defaultVibrator
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(durationMs)
                }
            }
        } catch (_: Exception) {
            // Non-blocking fallback
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopGameLoop()
    }
}
