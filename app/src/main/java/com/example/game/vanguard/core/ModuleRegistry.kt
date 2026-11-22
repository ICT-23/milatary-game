package com.example.game.vanguard.core

/**
 * Section 4: the status vocabulary of MODULE_REGISTRY.json.
 *
 * `LOCKED_VERIFIED` is the only state that blocks an edit, and it is only
 * reachable once the contract audit has been run and has passed in full.
 */
enum class ModuleStatus(val label: String) {
    PENDING_IMPLEMENTATION("PENDING_IMPLEMENTATION"),
    IMPLEMENTED_PENDING_AUDIT_RUN("IMPLEMENTED_PENDING_AUDIT_RUN"),
    LOCKED_VERIFIED("LOCKED_VERIFIED");

    val isLocked: Boolean get() = this == LOCKED_VERIFIED
}

data class ModuleEntry(val key: String, val status: ModuleStatus)

/**
 * Section 1.1 "protect the module registry": the in-process mirror of the JSON
 * file at the repository root. An agent asks [mayEdit] before changing a
 * public signature.
 */
class ModuleRegistry(entries: List<ModuleEntry>) {

    private val statuses = LinkedHashMap<String, ModuleStatus>().apply {
        entries.forEach { entry -> put(entry.key, entry.status) }
    }

    val lockedCount: Int get() = statuses.values.count { it.isLocked }

    val size: Int get() = statuses.size

    fun statusOf(key: String): ModuleStatus? = statuses[key]

    /** An unknown key is a new module and is free to build. */
    fun mayEdit(key: String): Boolean = statuses[key]?.isLocked != true

    fun lock(key: String): Boolean {
        if (!statuses.containsKey(key)) return false
        statuses[key] = ModuleStatus.LOCKED_VERIFIED
        return true
    }

    fun markImplemented(key: String): Boolean {
        if (!statuses.containsKey(key) || statuses[key]?.isLocked == true) return false
        statuses[key] = ModuleStatus.IMPLEMENTED_PENDING_AUDIT_RUN
        return true
    }

    fun entries(): List<ModuleEntry> = statuses.map { (key, status) -> ModuleEntry(key, status) }

    fun lockedKeys(): List<String> = statuses.filterValues { it.isLocked }.keys.toList()

    companion object {
        /** The six keys fixed by Section 4, in their documented order. */
        val SPEC_KEYS: List<String> = listOf(
            "Core_EventBus_Result",
            "Progression_RankFSM_UniformBinder",
            "Gunsmith_ModularSocketGraph",
            "Combat_DualVectorBallistics_Hitboxes",
            "Narrative_DilemmaEngine",
            "Meta_ShipVehicle_PresidentialSim"
        )

        /**
         * The state the repository currently holds: six modules built and
         * covered by contract cases, none executed yet on this machine.
         */
        fun canonical(): ModuleRegistry = ModuleRegistry(
            SPEC_KEYS.map { key -> ModuleEntry(key, ModuleStatus.IMPLEMENTED_PENDING_AUDIT_RUN) }
        )

        /** Parses the flat `{"key": "STATUS"}` shape used by the JSON file. */
        fun fromPairs(pairs: Map<String, String>): ModuleRegistry = ModuleRegistry(
            pairs.map { (key, status) ->
                val parsed = ModuleStatus.entries.firstOrNull { it.label == status.trim().uppercase() }
                    ?: ModuleStatus.PENDING_IMPLEMENTATION
                ModuleEntry(key, parsed)
            }
        )
    }
}
