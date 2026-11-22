package com.example.game.valor.plan

import com.example.game.vanguard.CaseRecorder
import com.example.game.vanguard.ContractCase
import com.example.game.vanguard.observed

/** Section 5.19: the save file, its atomic write, its migrations and checkpoints. */
object PlanSaveCases {

    fun run(): List<ContractCase> {
        val r = CaseRecorder(ValorPlanAudit.GROUP_SAVE)
        codecCases(r)
        writeCases(r)
        loadCases(r)
        checkpointCases(r)
        return r.result()
    }

    private fun codecCases(r: CaseRecorder) {
        val profile = SaveProfile(rank = 9, xp = 1_234, wallet = Wallet(cash = 50))
        val document = profile.toDocument().set(SaveKeys.SAVE_VERSION, SaveMigrations.CURRENT_VERSION)
        r.check(
            "Versioned JSON round-trip", "a profile survives encode and decode unchanged",
            SaveDocument.decode(document.encode()) == document &&
                SaveProfile.fromDocument(document).rank == 9 &&
                SaveProfile.fromDocument(document).wallet.cash == 50,
            observed(SaveProfile.fromDocument(document).rank, 9)
        )
        r.check(
            "Corruption is a value", "an unparsable or unversioned file decodes to null, never throws",
            SaveDocument.decode("{") == null &&
                SaveDocument.decode("") == null &&
                SaveDocument.decode("{\"profile.rank\":\"9\"}") == null,
            observed(SaveDocument.decode("{\"profile.rank\":\"9\"}"), null)
        )
        r.check(
            "Transient state excluded", "no half-dead health is ever written",
            SaveSystem.SAVE_EXCLUDES_TRANSIENT_COMBAT_STATE &&
                !SaveSystem.CLOUD_SAVE_SUPPORTED &&
                document.keys().none { it.contains("health") || it.contains("reload") },
            observed(document.keys().size, "profile fields only")
        )
    }

    private fun writeCases(r: CaseRecorder) {
        val store = InMemorySaveStore()
        val system = SaveSystem(store)
        val first = system.save(SaveProfile(rank = 2), "2026-10-31T10:00")
        r.check(
            "Atomic write", "tmp then re-read and verify, then replace save.json",
            first.saved && first.wroteTemporary && first.verified && first.replacedPrimary &&
                !first.keptBackup && store.raw(SaveFiles.TMP) == null && store.exists(SaveFiles.SAVE),
            observed(first.reason, null)
        )
        val second = system.save(SaveProfile(rank = 3))
        val backup = store.raw(SaveFiles.BACKUP)
        r.check(
            "Previous good file kept", "the second save leaves the first one as save.bak",
            second.keptBackup && backup != null &&
                SaveDocument.decode(backup)?.int(SaveKeys.RANK) == 2,
            observed(second.keptBackup, true)
        )
        val before = store.raw(SaveFiles.SAVE)
        store.failWritesOf(SaveFiles.TMP)
        val refused = system.save(SaveProfile(rank = 4))
        r.check(
            "Failed write changes nothing", "a refused tmp write never touches save.json",
            !refused.saved && !refused.wroteTemporary && store.raw(SaveFiles.SAVE) == before,
            observed(refused.reason, "Temporary write failed")
        )
        r.check(
            "Five autosave triggers", "mission end, purchase, rank-up, settings and pause",
            AutosaveTrigger.entries.size == 5 &&
                AutosaveTrigger.entries.all { system.autosavesOn(it) } &&
                AutosaveTrigger.APP_PAUSE_OR_QUIT in AutosaveTrigger.entries,
            observed(AutosaveTrigger.entries.size, 5)
        )
    }

    private fun loadCases(r: CaseRecorder) {
        val good = SaveProfile(rank = 9).toDocument().set(SaveKeys.SAVE_VERSION, 3)
        val broken = InMemorySaveStore(
            mapOf(SaveFiles.SAVE to "not a save", SaveFiles.BACKUP to good.encode())
        )
        val recovered = SaveSystem(broken).load()
        r.check(
            "Backup fallback", "a corrupt primary falls back to save.bak",
            recovered.source == LoadSource.BACKUP && recovered.profile.rank == 9,
            observed(recovered.source, LoadSource.BACKUP)
        )
        val fresh = SaveSystem(InMemorySaveStore()).load()
        r.check(
            "Fresh profile", "with no valid file the player starts at rank 1",
            fresh.source == LoadSource.NEW_PROFILE && fresh.profile.rank == 1 &&
                fresh.document.version() == SaveMigrations.CURRENT_VERSION,
            observed(fresh.source, LoadSource.NEW_PROFILE)
        )
        val old = SaveDocument().set(SaveKeys.SAVE_VERSION, 1).set("custom.future", "keep")
        val migrated = SaveMigrations.chain(old)
        r.check(
            "Sequential migration", "v1 to v2 to v3 adds the new fields in order",
            migrated.version() == SaveMigrations.CURRENT_VERSION &&
                migrated.value(SaveKeys.PRESTIGE) == "0" &&
                migrated.value(SaveKeys.METERS) == "0,0,0" &&
                SaveMigrations.needsMigration(old) &&
                !SaveMigrations.needsMigration(migrated),
            observed(migrated.version(), SaveMigrations.CURRENT_VERSION)
        )
        r.check(
            "Unknown fields survive", "a field a newer build wrote is never deleted",
            migrated.keys().containsAll(old.keys()) && migrated.value("custom.future") == "keep",
            observed(migrated.keys().size, 4)
        )
        val loadedOld = SaveSystem(InMemorySaveStore(mapOf(SaveFiles.SAVE to old.encode()))).load()
        r.check(
            "Load migrates", "an old file is upgraded during load and reported as migrated",
            loadedOld.migrated && loadedOld.source == LoadSource.PRIMARY &&
                loadedOld.document.version() == SaveMigrations.CURRENT_VERSION,
            observed(loadedOld.migrated, true)
        )
    }

    private fun checkpointCases(r: CaseRecorder) {
        val files = InMemorySaveStore()
        val system = SaveSystem(files)
        val checkpoint = Checkpoint("obj2", 42.5f, 90, listOf("obj1"))
        val stored = system.saveCheckpoint(checkpoint)
        val restored = system.loadCheckpoint()
        val cleared = system.clearCheckpoint()
        r.check(
            "Checkpoint file", "mid-mission state round-trips and is cleared on mission end",
            stored && restored == checkpoint && cleared &&
                !files.exists(SaveFiles.CHECKPOINT) && system.loadCheckpoint() == null,
            observed(restored?.objectiveId, "obj2")
        )
    }
}
