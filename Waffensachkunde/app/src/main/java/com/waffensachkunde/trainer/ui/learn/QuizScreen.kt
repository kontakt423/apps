package com.waffensachkunde.trainer.ui.learn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.waffensachkunde.trainer.data.model.QuestionType
import com.waffensachkunde.trainer.ui.common.McOptionRow
import com.waffensachkunde.trainer.ui.common.MnemonicSection
import com.waffensachkunde.trainer.ui.common.ResultBanner

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
                onNextRound = viewModel::nextRound,
                modifier = Modifier.padding(padding)
            )
            state.current != null -> QuizQuestionContent(
                state = state,
                onToggleOption = viewModel::toggleOption,
                onSubmitMc = viewModel::submitMc,
                onRevealDirect = viewModel::revealDirect,
                onSelfAssess = viewModel::selfAssess,
                onToggleMnemonic = viewModel::toggleMnemonic,
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
    onToggleOption: (Int) -> Unit,
    onSubmitMc: () -> Unit,
    onRevealDirect: () -> Unit,
    onSelfAssess: (Boolean) -> Unit,
    onToggleMnemonic: () -> Unit,
    onNext: () -> Unit,
    onToggleBookmark: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shuffled = state.current ?: return
    val question = shuffled.question
    val graded = if (question.type == QuestionType.MC) state.submitted else state.selfAssessedCorrect != null

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

        Box(Modifier.fillMaxWidth()) {
            Text(
                question.question,
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

        if (question.type == QuestionType.MC) {
            Text(
                "Mehrfachauswahl möglich - mindestens eine Antwort ist richtig.",
                style = MaterialTheme.typography.bodySmall
            )
            shuffled.displayOptions.forEachIndexed { index, option ->
                McOptionRow(
                    text = option,
                    checked = index in state.selectedIndices,
                    correct = index in shuffled.correctDisplayIndices,
                    submitted = state.submitted,
                    onToggle = { onToggleOption(index) }
                )
            }
            if (!state.submitted) {
                Button(
                    onClick = onSubmitMc,
                    enabled = state.selectedIndices.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Prüfen") }
            } else {
                val allCorrect = state.selectedIndices == shuffled.correctDisplayIndices
                ResultBanner(correct = allCorrect)
            }
        } else {
            if (!state.submitted) {
                Button(onClick = onRevealDirect, modifier = Modifier.fillMaxWidth()) {
                    Text("Auflösung anzeigen")
                }
            } else {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Musterantwort", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(question.modelAnswer)
                    }
                }
                if (state.selfAssessedCorrect == null) {
                    Text("Hattest du das richtig beantwortet?", style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = { onSelfAssess(true) }, modifier = Modifier.weight(1f)) {
                            Text("Richtig")
                        }
                        OutlinedButton(onClick = { onSelfAssess(false) }, modifier = Modifier.weight(1f)) {
                            Text("Falsch")
                        }
                    }
                } else {
                    ResultBanner(correct = state.selfAssessedCorrect)
                }
            }
        }

        if (graded) {
            MnemonicSection(
                revealed = state.mnemonicRevealed,
                mnemonic = question.mnemonic,
                onToggle = onToggleMnemonic
            )
            Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
                Text(if (state.currentIndex + 1 >= state.questions.size) "Fertig" else "Weiter")
            }
        }
    }
}

@Composable
private fun QuizFinishedContent(
    correct: Int,
    total: Int,
    onBack: () -> Unit,
    onNextRound: () -> Unit,
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
        Button(onClick = onNextRound, modifier = Modifier.fillMaxWidth()) { Text("Nächste Runde (10 Fragen)") }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Zurück") }
    }
}
