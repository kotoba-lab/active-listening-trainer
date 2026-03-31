package com.melof.activelisteningtrainer.data

/**
 * 統合LLM採点プロンプトビルダー。
 *
 * 通常のALシナリオ（Scenario）と依存・不安型（DependencyScenario）の両方に対応し、
 * キーワードマッチより精度の高い採点をClaude API等で行うためのプロンプトを生成する。
 *
 * 出力JSON形式（両タイプ共通）:
 * {
 *   "score": 0-100,
 *   "involvement": 0-100（依存型のみ有効、ALシナリオは常に0）,
 *   "slots": { "emotional_reflection": true/false, ... },
 *   "penalties": [ { "type": "advice", "reason": "..." } ],
 *   "feedback": "簡潔な改善指示",
 *   "advice": "次回意識するポイント"
 * }
 */
object LlmScoringPromptBuilder {

    // ── 通常ALシナリオ向け ──────────────────────────────────────────────────────

    fun buildForScenario(input: String, scenario: Scenario): String {
        val targetSlotList = scenario.freeResponseScoring.targetSlots.joinToString("\n") {
            "- ${it.name.lowercase()} （${it.label}、+${it.score}pt）"
        }
        val optionalSlotList = scenario.freeResponseScoring.optionalSlots.joinToString("\n") {
            "- ${it.name.lowercase()} （${it.label}、ボーナス+${it.score}pt）"
        }.ifEmpty { "なし" }
        val penaltyList = PenaltyType.entries.joinToString("\n") {
            "- ${it.name.lowercase()} （${it.label}、${it.score}pt）"
        }

        return """
あなたはコミュニケーション訓練の評価者です。
以下の「状況」と「相手のセリフ」に対する「返答」を構造的に採点してください。

## 状況
${scenario.situation}

## 相手のセリフ
「${scenario.utterance}」

## 返答（評価対象）
「$input」

## 必須評価スロット（達成=true/false）
$targetSlotList

## ボーナス評価スロット
$optionalSlotList

## 減点ペナルティ（該当したものを列挙）
$penaltyList

## 出力形式（JSON厳守）
```json
{
  "score": 0から100の整数,
  "involvement": 0,
  "slots": {
    "emotional_reflection": true または false,
    "acceptance": true または false,
    "prompt": true または false,
    "light_focus": true または false,
    "summary_like": true または false,
    "safe_pacing": true または false
  },
  "penalties": [
    {
      "type": "ペナルティ名（英語キー）",
      "reason": "なぜこの返答がそのペナルティに当たるか（1文）"
    }
  ],
  "feedback": "最重要の改善点を1〜2文で",
  "advice": "次回意識してほしいポイントを1文で"
}
```

## 採点ガイドライン
- 感情語や受け止めの言葉がある → emotional_reflection, acceptance = true
- 「もう少し話して」等の促しがある → prompt = true
- 解決策・アドバイスを提示している → advice ペナルティ
- 「なぜ」「どうして」等の連続質問 → interrogation ペナルティ
- 自分の体験を話し始める → self_talk ペナルティ
- 「大丈夫」「絶対うまくいく」等 → advice ペナルティ（reassurance）
- 返答の文脈・意図・言葉づかいを総合的に判断すること
        """.trimIndent()
    }

    // ── 依存・不安型シナリオ向け ────────────────────────────────────────────────

    fun buildForDependencyScenario(input: String, scenario: DependencyScenario): String {
        val ngList = scenario.ngPatterns.joinToString("\n") {
            "- ${it.name.lowercase()} （${it.label}、+${it.involvementScore}）"
        }
        val okList = scenario.okPatterns.joinToString("\n") {
            "- ${it.name.lowercase()} （${it.label}、${it.involvementScore}）"
        }

        return """
あなたはコミュニケーション訓練の評価者です。
以下の「状況」と「相手のセリフ」に対する「返答」を採点してください。
このシナリオは依存・不安型の相互作用パターンの訓練です。

## 状況
${scenario.situation}

## 相手のセリフ
「${scenario.utterance}」

## フェーズ
${scenario.phase.label}：${scenario.phase.description}

## 返答（評価対象）
「$input」

## 巻き込まれNGパターン（該当すると巻き込まれ度が上昇）
$ngList

## 境界維持OKパターン（該当すると巻き込まれ度が低下）
$okList

## 巻き込まれ度算出基準
- 各NGパターンが該当: involvementScoreを加算
- 各OKパターンが達成: involvementScoreを加算（負の値なので実質減算）
- 合計を0〜100にクランプして"involvement"とする

## 出力形式（JSON厳守）
```json
{
  "score": 0から100の整数（境界維持スキルの総合評価）,
  "involvement": 0から100の整数（巻き込まれ度）,
  "slots": {
    "boundary_maintenance": true または false,
    "role_clarity": true または false,
    "consistency": true または false,
    "de_escalation": true または false,
    "emotional_reflection": true または false,
    "neutrality": true または false
  },
  "penalties": [
    {
      "type": "NGパターン名（英語キー）",
      "reason": "なぜこの返答がそのパターンに当たるか（1文）"
    }
  ],
  "feedback": "最重要の改善点または良かった点を1〜2文で",
  "advice": "次回意識してほしいポイントを1文で"
}
```

## 採点ガイドライン
- 「いつでも連絡して」「ずっといるよ」等 → dependency_reinforcement
- 「あなただけ」「特別だから」等 → exclusive_bonding
- 「大丈夫、絶対うまくいく」等 → reassurance_overuse
- 「今すぐ会いに行く」等 → rescue_behavior
- 時間・頻度の制限を伝える → boundary_maintenance
- 専門家への接続を提案する → role_clarity
- 感情を一旦受け止めてから制限を伝える → emotional_reflection + boundary_maintenance
- 返答の文脈・意図・言葉づかいを総合的に判断すること
        """.trimIndent()
    }

    // ── 統合ラッパー（型で自動判別） ────────────────────────────────────────────

    /**
     * Scenario または DependencyScenario を Any で受け取り、型に応じてプロンプトを返す。
     * ViewModel から型を気にせず呼べる便利メソッド。
     */
    fun build(input: String, scenario: Any): String = when (scenario) {
        is Scenario           -> buildForScenario(input, scenario)
        is DependencyScenario -> buildForDependencyScenario(input, scenario)
        else                  -> error("Unsupported scenario type: ${scenario::class.simpleName}")
    }
}
