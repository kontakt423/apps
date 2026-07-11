package com.waffensachkunde.trainer.ui.exam

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.waffensachkunde.trainer.data.repository.QuestionRepository

@Composable
fun ExamIntroScreen(
    onStart: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Prüfungssimulation") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Ablauf der Simulation", style = MaterialTheme.typography.titleLarge)
            Text(
                "Diese Simulation orientiert sich am Ablauf der Sachkundeprüfung nach § 7 WaffG: " +
                    "${QuestionRepository.EXAM_QUESTION_COUNT} Fragen aus allen Themengebieten, " +
                    "${ExamViewModel.EXAM_DURATION_SECONDS / 60} Minuten Zeit, " +
                    "mindestens 75 % richtige Antworten zum Bestehen."
            )
            Text("Bei Multiple-Choice-Fragen wird dir direkt nach dem Prüfen angezeigt, ob deine Auswahl richtig war. Bei Fragen mit Musterantwort siehst du nach der Auflösung die amtliche Musterantwort und schätzt selbst ein, ob du richtig lagst. Du kannst zwischen Fragen wechseln und Antworten bis zur Abgabe ändern.")
            Text(
                "Hinweis: Die Fragen und Antworten stammen wortgetreu aus dem amtlichen Fragenkatalog für die Sachkundeprüfung (Stand 16.12.2024) des Bundesverwaltungsamtes. Die Eselsbrücken sind eigenständig erstellte Lernhilfen und nicht Teil des amtlichen Katalogs.",
                style = MaterialTheme.typography.bodySmall
            )
            Button(onClick = onStart) { Text("Prüfung starten") }
        }
    }
}
