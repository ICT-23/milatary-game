package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.game.valor.balance.AttachmentSpec
import com.example.game.valor.balance.DataContract
import com.example.game.valor.balance.FieldItemSpec
import com.example.game.valor.balance.GearItem
import com.example.game.valor.balance.MissionSpec
import com.example.game.valor.balance.NpcSpec
import com.example.game.valor.balance.RankSpec
import com.example.game.valor.balance.SkillSpec
import com.example.game.valor.balance.WeaponSpec
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CoolSteel
import com.example.ui.theme.RadarCyan
import com.example.ui.theme.SignalAmber
import com.example.ui.theme.WeatheredIvory
import kotlin.math.roundToInt

/**
 * One-line read-outs for the balance tables. Each line prints the numbers the
 * domain object actually holds, so the screen can never drift from the data.
 */
@Composable
internal fun BalanceWeaponLine(weapon: WeaponSpec) {
    val stkMiss = weapon.targetStk != null && weapon.targetStk != weapon.derivedStk
    val target = weapon.targetStk?.let { " / target $it" } ?: " / prose"
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp)) {
        MonoLabel(
            text = "${weapon.name} [${weapon.weaponClass.displayName}]",
            size = 9,
            color = WeatheredIvory
        )
        MonoLabel(
            text = "${weapon.damage.roundToInt()} dmg x${weapon.pellets} / " +
                "${weapon.rpm.roundToInt()} rpm / ${weapon.rangeMeters.roundToInt()} m / " +
                "STK ${weapon.derivedStk}$target / R${weapon.unlockRank} / ${weapon.price}",
            size = 8,
            color = if (stkMiss) AlertRed else CoolSteel
        )
    }
}

@Composable
internal fun BalanceFieldItemLine(item: FieldItemSpec) {
    val price = if (item.pricedEach) "${item.price} each" else "${item.price}"
    MonoLabel(
        text = "${item.name} - ${item.note} / R${item.unlockRank} / $price",
        size = 8
    )
}

@Composable
internal fun BalanceAttachmentLine(attachment: AttachmentSpec) {
    val mods = attachment.mods.entries.joinToString("+") { "${it.key.displayName} ${it.value}" }
    MonoLabel(
        text = "${attachment.slot.displayName}: ${attachment.name} - $mods / " +
            "${attachment.steel}s ${attachment.parts}p ${attachment.cash}c / R${attachment.unlockRank}",
        size = 8
    )
}

@Composable
internal fun BalanceGearLine(item: GearItem) {
    val tier = item.tier?.let { "T$it" } ?: "-"
    MonoLabel(
        text = "${item.slot.displayName} $tier - ${item.effect} / ${item.price} / R${item.unlockRank}",
        size = 8
    )
}

@Composable
internal fun BalanceRankLine(rank: RankSpec) {
    val xp = rank.cumulativeXp?.toString() ?: "story gated"
    MonoLabel(
        text = "${rank.index}. ${rank.name} - $xp XP / ${rank.skillPoints} pt / " +
            "act ${rank.act ?: "-"}",
        size = 8,
        color = if (rank.storyGated) SignalAmber else CoolSteel
    )
}

@Composable
internal fun BalanceSkillLine(skill: SkillSpec) {
    MonoLabel(
        text = "${skill.name} (${skill.maxLevel}) - ${skill.effectText}",
        size = 8
    )
}

@Composable
internal fun BalanceMissionLine(mission: MissionSpec) {
    MonoLabel(
        text = "${mission.id} ${mission.type.code} ${mission.name} - ${mission.location} / " +
            "${mission.baseXp} XP / ${mission.cash} cash",
        size = 8,
        color = if (mission.type.code == "BOSS") AlertRed else CoolSteel
    )
}

@Composable
internal fun BalanceNpcLine(npc: NpcSpec) {
    MonoLabel(
        text = "${npc.name} - ${npc.hp?.toString() ?: "-"} HP / ${npc.weapon ?: "-"} / " +
            "${npc.role} / ${npc.special}",
        size = 8,
        color = if (npc.isStoryCritical) RadarCyan else CoolSteel
    )
}

@Composable
internal fun BalanceContractLine(contract: DataContract) {
    val gap = if (contract.missing.isEmpty()) "complete" else contract.missing.joinToString(", ")
    MonoLabel(
        text = "${contract.schemaName} - ${contract.coveragePercent}% / " +
            "${contract.declared.size} of ${contract.fields.size}",
        size = 9,
        color = if (contract.isComplete) RadarCyan else SignalAmber
    )
    MonoLabel(text = gap, size = 8, color = CoolSteel)
}
