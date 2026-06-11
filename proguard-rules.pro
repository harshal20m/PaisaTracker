# PaisaTracker ProGuard Rules

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# iText7
-dontwarn com.itextpdf.**
-keep class com.itextpdf.** { *; }

# opencsv
-dontwarn com.opencsv.**

# slf4j (used by iText/opencsv)
-dontwarn org.slf4j.**

# Room
-keep class * extends androidx.room.RoomDatabase
-keep class * extends androidx.room.Entity
-keep class * extends androidx.room.Dao

# Firebase Analytics
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Keep Analytics Manager
-keep class com.example.paisatracker.analytics.AnalyticsManager { *; }
