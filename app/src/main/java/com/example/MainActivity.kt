package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.TacticalSettingsEntity
import com.example.game.MissionCatalog
import com.example.game.settings.QualityPreset
import com.example.ui.AppScreen
import com.example.ui.EmberwatchViewModel
import com.example.ui.components.DecisionModal
import com.example.ui.components.DialogueBox
import com.example.ui.components.PauseModal
import com.example.ui.components.PromotionOverlay
import com.example.ui.components.TacticalHud
import com.example.ui.components.VirtualJoystick
import com.example.ui.screens.AcademyScreen
import com.example.ui.screens.ArmoryScreen
import com.example.ui.screens.BriefingScreen
import com.example.ui.screens.DebriefScreen
import com.example.ui.screens.DrillPlayScreen
import com.example.ui.screens.Game3DView
import com.example.ui.screens.LaunchOpsScreen
import com.example.ui.screens.MissionCanvasView
import com.example.ui.screens.MissionSelectScreen
import com.example.ui.screens.RankScreen
import com.example.ui.screens.ReactNativeSpecScreen
import com.example.ui.screens.RosterScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TacticalEvolutionScreen
import com.example.ui.screens.TitleScreen
import com.example.ui.screens.ValorBalanceScreen
import com.example.ui.screens.ValorBibleScreen
import com.example.ui.screens.ValorGddScreen
import com.example.ui.screens.ValorPlanScreen
import com.example.ui.screens.VanguardSystemsScreen
import com.example.ui.theme.DeepGraphite
import com.example.ui.theme.EmberwatchTheme

class MainActivity : ComponentActivity() {

    private val viewModel: EmberwatchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EmberwatchTheme {
                EmberwatchApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun EmberwatchApp(viewModel: EmberwatchViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val profile by viewModel.campaignProfile.collectAsStateWithLifecycle()
    val settings by viewModel.tacticalSettings.collectAsStateWithLifecycle()
    val activeDialogue by viewModel.activeDialogue.collectAsStateWithLifecycle()
    val activeDecision by viewModel.activeDecision.collectAsStateWithLifecycle()
    val isPaused by viewModel.isPaused.collectAsStateWithLifecycle()
    val missions by viewModel.allMissions.collectAsStateWithLifecycle()
    val selectedMissionId by viewModel.selectedMissionId.collectAsStateWithLifecycle()
    val lastResult by viewModel.lastResult.collectAsStateWithLifecycle()
    val pauseSignal by viewModel.pauseSignal.collectAsStateWithLifecycle()
    val academyPromotion by viewModel.academy.promotion.collectAsStateWithLifecycle()

    // Android Hardware/Gesture Back Navigation
    BackHandler(enabled = currentScreen != AppScreen.TITLE) {
        when (currentScreen) {
            AppScreen.MISSION_SELECT -> viewModel.navigateTo(AppScreen.TITLE)
            AppScreen.BRIEFING -> viewModel.navigateTo(AppScreen.MISSION_SELECT)
            AppScreen.MISSION_PLAY_3D -> viewModel.requestGamePause()
            AppScreen.MISSION_PLAY_2D -> {
                if (activeDecision != null) {
                    // Cannot back out of critical crisis decision
                } else if (activeDialogue != null) {
                    viewModel.dismissDialogue()
                } else {
                    viewModel.togglePause()
                }
            }
            AppScreen.DEBRIEF -> viewModel.navigateTo(AppScreen.MISSION_SELECT)
            AppScreen.ROSTER -> viewModel.navigateTo(AppScreen.TITLE)
            AppScreen.SETTINGS -> viewModel.navigateTo(AppScreen.TITLE)
            AppScreen.EXPORT_SPEC -> viewModel.navigateTo(AppScreen.TITLE)
            AppScreen.ACADEMY -> viewModel.navigateTo(AppScreen.TITLE)
            AppScreen.ARMORY -> viewModel.navigateTo(AppScreen.TITLE)
            AppScreen.RANKS -> viewModel.navigateTo(AppScreen.TITLE)
            AppScreen.VANGUARD -> viewModel.navigateTo(AppScreen.TITLE)
            AppScreen.TACTICAL -> viewModel.navigateTo(AppScreen.TITLE)
            AppScreen.VALOR -> viewModel.navigateTo(AppScreen.TITLE)
            AppScreen.BALANCE -> viewModel.navigateTo(AppScreen.TITLE)
            AppScreen.GDD -> viewModel.navigateTo(AppScreen.TITLE)
            AppScreen.PLAN -> viewModel.navigateTo(AppScreen.TITLE)
            AppScreen.LAUNCH -> viewModel.navigateTo(AppScreen.TITLE)
            AppScreen.DRILL -> {
                viewModel.academy.dismissDrill()
                viewModel.navigateTo(AppScreen.ACADEMY)
            }
            AppScreen.TITLE -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepGraphite)
    ) {
        when (currentScreen) {
            AppScreen.TITLE -> {
                TitleScreen(
                    profile = profile,
                    onStartCampaign = { viewModel.navigateTo(AppScreen.MISSION_SELECT) },
                    onOpenRoster = { viewModel.navigateTo(AppScreen.ROSTER) },
                    onOpenSettings = { viewModel.navigateTo(AppScreen.SETTINGS) },
                    onOpenExportSpec = { viewModel.navigateTo(AppScreen.EXPORT_SPEC) },
                    onOpenAcademy = { viewModel.navigateTo(AppScreen.ACADEMY) },
                    onOpenArmory = { viewModel.navigateTo(AppScreen.ARMORY) },
                    onOpenRanks = { viewModel.navigateTo(AppScreen.RANKS) },
                    onOpenVanguard = { viewModel.navigateTo(AppScreen.VANGUARD) },
                    onOpenTactical = { viewModel.navigateTo(AppScreen.TACTICAL) },
                    onOpenValor = { viewModel.navigateTo(AppScreen.VALOR) },
                    onOpenBalance = { viewModel.navigateTo(AppScreen.BALANCE) },
                    onOpenGdd = { viewModel.navigateTo(AppScreen.GDD) },
                    onOpenPlan = { viewModel.navigateTo(AppScreen.PLAN) },
                    onOpenLaunch = { viewModel.navigateTo(AppScreen.LAUNCH) },
                    modifier = Modifier.systemBarsPadding()
                )
            }

            AppScreen.MISSION_SELECT -> {
                MissionSelectScreen(
                    missions = missions,
                    profile = profile,
                    onSelectMission = { viewModel.selectMission(it) },
                    onBack = { viewModel.navigateTo(AppScreen.TITLE) },
                    modifier = Modifier.systemBarsPadding()
                )
            }

            AppScreen.BRIEFING -> {
                val dossier = MissionCatalog.byId(selectedMissionId)
                BriefingScreen(
                    dossier = dossier,
                    onDeploy = { is3D ->
                        if (is3D) {
                            viewModel.navigateTo(AppScreen.MISSION_PLAY_3D)
                        } else {
                            viewModel.navigateTo(AppScreen.MISSION_PLAY_2D)
                        }
                    },
                    onBack = { viewModel.navigateTo(AppScreen.MISSION_SELECT) },
                    allowTacticalMap = dossier.chapterNumber == 1,
                    modifier = Modifier.systemBarsPadding()
                )
            }

            AppScreen.MISSION_PLAY_3D -> {
                // Real-time 3D Three.js chapter viewport
                Game3DView(
                    missionId = selectedMissionId,
                    sensitivity = settings?.joystickSensitivity ?: 1f,
                    sfxVolume = settings?.soundEffectsVolume ?: 0.8f,
                    hapticsEnabled = settings?.hapticEnabled ?: true,
                    highContrast = settings?.highContrastHud ?: false,
                    qualityPreset = QualityPreset.fromId(settings?.qualityPreset),
                    masterVolume = settings?.masterVolume ?: 0.9f,
                    dialogueVolume = settings?.dialogueVolume ?: 0.85f,
                    subtitlesEnabled = settings?.subtitlesEnabled ?: true,
                    pauseSignal = pauseSignal,
                    onMissionFinished = { score, choice, timeSec, healthPct ->
                        viewModel.on3DMissionComplete(score, choice, timeSec, healthPct)
                    },
                    onAbortMission = { viewModel.abort3DToTitle() },
                    modifier = Modifier.fillMaxSize()
                )
            }

            AppScreen.MISSION_PLAY_2D -> {
                // Tactical 2.5D Viewport
                MissionCanvasView(
                    engine = viewModel.gameEngine,
                    modifier = Modifier.fillMaxSize()
                )

                // Virtual Analog Joystick
                VirtualJoystick(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 24.dp, bottom = 24.dp),
                    sizeDp = 124,
                    onMove = { viewModel.setJoystickVector(it) }
                )

                // Tactical HUD Overlay
                TacticalHud(
                    callsign = profile?.callsign ?: "SPECTRE-7",
                    health = viewModel.gameEngine.playerHealth,
                    morale = viewModel.gameEngine.playerMorale,
                    currentObjective = viewModel.gameEngine.objectives.getOrNull(viewModel.gameEngine.currentObjectiveIndex),
                    loopStage = viewModel.gameEngine.currentLoopStage,
                    nearestTarget = viewModel.gameEngine.nearestTarget,
                    scannerCooldownSec = viewModel.gameEngine.activeScannerCooldown,
                    timeElapsedSeconds = viewModel.gameEngine.timeElapsedSeconds,
                    onPauseClick = { viewModel.togglePause() },
                    onScanClick = { viewModel.triggerScanner() },
                    onInteractClick = { viewModel.handleInteractClick() },
                    modifier = Modifier.systemBarsPadding()
                )

                // Tactical Dialogue Modal
                activeDialogue?.let { dialogue ->
                    DialogueBox(
                        dialogue = dialogue,
                        onDismiss = { viewModel.dismissDialogue() },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .systemBarsPadding()
                    )
                }

                // Critical Decision Modal
                activeDecision?.let { choice ->
                    DecisionModal(
                        choice = choice,
                        onSelectOption = { viewModel.selectDecisionOption(it) }
                    )
                }

                // Pause Modal
                if (isPaused) {
                    PauseModal(
                        onResume = { viewModel.togglePause() },
                        onRestart = { viewModel.restartMission() },
                        onQuitToTitle = {
                            viewModel.togglePause()
                            viewModel.navigateTo(AppScreen.TITLE)
                        }
                    )
                }
            }

            AppScreen.ACADEMY -> {
                AcademyScreen(
                    controller = viewModel.academy,
                    onBack = { viewModel.navigateTo(AppScreen.TITLE) },
                    onStartDrill = { drill ->
                        if (viewModel.academy.startDrill(drill)) {
                            viewModel.navigateTo(AppScreen.DRILL)
                        }
                    },
                    modifier = Modifier.systemBarsPadding()
                )
            }

            AppScreen.ARMORY -> {
                ArmoryScreen(
                    controller = viewModel.academy,
                    onBack = { viewModel.navigateTo(AppScreen.TITLE) },
                    modifier = Modifier.systemBarsPadding()
                )
            }

            AppScreen.RANKS -> {
                RankScreen(
                    controller = viewModel.academy,
                    onBack = { viewModel.navigateTo(AppScreen.TITLE) },
                    modifier = Modifier.systemBarsPadding()
                )
            }

            AppScreen.VANGUARD -> {
                VanguardSystemsScreen(
                    onBack = { viewModel.navigateTo(AppScreen.TITLE) },
                    modifier = Modifier.systemBarsPadding()
                )
            }

            AppScreen.TACTICAL -> {
                TacticalEvolutionScreen(
                    onBack = { viewModel.navigateTo(AppScreen.TITLE) },
                    modifier = Modifier.systemBarsPadding()
                )
            }

            AppScreen.VALOR -> {
                ValorBibleScreen(
                    onBack = { viewModel.navigateTo(AppScreen.TITLE) },
                    modifier = Modifier.systemBarsPadding()
                )
            }

            AppScreen.BALANCE -> {
                ValorBalanceScreen(
                    onBack = { viewModel.navigateTo(AppScreen.TITLE) },
                    modifier = Modifier.systemBarsPadding()
                )
            }

            AppScreen.GDD -> {
                ValorGddScreen(
                    onBack = { viewModel.navigateTo(AppScreen.TITLE) },
                    modifier = Modifier.systemBarsPadding()
                )
            }

            AppScreen.PLAN -> {
                ValorPlanScreen(
                    onBack = { viewModel.navigateTo(AppScreen.TITLE) },
                    modifier = Modifier.systemBarsPadding()
                )
            }

            AppScreen.LAUNCH -> {
                LaunchOpsScreen(
                    onBack = { viewModel.navigateTo(AppScreen.TITLE) },
                    modifier = Modifier.systemBarsPadding()
                )
            }

            AppScreen.DRILL -> {
                DrillPlayScreen(
                    controller = viewModel.academy,
                    onExit = { viewModel.navigateTo(AppScreen.ACADEMY) },
                    modifier = Modifier.systemBarsPadding()
                )
            }

            AppScreen.DEBRIEF -> {
                val result = lastResult
                if (result == null) {
                    // No recorded run (e.g. process restored) - return to the board.
                    LaunchedEffect(Unit) { viewModel.navigateTo(AppScreen.MISSION_SELECT) }
                } else {
                    DebriefScreen(
                        result = result,
                        onContinue = { viewModel.navigateTo(AppScreen.MISSION_SELECT) },
                        onReplay = { viewModel.replayLastMission() },
                        modifier = Modifier.systemBarsPadding()
                    )
                }
            }

            AppScreen.ROSTER -> {
                RosterScreen(
                    profile = profile,
                    onBack = { viewModel.navigateTo(AppScreen.TITLE) },
                    modifier = Modifier.systemBarsPadding()
                )
            }

            AppScreen.SETTINGS -> {
                SettingsScreen(
                    settings = settings ?: TacticalSettingsEntity(),
                    onSaveSettings = { viewModel.saveSettings(it) },
                    onResetCampaign = { viewModel.resetCampaign() },
                    onBack = { viewModel.navigateTo(AppScreen.TITLE) },
                    modifier = Modifier.systemBarsPadding()
                )
            }

            AppScreen.EXPORT_SPEC -> {
                ReactNativeSpecScreen(
                    onBack = { viewModel.navigateTo(AppScreen.TITLE) },
                    modifier = Modifier.systemBarsPadding()
                )
            }
        }

        // Academy promotion ceremony overlays whichever screen is active.
        PromotionOverlay(
            award = academyPromotion,
            onDismiss = { viewModel.academy.dismissPromotion() }
        )
    }
}
