# -*- coding: utf-8 -*-
"""
Instagram-Reel Generator  –  stilles Vorher/Nachher-MP4 (kein Audio)
====================================================================

Erzeugt ein fertiges Reel als H.264-MP4:
  * 9:16 vertikal, 1080 x 1920 Pixel
  * 30 fps
  * ca. 9 Sekunden
  * KEIN Ton (Sound wird spaeter direkt in Instagram hinzugefuegt)

Technik:
  * moviepy    -> Video-Compositing / MP4-Export (libx264)
  * Pillow     -> Text-Overlays werden als transparente PNGs gerendert,
                  damit deutsche Umlaute (ä ö ü ß) und eine schoene
                  fette Schrift sauber und randscharf erscheinen.

Bilder werden per Center-Crop ("cover") auf 1080x1920 eingepasst –
nichts wird verzerrt, es wird nur der ueberstehende Rand beschnitten.

Aufruf:   python3 reel_generator.py
"""

import os
import numpy as np
from PIL import Image, ImageDraw, ImageFont, ImageFilter

from moviepy import ImageClip, CompositeVideoClip, ColorClip
from moviepy.video.fx import CrossFadeIn


# ============================================================================
#  1) BASIS-EINSTELLUNGEN
# ============================================================================
BASE_DIR   = os.path.dirname(os.path.abspath(__file__))

# --- Eingabebilder -----------------------------------------------------------
VORHER_IMG  = os.path.join(BASE_DIR, "assets", "vorher_718.jpg")    # ...718.jpg
NACHHER_IMG = os.path.join(BASE_DIR, "assets", "nachher_569.png")   # ...569.png

# --- Ausgabe -----------------------------------------------------------------
OUTPUT_MP4  = os.path.join(BASE_DIR, "reel_output.mp4")

# --- Video-Format ------------------------------------------------------------
WIDTH   = 1080
HEIGHT  = 1920
FPS     = 30


# ============================================================================
#  2) ZEITEN  (alles in Sekunden – hier bequem anpassen!)
# ============================================================================
# Gesamtlaenge des Reels
TOTAL_DURATION = 9.0

# Wie lange ueberblendet das NACHHER-Bild ins VORHER-Bild (Crossfade)?
CROSSFADE = 0.6

# --- VORHER-Bild -------------------------------------------------------------
VORHER_START = 0.0                      # ab wann sichtbar
VORHER_END   = 4.5                      # bis wann sichtbar

# --- NACHHER-Bild ------------------------------------------------------------
# Beginnt etwas frueher (= CROSSFADE), damit es weich ueberblendet.
NACHHER_START = VORHER_END - CROSSFADE  # = 3.9
NACHHER_END   = TOTAL_DURATION          # bis zum Schluss

# --- Text "VORHER" -----------------------------------------------------------
LABEL_VORHER_START    = 0.35            # erscheint
LABEL_VORHER_END      = 4.30            # verschwindet
LABEL_VORHER_FADEIN   = 0.35
LABEL_VORHER_FADEOUT  = 0.30

# --- Text "NACHHER" ----------------------------------------------------------
LABEL_NACHHER_START   = 4.55            # erscheint (kurz nach dem Wechsel)
LABEL_NACHHER_END     = TOTAL_DURATION
LABEL_NACHHER_FADEIN  = 0.35
LABEL_NACHHER_FADEOUT = 0.0            # 0 = bleibt bis zum Ende stehen

# --- Text Ueberschrift (oben, ueber beiden Bildern) --------------------------
HEADLINE_START   = 0.35
HEADLINE_END     = TOTAL_DURATION
HEADLINE_FADEIN  = 0.4
HEADLINE_FADEOUT = 0.0

# --- Text Call-to-Action (unten, nur am Ende) --------------------------------
CTA_START   = 5.2
CTA_END     = TOTAL_DURATION
CTA_FADEIN  = 0.5
CTA_FADEOUT = 0.0


# ============================================================================
#  3) TEXT-INHALTE  (Umlaute sind hier ausdruecklich erlaubt)
# ============================================================================
TXT_HEADLINE = "Aus Handyfoto wird Profifoto"
TXT_VORHER   = "VORHER"
TXT_NACHHER  = "NACHHER"
TXT_CTA      = "Jetzt Produktfotos sichern"


# ============================================================================
#  4) DESIGN / SCHRIFT
# ============================================================================
# Fette Schrift mit vollstaendiger Umlaut-Unterstuetzung.
FONT_CANDIDATES = [
    "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf",
    "/usr/share/fonts/truetype/liberation/LiberationSans-Bold.ttf",
    "/usr/share/fonts/truetype/freefont/FreeSansBold.ttf",
]

# Schriftgroessen (Pixel)
SIZE_HEADLINE = 66
SIZE_LABEL    = 92
SIZE_CTA      = 60

# Maximale Textbreite (Pixel) – laengere Texte werden automatisch verkleinert,
# damit nie etwas am Bildrand abgeschnitten wird (90 % von 1080).
MAX_TEXT_WIDTH = 972

# Farben (R, G, B)
COLOR_TEXT        = (255, 255, 255)     # weisser Text
COLOR_STROKE      = (0, 0, 0)           # schwarzer Rand um den Text
COLOR_BADGE_VOR   = (200, 40, 40)       # rote Pille fuer VORHER
COLOR_BADGE_NACH  = (34, 150, 74)       # gruene Pille fuer NACHHER
COLOR_PILL_DARK   = (0, 0, 0)           # dunkle Pille fuer Headline/CTA


def _load_font(size):
    """Laedt die erste verfuegbare fette Schrift."""
    for path in FONT_CANDIDATES:
        if os.path.exists(path):
            return ImageFont.truetype(path, size)
    # Notfall (sollte nie noetig sein)
    return ImageFont.load_default()


# ============================================================================
#  5) BILD -> 1080x1920 (Center-Crop / "cover", kein Verzerren)
# ============================================================================
def fit_cover(path, target_w=WIDTH, target_h=HEIGHT):
    """
    Skaliert das Bild proportional so, dass es den 1080x1920-Rahmen
    komplett fuellt, und schneidet den Ueberstand mittig weg.
    Gibt ein numpy-RGB-Array (H, W, 3) zurueck.
    """
    img = Image.open(path).convert("RGB")
    src_w, src_h = img.size

    scale = max(target_w / src_w, target_h / src_h)
    new_w = round(src_w * scale)
    new_h = round(src_h * scale)
    img = img.resize((new_w, new_h), Image.LANCZOS)

    left = (new_w - target_w) // 2
    top  = (new_h - target_h) // 2
    img = img.crop((left, top, left + target_w, top + target_h))
    return np.array(img)


# ============================================================================
#  6) TEXT -> transparentes PNG (Pillow) -> numpy-RGBA-Array
# ============================================================================
def render_text_png(
    text,
    font_size,
    text_color=COLOR_TEXT,
    stroke_color=COLOR_STROKE,
    stroke_width=4,
    pill_color=None,
    pill_alpha=150,
    pad_x=48,
    pad_y=26,
    radius=None,
    shadow=True,
    max_width=None,
):
    """
    Rendert 'text' als transparentes RGBA-Bild.
    Optional mit halbtransparenter, abgerundeter "Pille" als Hintergrund
    und weichem Schlagschatten -> gute Lesbarkeit auf jedem Motiv.

    max_width: Wenn gesetzt, wird die Schrift automatisch verkleinert,
               bis der gesamte Text (inkl. Pille/Rand) hineinpasst –
               so wird nie etwas am Bildrand abgeschnitten.

    Rueckgabe: numpy-Array (H, W, 4), uint8.
    """
    tmp = Image.new("RGBA", (10, 10), (0, 0, 0, 0))
    d = ImageDraw.Draw(tmp)

    # --- Schrift automatisch an max_width anpassen ---------------------------
    font = _load_font(font_size)
    if max_width is not None:
        size = font_size
        while size > 12:
            font = _load_font(size)
            bbox = d.textbbox((0, 0), text, font=font, stroke_width=stroke_width)
            if (bbox[2] - bbox[0]) + pad_x * 2 <= max_width:
                break
            size -= 2

    # Textgroesse messen
    bbox = d.textbbox((0, 0), text, font=font, stroke_width=stroke_width)
    tw = bbox[2] - bbox[0]
    th = bbox[3] - bbox[1]

    margin = 40  # Platz fuer Schatten / Rand
    canvas_w = tw + pad_x * 2 + margin * 2
    canvas_h = th + pad_y * 2 + margin * 2

    canvas = Image.new("RGBA", (canvas_w, canvas_h), (0, 0, 0, 0))
    draw = ImageDraw.Draw(canvas)

    # --- Hintergrund-Pille ---------------------------------------------------
    if pill_color is not None:
        if radius is None:
            radius = (th + pad_y * 2) // 2
        pill_box = [
            margin, margin,
            canvas_w - margin, canvas_h - margin,
        ]
        pill = Image.new("RGBA", (canvas_w, canvas_h), (0, 0, 0, 0))
        pdraw = ImageDraw.Draw(pill)
        pdraw.rounded_rectangle(
            pill_box, radius=radius,
            fill=(pill_color[0], pill_color[1], pill_color[2], pill_alpha),
        )
        canvas = Image.alpha_composite(canvas, pill)
        draw = ImageDraw.Draw(canvas)

    # --- Schlagschatten (weich) ----------------------------------------------
    if shadow:
        shadow_layer = Image.new("RGBA", (canvas_w, canvas_h), (0, 0, 0, 0))
        sdraw = ImageDraw.Draw(shadow_layer)
        sx = margin + pad_x - bbox[0] + 3
        sy = margin + pad_y - bbox[1] + 3
        sdraw.text(
            (sx, sy), text, font=font,
            fill=(0, 0, 0, 180),
            stroke_width=stroke_width, stroke_fill=(0, 0, 0, 180),
        )
        shadow_layer = shadow_layer.filter(ImageFilter.GaussianBlur(6))
        canvas = Image.alpha_composite(canvas, shadow_layer)
        draw = ImageDraw.Draw(canvas)

    # --- eigentlicher Text ---------------------------------------------------
    tx = margin + pad_x - bbox[0]
    ty = margin + pad_y - bbox[1]
    draw.text(
        (tx, ty), text, font=font,
        fill=text_color,
        stroke_width=stroke_width, stroke_fill=stroke_color,
    )

    return np.array(canvas)


# ============================================================================
#  7) HILFSFUNKTION: Text-Clip mit Zeit, Position, Fade
# ============================================================================
def text_clip(png_array, start, end, position, fadein=0.0, fadeout=0.0):
    """Baut aus einem RGBA-Array einen zeitlich/positionierten ImageClip."""
    dur = max(0.0, end - start)
    clip = (
        ImageClip(png_array, transparent=True)
        .with_start(start)
        .with_duration(dur)
        .with_position(position)
    )
    from moviepy.video.fx import FadeIn, FadeOut
    fx = []
    if fadein and fadein > 0:
        fx.append(FadeIn(fadein))
    if fadeout and fadeout > 0:
        fx.append(FadeOut(fadeout))
    if fx:
        clip = clip.with_effects(fx)
    return clip


# ============================================================================
#  8) REEL ZUSAMMENBAUEN
# ============================================================================
def build_reel():
    print(">> Bilder werden auf 1080x1920 eingepasst (Center-Crop) ...")
    vorher_arr  = fit_cover(VORHER_IMG)
    nachher_arr = fit_cover(NACHHER_IMG)

    # Schwarzer Hintergrund als Sicherheitsnetz (falls Ueberblendung Luecken laesst)
    background = ColorClip(size=(WIDTH, HEIGHT), color=(0, 0, 0)).with_duration(TOTAL_DURATION)

    # --- VORHER-Bild ---------------------------------------------------------
    vorher_clip = (
        ImageClip(vorher_arr)
        .with_start(VORHER_START)
        .with_duration(VORHER_END - VORHER_START)
        .with_position("center")
    )

    # --- NACHHER-Bild (blendet weich ueber VORHER) ---------------------------
    nachher_clip = (
        ImageClip(nachher_arr)
        .with_start(NACHHER_START)
        .with_duration(NACHHER_END - NACHHER_START)
        .with_position("center")
        .with_effects([CrossFadeIn(CROSSFADE)])
    )

    print(">> Text-Overlays werden mit Pillow gerendert ...")
    # Headline oben
    headline_png = render_text_png(
        TXT_HEADLINE, SIZE_HEADLINE,
        pill_color=COLOR_PILL_DARK, pill_alpha=140, stroke_width=3,
        max_width=MAX_TEXT_WIDTH,
    )
    # VORHER-Badge
    vorher_png = render_text_png(
        TXT_VORHER, SIZE_LABEL,
        pill_color=COLOR_BADGE_VOR, pill_alpha=210, stroke_width=3,
        pad_x=60, pad_y=24,
    )
    # NACHHER-Badge
    nachher_png = render_text_png(
        TXT_NACHHER, SIZE_LABEL,
        pill_color=COLOR_BADGE_NACH, pill_alpha=210, stroke_width=3,
        pad_x=60, pad_y=24,
    )
    # Call-to-Action unten
    cta_png = render_text_png(
        TXT_CTA, SIZE_CTA,
        pill_color=COLOR_PILL_DARK, pill_alpha=150, stroke_width=3,
        max_width=MAX_TEXT_WIDTH,
    )

    # --- Positionen (x zentriert, y in Pixeln von oben) ----------------------
    headline_clip = text_clip(
        headline_png, HEADLINE_START, HEADLINE_END,
        position=("center", 150),
        fadein=HEADLINE_FADEIN, fadeout=HEADLINE_FADEOUT,
    )
    vorher_label = text_clip(
        vorher_png, LABEL_VORHER_START, LABEL_VORHER_END,
        position=("center", 1500),
        fadein=LABEL_VORHER_FADEIN, fadeout=LABEL_VORHER_FADEOUT,
    )
    nachher_label = text_clip(
        nachher_png, LABEL_NACHHER_START, LABEL_NACHHER_END,
        position=("center", 1420),
        fadein=LABEL_NACHHER_FADEIN, fadeout=LABEL_NACHHER_FADEOUT,
    )
    cta_clip = text_clip(
        cta_png, CTA_START, CTA_END,
        position=("center", 1620),
        fadein=CTA_FADEIN, fadeout=CTA_FADEOUT,
    )

    print(">> Composite wird gebaut ...")
    final = CompositeVideoClip(
        [
            background,
            vorher_clip,
            nachher_clip,
            headline_clip,
            vorher_label,
            nachher_label,
            cta_clip,
        ],
        size=(WIDTH, HEIGHT),
    ).with_duration(TOTAL_DURATION)

    # Ganz sicher stumm
    final = final.without_audio()

    print(">> MP4 wird exportiert (H.264, kein Audio) ...")
    final.write_videofile(
        OUTPUT_MP4,
        fps=FPS,
        codec="libx264",
        audio=False,
        preset="medium",
        ffmpeg_params=["-pix_fmt", "yuv420p", "-movflags", "+faststart"],
    )
    print(f">> Fertig: {OUTPUT_MP4}")


if __name__ == "__main__":
    build_reel()
