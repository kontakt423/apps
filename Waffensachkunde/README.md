# Waffensachkunde Trainer

Android-App (Kotlin, Jetpack Compose) zum Lernen für die Sachkundeprüfung nach
**§ 7 Waffengesetz (WaffG)** im Quizformat, mit Lernmodus nach Themengebieten
und einer Prüfungssimulation.

## Fragenkatalog

Die App enthält **574 Fragen** aus dem amtlichen *Fragenkatalog für die
Sachkundeprüfung (gemäß § 7 WaffG)*, herausgegeben vom
**Bundesverwaltungsamt** im Auftrag des Bundesministeriums des Innern und für
Heimat (Stand: 16.12.2024). Fragen, Multiple-Choice-Antworten und
Musterantworten sind wortgetreu aus dem amtlichen Katalog übernommen.

| Kategorie | Bereich | Fragen |
| --- | --- | --- |
| Begriffe des Waffenrechts | Waffenrecht | 90 |
| Rechte und Pflichten | Waffenrecht | 154 |
| Kennzeichnung von Schusswaffen und Munition | Waffenrecht | 34 |
| Aufbewahrung von Schusswaffen und Munition | Waffenrecht | 23 |
| Notwehr und Notstand | Waffenrecht | 43 |
| Waffentechnik (Waffen, Munition, Geschosse) | Waffentechnik | 92 |
| Handhabung von Schusswaffen und Munition | Waffentechnik | 49 |
| Not- und Seenotsignalmittel | Sonstiges | 89 |

Eine Frage des amtlichen Katalogs (Nr. 3.05) wurde ausgeschlossen, da ihre
Antwortoptionen nur aus nicht darstellbaren Beschusszeichen-Grafiken ohne
Text bestehen und daher im Quizformat nicht sinnvoll nutzbar sind.

Jede Frage ist zusätzlich mit einer **Eselsbrücke** versehen — einer
eigenständig erstellten Lernhilfe, die *nicht* Teil des amtlichen Katalogs
ist.

Der amtliche Fragenkatalog wird vom Bundesverwaltungsamt in unregelmäßigen
Abständen aktualisiert. Für die verbindliche Prüfungsvorbereitung sollte
zusätzlich der jeweils aktuelle amtliche Fragenkatalog herangezogen werden.

## Funktionen

- **Lernmodus**: Fragen nach Themengebiet oder gemischt über alle 574 Fragen
  üben.
  - **Multiple-Choice-Fragen** (Mehrfachauswahl möglich, mindestens eine
    Antwort ist immer richtig): Nach Antippen von "Prüfen" wird sofort
    angezeigt, ob die Auswahl vollständig richtig war.
  - **Fragen mit Musterantwort**: Es wird zunächst nur die Frage gezeigt. Ein
    "Auflösung anzeigen"-Button blendet die amtliche Musterantwort ein.
    Danach schätzt man selbst ein, ob man richtig oder falsch geantwortet
    hat — diese Selbsteinschätzung zählt für das Ergebnis.
  - Nach jeder beantworteten Frage (unabhängig von richtig/falsch) kann über
    einen Toggle die **Eselsbrücke** eingeblendet werden.
  - Fragen können mit einem Lesezeichen markiert werden.
- **Prüfungssimulation**: 100 zufällig gezogene Fragen aus dem gesamten
  Fragenpool, 60 Minuten Zeit, mindestens 75 % richtige Antworten zum
  Bestehen — angelehnt an den Ablauf der echten Sachkundeprüfung. MC-Fragen
  werden direkt nach dem Prüfen ausgewertet, Fragen mit Musterantwort per
  Selbsteinschätzung wie im Lernmodus. Am Ende folgt eine Auswertung mit
  Liste der falsch beantworteten Fragen samt Musterantwort/richtiger
  Antwort und Eselsbrücke.
- **Lesezeichen**: Fragen im Lernmodus markieren und später gesammelt üben.
- **Statistik**: Fortschritt und Trefferquote je Themengebiet, Verlauf aller
  Prüfungssimulationen, Zurücksetzen des Fortschritts.
- Läuft vollständig offline, keine Internetberechtigung erforderlich.

### Fragenkatalog aktualisieren

Die Datei `app/src/main/assets/questions.json` hat folgenden Aufbau:

```json
{
  "catalogVersion": "amtlich-2024-12-16",
  "note": "...",
  "categories": [
    { "id": "kap1-begriffe", "name": "Begriffe des Waffenrechts", "section": "Waffenrecht" }
  ],
  "questions": [
    {
      "id": "1.01",
      "categoryId": "kap1-begriffe",
      "type": "direct",
      "question": "Was regelt das Waffengesetz?",
      "modelAnswer": "Das Waffengesetz regelt den Umgang mit Waffen oder Munition ...",
      "mnemonic": "WaffG = Waffen + Gesetz: ..."
    },
    {
      "id": "1.02",
      "categoryId": "kap1-begriffe",
      "type": "mc",
      "question": "Umgang mit einer Schusswaffe hat…",
      "options": ["wer damit schießt.", "wer die Waffe verbringt oder mitnimmt.", "..."],
      "correctIndices": [0, 1, 2],
      "mnemonic": "..."
    }
  ]
}
```

Fragen vom Typ `mc` haben `options` und `correctIndices` (mehrere korrekte
Indizes möglich); die App mischt die Reihenfolge der Antwortoptionen bei
jeder Anzeige zufällig. Fragen vom Typ `direct` haben stattdessen eine
`modelAnswer`. Neue oder aktualisierte Fragen können einfach ergänzt bzw.
ersetzt werden, ohne Code ändern zu müssen.

## Architektur

- **UI**: Jetpack Compose, Material 3, Navigation Compose, MVVM
  (`AndroidViewModel` pro Screen).
- **Daten**: Fragenkatalog wird beim Start aus dem Assets-JSON geladen
  (`CatalogLoader`, reines `org.json`, keine zusätzliche Serialisierungs-
  Bibliothek). Fortschritt, Lesezeichen und Prüfungsverlauf werden lokal in
  einer Room-Datenbank gespeichert.
- Kein Netzwerkzugriff, keine Werbung, keine Tracking-Bibliotheken.

## Build

```bash
./gradlew assembleDebug
```

Erfordert Android SDK (compileSdk/targetSdk 35, minSdk 26) und JDK 17+.
