package com.example.game.valor.plan

/**
 * Section 5.19: the save file is a flat JSON object of string values. Keeping it
 * flat is what lets a migration add a field without dropping the ones a newer
 * build wrote, and every value round-trips without a schema.
 */
data class SaveDocument(val fields: Map<String, String> = emptyMap()) {

    fun value(key: String): String? = fields[key]

    fun int(key: String, fallback: Int = 0): Int = fields[key]?.toIntOrNull() ?: fallback

    fun float(key: String, fallback: Float = 0f): Float = fields[key]?.toFloatOrNull() ?: fallback

    fun set(key: String, value: String): SaveDocument = copy(fields = fields + (key to value))

    fun set(key: String, value: Int): SaveDocument = set(key, value.toString())

    fun without(key: String): SaveDocument = copy(fields = fields - key)

    fun keys(): Set<String> = fields.keys

    fun version(): Int? = fields[SaveKeys.SAVE_VERSION]?.toIntOrNull()

    fun encode(): String =
        fields.entries.joinToString(",", prefix = "{", postfix = "}") { (key, value) ->
            "\"${escape(key)}\":\"${escape(value)}\""
        }

    companion object {

        private const val QUOTE = '"'
        private const val BACKSLASH = '\\'

        /**
         * Returns null for anything that is not a well-formed object, which is the
         * corruption signal the loader turns into a backup read.
         */
        fun decode(text: String): SaveDocument? {
            val trimmed = text.trim()
            if (trimmed.length < 2 || trimmed.first() != '{' || trimmed.last() != '}') return null
            val body = trimmed.substring(1, trimmed.length - 1)
            val parsed = LinkedHashMap<String, String>()
            var index = 0
            while (index < body.length) {
                while (index < body.length && (body[index].isWhitespace() || body[index] == ',')) index++
                if (index >= body.length) break
                if (body[index] != QUOTE) return null
                val keyEnd = stringEnd(body, index) ?: return null
                val key = unescape(body.substring(index + 1, keyEnd))
                index = keyEnd + 1
                while (index < body.length && body[index].isWhitespace()) index++
                if (index >= body.length || body[index] != ':') return null
                index++
                while (index < body.length && body[index].isWhitespace()) index++
                if (index >= body.length) return null
                if (body[index] == QUOTE) {
                    val valueEnd = stringEnd(body, index) ?: return null
                    parsed[key] = unescape(body.substring(index + 1, valueEnd))
                    index = valueEnd + 1
                } else {
                    val start = index
                    while (index < body.length && body[index] != ',') index++
                    val raw = body.substring(start, index).trim()
                    if (raw.isEmpty() || raw.toDoubleOrNull() == null) return null
                    parsed[key] = raw
                }
            }
            if (parsed[SaveKeys.SAVE_VERSION]?.toIntOrNull() == null) return null
            return SaveDocument(parsed)
        }

        private fun stringEnd(body: String, quoteIndex: Int): Int? {
            var cursor = quoteIndex + 1
            while (cursor < body.length) {
                when (body[cursor]) {
                    BACKSLASH -> cursor += 2
                    QUOTE -> return cursor
                    else -> cursor++
                }
            }
            return null
        }

        private fun escape(raw: String): String =
            raw.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")

        private fun unescape(raw: String): String =
            raw.replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\")
    }
}

/** Section 5.19: the field names of the save file, in one place. */
object SaveKeys {
    const val SAVE_VERSION = "saveVersion"
    const val LAST_SAVED = "lastSaved"
    const val RANK = "profile.rank"
    const val XP = "profile.xp"
    const val CASH = "profile.cash"
    const val STEEL = "profile.steel"
    const val PARTS = "profile.parts"
    const val MEDALS = "profile.medals"
    const val UNLOCKED = "profile.unlocked"
    const val WEAPONS = "profile.weapons"
    const val MISSION_STARS = "profile.missionStars"
    const val SETTINGS = "profile.settings"
    const val STORY_CHOICES = "profile.storyChoices"
    const val METERS = "story.meters"
    const val PRESTIGE = "profile.prestige"
}

/** Section 5.19: the four files the save system touches, and nothing else. */
object SaveFiles {
    const val SAVE = "save.json"
    const val TMP = "save.tmp"
    const val BACKUP = "save.bak"
    const val CHECKPOINT = "checkpoint.json"

    val ALL: List<String> = listOf(SAVE, TMP, BACKUP, CHECKPOINT)
}

/** Section 5.19: a list field is comma separated, a record field semicolon separated. */
object SaveCodec {

    private const val ITEM = ","
    private const val RECORD = ";"
    private const val FIELD = ":"
    private const val WEAPON_PART = "|"

    fun list(raw: String?): List<String> =
        raw.orEmpty().split(ITEM).map { it.trim() }.filter { it.isNotEmpty() }

    fun set(raw: String?): Set<String> = listOf(raw).flatMap(::list).toSet()

    fun join(values: Collection<String>): String = values.joinToString(ITEM)

    fun map(raw: String?): Map<String, String> =
        raw.orEmpty().split(RECORD)
            .mapNotNull { entry ->
                val split = entry.indexOf(FIELD)
                if (split <= 0) null else entry.substring(0, split).trim() to entry.substring(split + 1).trim()
            }
            .toMap()

    fun joinMap(values: Map<String, String>): String =
        values.entries.joinToString(RECORD) { "${it.key}$FIELD${it.value}" }

    fun weapon(state: SaveWeaponState): String =
        listOf(state.weaponId, state.level.toString(), join(state.attachments)).joinToString(WEAPON_PART)

    fun weaponRecord(raw: String): SaveWeaponState? {
        val parts = raw.split(WEAPON_PART)
        if (parts.size < 2 || parts[0].isBlank()) return null
        val level = parts[1].toIntOrNull() ?: return null
        val attachments = if (parts.size >= 3) list(parts[2]) else emptyList()
        return SaveWeaponState(parts[0].trim(), level, attachments)
    }

    fun weapons(raw: String?): List<SaveWeaponState> =
        raw.orEmpty().split(RECORD).mapNotNull { weaponRecord(it.trim()) }

    fun joinWeapons(states: List<SaveWeaponState>): String = states.joinToString(RECORD, transform = ::weapon)
}
