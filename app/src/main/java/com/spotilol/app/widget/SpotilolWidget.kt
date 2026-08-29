package com.spotilol.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import com.spotilol.app.MainActivity
import com.spotilol.app.R
import com.spotilol.app.service.PlaybackService
import com.spotilol.app.util.Prefs

/**
 * Home-screen widget showing the current track, album cover and transport
 * controls.
 *
 * Control buttons send explicit commands to the running [PlaybackService], which
 * relays them to the web player. This reaches an already-running foreground
 * service reliably, whereas media-button broadcasts can be blocked in the
 * background. No Firebase, no network here.
 */
class SpotilolWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        for (id in ids) manager.updateAppWidget(id, buildViews(context))
    }

    companion object {
        /** Called by the playback service whenever the track/state changes. */
        fun refresh(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, SpotilolWidget::class.java)
            )
            if (ids.isEmpty()) return
            val views = buildViews(context)
            for (id in ids) manager.updateAppWidget(id, views)
        }

        private fun buildViews(context: Context): RemoteViews {
            val prefs = Prefs(context)
            val views = RemoteViews(context.packageName, R.layout.widget_player)

            val title = prefs.npTitle.ifEmpty { context.getString(R.string.app_name) }
            views.setTextViewText(R.id.widget_title, title)
            views.setTextViewText(R.id.widget_artist, prefs.npArtist)
            views.setImageViewResource(
                R.id.widget_play_pause,
                if (prefs.npPlaying) R.drawable.ic_pause else R.drawable.ic_play
            )

            // Album cover (falls back to the app icon when none is loaded yet).
            val art = NowPlayingArt.bitmap
            if (art != null) {
                views.setImageViewBitmap(R.id.widget_art, art)
            } else {
                views.setImageViewResource(R.id.widget_art, R.drawable.ic_launcher_foreground)
            }

            views.setOnClickPendingIntent(R.id.widget_prev, command(context, PlaybackService.ACTION_PREV, 1))
            views.setOnClickPendingIntent(R.id.widget_play_pause, command(context, PlaybackService.ACTION_TOGGLE, 2))
            views.setOnClickPendingIntent(R.id.widget_next, command(context, PlaybackService.ACTION_NEXT, 3))

            // Tapping the track info opens the app.
            val open = PendingIntent.getActivity(
                context, 0, Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_info, open)

            return views
        }

        /** A PendingIntent that delivers [action] to the playback service. */
        private fun command(context: Context, action: String, req: Int): PendingIntent {
            val intent = Intent(context, PlaybackService::class.java).setAction(action)
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                PendingIntent.getForegroundService(context, req, intent, flags)
            } else {
                PendingIntent.getService(context, req, intent, flags)
            }
        }
    }
}
