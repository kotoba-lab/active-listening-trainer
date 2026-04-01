package com.melof.activelisteningtrainer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.melof.activelisteningtrainer.data.ActiveSkill
import com.melof.activelisteningtrainer.viewmodel.TrainerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDictionaryScreen(
    vm: TrainerViewModel,
    onBack: () -> Unit,
) {
    val userPhrases by vm.userPhrases.collectAsStateWithLifecycle()

    // 登録フレーズがあるスキルだけ表示
    val skillsWithPhrases = ActiveSkill.entries.filter { skill ->
        userPhrases[skill]?.isNotEmpty() == true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("マイ表現辞書") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "戻る"
                        )
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
        if (skillsWithPhrases.isEmpty()) {
            // 空状態でも API キー設定は表示する
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 24.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "登録済みのフレーズはありません",
                        fontSize = 15.sp,
                        color = Color(0xFF888888)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "フィードバック画面のスキル評価から\n「OK表現を登録する」で追加できます",
                        fontSize = 12.sp,
                        color = Color(0xFFAAAAAA),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
                ApiKeySection(vm)
                Spacer(modifier = Modifier.height(16.dp))
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                skillsWithPhrases.forEach { skill ->
                    val phrases = userPhrases[skill] ?: emptyList()

                    item(key = skill.name) {
                        Text(
                            text = "${skill.label}（+${skill.score}pt）",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF2E5C3A),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    items(phrases, key = { "${skill.name}::$it" }) { phrase ->
                        PhraseRow(
                            phrase = phrase,
                            onDelete = { vm.removePhrase(skill, phrase) }
                        )
                    }

                    item(key = "${skill.name}::divider") {
                        HorizontalDivider(color = Color(0xFFE0E0E0))
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // APIキー設定
                item {
                    ApiKeySection(vm)
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun ApiKeySection(vm: TrainerViewModel) {
    var keyText by rememberSaveable { mutableStateOf(vm.loadApiKey()) }
    var saved by rememberSaveable { mutableStateOf(false) }

    Column(modifier = Modifier.padding(top = 8.dp)) {
        HorizontalDivider(color = Color(0xFFE0E0E0))
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Claude API設定",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color(0xFF5C4A7C)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "AI採点機能に使用します。キーは端末内に保存されます。",
            fontSize = 11.sp,
            color = Color(0xFF888888)
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = keyText,
            onValueChange = { keyText = it; saved = false },
            label = { Text("APIキー（sk-ant-...）") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                vm.saveApiKey(keyText)
                saved = true
            },
            enabled = keyText.isNotBlank() && !saved,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C4A7C))
        ) {
            Text(if (saved) "保存済み" else "保存する", fontSize = 14.sp)
        }
    }
}

@Composable
private fun PhraseRow(
    phrase: String,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "「$phrase」",
            fontSize = 14.sp,
            color = Color(0xFF333333),
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                Icons.Default.DeleteOutline,
                contentDescription = "削除",
                tint = Color(0xFFB00020),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
