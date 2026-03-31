package com.melof.activelisteningtrainer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.melof.activelisteningtrainer.data.Difficulty
import com.melof.activelisteningtrainer.data.Scenario
import com.melof.activelisteningtrainer.viewmodel.TrainerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScenarioListScreen(
    vm: TrainerViewModel,
    onScenarioSelected: (Scenario) -> Unit
) {
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
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
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
                    ScenarioCard(scenario = scenario, onClick = { onScenarioSelected(scenario) })
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun ScenarioCard(scenario: Scenario, onClick: () -> Unit) {
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
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = scenario.title,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = Color(0xFF555555)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "「${scenario.utterance.take(28)}${if (scenario.utterance.length > 28) "…」" else "」"}",
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
    }
}
