package com.melof.activelisteningtrainer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    data class TabInfo(val label: String, val mode: PlayMode, val description: String)
    val tabs = listOf(
        TabInfo("選択式",   PlayMode.CHOICE,  "4択から最適な返しを選ぶ"),
        TabInfo("ガイド付き", PlayMode.GUIDED, "スキルのヒントを見ながら自由に返す"),
        TabInfo("自由回答",  PlayMode.FREE,   "ヒントなし。自分の言葉だけで返す"),
    )
    val currentMode = tabs[selectedTab].mode

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("アクティブリスニング練習") },
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
            Text(
                text = tabs[selectedTab].description,
                fontSize = 12.sp,
                color = Color(0xFF666666),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // ── シナリオリスト ─────────────────────────────────────────────────
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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
fun ScenarioCard(
    scenario: Scenario,
    mode: PlayMode,
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
