# Proguard rules for Matrix Camera

# Keep ML Kit face detection models
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_face_detection.** { *; }

# Keep CameraX internals
-keep class androidx.camera.** { *; }

# Keep data classes (FaceData, etc.)
-keep class com.nthg.matrixcamera.face.FaceData { *; }
-keep class com.nthg.matrixcamera.matrix.MatrixStyle { *; }

# Standard Android proguard rules
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

# Kotlin serialization
-keepattributes RuntimeVisibleAnnotations, AnnotationDefault
