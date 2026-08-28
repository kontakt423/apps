/*
 * inject.js — Spotilol (Firebase-free rebuild)
 *
 * Runs inside the Spotify Web Player WebView. Responsibilities:
 *   1. Bridge the web player's state to the native Android MediaSession
 *      (title / artist / artwork / playing / position / duration).
 *   2. Expose control functions that native code can call via
 *      WebView.evaluateJavascript(...) — play, pause, next, prev, seek.
 *   3. Apply mobile-friendly / AMOLED cosmetic tweaks.
 *
 * NOTE: Spotify frequently changes its DOM. All selectors are centralised in
 * SEL below so they are easy to update in one place. Everything is best-effort
 * and fails silently if a node is missing.
 */
(function () {
    "use strict";

    if (window.__spotilolInjected) return;
    window.__spotilolInjected = true;

    var SEL = {
        playPause: '[data-testid="control-button-playpause"]',
        next: '[data-testid="control-button-skip-forward"]',
        prev: '[data-testid="control-button-skip-back"]',
        title: '[data-testid="context-item-info-title"], [data-testid="context-item-link"]',
        artist: '[data-testid="context-item-info-subtitles"] a, [data-testid="context-item-artist"]',
        artwork: '[data-testid="now-playing-widget"] img, .cover-art img, [data-testid="cover-art-image"]',
        widget: '[data-testid="now-playing-widget"]',
        posLabel: '[data-testid="playback-position"]',
        durLabel: '[data-testid="playback-duration"]'
    };

    function q(sel) { try { return document.querySelector(sel); } catch (e) { return null; } }
    function text(sel) { var n = q(sel); return n ? (n.textContent || "").trim() : ""; }

    function timeToSeconds(t) {
        if (!t) return 0;
        var parts = t.split(":").map(function (p) { return parseInt(p, 10) || 0; });
        if (parts.length === 3) return parts[0] * 3600 + parts[1] * 60 + parts[2];
        if (parts.length === 2) return parts[0] * 60 + parts[1];
        return parts[0] || 0;
    }

    function isPlaying() {
        var btn = q(SEL.playPause);
        if (!btn) return false;
        var label = (btn.getAttribute("aria-label") || "").toLowerCase();
        // When a track plays the button offers "Pause".
        return label.indexOf("pause") !== -1;
    }

    function readState() {
        var art = q(SEL.artwork);
        return {
            title: text(SEL.title),
            artist: text(SEL.artist),
            artwork: art ? (art.getAttribute("src") || "") : "",
            playing: isPlaying(),
            position: timeToSeconds(text(SEL.posLabel)),
            duration: timeToSeconds(text(SEL.durLabel))
        };
    }

    // --- Native control surface --------------------------------------------
    // Called from Android via evaluateJavascript("SpotilolControls.xxx()").
    window.SpotilolControls = {
        toggle: function () { var b = q(SEL.playPause); if (b) b.click(); },
        play: function () { if (!isPlaying()) { var b = q(SEL.playPause); if (b) b.click(); } },
        pause: function () { if (isPlaying()) { var b = q(SEL.playPause); if (b) b.click(); } },
        next: function () { var b = q(SEL.next); if (b) b.click(); },
        prev: function () { var b = q(SEL.prev); if (b) b.click(); },
        state: function () { return JSON.stringify(readState()); }
    };

    // --- Push state to native on change ------------------------------------
    var lastSerialized = "";
    function pushState() {
        try {
            var s = readState();
            var serialized = JSON.stringify(s);
            if (serialized !== lastSerialized && window.SpotilolBridge) {
                lastSerialized = serialized;
                window.SpotilolBridge.onPlayerState(serialized);
            }
        } catch (e) { /* ignore */ }
    }

    // Poll (cheap) + observe DOM mutations for immediate updates.
    setInterval(pushState, 1000);
    try {
        var obs = new MutationObserver(function () { pushState(); });
        var w = q(SEL.widget) || document.body;
        obs.observe(w, { subtree: true, childList: true, attributes: true });
    } catch (e) { /* ignore */ }

    // --- Cosmetic tweaks ----------------------------------------------------
    window.SpotilolTheme = {
        applyAmoled: function (on) {
            var id = "spotilol-amoled";
            var existing = document.getElementById(id);
            if (existing) existing.remove();
            if (!on) return;
            var css = document.createElement("style");
            css.id = id;
            css.textContent =
                "html,body,.Root__main-view,.Root__top-container," +
                "[data-testid='root'],.main-view-container__scroll-node{" +
                "background:#000 !important;}";
            document.head.appendChild(css);
        }
    };

    // Signal readiness to native.
    if (window.SpotilolBridge) window.SpotilolBridge.onReady();
})();
