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
import com.example.game.ChoiceOption
import com.example.game.DialogueLine
import com.example.game.GameEngine
import com.example.game.MissionCatalog
import com.example.game.MissionChoice
import com.example.game.Vector2
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
    BRIEFING,
    MISSION_PLAY_3D,
    MISSION_PLAY_2D,
    DEBRIEF,
    ROSTER,
    SETTINGS,
    EXPORT_SPEC
}

class EmberwatchViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: EmberwatchRepository
    val audioFeedback: AudioFeedback

    private val _currentScreen = MutableStateFlow(AppScreen.TITLE)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    val campaignProfile: StateFlow<CampaignProfileEntity?>
    val tacticalSettings: StateFlow<TacticalSettingsEntity?>
    val allMissions: StateFlow<List<MissionProgressEntity>>

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

    fun on3DMissionComplete(score: Int, choice: String, timeSec: Int) {
        viewModelScope.launch {
            val choiceId = if (choice.contains("CIVILIAN", ignoreCase = true)) "SAFEGUARD_CIVILIANS" else "INTERCEPT_DATA"
            repository.completeMission(
                missionId = "M1_SIGNAL_IN_DUST",
                score = score,
                stars = 3,
                civilianChoice = choiceId,
                timeSeconds = timeSec.toLong()
            )
            repository.setStoryFlag("M1_CHOICE", choiceId)
            _selectedChoice.value = if (choiceId == "SAFEGUARD_CIVILIANS") {
                MissionCatalog.MISSION_1_CHOICE.optionA
            } else {
                MissionCatalog.MISSION_1_CHOICE.optionB
            }
            _currentScreen.value = AppScreen.DEBRIEF
        }
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
        for (target in gameEngine.interactiveTargets) {
            target.isInteracted = false
            target.isEnabled = target.id == "TARGET_TOMAS"
        }
        startGameLoop()
    }

    private fun completeCurrentMission() {
        stopGameLoop()
        viewModelScope.launch {
            val choiceId = _selectedChoice.value?.id ?: "SAFEGUARD_CIVILIANS"
            repository.completeMission(
                missionId = "M1_SIGNAL_IN_DUST",
                score = 8500,
                stars = 3,
                civilianChoice = choiceId,
                timeSeconds = gameEngine.timeElapsedSeconds.toLong()
            )
            repository.setStoryFlag("M1_CHOICE", choiceId)
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
