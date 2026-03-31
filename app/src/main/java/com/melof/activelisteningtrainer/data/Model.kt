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
enum class Difficulty(val label: String, val stars: String, val levelRange: IntRange) {
    BEGINNER(    "初級",         "☆",      1..2),
    INTERMEDIATE("中級",         "☆☆",     3..4),
    TRAP(        "崩しトラップ", "☆☆☆",    5..6),
    EXPERT(      "エキスパート", "☆☆☆☆",  7..9),
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

// ═══════════════════════════════════════════════════════════════════════════════
// 依存・不安型ケース モデル
// ═══════════════════════════════════════════════════════════════════════════════

// ─── 巻き込まれNGパターン ────────────────────────────────────────────────────────
enum class DependencyNGPattern(val label: String, val involvementScore: Int) {
    DEPENDENCY_REINFORCEMENT("依存強化",       +20),
    ROLE_BLURRING(           "役割混乱",       +15),
    REASSURANCE_OVERUSE(     "過剰保証",       +15),
    EXCLUSIVE_BONDING(       "特別関係強調",   +10),
    RESCUE_BEHAVIOR(         "救出行動",       +10),
    EMOTIONAL_ABSORPTION(    "感情の巻き込まれ", +10),
    INCONSISTENCY(           "言動不一致",      +5),
    ARGUMENT(                "議論化",          +5),
}

// ─── 境界維持OKパターン ──────────────────────────────────────────────────────────
enum class DependencyOKPattern(val label: String, val involvementScore: Int) {
    BOUNDARY_MAINTENANCE("境界維持",     -15),
    ROLE_CLARITY(        "役割明確化",   -10),
    CONSISTENCY(         "一貫性維持",   -10),
    DE_ESCALATION(       "緩和",         -10),
    EMOTIONAL_REFLECTION("感情反映",      -5),
    NEUTRALITY(          "中立的立場",    -5),
}

// ─── 依存フェーズ ────────────────────────────────────────────────────────────────
enum class DependencyPhase(val label: String, val description: String) {
    ANXIETY_ESCALATION(    "不安増大期",         "相手の不安が大きくなり、あなたを頼り始める段階"),
    EXCESSIVE_CONTACT(     "接触過多期",         "連絡頻度が増し、境界が曖昧になってくる段階"),
    PSYCHOLOGICAL_CONTROL( "心理的コントロール期", "罪悪感や特別感を使って行動を縛ろうとする段階"),
    SPLITTING_ATTACK(      "分裂・攻撃期",       "急な怒りや脅しで関係を揺さぶってくる段階"),
}

// ─── 依存・不安型シナリオ ─────────────────────────────────────────────────────────
data class DependencyScenario(
    val id: String,
    val phase: DependencyPhase,
    val difficulty: Difficulty = Difficulty.BEGINNER,
    val title: String,
    val situation: String,
    val utterance: String,
    val hint: String = "",
    val ngPatterns: List<DependencyNGPattern>,
    val okPatterns: List<DependencyOKPattern>,
    val ngKeywords: Map<DependencyNGPattern, List<String>>,
    val okKeywords: Map<DependencyOKPattern, List<String>>,
    val sampleResponse: String = "",
    val feedbackNG: String = "優しさで巻き込まれました",
    val feedbackOK: String = "境界を保てています",
)

// ─── 依存採点結果 ────────────────────────────────────────────────────────────────
data class NGPatternResult(
    val pattern: DependencyNGPattern,
    val triggered: Boolean,
    val triggeredWords: List<String> = emptyList(),
)

data class OKPatternResult(
    val pattern: DependencyOKPattern,
    val achieved: Boolean,
    val matchedWords: List<String> = emptyList(),
)

data class InvolvementResult(
    val input: String,
    val scenario: DependencyScenario,
    val ngResults: List<NGPatternResult>,
    val okResults: List<OKPatternResult>,
) {
    val rawScore: Int
        get() {
            val ng = ngResults.filter { it.triggered }.sumOf { it.pattern.involvementScore }
            val ok = okResults.filter { it.achieved }.sumOf { it.pattern.involvementScore }
            return (ng + ok).coerceIn(0, 100)
        }

    val involvementLevel: InvolvementLevel
        get() = when {
            rawScore >= 70 -> InvolvementLevel.DANGER
            rawScore >= 40 -> InvolvementLevel.WARNING
            rawScore >= 20 -> InvolvementLevel.CAUTION
            else           -> InvolvementLevel.SAFE
        }

    val feedbackMessage: String
        get() = when (involvementLevel) {
            InvolvementLevel.DANGER  -> "あなたは今、優しさで完全に巻き込まれました"
            InvolvementLevel.WARNING -> "あなたは今、優しさで巻き込まれました"
            InvolvementLevel.CAUTION -> "巻き込まれかけています。境界を意識しましょう"
            InvolvementLevel.SAFE    -> "境界を保てています。よい対応でした"
        }
}

enum class InvolvementLevel(val label: String) {
    SAFE("安全"), CAUTION("やや注意"), WARNING("注意"), DANGER("危険")
}
