# Unity Ads
-keep class com.unity3d.ads.** { *; }
-keep interface com.unity3d.ads.** { *; }
-keep class com.unity3d.services.** { *; }
-keep interface com.unity3d.services.** { *; }
-keep class com.unity3d.ads.metadata.** { *; }

# Firebase Firestore
-keep class com.google.firebase.firestore.** { *; }

# Firebase Auth
-keep class com.google.firebase.auth.** { *; }

# Moshi / Reflection-based models
-keep class com.directcash.app.data.model.** { *; }
-keep class com.directcash.app.data.repository.** { *; }

# Preserve R classes (optional but sometimes helpful for shrinking)
-keep class **.R$* { *; }

# Handle Kotlin Serialization if used
-keepattributes *Annotation*, EnclosingMethod, Signature
-keepnames class kotlinx.serialization.internal.GeneratedSerializer* {
    private static final kotlinx.serialization.internal.GeneratedSerializer INSTANCE;
}
-keepclassmembers class com.directcash.app.data.model.** {
    *** Companion;
    *** $serializer;
}
