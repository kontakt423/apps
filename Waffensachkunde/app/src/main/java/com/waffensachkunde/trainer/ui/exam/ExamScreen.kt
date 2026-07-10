package com.waffensachkunde.trainer.ui.exam

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ExamScreen(
    viewModel: ExamViewModel,
    onSubmitted: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showSubmitConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(state.phase) {
        if (state.phase == ExamPhase.FINISHED) onSubmitted()
    }

    if (state.phase == ExamPhase.FINISHED) return
    val question = state.questions.getOrNull(state.currentIndex) ?: return
    val minutes = state.timeRemainingSeconds / 60
    val seconds = state.timeRemainingSeconds % 60

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Verbleibend: %02d:%02d".format(minutes, seconds)) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LinearProgressIndicator(
                progress = { (state.currentIndex + 1).toFloat() / state.questions.size },
                modifier = Modifier.fillMaxWidth()
            )
            Text("Frage ${state.currentIndex + 1} von ${state.questions.size}", style = MaterialTheme.typography.labelMedium)
            Text(question.question.question, style = MaterialTheme.typography.titleLarge)

            val selected = state.answers[state.currentIndex]
            question.displayOptions.forEachIndexed { index, option ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectAnswer(index) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selected == index) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Text(option, modifier = Modifier.padding(16.dp))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = viewModel::previous,
                    enabled = state.currentIndex > 0,
                    modifier = Modifier.weight(1f)
                ) { Text("Zurück") }
                if (state.currentIndex + 1 < state.questions.size) {
                    Button(onClick = viewModel::next, modifier = Modifier.weight(1f)) { Text("Weiter") }
                } else {
                    Button(onClick = { showSubmitConfirm = true }, modifier = Modifier.weight(1f)) { Text("Abgeben") }
                }
            }
            TextButton(onClick = { showSubmitConfirm = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Prüfung jetzt abgeben (${state.answers.size}/${state.questions.size} beantwortet)")
            }
        }
    }

    if (showSubmitConfirm) {
        AlertDialog(
            onDismissRequest = { showSubmitConfirm = false },
            title = { Text("Prüfung abgeben?") },
            text = {
                val unanswered = state.questions.size - state.answers.size
                Text(if (unanswered > 0) "Du hast noch $unanswered unbeantwortete Fragen. Trotzdem abgeben?" else "Möchtest du die Prüfung jetzt abgeben?")
            },
            confirmButton = {
                TextButton(onClick = {
                    showSubmitConfirm = false
                    viewModel.submit()
                }) { Text("Abgeben") }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitConfirm = false }) { Text("Weiter bearbeiten") }
            }
        )
    }
}
