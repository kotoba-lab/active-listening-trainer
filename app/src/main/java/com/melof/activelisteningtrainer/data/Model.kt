package com.melof.activelisteningtrainer.data

// ─── 加点スロット ────────────────────────────────────────────────────────────────
enum class ActiveSkill(val label: String, val score: Int) {
    EMOTIONAL_REFLECTION("感情反映",        3),
    ACCEPTANCE(          "受け止め",        2),
    PROMPT(              "促し",            1),
    LIGHT_FOCUS(         "軽い焦点化",      1),
    SUMMARY_LIKE(        "要約的返し",      1),
    SAFE_PACING(         "安全なペーシング", 2),
}

// ─── 減点ペナルティ ──────────────────────────────────────────────────────────────
enum class PenaltyType(val label: String, val score: Int) {
    ADVICE(             "助言",                  -2),
    JUDGMENT(           "評価・判断",            -3),
    MINIMIZATION(       "矮小化",                -3),
    SELF_TALK(          "自分語り",              -3),
    EARLY_CLARIFICATION("早期明確化",            -2),
    INTERROGATION(      "尋問化",                -3),
    PREMATURE_REFRAME(  "早すぎるリフレーミング", -2),
    JOIN_ATTACK(        "一緒に攻撃",            -2),
}

// ─── シナリオ分類 ────────────────────────────────────────────────────────────────
enum class ScenarioCategory(val label: String) {
    VENTING(          "愚痴を聞く"),
    SADNESS(          "落ち込みを受け止める"),
    ANXIETY(          "不安をほどく"),
    SCATTERED(        "話が散らかった相手を整理する"),
    ANGER(            "怒りの裏の感情を拾う"),
    SHAME(            "恥や気まずさのある相談"),
    SELF_CAUSED(      "自業自得っぽい相談"),
    ADVICE_TRAP(      "助言したくなる相談"),
    SELF_TALK_TRAP(   "自分語りしたくなる相談"),
    EARLY_FOCUS_TRAP( "明確化したいが早すぎる場面"),
}

// ─── 難易度 ──────────────────────────────────────────────────────────────────────
enum class Difficulty(val label: String, val stars: String) {
    BEGINNER(    "初級",         "☆"),
    INTERMEDIATE("中級",         "☆☆"),
    TRAP(        "崩しトラップ", "☆☆☆"),
}

// ─── 選択肢（choice_mode） ────────────────────────────────────────────────────────
data class ChoiceOption(
    val text: String,
    val isCorrect: Boolean,
    val badType: String? = null,   // e.g. "bad_advice", "bad_judgment"
    val explanation: String,
)

// ─── 自由回答採点ルール ──────────────────────────────────────────────────────────
data class FreeResponseScoring(
    val targetSlots: List<ActiveSkill>,
    val optionalSlots: List<ActiveSkill>,
    val penaltyFocus: List<PenaltyType>,
    val positiveKeywords: List<String>,
    val negativeKeywords: List<String>,
    val customFeedback: Map<String, String> = emptyMap(),  // key = slot/penalty name
)

// ─── シナリオ ─────────────────────────────────────────────────────────────────────
data class Scenario(
    val id: String,               // "AL-001" 〜 "AL-030"
    val category: ScenarioCategory,
    val difficulty: Difficulty,
    val title: String,
    val situation: String,
    val utterance: String,        // 相手のセリフ
    val hint: String = "",
    val choiceOptions: List<ChoiceOption>,
    val freeResponseScoring: FreeResponseScoring,
    val sampleResponse: String = "",
)

// ─── 採点結果 ─────────────────────────────────────────────────────────────────────
data class SlotResult(
    val skill: ActiveSkill,
    val achieved: Boolean,
    val matchedKeywords: List<String> = emptyList(),
)

data class PenaltyResult(
    val penalty: PenaltyType,
    val triggered: Boolean,
    val triggeredWords: List<String> = emptyList(),
)

data class ScoreResult(
    val input: String,
    val slotResults: List<SlotResult>,
    val penaltyResults: List<PenaltyResult>,
    val scenario: Scenario,
) {
    val skillScore: Int
        get() = slotResults.filter { it.achieved }.sumOf { it.skill.score }

    val penaltyScore: Int
        get() = penaltyResults.filter { it.triggered }.sumOf { it.penalty.score }

    val totalScore: Int
        get() = (skillScore + penaltyScore).coerceAtLeast(0)

    val maxScore: Int
        get() = scenario.freeResponseScoring.targetSlots.sumOf { it.score }
}
