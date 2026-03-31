package com.melof.activelisteningtrainer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.melof.activelisteningtrainer.data.Difficulty
import com.melof.activelisteningtrainer.data.Scenario
import com.melof.activelisteningtrainer.viewmodel.TrainerViewModel

/**
 * @param onScenarioSelected シナリオと isChoiceMode (true=選択式 / false=自由回答) を返す
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScenarioListScreen(
    vm: TrainerViewModel,
    onScenarioSelected: (Scenario, isChoiceMode: Boolean) -> Unit,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("選択式", "自由回答")

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
            // ── モード切り替えタブ ─────────────────────────────────────────────
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF3D6B4C),
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color.White
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 14.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold
                                             else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // ── モード説明テキスト ──────────────────────────────────────────────
            val modeDescription = if (selectedTab == 0)
                "4択から最適な返しを選ぶ"
            else
                "自分の言葉で声に出して返す"

            Text(
                text = modeDescription,
                fontSize = 12.sp,
                color = Color(0xFF666666),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // ── シナリオリスト ─────────────────────────────────────────────────
            val isChoiceMode = selectedTab == 0

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
                            isChoiceMode = isChoiceMode,
                            onClick = { onScenarioSelected(scenario, isChoiceMode) }
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
    isChoiceMode: Boolean,
    onClick: () -> Unit,
) {
    val bgColor = when (scenario.difficulty) {
        Difficulty.BEGINNER     -> Color(0xFFE8F5E9)
        Difficulty.INTERMEDIATE -> Color(0xFFFFF8E1)
        Difficulty.TRAP         -> Color(0xFFFFEBEE)
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
            // モードバッジ
            SuggestionChip(
                onClick = {},
                enabled = false,
                label = {
                    Text(
                        text = if (isChoiceMode) "4択" else "自由",
                        fontSize = 11.sp
                    )
                },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    disabledContainerColor = if (isChoiceMode) Color(0xFFE3F2FD) else Color(0xFFF3E5F5),
                    disabledLabelColor = if (isChoiceMode) Color(0xFF1565C0) else Color(0xFF6A1B9A)
                )
            )
        }
    }
}
