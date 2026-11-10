package com.termuxai.terminal.ui

import android.content.Intent
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
import com.termuxai.terminal.TerminalApplication
import com.termuxai.terminal.ui.theme.TerminalEmulatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TerminalEmulatorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        onOpenTerminal = { startActivity(Intent(this, TerminalActivity::class.java)) },
                        onOpenAIModels = { startActivity(Intent(this, AIModelActivity::class.java)) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onOpenTerminal: () -> Unit,
    onOpenAIModels: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Terminal", "AI Models", "Settings")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Terminal Emulator + AI") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        icon = {
                            when (index) {
                                0 -> Icon(Icons.Default.Terminal, contentDescription = title)
                                1 -> Icon(Icons.Default.Psychology, contentDescription = title)
                                2 -> Icon(Icons.Default.Settings, contentDescription = title)
                            }
                        },
                        label = { Text(title) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> TerminalTab(onOpenTerminal)
                1 -> AIModelsTab(onOpenAIModels)
                2 -> SettingsTab()
            }
        }
    }
}

@Composable
fun TerminalTab(onOpenTerminal: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            onClick = onOpenTerminal
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Open Terminal", style = MaterialTheme.typography.headlineSmall)
                Text("Launch a new terminal session with Unix-like shell")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Active Sessions", style = MaterialTheme.typography.titleMedium)
                Text("No active terminal sessions")
            }
        }
    }
}

@Composable
fun AIModelsTab(onOpenAIModels: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            onClick = onOpenAIModels
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("AI Model Manager", style = MaterialTheme.typography.headlineSmall)
                Text("Download, manage, and run local AI models")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Available Models", style = MaterialTheme.typography.titleMedium)
                Text("Gemma 2B, Gemma 7B, and more")
            }
        }
    }
}

@Composable
fun SettingsTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        // Settings items would go here
    }
}