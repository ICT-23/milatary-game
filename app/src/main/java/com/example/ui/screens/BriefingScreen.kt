package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.MissionCatalog
import com.example.game.MissionDossier
import com.example.ui.theme.CoolSteel
import com.example.ui.theme.DeepGraphite
import com.example.ui.theme.DeepGraphiteBorder
import com.example.ui.theme.DeepGraphiteElevated
import com.example.ui.theme.FieldOlive
import com.example.ui.theme.SignalAmber
import com.example.ui.theme.WeatheredIvory

@Composable
fun BriefingScreen(
    dossier: MissionDossier = MissionCatalog.ALL_MISSIONS[0],
    onDeploy: (is3D: Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepGraphite)
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .testTag("briefing_screen")
    ) {
        // TOP BAR
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DeepGraphiteElevated)
                    .border(1.dp, DeepGraphiteBorder, RoundedCornerShape(8.dp))
                    .testTag("briefing_back_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = WeatheredIvory
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "OPERATIONAL DOSSIER // CHAPTER ${dossier.chapterNumber}",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = SignalAmber,
                    letterSpacing = 1.sp
                )
                Text(
                    text = dossier.location.uppercase(),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = CoolSteel
                )
            }

            Spacer(modifier = Modifier.size(42.dp))
        }

        Spacer(modifier = Modifier.height(14.dp))

        // BRIEFING SCROLLABLE CONTENT
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // MISSION HEADER CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DeepGraphiteElevated),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = dossier.title.uppercase(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = WeatheredIvory,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = dossier.subtitle,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = SignalAmber
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = dossier.briefingOverview,
                        style = MaterialTheme.typography.bodyMedium,
                        color = WeatheredIvory,
                        lineHeight = 20.sp
                    )
                }
            }

            // OBJECTIVES BREAKDOWN
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DeepGraphiteElevated),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "MISSION OBJECTIVES",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = SignalAmber,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Primary Objective
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SignalAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "PRIMARY: ${dossier.primaryObjective}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = WeatheredIvory
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Secondary Objective
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = FieldOlive,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "SECONDARY: ${dossier.secondaryObjective}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CoolSteel
                            )
                        }
                    }
                }
            }

            // SQUAD DISPATCH CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DeepGraphiteElevated),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "FIELD ROSTER ASSIGNMENTS",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = SignalAmber,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "• SPECTRE-7: Primary Field Operative (You)\n• TOMAS VALE (HAWK-EYE): Forward Reconnaissance Liaison\n• CAPT. MARA VOSS (IRON-HEART): Tactical Overwatch\n• IDRIS KANE (SOLACE): Medical Evac Support",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 18.sp,
                        color = WeatheredIvory
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // DEPLOY BUTTONS
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onDeploy(true) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("deploy_operative_button_3d"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SignalAmber,
                    contentColor = DeepGraphite
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "DEPLOY IN FULL 3D ACTION (REAL-TIME 3D)",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            }

            androidx.compose.material3.OutlinedButton(
                onClick = { onDeploy(false) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("deploy_operative_button_2d"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = DeepGraphiteElevated.copy(alpha = 0.8f),
                    contentColor = CoolSteel
                )
            ) {
                Text(
                    text = "DEPLOY 2.5D TACTICAL MAP MODE",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}
