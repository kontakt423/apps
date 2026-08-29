package com.spotilol.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.RemoteViews
import androidx.media.session.MediaButtonReceiver
import com.spotilol.app.MainActivity
import com.spotilol.app.R
import com.spotilol.app.util.Prefs

/**
 * Home-screen widget showing the current track and transport controls.
 *
 * Buttons reuse the same MediaSession transport as the notification (via
 * [MediaButtonReceiver]), so the widget, notification, lock screen and
 * Bluetooth all drive the exact same playback. No Firebase, no network.
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

            // Transport buttons -> MediaSession (same path as the notification).
            views.setOnClickPendingIntent(
                R.id.widget_prev,
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    context, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                )
            )
            views.setOnClickPendingIntent(
                R.id.widget_play_pause,
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    context, PlaybackStateCompat.ACTION_PLAY_PAUSE
                )
            )
            views.setOnClickPendingIntent(
                R.id.widget_next,
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    context, PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                )
            )

            // Tapping the track info opens the app.
            val open = PendingIntent.getActivity(
                context, 0, Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_info, open)

            return views
        }
    }
}
