package com.melof.activelisteningtrainer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.melof.activelisteningtrainer.data.Difficulty
import com.melof.activelisteningtrainer.data.DependencyPhase
import com.melof.activelisteningtrainer.data.DependencyScenario
import com.melof.activelisteningtrainer.viewmodel.TrainerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DependencyListScreen(
    vm: TrainerViewModel,
    onScenarioSelected: (DependencyScenario) -> Unit,
    onBack: () -> Unit,
) {
    var selectedDifficulty by remember { mutableStateOf<Difficulty?>(null) }

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
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ランダム開始ボタン + 難易度フィルター
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val picked = vm.randomDepScenario(selectedDifficulty)
                            if (picked != null) onScenarioSelected(picked)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C4A7C))
                    ) {
                        Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (selectedDifficulty == null) "ランダムで始める"
                                   else "「${selectedDifficulty!!.label}」からランダム",
                            fontSize = 15.sp
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("難易度：", fontSize = 12.sp, color = Color(0xFF666666))
                        FilterChip(
                            selected = selectedDifficulty == null,
                            onClick = { selectedDifficulty = null },
                            label = { Text("すべて", fontSize = 12.sp) }
                        )
                        Difficulty.entries.forEach { diff ->
                            FilterChip(
                                selected = selectedDifficulty == diff,
                                onClick = { selectedDifficulty = if (selectedDifficulty == diff) null else diff },
                                label = { Text(diff.label, fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }

            item {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "巻き込まれ度メーター",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF6A1B9A)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "返答内容によって「巻き込まれ度（0〜100）」が変わります。境界を維持するOK返しができるか試してみましょう。",
                            fontSize = 12.sp,
                            color = Color(0xFF555555),
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            DependencyPhase.entries.forEach { phase ->
                val phaseScenarios = vm.dependencyScenarios.filter { it.phase == phase }
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    ) {
                        val phaseColor = phaseColor(phase)
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = phaseColor.copy(alpha = 0.15f),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = phase.label,
                                fontSize = 11.sp,
                                color = phaseColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Text(
                            text = phase.description,
                            fontSize = 11.sp,
                            color = Color(0xFF888888)
                        )
                    }
                }
                items(phaseScenarios) { scenario ->
                    DependencyScenarioCard(
                        scenario = scenario,
                        onClick = { onScenarioSelected(scenario) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun DependencyScenarioCard(
    scenario: DependencyScenario,
    onClick: () -> Unit,
) {
    val phaseColor = phaseColor(scenario.phase)
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = scenario.id,
                    fontSize = 10.sp,
                    color = Color(0xFF999999)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = scenario.title,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color(0xFF212121)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "「${scenario.utterance.take(24)}${if (scenario.utterance.length > 24) "…」" else "」"}",
                    fontSize = 12.sp,
                    color = Color(0xFF666666),
                    lineHeight = 18.sp
                )
            }
            Surface(
                shape = MaterialTheme.shapes.small,
                color = phaseColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = scenario.phase.label,
                    fontSize = 10.sp,
                    color = phaseColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }
    }
}

internal fun phaseColor(phase: DependencyPhase): Color = when (phase) {
    DependencyPhase.ANXIETY_ESCALATION    -> Color(0xFF1976D2)
    DependencyPhase.EXCESSIVE_CONTACT     -> Color(0xFFE65100)
    DependencyPhase.PSYCHOLOGICAL_CONTROL -> Color(0xFF6A1B9A)
    DependencyPhase.SPLITTING_ATTACK      -> Color(0xFFB71C1C)
}
