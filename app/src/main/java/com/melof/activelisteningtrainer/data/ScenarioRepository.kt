package com.melof.activelisteningtrainer.data

/**
 * シナリオデータのリポジトリ。
 *
 * 現状はプレースホルダー1件のみ。
 * AL-001〜AL-030 の YAML データが完成次第、全シナリオを追加する。
 *
 * TODO: YAML/JSON ローダーへの移行を検討（YAMLデータが揃ったタイミング）
 */
object ScenarioRepository {

    val scenarios: List<Scenario> = listOf(
        // ── PLACEHOLDER ─────────────────────────────────────────────────────
        // YAML データ待ち。AL-001〜AL-030 が追加されたらこのブロックを削除。
        Scenario(
            id         = "AL-000",
            category   = ScenarioCategory.VENTING,
            difficulty = Difficulty.BEGINNER,
            title      = "（サンプル）愚痴を聞く",
            situation  = "友人が職場の愚痴を話しています。",
            utterance  = "もう本当に嫌なんだよね、毎日上司に細かいこと言われて疲れた",
            hint       = "感情反映 → 促し の2ターンを意識しましょう",
            choiceOptions = listOf(
                ChoiceOption(
                    text        = "それは疲れるよね。もう少し聞かせて。",
                    isCorrect   = true,
                    badType     = null,
                    explanation = "感情反映＋促し。相手中心の返しができています。",
                ),
                ChoiceOption(
                    text        = "上司に直接言ってみたら？",
                    isCorrect   = false,
                    badType     = "bad_advice",
                    explanation = "助言は相手が求めていない段階では逆効果です。",
                ),
                ChoiceOption(
                    text        = "私も前の職場でそういう上司いたよ。",
                    isCorrect   = false,
                    badType     = "bad_self_talk",
                    explanation = "自分語りは会話の焦点を自分に移してしまいます。",
                ),
                ChoiceOption(
                    text        = "なんで細かいことって言われるの？",
                    isCorrect   = false,
                    badType     = "bad_early_clarification",
                    explanation = "感情を受け止める前に理由を聞くのは早すぎます。",
                ),
            ),
            freeResponseScoring = FreeResponseScoring(
                targetSlots   = listOf(ActiveSkill.EMOTIONAL_REFLECTION, ActiveSkill.PROMPT),
                optionalSlots = listOf(ActiveSkill.ACCEPTANCE),
                penaltyFocus  = listOf(
                    PenaltyType.ADVICE,
                    PenaltyType.SELF_TALK,
                    PenaltyType.EARLY_CLARIFICATION,
                ),
                positiveKeywords = listOf("疲れるよね", "大変だね", "それはきつい"),
                negativeKeywords = listOf("言ってみれば", "慣れたら"),
                customFeedback   = mapOf(
                    "EMOTIONAL_REFLECTION" to "相手の「疲れた」という感情をそのまま言葉にして返せています。",
                    "ADVICE"               to "まず気持ちを受け止めてから、もし相手が求めたら一緒に考えましょう。",
                ),
            ),
            sampleResponse = "それは毎日疲れるよね。どんなことが特にしんどかった？",
        ),
    )
}
