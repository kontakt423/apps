package com.spotilol.app.util

import android.app.Application

/**
 * Application entry point.
 *
 * Intentionally empty of any analytics / crash-reporting / performance SDK
 * initialisation. The original Spotilol initialises Firebase here; this rebuild
 * initialises nothing external.
 */
class App : Application()
