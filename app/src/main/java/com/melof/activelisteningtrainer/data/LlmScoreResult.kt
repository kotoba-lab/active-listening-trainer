package com.melof.activelisteningtrainer.data

data class LlmScoreResult(
    val score: Int,                        // 0-100
    val involvement: Int,                  // 0-100（DEP用、ALは0）
    val feedback: String,                  // 改善点（1-2文）
    val advice: String,                    // 次回意識するポイント
    val slots: Map<String, Boolean>,       // スキルスロット達成状態
    val penalties: List<LlmPenalty>,       // 検出されたペナルティ
)

data class LlmPenalty(
    val type: String,   // ペナルティ種別（英語キー）
    val reason: String, // 検出理由
)
