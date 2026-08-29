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
            // Mobile UA: serves the touch player that plays reliably in a WebView.
            userAgentString = MOBILE_UA
            loadWithOverviewMode = true
            useWideViewPort = true
            setSupportZoom(false)
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

        // A MOBILE (Android/Chrome) user agent serves the touch web player, which
        // is the variant that actually plays back reliably inside an Android
        // WebView. The desktop player, while it exposes more controls, does not
        // start playback reliably in a WebView — so the mobile player is the
        // correct default (this is also what the original Spotilol uses).
        private const val MOBILE_UA =
            "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/124.0.0.0 Mobile Safari/537.36"
    }
}
