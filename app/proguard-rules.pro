# Keep the JavaScript bridge intact — its methods are called from injected JS.
-keepclassmembers class com.spotilol.app.bridge.WebAppBridge {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep model/data classes used by org.json reflection-free code (no-op safety).
-keep class com.spotilol.app.update.** { *; }
