package com.spotilol.app.widget

import android.graphics.Bitmap

/**
 * In-memory holder for the current album cover bitmap, shared between the
 * playback service (which downloads it) and the widget/notification (which
 * display it). Kept tiny on purpose — it is only ever one cover at a time.
 */
object NowPlayingArt {
    @Volatile
    var bitmap: Bitmap? = null
}
