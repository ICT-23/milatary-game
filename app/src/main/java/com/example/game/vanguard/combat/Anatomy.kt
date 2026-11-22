package com.example.game.vanguard.combat

import com.example.game.vanguard.core.GameErrorCategory
import com.example.game.vanguard.core.GameResult

/**
 * Section 3.1 gate 4: four distinct anatomical colliders replace any single
 * capsule. The identifiers are the ones the specification names, so a scene
 * binding can look them up by string.
 */
enum class AnatomicalHitbox(
    val colliderId: String,
    val displayName: String,
    val multiplier: Float,
    val bypassesTorsoArmor: Boolean,
    val collapsesMobility: Boolean,
    val isLimb: Boolean,
    val aimSwayPenalty: Float,
    val movementSlowPenalty: Float
) {
    SKULL(
        colliderId = "Hitbox_Skull",
        displayName = "Cranial Vault",
        multiplier = 3.5f,
        bypassesTorsoArmor = true,
        collapsesMobility = false,
        isLimb = false,
        aimSwayPenalty = 0f,
        movementSlowPenalty = 0f
    ),
    TORSO(
        colliderId = "Hitbox_Torso",
        displayName = "Sternum & Thoracic Vitals",
        multiplier = 1.0f,
        bypassesTorsoArmor = false,
        collapsesMobility = false,
        isLimb = false,
        aimSwayPenalty = 0f,
        movementSlowPenalty = 0f
    ),
    PELVIS(
        colliderId = "Hitbox_Pelvis",
        displayName = "Pelvic Girdle",
        multiplier = 1.1f,
        bypassesTorsoArmor = true,
        collapsesMobility = true,
        isLimb = false,
        aimSwayPenalty = 0f,
        movementSlowPenalty = 1f
    ),
    LIMBS(
        colliderId = "Hitbox_Limbs",
        displayName = "Upper & Lower Extremities",
        multiplier = 0.45f,
        bypassesTorsoArmor = false,
        collapsesMobility = false,
        isLimb = true,
        aimSwayPenalty = 0.25f,
        movementSlowPenalty = 0.35f
    )
}

/**
 * Plate rating versus ammunition penetration. A plate that outclasses the round
 * stops it; anything else passes through and pays full damage.
 */
data class TargetArmor(
    val plateRating: Int = 0,
    val ammoPenetrationRating: Int = 0
) {
    val plateStopsRound: Boolean get() = plateRating > 0 && ammoPenetrationRating < plateRating
}

/** Everything a hit produces: numbers for the simulation, flags for the VFX layer. */
data class WoundOutcome(
    val hitbox: AnatomicalHitbox,
    val damage: Float,
    val absorbedByPlate: Boolean,
    val penetratedPlate: Boolean,
    val mobilityCollapsed: Boolean,
    val aimSwayPenalty: Float,
    val movementSlowPenalty: Float,
    val entryWoundDecal: Boolean,
    val exitWoundDecal: Boolean,
    val residualEnergyJoules: Float
)

object AnatomicalDamage {

    /** Energy that still has enough left to punch out the far side. */
    const val EXIT_WOUND_ENERGY_JOULES = 400f

    fun resolve(
        hitbox: AnatomicalHitbox,
        baseDamage: Float,
        armor: TargetArmor = TargetArmor(),
        impactEnergyJoules: Float = 2_500f
    ): GameResult<WoundOutcome> {
        if (baseDamage <= 0f) {
            return GameResult.fail(
                GameErrorCategory.INVALID_STATE,
                "NO_INCOMING_DAMAGE",
                "A hit must carry positive base damage"
            )
        }

        val plateApplies = !hitbox.bypassesTorsoArmor && armor.plateRating > 0
        if (plateApplies && armor.plateStopsRound) {
            return GameResult.Ok(
                WoundOutcome(
                    hitbox = hitbox,
                    damage = 0f,
                    absorbedByPlate = true,
                    penetratedPlate = false,
                    mobilityCollapsed = false,
                    aimSwayPenalty = 0f,
                    movementSlowPenalty = 0f,
                    entryWoundDecal = false,
                    exitWoundDecal = false,
                    residualEnergyJoules = 0f
                )
            )
        }

        val damage = (baseDamage * hitbox.multiplier).coerceAtLeast(0f)
        val residual = (impactEnergyJoules * hitbox.multiplier * 0.5f).coerceAtLeast(0f)
        return GameResult.Ok(
            WoundOutcome(
                hitbox = hitbox,
                damage = damage,
                absorbedByPlate = false,
                penetratedPlate = plateApplies,
                mobilityCollapsed = hitbox.collapsesMobility,
                aimSwayPenalty = hitbox.aimSwayPenalty,
                movementSlowPenalty = hitbox.movementSlowPenalty,
                entryWoundDecal = damage > 0f,
                exitWoundDecal = residual >= EXIT_WOUND_ENERGY_JOULES,
                residualEnergyJoules = residual
            )
        )
    }

    /** Limbs split their penalties: an arm hit spoils aim, a leg hit slows movement. */
    fun limbPenalty(hitbox: AnatomicalHitbox, isArm: Boolean): Float {
        if (!hitbox.isLimb) return 0f
        return if (isArm) hitbox.aimSwayPenalty else hitbox.movementSlowPenalty
    }
}

/**
 * The Mozambique Drill the specification calls out by name: two sternum impacts
 * stagger the target, and the third shot to the cranial vault is the finisher.
 */
data class MozambiqueState(
    val torsoHits: Int,
    val staggered: Boolean,
    val finisher: Boolean
)

class MozambiqueTracker(private val requiredTorsoHits: Int = 2) {

    private var torsoHits = 0
    private var staggered = false
    private var finisher = false

    fun record(hitbox: AnatomicalHitbox): MozambiqueState {
        when (hitbox) {
            AnatomicalHitbox.TORSO -> {
                torsoHits += 1
                if (torsoHits >= requiredTorsoHits) staggered = true
            }
            AnatomicalHitbox.SKULL -> {
                if (staggered) finisher = true
            }
            else -> Unit
        }
        return snapshot()
    }

    fun snapshot(): MozambiqueState = MozambiqueState(torsoHits, staggered, finisher)

    fun reset() {
        torsoHits = 0
        staggered = false
        finisher = false
    }
}
