package com.spotilol.app.webview

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.webkit.WebView
import java.io.BufferedReader

/**
 * Configured WebView that hosts the Spotify Web Player and injects Spotilol's
 * helper scripts (control bridge + client-side ad handling).
 */
class SpotiWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : WebView(context, attrs) {

    @SuppressLint("SetJavaScriptEnabled")
    fun configure() {
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            mediaPlaybackRequiresUserGesture = false
            cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
            // Use an up-to-date desktop-ish UA so Spotify serves the full player.
            userAgentString = DESKTOP_UA
            // Fit the wide desktop layout to the screen, and allow pinch-zoom so
            // the smaller desktop UI stays readable on a phone.
            loadWithOverviewMode = true
            useWideViewPort = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
        }
    }

    fun loadPlayer() = loadUrl(PLAYER_URL)

    /** Read a script from assets and evaluate it in the page. */
    fun injectAsset(name: String) {
        val js = readAsset(name) ?: return
        evaluateJavascript(js, null)
    }

    fun runControl(fn: String) = evaluateJavascript("try{window.SpotilolControls&&SpotilolControls.$fn;}catch(e){}", null)

    fun applyAmoled(enabled: Boolean) =
        evaluateJavascript("try{window.SpotilolTheme&&SpotilolTheme.applyAmoled($enabled);}catch(e){}", null)

    private fun readAsset(name: String): String? = try {
        context.assets.open(name).bufferedReader().use(BufferedReader::readText)
    } catch (e: Exception) {
        null
    }

    companion object {
        const val PLAYER_URL = "https://open.spotify.com/"

        // A pure DESKTOP user agent (Windows/Chrome, no "Android"/"Mobile") makes
        // Spotify serve the full PC web player, which offers real previous-track
        // skipping and the complete desktop control set — as if streaming from a PC.
        private const val DESKTOP_UA =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
    }
}
