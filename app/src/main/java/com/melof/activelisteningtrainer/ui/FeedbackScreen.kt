package com.melof.activelisteningtrainer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Check
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.melof.activelisteningtrainer.data.ActiveSkill
import com.melof.activelisteningtrainer.data.PenaltyResult
import com.melof.activelisteningtrainer.data.ScoreResult
import com.melof.activelisteningtrainer.data.SlotResult
import com.melof.activelisteningtrainer.viewmodel.TrainerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(
    vm: TrainerViewModel,
    onRetry: () -> Unit,
    onNext: () -> Unit
) {
    val result by vm.scoreResult.collectAsStateWithLifecycle()
    val scenario by vm.currentScenario.collectAsStateWithLifecycle()
    val userPhrases by vm.userPhrases.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("フィードバック") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4A7C59),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        result?.let { score ->
            val targetSlots = score.scenario.freeResponseScoring.targetSlots

            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // あなたの返答
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "あなたの返答",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = score.input, fontSize = 15.sp, lineHeight = 24.sp)
                    }
                }

                // 強フィードバックコピー（特定ペナルティが強ければ強調表示）
                val strongCopy = buildStrongFeedbackCopy(score)
                if (strongCopy != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF212121)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 18.dp, vertical = 14.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(text = "⚠", fontSize = 20.sp)
                            Text(
                                text = strongCopy,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFFFFD740),
                                lineHeight = 22.sp
                            )
                        }
                    }
                }

                // スキルスロット評価
                Text("スキル評価", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // 必須スロット
                        val targetResults = score.slotResults.filter { it.skill in targetSlots }
                        targetResults.forEachIndexed { index, slotResult ->
                            SkillSlotRow(
                                slotResult       = slotResult,
                                required         = true,
                                // カテゴリに何か登録済みではなく、提案フレーズ自体が登録済みかを判定する
                                isAlreadyRegistered = userPhrases[slotResult.skill]
                                    ?.contains(suggestPhrase(slotResult.skill)) == true,
                                onRegister       = { phrase -> vm.registerPhrase(slotResult.skill, phrase) }
                            )
                            if (index != targetResults.lastIndex) {
                                HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 0.5.dp)
                            }
                        }

                        // ボーナス（任意スロット達成）
                        val optionalSlots = score.scenario.freeResponseScoring.optionalSlots
                        val bonusResults = score.slotResults.filter {
                            it.skill in optionalSlots && it.achieved
                        }
                        bonusResults.forEach { slotResult ->
                            HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 0.5.dp)
                            SkillSlotRow(
                                slotResult          = slotResult,
                                required            = false,
                                isAlreadyRegistered = false,
                                onRegister          = {}
                            )
                        }
                    }
                }

                // ペナルティ検出
                val triggeredPenalties = score.penaltyResults.filter { it.triggered }
                if (triggeredPenalties.isNotEmpty()) {
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
                                PenaltyRow(penalty)
                            }
                        }
                    }
                }

                // スコアバッジ
                val targetAchieved = score.slotResults.count { it.skill in targetSlots && it.achieved }
                val targetTotal    = targetSlots.size
                val hasPenalty     = triggeredPenalties.isNotEmpty()
                val allClear       = targetAchieved == targetTotal && !hasPenalty
                val halfOrMore     = targetTotal > 0 && targetAchieved >= (targetTotal + 1) / 2

                val scoreColor = when {
                    allClear   -> Color(0xFF2E7D32)
                    halfOrMore -> Color(0xFFF57F17)
                    else       -> Color(0xFFB00020)
                }
                val scoreLabel = when {
                    allClear   -> "よくできました"
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
                            text = "必須 $targetAchieved / $targetTotal",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = scoreColor
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "合計 ${score.totalScore}pt",
                            fontSize = 16.sp,
                            color = scoreColor
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = scoreLabel, fontSize = 16.sp, color = scoreColor)
                    }
                }

                // 文例カード
                val sample = scenario?.sampleResponse ?: ""
                if (sample.isNotEmpty()) {
                    var sampleExpanded by rememberSaveable { mutableStateOf(false) }
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
                                    fontSize = 14.sp,
                                    color = Color(0xFF6A1B9A)
                                )
                                TextButton(
                                    onClick = { sampleExpanded = !sampleExpanded },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                ) {
                                    Text(
                                        text = if (sampleExpanded) "閉じる" else "表示",
                                        fontSize = 13.sp,
                                        color = Color(0xFF6A1B9A)
                                    )
                                }
                            }
                            if (sampleExpanded) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = sample, fontSize = 14.sp, lineHeight = 24.sp)
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

                // 次の練習ポイント
                val advice = buildAdvice(score)
                if (advice.isNotEmpty()) {
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

                // ボタン
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onRetry,
                        modifier = Modifier.weight(1f).height(52.dp)
                    ) {
                        Text("もう一度", fontSize = 16.sp)
                    }
                    Button(
                        onClick = onNext,
                        modifier = Modifier.weight(1f).height(52.dp)
                    ) {
                        Text("シナリオ一覧", fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun SkillSlotRow(
    slotResult: SlotResult,
    required: Boolean,
    isAlreadyRegistered: Boolean,
    onRegister: (String) -> Unit
) {
    var showEditor by rememberSaveable { mutableStateOf(false) }
    var phraseText by rememberSaveable { mutableStateOf("") }

    val label = if (required) slotResult.skill.label
                else "${slotResult.skill.label}（ボーナス）"

    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = if (required) FontWeight.Medium else FontWeight.Normal,
                color = if (required) Color(0xFF212121) else Color(0xFF757575)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (required && !slotResult.achieved) {
                    Text(text = "必須", fontSize = 10.sp, color = Color(0xFFB00020))
                }
                if (required) {
                    Text(
                        text = "+${slotResult.skill.score}pt",
                        fontSize = 11.sp,
                        color = if (slotResult.achieved) Color(0xFF2E7D32) else Color(0xFFAAAAAA)
                    )
                }
                Icon(
                    imageVector = if (slotResult.achieved) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = if (slotResult.achieved) Color(0xFF2E7D32) else Color(0xFFB00020),
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // 未達かつ必須スロットの場合にOK表現登録UIを表示
        if (required && !slotResult.achieved) {
            when {
                isAlreadyRegistered -> {
                    Text(
                        text = "✓ 登録済み",
                        fontSize = 12.sp,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                }
                !showEditor -> {
                    TextButton(
                        onClick = {
                            phraseText = suggestPhrase(slotResult.skill)
                            showEditor = true
                        },
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(
                            Icons.Default.AddCircleOutline,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = Color(0xFF4A7C59)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("OK表現を登録する", fontSize = 12.sp, color = Color(0xFF4A7C59))
                    }
                }
                else -> {
                    val validationError = com.melof.activelisteningtrainer.data.UserDictionaryStore
                        .validationErrorMessage(phraseText)
                    Column(modifier = Modifier.padding(top = 4.dp)) {
                        OutlinedTextField(
                            value = phraseText,
                            onValueChange = { if (it.length <= 24) phraseText = it },
                            placeholder = { Text("核フレーズを入力（6〜24字）", fontSize = 12.sp) },
                            singleLine = true,
                            isError = phraseText.isNotEmpty() && validationError != null,
                            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                            modifier = Modifier.fillMaxWidth(),
                            supportingText = {
                                if (phraseText.isNotEmpty() && validationError != null) {
                                    Text(validationError, fontSize = 11.sp, color = Color(0xFFB00020))
                                } else {
                                    Text("${phraseText.length}/24", fontSize = 11.sp)
                                }
                            }
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showEditor = false }) {
                                Text("キャンセル", fontSize = 12.sp)
                            }
                            Button(
                                onClick = {
                                    val t = phraseText.trim()
                                    if (com.melof.activelisteningtrainer.data.UserDictionaryStore.isValidPhrase(t)) {
                                        onRegister(t)
                                        showEditor = false
                                    }
                                },
                                enabled = phraseText.isNotBlank() &&
                                    com.melof.activelisteningtrainer.data.UserDictionaryStore.isValidPhrase(phraseText.trim())
                            ) {
                                Text("保存", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PenaltyRow(penalty: PenaltyResult) {
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

private fun suggestPhrase(skill: ActiveSkill): String = when (skill) {
    ActiveSkill.EMOTIONAL_REFLECTION -> "それはつらいね"
    ActiveSkill.ACCEPTANCE           -> "そうなんだね、聞いてるよ"
    ActiveSkill.PROMPT               -> "もう少し話してみて"
    ActiveSkill.LIGHT_FOCUS          -> "特にどの部分が一番しんどかった？"
    ActiveSkill.SUMMARY_LIKE         -> "つまり〜ということだったんだね"
    ActiveSkill.SAFE_PACING          -> "ゆっくりでいいよ"
}

private fun buildAdvice(score: ScoreResult): List<String> {
    val advice = mutableListOf<String>()
    val targetSlots = score.scenario.freeResponseScoring.targetSlots

    score.slotResults
        .filter { it.skill in targetSlots && !it.achieved }
        .forEach { slot ->
            val customFeedback = score.scenario.freeResponseScoring
                .customFeedback[slot.skill.name]
            advice.add(customFeedback ?: defaultSlotAdvice(slot.skill))
        }

    score.penaltyResults
        .filter { it.triggered }
        .take(2)   // 一度に表示するペナルティは2件まで
        .forEach { penalty ->
            val customFeedback = score.scenario.freeResponseScoring
                .customFeedback[penalty.penalty.name]
            advice.add(customFeedback ?: defaultPenaltyAdvice(penalty.penalty))
        }

    return advice
}

private fun defaultSlotAdvice(skill: ActiveSkill): String = when (skill) {
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

/**
 * 特定ペナルティが複数トリガーされた場合に強フィードバックコピーを返す。
 * 設計書の「正しさで関係が閉じました」「アドバイスで扉が閉まりました」等に対応。
 */
private fun buildStrongFeedbackCopy(score: ScoreResult): String? {
    val triggered = score.penaltyResults.filter { it.triggered }.map { it.penalty }
    if (triggered.isEmpty()) return null

    return when {
        com.melof.activelisteningtrainer.data.PenaltyType.JUDGMENT in triggered &&
        com.melof.activelisteningtrainer.data.PenaltyType.ADVICE in triggered ->
            "正しさで関係が閉じました"

        com.melof.activelisteningtrainer.data.PenaltyType.JUDGMENT in triggered ->
            "正しさで関係が閉じました"

        com.melof.activelisteningtrainer.data.PenaltyType.ADVICE in triggered &&
        com.melof.activelisteningtrainer.data.PenaltyType.PREMATURE_REFRAME in triggered ->
            "アドバイスで扉が閉まりました"

        com.melof.activelisteningtrainer.data.PenaltyType.ADVICE in triggered ->
            "解決しようとして、聞くことをやめてしまいました"

        com.melof.activelisteningtrainer.data.PenaltyType.SELF_TALK in triggered ->
            "自分の話にしたとき、相手は独りになりました"

        com.melof.activelisteningtrainer.data.PenaltyType.INTERROGATION in triggered ->
            "質問で追い詰めてしまいました"

        com.melof.activelisteningtrainer.data.PenaltyType.JOIN_ATTACK in triggered ->
            "一緒に怒ることで、感情の出口を塞ぎました"

        com.melof.activelisteningtrainer.data.PenaltyType.MINIMIZATION in triggered ->
            "「大したことない」が、相手の気持ちを消しました"

        triggered.size >= 2 ->
            "複数の落とし穴に同時に入りました"

        else -> null
    }
}

private fun defaultPenaltyAdvice(penalty: com.melof.activelisteningtrainer.data.PenaltyType): String =
    when (penalty) {
        com.melof.activelisteningtrainer.data.PenaltyType.ADVICE ->
            "まず気持ちを受け止めてから。アドバイスは相手が求めたときに。"
        com.melof.activelisteningtrainer.data.PenaltyType.JUDGMENT ->
            "「正しい/間違い」などの評価は、相手の気持ちを閉じさせます。"
        com.melof.activelisteningtrainer.data.PenaltyType.MINIMIZATION ->
            "「大したことない」は相手の感情を否定します。まず受け止めましょう。"
        com.melof.activelisteningtrainer.data.PenaltyType.SELF_TALK ->
            "「私も〜」と自分の話にすると、焦点が相手から離れてしまいます。"
        com.melof.activelisteningtrainer.data.PenaltyType.EARLY_CLARIFICATION ->
            "感情を受け止める前の「なぜ？」は相手を尋問されているように感じさせます。"
        com.melof.activelisteningtrainer.data.PenaltyType.INTERROGATION ->
            "矢継ぎ早の質問は会話を詰問化します。1つ受け止めてから次へ。"
        com.melof.activelisteningtrainer.data.PenaltyType.PREMATURE_REFRAME ->
            "まだ気持ちが整理されていない段階でのポジティブ変換は逆効果です。"
        com.melof.activelisteningtrainer.data.PenaltyType.JOIN_ATTACK ->
            "相手と一緒に第三者を攻撃すると、感情的な連帯が強化されてしまいます。"
    }
