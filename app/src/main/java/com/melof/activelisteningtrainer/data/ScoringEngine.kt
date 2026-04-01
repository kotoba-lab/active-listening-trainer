package com.melof.activelisteningtrainer.data

import java.text.Normalizer

// ─── マッチルール ─────────────────────────────────────────────────────────────────

enum class MatchMode {
    /** テキスト中に部分一致（デフォルト） */
    SUBSTRING,
    /**
     * 節境界を考慮したマッチ。
     * フレーズの前後が文頭/文末/句読点/空白であることを要求する。
     * 短い単語の誤検知を抑制する。
     */
    CLAUSE,
}

data class MatchRule(
    val phrase: String,
    val minLength: Int = 4,
    val mode: MatchMode = MatchMode.SUBSTRING,
)

/** NFKC正規化 + 空白統一 */
private fun normalizeText(text: String): String =
    Normalizer.normalize(text, Normalizer.Form.NFKC)
        .replace(Regex("\\s+"), " ")
        .replace("　", " ")
        .trim()

/** MatchRuleでマッチ判定 */
private fun matchesRule(normalizedText: String, rule: MatchRule): Boolean {
    if (rule.phrase.length < rule.minLength) return false
    val normalizedPhrase = normalizeText(rule.phrase)
    return when (rule.mode) {
        MatchMode.SUBSTRING -> normalizedText.contains(normalizedPhrase)
        MatchMode.CLAUSE    -> Regex(
            """(^|[、。,.\s])${Regex.escape(normalizedPhrase)}([、。,.\s]|$)"""
        ).containsMatchIn(normalizedText)
    }
}

/** String を MatchRule に変換するユーティリティ。
 *  4文字未満の語は CLAUSE モードに昇格させ節境界を要求する。 */
private fun String.toRule(): MatchRule {
    val minLen = 4
    return if (this.length < minLen) {
        MatchRule(this, minLength = 1, mode = MatchMode.CLAUSE)
    } else {
        MatchRule(this, minLength = minLen, mode = MatchMode.SUBSTRING)
    }
}

// ─── 採点エンジン ────────────────────────────────────────────────────────────────

object ScoringEngine {

    /** 各 ActiveSkill に対応する共通辞書キーワード */
    private val skillRules: Map<ActiveSkill, List<MatchRule>> = mapOf(
        ActiveSkill.EMOTIONAL_REFLECTION to SynonymDictionary.emotionWords.map { it.toRule() },
        ActiveSkill.ACCEPTANCE           to SynonymDictionary.acceptancePhrases.map { it.toRule() },
        ActiveSkill.PROMPT               to SynonymDictionary.promptPhrases.map { it.toRule() },
        ActiveSkill.LIGHT_FOCUS          to SynonymDictionary.lightFocusPhrases.map { it.toRule() },
        ActiveSkill.SUMMARY_LIKE         to SynonymDictionary.summaryPhrases.map { it.toRule() },
        ActiveSkill.SAFE_PACING          to SynonymDictionary.safePacingPhrases.map { it.toRule() },
    )

    /** 各 PenaltyType に対応する共通辞書キーワード */
    private val penaltyRules: Map<PenaltyType, List<MatchRule>> = mapOf(
        PenaltyType.ADVICE              to SynonymDictionary.adviceWords.map { it.toRule() },
        PenaltyType.JUDGMENT            to SynonymDictionary.judgmentWords.map { it.toRule() },
        PenaltyType.MINIMIZATION        to SynonymDictionary.minimizationWords.map { it.toRule() },
        PenaltyType.SELF_TALK           to SynonymDictionary.selfTalkWords.map { it.toRule() },
        PenaltyType.EARLY_CLARIFICATION to SynonymDictionary.earlyClarificationWords.map { it.toRule() },
        PenaltyType.INTERROGATION       to SynonymDictionary.interrogationWords.map { it.toRule() },
        PenaltyType.PREMATURE_REFRAME   to SynonymDictionary.prematureReframeWords.map { it.toRule() },
        PenaltyType.JOIN_ATTACK         to SynonymDictionary.joinAttackWords.map { it.toRule() },
    )

    /**
     * ユーザー入力を採点して ScoreResult を返す。
     *
     * @param input       ユーザーが入力したテキスト
     * @param scenario    採点対象シナリオ（採点ルールを参照）
     * @param userPhrases ユーザーが登録したカスタムフレーズ（スキルごと）
     */
    fun evaluate(
        input: String,
        scenario: Scenario,
        userPhrases: Map<ActiveSkill, List<String>> = emptyMap(),
    ): ScoreResult {
        val text    = normalizeText(input)
        val scoring = scenario.freeResponseScoring

        // シナリオ固有キーワードは第1必須スロット専用とする。
        // 全スロットへ適用すると、1語一致で全スキル達成になる誤爆を防ぐ。
        val primarySkill       = scoring.targetSlots.firstOrNull()
        val scenarioPosRules   = scoring.positiveKeywords.map { it.toRule() }
        val scenarioNegRules   = scoring.negativeKeywords.map { it.toRule() }

        // 検査対象スロット = target + optional
        val allSkills = (scoring.targetSlots + scoring.optionalSlots).distinct()

        val slotResults = allSkills.map { skill ->
            val builtIn = skillRules[skill] ?: emptyList()
            val custom  = (userPhrases[skill] ?: emptyList()).map { it.toRule() }
            // シナリオ固有加点キーワードは第1必須スロットのみに加算
            val scenarioBonus = if (skill == primarySkill) scenarioPosRules else emptyList()

            val allRules  = builtIn + custom + scenarioBonus
            val matched   = allRules.filter { matchesRule(text, it) }.map { it.phrase }
            SlotResult(skill = skill, achieved = matched.isNotEmpty(), matchedKeywords = matched)
        }

        // penalty_focus に限らず全ペナルティを検査する
        val penaltyResults = PenaltyType.entries.map { penalty ->
            val builtIn   = penaltyRules[penalty] ?: emptyList()
            // シナリオ固有ネガティブキーワードはペナルティ全体に適用（禁止語として機能）
            val allRules  = builtIn + scenarioNegRules
            val triggered = allRules.filter { matchesRule(text, it) }.map { it.phrase }
            PenaltyResult(penalty = penalty, triggered = triggered.isNotEmpty(), triggeredWords = triggered)
        }

        return ScoreResult(
            input          = input,
            slotResults    = slotResults,
            penaltyResults = penaltyResults,
            scenario       = scenario,
        )
    }
}
