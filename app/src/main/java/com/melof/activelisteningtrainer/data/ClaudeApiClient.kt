package com.melof.activelisteningtrainer.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object ClaudeApiClient {

    private const val API_URL = "https://api.anthropic.com/v1/messages"
    private const val MODEL = "claude-haiku-4-5-20251001"
    private const val MAX_TOKENS = 600

    suspend fun score(prompt: String, apiKey: String): Result<LlmScoreResult> =
        withContext(Dispatchers.IO) {
            runCatching {
                val requestBody = buildRequestJson(prompt)

                val url = URL(API_URL)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("x-api-key", apiKey)
                conn.setRequestProperty("anthropic-version", "2023-06-01")
                conn.setRequestProperty("content-type", "application/json")
                conn.doOutput = true
                conn.connectTimeout = 15_000
                conn.readTimeout = 30_000

                conn.outputStream.use { it.write(requestBody.toByteArray(Charsets.UTF_8)) }

                val code = conn.responseCode
                val body = if (code == 200) {
                    conn.inputStream.bufferedReader(Charsets.UTF_8).readText()
                } else {
                    val errBody = conn.errorStream?.bufferedReader(Charsets.UTF_8)?.readText()
                        ?: "(no body)"
                    conn.disconnect()
                    throw Exception("HTTP $code: $errBody")
                }
                conn.disconnect()

                parseApiResponse(body)
            }
        }

    private fun buildRequestJson(prompt: String): String =
        JSONObject().apply {
            put("model", MODEL)
            put("max_tokens", MAX_TOKENS)
            put("messages", JSONArray().put(
                JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                }
            ))
        }.toString()

    private fun parseApiResponse(responseJson: String): LlmScoreResult {
        val root = JSONObject(responseJson)
        val textContent = root.getJSONArray("content")
            .getJSONObject(0)
            .getString("text")

        val scoringJson = extractJsonBlock(textContent)
        val obj = JSONObject(scoringJson)

        val slots = mutableMapOf<String, Boolean>()
        obj.optJSONObject("slots")?.let { slotsObj ->
            slotsObj.keys().forEach { key -> slots[key] = slotsObj.optBoolean(key, false) }
        }

        val penalties = mutableListOf<LlmPenalty>()
        obj.optJSONArray("penalties")?.let { arr ->
            for (i in 0 until arr.length()) {
                val p = arr.optJSONObject(i) ?: continue
                penalties.add(LlmPenalty(
                    type   = p.optString("type", ""),
                    reason = p.optString("reason", ""),
                ))
            }
        }

        return LlmScoreResult(
            score       = obj.optInt("score", 0),
            involvement = obj.optInt("involvement", 0),
            feedback    = obj.optString("feedback", ""),
            advice      = obj.optString("advice", ""),
            slots       = slots,
            penalties   = penalties,
        )
    }

    /** LLM 出力から ```json ... ``` ブロック、または { ... } を抽出する */
    private fun extractJsonBlock(text: String): String {
        val fenceStart = text.indexOf("```json")
        if (fenceStart >= 0) {
            val bodyStart = fenceStart + 7
            val fenceEnd = text.indexOf("```", bodyStart)
            if (fenceEnd > bodyStart) return text.substring(bodyStart, fenceEnd).trim()
        }
        val objStart = text.indexOf("{")
        val objEnd = text.lastIndexOf("}")
        if (objStart >= 0 && objEnd > objStart) return text.substring(objStart, objEnd + 1)
        return text.trim()
    }
}
