package com.melof.activelisteningtrainer.ui

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.melof.activelisteningtrainer.data.Difficulty
import com.melof.activelisteningtrainer.data.Scenario
import com.melof.activelisteningtrainer.viewmodel.TrainerViewModel

/** 練習モード */
enum class PlayMode { CHOICE, GUIDED, FREE }

/**
 * @param onScenarioSelected シナリオと練習モードを返す
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScenarioListScreen(
    vm: TrainerViewModel,
    onScenarioSelected: (Scenario, PlayMode) -> Unit,
    onDependencyMode: () -> Unit = {},
    onDictionary: () -> Unit = {},
    onHistory: () -> Unit = {},
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    data class TabInfo(val label: String, val mode: PlayMode, val description: String)
    val tabs = listOf(
        TabInfo("選択式",   PlayMode.CHOICE,  "4択から最適な返しを選ぶ"),
        TabInfo("ガイド付き", PlayMode.GUIDED, "スキルのヒントを見ながら自由に返す"),
        TabInfo("自由回答",  PlayMode.FREE,   "ヒントなし。自分の言葉だけで返す"),
    )
    val currentMode = tabs[selectedTab].mode

    val context = LocalContext.current
    val helpPrefs = remember { context.getSharedPreferences("help_prefs", Context.MODE_PRIVATE) }
    var helpDismissed by remember { mutableStateOf(helpPrefs.getBoolean("help_card_dismissed", false)) }
    var showModeHelp by remember { mutableStateOf(false) }
    val masteredIds by vm.masteredScenarioIds.collectAsStateWithLifecycle()

    if (showModeHelp) {
        val currentMode = tabs[selectedTab].mode
        val (modeDesc1, modeDesc2, modeNudge) = when (currentMode) {
            PlayMode.CHOICE -> Triple(
                "4つの返しから、いちばん相手を受け止めやすいものを選ぶモードです",
                "まずは感覚をつかみたい人向けです",
                null
            )
            PlayMode.GUIDED -> Triple(
                "意識したいスキルを見ながら、自分の言葉で返すモードです",
                "自由回答の前に練習したい人に向いています",
                "まず「選択式」で返しのパターンを覚えてから始めると効果的です"
            )
            PlayMode.FREE -> Triple(
                "ヒントなしで、自分の言葉だけで返すモードです",
                "実戦に近い形で練習したい人向けです",
                "「選択式」→「ガイド付き」で型を身につけてからがおすすめです"
            )
        }
        AlertDialog(
            onDismissRequest = { showModeHelp = false },
            title = { Text("${tabs[selectedTab].label}モードとは", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(text = modeDesc1, fontSize = 14.sp, lineHeight = 22.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = modeDesc2, fontSize = 13.sp, color = Color(0xFF666666))

                    // 推奨学習順序ナッジ
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "推奨学習順序",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF444444)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    listOf(PlayMode.CHOICE to "選択式", PlayMode.GUIDED to "ガイド付き", PlayMode.FREE to "自由回答")
                        .forEach { (mode, label) ->
                            val isActive = mode == currentMode
                            Text(
                                text = if (isActive) "▶  $label  ← 今ここ" else "　  $label",
                                fontSize = 13.sp,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                color = if (isActive) Color(0xFF1565C0) else Color(0xFFAAAAAA),
                                lineHeight = 22.sp
                            )
                        }
                    if (modeNudge != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = modeNudge,
                            fontSize = 12.sp,
                            color = Color(0xFF888888),
                            lineHeight = 18.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showModeHelp = false }) { Text("閉じる") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("アクティブリスニング練習") },
                actions = {
                    IconButton(onClick = onHistory) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = "練習履歴",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = onDictionary) {
                        Icon(
                            Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = "マイ表現辞書",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4A7C59),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // ── 依存・不安型バナー ─────────────────────────────────────────────
            Card(
                onClick = onDependencyMode,
                colors = CardDefaults.cardColors(containerColor = Color(0xFF5C4A7C)),
                shape = RoundedCornerShape(0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "依存・不安型ケース",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                        Text(
                            text = "巻き込まれ度メーター付き  10問",
                            fontSize = 11.sp,
                            color = Color(0xFFCCB8E8)
                        )
                    }
                    Text(text = "→", fontSize = 18.sp, color = Color.White)
                }
            }

            // ── 初回ヘルプカード ───────────────────────────────────────────────
            if (!helpDismissed) {
                FirstTimeHelpCard(
                    onDismiss = {
                        helpPrefs.edit().putBoolean("help_card_dismissed", true).apply()
                        helpDismissed = true
                    },
                    onStartChoice = {
                        selectedTab = 0
                        helpPrefs.edit().putBoolean("help_card_dismissed", true).apply()
                        helpDismissed = true
                    }
                )
            }

            // ── モード切り替えタブ ─────────────────────────────────────────────
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF3D6B4C),
                contentColor = Color.White,
                edgePadding = 0.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color.White
                    )
                }
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = tab.label,
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold
                                             else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // ── モード説明テキスト ──────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = tabs[selectedTab].description,
                    fontSize = 12.sp,
                    color = Color(0xFF666666),
                )
                IconButton(
                    onClick = { showModeHelp = true },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "モード説明",
                        tint = Color(0xFFAAAAAA),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // ── シナリオリスト ─────────────────────────────────────────────────
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ランダム開始カード
                item {
                    RandomStartCard(
                        scenarios = vm.scenarios,
                        currentMode = currentMode,
                        onScenarioSelected = onScenarioSelected
                    )
                }

                Difficulty.entries.forEach { difficulty ->
                    item {
                        Text(
                            text = "${difficulty.stars}　${difficulty.label}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF333333),
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }
                    items(vm.scenarios.filter { it.difficulty == difficulty }) { scenario ->
                        ScenarioCard(
                            scenario = scenario,
                            mode = currentMode,
                            mastered = scenario.id in masteredIds,
                            onClick = { onScenarioSelected(scenario, currentMode) }
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun FirstTimeHelpCard(
    onDismiss: () -> Unit,
    onStartChoice: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
        shape = RoundedCornerShape(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "はじめての方へ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF5D4037)
                )
                TextButton(
                    onClick = onDismiss,
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("×", fontSize = 16.sp, color = Color(0xFF888888))
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text("・まずは「選択式」から始めるのがおすすめです", fontSize = 13.sp, lineHeight = 20.sp, color = Color(0xFF5D4037))
            Text("・このアプリは正しい助言をする練習ではなく、相手の気持ちを受け止める練習です", fontSize = 13.sp, lineHeight = 20.sp, color = Color(0xFF5D4037))
            Text("・慣れてきたら「ガイド付き」→「自由回答」と進むと使いやすいです", fontSize = 13.sp, lineHeight = 20.sp, color = Color(0xFF5D4037))
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF888888))
                ) {
                    Text("閉じる", fontSize = 13.sp)
                }
                Button(
                    onClick = onStartChoice,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A7C59))
                ) {
                    Text("選択式から始める", fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun RandomStartCard(
    scenarios: List<Scenario>,
    currentMode: PlayMode,
    onScenarioSelected: (Scenario, PlayMode) -> Unit,
) {
    var selectedDifficulty by remember { mutableStateOf<Difficulty?>(null) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8F3)),
        border = BorderStroke(1.dp, Color(0xFF4A7C59).copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "ランダムで始める",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF2E5C3A)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // 難易度フィルターチップ
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChip(
                    selected = selectedDifficulty == null,
                    onClick = { selectedDifficulty = null },
                    label = { Text("全難易度", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF4A7C59),
                        selectedLabelColor = Color.White
                    )
                )
                Difficulty.entries.forEach { diff ->
                    FilterChip(
                        selected = selectedDifficulty == diff,
                        onClick = {
                            selectedDifficulty = if (selectedDifficulty == diff) null else diff
                        },
                        label = { Text(diff.label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF4A7C59),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    val pool = if (selectedDifficulty == null) scenarios
                               else scenarios.filter { it.difficulty == selectedDifficulty }
                    pool.randomOrNull()?.let { scenario ->
                        onScenarioSelected(scenario, currentMode)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A7C59))
            ) {
                Text("ランダムで始める", fontSize = 15.sp)
            }
        }
    }
}

@Composable
fun ScenarioCard(
    scenario: Scenario,
    mode: PlayMode,
    mastered: Boolean = false,
    onClick: () -> Unit,
) {
    val bgColor = when (scenario.difficulty) {
        Difficulty.BEGINNER     -> Color(0xFFE8F5E9)
        Difficulty.INTERMEDIATE -> Color(0xFFFFF8E1)
        Difficulty.TRAP         -> Color(0xFFFFEBEE)
        Difficulty.EXPERT       -> Color(0xFFF3E5F5)
    }
    val badgeText = when (mode) {
        PlayMode.CHOICE  -> "4択"
        PlayMode.GUIDED  -> "ガイド"
        PlayMode.FREE    -> "自由"
    }
    val badgeBg = when (mode) {
        PlayMode.CHOICE  -> Color(0xFFE3F2FD)
        PlayMode.GUIDED  -> Color(0xFFF1F8E9)
        PlayMode.FREE    -> Color(0xFFF3E5F5)
    }
    val badgeFg = when (mode) {
        PlayMode.CHOICE  -> Color(0xFF1565C0)
        PlayMode.GUIDED  -> Color(0xFF2E7D32)
        PlayMode.FREE    -> Color(0xFF6A1B9A)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = scenario.title,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = Color(0xFF555555)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "「${scenario.utterance.take(26)}${if (scenario.utterance.length > 26) "…」" else "」"}",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = scenario.category.label,
                    fontSize = 11.sp,
                    color = Color(0xFF888888)
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (mastered) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = Color(0xFF2E7D32)
                    ) {
                        Text(
                            text = "✓ 習得",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                SuggestionChip(
                    onClick = {},
                    enabled = false,
                    label = { Text(text = badgeText, fontSize = 11.sp) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        disabledContainerColor = badgeBg,
                        disabledLabelColor = badgeFg
                    )
                )
            }
        }
    }
}
