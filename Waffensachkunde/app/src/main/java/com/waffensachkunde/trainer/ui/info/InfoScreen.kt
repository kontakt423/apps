package com.waffensachkunde.trainer.ui.info

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun InfoScreen() {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Über diese App") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Zweck der App", style = MaterialTheme.typography.titleMedium)
            Text(
                "Diese App unterstützt die Vorbereitung auf die Sachkundeprüfung nach § 7 " +
                    "Waffengesetz (WaffG) im Quizformat, mit einem Lernmodus nach Themengebieten " +
                    "und einer Prüfungssimulation (100 Fragen, 60 Minuten, 75 % zum Bestehen)."
            )

            Text("Hinweis zum Fragenkatalog", style = MaterialTheme.typography.titleMedium)
            Text(
                "Die Fragen, Multiple-Choice-Antworten und Musterantworten dieser App sind wortgetreu " +
                    "aus dem amtlichen Fragenkatalog für die Sachkundeprüfung gemäß § 7 WaffG übernommen, " +
                    "herausgegeben vom Bundesverwaltungsamt (BVA) im Auftrag des Bundesministeriums des " +
                    "Innern und für Heimat (Stand: 16.12.2024). Die Eselsbrücken zu jeder Frage sind " +
                    "eigenständig erstellte Lernhilfen und nicht Teil des amtlichen Katalogs."
            )
            Text(
                "Für die verbindliche Prüfungsvorbereitung sollte zusätzlich der jeweils aktuelle " +
                    "amtliche Fragenkatalog des Bundesverwaltungsamtes herangezogen werden, da dieser " +
                    "regelmäßig aktualisiert wird."
            )

            Text("Rechtlicher Hinweis", style = MaterialTheme.typography.titleMedium)
            Text(
                "Alle Inhalte dienen ausschließlich der Lernunterstützung und ersetzen keine " +
                    "Rechtsberatung. Für die Richtigkeit und Vollständigkeit der Inhalte wird keine " +
                    "Gewähr übernommen; maßgeblich sind stets die aktuellen gesetzlichen Vorschriften " +
                    "und der amtliche Fragenkatalog."
            )
        }
    }
}
