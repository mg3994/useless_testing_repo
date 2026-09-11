# DartNative & JNIGen Camera Plugin Architecture

## Overview
This architecture details the JNIGen / DartNative direct binding setup for `dartnative_camera`.

### JNIGen / DartNative Direct FFI Bindings
Using Flutter `jnigen`, Dart classes are automatically generated to bind to Java/Kotlin bytecode directly via FFI without MethodChannels:

```yaml
# jnigen.yaml configuration example for DartNative camera
output:
  dart:
    path: lib/src/third_party/dartnative_camera.dart
classes:
  - 'ch.zeitmessungen.equestre.plugin.DartNativeCameraBridge'
  - 'ch.zeitmessungen.equestre.ui.recording.StartRecordingActivity'
android_sdk_config:
  versions:
    - '34'
```

### JNI Native Flow
1. **Dart Business Logic Layer**:
   - Manages Socket.IO events, state calculation, timing, penalties, and competitors.
   - Invokes `DartNativeCameraBridge.nativeUpdateOverlayData(...)` via `jnigen`-generated FFI wrappers.
2. **Native Android Hardware Layer**:
   - CameraX preview & video capture recording.
   - Media3 GPU dynamic texture overlay rendering using native `ParallelogramSpan`.

---

## Gradle AAR Packaging Configuration (`dartnative_camera.aar`)

```groovy
configurations.maybeCreate('default')
artifacts.add('default', file('dartnative_camera.aar'))
dependencies {
    add('default', 'androidx.camera:camera-core:1.3.0-beta01')
    add('default', 'androidx.camera:camera-camera2:1.3.0-beta01')
    add('default', 'androidx.camera:camera-lifecycle:1.3.0-beta01')
    add('default', 'androidx.camera:camera-video:1.3.0-beta01')
    add('default', 'androidx.camera:camera-view:1.3.0-beta01')
    add('default', 'androidx.media3:media3-effect:1.2.0')
    add('default', 'com.google.guava:guava:32.0.1-android')
    add('default', 'androidx.core:core-ktx:1.12.0')
}
```
