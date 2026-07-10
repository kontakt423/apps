package com.waffensachkunde.trainer.ui.learn

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun QuizScreen(
    mode: QuizMode,
    categoryId: String?,
    onBack: () -> Unit,
    viewModel: QuizViewModel = viewModel()
) {
    LaunchedEffect(mode, categoryId) {
        viewModel.start(mode, categoryId)
    }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text(state.title.ifBlank { "Lernmodus" }) }) }
    ) { padding ->
        when {
            state.loading -> Box(Modifier.fillMaxSize().padding(padding))
            state.empty -> Box(Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
                Text("Keine Fragen in dieser Auswahl. Übe zunächst im Lernmodus oder markiere Fragen mit einem Lesezeichen.")
            }
            state.finished -> QuizFinishedContent(
                correct = state.sessionCorrect,
                total = state.questions.size,
                onBack = onBack,
                modifier = Modifier.padding(padding)
            )
            state.current != null -> QuizQuestionContent(
                state = state,
                onSelect = viewModel::selectAnswer,
                onNext = viewModel::next,
                onToggleBookmark = viewModel::toggleBookmark,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun QuizQuestionContent(
    state: QuizUiState,
    onSelect: (Int) -> Unit,
    onNext: () -> Unit,
    onToggleBookmark: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shuffled = state.current ?: return
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LinearProgressIndicator(
            progress = { (state.currentIndex + 1).toFloat() / state.questions.size },
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            "Frage ${state.currentIndex + 1} von ${state.questions.size}",
            style = MaterialTheme.typography.labelMedium
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(Modifier.fillMaxWidth()) {
                Text(
                    shuffled.question.question,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(end = 40.dp)
                )
                IconButton(onClick = onToggleBookmark, modifier = Modifier.align(Alignment.TopEnd)) {
                    Icon(
                        if (state.bookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                        contentDescription = "Lesezeichen"
                    )
                }
            }
        }

        shuffled.displayOptions.forEachIndexed { index, option ->
            AnswerOption(
                text = option,
                selected = state.selectedIndex == index,
                correct = index == shuffled.correctDisplayIndex,
                answered = state.answered,
                onClick = { onSelect(index) }
            )
        }

        if (state.answered) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Erklärung", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(shuffled.question.explanation)
                    if (shuffled.question.reference.isNotBlank()) {
                        Text(shuffled.question.reference, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
                Text(if (state.currentIndex + 1 >= state.questions.size) "Fertig" else "Weiter")
            }
        }
    }
}

@Composable
private fun AnswerOption(
    text: String,
    selected: Boolean,
    correct: Boolean,
    answered: Boolean,
    onClick: () -> Unit
) {
    val containerColor = when {
        !answered -> MaterialTheme.colorScheme.surface
        correct -> Color(0xFF2E7D32)
        selected && !correct -> Color(0xFFC62828)
        else -> MaterialTheme.colorScheme.surface
    }
    val contentColor = if (answered && (correct || selected)) Color.White else MaterialTheme.colorScheme.onSurface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !answered) { onClick() },
        colors = CardDefaults.cardColors(containerColor = containerColor, contentColor = contentColor)
    ) {
        Text(text, modifier = Modifier.padding(16.dp))
    }
}

@Composable
private fun QuizFinishedContent(
    correct: Int,
    total: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Runde abgeschlossen", style = MaterialTheme.typography.headlineSmall)
        Text("Du hast $correct von $total Fragen richtig beantwortet.")
        Button(onClick = onBack) { Text("Zurück") }
    }
}
