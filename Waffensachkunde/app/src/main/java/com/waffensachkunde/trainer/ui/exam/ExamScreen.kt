package com.waffensachkunde.trainer.ui.exam

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.waffensachkunde.trainer.data.model.QuestionType
import com.waffensachkunde.trainer.ui.common.McOptionRow
import com.waffensachkunde.trainer.ui.common.MnemonicSection
import com.waffensachkunde.trainer.ui.common.ResultBanner

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
    val shuffled = state.questions.getOrNull(state.currentIndex) ?: return
    val question = shuffled.question
    val answer = state.answers[state.currentIndex] ?: ExamAnswerState()
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
            Text(question.question, style = MaterialTheme.typography.titleLarge)

            if (question.type == QuestionType.MC) {
                Text(
                    "Mehrfachauswahl möglich - mindestens eine Antwort ist richtig.",
                    style = MaterialTheme.typography.bodySmall
                )
                shuffled.displayOptions.forEachIndexed { index, option ->
                    McOptionRow(
                        text = option,
                        checked = index in answer.selectedIndices,
                        correct = index in shuffled.correctDisplayIndices,
                        submitted = answer.submitted,
                        onToggle = { viewModel.toggleMcOption(index) }
                    )
                }
                if (!answer.submitted) {
                    Button(
                        onClick = viewModel::submitMcCurrent,
                        enabled = answer.selectedIndices.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Prüfen") }
                } else {
                    val allCorrect = answer.selectedIndices == shuffled.correctDisplayIndices
                    ResultBanner(correct = allCorrect)
                }
            } else {
                if (!answer.submitted) {
                    Button(onClick = viewModel::revealDirectCurrent, modifier = Modifier.fillMaxWidth()) {
                        Text("Auflösung anzeigen")
                    }
                } else {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Musterantwort", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(question.modelAnswer)
                        }
                    }
                    if (answer.selfCorrect == null) {
                        Text("Hattest du das richtig beantwortet?", style = MaterialTheme.typography.bodyMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(onClick = { viewModel.selfAssessCurrent(true) }, modifier = Modifier.weight(1f)) {
                                Text("Richtig")
                            }
                            OutlinedButton(onClick = { viewModel.selfAssessCurrent(false) }, modifier = Modifier.weight(1f)) {
                                Text("Falsch")
                            }
                        }
                    } else {
                        ResultBanner(correct = answer.selfCorrect)
                    }
                }
            }

            val graded = if (question.type == QuestionType.MC) answer.submitted else answer.selfCorrect != null
            if (graded) {
                MnemonicSection(
                    revealed = answer.mnemonicRevealed,
                    mnemonic = question.mnemonic,
                    onToggle = viewModel::toggleMnemonicCurrent
                )
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
                Text("Prüfung jetzt abgeben")
            }
        }
    }

    if (showSubmitConfirm) {
        val unresolved = viewModel.unresolvedCount()
        AlertDialog(
            onDismissRequest = { showSubmitConfirm = false },
            title = { Text("Prüfung abgeben?") },
            text = {
                Text(if (unresolved > 0) "Du hast noch $unresolved unbearbeitete Fragen. Trotzdem abgeben?" else "Möchtest du die Prüfung jetzt abgeben?")
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
