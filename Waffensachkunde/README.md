# Waffensachkunde Trainer

Android-App (Kotlin, Jetpack Compose) zum Lernen für die Sachkundeprüfung nach
**§ 7 Waffengesetz (WaffG)** im Quizformat, mit Lernmodus nach Themengebieten
und einer Prüfungssimulation.

## Funktionen

- **Lernmodus**: 19 Themengebiete (Waffenrecht und Waffentechnik), jede Frage
  mit vier Antwortoptionen, sofortiger Rückmeldung und Erklärung samt
  Rechtsgrundlage.
- **Prüfungssimulation**: 100 Fragen aus dem gesamten Fragenpool, 60 Minuten
  Zeit, mindestens 75 % richtige Antworten zum Bestehen — angelehnt an den
  Ablauf der echten Sachkundeprüfung. Während der Prüfung gibt es (wie im
  echten Ablauf) keine sofortige Rückmeldung pro Frage; am Ende folgt eine
  Auswertung mit Liste der falsch beantworteten Fragen samt Erklärung.
- **Lesezeichen**: Fragen im Lernmodus markieren und später gesammelt üben.
- **Statistik**: Fortschritt und Trefferquote je Themengebiet, Verlauf aller
  Prüfungssimulationen, Zurücksetzen des Fortschritts.
- Läuft vollständig offline, keine Internetberechtigung erforderlich.

## Wichtiger Hinweis zum Fragenkatalog

Der amtliche Fragenkatalog zur Sachkundeprüfung nach § 7 WaffG wird vom
**Bundesverwaltungsamt (BVA)** herausgegeben und in regelmäßigen Abständen
aktualisiert (öffentlich einsehbar auf bva.bund.de). Aus dieser
Entwicklungsumgebung heraus war ein automatisierter Abruf der aktuellen
amtlichen PDF-Fassung nicht möglich (die Zielseiten blockieren automatisierte
Anfragen). Die in `app/src/main/assets/questions.json` enthaltenen 133 Fragen
sind daher **eigenständig formulierte Übungsfragen**, die sich inhaltlich und
strukturell an den Themengebieten des amtlichen Katalogs orientieren
(waffenrechtliche und waffentechnische Kenntnisse gemäß §§ 1, 2 WaffG und
AWaffV) — **kein wortgleicher Abdruck** des amtlichen Katalogs.

Für eine 1:1-Abdeckung des aktuellen amtlichen Wortlauts empfiehlt es sich,
den jeweils aktuellen Fragenkatalog direkt bei bva.bund.de zu laden und die
Fragen im unten beschriebenen JSON-Format zu ergänzen oder zu ersetzen.
Alle Inhalte der App dienen ausschließlich der Lernunterstützung und
ersetzen keine Rechtsberatung; maßgeblich sind stets die aktuellen
gesetzlichen Vorschriften und der amtliche Fragenkatalog.

### Fragenkatalog aktualisieren

Die Datei `app/src/main/assets/questions.json` hat folgenden Aufbau:

```json
{
  "categories": [
    { "id": "wr-begriffe", "name": "Waffenrechtliche Begriffe", "section": "Waffenrecht" }
  ],
  "questions": [
    {
      "id": "wr-begriffe-1",
      "categoryId": "wr-begriffe",
      "question": "Fragetext",
      "options": ["richtige Antwort", "falsch 1", "falsch 2", "falsch 3"],
      "correctIndex": 0,
      "explanation": "Erklärung der richtigen Antwort",
      "reference": "§ 1 WaffG"
    }
  ]
}
```

Die erste Option (`correctIndex`) muss die richtige Antwort sein; die App
mischt die Reihenfolge der Antwortoptionen bei jeder Anzeige zufällig. Neue
oder aktualisierte Fragen können einfach ergänzt bzw. ersetzt werden, ohne
Code ändern zu müssen.

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
