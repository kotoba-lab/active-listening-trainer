package com.melof.activelisteningtrainer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.melof.activelisteningtrainer.data.PracticeLogEntry
import com.melof.activelisteningtrainer.viewmodel.TrainerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeHistoryScreen(
    vm: TrainerViewModel,
    onBack: () -> Unit,
) {
    val log by vm.practiceLog.collectAsStateWithLifecycle()

    // 日付ごとにグループ化（"yyyy/MM/dd" キー、新しい順）
    val dayFmt = remember { SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN) }
    val grouped: List<Pair<String, List<PracticeLogEntry>>> = remember(log) {
        log.groupBy { dayFmt.format(Date(it.timestampMs)) }
            .entries
            .sortedByDescending { it.key }
            .map { it.key to it.value }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("練習履歴") },
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
        if (log.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "練習記録はまだありません",
                    fontSize = 15.sp,
                    color = Color(0xFF888888)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                grouped.forEach { (date, entries) ->
                    item(key = date) {
                        Text(
                            text = date,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF555555),
                            modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
                        )
                    }
                    items(entries, key = { it.timestampMs }) { entry ->
                        HistoryRow(entry)
                    }
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun HistoryRow(entry: PracticeLogEntry) {
    val timeFmt = remember { SimpleDateFormat("HH:mm", Locale.JAPAN) }
    val time = timeFmt.format(Date(entry.timestampMs))

    val (modeLabel, modeBg, modeFg) = when (entry.playMode) {
        "CHOICE" -> Triple("4択",    Color(0xFFE3F2FD), Color(0xFF1565C0))
        "GUIDED" -> Triple("ガイド", Color(0xFFF1F8E9), Color(0xFF2E7D32))
        "FREE"   -> Triple("自由",   Color(0xFFF3E5F5), Color(0xFF6A1B9A))
        "DEP"    -> Triple("依存型", Color(0xFFEDE7F6), Color(0xFF5C4A7C))
        else     -> Triple(entry.playMode, Color(0xFFF5F5F5), Color(0xFF555555))
    }

    val clearedColor = if (entry.cleared) Color(0xFF2E7D32) else Color(0xFF888888)
    val clearedLabel = if (entry.cleared) "クリア" else "未クリア"

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // モードバッジ
            Surface(
                color = modeBg,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = modeLabel,
                    fontSize = 10.sp,
                    color = modeFg,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            // シナリオタイトル（主体）
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.scenarioTitle,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                // DEP は必須/スコアが概念的に違う
                if (entry.playMode != "DEP") {
                    Text(
                        text = "必須 ${entry.targetAchieved}/${entry.targetTotal}  ${entry.totalScore}pt",
                        fontSize = 11.sp,
                        color = Color(0xFF888888)
                    )
                } else {
                    Text(
                        text = "巻き込まれ度 ${entry.totalScore}pt",
                        fontSize = 11.sp,
                        color = Color(0xFF888888)
                    )
                }
            }

            // クリア状態 + 時刻
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = clearedLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = clearedColor
                )
                Text(
                    text = time,
                    fontSize = 10.sp,
                    color = Color(0xFFAAAAAA)
                )
            }
        }
    }
}
