package com.waffensachkunde.trainer.ui.bookmarks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun BookmarksScreen(
    onStartBookmarkQuiz: () -> Unit,
    viewModel: BookmarksViewModel = viewModel()
) {
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Lesezeichen") }) }
    ) { padding ->
        if (bookmarks.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
                Text("Noch keine Fragen mit einem Lesezeichen versehen. Tippe im Lernmodus auf das Lesezeichen-Symbol, um Fragen hier zu sammeln.")
            }
            return@Scaffold
        }
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.padding(padding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Button(onClick = onStartBookmarkQuiz, modifier = Modifier.fillMaxWidth()) {
                    Text("Lesezeichen üben (${bookmarks.size})")
                }
            }
            items(bookmarks, key = { it.id }) { question ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(question.question, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
