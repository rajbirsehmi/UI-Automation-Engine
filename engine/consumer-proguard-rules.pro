# --- Core Library Public API ---
# Keep all public classes and members in the main package and subpackages to ensure
# they remain accessible to library consumers after obfuscation.
-keep public class com.sehmi.engine.** {
    public protected *;
}

# --- Jetpack Compose ---
# Preserve Compose-related metadata and Composable functions to maintain
# compatibility with the Compose compiler and runtime in consuming apps.
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
    @androidx.compose.runtime.ReadOnlyComposable <methods>;
}

# Keep Compose UI Testing entry points
-keep class androidx.compose.ui.test.** { *; }

# --- Hilt / Dagger ---
# Hilt entry points and generated components must be preserved to allow
# dependency injection to function correctly in the final APK.
-keep @dagger.hilt.InstallIn class *
-keep class * extends android.app.Application
-keep class * extends android.app.Activity
-keep class * extends android.app.Service
-keep class * extends android.content.BroadcastReceiver
-keep class * extends android.content.ContentProvider
-keep class * extends androidx.fragment.app.Fragment
-keep class * extends androidx.lifecycle.ViewModel

# --- Log4j2 ---
# Log4j2 uses reflection for configuration and plugin discovery.
-keep class org.apache.logging.log4j.** { *; }
-dontwarn org.apache.logging.log4j.**

# --- UI Automation Engine Specifics ---
# Preserve the Robot Pattern entry points and core engine scopes.
-keepclassmembers class * extends com.sehmi.engine.core.ComposeRuleScope { *; }
