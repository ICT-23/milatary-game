package com.example.game.tactical.gunsmith

/**
 * Phase 4 deliverables: the 3D workbench camera and part snapping.
 *
 * The camera orbits and zooms inside fixed limits so the bench can never swing
 * under the floor or into the receiver, and a part only seats when it is dropped
 * within tolerance of its own socket.
 */
data class WorkbenchCamera(
    val yawDegrees: Float = 25f,
    val pitchDegrees: Float = 12f,
    val zoomMeters: Float = 0.85f
) {
    val pitchWithinLimits: Boolean get() = pitchDegrees in WorkbenchRig.MIN_PITCH..WorkbenchRig.MAX_PITCH
    val zoomWithinLimits: Boolean get() = zoomMeters in WorkbenchRig.MIN_ZOOM..WorkbenchRig.MAX_ZOOM
}

object WorkbenchRig {

    const val MIN_PITCH = -15f
    const val MAX_PITCH = 70f
    const val MIN_ZOOM = 0.35f
    const val MAX_ZOOM = 1.80f
    const val ORBIT_SENSITIVITY_DEGREES = 0.25f
    const val DEFAULT_SNAP_TOLERANCE_METERS = 0.03f

    fun orbit(camera: WorkbenchCamera, deltaYawDegrees: Float, deltaPitchDegrees: Float): WorkbenchCamera =
        camera.copy(
            yawDegrees = wrapDegrees(camera.yawDegrees + deltaYawDegrees * ORBIT_SENSITIVITY_DEGREES),
            pitchDegrees = (camera.pitchDegrees + deltaPitchDegrees * ORBIT_SENSITIVITY_DEGREES)
                .coerceIn(MIN_PITCH, MAX_PITCH)
        )

    fun zoom(camera: WorkbenchCamera, deltaMeters: Float): WorkbenchCamera =
        camera.copy(zoomMeters = (camera.zoomMeters + deltaMeters).coerceIn(MIN_ZOOM, MAX_ZOOM))

    fun reset(): WorkbenchCamera = WorkbenchCamera()

    private fun wrapDegrees(value: Float): Float {
        val wrapped = value % 360f
        return if (wrapped < 0f) wrapped + 360f else wrapped
    }
}

/** A 3D socket on the receiver that accepts exactly one assembly part. */
data class PartSocket(
    val part: AssemblyPart,
    val position: Vec3Like,
    val rotationDegrees: Float = 0f
)

/** Minimal position triple so the rig does not depend on the ballistics package. */
data class Vec3Like(val x: Float, val y: Float, val z: Float) {
    fun distanceTo(other: Vec3Like): Float {
        val dx = x - other.x
        val dy = y - other.y
        val dz = z - other.z
        return kotlin.math.sqrt(dx * dx + dy * dy + dz * dz)
    }
}

data class SnapResult(
    val seatedPart: AssemblyPart?,
    val socketPart: AssemblyPart?,
    val distanceMeters: Float,
    val message: String
) {
    val accepted: Boolean get() = seatedPart != null && seatedPart == socketPart
}

object PartSnapper {

    fun socketFor(sockets: List<PartSocket>, part: AssemblyPart): PartSocket? =
        sockets.firstOrNull { it.part == part }

    /** Drops [part] at [position] and seats it only inside the tolerance band. */
    fun snap(
        part: AssemblyPart,
        position: Vec3Like,
        sockets: List<PartSocket>,
        toleranceMeters: Float = WorkbenchRig.DEFAULT_SNAP_TOLERANCE_METERS
    ): SnapResult {
        val socket = socketFor(sockets, part)
            ?: return SnapResult(
                part, null, Float.MAX_VALUE,
                "${part.displayName} has no socket on this receiver"
            )
        val distance = position.distanceTo(socket.position)
        return if (distance <= toleranceMeters) {
            SnapResult(part, socket.part, distance, "${part.displayName} seated")
        } else {
            SnapResult(
                part, socket.part, distance,
                "%.1f mm from the ${part.displayName} socket".format(distance * 1_000f)
            )
        }
    }

    /** Parts still open on the receiver, in assembly order. */
    fun openSockets(sockets: List<PartSocket>, seated: Set<AssemblyPart>): List<PartSocket> =
        sockets.filter { it.part !in seated }.sortedBy { it.part.ordinal }
}
