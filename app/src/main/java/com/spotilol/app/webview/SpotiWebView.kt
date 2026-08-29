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

    /**
     * @param desktop when true, use a desktop UA so Spotify serves the full web
     * player (full library / playlist browsing). Trade-off: playback in a WebView
     * is less reliable in this mode. Default (false) = mobile touch player, which
     * plays reliably but restricts library browsing.
     */
    @SuppressLint("SetJavaScriptEnabled")
    fun configure(desktop: Boolean) {
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            mediaPlaybackRequiresUserGesture = false
            cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
            userAgentString = if (desktop) DESKTOP_UA else MOBILE_UA
            loadWithOverviewMode = true
            useWideViewPort = true
            // Desktop layout is wide, so allow pinch-zoom to read it on a phone.
            setSupportZoom(desktop)
            builtInZoomControls = desktop
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

        // A MOBILE (Android/Chrome) user agent serves the touch web player, which
        // is the variant that actually plays back reliably inside an Android
        // WebView. The desktop player, while it exposes more controls, does not
        // start playback reliably in a WebView — so the mobile player is the
        // correct default (this is also what the original Spotilol uses).
        private const val MOBILE_UA =
            "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/124.0.0.0 Mobile Safari/537.36"

        // Desktop UA: full web player incl. library browsing (see configure()).
        private const val DESKTOP_UA =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
    }
}
