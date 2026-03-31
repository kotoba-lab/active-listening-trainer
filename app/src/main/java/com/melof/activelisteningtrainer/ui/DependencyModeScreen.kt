package com.melof.activelisteningtrainer.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.melof.activelisteningtrainer.data.DependencyNGPattern
import com.melof.activelisteningtrainer.data.DependencyOKPattern
import com.melof.activelisteningtrainer.data.InvolvementLevel
import com.melof.activelisteningtrainer.data.InvolvementResult
import com.melof.activelisteningtrainer.viewmodel.TrainerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DependencyModeScreen(
    vm: TrainerViewModel,
    onBack: () -> Unit,
) {
    val scenario by vm.currentDepScenario.collectAsStateWithLifecycle()
    val result by vm.involvementResult.collectAsStateWithLifecycle()

    var inputText by remember { mutableStateOf("") }

    // result が null になったら（retry後）入力もリセット
    LaunchedEffect(result) {
        if (result == null) inputText = ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("依存・不安型ケース") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF5C4A7C),
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
                // フェーズバッジ
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = phaseColor(sc.phase).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = sc.phase.label,
                        fontSize = 11.sp,
                        color = phaseColor(sc.phase),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // 状況カード
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE7F6))) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "状況",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFF6A1B9A)
                        )
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
                        Text(
                            text = "相手のセリフ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFFB00020)
                        )
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

                // ヒント
                if (result == null && sc.hint.isNotEmpty()) {
                    Text(
                        text = "ヒント：${sc.hint}",
                        fontSize = 12.sp,
                        color = Color(0xFF757575)
                    )
                }

                HorizontalDivider(color = Color(0xFFE0E0E0))

                if (result == null) {
                    // 入力エリア
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        label = { Text("あなたの返答") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 6,
                    )

                    Button(
                        onClick = { vm.submitDepResponse(inputText) },
                        enabled = inputText.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C4A7C))
                    ) {
                        Text("採点する", fontSize = 16.sp)
                    }
                } else {
                    // 採点結果
                    InvolvementResultSection(
                        result = result!!,
                        onRetry = { vm.retryDep() },
                        onBack = onBack,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// ── 採点結果セクション ──────────────────────────────────────────────────────────

@Composable
private fun InvolvementResultSection(
    result: InvolvementResult,
    onRetry: () -> Unit,
    onBack: () -> Unit,
) {
    val score = result.rawScore

    // 巻き込まれ度メーター
    InvolvementMeter(score = score, level = result.involvementLevel)

    Spacer(modifier = Modifier.height(4.dp))

    // フィードバックメッセージ
    val (bgColor, textColor) = when (result.involvementLevel) {
        InvolvementLevel.DANGER  -> Color(0xFFFFEBEE) to Color(0xFFB71C1C)
        InvolvementLevel.WARNING -> Color(0xFFFFF3E0) to Color(0xFFE65100)
        InvolvementLevel.CAUTION -> Color(0xFFFFFDE7) to Color(0xFFF57F17)
        InvolvementLevel.SAFE    -> Color(0xFFE8F5E9) to Color(0xFF1B5E20)
    }
    Card(colors = CardDefaults.cardColors(containerColor = bgColor)) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (result.involvementLevel == InvolvementLevel.SAFE)
                    Icons.Default.Check else Icons.Default.Warning,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = result.feedbackMessage,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = textColor
            )
        }
    }

    // NGパターン結果
    val triggeredNG = result.ngResults.filter { it.triggered }
    if (triggeredNG.isNotEmpty()) {
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "巻き込まれパターン",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFFE65100)
                )
                Spacer(modifier = Modifier.height(6.dp))
                triggeredNG.forEach { ng ->
                    NGPatternRow(ng.pattern, ng.triggeredWords)
                }
            }
        }
    }

    // OKパターン結果
    val achievedOK = result.okResults.filter { it.achieved }
    if (achievedOK.isNotEmpty()) {
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "境界維持パターン",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF2E7D32)
                )
                Spacer(modifier = Modifier.height(6.dp))
                achievedOK.forEach { ok ->
                    OKPatternRow(ok.pattern, ok.matchedWords)
                }
            }
        }
    }

    // 目標OKパターン（未達成）
    val missedOK = result.okResults.filter { !it.achieved }
    if (missedOK.isNotEmpty()) {
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "次回意識したいポイント",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF666666)
                )
                Spacer(modifier = Modifier.height(6.dp))
                missedOK.forEach { ok ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = Color(0xFFBBBBBB),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = ok.pattern.label,
                            fontSize = 13.sp,
                            color = Color(0xFF888888)
                        )
                    }
                }
            }
        }
    }

    // 模範解答
    if (result.scenario.sampleResponse.isNotEmpty()) {
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE7F6))) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "模範解答",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF6A1B9A)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = result.scenario.sampleResponse,
                    fontSize = 13.sp,
                    lineHeight = 22.sp,
                    color = Color(0xFF212121)
                )
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
            modifier = Modifier
                .weight(1f)
                .height(52.dp)
        ) {
            Text("もう一度", fontSize = 16.sp)
        }
        Button(
            onClick = onBack,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C4A7C))
        ) {
            Text("一覧に戻る", fontSize = 16.sp)
        }
    }
}

// ── 巻き込まれ度メーター ──────────────────────────────────────────────────────────

@Composable
fun InvolvementMeter(score: Int, level: InvolvementLevel) {
    val animatedProgress by animateFloatAsState(
        targetValue = score / 100f,
        animationSpec = tween(durationMillis = 800),
        label = "involvementMeter"
    )

    val safeColor   = Color(0xFF4CAF50)
    val dangerColor = Color(0xFFF44336)
    val barColor    = lerp(safeColor, dangerColor, animatedProgress)

    val levelColor = when (level) {
        InvolvementLevel.SAFE    -> Color(0xFF2E7D32)
        InvolvementLevel.CAUTION -> Color(0xFFF57F17)
        InvolvementLevel.WARNING -> Color(0xFFE65100)
        InvolvementLevel.DANGER  -> Color(0xFFB71C1C)
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "巻き込まれ度",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF333333)
            )
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "$score",
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = levelColor
                )
                Text(
                    text = "/ 100",
                    fontSize = 13.sp,
                    color = Color(0xFF888888),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = levelColor.copy(alpha = 0.15f),
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                ) {
                    Text(
                        text = level.label,
                        fontSize = 11.sp,
                        color = levelColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // バーグラフ
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(Color(0xFFE0E0E0))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = animatedProgress.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(7.dp))
                    .background(barColor)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 目盛りラベル
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("安全", fontSize = 9.sp, color = Color(0xFF4CAF50))
            Text("やや注意", fontSize = 9.sp, color = Color(0xFFF57F17))
            Text("注意", fontSize = 9.sp, color = Color(0xFFE65100))
            Text("危険", fontSize = 9.sp, color = Color(0xFFF44336))
        }
    }
}

// ── パターン行 ────────────────────────────────────────────────────────────────────

@Composable
private fun NGPatternRow(pattern: DependencyNGPattern, triggeredWords: List<String>) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(vertical = 3.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = Color(0xFFE65100),
            modifier = Modifier
                .size(16.dp)
                .padding(top = 2.dp)
        )
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = pattern.label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF212121)
                )
                Text(
                    text = "+${pattern.involvementScore}",
                    fontSize = 11.sp,
                    color = Color(0xFFE65100),
                    fontWeight = FontWeight.Bold
                )
            }
            if (triggeredWords.isNotEmpty()) {
                Text(
                    text = "「${triggeredWords.take(3).joinToString("」「")}」",
                    fontSize = 10.sp,
                    color = Color(0xFF888888)
                )
            }
        }
    }
}

@Composable
private fun OKPatternRow(pattern: DependencyOKPattern, matchedWords: List<String>) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(vertical = 3.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = Color(0xFF2E7D32),
            modifier = Modifier
                .size(16.dp)
                .padding(top = 2.dp)
        )
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = pattern.label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF212121)
                )
                Text(
                    text = "${pattern.involvementScore}",
                    fontSize = 11.sp,
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.Bold
                )
            }
            if (matchedWords.isNotEmpty()) {
                Text(
                    text = "「${matchedWords.take(3).joinToString("」「")}」",
                    fontSize = 10.sp,
                    color = Color(0xFF888888)
                )
            }
        }
    }
}
