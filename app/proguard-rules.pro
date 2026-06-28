# ── kotlinx.serialization ─────────────────────────────────────────────────────
# Keep all @Serializable-annotated classes and the generated $$serializer companions.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class com.rustam.quizapp.**$$serializer { *; }
-keep @kotlinx.serialization.Serializable class * { *; }
-keepclassmembers @kotlinx.serialization.Serializable class * {
    *** Companion;
    static ** serializer();
}
-keepclasseswithmembers class com.rustam.quizapp.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ── Room ───────────────────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**

# ── WorkManager ────────────────────────────────────────────────────────────────
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.CoroutineWorker { *; }
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# ── Jetpack Navigation Compose ─────────────────────────────────────────────────
# Route strings and NavType parsing rely on class/enum names at runtime.
-keep class com.rustam.quizapp.ui.navigation.** { *; }
-dontwarn androidx.navigation.**

# ── Enums ──────────────────────────────────────────────────────────────────────
# kotlinx.serialization and NavArgs serialize enum names; keep them intact.
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    public final ** name();
    public final int ordinal();
}

# ── Kotlin metadata and JVM statics ────────────────────────────────────────────
-keep class kotlin.Metadata { *; }
-keepclassmembers class ** {
    @kotlin.jvm.JvmStatic *;
    @kotlin.jvm.JvmField *;
}
-dontwarn kotlin.**
-dontwarn kotlinx.**
