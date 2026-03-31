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
        val current = loadPhrases(skill).toMutableList()
        if (phrase.isNotBlank() && !current.contains(phrase.trim())) {
            current.add(phrase.trim())
            prefs.edit().putString(key(skill), current.joinToString("\n")).apply()
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
