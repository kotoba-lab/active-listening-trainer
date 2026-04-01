package com.melof.activelisteningtrainer.data

/**
 * シナリオタイプ。ScenarioCategoryをより大まかに分類したもの。
 * ランダム生成フローの「type選択」に対応する。
 */
enum class ScenarioType(val label: String, val weight: Int) {
    EMOTION(     "感情系",       35),  // 落ち込み・不安・愚痴
    CONFUSION(   "混乱系",       25),  // 話が散らかった・「どうしたら」
    ANGER(       "怒り系",       20),  // 怒り・被害者意識・不公平感
    DEPENDENCY(  "依存・操作系", 20),  // 依存強化・役割混乱・分裂
}

/**
 * トラップタイプ。学習者が踏みやすい誤りのパターン。
 */
enum class TrapType(val label: String) {
    ADVICE(         "助言衝動"),
    SELF_TALK(      "自分語り"),
    BONDING(        "特別関係"),
    INTERROGATION(  "尋問化"),
    REASSURANCE(    "安心乱用"),
    JUDGMENT(       "評価・判断"),
}

/**
 * 文脈タイプ。
 */
enum class ContextType(val label: String) {
    WORK(   "職場"),
    CARE(   "ケア・支援"),
    FAMILY( "家族・友人"),
}

/**
 * シナリオ生成パラメータ。
 * このパラメータを元に既存シナリオプールから選択、またはLLMに渡してテキスト生成する。
 */
data class ScenarioGenerationParams(
    val type:      ScenarioType,
    val intensity: Int,            // 1–9（Difficultyのlevelに対応）
    val traps:     List<TrapType>,
    val context:   ContextType,
)

/**
 * ScenarioCategory → ScenarioType のマッピング
 */
val ScenarioCategory.scenarioType: ScenarioType
    get() = when (this) {
        ScenarioCategory.VENTING,
        ScenarioCategory.SADNESS,
        ScenarioCategory.SHAME,
        ScenarioCategory.SELF_CAUSED      -> ScenarioType.EMOTION

        ScenarioCategory.ANXIETY,
        ScenarioCategory.SCATTERED        -> ScenarioType.CONFUSION

        ScenarioCategory.ANGER            -> ScenarioType.ANGER

        ScenarioCategory.ADVICE_TRAP,
        ScenarioCategory.SELF_TALK_TRAP,
        ScenarioCategory.EARLY_FOCUS_TRAP,
        ScenarioCategory.LOW_VERBAL_OR_SILENCE,
        ScenarioCategory.REPAIR_AND_BRIDGE -> ScenarioType.CONFUSION
    }

/**
 * PenaltyType → TrapType のマッピング
 */
val PenaltyType.trapType: TrapType
    get() = when (this) {
        PenaltyType.ADVICE              -> TrapType.ADVICE
        PenaltyType.JUDGMENT            -> TrapType.JUDGMENT
        PenaltyType.MINIMIZATION        -> TrapType.JUDGMENT
        PenaltyType.SELF_TALK           -> TrapType.SELF_TALK
        PenaltyType.EARLY_CLARIFICATION -> TrapType.INTERROGATION
        PenaltyType.INTERROGATION       -> TrapType.INTERROGATION
        PenaltyType.PREMATURE_REFRAME   -> TrapType.ADVICE
        PenaltyType.JOIN_ATTACK         -> TrapType.BONDING
    }

/**
 * Difficulty → intensity 中間値
 */
val Difficulty.intensityMid: Int
    get() = levelRange.first + (levelRange.last - levelRange.first) / 2

object ScenarioGenerator {

    /**
     * 指定パラメータに最もマッチする既存シナリオを返す。
     * マッチスコアが高い順に並べ、上位3件からランダムに選ぶ。
     */
    fun pickScenario(
        params: ScenarioGenerationParams,
        pool: List<Scenario>,
    ): Scenario? {
        if (pool.isEmpty()) return null

        data class Scored(val scenario: Scenario, val score: Int)

        val scored = pool.map { sc ->
            var score = 0

            // タイプ一致
            if (sc.category.scenarioType == params.type) score += 10

            // 難易度（intensity近さ）
            val mid = sc.difficulty.intensityMid
            score += (5 - minOf(5, kotlin.math.abs(params.intensity - mid))) * 2

            // トラップ一致（penaltyFocusとtrapを照合）
            val scenarioTraps = sc.freeResponseScoring.penaltyFocus.map { it.trapType }.toSet()
            score += params.traps.count { it in scenarioTraps } * 3

            Scored(sc, score)
        }

        val top3 = scored.sortedByDescending { it.score }.take(3)
        return top3.randomOrNull()?.scenario
    }

    /**
     * 難易度とタイプからランダムなパラメータを生成する。
     * UI上の「ランダム生成」ボタンのバックエンド。
     */
    fun randomParams(
        difficulty: Difficulty,
        type: ScenarioType? = null,
    ): ScenarioGenerationParams {
        val resolvedType  = type ?: weightedRandom(ScenarioType.entries, ScenarioType::weight)
        val intensity     = difficulty.levelRange.random()
        val trapCount     = when (difficulty) {
            Difficulty.BEGINNER     -> 1
            Difficulty.INTERMEDIATE -> 2
            Difficulty.TRAP         -> 3
            Difficulty.EXPERT       -> 3
        }
        val traps    = TrapType.entries.shuffled().take(trapCount)
        val context  = ContextType.entries.random()

        return ScenarioGenerationParams(
            type      = resolvedType,
            intensity = intensity,
            traps     = traps,
            context   = context,
        )
    }

    /**
     * LLM向けシナリオ生成プロンプト。
     * このプロンプトをClaude APIに渡すと新しいシナリオテキストが生成される。
     */
    fun buildGenerationPrompt(params: ScenarioGenerationParams): String {
        val trapStr    = params.traps.joinToString("、") { it.label }
        val contextStr = params.context.label
        return """
あなたはアクティブリスニング訓練アプリのシナリオ作成者です。
以下のパラメータに基づき、日本語の会話シナリオを1件生成してください。

## パラメータ
- タイプ: ${params.type.label}
- 強度: ${params.intensity}/9
- 誘惑トラップ: $trapStr
- 文脈: $contextStr

## 出力形式（JSON）
```json
{
  "title": "シナリオのタイトル（10字以内）",
  "situation": "状況説明（2文程度）",
  "utterance": "相手のセリフ（自然な口語、40字前後）",
  "hint": "学習者へのヒント（1文）",
  "traps": ["踏みやすいNG例1", "踏みやすいNG例2"],
  "ideal_response": "模範的な返し（自然な口語）"
}
```

## 制約
- utteranceは具体的な状況を含む自然な口語にすること
- 感情が明確に伝わるようにすること
- 強度${params.intensity}に見合った複雑さにすること（高いほどトラップが巧妙）
- 正論や解決策を返したくなる誘惑が${trapStr}の方向に働くようにすること
        """.trimIndent()
    }

    // ── 内部ユーティリティ ─────────────────────────────────────────────────────

    private fun <T> weightedRandom(items: List<T>, weightFn: (T) -> Int): T {
        val total = items.sumOf { weightFn(it) }
        var rand  = (1..total).random()
        for (item in items) {
            rand -= weightFn(item)
            if (rand <= 0) return item
        }
        return items.last()
    }
}
