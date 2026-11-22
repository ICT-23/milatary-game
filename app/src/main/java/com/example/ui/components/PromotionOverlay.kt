package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MeritAward
import com.example.ui.theme.CoolSteel
import com.example.ui.theme.DeepGraphiteElevated
import com.example.ui.theme.RadarCyan
import com.example.ui.theme.SignalAmber
import com.example.ui.theme.WeatheredIvory

/** Promotion ceremony: the awarded rung, its insignia and newly unlocked kit. */
@Composable
fun PromotionOverlay(
    award: MeritAward?,
    onDismiss: () -> Unit
) {
    if (award == null || award.promoted.isEmpty()) return
    val tier = award.promoted.last()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC101418)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .clip(RoundedCornerShape(14.dp))
                .background(DeepGraphiteElevated)
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "PROMOTION CEREMONY",
                color = SignalAmber,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = tier.insignia,
                color = WeatheredIvory,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 40.sp
            )
            Text(
                text = "${tier.code} · ${tier.title}",
                color = WeatheredIvory,
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${award.totalMerit} MP  |  +${award.newRankIndex - award.previousRankIndex} rung(s)",
                color = CoolSteel,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            tier.unlocks.forEach { unlock ->
                Text(
                    text = "+ $unlock",
                    color = RadarCyan,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = SignalAmber, contentColor = Color(0xFF20262C)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    "ACKNOWLEDGE",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}
