package com.melof.activelisteningtrainer.data

import android.content.Context

/**
 * ユーザーが「この表現もOK」と登録したフレーズを SharedPreferences に保存する。
 * ActiveSkill ごとに改行区切りで保存する。
 */
class UserDictionaryStore(context: Context) {

    private val prefs = context.getSharedPreferences("user_dict", Context.MODE_PRIVATE)

    private fun key(skill: ActiveSkill) = skill.name

    fun loadPhrases(skill: ActiveSkill): List<String> {
        val raw = prefs.getString(key(skill), "") ?: ""
        return raw.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
    }

    fun addPhrase(skill: ActiveSkill, phrase: String) {
        val trimmed = phrase.trim()
        if (!isValidPhrase(trimmed)) return
        val current = loadPhrases(skill).toMutableList()
        if (!current.contains(trimmed)) {
            current.add(trimmed)
            prefs.edit().putString(key(skill), current.joinToString("\n")).apply()
        }
    }

    companion object {
        // 短すぎ（助詞・相づちのみ）や長すぎ（全文登録）を弾く
        private const val MIN_PHRASE_LEN = 6
        private const val MAX_PHRASE_LEN = 24

        // 助詞・相づち・疑問詞だけのフレーズを拒否するパターン
        private val INVALID_PATTERN = Regex(
            """^(ね|よ|か|は|が|を|に|で|と|も|や|から|まで|より|ので|けど|だけ|うん|はい|ええ|なぜ|なんで|どうして|どうぞ)+$"""
        )

        fun isValidPhrase(phrase: String): Boolean {
            if (phrase.length < MIN_PHRASE_LEN || phrase.length > MAX_PHRASE_LEN) return false
            if (INVALID_PATTERN.matches(phrase)) return false
            return true
        }

        fun validationErrorMessage(phrase: String): String? {
            val trimmed = phrase.trim()
            return when {
                trimmed.length < MIN_PHRASE_LEN -> "${MIN_PHRASE_LEN}文字以上で入力してください"
                trimmed.length > MAX_PHRASE_LEN -> "${MAX_PHRASE_LEN}文字以内で入力してください"
                INVALID_PATTERN.matches(trimmed) -> "助詞・相づちだけのフレーズは登録できません"
                else                             -> null
            }
        }
    }

    fun removePhrase(skill: ActiveSkill, phrase: String) {
        val current = loadPhrases(skill).toMutableList()
        current.remove(phrase.trim())
        prefs.edit().putString(key(skill), current.joinToString("\n")).apply()
    }

    fun loadAll(): Map<ActiveSkill, List<String>> =
        ActiveSkill.entries.associateWith { loadPhrases(it) }
}
