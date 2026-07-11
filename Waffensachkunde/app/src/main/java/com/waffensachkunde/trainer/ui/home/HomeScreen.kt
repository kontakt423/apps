package com.waffensachkunde.trainer.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HomeScreen(
    onNavigateLearn: () -> Unit,
    onNavigateExam: () -> Unit,
    onNavigateStats: () -> Unit,
    onNavigateBookmarks: () -> Unit,
    onNavigateInfo: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Waffensachkunde Trainer") })
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
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Dein Fortschritt", style = MaterialTheme.typography.titleMedium)
                    Text("${state.answeredQuestions} von ${state.totalQuestions} Fragen geübt")
                    Text("Trefferquote: ${state.correctRate} %")
                    if (state.examAttemptCount > 0) {
                        val statusText = if (state.lastExamPassed == true) "bestanden" else "nicht bestanden"
                        Text("Letzte Prüfungssimulation: ${state.lastExamScore} ($statusText)")
                    }
                }
            }

            HomeActionCard(
                icon = Icons.Filled.MenuBook,
                title = "Lernmodus",
                description = "Runden mit je 10 Fragen üben, inkl. Wiederholung häufiger Fehler.",
                onClick = onNavigateLearn
            )
            HomeActionCard(
                icon = Icons.Filled.Timer,
                title = "Prüfungssimulation",
                description = "100 Fragen, 60 Minuten, 75 richtige Antworten zum Bestehen.",
                onClick = onNavigateExam
            )
            HomeActionCard(
                icon = Icons.Filled.Bookmark,
                title = "Lesezeichen",
                description = "Gemerkte Fragen erneut üben.",
                onClick = onNavigateBookmarks
            )
            HomeActionCard(
                icon = Icons.Filled.QueryStats,
                title = "Statistik",
                description = "Fortschritt je Themengebiet und Prüfungsverlauf.",
                onClick = onNavigateStats
            )

            OutlinedButton(onClick = onNavigateInfo, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Info, contentDescription = null)
                Text("  Über diese App & Fragenkatalog")
            }
        }
    }
}

@Composable
private fun HomeActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(description, style = MaterialTheme.typography.bodySmall)
            }
        }
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Button(onClick = onClick) { Text("Starten") }
        }
    }
}
