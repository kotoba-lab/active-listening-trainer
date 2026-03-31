package com.melof.activelisteningtrainer.ui

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.melof.activelisteningtrainer.data.ActiveSkill
import com.melof.activelisteningtrainer.data.PenaltyType
import com.melof.activelisteningtrainer.data.ScoreResult
import com.melof.activelisteningtrainer.data.Scenario
import com.melof.activelisteningtrainer.viewmodel.TrainerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuidedResponseScreen(
    vm: TrainerViewModel,
    onBack: () -> Unit,
) {
    val scenario by vm.currentScenario.collectAsStateWithLifecycle()
    val scoreResult by vm.scoreResult.collectAsStateWithLifecycle()

    // 結果表示中は true
    val hasResult = scoreResult != null

    var localText by rememberSaveable { mutableStateOf("") }

    // 「もう一度」でリセット
    fun retry() {
        vm.retry()
        localText = ""
    }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val recognized = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull() ?: ""
            if (recognized.isNotEmpty()) localText = recognized
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ガイド付き練習") },
                navigationIcon = {
                    IconButton(onClick = {
                        vm.retry()
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4A7C59),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        scenario?.let { sc ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 状況カード
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("状況", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF1565C0))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = sc.situation, fontSize = 13.sp, lineHeight = 20.sp)
                    }
                }

                // 相手のセリフ
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("相手のセリフ", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFFB00020))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = sc.utterance,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 26.sp,
                            color = Color(0xFF212121)
                        )
                    }
                }

                // ── 練習スキルガイド ─────────────────────────────────────────────
                SkillGuideCard(sc)

                HorizontalDivider(color = Color(0xFFE0E0E0))

                if (!hasResult) {
                    // ── 入力エリア ───────────────────────────────────────────────
                    OutlinedTextField(
                        value = localText,
                        onValueChange = { localText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 96.dp),
                        placeholder = {
                            Text(
                                text = "マイクボタンを押して話す\nまたはここに直接入力",
                                color = Color(0xFFAAAAAA),
                                fontSize = 14.sp,
                                lineHeight = 22.sp
                            )
                        },
                        label = { Text("返答") },
                        supportingText = {
                            Text(
                                text = "${localText.length}文字",
                                color = Color(0xFF9E9E9E)
                            )
                        },
                        minLines = 3,
                        textStyle = TextStyle(fontSize = 15.sp)
                    )

                    // マイクボタン
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ja-JP")
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "返答を話してください")
                            }
                            speechLauncher.launch(intent)
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF4A7C59))
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = "音声入力")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (localText.isEmpty()) "話す" else "言い直す",
                            fontSize = 16.sp
                        )
                    }

                    // 採点ボタン
                    Button(
                        onClick = { vm.submitResponse(localText) },
                        enabled = localText.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A7C59))
                    ) {
                        Text("採点する", fontSize = 18.sp)
                    }

                } else {
                    // ── 結果表示（インライン） ────────────────────────────────────
                    scoreResult?.let { result ->
                        GuidedResultSection(sc, result)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { retry() },
                                modifier = Modifier.weight(1f).height(52.dp)
                            ) {
                                Text("もう一度", fontSize = 16.sp)
                            }
                            Button(
                                onClick = {
                                    vm.retry()
                                    onBack()
                                },
                                modifier = Modifier.weight(1f).height(52.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A7C59))
                            ) {
                                Text("一覧に戻る", fontSize = 16.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// ── スキルガイドカード ──────────────────────────────────────────────────────────

@Composable
private fun SkillGuideCard(sc: Scenario) {
    val scoring = sc.freeResponseScoring

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "今回意識するスキル",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFF33691E)
            )
            Spacer(modifier = Modifier.height(10.dp))

            // 必須スキル
            scoring.targetSlots.forEach { skill ->
                SkillHintRow(skill = skill, required = true)
                Spacer(modifier = Modifier.height(6.dp))
            }

            // 任意スキル（あれば）
            if (scoring.optionalSlots.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "＋ 加点（余裕があれば）",
                    fontSize = 11.sp,
                    color = Color(0xFF666666)
                )
                Spacer(modifier = Modifier.height(6.dp))
                scoring.optionalSlots.forEach { skill ->
                    SkillHintRow(skill = skill, required = false)
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            // 注意ポイント
            if (scoring.penaltyFocus.isNotEmpty()) {
                HorizontalDivider(
                    color = Color(0xFFCCDDCC),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text(
                    text = "⚠ 気をつけること",
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = Color(0xFFBF360C)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    scoring.penaltyFocus.forEach { penalty ->
                        SuggestionChip(
                            onClick = {},
                            label = {
                                Text(
                                    text = "${penalty.label}（${penalty.score}pt）",
                                    fontSize = 11.sp
                                )
                            },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = Color(0xFFFFEBEE),
                                labelColor = Color(0xFFB00020)
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SkillHintRow(skill: ActiveSkill, required: Boolean) {
    val hints = skillHintPhrases(skill)
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SuggestionChip(
                onClick = {},
                label = {
                    Text(
                        text = "${skill.label} +${skill.score}pt",
                        fontSize = 12.sp,
                        fontWeight = if (required) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = if (required) Color(0xFFE8F5E9) else Color(0xFFF5F5F5),
                    labelColor = if (required) Color(0xFF2E7D32) else Color(0xFF666666)
                )
            )
            if (required) {
                Text(text = "必須", fontSize = 10.sp, color = Color(0xFFB00020))
            }
        }
        // ヒントフレーズをチップで表示
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
        ) {
            hints.forEach { phrase ->
                SuggestionChip(
                    onClick = {},
                    label = { Text(text = "「$phrase」", fontSize = 11.sp) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = Color(0xFFF9FBF9),
                        labelColor = Color(0xFF555555)
                    )
                )
            }
        }
    }
}

// ── 結果セクション ─────────────────────────────────────────────────────────────

@Composable
private fun GuidedResultSection(sc: Scenario, result: ScoreResult) {
    val targetSlots = sc.freeResponseScoring.targetSlots
    val targetAchieved = result.slotResults.count { it.skill in targetSlots && it.achieved }
    val targetTotal = targetSlots.size
    val triggeredPenalties = result.penaltyResults.filter { it.triggered }
    val allClear = targetAchieved == targetTotal && triggeredPenalties.isEmpty()

    // あなたの返答
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("あなたの返答", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = result.input, fontSize = 15.sp, lineHeight = 24.sp)
        }
    }

    // スコアバッジ
    val scoreColor = when {
        allClear                              -> Color(0xFF2E7D32)
        targetAchieved >= (targetTotal + 1) / 2 -> Color(0xFFF57F17)
        else                                  -> Color(0xFFB00020)
    }
    Card(colors = CardDefaults.cardColors(containerColor = scoreColor.copy(alpha = 0.1f))) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (allClear) "クリア！" else "もう少し",
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
                text = "${result.totalScore}pt",
                fontSize = 16.sp,
                color = scoreColor
            )
        }
    }

    // スキル達成チェック
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("スキル結果", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
            result.slotResults.filter { it.skill in targetSlots }.forEach { slotResult ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (slotResult.achieved) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            tint = if (slotResult.achieved) Color(0xFF2E7D32) else Color(0xFFB00020),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(text = slotResult.skill.label, fontSize = 14.sp)
                    }
                    Text(
                        text = if (slotResult.achieved) "+${slotResult.skill.score}pt" else "---",
                        fontSize = 13.sp,
                        color = if (slotResult.achieved) Color(0xFF2E7D32) else Color(0xFFAAAAAA)
                    )
                }
            }

            // ペナルティ
            if (triggeredPenalties.isNotEmpty()) {
                HorizontalDivider(color = Color(0xFFF0F0F0), modifier = Modifier.padding(vertical = 6.dp))
                Text("⚠ 気をつけたい要素", fontSize = 12.sp, color = Color(0xFFB00020))
                Spacer(modifier = Modifier.height(4.dp))
                triggeredPenalties.forEach { pr ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = pr.penalty.label, fontSize = 13.sp, color = Color(0xFFB00020))
                        Text(
                            text = "${pr.penalty.score}pt",
                            fontSize = 13.sp,
                            color = Color(0xFFB00020)
                        )
                    }
                }
            }
        }
    }

    // 未達スキルのフィードバック
    val missedSlots = result.slotResults.filter { it.skill in targetSlots && !it.achieved }
    if (missedSlots.isNotEmpty()) {
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("次の練習ポイント", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1565C0))
                Spacer(modifier = Modifier.height(8.dp))
                missedSlots.forEach { slotResult ->
                    val feedback = sc.freeResponseScoring.customFeedback[slotResult.skill.name]
                        ?: defaultSlotAdvice(slotResult.skill)
                    Text(text = "・$feedback", fontSize = 13.sp, lineHeight = 20.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }

    // 文例
    val sample = sc.sampleResponse
    if (sample.isNotEmpty()) {
        var expanded by rememberSaveable { mutableStateOf(false) }
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("文例を見る", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF6A1B9A))
                    TextButton(onClick = { expanded = !expanded }) {
                        Text(if (expanded) "閉じる" else "表示", fontSize = 13.sp, color = Color(0xFF6A1B9A))
                    }
                }
                if (expanded) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = sample, fontSize = 14.sp, lineHeight = 24.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "※ あくまで一例です。",
                        fontSize = 11.sp,
                        color = Color(0xFF9E9E9E)
                    )
                }
            }
        }
    }
}

// ── ヘルパー関数 ───────────────────────────────────────────────────────────────

/** スキルごとのヒントフレーズ（3〜4件） */
private fun skillHintPhrases(skill: ActiveSkill): List<String> = when (skill) {
    ActiveSkill.EMOTIONAL_REFLECTION -> listOf("それはつらいね", "しんどいね", "怖かったね")
    ActiveSkill.ACCEPTANCE           -> listOf("そうなんだね", "聞いてるよ", "大変だったね")
    ActiveSkill.PROMPT               -> listOf("もう少し話してみて", "続けて", "それで？")
    ActiveSkill.LIGHT_FOCUS          -> listOf("特にどの部分が？", "一番しんどかったのは？")
    ActiveSkill.SUMMARY_LIKE         -> listOf("つまり〜ということだね", "〜ということだったんだね")
    ActiveSkill.SAFE_PACING          -> listOf("ゆっくりでいいよ", "急がなくていい")
}

private fun defaultSlotAdvice(skill: ActiveSkill): String = when (skill) {
    ActiveSkill.EMOTIONAL_REFLECTION -> "「それはつらいね」など、相手の感情を言葉にして返してみましょう"
    ActiveSkill.ACCEPTANCE           -> "「そうなんだね」「聞いてるよ」など、受け止めの言葉を入れてみましょう"
    ActiveSkill.PROMPT               -> "「もう少し話してみて」など、相手が続けやすい言葉を足してみましょう"
    ActiveSkill.LIGHT_FOCUS          -> "「特にどの部分が？」など、軽く焦点を当ててみましょう"
    ActiveSkill.SUMMARY_LIKE         -> "「つまり〜ということだったんだね」など、相手の話を整理して返しましょう"
    ActiveSkill.SAFE_PACING          -> "「ゆっくりでいいよ」など、相手のペースを尊重する言葉を意識しましょう"
}
