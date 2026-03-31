# Jetpack Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Kotlin Serialization (将来使用時のため)
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# ViewModel
-keep class * extends androidx.lifecycle.ViewModel { *; }

# アプリ固有クラス（難読化除外）
-keep class com.melof.complainttrainer.data.** { *; }
