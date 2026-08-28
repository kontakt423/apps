# Spotilol — Firebase-free rebuild

An Android app that wraps the **Spotify Web Player** in a customized `WebView`
and adds native media controls plus client-side audio-ad handling.

This is a clean-room–style **rebuild of the concept behind
[lyssadev/Spotilol](https://github.com/lyssadev/Spotilol)**, deliberately built
**without any Firebase** (no Analytics, no Crashlytics, no Performance
Monitoring) and without the `google-services` Gradle plugin or a
`google-services.json`. It builds and runs with **zero Google/Firebase backend
configuration**.

> ⚠️ **Legal note.** Blocking ads in the Spotify player violates Spotify's Terms
> of Service and may lead to account restrictions. This project is intended for
> educational and personal-research use. Use it at your own risk.

---

## What it does

- Loads `open.spotify.com` in a hardened WebView (log in with a free or premium
  account).
- **Native MediaSession + notification** — play / pause / next / previous from
  the lock screen, Bluetooth devices and Wear OS.
- **Client-side audio-ad handling** — detects ads in the player DOM, mutes all
  media elements and fast-forwards through them. No proxy or certificate needed.
- **AMOLED pure-black mode**, **keep-screen-on** during playback.
- **Update checker** against the GitHub Releases API (automatic once/day +
  manual button). *This is the only network call the app makes beyond Spotify
  itself — and it does not use Firebase Remote Config.*

## How Firebase was removed

| Original Spotilol | This rebuild |
|---|---|
| `com.google.gms.google-services` plugin | **not present** |
| `google-services.json` required in `app/` | **not required** (and git-ignored) |
| Firebase Analytics | **removed** — the app sends no usage data anywhere |
| Firebase Crashlytics | **removed** — optional OSS alternative: [ACRA](https://github.com/ACRA/acra) |
| Firebase Performance | **removed** |
| Update check | **unchanged in spirit** — still the public GitHub Releases API, which never depended on Firebase |

The core value of the app (WebView wrapper, JS injection, ad handling,
MediaSession) has **no dependency on Firebase**, so removing it changes nothing
about the features.

## Architecture

```
com.spotilol.app
├── MainActivity            # hosts the WebView, binds the playback service
├── webview/
│   ├── SpotiWebView        # configured WebView + asset injection helpers
│   └── PlayerState         # parsed snapshot of the web player state
├── bridge/
│   └── WebAppBridge        # @JavascriptInterface (window.SpotilolBridge)
├── service/
│   └── PlaybackService     # foreground MediaSession + media notification
├── update/
│   └── UpdateChecker       # GitHub Releases API, once/day, manual check
├── ui/
│   └── SettingsActivity    # PreferenceFragment-based settings
└── util/
    ├── App                 # Application (intentionally empty — no SDK init)
    └── Prefs               # typed SharedPreferences wrapper

assets/
├── inject.js               # player state bridge + native control surface + AMOLED
└── adblock.js              # DOM/media-based audio-ad detection & muting
```

**Flow:** `inject.js` polls the player DOM and reports state to Kotlin via the
`SpotilolBridge` JS interface → `MainActivity` forwards it to `PlaybackService`,
which updates the `MediaSessionCompat` and the notification → transport buttons
call back into `MainActivity`, which runs `window.SpotilolControls.*` in the
WebView.

### Ad-blocking modes

- **Normal (implemented here):** pure DOM/media approach inside the WebView —
  no certificate, no proxy. See `assets/adblock.js`. Because Spotify changes its
  markup, the selectors/heuristics there may need occasional updating.
- **Proxy MITM (not enabled):** the original optionally runs a local proxy with
  a user-installed CA to block ad *requests* at the network layer. That is more
  powerful but requires installing a certificate, so it is intentionally left
  out of this Firebase-free rebuild. The `PlaybackService`/WebView design does
  not preclude adding it later behind a setting.

## Building

Requirements: **Android Studio** (Ladybug/Koala or newer) or a machine with the
**Android SDK** (compileSdk 34) and **JDK 17**, and network access to Google's
Maven repository (`dl.google.com`).

```bash
# Easiest: open the project in Android Studio and press Run.

# Command line (after the Gradle wrapper jar exists — see note below):
./gradlew assembleDebug
# output: app/build/outputs/apk/debug/app-debug.apk
```

> **Gradle wrapper jar.** `gradle/wrapper/gradle-wrapper.jar` is a binary and is
> not committed here. Generate it once with a local Gradle install
> (`gradle wrapper --gradle-version 8.9`) or simply open the project in Android
> Studio, which provisions the wrapper automatically. The `gradlew` /
> `gradlew.bat` launcher scripts and `gradle-wrapper.properties` are included.

- **minSdk:** 26 (Android 8.0) · **targetSdk/compileSdk:** 34
- No `google-services.json` step. There is nothing Firebase to configure.

## Configuration

`app/build.gradle.kts` exposes one field used by the update checker:

```kotlin
buildConfigField("String", "UPDATE_REPO", "\"lyssadev/Spotilol\"")
```

Point it at your own `owner/repo` if you publish your own releases.

## Troubleshooting

**"Wiedergabe deaktiviert" / "Playback disabled".** Spotify streams
DRM-protected audio via Widevine (EME). An Android `WebView` only plays it when
the app grants the `PROTECTED_MEDIA_ID` permission, so the app overrides
`WebChromeClient.onPermissionRequest` and grants it (see `MainActivity`). If you
still hit this:

- Update **Android System WebView** and **Chrome** from the Play Store — the
  WebView provides the Widevine (usually L3) CDM.
- Ensure cookies are enabled (the app enables first- and third-party cookies for
  the login session) and that you are **not** running a WebView with DRM/EME
  disabled by a privacy/de-Googled ROM. On such ROMs Widevine may be missing
  entirely and DRM playback cannot work.
- Give the device a moment on first playback: Widevine may need to provision
  itself over the network once.

## Attribution

Concept and feature set inspired by
[lyssadev/Spotilol](https://github.com/lyssadev/Spotilol) (itself a Kotlin port
of *Spotifuck* by deviato). This repository is an independent, Firebase-free
reimplementation.
