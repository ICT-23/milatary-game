package com.example.ui.screens

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.game.valor.art.WeaponClass
import com.example.game.valor.balance.AttachmentCatalog
import com.example.game.valor.balance.AttachmentSlot
import com.example.game.valor.balance.DamageMath
import com.example.game.valor.balance.Difficulty
import com.example.game.valor.balance.EnemyCatalog
import com.example.game.valor.balance.GearCatalog
import com.example.game.valor.balance.GunsmithProgression
import com.example.game.valor.balance.HitRegion
import com.example.game.valor.balance.TimeToKillTargets
import com.example.game.valor.balance.WaveScaling
import com.example.game.valor.balance.WeaponCatalog
import com.example.ui.components.BalanceAttachmentLine
import com.example.ui.components.BalanceFieldItemLine
import com.example.ui.components.BalanceGearLine
import com.example.ui.components.BalanceWeaponLine
import com.example.ui.components.MonoLabel
import com.example.ui.components.TacticalDetailRow
import com.example.ui.components.TacticalSectionCard
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CoolSteel
import com.example.ui.theme.RadarCyan
import com.example.ui.theme.SignalAmber
import kotlin.math.roundToInt

/** Spacer plus card, so every balance section starts at the same rhythm. */
@Composable
internal fun BalanceSection(
    title: String,
    subtitle: String,
    accent: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Spacer(modifier = Modifier.height(12.dp))
    TacticalSectionCard(title, subtitle, accent, content)
}

private fun percent(factor: Float): Int = ((factor - 1f) * 100f).roundToInt()

/** Section 1 and the section 4.3 sanity targets. */
@Composable
internal fun BalanceCombatSection() {
    BalanceSection("COMBAT MATH", "SECTION 1 + 4.3 - HIT MULTIPLIERS AND TTK", RadarCyan) {
        TacticalDetailRow("HEAD / LIMB", "x${HitRegion.HEAD.multiplier} / x${HitRegion.LIMB.multiplier}")
        TacticalDetailRow("FALLOFF FLOOR", "0.5 rifle-like / 0.3 shotgun + SMG")
        TacticalDetailRow(
            "AR VS RIFLEMAN",
            "${TimeToKillTargets.AR_VS_RIFLEMAN_SECONDS} s / ${DamageMath.bodyShotsToKill(28f)} body shots"
        )
        TacticalDetailRow(
            "HEAVY GUNNER",
            "${TimeToKillTargets.HEAVY_FOCUSED_SECONDS} s focused / " +
                "${TimeToKillTargets.HEAVY_ARMOR_PIERCING_SECONDS} s AP"
        )
        TacticalDetailRow("COVER", "must exceed ${TimeToKillTargets.COVER_MUST_EXCEED_SECONDS} s")
        TacticalDetailRow("PLAYER", "${TimeToKillTargets.PLAYER_BASE_HEALTH.roundToInt()} HP base")
    }
}

/** Sections 2.1 to 2.5: the full weapon catalogue. */
@Composable
internal fun BalanceWeaponSection() {
    BalanceSection("WEAPON TABLES", "SECTIONS 2.1 - 2.5", SignalAmber) {
        TacticalDetailRow(
            "FIREARMS / FIELD ITEMS",
            "${WeaponCatalog.WEAPONS.size} / ${WeaponCatalog.FIELD_ITEMS.size}"
        )
        WeaponClass.entries.forEach { weaponClass ->
            TacticalDetailRow(
                weaponClass.displayName.uppercase(),
                "${WeaponCatalog.ofClass(weaponClass).size} rows"
            )
        }
        WeaponCatalog.stkGaps().forEach { gap ->
            TacticalDetailRow(
                "STK GAP",
                "${gap.name}: target ${gap.targetStk} vs ${gap.derivedStk} computed",
                AlertRed
            )
        }
        WeaponCatalog.WEAPONS.forEach { BalanceWeaponLine(it) }
        WeaponCatalog.FIELD_ITEMS.forEach { BalanceFieldItemLine(it) }
    }
}

/** Section 2.6: the gunsmith ladder and its costs. */
@Composable
internal fun BalanceGunsmithSection() {
    BalanceSection("GUNSMITH", "SECTION 2.6 - UPGRADES AND MASTERY", CoolSteel) {
        TacticalDetailRow("LEVELS", "${GunsmithProgression.MIN_LEVEL}-${GunsmithProgression.MAX_LEVEL}")
        TacticalDetailRow(
            "LEVEL 10",
            "+${percent(GunsmithProgression.damageMultiplier(10))}% damage / " +
                "+${percent(GunsmithProgression.handlingMultiplier(10))}% handling / " +
                "+${percent(GunsmithProgression.accuracyMultiplier(10))}% accuracy"
        )
        TacticalDetailRow("COST", "base x 1.35^(level-1), base = 10% of price, min 150")
        TacticalDetailRow(
            "STEEL",
            "${GunsmithProgression.STEEL_PER_LEVEL} x level " +
                "(${GunsmithProgression.totalSteelTo(10)} to max)"
        )
        TacticalDetailRow(
            "MASTERY",
            GunsmithProgression.MASTERY_TIERS.joinToString(" / ") { "${it.kills} ${it.reward}" }
        )
    }
}

/** Section 2.7: the modifier rows and the stacking rule. */
@Composable
internal fun BalanceAttachmentSection() {
    BalanceSection("ATTACHMENTS", "SECTION 2.7 - MODIFIER ROWS", RadarCyan) {
        TacticalDetailRow(
            "ROWS",
            "${AttachmentCatalog.ATTACHMENTS.size} across ${AttachmentSlot.entries.size} slots"
        )
        AttachmentSlot.entries.forEach { slot ->
            TacticalDetailRow(slot.displayName.uppercase(), AttachmentCatalog.forSlot(slot).size.toString())
        }
        TacticalDetailRow("STACKING", "add percentages, then multiply factors, clamp 0-3")
        AttachmentCatalog.ATTACHMENTS.forEach { BalanceAttachmentLine(it) }
    }
}

/** Section 3: helmets, vests, backpacks and support equipment. */
@Composable
internal fun BalanceGearSection() {
    BalanceSection("ARMOR & GEAR", "SECTION 3 - HELMETS, VESTS, SUPPORT", SignalAmber) {
        TacticalDetailRow(
            "HELMET MULT",
            GearCatalog.HELMETS.joinToString(" / ") { "${it.headMultiplier}" }
        )
        TacticalDetailRow(
            "VEST POOL",
            GearCatalog.VESTS.joinToString(" / ") {
                "${it.armorPoints?.roundToInt()} pt ${((it.armorAbsorb ?: 0f) * 100f).roundToInt()}%"
            }
        )
        TacticalDetailRow(
            "BACKPACK",
            GearCatalog.BACKPACKS.joinToString(" / ") { "+${it.reserveBonusPercent}%" }
        )
        GearCatalog.ALL.forEach { BalanceGearLine(it) }
    }
}

/** Section 4.1: the twelve base enemy rows. */
@Composable
internal fun BalanceEnemySection() {
    BalanceSection("ENEMIES", "SECTION 4.1 - BASE STATS", CoolSteel) {
        TacticalDetailRow(
            "ROWS / ARMOURED",
            "${EnemyCatalog.ENEMIES.size} / ${EnemyCatalog.armoredEnemies().size}"
        )
        EnemyCatalog.ENEMIES.forEach { enemy ->
            TacticalDetailRow(
                enemy.type.uppercase(),
                "${enemy.hp.roundToInt()}-${enemy.hpMax.roundToInt()} HP / " +
                    "${enemy.armor.roundToInt()} armour / ${enemy.xp} XP"
            )
        }
    }
}

/** Section 4.4: Survival wave scaling. */
@Composable
internal fun BalanceWaveSection() {
    BalanceSection("WAVE SCALING", "SECTION 4.4 - SURVIVAL", RadarCyan) {
        TacticalDetailRow(
            "COUNT",
            "${WaveScaling.enemyCount(10).roundToInt()} at wave 10, cap " +
                "${WaveScaling.TOTAL_CAP.roundToInt()}, ${WaveScaling.MAX_SIMULTANEOUS} at once"
        )
        TacticalDetailRow("HP", "1 + wave x ${WaveScaling.HP_MULT_PER_WAVE}")
        TacticalDetailRow(
            "ACCURACY",
            "+${WaveScaling.ACCURACY_PER_WAVE} per wave, cap +${WaveScaling.ACCURACY_CAP_BONUS}"
        )
        TacticalDetailRow(
            "CADENCE",
            "elite every ${WaveScaling.ELITE_EVERY}, mini boss every ${WaveScaling.MINI_BOSS_EVERY}"
        )
        TacticalDetailRow(
            "REWARDS",
            "${WaveScaling.CASH_PER_WAVE} cash / ${WaveScaling.XP_PER_WAVE} XP per wave"
        )
    }
}

/** Section 4.2: the four settings and their seven multipliers. */
@Composable
internal fun BalanceDifficultySection() {
    BalanceSection("DIFFICULTY", "SECTION 4.2 - FOUR SETTINGS", SignalAmber) {
        Difficulty.entries.forEach { setting ->
            TacticalDetailRow(
                setting.displayName.uppercase(),
                "HP x${setting.enemyHpMult} / dmg x${setting.enemyDamageMult} / " +
                    "acc x${setting.enemyAccuracyMult} / react x${setting.enemyReactionTimeMult} / " +
                    "reward x${setting.rewardMult}"
            )
            MonoLabel(
                text = "${setting.regen.displayName} regen / ${setting.friendlyFire.displayName}",
                size = 8
            )
        }
    }
}
