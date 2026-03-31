# Codex レビュー依頼 — active-listening-trainer

## プロジェクト概要

アクティブリスニングの**衝動制御訓練アプリ**（Android / Jetpack Compose）。
「助言したい」「判断したい」「自分語りしたい」「急かしたい」という衝動を抑え、
相手の感情に寄り添う返しができるかを繰り返し練習することが目的。

complaint-trainer（苦情対応練習）の姉妹アプリとして新規リポジトリで開発。

GitHub: https://github.com/kotoba-lab/active-listening-trainer

---

## 技術スタック

- Kotlin + Jetpack Compose (minSdk 26, compileSdk 35)
- Navigation Compose（Single-Activity, NavHost）
- AndroidViewModel + StateFlow
- 音声入力: `RecognizerIntent`（ja-JP）
- 採点: ルールベース（辞書キーワード contains マッチ）
- ユーザー辞書: SharedPreferences（`ActiveSkill` キーで保存）

---

## ファイル構成

```
app/src/main/java/com/melof/activelisteningtrainer/
├── MainActivity.kt                        # NavHost（5ルート + 依存型2ルート）
├── data/
│   ├── Model.kt                           # 全データモデル定義
│   ├── SynonymDictionary.kt               # 採点用辞書（6スキル + 8ペナルティ）
│   ├── ScoringEngine.kt                   # 通常ALシナリオ採点ロジック
│   ├── ScenarioRepository.kt              # AL-001〜AL-030（30問）
│   ├── UserDictionaryStore.kt             # ユーザー登録辞書（SharedPreferences）
│   ├── DependencyRepository.kt            # DEP-001〜DEP-010（依存・不安型10問）
│   ├── DependencyScoring.kt               # 依存型採点ロジック（巻き込まれ度）
│   ├── ScenarioGenerator.kt               # 構造化ランダム生成ロジック
│   └── LlmScoringPromptBuilder.kt         # LLM採点プロンプト生成（未API接続）
├── viewmodel/
│   └── TrainerViewModel.kt                # AndroidViewModel（AL + 依存型 両対応）
└── ui/
    ├── ScenarioListScreen.kt              # シナリオ一覧（3モードタブ + 依存型バナー）
    ├── ChoiceModeScreen.kt                # 4択モード
    ├── GuidedResponseScreen.kt            # ガイド付き中間モード
    ├── SpeakScreen.kt                     # 自由回答（音声 or テキスト入力）
    ├── FeedbackScreen.kt                  # 採点結果（強フィードバックコピー付き）
    ├── DependencyListScreen.kt            # 依存・不安型一覧（難易度フィルター + ランダム開始）
    ├── DependencyModeScreen.kt            # 依存型回答画面（巻き込まれ度メーター付き）
    └── theme/Theme.kt                     # グリーン基調テーマ
```

---

## データモデル（Model.kt）

### 通常ALシナリオ系

| 型 | 役割 |
|---|---|
| `ActiveSkill` enum | 6種の加点スキル（感情反映・受け止め・促し・軽い焦点化・要約的返し・安全なペーシング） |
| `PenaltyType` enum | 8種の減点ペナルティ（助言・評価・矮小化・自分語り・早期明確化・尋問化・早すぎるリフレーミング・一緒に攻撃） |
| `ScenarioCategory` enum | 10カテゴリ（愚痴・落ち込み・不安・散漫・怒り・恥・自業自得・助言トラップ・自分語りトラップ・明確化トラップ） |
| `Difficulty` enum | 4段階（BEGINNER/INTERMEDIATE/TRAP/EXPERT）、`levelRange: IntRange` 付き |
| `Scenario` | AL-001〜AL-030。choiceOptions・freeResponseScoring 両形式を内包 |
| `ScoreResult` | スロット結果・ペナルティ結果・スコア計算プロパティ |

### 依存・不安型系

| 型 | 役割 |
|---|---|
| `DependencyNGPattern` enum | 8種のNG（依存強化+20〜言動不一致+5） |
| `DependencyOKPattern` enum | 6種のOK（境界維持-15〜中立的立場-5） |
| `DependencyPhase` enum | 4フェーズ（不安増大期/接触過多期/心理的コントロール期/分裂・攻撃期） |
| `DependencyScenario` | DEP-001〜DEP-010。ngKeywords / okKeywords でキーワードマップ保持 |
| `InvolvementResult` | NGスコア合計→0〜100にクランプした `rawScore`、`InvolvementLevel`、`feedbackMessage` |

---

## 画面フロー

```
ScenarioListScreen
 ├── [4択タブ]    → ChoiceModeScreen（インライン結果表示）
 ├── [ガイドタブ]  → GuidedResponseScreen（インライン結果表示）
 ├── [自由タブ]   → SpeakScreen → FeedbackScreen
 └── [依存型バナー] → DependencyListScreen → DependencyModeScreen（インライン結果表示）
```

---

## 採点の仕組み

### 通常ALシナリオ（ScoringEngine.kt）

1. `contains` マッチで 6スキル × 8ペナルティを全検査
2. ユーザー登録フレーズ（UserDictionaryStore）も合算
3. `SlotResult`・`PenaltyResult` のリストを `ScoreResult` にまとめる
4. `totalScore = (skillScore + penaltyScore).coerceAtLeast(0)`

### 依存・不安型（DependencyScoring.kt）

1. シナリオごとの `ngKeywords` / `okKeywords` に対して `contains` マッチ
2. NGパターン合計 + OKパターン合計（負値）を 0〜100 にクランプ → `rawScore`（巻き込まれ度）
3. スコアに応じて SAFE / CAUTION / WARNING / DANGER の4段階表示

### 強フィードバックコピー（FeedbackScreen.kt）

特定ペナルティが検出された場合に黒背景・黄テキストで強調表示：
- JUDGMENT → 「正しさで関係が閉じました」
- ADVICE + PREMATURE_REFRAME → 「アドバイスで扉が閉まりました」
- SELF_TALK → 「自分の話にしたとき、相手は独りになりました」
- INTERROGATION → 「質問で追い詰めてしまいました」
- JOIN_ATTACK → 「一緒に怒ることで、感情の出口を塞ぎました」

---

## ランダム生成ロジック（ScenarioGenerator.kt）

```kotlin
data class ScenarioGenerationParams(
    val type:      ScenarioType,   // EMOTION / CONFUSION / ANGER / DEPENDENCY
    val intensity: Int,            // 1–9（Difficultyのlevelに対応）
    val traps:     List<TrapType>, // ADVICE / SELF_TALK / BONDING / INTERROGATION 等
    val context:   ContextType,    // WORK / CARE / FAMILY
)
```

- `randomParams(difficulty)` で重み付きランダムにパラメータ生成
- `pickScenario(params, pool)` で既存30問からマッチスコア上位3件を選んでランダム抽出
- `buildGenerationPrompt(params)` でLLM向けシナリオ生成プロンプトも出力可能

---

## LLM採点プロンプト（LlmScoringPromptBuilder.kt）

Claude API向けの採点プロンプトを `Scenario` / `DependencyScenario` 両対応で生成。
**現時点はAPIに実接続していない**（プロンプト文字列を返すのみ）。

出力JSON形式（両タイプ共通）:
```json
{
  "score": 0-100,
  "involvement": 0-100,
  "slots": { "emotional_reflection": true, ... },
  "penalties": [ { "type": "advice", "reason": "..." } ],
  "feedback": "改善指示",
  "advice": "次回のポイント"
}
```

---

## シナリオ一覧

### 通常ALシナリオ（AL-001〜AL-030）

| 難易度 | 問数 | 説明 |
|--------|------|------|
| BEGINNER（初級, L1-2） | 10問 | 感情明確・衝動弱・助言/reassurance抑制 |
| INTERMEDIATE（中級, L3-4） | 10問 | 感情混在・軽い矛盾・明確化タイミング |
| TRAP（崩しトラップ, L5-6） | 10問 | 社会的圧力・正論が通じない・ディエスカレーション |

各シナリオは 4択・ガイド付き・自由回答の3モードに対応。

### 依存・不安型（DEP-001〜DEP-010）

| フェーズ | 問数 | 難易度 |
|----------|------|--------|
| 不安増大期 | 2問（DEP-001〜002） | BEGINNER |
| 接触過多期 | 3問（DEP-003〜005） | INTERMEDIATE |
| 心理的コントロール期 | 3問（DEP-006〜008） | INTERMEDIATE |
| 分裂・攻撃期 | 2問（DEP-009〜010） | EXPERT（L7-9） |

---

## レビューで見てほしいポイント

### 1. 採点ロジックの精度・穴

- `SynonymDictionary.kt` の辞書に抜け漏れや誤検知がないか
- `contains` マッチの粒度問題：短いキーワード（「特に」「うん」「なぜ」等）が誤検知源になっていないか
- `EARLY_CLARIFICATION` の辞書に「なぜ」「どうして」があるが、感情反映の文脈（「なぜそんなにつらいんだろうね」等）でも引っかかる可能性がある
- `ScoringEngine` が `positiveKeywords`・`negativeKeywords`（シナリオ固有）を **全スロット共通** で適用している点が設計として正しいか（特定スキル専用のはずが全スキルの判定に加算される）

### 2. 依存型採点の辞書設計

- `ngKeywords` / `okKeywords` がシナリオごとに手書きで、共通辞書（SynonymDictionary相当）がない
- DEP用の共通NGワード辞書を `DependencyScoring` に持たせるべきか
- OKパターンの検出率が低い（境界維持を示す言葉のバリエーションが少ない）

### 3. ユーザー辞書の設計

- `UserDictionaryStore.addPhrase` はフレーズ文字列をそのまま保存し、採点時は `contains` で照合
- 40字制限を設けているが、長いフレーズは次回一致しにくい
- `ActiveSkill` キー単位でしか保存できないため、ペナルティ回避フレーズの個人登録ができない
- ユーザー辞書は `GuidedResponseScreen` と `DependencyModeScreen` では未使用

### 4. コード品質・Android慣習

- `GuidedResponseScreen` と `DependencyModeScreen` がインライン結果表示のため、`FeedbackScreen` と採点結果UIが別実装になっている（ロジックの重複）
- `ScenarioListScreen` の `RoundedCornerShape(0.dp)` は `RectangleShape` で書くべき
- `SpeakScreen.kt` に deprecated の `Divider` が残っている（`HorizontalDivider` に要更新）
- `ScenarioGenerator.pickScenario` で `kotlin.math.abs` を完全修飾で書いている（import すべき）
- `DependencyListScreen` の難易度チップはランダム開始のフィルターになっているが、一覧表示自体はフィルターされない（一覧も連動させるべきか）

### 5. 未実装・拡張予定

以下は設計済みだが未実装。実装上の問題点があれば指摘してほしい。

- **LLM採点API接続**：`LlmScoringPromptBuilder` でプロンプトは完成済み。Claude API（`claude-sonnet-4-6`）を叩いてJSONレスポンスをパースし、`ScoreResult` / `InvolvementResult` を置き換える設計
- **練習ログ・履歴**：問題別の正答率・よく踏むペナルティを記録・可視化
- **難易度自動調整**：正答率に応じて次の問題を選ぶ
- **ALシナリオ側のランダム開始UI**：`ScenarioGenerator.pickScenario` は実装済みだがScenarioListScreenに開始ボタンがない
- **設定・辞書管理画面**：登録フレーズの一覧・削除UI

---

## ローカル確認方法

Android Studio で `C:\Users\melof\active-listening-trainer` を開き、
Gradle sync 後に ▶ で実行（minSdk 26 = Android 8.0以上）。
