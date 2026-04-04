package com.melof.activelisteningtrainer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.melof.activelisteningtrainer.viewmodel.TrainerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    vm: TrainerViewModel,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("設定") },
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
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            ApiKeySection(vm)
        }
    }
}

@Composable
internal fun ApiKeySection(vm: TrainerViewModel) {
    var keyText by rememberSaveable { mutableStateOf(vm.loadApiKey()) }
    var saved by rememberSaveable { mutableStateOf(false) }

    Column(modifier = Modifier.padding(top = 8.dp)) {
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
            textStyle = TextStyle(fontSize = 13.sp),
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
