# ProGuard rules for Terminal Emulator + AI

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep AI model classes
-keep class com.termuxai.terminal.ai.** { *; }

# Keep accessibility service
-keep class com.termuxai.terminal.accessibility.** { *; }

# Keep state saving
-keep class com.termuxai.terminal.services.** { *; }

# Keep terminal classes
-keep class com.termuxai.terminal.terminal.** { *; }

# Keep JNI
-keep class com.termuxai.terminal.utils.NativeTerminal { *; }

# TensorFlow Lite
-keep class org.tensorflow.lite.** { *; }
-keep class org.tensorflow.lite.gpu.** { *; }
-keep class org.tensorflow.lite.nnapi.** { *; }

# ExecuTorch
-keep class org.pytorch.executorch.** { *; }

# Room
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase

# Gson
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer