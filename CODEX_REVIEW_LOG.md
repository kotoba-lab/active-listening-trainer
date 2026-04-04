# Codex Review Log

対象: `CODEX_REVIEW.md` / active-listening-trainer  
作成日: 2026-04-01

## Findings

### 1. `todayDone` が日付跨ぎで更新されず、当日ノルマ表示が誤る
- 重要度: High
- 根拠:
  - `todayDone` は `dao.todayAttempts(todayStart())` に ViewModel 初期化時点の境界値を渡して固定しています。`todayStart()` は毎回計算できる関数ですが、実際に `Flow` へ渡される値は一度だけです。
  - そのため、アプリを起動したまま 0:00 を跨ぐと、前日の `todayStart` を使い続けてしまい、`todayDone` が前日の達成状態を引き継ぎます。
  - 同系統で `daysSinceLastPlay` も `init` 時に一度計算しているだけなので、長時間起動中は復帰判定が古くなります。
- 参照:
  - `app/src/main/java/com/melof/activelisteningtrainer/viewmodel/TrainerViewModel.kt:45`
  - `app/src/main/java/com/melof/activelisteningtrainer/viewmodel/TrainerViewModel.kt:63`
  - `app/src/main/java/com/melof/activelisteningtrainer/viewmodel/TrainerViewModel.kt:71`
- 提案:
  - `todayStart` を時刻依存 `Flow` と組み合わせて日付変更時に再購読させるか、`todayDone`/`daysSinceLastPlay` を `onResume` 相当で再計算する。
  - Room 由来の値に寄せるなら「最終プレー日時」も DAO の `Flow` で監視して UI 側で日次再評価した方が自然です。

### 2. `masteredScenarioIds` は N+1 クエリ構造で、履修済み数が増えるほど再計算コストが増える
- 重要度: Medium
- 根拠:
  - `dao.allAttemptedIds()` のたびに各 `id` ごとへ `dao.lastThree(id)` を順次呼んでいます。
  - Room の `suspend` DAO なのでメインスレッドブロックには直結しにくいですが、再計算が「試行済みシナリオ数 + 1 クエリ」になっており、将来のデータ増加やバッジ表示再描画で無駄が増えます。
  - 現状件数では致命傷ではないものの、学習履歴を Room に寄せていく設計なら早めに 1 クエリ化した方が安全です。
- 参照:
  - `app/src/main/java/com/melof/activelisteningtrainer/viewmodel/TrainerViewModel.kt:53`
  - `app/src/main/java/com/melof/activelisteningtrainer/viewmodel/TrainerViewModel.kt:56`
- 提案:
  - `ROW_NUMBER()` などで「各 scenarioId の直近3件」をまとめて判定する DAO を生やす。
  - もしくは全履歴 `Flow` を取り、メモリ上で `groupBy + take(3)` して判定する。

### 3. AL 側に `EXPERT` 問題がないのに一覧で `EXPERT` 見出しが常に表示される
- 重要度: Medium
- 根拠:
  - 一覧は `Difficulty.entries.forEach` で難易度見出しを必ず描画しています。
  - 一方、`ScenarioRepository` には `BEGINNER` / `INTERMEDIATE` / `TRAP` しかなく、AL 側 `EXPERT` は 0 件です。
  - その結果、ユーザーには空の難易度セクションが見え、「未解放なのか未実装なのか」が判別できません。
- 参照:
  - `app/src/main/java/com/melof/activelisteningtrainer/ui/ScenarioListScreen.kt:293`
  - `app/src/main/java/com/melof/activelisteningtrainer/data/ScenarioRepository.kt`
- 提案:
  - `vm.scenarios.filter { it.difficulty == difficulty }.isNotEmpty()` のときだけ見出しとカードを出す。
  - もし意図的な予告なら「準備中」表記を出した方が親切です。

### 4. 練習履歴が `SharedPreferences` と Room に分裂しており、将来の整合性崩れが見え始めている
- 重要度: Medium
- 根拠:
  - `submitResponse` / `submitDepResponse` は `PracticeLogStore` と `AttemptRecord` の両方へ書き込んでいます。
  - ただし保存項目が異なり、履歴画面は `PracticeLogStore`、習得判定やノルマは Room と責務が分散しています。
  - 片方だけ削除・移行・破損したときに、ユーザーから見ると「履歴はあるのに習得が消えた」「今日はやったのにノルマ未達」などの齟齬が起きえます。
- 参照:
  - `app/src/main/java/com/melof/activelisteningtrainer/viewmodel/TrainerViewModel.kt:124`
  - `app/src/main/java/com/melof/activelisteningtrainer/viewmodel/TrainerViewModel.kt:137`
  - `app/src/main/java/com/melof/activelisteningtrainer/viewmodel/TrainerViewModel.kt:199`
  - `app/src/main/java/com/melof/activelisteningtrainer/viewmodel/TrainerViewModel.kt:212`
  - `app/src/main/java/com/melof/activelisteningtrainer/data/PracticeLogStore.kt:20`
- 提案:
  - 中期的には Room を単一の練習履歴ソースに寄せるのがよいです。
  - `AttemptRecord` を拡張し、画面表示に必要な `playMode` / `score` / `targetAchieved` などを持たせると一本化しやすいです。

## Additional Notes

### Room 設計
- `AttemptRecord` が `scenarioId` / `passed` / `timestamp` の最小設計なのは、現時点のバッジ・ノルマ用途には十分です。
- ただし今後「難易度自動調整」「苦手分析」「モード別定着率」をやるなら、少なくとも `playMode` と `totalScore`、できれば `penaltyCount` くらいまでは保存したくなります。
- `dep_${scenario.id}` の保存方式は今すぐ壊れてはいませんが、ID 正規化責務が ViewModel に漏れているので、将来参照箇所が増えるとズレやすいです。`AttemptRecord` 側で `domain/type` を分ける方が安全です。

### FeedbackUiState 設計
- `ScoreResult.toFeedbackUiState()` は pure function なので扱いやすいです。
- ただし `advice` 生成まで抱えているため、「表示用整形」と「学習介入ロジック」が同居しています。責務分離したいなら `FeedbackAdviceBuilder` のような薄い層を 1 枚置くと整理しやすいです。
- `FeedbackScreen` の `SkillSlotRow` と `SkillChecklistCard` は完全共通化しなくて大丈夫です。登録 UI の有無が本質的に違うので、無理に 1 composable に畳むと分岐が増えて読みにくくなりやすいです。

### 採点ロジック
- `ScoringEngine` 側では、シナリオ固有 `positiveKeywords` を第1必須スロットのみに限定しており、「全スロット共通適用」問題はすでに緩和されています。
- ただし短語誤爆の課題はまだ残っています。`CLAUSE` 境界導入は前進ですが、`なぜ` は文脈でペナルティにも共感にもなりうるので、単語検出だけでは教育的にノイズが出やすいです。
- 特に `EARLY_CLARIFICATION` は、「感情反映 + なぜ」が同居したときに減点しすぎない条件分岐を別途持った方が納得感が上がります。

### 小さな技術負債
- `Divider` は deprecated なので `HorizontalDivider` へ置き換えたいです。
  - `app/src/main/java/com/melof/activelisteningtrainer/ui/SpeakScreen.kt:133`
- `RoundedCornerShape(0.dp)` は `RectangleShape` の方が意図が明確です。
  - `app/src/main/java/com/melof/activelisteningtrainer/ui/ScenarioListScreen.kt:163`
  - `app/src/main/java/com/melof/activelisteningtrainer/ui/ScenarioListScreen.kt:325`
- `currentMode` のシャドーイングは実害は小さいですが、レビューコストを上げます。
  - `app/src/main/java/com/melof/activelisteningtrainer/ui/ScenarioListScreen.kt:52`
  - `app/src/main/java/com/melof/activelisteningtrainer/ui/ScenarioListScreen.kt:63`

## Educational UX Review

### 学習モデル
- 選択式 → ガイド付き → 自由回答の 3 段階はかなり妥当です。認知負荷の上げ方が自然で、衝動抑制の「型学習」には合っています。
- `3回連続クリア` は tiny habit 文脈では分かりやすく、初期運用基準としては良いです。ただし「本当に定着したか」を見るには少し脆いので、将来的には `直近5回中4回` か「時間を空けた再現成功」を含める形がより教育妥当です。
- `1問でノルマ達成` は開始率を上げる設計として適切です。上げすぎるというより、「1問で終えても自己効力感が残る」点がこのアプリの目的と合っています。

### フィードバック
- 強フィードバックコピーは印象には残りますが、現状の黒背景黄文字 + 強断定文は、失敗が続く学習者にはやや強すぎます。
- 完全にやめるより、「強コピーの下に再挑戦導線を必ず添える」「同じミスの連発時だけ少し柔らかくする」くらいが良さそうです。
- `AdviceCard` は未達スキル全部 + ペナルティ上位2件なので、初心者には少し多い場面があります。次回行動に直結する 1〜2 件へ優先順位づけすると教育効果が上がります。
- `penaltyWhyReason()` の長さとトーンは概ね良好です。さらに良くするなら 1 行だけ具体例を添えると、「何がダメか」より「何に置き換えるか」が伝わりやすいです。

### シナリオ設計
- AL の `BEGINNER → INTERMEDIATE → TRAP` 遷移は全体として自然です。
- ただし AL 側に `EXPERT` がなく、DEP 側には `EXPERT` があるため、全体の難度ラダーがやや不揃いです。通常 AL の延長として DEP があるというより、別トラックに見えやすいです。
- TRAP は教育的には有効ですが、「理不尽」と感じさせないために、フィードバックで trap の構造説明をもう少し入れると納得感が増します。

### 習慣化設計
- 復帰ボーナス 3 日はよい線です。1 日だと通常利用でも出すぎ、7 日だと復帰介入が遅いので、現状値はかなり妥当です。
- 習得バッジはモチベーションに効きます。ただし「完了」誤認は起きうるので、「習得済み」より「ひとまず安定」寄りの語感にする余地はあります。
- 今日のノルマ達成バナーは文字だけでも成立していますが、達成感はやや控えめです。大げさな演出は不要でも、色・アイコン・軽いアニメーションのどれか 1 つあると習慣化装置として強くなります。

### 対象ユーザー適合
- 一般ユーザーにも十分使えますが、用語の専門感はややあります。
- 初見ヘルプで「感情を受け止める練習」は伝わっています。ただ、「解決しない」「すぐ正さない」という禁止行動まで明示すると、アプリの狙いがさらに通りやすいです。

## Overall

今回の実装は、前回指摘への応答としてかなり良い方向に進んでいます。  
特にヘルプ導線、NG 理由ダイアログ、段階学習の可視化は、このアプリの教育目的とよく噛み合っています。  
次に優先すべきは、`todayDone` の日付跨ぎ修正と、履歴ストアの一本化方針の決定です。
