/*
 * adblock.js — Spotilol (Firebase-free rebuild)
 *
 * Client-side (DOM/media) audio-ad mitigation for the Spotify Web Player.
 *
 * This is the "normal mode" ad handling: it needs no local proxy and no custom
 * CA certificate. It works purely inside the WebView by:
 *   1. Detecting when the currently playing item is an advertisement.
 *   2. Muting every media element while the ad plays (so nothing is audible).
 *   3. Attempting to fast-forward / skip the ad item.
 *
 * The stronger "proxy MITM" mode of the original (block ad requests at the
 * network layer) is documented in README.md but not enabled here, because it
 * requires installing a user CA certificate. This DOM approach is the safe,
 * dependency-free default.
 *
 * Selectors and heuristics may need updating as Spotify changes its player.
 */
(function () {
    "use strict";

    if (window.__spotilolAdblock) return;
    window.__spotilolAdblock = true;

    function isAdShowing() {
        // Heuristic 1: now-playing widget announces an advertisement.
        var widget = document.querySelector('[data-testid="now-playing-widget"]');
        if (widget) {
            var label = (widget.getAttribute("aria-label") || "").toLowerCase();
            if (label.indexOf("advertisement") !== -1 || label.indexOf("werbung") !== -1) return true;
        }
        // Heuristic 2: an explicit "Advertisement" text badge in the player bar.
        var badges = document.querySelectorAll('a[href*="/ad"], [data-testid="context-item-info-ad"]');
        if (badges.length > 0) return true;
        // Heuristic 3: the visible title reads as an ad marker.
        var title = document.querySelector('[data-testid="context-item-info-title"]');
        if (title) {
            var t = (title.textContent || "").trim().toLowerCase();
            if (t === "advertisement" || t === "spotify" && !title.querySelector("a")) return true;
        }
        return false;
    }

    function setMuted(muted) {
        var media = document.querySelectorAll("audio, video");
        for (var i = 0; i < media.length; i++) {
            try { media[i].muted = muted; } catch (e) { /* ignore */ }
        }
    }

    function trySkip() {
        // Fast-forward the ad element to its end so playback moves on quickly.
        var media = document.querySelectorAll("audio, video");
        for (var i = 0; i < media.length; i++) {
            try {
                var m = media[i];
                if (m.duration && isFinite(m.duration)) {
                    m.currentTime = Math.max(0, m.duration - 0.1);
                }
            } catch (e) { /* ignore */ }
        }
    }

    var wasAd = false;
    function tick() {
        var ad = isAdShowing();
        if (ad) {
            setMuted(true);
            trySkip();
            wasAd = true;
        } else if (wasAd) {
            // Ad finished — restore audio exactly once.
            setMuted(false);
            wasAd = false;
        }
    }

    setInterval(tick, 500);

    try {
        var obs = new MutationObserver(tick);
        obs.observe(document.body, { subtree: true, childList: true, attributes: true });
    } catch (e) { /* ignore */ }
})();
