package com.waffensachkunde.trainer.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

@Composable
fun McOptionRow(
    text: String,
    checked: Boolean,
    correct: Boolean,
    submitted: Boolean,
    onToggle: () -> Unit
) {
    val containerColor = when {
        !submitted -> MaterialTheme.colorScheme.surface
        correct -> Color(0xFF2E7D32)
        checked && !correct -> Color(0xFFC62828)
        else -> MaterialTheme.colorScheme.surface
    }
    val contentColor = if (submitted && (correct || checked)) Color.White else MaterialTheme.colorScheme.onSurface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !submitted, onClick = onToggle),
        colors = CardDefaults.cardColors(containerColor = containerColor, contentColor = contentColor)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(checked = checked, onCheckedChange = { onToggle() }, enabled = !submitted)
            Text(text, modifier = Modifier.weight(1f))
            if (submitted && correct) {
                Icon(Icons.Filled.Check, contentDescription = "Richtig", tint = contentColor)
            } else if (submitted && checked && !correct) {
                Icon(Icons.Filled.Close, contentDescription = "Falsch", tint = contentColor)
            }
        }
    }
}

@Composable
fun ResultBanner(correct: Boolean) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (correct) Color(0xFF2E7D32) else Color(0xFFC62828)
        )
    ) {
        Text(
            if (correct) "Richtig!" else "Leider falsch.",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun MnemonicSection(
    revealed: Boolean,
    mnemonic: String,
    onToggle: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(onClick = onToggle) {
            Text(if (revealed) "Eselsbrücke ausblenden" else "Eselsbrücke anzeigen")
        }
        if (revealed && mnemonic.isNotBlank()) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Eselsbrücke", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(mnemonic)
                }
            }
        }
    }
}
