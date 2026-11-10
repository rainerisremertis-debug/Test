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
import com.termuxai.terminal.ui.theme.TerminalEmulatorTheme
import com.termuxai.terminal.terminal.TerminalSession
import com.termuxai.terminal.terminal.TerminalView

class TerminalActivity : ComponentActivity() {
    private lateinit var terminalSession: TerminalSession

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        terminalSession = TerminalSession(this)

        setContent {
            TerminalEmulatorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TerminalScreen(
                        terminalSession = terminalSession,
                        onBack = { finish() }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        terminalSession.destroy()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalScreen(
    terminalSession: TerminalSession,
    onBack: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    var outputText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Terminal") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { terminalSession.clear() }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Terminal output area
            TerminalView(
                output = outputText,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )

            // Input area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("$ ") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    terminalSession.execute(inputText) { result ->
                        outputText += "$ $inputText\n$result\n"
                    }
                    inputText = ""
                }) {
                    Icon(Icons.Default.Send, contentDescription = "Execute")
                }
            }
        }
    }
}