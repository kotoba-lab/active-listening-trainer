package com.melof.activelisteningtrainer.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.melof.activelisteningtrainer.data.ChoiceOption
import com.melof.activelisteningtrainer.viewmodel.TrainerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChoiceModeScreen(
    vm: TrainerViewModel,
    onRetry: () -> Unit,
    onBack: () -> Unit,
) {
    val scenario by vm.currentScenario.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("選択式") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
            // シナリオが変わるたびにシャッフルし直す
            val shuffledOptions = remember(sc.id) { sc.choiceOptions.shuffled() }
            var selectedIndex by remember(sc.id) { mutableStateOf<Int?>(null) }

            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 難易度バッジ + カテゴリ
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text(sc.difficulty.stars, fontSize = 12.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = Color(0xFFE8F5E9)
                        )
                    )
                    Text(
                        text = sc.category.label,
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                }

                // 状況カード
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "状況",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFF1565C0)
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

                // ヒント（未回答時のみ）
                if (selectedIndex == null && sc.hint.isNotEmpty()) {
                    Text(
                        text = "ヒント：${sc.hint}",
                        fontSize = 12.sp,
                        color = Color(0xFF757575)
                    )
                }

                HorizontalDivider(color = Color(0xFFE0E0E0))

                // 選択肢
                Text(
                    text = "どう返す？",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                shuffledOptions.forEachIndexed { index, option ->
                    ChoiceOptionButton(
                        option = option,
                        index = index,
                        selectedIndex = selectedIndex,
                        onSelect = { if (selectedIndex == null) selectedIndex = index }
                    )
                }

                // 回答後: 解説カード
                selectedIndex?.let { idx ->
                    val selected = shuffledOptions[idx]
                    ResultCard(selected)
                }

                // 回答後: ボタン
                if (selectedIndex != null) {
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
                            onClick = onBack,
                            modifier = Modifier.weight(1f).height(52.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4A7C59)
                            )
                        ) {
                            Text("一覧に戻る", fontSize = 16.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ChoiceOptionButton(
    option: ChoiceOption,
    index: Int,
    selectedIndex: Int?,
    onSelect: () -> Unit,
) {
    val isSelected = selectedIndex == index
    val answered = selectedIndex != null

    // 色の決定ロジック
    val containerColor = when {
        !answered         -> Color(0xFFFFFFFF)
        isSelected && option.isCorrect  -> Color(0xFFE8F5E9)   // 正解選択
        isSelected && !option.isCorrect -> Color(0xFFFFEBEE)   // 不正解選択
        !isSelected && option.isCorrect -> Color(0xFFF1F8E9)   // 正解（未選択）
        else              -> Color(0xFFF5F5F5)                 // 不正解（未選択）
    }
    val borderColor = when {
        !answered         -> Color(0xFFCCCCCC)
        isSelected && option.isCorrect  -> Color(0xFF2E7D32)
        isSelected && !option.isCorrect -> Color(0xFFB00020)
        !isSelected && option.isCorrect -> Color(0xFF66BB6A)
        else              -> Color(0xFFE0E0E0)
    }
    val iconTint = when {
        isSelected && option.isCorrect  -> Color(0xFF2E7D32)
        isSelected && !option.isCorrect -> Color(0xFFB00020)
        !isSelected && option.isCorrect && answered -> Color(0xFF66BB6A)
        else -> Color.Transparent
    }
    val icon = when {
        option.isCorrect -> Icons.Default.Check
        else             -> Icons.Default.Close
    }

    Card(
        onClick = onSelect,
        enabled = !answered,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(
            width = if (isSelected || (!isSelected && option.isCorrect && answered)) 2.dp else 1.dp,
            color = borderColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (!answered) 2.dp else 0.dp
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = option.text,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                modifier = Modifier.weight(1f),
                color = Color(0xFF212121)
            )
            if (answered && iconTint != Color.Transparent) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ResultCard(selected: ChoiceOption) {
    val isCorrect = selected.isCorrect
    val bgColor = if (isCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    val accentColor = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFB00020)
    val label = if (isCorrect) "正解！" else "惜しい"

    Card(colors = CardDefaults.cardColors(containerColor = bgColor)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = if (isCorrect) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = label,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = accentColor
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = selected.explanation,
                fontSize = 13.sp,
                lineHeight = 22.sp,
                color = Color(0xFF212121)
            )
            if (!isCorrect && selected.badType != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "パターン：${badTypeLabel(selected.badType)}",
                    fontSize = 11.sp,
                    color = Color(0xFF888888)
                )
            }
        }
    }
}

private fun badTypeLabel(badType: String): String = when (badType) {
    "bad_advice"             -> "助言"
    "bad_judgment"           -> "評価・判断"
    "bad_minimization"       -> "矮小化"
    "bad_self_talk"          -> "自分語り"
    "bad_early_clarification"-> "早期明確化"
    "bad_interrogation"      -> "尋問化"
    "bad_premature_reframe"  -> "早すぎるリフレーミング"
    "bad_join_attack"        -> "一緒に攻撃"
    else                     -> badType
}
