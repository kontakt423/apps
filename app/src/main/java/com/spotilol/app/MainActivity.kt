package com.spotilol.app

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.spotilol.app.bridge.WebAppBridge
import com.spotilol.app.databinding.ActivityMainBinding
import com.spotilol.app.service.PlaybackService
import com.spotilol.app.ui.SettingsActivity
import com.spotilol.app.update.UpdateChecker
import com.spotilol.app.util.Prefs
import com.spotilol.app.webview.PlayerState

class MainActivity : AppCompatActivity(), WebAppBridge.Listener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: Prefs

    private var service: PlaybackService? = null
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            service = (binder as? PlaybackService.LocalBinder)?.service
            service?.setControls(controls)
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            service = null
        }
    }

    private val controls = object : PlaybackService.Controls {
        override fun play() = runOnUiThread { binding.webView.runControl("play()") }
        override fun pause() = runOnUiThread { binding.webView.runControl("pause()") }
        override fun next() = runOnUiThread { binding.webView.runControl("next()") }
        override fun previous() = runOnUiThread { binding.webView.runControl("prev()") }
        override fun toggle() = runOnUiThread { binding.webView.runControl("toggle()") }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyKeepScreenOn()
        setupWebView()
        binding.fabSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.webView.canGoBack()) binding.webView.goBack() else finish()
            }
        })

        // Start + bind the media playback service (foreground, no Firebase).
        val svc = Intent(this, PlaybackService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(svc) else startService(svc)
        bindService(svc, connection, Context.BIND_AUTO_CREATE)

        maybeCheckForUpdates()
    }

    private fun setupWebView() {
        binding.webView.configure()
        binding.webView.addJavascriptInterface(WebAppBridge(this), WebAppBridge.NAME)

        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                binding.progress.progress = newProgress
                binding.progress.visibility = if (newProgress in 1..99) View.VISIBLE else View.GONE
            }
        }

        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.webView.injectAsset("inject.js")
                if (prefs.adBlockEnabled) binding.webView.injectAsset("adblock.js")
                binding.webView.applyAmoled(prefs.amoledMode)
            }
        }

        binding.webView.loadPlayer()
    }

    private fun applyKeepScreenOn() {
        if (prefs.keepScreenOn) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        else window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun maybeCheckForUpdates() {
        if (!prefs.autoUpdate) return
        UpdateChecker(this).checkIfDue(prefs) { release ->
            runOnUiThread { UpdateChecker.showDialog(this, release) }
        }
    }

    // --- WebAppBridge.Listener ---------------------------------------------
    override fun onPlayerState(state: PlayerState) {
        service?.updateState(state)
    }

    override fun onWebPlayerReady() { /* injection already handled on page finish */ }

    override fun onResume() {
        super.onResume()
        applyKeepScreenOn()
        // Re-apply AMOLED in case it was toggled in settings.
        binding.webView.applyAmoled(prefs.amoledMode)
    }

    override fun onDestroy() {
        try { unbindService(connection) } catch (e: Exception) { /* not bound */ }
        binding.webView.destroy()
        super.onDestroy()
    }
}
