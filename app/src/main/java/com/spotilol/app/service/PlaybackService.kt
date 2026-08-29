package com.spotilol.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media.session.MediaButtonReceiver
import com.spotilol.app.MainActivity
import com.spotilol.app.R
import com.spotilol.app.util.Prefs
import com.spotilol.app.webview.PlayerState
import com.spotilol.app.widget.SpotilolWidget

/**
 * Foreground service that mirrors the web player into an Android MediaSession and
 * a media-style notification, giving lock-screen / Bluetooth / Wear OS controls.
 *
 * Playback itself lives in the WebView; this service only reflects its state and
 * forwards transport commands back to the Activity via [Controls]. There is no
 * Firebase, no analytics, and no network access here.
 */
class PlaybackService : Service() {

    /** Transport commands are sent back to whoever owns the WebView. */
    interface Controls {
        fun play()
        fun pause()
        fun next()
        fun previous()
        fun toggle()
    }

    inner class LocalBinder : Binder() {
        val service: PlaybackService get() = this@PlaybackService
    }

    private val binder = LocalBinder()
    private var controls: Controls? = null
    private lateinit var session: MediaSessionCompat
    private var lastState = PlayerState()

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createChannel()
        session = MediaSessionCompat(this, "Spotilol").apply {
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() { controls?.play() }
                override fun onPause() { controls?.pause() }
                override fun onSkipToNext() { controls?.next() }
                override fun onSkipToPrevious() { controls?.previous() }
                override fun onStop() { controls?.pause() }
            })
            isActive = true
        }
        // Post an initial (empty) notification so we satisfy startForeground quickly.
        startForeground(NOTIF_ID, buildNotification(null))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(session, intent)
        return START_STICKY
    }

    fun setControls(c: Controls?) { controls = c }

    /** Called by the Activity whenever the web player's state changes. */
    fun updateState(state: PlayerState) {
        lastState = state
        val metadata = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, state.title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, state.artist)
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, state.durationSec * 1000)
            .build()
        session.setMetadata(metadata)

        val playState = if (state.playing) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
        val actions = PlaybackStateCompat.ACTION_PLAY or
            PlaybackStateCompat.ACTION_PAUSE or
            PlaybackStateCompat.ACTION_PLAY_PAUSE or
            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
            PlaybackStateCompat.ACTION_STOP
        session.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setActions(actions)
                .setState(playState, state.positionSec * 1000, 1f)
                .build()
        )

        notificationManager().notify(NOTIF_ID, buildNotification(null))

        // Mirror state to the home-screen widget.
        Prefs(this).saveNowPlaying(state.title, state.artist, state.playing)
        SpotilolWidget.refresh(this)
    }

    private fun buildNotification(art: Bitmap?): Notification {
        val contentIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIcon = if (lastState.playing) R.drawable.ic_pause else R.drawable.ic_play
        val playPauseAction = if (lastState.playing) {
            action(playPauseIcon, "Pause", PlaybackStateCompat.ACTION_PAUSE)
        } else {
            action(playPauseIcon, "Play", PlaybackStateCompat.ACTION_PLAY)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(lastState.title.ifEmpty { getString(R.string.app_name) })
            .setContentText(lastState.artist)
            .setLargeIcon(art)
            .setContentIntent(contentIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .addAction(action(R.drawable.ic_prev, "Previous", PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS))
            .addAction(playPauseAction)
            .addAction(action(R.drawable.ic_next, "Next", PlaybackStateCompat.ACTION_SKIP_TO_NEXT))
            .setStyle(
                MediaStyle()
                    .setMediaSession(session.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .build()
    }

    private fun action(icon: Int, title: String, playbackAction: Long): NotificationCompat.Action {
        val intent = MediaButtonReceiver.buildMediaButtonPendingIntent(this, playbackAction)
        return NotificationCompat.Action(icon, title, intent)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notif_channel_playback),
                NotificationManager.IMPORTANCE_LOW
            ).apply { setShowBadge(false) }
            notificationManager().createNotificationChannel(channel)
        }
    }

    private fun notificationManager() =
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override fun onDestroy() {
        session.isActive = false
        session.release()
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL_ID = "spotilol_playback"
        private const val NOTIF_ID = 1001
    }
}
