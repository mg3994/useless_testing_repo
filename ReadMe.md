# Equestre Native Camera & DartNative Plugin Architecture

Special thanks to @Coding With Nobody (Youtube)

## Architecture & Integration
This project is designed as an advanced **DartNative Camera Plugin** (`dartnative_camera`), mimicking the official [DartNative Plugin](https://github.com/DartNative/dartnative/tree/main/plugins/dartnative_camera) architecture:

- **Native UI & Hardware Rendering**: Managed on Android via CameraX, Media3 Effect (`Media3Effect`, `OverlayEffect`), custom `ParallelogramSpan` GPU dynamic overlays, and native video recording.
- **Dart Business Logic Layer**: Direct JNI invocation via DartNative runtime (`dn plugin build`), handling socket connections (`Socket.IO`), real-time data ingestion (`InfoModel`, `RealtimeModel`, `RiderModel`, `HorseModel`), and state management in Dart.

Detailed architecture specifications, JNI native signatures, and `.aar` plugin distribution configuration are documented in [DART_NATIVE_PLUGIN_DESIGN.md](DART_NATIVE_PLUGIN_DESIGN.md).

## Dependencies

```kotlin
// AndroidX & CameraX Dependencies
implementation(libs.androidx.activity)
implementation(libs.androidx.constraintlayout)
implementation(libs.androidx.camera.core)
implementation(libs.androidx.camera.camera2)
implementation(libs.androidx.camera.lifecycle)
implementation(libs.androidx.camera.video)
implementation(libs.androidx.camera.view)
implementation(libs.androidx.media3.effect)
implementation(libs.media3.effect)
```
