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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.TacticalSettingsEntity
import com.example.ui.AppScreen
import com.example.ui.EmberwatchViewModel
import com.example.ui.components.DecisionModal
import com.example.ui.components.DialogueBox
import com.example.ui.components.PauseModal
import com.example.ui.components.TacticalHud
import com.example.ui.components.VirtualJoystick
import com.example.ui.screens.BriefingScreen
import com.example.ui.screens.DebriefScreen
import com.example.ui.screens.Game3DView
import com.example.ui.screens.MissionCanvasView
import com.example.ui.screens.ReactNativeSpecScreen
import com.example.ui.screens.RosterScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TitleScreen
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
    val selectedChoice by viewModel.selectedChoice.collectAsStateWithLifecycle()
    val isPaused by viewModel.isPaused.collectAsStateWithLifecycle()

    // Android Hardware/Gesture Back Navigation
    BackHandler(enabled = currentScreen != AppScreen.TITLE) {
        when (currentScreen) {
            AppScreen.BRIEFING -> viewModel.navigateTo(AppScreen.TITLE)
            AppScreen.MISSION_PLAY_3D -> viewModel.navigateTo(AppScreen.TITLE)
            AppScreen.MISSION_PLAY_2D -> {
                if (activeDecision != null) {
                    // Cannot back out of critical crisis decision
                } else if (activeDialogue != null) {
                    viewModel.dismissDialogue()
                } else {
                    viewModel.togglePause()
                }
            }
            AppScreen.DEBRIEF -> viewModel.navigateTo(AppScreen.TITLE)
            AppScreen.ROSTER -> viewModel.navigateTo(AppScreen.TITLE)
            AppScreen.SETTINGS -> viewModel.navigateTo(AppScreen.TITLE)
            AppScreen.EXPORT_SPEC -> viewModel.navigateTo(AppScreen.TITLE)
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
                    onStartCampaign = { viewModel.navigateTo(AppScreen.BRIEFING) },
                    onOpenRoster = { viewModel.navigateTo(AppScreen.ROSTER) },
                    onOpenSettings = { viewModel.navigateTo(AppScreen.SETTINGS) },
                    onOpenExportSpec = { viewModel.navigateTo(AppScreen.EXPORT_SPEC) },
                    modifier = Modifier.systemBarsPadding()
                )
            }

            AppScreen.BRIEFING -> {
                BriefingScreen(
                    onDeploy = { is3D ->
                        if (is3D) {
                            viewModel.navigateTo(AppScreen.MISSION_PLAY_3D)
                        } else {
                            viewModel.navigateTo(AppScreen.MISSION_PLAY_2D)
                        }
                    },
                    onBack = { viewModel.navigateTo(AppScreen.TITLE) },
                    modifier = Modifier.systemBarsPadding()
                )
            }

            AppScreen.MISSION_PLAY_3D -> {
                // Real-time 3D Three.js Animated Game Viewport
                Game3DView(
                    onMissionFinished = { score, choice, timeSec ->
                        viewModel.on3DMissionComplete(score, choice, timeSec)
                    },
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

            AppScreen.DEBRIEF -> {
                DebriefScreen(
                    choiceMade = selectedChoice?.id,
                    timeElapsedSeconds = viewModel.gameEngine.timeElapsedSeconds,
                    onContinue = { viewModel.navigateTo(AppScreen.TITLE) },
                    modifier = Modifier.systemBarsPadding()
                )
            }

            AppScreen.ROSTER -> {
                RosterScreen(
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
    }
}
