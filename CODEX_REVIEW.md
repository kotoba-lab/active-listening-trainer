# Codex レビュー依頼 — active-listening-trainer（第2回）

## プロジェクト概要

アクティブリスニングの**衝動制御訓練アプリ**（Android / Jetpack Compose）。
「助言したい」「判断したい」「自分語りしたい」「急かしたい」という衝動を抑え、
相手の感情に寄り添う返しができるかを繰り返し練習することが目的。

complaint-trainer（苦情対応練習）の姉妹アプリとして開発中。

**GitHub:** https://github.com/kotoba-lab/active-listening-trainer
**ローカル:** `C:\Users\melof\active-listening-trainer`
**ビルド:** `JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug`

---

## 第2回レビューの背景

前回（第1回）のCodexレビュー指摘を反映したあと、以下を追加実装した。
今回はこれらの実装品質と、**教育アプリとしての設計妥当性** の両面をレビューしてほしい。

---

## 前回レビュー以降の主な実装

### A. 初見ユーザー向けヘルプUI

- `ScenarioListScreen`: 初回クイック案内カード（SharedPreferences で dismissed 永続化）
- `ScenarioListScreen`: モード説明に `?` アイコン → AlertDialog（推奨学習順序ナッジ付き）
- `SpeakScreen`: 「返答のコツ」折りたたみカード
- `FeedbackScreen`: 結果の読み方ガイド1行

### B. /learning-app-upgrade フェーズ1（DB不要）

- `FeedbackComponents` の `NgWordsCard`: NG要素タップ → `penaltyWhyReason()` で理由説明 AlertDialog
- `SpeakScreen` / `GuidedResponseScreen`: 音声認識失敗時の Snackbar
- モード `?` ダイアログ内に推奨学習順序セクション（現在地ハイライト＋補足ナッジ）

### C. /learning-app-upgrade フェーズ2（Room DB）

- `AttemptRecord` Entity / `AttemptDao` / `AppDatabase`（`trainer.db`）
- `TrainerViewModel` に3つの StateFlow を追加：
  - `masteredScenarioIds`: 直近3回連続クリアで習得済みと判定
  - `todayDone`: 当日1問以上回答で true
  - `daysSinceLastPlay`: 最終プレーからの経過日数
- `submitResponse` / `submitDepResponse` で AttemptRecord を保存
- `ScenarioCard`: 習得済みに「✓ 習得」緑バッジ
- `TodayQuotaBanner`: 当日達成状況バナー
- `ReturnBonusCard`: 3日以上空いたら入門シナリオへ誘導

### D. FeedbackUiState によるUI共通化

- `FeedbackUiState` データクラスを `FeedbackComponents.kt` に追加
- `ScoreResult.toFeedbackUiState()` 拡張関数（アドバイス生成ロジックも内包）
- `SkillChecklistCard` composable（登録UIなしの共通スキルチェックリスト）
- `GuidedResultSection`: 独自実装（70行）→ `SkillChecklistCard` + `AdviceCard` + `SampleResponseCard` に置き換え
- `FeedbackScreen`: `buildAdvice()` を削除し `uiState.advice` に統合

---

## 現在のファイル構成

```
app/src/main/java/com/melof/activelisteningtrainer/
├── MainActivity.kt
├── data/
│   ├── Model.kt                        # 全データモデル
│   ├── SynonymDictionary.kt            # 採点辞書（6スキル + 8ペナルティ）
│   ├── ScoringEngine.kt                # 通常AL採点
│   ├── ScenarioRepository.kt           # AL-001〜AL-030（30問）
│   ├── DependencyRepository.kt         # DEP-001〜DEP-010（10問）
│   ├── DependencyScoring.kt            # 依存型採点（巻き込まれ度）
│   ├── ScenarioGenerator.kt            # 構造化ランダム生成
│   ├── LlmScoringPromptBuilder.kt      # LLM採点プロンプト生成（未接続）
│   ├── UserDictionaryStore.kt          # ユーザー登録辞書（SharedPreferences）
│   ├── PracticeLogStore.kt             # 練習ログ（SharedPreferences）
│   ├── ApiKeyStore.kt                  # Claude APIキー管理
│   ├── ClaudeApiClient.kt              # Claude API クライアント（接続準備済み）
│   ├── LlmScoreResult.kt               # LLM採点結果モデル
│   ├── AttemptRecord.kt                # Room Entity（シナリオ×合否×日時）
│   ├── AttemptDao.kt                   # Room DAO
│   └── AppDatabase.kt                  # Room Database（Singleton）
├── viewmodel/
│   └── TrainerViewModel.kt             # AndroidViewModel（全機能統合）
└── ui/
    ├── ScenarioListScreen.kt           # 一覧（ヘルプ/タブ/ノルマ/復帰/習得バッジ）
    ├── ChoiceModeScreen.kt             # 4択モード
    ├── GuidedResponseScreen.kt         # ガイド付きモード
    ├── SpeakScreen.kt                  # 自由回答（返答のコツ付き）
    ├── FeedbackScreen.kt               # 採点結果（FeedbackUiStateベース）
    ├── FeedbackComponents.kt           # 共通UI部品（FeedbackUiState含む）
    ├── DependencyListScreen.kt         # 依存型一覧
    ├── DependencyModeScreen.kt         # 依存型回答・採点
    ├── PracticeHistoryScreen.kt        # 練習履歴一覧
    └── UserDictionaryScreen.kt         # ユーザー辞書管理
```

---

## 設計思想（学習UX）

complaint-trainer で整理した設計哲学をそのまま適用している。

- **Tiny Habits**: 1問やったらノルマ達成。開始率を上げるためにハードルを極限まで下げる
- **Return over Streak**: 連続記録より復帰を促進。3日空いても罰せず、入門問題に誘導する
- **Scaffolded Difficulty**: 選択式→ガイド付き→自由回答の推奨順序を可視化
- **Spaced Repetition lite**: 3回連続クリアで「習得済み」バッジ。無限周回を防ぐ

---

## コードレビューで見てほしいポイント

### 1. Room DB 設計

- `AttemptRecord` は `scenarioId` と `passed` のみ。スコア・ペナルティ詳細は保存していない。これは意図的な最小設計だが、将来の分析に支障が出るか
- `masteredScenarioIds` の判定: `dao.lastThree(id)` を `allAttemptedIds()` の Flow の中で `suspend` 関数として呼んでいる。`map { }` の中でサスペンドしているが、これは IO スレッドで実行されるか確認したい
- `todayStart()` は ViewModel 初期化時に1回だけ計算される（`init` ブロックの外で呼ばれる）。日付をまたいでアプリを使い続けた場合に `todayDone` が正しく更新されない可能性がある
- `DependencyScenario.id` に `"dep_"` プレフィックスをコード内で付けて保存しているが、`DependencyRepository` の id 定義と不整合が起きる可能性がある

### 2. FeedbackUiState の設計

- `ScoreResult.toFeedbackUiState()` がアドバイス生成ロジックも内包しているが、これはデータ変換の責務を超えているかもしれない（ViewModel か別ユーティリティに出すべきか）
- `FeedbackScreen` は登録UI付きの `SkillSlotRow`、`GuidedResultSection` は `SkillChecklistCard` と二系統になった。FeedbackScreen の `SkillSlotRow` も共通化できるか
- `toFeedbackUiState()` は pure function だが、`FeedbackComponents.kt` に置かれているため UI パッケージに採点ロジックが混在している

### 3. 採点ロジックの精度（前回からの継続課題）

- `SynonymDictionary.kt` の短いキーワード（「特に」「うん」「なぜ」等）の誤検知
- `EARLY_CLARIFICATION` の「なぜ」が感情反映文脈（「なぜそんなにつらいんだろうね」）でも引っかかる
- `ScoringEngine` の `positiveKeywords`・`negativeKeywords`（シナリオ固有）が全スロット共通で適用される設計

### 4. ViewModel の肥大化

- `TrainerViewModel` が AL通常・依存型・LLM採点・UserDictionary・PracticeLog・Room DB の全責務を持っている
- `submitResponse` と `submitDepResponse` で `PracticeLogStore`（SharedPreferences）と `AttemptRecord`（Room DB）の両方に書いており、同じ練習記録が2系統に分散している

### 5. 小さな技術的負債

- `SpeakScreen.kt` に deprecated の `Divider` が残っている（`HorizontalDivider` に要更新）
- `ScenarioListScreen` の `RoundedCornerShape(0.dp)` → `RectangleShape`
- `ScenarioListScreen` で `currentMode` がシャドウされている（`tabs[selectedTab].mode` と AlertDialog 内の `currentMode` が衝突）
- `PracticeLogStore`（SharedPreferences）と `AppDatabase`（Room）の練習ログが二重管理になっている。どちらかに統一すべきか

---

## 教育アプリとしてのレビューをお願いしたいポイント

このアプリは「知識を教える」のではなく、「衝動を抑制する習慣を身につける」ことが目的。
以下の観点から、教育設計として妥当かどうかを評価してほしい。

### 1. 学習モデルの妥当性

- 選択式→ガイド付き→自由回答 の3段階は、スキル習得の段階的足場（scaffolding）として機能しているか
- 「3回連続クリア = 習得済み」という基準は適切か。連続3回ではなく「直近5回中4回クリア」のような基準の方が実態に即しているか
- Tiny Habits の「1問=ノルマ達成」は、習熟を促す設計として適切か、それとも逆にモチベーションを上げすぎてしまうか

### 2. フィードバックの教育効果

- 強フィードバックコピー（「正しさで関係が閉じました」等の黒背景黄テキスト）は、学習者の行動変容に有効か、それとも萎縮・ネガティブ感情を生みやすいか
- NG要素の「なぜ？」ダイアログ（`penaltyWhyReason`）で理由説明をしている。これは正しい。ただし説明文の長さ・トーン・具体例の有無として現状は適切か
- 「次の練習ポイント」（AdviceCard）は未達スキル＋発動ペナルティの列挙になっている。これは学習者に多すぎる情報を与えていないか

### 3. シナリオの教育的品質

- AL-001〜AL-030 の30問で、BEGINNER → INTERMEDIATE → TRAP の難易度遷移は自然か
- TRAPシナリオ（「なぜ頑張れないの」系の正論トラップ）が学習者にとって理解できる難しさか、それとも理不尽に感じるレベルか
- DEP-001〜010（依存・不安型）のシナリオは、通常ALシナリオの発展形として適切な位置づけか
- シナリオの文言に、特定の価値観や職種に偏りがないか（ケアワーカー・会社員・家族関係が混在している）

### 4. 習慣化設計の妥当性

- 「復帰ボーナス（3日で入門誘導）」の閾値は適切か。1日でも誘導すべきか、7日待つべきか
- 「習得バッジ」は学習者のモチベーションを上げるか、それとも「もうこの問題は終わり」という誤解を生むか
- 今日のノルマ達成バナーに絵文字ではなく文字のみを使っているが、視覚的な達成感として十分か

### 5. アプリの対象ユーザーと難易度の一致

- このアプリは「ケアワーカー・支援職・接客業を想定した訓練ツール」として設計されているが、一般ユーザーにも使えるか
- 用語（「アクティブリスニング」「感情反映」「ペーシング」等）が専門的すぎて一般ユーザーの入り口を狭めていないか
- 初見ユーザー向けのヘルプカードで「感情を受け止める練習」という説明は十分か。「解決しない・共感する」という本質的な行動変容のゴールが伝わるか

---

## 現時点での未実装・設計宿題

| 項目 | 状態 | 備考 |
|------|------|------|
| LLM採点API実接続 | 未接続 | `ClaudeApiClient` / `LlmScoringPromptBuilder` は実装済み |
| PracticeLogStore と AppDatabase の統合 | 二重管理中 | どちらかに統一推奨 |
| EXPERT難易度シナリオ | 未作成 | Difficultyに定義はあるがシナリオが0問 |
| シナリオ自動生成（LLM）UI | 未接続 | `ScenarioGenerator.buildGenerationPrompt` は実装済み |
| 難易度自動調整 | 未実装 | AttemptRecord があれば実装可能 |
| FeedbackScreen の SkillSlotRow 共通化 | 未対応 | 登録UI付きのため `SkillChecklistCard` と別系統のまま |

---

## ローカル確認方法

Android Studio で `C:\Users\melof\active-listening-trainer` を開き、
Gradle sync 後に ▶ で実行（minSdk 26 = Android 8.0以上）。

最新コミット: `cd124e5`（FeedbackUiState 共通化）
