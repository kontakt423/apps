package com.waffensachkunde.trainer.ui.exam

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ExamResultScreen(
    viewModel: ExamViewModel,
    onDone: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Ergebnis") }) }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.padding(padding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (state.passed) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            if (state.passed) "Bestanden" else "Nicht bestanden",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White
                        )
                        Text(
                            "${state.correctCount} von ${state.questions.size} richtig (mind. ${state.passThreshold} erforderlich)",
                            color = Color.White
                        )
                        if (state.unansweredCount > 0) {
                            Text("${state.unansweredCount} Fragen unbeantwortet", color = Color.White)
                        }
                    }
                }
            }

            if (state.wrongQuestions.isNotEmpty()) {
                item {
                    Text(
                        "Zum Nacharbeiten (${state.wrongQuestions.size})",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(state.wrongQuestions, key = { it.id }) { question ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(question.question, fontWeight = FontWeight.Bold)
                            Text("Richtig: ${question.options[question.correctIndex]}")
                            Text(question.explanation, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            item {
                Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) { Text("Zur Startseite") }
            }
        }
    }
}
