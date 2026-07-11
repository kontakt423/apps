package com.waffensachkunde.trainer.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatsScreen(
    viewModel: StatsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showResetConfirm by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMANY) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Statistik") }) }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.padding(padding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Text("Themengebiete", style = MaterialTheme.typography.titleMedium) }
            items(state.categoryProgress, key = { it.categoryId }) { progress ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(progress.name, style = MaterialTheme.typography.titleSmall)
                        val fraction = if (progress.totalCount > 0) progress.answeredCount.toFloat() / progress.totalCount else 0f
                        LinearProgressIndicator(progress = { fraction }, modifier = Modifier.fillMaxWidth())
                        Text("${progress.answeredCount}/${progress.totalCount} geübt · Trefferquote ${progress.correctRate} %")
                    }
                }
            }

            item { Text("Prüfungsverlauf", style = MaterialTheme.typography.titleMedium) }
            if (state.examHistory.isEmpty()) {
                item { Text("Noch keine Prüfungssimulation absolviert.") }
            }
            items(state.examHistory, key = { it.id }) { attempt ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(dateFormat.format(Date(attempt.startedAt)))
                        Text("${attempt.correctCount}/${attempt.totalQuestions} richtig – ${if (attempt.passed) "bestanden" else "nicht bestanden"}")
                        Text("Dauer: ${attempt.durationSeconds / 60} Min.")
                    }
                }
            }

            item {
                OutlinedButton(onClick = { showResetConfirm = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Fortschritt zurücksetzen")
                }
            }
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Fortschritt zurücksetzen?") },
            text = { Text("Alle Statistiken, Lesezeichen und der Prüfungsverlauf werden unwiderruflich gelöscht.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetProgress()
                    showResetConfirm = false
                }) { Text("Zurücksetzen") }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) { Text("Abbrechen") }
            }
        )
    }
}
