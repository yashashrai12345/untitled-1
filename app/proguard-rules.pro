# ──────────────────────────────────────────────────────────────────────────────
# Arrows Puzzle Escape – Production ProGuard / R8 Rules
# ──────────────────────────────────────────────────────────────────────────────

# ── Global R8 Optimisation Settings ──────────────────────────────────────────
# Repack all classes into root package to reduce class-file overhead
-repackageclasses ''
-allowaccessmodification

# Run optimisation passes (R8 default is 5; increase for tighter release)
-optimizationpasses 5
-mergeinterfacesaggressively

# ── Kotlin ────────────────────────────────────────────────────────────────────
# Keep Kotlin metadata so reflection / serialization won't break
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$Companion { *; }

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}

# Coroutine debug info is not needed in production
-dontwarn kotlinx.coroutines.debug.**

# ── Jetpack Compose ───────────────────────────────────────────────────────────
-keepclassmembers class * extends androidx.compose.ui.Modifier { *; }
# Keep Compose @Stable / @Immutable annotations so the compiler honours them
-keep @androidx.compose.runtime.Stable class *
-keep @androidx.compose.runtime.Immutable class *
# Keep composable function names used in stack traces (easier debugging)
-keepattributes SourceFile,LineNumberTable

# ── Game Model & State (must survive minification intact) ─────────────────────
-keep class com.example.game.model.** { *; }
-keep class com.example.game.animation.** { *; }
-keep class com.example.data.** { *; }

# ── Android SharedPreferences & Context ──────────────────────────────────────
-keep class com.example.data.persistence.GamePreferences { *; }

# ── Strip ALL Android Log calls from release builds ──────────────────────────
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
    public static int wtf(...);
}

# ── Remove Kotlin null-check assertions in release (tiny speed gain) ──────────
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    public static void checkNotNull(...);
    public static void checkParameterIsNotNull(...);
    public static void checkNotNullParameter(...);
    public static void checkExpressionValueIsNotNull(...);
    public static void checkNotNullExpressionValue(...);
}
