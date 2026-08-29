package com.spotilol.app.webview

import org.json.JSONObject

/** Immutable snapshot of the web player's state, parsed from injected JS. */
data class PlayerState(
    val title: String = "",
    val artist: String = "",
    val artworkUrl: String = "",
    val playing: Boolean = false,
    val positionSec: Long = 0,
    val durationSec: Long = 0
) {
    companion object {
        fun fromJson(json: String): PlayerState = try {
            val o = JSONObject(json)
            PlayerState(
                title = o.optString("title"),
                artist = o.optString("artist"),
                artworkUrl = o.optString("artwork"),
                playing = o.optBoolean("playing"),
                positionSec = o.optLong("position"),
                durationSec = o.optLong("duration")
            )
        } catch (e: Exception) {
            PlayerState()
        }
    }
}
