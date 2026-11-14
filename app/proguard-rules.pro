# Unity Ads SDK & WorkManager reflection keep rules for release / sideload APKs
-keep class com.unity3d.ads.** { *; }
-keep class com.unity3d.services.** { *; }
-keep public class gatewayprotocol.v1.** { *; }
-keep class androidx.work.impl.WorkDatabase_Impl { *; }
-keep class * extends androidx.work.ListenableWorker { <init>(android.content.Context, androidx.work.WorkerParameters); }
-keep class * extends androidx.work.InputMerger { <init>(); }

# Firestore models & kotlinx serialization
-keepclassmembers class com.example.data.model.** { *; }
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keep,includedescriptorclasses class com.example.ui.**Route** { *; }
