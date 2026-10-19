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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CoolSteel
import com.example.ui.theme.DeepGraphite
import com.example.ui.theme.DeepGraphiteBorder
import com.example.ui.theme.DeepGraphiteElevated
import com.example.ui.theme.FieldOlive
import com.example.ui.theme.RadarCyan
import com.example.ui.theme.SignalAmber
import com.example.ui.theme.WeatheredIvory

@Composable
fun DebriefScreen(
    choiceMade: String?,
    timeElapsedSeconds: Float,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepGraphite)
            .padding(24.dp)
            .testTag("debrief_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // TOP BADGE
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(SignalAmber.copy(alpha = 0.2f))
                    .border(2.dp, SignalAmber, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MilitaryTech,
                    contentDescription = null,
                    tint = SignalAmber,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "MISSION ACCOMPLISHED",
                style = MaterialTheme.typography.headlineMedium,
                color = WeatheredIvory,
                letterSpacing = 2.sp
            )

            Text(
                text = "AFTER-ACTION REPORT // CHAPTER 1: SIGNAL IN THE DUST",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = SignalAmber,
                fontWeight = FontWeight.Bold
            )
        }

        // CENTER STATS & REWARDS CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DeepGraphiteElevated),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Star Rating
                Row(horizontalArrangement = Arrangement.Center) {
                    repeat(3) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = SignalAmber,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Time & Score
                val mins = (timeElapsedSeconds / 60).toInt()
                val secs = (timeElapsedSeconds % 60).toInt()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "TIME", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = CoolSteel)
                        Text(text = String.format("%02d:%02d", mins, secs), fontFamily = FontFamily.Monospace, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = WeatheredIvory)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "RATING", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = CoolSteel)
                        Text(text = "DISTINGUISHED", fontFamily = FontFamily.Monospace, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = RadarCyan)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "EXPERIENCE", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = CoolSteel)
                        Text(text = "+250 XP", fontFamily = FontFamily.Monospace, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SignalAmber)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tactical Choice Outcome
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DeepGraphite)
                        .border(1.dp, DeepGraphiteBorder, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "COMMAND DISPATCH RECORD:",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = SignalAmber
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val decisionSummary = if (choiceMade == "SAFEGUARD_CIVILIANS") {
                            "Humanitarian protocol enacted. 40 civilians safeguarded from generator rupture. Voss trust increased (+15), Idris respect increased (+20)."
                        } else {
                            "Intel harvest protocol enacted. Encrypted saboteur frequency recovered. High command gained enemy ciphers. Rowan rivalry reduced (-15)."
                        }
                        Text(
                            text = decisionSummary,
                            style = MaterialTheme.typography.bodySmall,
                            color = WeatheredIvory,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Promotion Banner
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(FieldOlive.copy(alpha = 0.25f))
                        .border(1.dp, FieldOlive, RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(imageVector = Icons.Default.MilitaryTech, contentDescription = null, tint = SignalAmber, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PROMOTION: CADET OPERATIVE RANK ACHIEVED",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = WeatheredIvory
                    )
                }
            }
        }

        // BOTTOM ACTION
        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("debrief_continue_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = SignalAmber,
                contentColor = DeepGraphite
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                text = "PROCEED TO HEADQUARTERS",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 1.sp
            )
        }
    }
}
