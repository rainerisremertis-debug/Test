package com.termuxai.terminal.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.termuxai.terminal.ai.AIModelManager
import com.termuxai.terminal.ui.theme.TerminalEmulatorTheme

class AIModelActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TerminalEmulatorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AIModelScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIModelScreen() {
    val models = AIModelManager.getAvailableModels()
    var selectedModel by remember { mutableStateOf<String?>(null) }
    var downloadProgress by remember { mutableStateOf(0f) }
    var isDownloading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Model Manager") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Available Models", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            models.forEach { model ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    onClick = { selectedModel = model.id }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(model.name, style = MaterialTheme.typography.titleMedium)
                            if (AIModelManager.isModelDownloaded(model.id)) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Downloaded",
                                    tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Text(model.description, style = MaterialTheme.typography.bodySmall)
                        Text("Size: ${model.size / 1_000_000}MB", style = MaterialTheme.typography.bodySmall)
                        Text("Backend: ${model.backend}", style = MaterialTheme.typography.bodySmall)
                        if (model.hasRAG) {
                            Text("✓ RAG Memory", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary)
                        }
                        if (model.hasSkillLearning) {
                            Text("✓ Dynamic Skill Learning", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            selectedModel?.let { modelId ->
                val model = AIModelManager.getModel(modelId)
                if (model != null && !AIModelManager.isModelDownloaded(modelId)) {
                    if (isDownloading) {
                        LinearProgressIndicator(
                            progress = downloadProgress,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text("Downloading: ${(downloadProgress * 100).toInt()}%")
                    } else {
                        Button(
                            onClick = {
                                isDownloading = true
                                // Launch download coroutine
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Download ${model.name}")
                        }
                    }
                }
            }
        }
    }
}