package com.spotilol.app.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AlertDialog
import com.spotilol.app.BuildConfig
import com.spotilol.app.R
import com.spotilol.app.util.Prefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Checks GitHub Releases for a newer version.
 *
 * This is the ONLY update mechanism — there is no Firebase Remote Config and no
 * proprietary update backend. It simply hits the public GitHub REST API for the
 * configured repository, at most once per [CHECK_INTERVAL_MS].
 */
class UpdateChecker(private val context: Context) {

    data class Release(val version: String, val htmlUrl: String, val notes: String)

    /** Run a check only if a day has passed since the last one. */
    fun checkIfDue(prefs: Prefs, onUpdate: (Release) -> Unit) {
        val now = System.currentTimeMillis()
        if (now - prefs.lastUpdateCheck < CHECK_INTERVAL_MS) return
        prefs.lastUpdateCheck = now
        check(onUpdate = onUpdate, onNone = {}, onError = {})
    }

    /** Force a check regardless of the interval (used by the manual button). */
    fun check(
        onUpdate: (Release) -> Unit,
        onNone: () -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val release = fetchLatest()
                withContext(Dispatchers.Main) {
                    if (release != null && isNewer(release.version, BuildConfig.VERSION_NAME)) {
                        onUpdate(release)
                    } else {
                        onNone()
                    }
                }
            } catch (t: Throwable) {
                withContext(Dispatchers.Main) { onError(t) }
            }
        }
    }

    private fun fetchLatest(): Release? {
        val url = URL("https://api.github.com/repos/${BuildConfig.UPDATE_REPO}/releases/latest")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 10_000
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", "Spotilol-Android")
        }
        try {
            if (conn.responseCode != 200) return null
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val o = JSONObject(body)
            val tag = o.optString("tag_name").ifEmpty { o.optString("name") }
            return Release(
                version = tag.removePrefix("v"),
                htmlUrl = o.optString("html_url"),
                notes = o.optString("body")
            )
        } finally {
            conn.disconnect()
        }
    }

    companion object {
        const val CHECK_INTERVAL_MS = 24L * 60 * 60 * 1000

        /** Semantic-ish version comparison; returns true if [remote] > [local]. */
        fun isNewer(remote: String, local: String): Boolean {
            val r = remote.split(".").mapNotNull { it.toIntOrNull() }
            val l = local.split(".").mapNotNull { it.toIntOrNull() }
            val n = maxOf(r.size, l.size)
            for (i in 0 until n) {
                val rv = r.getOrElse(i) { 0 }
                val lv = l.getOrElse(i) { 0 }
                if (rv != lv) return rv > lv
            }
            return false
        }

        fun showDialog(context: Context, release: Release) {
            AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.update_available, release.version))
                .setMessage(release.notes.take(500))
                .setPositiveButton(R.string.update_download) { _, _ ->
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(release.htmlUrl)))
                }
                .setNegativeButton(R.string.update_later, null)
                .show()
        }
    }
}
