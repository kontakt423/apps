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
                "Der amtliche Fragenkatalog zur Sachkundeprüfung wird vom Bundesverwaltungsamt (BVA) " +
                    "herausgegeben und regelmäßig aktualisiert. Die in dieser App enthaltenen Fragen " +
                    "sind eigenständig formulierte Übungsfragen, die sich an denselben Themengebieten " +
                    "orientieren (waffenrechtliche und waffentechnische Kenntnisse gemäß §§ 1, 2 WaffG " +
                    "und AWaffV). Es handelt sich nicht um einen wortgleichen Abdruck des amtlichen Katalogs."
            )
            Text(
                "Für die verbindliche Prüfungsvorbereitung sollte zusätzlich der jeweils aktuelle " +
                    "amtliche Fragenkatalog des Bundesverwaltungsamtes herangezogen werden."
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
