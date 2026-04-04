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
import com.melof.activelisteningtrainer.data.PenaltyType
import com.melof.activelisteningtrainer.data.ScoreResult
import com.melof.activelisteningtrainer.data.SlotResult
import com.melof.activelisteningtrainer.data.UserDictionaryStore
import com.melof.activelisteningtrainer.viewmodel.TrainerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(
    vm: TrainerViewModel,
    onRetry: () -> Unit,
    onNext: () -> Unit,
    onGoToSettings: () -> Unit = {},
) {
    val result by vm.scoreResult.collectAsStateWithLifecycle()
    val scenario by vm.currentScenario.collectAsStateWithLifecycle()
    val userPhrases by vm.userPhrases.collectAsStateWithLifecycle()
    val llmScore by vm.llmScore.collectAsStateWithLifecycle()
    val llmLoading by vm.llmLoading.collectAsStateWithLifecycle()
    val llmError by vm.llmError.collectAsStateWithLifecycle()

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
            val uiState    = score.toFeedbackUiState()
            val strongCopy = buildStrongFeedbackCopy(score)

            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // あなたの返答
                InputResponseCard(input = uiState.input)

                // 強フィードバックコピー（特定ペナルティが強ければ強調表示）
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

                // スキルスロット評価（登録UI付き詳細版）
                Text("スキル評価", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        uiState.requiredSlots.forEachIndexed { index, slotResult ->
                            SkillSlotRow(
                                slotResult          = slotResult,
                                required            = true,
                                isAlreadyRegistered = userPhrases[slotResult.skill]
                                    ?.contains(suggestPhrase(slotResult.skill)) == true,
                                onRegister          = { phrase -> vm.registerPhrase(slotResult.skill, phrase) }
                            )
                            if (index != uiState.requiredSlots.lastIndex) {
                                HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 0.5.dp)
                            }
                        }
                        uiState.bonusSlots.forEach { slotResult ->
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
                NgWordsCard(uiState.triggeredPenalties)

                // 結果の読み方ガイド
                Text(
                    text = "まずは「必須スキル」が入っているかを見て、次に「気をつけたい要素」を確認すると分かりやすいです",
                    fontSize = 11.sp,
                    color = Color(0xFF999999),
                    lineHeight = 17.sp,
                )

                // スコアバッジ
                ScoreBadge(
                    targetAchieved = uiState.requiredAchieved,
                    targetTotal    = uiState.requiredTotal,
                    totalScore     = uiState.totalScore,
                    hasPenalty     = uiState.triggeredPenalties.isNotEmpty()
                )

                // 文例カード
                SampleResponseCard(sampleResponses = uiState.sampleResponses)

                // 次の練習ポイント
                AdviceCard(advice = uiState.advice)

                // AI採点
                AiScoreButton(
                    loading        = llmLoading,
                    hasResult      = llmScore != null,
                    hasApiKey      = vm.hasApiKey(),
                    onClick        = { vm.requestLlmScore() },
                    onGoToSettings = onGoToSettings
                )
                llmScore?.let { LlmFeedbackCard(it) }
                llmError?.let { LlmErrorCard(it) }

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
                    val validationError = UserDictionaryStore
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
                                    if (UserDictionaryStore.isValidPhrase(t)) {
                                        onRegister(t)
                                        showEditor = false
                                    }
                                },
                                enabled = phraseText.isNotBlank() &&
                                    UserDictionaryStore.isValidPhrase(phraseText.trim())
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


private fun suggestPhrase(skill: ActiveSkill): String = when (skill) {
    ActiveSkill.EMOTIONAL_REFLECTION -> "それはつらいね"
    ActiveSkill.ACCEPTANCE           -> "そうなんだね、聞いてるよ"
    ActiveSkill.PROMPT               -> "もう少し話してみて"
    ActiveSkill.LIGHT_FOCUS          -> "特にどの部分が一番しんどかった？"
    ActiveSkill.SUMMARY_LIKE         -> "つまり〜ということだったんだね"
    ActiveSkill.SAFE_PACING          -> "ゆっくりでいいよ"
}

/**
 * 特定ペナルティが複数トリガーされた場合に強フィードバックコピーを返す。
 * 設計書の「正しさで関係が閉じました」「アドバイスで扉が閉まりました」等に対応。
 */
private fun buildStrongFeedbackCopy(score: ScoreResult): String? {
    val triggered = score.penaltyResults.filter { it.triggered }.map { it.penalty }
    if (triggered.isEmpty()) return null

    return when {
        PenaltyType.JUDGMENT in triggered &&
        PenaltyType.ADVICE in triggered ->
            "正しさで関係が閉じました"

        PenaltyType.JUDGMENT in triggered ->
            "正しさで関係が閉じました"

        PenaltyType.ADVICE in triggered &&
        PenaltyType.PREMATURE_REFRAME in triggered ->
            "アドバイスで扉が閉まりました"

        PenaltyType.ADVICE in triggered ->
            "解決しようとして、聞くことをやめてしまいました"

        PenaltyType.SELF_TALK in triggered ->
            "自分の話にしたとき、相手は独りになりました"

        PenaltyType.INTERROGATION in triggered ->
            "質問で追い詰めてしまいました"

        PenaltyType.JOIN_ATTACK in triggered ->
            "一緒に怒ることで、感情の出口を塞ぎました"

        PenaltyType.MINIMIZATION in triggered ->
            "「大したことない」が、相手の気持ちを消しました"

        triggered.size >= 2 ->
            "複数の落とし穴に同時に入りました"

        else -> null
    }
}
