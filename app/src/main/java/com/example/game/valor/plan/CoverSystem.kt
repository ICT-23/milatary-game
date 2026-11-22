package com.example.game.valor.plan

/** Section 5.8: a cover point is low or high, and may be destructible. */
enum class CoverHeight(val displayName: String) {
    LOW("Low"),
    HIGH("High")
}

/** Section 5.8: how cover was left, which decides whether the player is exposed. */
enum class CoverExit(val displayName: String) {
    MOVED_AWAY("Moved away"),
    PRESSED_COVER("Pressed Cover again"),
    DESTROYED("Cover destroyed (auto-exit)")
}

/** Section 5.8: one slot on the Cover layer, claimed by at most one occupant. */
class CoverPoint(
    val id: String,
    val position: Vec3,
    val height: CoverHeight,
    destructibleHp: Float = Float.POSITIVE_INFINITY
) {
    var occupantId: String? = null
        private set

    var remainingHp: Float = destructibleHp
        private set

    val destructible: Boolean get() = remainingHp.isFinite()
    val destroyed: Boolean get() = destructible && remainingHp <= 0f

    /** "Claim system: one occupant per slot." */
    fun claim(entityId: String): Boolean {
        if (destroyed) return false
        if (occupantId != null) return occupantId == entityId
        occupantId = entityId
        return true
    }

    fun release(entityId: String) {
        if (occupantId == entityId) occupantId = null
    }

    /** Returns true when this hit destroyed the cover. */
    fun applyDamage(amount: Float): Boolean {
        if (!destructible || amount <= 0f) return false
        remainingHp = (remainingHp - amount).coerceAtLeast(0f)
        if (destroyed) occupantId = null
        return destroyed
    }
}

/** Section 5.8: the snap, peek, blind-fire and exit rules. */
object CoverRules {

    const val SNAP_DISTANCE_METERS = 1.2f
    const val FACING_ANGLE_DEGREES = 70f
    const val BLIND_FIRE_SPREAD_MULT = 2f
    const val BLIND_FIRE_ACCURACY_MULT = 0.6f
    const val COVER_BLOCKS_BULLETS = true

    /** "Within 1.2 m AND facing it (angle < 70°) AND presses Cover/auto-snap." */
    fun canSnap(distanceMeters: Float, facingAngleDegrees: Float, autoSnapSetting: Boolean): Boolean =
        distanceMeters <= SNAP_DISTANCE_METERS &&
            facingAngleDegrees < FACING_ANGLE_DEGREES &&
            autoSnapSetting

    /** Low cover forces a crouch; high cover allows standing. */
    fun crouchesAt(height: CoverHeight): Boolean = height == CoverHeight.LOW

    fun blocksBullets(): Boolean = COVER_BLOCKS_BULLETS

    /** "Peek = move aim up/out", which exposes the upper body to the ray. */
    fun peekedHeightFraction(height: CoverHeight, peeking: Boolean): Float = when {
        !peeking -> 0f
        height == CoverHeight.LOW -> 0.5f
        else -> 1f
    }

    /** "Blind fire = shoot with 2x spread" and a reduced accuracy, not damage. */
    fun blindFireSpread(baseSpread: Float): Float = baseSpread * BLIND_FIRE_SPREAD_MULT

    fun blindFireAccuracy(baseAccuracy: Float): Float = baseAccuracy * BLIND_FIRE_ACCURACY_MULT

    fun damageUnchangedByBlindFire(baseDamage: Float): Float = baseDamage

    /** "Leaving cover: moving away or pressing Cover again." */
    fun leavesByDistance(distanceMeters: Float): Boolean = distanceMeters > SNAP_DISTANCE_METERS

    /** "When destroyed, the player is exposed (auto-exit cover)." */
    fun exitOnDestroyed(cover: CoverPoint): CoverExit =
        if (cover.destroyed) CoverExit.DESTROYED else CoverExit.PRESSED_COVER

    /** Cover on the same layer is used by the player and by the enemy AI. */
    fun claimableBy(cover: CoverPoint, entityId: String): Boolean = cover.claim(entityId)
}
