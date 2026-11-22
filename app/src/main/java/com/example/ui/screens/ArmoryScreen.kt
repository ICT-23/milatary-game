package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.game.combat.AttachmentSocket
import com.example.game.combat.CombatAudit
import com.example.game.combat.CombatValidation
import com.example.game.combat.GunsmithCatalog
import com.example.ui.AcademyController
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CoolSteel
import com.example.ui.theme.DeepGraphite
import com.example.ui.theme.DeepGraphiteElevated
import com.example.ui.theme.RadarCyan
import com.example.ui.theme.SignalAmber
import com.example.ui.theme.WeatheredIvory

/**
 * Phase 3 of the roadmap: the armory workbench. Sockets are physically
 * interchangeable and every choice recomputes the weapon from its base stats.
 */
@Composable
fun ArmoryScreen(
    controller: AcademyController,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val revision by controller.revision.collectAsStateWithLifecycle()
    val progress by controller.progress.collectAsStateWithLifecycle()
    val weaponId by controller.weaponId.collectAsStateWithLifecycle()
    val selection by controller.selection.collectAsStateWithLifecycle()

    val build = remember(revision, weaponId, selection, progress?.rankIndex) { controller.currentBuild() }
    val rank = controller.tier
    val weapons = remember(progress?.rankIndex) { controller.weaponsAvailable() }
    // Section 8.11: the audit fires the real pipeline once per visit to the workbench.
    val audit = remember { CombatValidation.audit() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepGraphite)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("armory_screen")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(onClick = onBack, shape = RoundedCornerShape(8.dp)) {
                Text("BACK", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ARMORY / GUNSMITH WORKBENCH",
                    color = WeatheredIvory,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "${rank.display}  |  ${controller.meritPoints} MP",
                    color = SignalAmber,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            item {
                Text(
                    text = "RECEIVER",
                    color = CoolSteel,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(GunsmithCatalog.WEAPONS) { weapon ->
                        val unlocked = controller.rankIndex >= weapon.requiredRankIndex
                        FilterChip(
                            selected = weapon.id == weaponId,
                            onClick = { controller.selectWeapon(weapon.id) },
                            enabled = unlocked,
                            label = {
                                Text(
                                    text = if (unlocked) weapon.name else "${weapon.name} [LOCKED]",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SignalAmber,
                                selectedLabelColor = DeepGraphite,
                                labelColor = WeatheredIvory
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                WeaponStatsCard(build, rank.display)
                Spacer(modifier = Modifier.height(12.dp))
                CombatAuditCard(audit)
                Spacer(modifier = Modifier.height(12.dp))
            }

            items(GunsmithCatalog.DEFAULT_SOCKETS) { socket ->
                SocketRow(
                    socket = socket,
                    controller = controller,
                    selectedId = selection[socket] ?: ""
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = { GunsmithCatalog.DEFAULT_SOCKETS.forEach(controller::resetSocket) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("RESET", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            }
            Button(
                onClick = { controller.saveLoadout() },
                modifier = Modifier.weight(1.6f),
                colors = ButtonDefaults.buttonColors(containerColor = SignalAmber, contentColor = DeepGraphite),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    "SAVE LOADOUT",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun WeaponStatsCard(
    build: com.example.game.combat.WeaponBuild,
    rankDisplay: String
) {
    val stats = build.stats
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DeepGraphiteElevated)
            .padding(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = build.name,
                color = WeatheredIvory,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Text(
                text = build.base.caliber,
                color = CoolSteel,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        val rows = listOf(
            "DAMAGE" to "${stats.damage.toInt()}  (${build.fireMode.displayName})",
            "CYCLIC" to "${stats.rpm.toInt()} RPM",
            "MAGAZINE" to "${stats.magazineCapacity} rnd  |  reserve ${stats.reserveRounds}",
            "VELOCITY" to "${stats.muzzleVelocity.toInt()} m/s  |  ${stats.effectiveRangeMeters.toInt()} m",
            "ARMOR PEN" to "${(stats.armorPenetration * 100).toInt()}%",
            "ADS" to "${(stats.adsSeconds * 1000).toInt()} ms  |  optic ${stats.opticZoom.toInt()}x",
            "RECOIL" to "V ${"%.2f".format(stats.verticalRecoil)}  /  H ${"%.2f".format(stats.horizontalRecoil)}",
            "SPREAD" to "${"%.2f".format(stats.spreadDegrees)} deg  |  drop x${"%.2f".format(stats.dropFactor)}",
            "WEIGHT" to "${"%.2f".format(stats.weightKg)} kg  |  report ${stats.audioSignaturePercent.toInt()}%",
            "MUZZLE" to if (stats.suppressed) "SUPPRESSED · flash hidden" else "Unsuppressed · flash visible",
            "CLEARANCE" to rankDisplay
        )
        rows.forEach { (label, value) ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp)) {
                Text(
                    text = label,
                    color = CoolSteel,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    modifier = Modifier.width(86.dp)
                )
                Text(
                    text = value,
                    color = WeatheredIvory,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp
                )
            }
        }
    }
}

@Composable
private fun CombatAuditCard(audit: CombatAudit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DeepGraphiteElevated)
            .padding(12.dp)
            .testTag("combat_audit_card")
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = "BALLISTIC MODEL AUDIT",
                color = WeatheredIvory,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
            Text(
                text = "${audit.passed}/${audit.total}",
                color = if (audit.allPassed) RadarCyan else AlertRed,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "SECTION 8.11 ACCEPTANCE CASES",
            color = CoolSteel,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        audit.results.forEach { result ->
            Text(
                text = "${if (result.passed) "PASS" else "FAIL"}  ${result.index}. ${result.title}",
                color = if (result.passed) CoolSteel else AlertRed,
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp
            )
            if (!result.passed) {
                Text(
                    text = "     expected ${result.expectation} · ${result.detail}",
                    color = AlertRed,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 8.sp
                )
            }
        }
    }
}

@Composable
private fun SocketRow(
    socket: AttachmentSocket,
    controller: AcademyController,
    selectedId: String
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
        Text(
            text = socket.displayName.uppercase(),
            color = CoolSteel,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(GunsmithCatalog.partsFor(socket)) { part ->
                val locked = controller.isLocked(part)
                FilterChip(
                    selected = part.id == selectedId,
                    onClick = { controller.equip(part.id) },
                    enabled = !locked,
                    label = {
                        Column {
                            Text(
                                text = if (locked) "${part.name} [LOCKED]" else part.name,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            )
                            if (part.id == selectedId) {
                                Text(
                                    text = part.detail,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 8.sp,
                                    color = if (locked) AlertRed else RadarCyan
                                )
                            }
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = DeepGraphiteElevated,
                        selectedLabelColor = SignalAmber,
                        labelColor = WeatheredIvory
                    )
                )
            }
        }
    }
}
