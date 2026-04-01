package com.melof.activelisteningtrainer.data

import android.content.Context

class ApiKeyStore(context: Context) {
    private val prefs = context.getSharedPreferences("api_keys", Context.MODE_PRIVATE)

    fun save(key: String) = prefs.edit().putString(KEY, key.trim()).apply()

    fun load(): String = prefs.getString(KEY, "") ?: ""

    fun hasKey(): Boolean = load().isNotBlank()

    companion object {
        private const val KEY = "claude_api_key"
    }
}
