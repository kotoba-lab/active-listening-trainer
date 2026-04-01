package com.melof.activelisteningtrainer.data

import android.content.Context

/**
 * 練習履歴を SharedPreferences にパイプ区切り文字列で保存する。
 * 最大 MAX_ENTRIES 件。超えた場合は古い順に削除。
 */
class PracticeLogStore(context: Context) {

    private val prefs = context.getSharedPreferences("practice_log", Context.MODE_PRIVATE)
    private val KEY = "entries"

    companion object {
        private const val MAX_ENTRIES = 200
        private const val SEP = "|"       // フィールド区切り
        private const val ROW_SEP = "\n"  // レコード区切り
    }

    fun saveEntry(entry: PracticeLogEntry) {
        val current = loadAll().toMutableList()
        current.add(0, entry)  // 先頭に追加（新しい順）
        val trimmed = if (current.size > MAX_ENTRIES) current.take(MAX_ENTRIES) else current
        prefs.edit().putString(KEY, trimmed.joinToString(ROW_SEP) { it.serialize() }).apply()
    }

    fun loadAll(): List<PracticeLogEntry> {
        val raw = prefs.getString(KEY, "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.split(ROW_SEP)
            .filter { it.isNotBlank() }
            .mapNotNull { PracticeLogEntry.deserialize(it) }
    }

    fun clearAll() {
        prefs.edit().remove(KEY).apply()
    }
}

data class PracticeLogEntry(
    val timestampMs: Long,
    val scenarioTitle: String,
    val playMode: String,       // "CHOICE" | "GUIDED" | "FREE" | "DEP"
    val totalScore: Int,
    val targetAchieved: Int,
    val targetTotal: Int,
    val cleared: Boolean,
) {
    fun serialize(): String = listOf(
        timestampMs,
        scenarioTitle.replace("|", "｜"),  // パイプ文字エスケープ
        playMode,
        totalScore,
        targetAchieved,
        targetTotal,
        if (cleared) "1" else "0",
    ).joinToString("|")

    companion object {
        fun deserialize(raw: String): PracticeLogEntry? {
            val parts = raw.split("|")
            if (parts.size < 7) return null
            return runCatching {
                PracticeLogEntry(
                    timestampMs    = parts[0].toLong(),
                    scenarioTitle  = parts[1],
                    playMode       = parts[2],
                    totalScore     = parts[3].toInt(),
                    targetAchieved = parts[4].toInt(),
                    targetTotal    = parts[5].toInt(),
                    cleared        = parts[6] == "1",
                )
            }.getOrNull()
        }
    }
}
