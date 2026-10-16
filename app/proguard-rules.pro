-dontshrink
-dontoptimize
-dontpreverify
-verbose

-keepclassmembers class **.R$* {public static <fields>;}

-keep class * extends android.app.Application
-keep class * extends android.app.Service

-keep class com.google.android.** { *; }

-dontwarn okio.**
