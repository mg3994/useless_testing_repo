# Equestre Native Camera & Dart Business Logic Architecture

Special thanks to @Coding With Nobody (Youtube)

## Architecture & Integration
This project is designed to bridge **Native Android UI/Rendering** with **Dart/Flutter Business Logic**.

- **Native UI & Rendering**: Managed in Android via CameraX, Media3 Effect (`Media3Effect`, `OverlayEffect`), custom `ParallelogramSpan` drawn text overlays, and native video capture capabilities.
- **Dart / Business Logic Layer**: Handles socket connections (`Socket.IO`), real-time data ingestion (`InfoModel`, `RealtimeModel`, `RiderModel`, `HorseModel`), and rule evaluation.

Detailed architecture design, MethodChannel APIs, and EventChannel specifications are available in [DART_NATIVE_PLUGIN_DESIGN.md](DART_NATIVE_PLUGIN_DESIGN.md).

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
