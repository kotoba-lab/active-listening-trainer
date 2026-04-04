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
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.melof.activelisteningtrainer.data.ActiveSkill
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
    val llmScore by vm.llmScore.collectAsStateWithLifecycle()
    val llmLoading by vm.llmLoading.collectAsStateWithLifecycle()
    val llmError by vm.llmError.collectAsStateWithLifecycle()

    // 結果表示中は true
    val hasResult = scoreResult != null

    var localText by rememberSaveable { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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
            if (recognized.isNotEmpty()) {
                localText = recognized
            } else {
                scope.launch {
                    snackbarHostState.showSnackbar("音声を認識できませんでした。もう一度お試しください")
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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

                        AiScoreButton(
                            loading        = llmLoading,
                            hasResult      = llmScore != null,
                            hasApiKey      = vm.hasApiKey(),
                            onClick        = { vm.requestLlmScore() },
                            onGoToSettings = {}
                        )
                        llmScore?.let { LlmFeedbackCard(it) }
                        llmError?.let { LlmErrorCard(it) }

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
    val uiState = result.toFeedbackUiState()

    // あなたの返答
    InputResponseCard(input = uiState.input)

    // スコアバッジ
    ScoreBadge(
        targetAchieved = uiState.requiredAchieved,
        targetTotal    = uiState.requiredTotal,
        totalScore     = uiState.totalScore,
        hasPenalty     = uiState.triggeredPenalties.isNotEmpty()
    )

    // スキルチェックリスト（共通カード）
    SkillChecklistCard(
        requiredSlots      = uiState.requiredSlots,
        bonusSlots         = uiState.bonusSlots,
        triggeredPenalties = uiState.triggeredPenalties,
    )

    // 次の練習ポイント
    AdviceCard(advice = uiState.advice)

    // 文例
    SampleResponseCard(sampleResponse = uiState.sampleResponse)
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

