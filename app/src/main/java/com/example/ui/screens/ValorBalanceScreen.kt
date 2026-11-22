package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.valor.balance.BalanceAudit
import com.example.ui.components.MonoLabel
import com.example.ui.theme.DeepGraphite
import com.example.ui.theme.DeepGraphiteBorder
import com.example.ui.theme.DeepGraphiteElevated
import com.example.ui.theme.WeatheredIvory

/**
 * Sections 1 - 9 of the Content & Balance Data as a live read-out.
 *
 * Every figure is read back from `com.example.game.valor.balance`; the section
 * bodies live in [BalanceSections.kt] and [BalanceContentSections.kt], and the
 * footer runs the twelve-group contract gate that guards those tables.
 */
@Composable
fun ValorBalanceScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val audit = remember { BalanceAudit.audit() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepGraphite)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("balance_screen")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DeepGraphiteElevated)
                    .border(1.dp, DeepGraphiteBorder, RoundedCornerShape(8.dp))
                    .testTag("balance_back_button")
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = WeatheredIvory)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "CONTENT & BALANCE DATA",
                    style = MaterialTheme.typography.titleLarge,
                    color = WeatheredIvory,
                    letterSpacing = 1.5.sp
                )
                MonoLabel(text = "VALOR ACADEMY - WEAPONS, ENEMIES, XP, ECONOMY, 60 MISSIONS", size = 10)
            }
        }

        BalanceCombatSection()
        BalanceWeaponSection()
        BalanceGunsmithSection()
        BalanceAttachmentSection()
        BalanceGearSection()
        BalanceEnemySection()
        BalanceWaveSection()
        BalanceDifficultySection()
        BalanceRankSection()
        BalanceSkillSection()
        BalanceEconomySection()
        BalanceMissionSection()
        BalanceNpcSection()
        BalanceContractSection()
        BalanceGateSection(audit)
        Spacer(modifier = Modifier.height(16.dp))
    }
}
