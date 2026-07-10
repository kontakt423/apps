package com.waffensachkunde.trainer.ui.learn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.waffensachkunde.trainer.ui.stats.CategoryProgress
import com.waffensachkunde.trainer.ui.stats.StatsViewModel

@Composable
fun CategoryListScreen(
    onCategorySelected: (String) -> Unit,
    onLearnAll: () -> Unit,
    viewModel: StatsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Lernmodus") }) }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.padding(padding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedButton(onClick = onLearnAll, modifier = Modifier.fillMaxWidth()) {
                    Text("Alle Fragen gemischt üben")
                }
            }
            items(state.categoryProgress, key = { it.categoryId }) { progress ->
                CategoryRow(progress) { onCategorySelected(progress.categoryId) }
            }
        }
    }
}

@Composable
private fun CategoryRow(progress: CategoryProgress, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(progress.section, style = MaterialTheme.typography.labelSmall)
            Text(progress.name, style = MaterialTheme.typography.titleMedium)
            val fraction = if (progress.totalCount > 0) progress.answeredCount.toFloat() / progress.totalCount else 0f
            LinearProgressIndicator(progress = { fraction }, modifier = Modifier.fillMaxWidth())
            Text(
                "${progress.answeredCount}/${progress.totalCount} geübt · Trefferquote ${progress.correctRate} %",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Start
            )
        }
    }
}
