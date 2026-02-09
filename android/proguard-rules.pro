# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep Tauri plugin classes
-keep class com.plugin.printerx.** { *; }

# Keep printer library classes
-keep class com.printer.** { *; }
-keepattributes *Annotation*
