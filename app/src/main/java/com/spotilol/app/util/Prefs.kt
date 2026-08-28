package com.spotilol.app.util

import android.content.Context
import androidx.preference.PreferenceManager

/**
 * Thin typed wrapper over the default SharedPreferences.
 *
 * Everything here is stored locally on the device. There is deliberately no
 * remote configuration source (no Firebase Remote Config) — settings live only
 * on the user's phone.
 */
class Prefs(context: Context) {

    private val sp = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)

    val amoledMode: Boolean get() = sp.getBoolean(KEY_AMOLED, false)
    val keepScreenOn: Boolean get() = sp.getBoolean(KEY_KEEP_SCREEN_ON, true)
    val adBlockEnabled: Boolean get() = sp.getBoolean(KEY_ADBLOCK, true)
    val autoplayEnabled: Boolean get() = sp.getBoolean(KEY_AUTOPLAY, true)
    val autoUpdate: Boolean get() = sp.getBoolean(KEY_AUTO_UPDATE, true)

    var lastUpdateCheck: Long
        get() = sp.getLong(KEY_LAST_UPDATE_CHECK, 0L)
        set(value) = sp.edit().putLong(KEY_LAST_UPDATE_CHECK, value).apply()

    companion object {
        const val KEY_AMOLED = "amoled_mode"
        const val KEY_KEEP_SCREEN_ON = "keep_screen_on"
        const val KEY_ADBLOCK = "adblock_enabled"
        const val KEY_AUTOPLAY = "autoplay_enabled"
        const val KEY_AUTO_UPDATE = "auto_update"
        const val KEY_LAST_UPDATE_CHECK = "last_update_check"
    }
}
