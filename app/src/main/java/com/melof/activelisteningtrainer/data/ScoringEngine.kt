package com.melof.activelisteningtrainer.data

object ScoringEngine {

    /** 各 ActiveSkill に対応する共通辞書キーワードリスト */
    private val skillKeywords: Map<ActiveSkill, List<String>> = mapOf(
        ActiveSkill.EMOTIONAL_REFLECTION to SynonymDictionary.emotionWords,
        ActiveSkill.ACCEPTANCE           to SynonymDictionary.acceptancePhrases,
        ActiveSkill.PROMPT               to SynonymDictionary.promptPhrases,
        ActiveSkill.LIGHT_FOCUS          to SynonymDictionary.lightFocusPhrases,
        ActiveSkill.SUMMARY_LIKE         to SynonymDictionary.summaryPhrases,
        ActiveSkill.SAFE_PACING          to SynonymDictionary.safePacingPhrases,
    )

    /** 各 PenaltyType に対応する共通辞書キーワードリスト */
    private val penaltyKeywords: Map<PenaltyType, List<String>> = mapOf(
        PenaltyType.ADVICE              to SynonymDictionary.adviceWords,
        PenaltyType.JUDGMENT            to SynonymDictionary.judgmentWords,
        PenaltyType.MINIMIZATION        to SynonymDictionary.minimizationWords,
        PenaltyType.SELF_TALK           to SynonymDictionary.selfTalkWords,
        PenaltyType.EARLY_CLARIFICATION to SynonymDictionary.earlyClarificationWords,
        PenaltyType.INTERROGATION       to SynonymDictionary.interrogationWords,
        PenaltyType.PREMATURE_REFRAME   to SynonymDictionary.prematureReframeWords,
        PenaltyType.JOIN_ATTACK         to SynonymDictionary.joinAttackWords,
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
        val text = input.replace("　", " ").trim()
        val scoring = scenario.freeResponseScoring

        // 検査対象スロット = target + optional
        val allSkills = (scoring.targetSlots + scoring.optionalSlots).distinct()

        val slotResults = allSkills.map { skill ->
            val builtIn  = skillKeywords[skill] ?: emptyList()
            val scenarioKw = scoring.positiveKeywords  // シナリオ固有の加点キーワード
            val custom   = userPhrases[skill] ?: emptyList()
            val matched  = (builtIn + scenarioKw + custom).filter { text.contains(it) }
            SlotResult(skill = skill, achieved = matched.isNotEmpty(), matchedKeywords = matched)
        }

        // penalty_focus = このシナリオで特に注意するペナルティ（全ペナルティも検査する）
        val allPenalties = PenaltyType.entries
        val penaltyResults = allPenalties.map { penalty ->
            val builtIn  = penaltyKeywords[penalty] ?: emptyList()
            val scenarioKw = scoring.negativeKeywords  // シナリオ固有の減点キーワード
            val triggered = (builtIn + scenarioKw).filter { text.contains(it) }
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
