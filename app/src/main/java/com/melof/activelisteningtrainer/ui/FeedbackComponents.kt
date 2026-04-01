package com.melof.activelisteningtrainer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.melof.activelisteningtrainer.data.ActiveSkill
import com.melof.activelisteningtrainer.data.LlmScoreResult
import com.melof.activelisteningtrainer.data.PenaltyResult
import com.melof.activelisteningtrainer.data.PenaltyType

// ── 共通カード：あなたの返答 ───────────────────────────────────────────────────

@Composable
internal fun InputResponseCard(input: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "あなたの返答",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = input, fontSize = 15.sp, lineHeight = 24.sp)
        }
    }
}

// ── 共通カード：スコアバッジ ──────────────────────────────────────────────────

@Composable
internal fun ScoreBadge(
    targetAchieved: Int,
    targetTotal: Int,
    totalScore: Int,
    hasPenalty: Boolean,
) {
    val allClear   = targetAchieved == targetTotal && !hasPenalty
    val halfOrMore = targetTotal > 0 && targetAchieved >= (targetTotal + 1) / 2

    val scoreColor = when {
        allClear   -> Color(0xFF2E7D32)
        halfOrMore -> Color(0xFFF57F17)
        else       -> Color(0xFFB00020)
    }
    val scoreLabel = when {
        allClear   -> "クリア！"
        halfOrMore -> "もう少し"
        else       -> "要練習"
    }

    Card(colors = CardDefaults.cardColors(containerColor = scoreColor.copy(alpha = 0.1f))) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = scoreLabel,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = scoreColor
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "必須 $targetAchieved / $targetTotal",
                fontSize = 16.sp,
                color = scoreColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${totalScore}pt",
                fontSize = 16.sp,
                color = scoreColor
            )
        }
    }
}

// ── 共通カード：ペナルティ行 ──────────────────────────────────────────────────

@Composable
internal fun SharedPenaltyRow(penalty: PenaltyResult) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = null,
                tint = Color(0xFFB00020),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "${penalty.penalty.label}（${penalty.penalty.score}pt）",
                fontSize = 13.sp,
                color = Color(0xFFB00020),
                fontWeight = FontWeight.Medium
            )
        }
        if (penalty.triggeredWords.isNotEmpty()) {
            Text(
                text = "該当: ${penalty.triggeredWords.joinToString("、") { "「$it」" }}",
                fontSize = 11.sp,
                color = Color(0xFF888888),
                modifier = Modifier.padding(start = 22.dp)
            )
        }
    }
}

// ── 共通カード：気をつけたい要素（ペナルティまとめ） ─────────────────────────

@Composable
internal fun NgWordsCard(triggeredPenalties: List<PenaltyResult>) {
    if (triggeredPenalties.isEmpty()) return
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "気をつけたい要素",
                fontWeight = FontWeight.Bold,
                color = Color(0xFFB00020),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            triggeredPenalties.forEach { penalty ->
                SharedPenaltyRow(penalty)
            }
        }
    }
}

// ── 共通カード：文例を見る ────────────────────────────────────────────────────

@Composable
internal fun SampleResponseCard(sampleResponse: String) {
    if (sampleResponse.isEmpty()) return
    var expanded by rememberSaveable { mutableStateOf(false) }
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "文例を見る",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF6A1B9A)
                )
                TextButton(
                    onClick = { expanded = !expanded },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = if (expanded) "閉じる" else "表示",
                        fontSize = 13.sp,
                        color = Color(0xFF6A1B9A)
                    )
                }
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = sampleResponse, fontSize = 14.sp, lineHeight = 24.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "※ あくまで一例です。自分の言葉でアレンジしましょう。",
                    fontSize = 11.sp,
                    color = Color(0xFF9E9E9E)
                )
            }
        }
    }
}

// ── 共通カード：次の練習ポイント ──────────────────────────────────────────────

@Composable
internal fun AdviceCard(advice: List<String>) {
    if (advice.isEmpty()) return
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "次の練習ポイント",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF1565C0)
            )
            Spacer(modifier = Modifier.height(8.dp))
            advice.forEach { point ->
                Text(text = "・$point", fontSize = 13.sp, lineHeight = 20.sp)
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

// ── 共通ヘルパー：スキル未達アドバイス ───────────────────────────────────────

internal fun defaultSlotAdvice(skill: ActiveSkill): String = when (skill) {
    ActiveSkill.EMOTIONAL_REFLECTION ->
        "「それはつらいね」など、相手の感情を言葉にして返してみましょう"
    ActiveSkill.ACCEPTANCE ->
        "「そうなんだね」「聞いてるよ」など、受け止めの言葉を入れてみましょう"
    ActiveSkill.PROMPT ->
        "「もう少し話してみて」など、相手が続けやすい言葉を足してみましょう"
    ActiveSkill.LIGHT_FOCUS ->
        "「特にどの部分が？」など、焦点を当てる問いかけを試してみましょう"
    ActiveSkill.SUMMARY_LIKE ->
        "「つまり〜ということだったんだね」など、相手の話を整理して返しましょう"
    ActiveSkill.SAFE_PACING ->
        "「ゆっくりでいいよ」など、相手のペースを尊重する言葉を意識しましょう"
}

internal fun defaultPenaltyAdvice(penalty: PenaltyType): String = when (penalty) {
    PenaltyType.ADVICE ->
        "まず気持ちを受け止めてから。アドバイスは相手が求めたときに。"
    PenaltyType.JUDGMENT ->
        "「正しい/間違い」などの評価は、相手の気持ちを閉じさせます。"
    PenaltyType.MINIMIZATION ->
        "「大したことない」は相手の感情を否定します。まず受け止めましょう。"
    PenaltyType.SELF_TALK ->
        "「私も〜」と自分の話にすると、焦点が相手から離れてしまいます。"
    PenaltyType.EARLY_CLARIFICATION ->
        "感情を受け止める前の「なぜ？」は相手を尋問されているように感じさせます。"
    PenaltyType.INTERROGATION ->
        "矢継ぎ早の質問は会話を詰問化します。1つ受け止めてから次へ。"
    PenaltyType.PREMATURE_REFRAME ->
        "まだ気持ちが整理されていない段階でのポジティブ変換は逆効果です。"
    PenaltyType.JOIN_ATTACK ->
        "相手と一緒に第三者を攻撃すると、感情的な連帯が強化されてしまいます。"
}

// ── AI採点ボタン ─────────────────────────────────────────────────────────────

@Composable
internal fun AiScoreButton(
    loading: Boolean,
    hasResult: Boolean,
    hasApiKey: Boolean,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = !loading && hasApiKey,
        modifier = Modifier.fillMaxWidth().height(48.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF5C4A7C))
    ) {
        Text(
            text = when {
                !hasApiKey -> "AI採点（APIキー未設定）"
                loading    -> "AI採点中..."
                hasResult  -> "AI採点を再実行"
                else       -> "AI採点する"
            },
            fontSize = 14.sp
        )
    }
}

// ── AI採点結果カード ─────────────────────────────────────────────────────────

@Composable
internal fun LlmFeedbackCard(result: LlmScoreResult) {
    val scoreColor = when {
        result.score >= 80 -> Color(0xFF2E7D32)
        result.score >= 50 -> Color(0xFFF57F17)
        else               -> Color(0xFFB00020)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EAF8)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "AI採点",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF5C4A7C)
                )
                Text(
                    text = "${result.score}pt",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = scoreColor
                )
            }

            if (result.feedback.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = result.feedback,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFF333333)
                )
            }

            if (result.penalties.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                result.penalties.forEach { penalty ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Text("⚠", fontSize = 12.sp)
                        Text(
                            text = "${penalty.type}: ${penalty.reason}",
                            fontSize = 12.sp,
                            color = Color(0xFFB00020),
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            if (result.advice.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color(0xFFDDD0EE))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "次回のポイント: ${result.advice}",
                    fontSize = 12.sp,
                    color = Color(0xFF5C4A7C),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

// ── AI採点エラー表示 ─────────────────────────────────────────────────────────

@Composable
internal fun LlmErrorCard(errorMessage: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))) {
        Text(
            text = "AI採点エラー: $errorMessage",
            fontSize = 12.sp,
            color = Color(0xFFB00020),
            modifier = Modifier.padding(12.dp)
        )
    }
}
