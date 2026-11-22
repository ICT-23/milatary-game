package com.example.game.valor.plan

/** Section 5.19: one weapon's saved level and fitted attachments. */
data class SaveWeaponState(val weaponId: String, val level: Int, val attachments: List<String> = emptyList())

/**
 * Section 5.19: the persisted profile. Combat-transient facts (current health,
 * reload timer) are deliberately absent, so a save taken during death can only
 * record a consistent state.
 */
data class SaveProfile(
    val rank: Int = 1,
    val xp: Int = 0,
    val wallet: Wallet = Wallet(),
    val unlockedItemIds: Set<String> = emptySet(),
    val weaponStates: List<SaveWeaponState> = emptyList(),
    val missionStars: Map<String, Int> = emptyMap(),
    val settings: Map<String, String> = emptyMap(),
    val storyChoices: Map<String, String> = emptyMap(),
    val medals: Set<String> = emptySet()
) {
    fun toDocument(): SaveDocument = SaveDocument()
        .set(SaveKeys.RANK, rank)
        .set(SaveKeys.XP, xp)
        .set(SaveKeys.CASH, wallet.cash)
        .set(SaveKeys.STEEL, wallet.steel)
        .set(SaveKeys.PARTS, wallet.parts)
        .set(SaveKeys.MEDALS, SaveCodec.join(medals))
        .set(SaveKeys.UNLOCKED, SaveCodec.join(unlockedItemIds))
        .set(SaveKeys.WEAPONS, SaveCodec.joinWeapons(weaponStates))
        .set(SaveKeys.MISSION_STARS, SaveCodec.joinMap(missionStars.mapValues { it.value.toString() }))
        .set(SaveKeys.SETTINGS, SaveCodec.joinMap(settings))
        .set(SaveKeys.STORY_CHOICES, SaveCodec.joinMap(storyChoices))

    companion object {
        fun fromDocument(document: SaveDocument): SaveProfile = SaveProfile(
            rank = document.int(SaveKeys.RANK, 1),
            xp = document.int(SaveKeys.XP, 0),
            wallet = Wallet(
                cash = document.int(SaveKeys.CASH),
                steel = document.int(SaveKeys.STEEL),
                parts = document.int(SaveKeys.PARTS),
                medals = 0
            ),
            unlockedItemIds = SaveCodec.set(document.value(SaveKeys.UNLOCKED)),
            weaponStates = SaveCodec.weapons(document.value(SaveKeys.WEAPONS)),
            missionStars = SaveCodec.map(document.value(SaveKeys.MISSION_STARS)).mapValues {
                it.value.toIntOrNull() ?: 0
            },
            settings = SaveCodec.map(document.value(SaveKeys.SETTINGS)),
            storyChoices = SaveCodec.map(document.value(SaveKeys.STORY_CHOICES)),
            medals = SaveCodec.set(document.value(SaveKeys.MEDALS))
        )
    }
}

/** Section 5.19: one sequential step of the migration chain. */
data class SaveMigration(val fromVersion: Int, val description: String, val addedDefaults: Map<String, String>)

/**
 * Section 5.19: "if saveVersion < current, run sequential migrations (v1->v2->...).
 * Never delete unknown fields." The chain only ever adds, so a key written by a
 * newer build survives an older one.
 */
object SaveMigrations {

    const val CURRENT_VERSION = 3

    val STEPS: List<SaveMigration> = listOf(
        SaveMigration(
            1, "Adds profile.prestige for the New Game+ counter",
            mapOf(SaveKeys.PRESTIGE to "0")
        ),
        SaveMigration(
            2, "Adds story.meters for the three alignment meters",
            mapOf(SaveKeys.METERS to "0,0,0")
        )
    )

    fun stepFrom(version: Int): SaveMigration? = STEPS.firstOrNull { it.fromVersion == version }

    fun chain(document: SaveDocument, target: Int = CURRENT_VERSION): SaveDocument {
        var version = document.version() ?: return document
        var current = document
        while (version < target) {
            val step = stepFrom(version) ?: break
            step.addedDefaults.forEach { (key, value) ->
                if (current.value(key) == null) current = current.set(key, value)
            }
            version += 1
            current = current.set(SaveKeys.SAVE_VERSION, version)
        }
        return current
    }

    fun needsMigration(document: SaveDocument, target: Int = CURRENT_VERSION): Boolean =
        (document.version() ?: target) < target
}

/** Section 5.19: the plain file operations the atomic write is built from. */
interface SaveStore {
    fun write(name: String, text: String): Boolean
    fun read(name: String): String?
    fun exists(name: String): Boolean
    fun delete(name: String): Boolean
}

/** A deterministic store so the write/verify/replace sequence can be tested. */
class InMemorySaveStore(initial: Map<String, String> = emptyMap()) : SaveStore {

    private val files = initial.toMutableMap()
    private val failingWrites = mutableSetOf<String>()

    var writes: Int = 0
        private set

    fun failWritesOf(name: String) {
        failingWrites += name
    }

    override fun write(name: String, text: String): Boolean {
        if (name in failingWrites) return false
        writes += 1
        files[name] = text
        return true
    }

    override fun read(name: String): String? = files[name]

    override fun exists(name: String): Boolean = files.containsKey(name)

    override fun delete(name: String): Boolean = files.remove(name) != null

    fun raw(name: String): String? = files[name]

    fun names(): Set<String> = files.keys.toSet()
}

/** Section 5.19: which file the loaded state came from. */
enum class LoadSource(val displayName: String) {
    PRIMARY("save.json"),
    BACKUP("save.bak"),
    NEW_PROFILE("new profile")
}

/** Section 5.19: the five moments that autosave. */
enum class AutosaveTrigger(val displayName: String) {
    MISSION_COMPLETE("Mission complete"),
    PURCHASE("Purchase"),
    RANK_UP("Rank-up"),
    SETTINGS_CHANGE("Settings change"),
    APP_PAUSE_OR_QUIT("App pause/quit")
}

data class SaveOutcome(
    val saved: Boolean,
    val wroteTemporary: Boolean,
    val verified: Boolean,
    val replacedPrimary: Boolean,
    val keptBackup: Boolean,
    val reason: String? = null
)

data class LoadOutcome(
    val profile: SaveProfile,
    val document: SaveDocument,
    val source: LoadSource,
    val migrated: Boolean = false,
    val newerThanBuild: Boolean = false
)

/**
 * Section 5.19: write `save.tmp`, re-read and parse it, replace `save.json` and
 * keep the previous good file as `save.bak`. Loading falls back primary -> backup
 * -> new profile, and never throws.
 */
class SaveSystem(private val store: SaveStore, private val currentVersion: Int = SaveMigrations.CURRENT_VERSION) {

    fun save(profile: SaveProfile, lastSaved: String = "", log: DebugLog? = null): SaveOutcome {
        val document = profile.toDocument()
            .set(SaveKeys.SAVE_VERSION, currentVersion)
            .set(SaveKeys.LAST_SAVED, lastSaved)
        val text = document.encode()

        if (!store.write(SaveFiles.TMP, text)) {
            log?.record(LogCategory.SAVE, "Temporary write failed")
            return SaveOutcome(false, false, false, false, false, "Temporary write failed")
        }
        val reread = store.read(SaveFiles.TMP)
        if (reread == null || SaveDocument.decode(reread) != document) {
            store.delete(SaveFiles.TMP)
            log?.record(LogCategory.SAVE, "Verification failed, save.json untouched")
            return SaveOutcome(false, true, false, false, false, "Verification failed")
        }

        val previous = store.read(SaveFiles.SAVE)
        val keptBackup = previous != null && store.write(SaveFiles.BACKUP, previous)

        if (!store.write(SaveFiles.SAVE, text)) {
            log?.record(LogCategory.SAVE, "Primary write failed")
            return SaveOutcome(false, true, true, false, keptBackup, "Primary write failed")
        }
        store.delete(SaveFiles.TMP)
        return SaveOutcome(true, true, true, true, keptBackup)
    }

    fun load(log: DebugLog? = null): LoadOutcome {
        SaveDocument.decode(store.read(SaveFiles.SAVE).orEmpty())?.let { primary ->
            return accepted(primary, LoadSource.PRIMARY)
        }
        log?.record(LogCategory.SAVE, "save.json invalid, trying backup")
        SaveDocument.decode(store.read(SaveFiles.BACKUP).orEmpty())?.let { backup ->
            return accepted(backup, LoadSource.BACKUP)
        }
        log?.record(LogCategory.SAVE, "No valid file, starting a new profile")
        return LoadOutcome(SaveProfile(), freshDocument(), LoadSource.NEW_PROFILE)
    }

    private fun accepted(document: SaveDocument, source: LoadSource): LoadOutcome {
        val migrated = SaveMigrations.chain(document, currentVersion)
        return LoadOutcome(
            profile = SaveProfile.fromDocument(migrated),
            document = migrated,
            source = source,
            migrated = migrated != document,
            newerThanBuild = (document.version() ?: 0) > currentVersion
        )
    }

    private fun freshDocument(): SaveDocument =
        SaveProfile().toDocument().set(SaveKeys.SAVE_VERSION, currentVersion)

    /** Section 5.19: the mid-mission checkpoint lives in its own file. */
    fun saveCheckpoint(checkpoint: Checkpoint): Boolean {
        val document = SaveDocument()
            .set(SaveKeys.SAVE_VERSION, currentVersion)
            .set(CHECKPOINT_OBJECTIVE, checkpoint.objectiveId)
            .set(CHECKPOINT_HEALTH, checkpoint.playerHealth.toString())
            .set(CHECKPOINT_AMMO, checkpoint.playerAmmoTotal)
            .set(CHECKPOINT_COMPLETE, SaveCodec.join(checkpoint.completedObjectiveIds))
        return store.write(SaveFiles.CHECKPOINT, document.encode())
    }

    fun loadCheckpoint(): Checkpoint? {
        val document = SaveDocument.decode(store.read(SaveFiles.CHECKPOINT).orEmpty()) ?: return null
        val objectiveId = document.value(CHECKPOINT_OBJECTIVE) ?: return null
        return Checkpoint(
            objectiveId = objectiveId,
            playerHealth = document.float(CHECKPOINT_HEALTH),
            playerAmmoTotal = document.int(CHECKPOINT_AMMO),
            completedObjectiveIds = SaveCodec.list(document.value(CHECKPOINT_COMPLETE))
        )
    }

    /** "Cleared on mission end." */
    fun clearCheckpoint(): Boolean = store.delete(SaveFiles.CHECKPOINT)

    fun autosavesOn(trigger: AutosaveTrigger): Boolean = trigger in AutosaveTrigger.entries

    companion object {
        const val CLOUD_SAVE_SUPPORTED = false
        const val SAVE_EXCLUDES_TRANSIENT_COMBAT_STATE = true

        private const val CHECKPOINT_OBJECTIVE = "checkpoint.objectiveId"
        private const val CHECKPOINT_HEALTH = "checkpoint.health"
        private const val CHECKPOINT_AMMO = "checkpoint.ammoTotal"
        private const val CHECKPOINT_COMPLETE = "checkpoint.completed"
    }
}
