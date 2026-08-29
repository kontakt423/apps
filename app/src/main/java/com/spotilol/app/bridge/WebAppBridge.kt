package com.spotilol.app.bridge

import android.webkit.JavascriptInterface
import com.spotilol.app.webview.PlayerState

/**
 * JavaScript <-> Kotlin bridge exposed to the Spotify web player as
 * `window.SpotilolBridge`. Only annotated methods are reachable from JS, and
 * the class is kept by ProGuard (see proguard-rules.pro).
 *
 * This bridge carries player state only. It sends nothing off-device — there is
 * no analytics endpoint of any kind.
 */
class WebAppBridge(private val listener: Listener) {

    interface Listener {
        fun onPlayerState(state: PlayerState)
        fun onWebPlayerReady()
    }

    @JavascriptInterface
    fun onPlayerState(json: String) {
        listener.onPlayerState(PlayerState.fromJson(json))
    }

    @JavascriptInterface
    fun onReady() {
        listener.onWebPlayerReady()
    }

    companion object {
        const val NAME = "SpotilolBridge"
    }
}
