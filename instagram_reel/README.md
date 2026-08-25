# Instagram-Reel Generator (Vorher/Nachher, stilles MP4)

Erzeugt ein fertiges Reel als **H.264-MP4** – 9:16, **1080×1920**, **30 fps**,
ca. **9 Sekunden**, **kein Audio** (Ton kommt später direkt in Instagram dazu).

## Verwenden
```bash
pip install moviepy Pillow imageio-ffmpeg
python3 reel_generator.py
```
Ergebnis: `reel_output.mp4`

## Anpassen
Ganz oben im Skript `reel_generator.py`:
- **Abschnitt 1** – Ein-/Ausgabepfade, Format (Breite/Höhe/FPS)
- **Abschnitt 2 (ZEITEN)** – alle Ein-/Ausblendzeiten als Variablen (Sekunden)
- **Abschnitt 3 (TEXT-INHALTE)** – Überschrift, Vorher/Nachher, Call-to-Action
- **Abschnitt 4 (DESIGN)** – Schriftgrößen, Farben, max. Textbreite

## Technik
- **moviepy** baut das Composite und exportiert das MP4 (libx264, yuv420p, faststart).
- **Pillow** rendert alle Texte als transparente PNGs – so kommen Umlaute (ä ö ü ß)
  und eine fette Schrift randscharf; lange Texte werden automatisch verkleinert,
  damit nie etwas am Bildrand abgeschnitten wird.
- Bilder werden per **Center-Crop ("cover")** ohne Verzerren auf 1080×1920 eingepasst.
