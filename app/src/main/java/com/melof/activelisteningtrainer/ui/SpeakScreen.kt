package com.melof.activelisteningtrainer.ui

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.melof.activelisteningtrainer.viewmodel.TrainerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeakScreen(
    vm: TrainerViewModel,
    onSubmit: (String) -> Unit,
    onBack: () -> Unit
) {
    val scenario by vm.currentScenario.collectAsStateWithLifecycle()
    var localText by rememberSaveable { mutableStateOf("") }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val results = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val recognized = results?.firstOrNull() ?: ""
            if (recognized.isNotEmpty()) localText = recognized
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("返答練習") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4A6FA5),
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 状況テキスト
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                ) {
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

                // 相手のセリフ（これが核心）
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

                // 返答のコツカード
                ResponseTipsCard()

                // ヒント
                if (sc.hint.isNotEmpty()) {
                    Text(
                        text = "ヒント：${sc.hint}",
                        fontSize = 12.sp,
                        color = Color(0xFF757575)
                    )
                }

                Divider(color = Color(0xFFE0E0E0))

                // 音声認識結果 + 手動編集エリア
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
                            text = "${localText.length}文字${if (localText.length > 80) "　⚠ 長め" else ""}",
                            color = if (localText.length > 80) Color(0xFFE65100) else Color(0xFF9E9E9E)
                        )
                    },
                    isError = localText.length > 80,
                    minLines = 3,
                    textStyle = TextStyle(fontSize = 15.sp)
                )

                // マイクボタン
                Button(
                    onClick = {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(
                                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                            )
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ja-JP")
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "返答を話してください")
                        }
                        speechLauncher.launch(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A6FA5))
                ) {
                    Icon(Icons.Default.Mic, contentDescription = "音声入力")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (localText.isEmpty()) "話す" else "言い直す",
                        fontSize = 17.sp
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // 採点ボタン
                Button(
                    onClick = { onSubmit(localText) },
                    enabled = localText.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text("採点する", fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
private fun ResponseTipsCard() {
    var expanded by rememberSaveable { mutableStateOf(true) }
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "返答のコツ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF5D4037)
                )
                TextButton(
                    onClick = { expanded = !expanded },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text(
                        text = if (expanded) "閉じる" else "開く",
                        fontSize = 12.sp,
                        color = Color(0xFF888888)
                    )
                }
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(6.dp))
                Text("・まずは解決より先に、気持ちを受け止める", fontSize = 13.sp, lineHeight = 20.sp, color = Color(0xFF5D4037))
                Text("・短くても大丈夫です", fontSize = 13.sp, lineHeight = 20.sp, color = Color(0xFF5D4037))
                Text("・助言・評価・正論から入らない方が高評価になりやすいです", fontSize = 13.sp, lineHeight = 20.sp, color = Color(0xFF5D4037))
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "例:「それはしんどかったね」「つらかったね」",
                    fontSize = 12.sp,
                    color = Color(0xFF888888)
                )
            }
        }
    }
}
