# DartNative / Flutter Camera & Overlay Plugin Architecture Design

## Overview
This architecture decouples the **Native UI & Rendering** from the **Business Logic**:
- **Native UI Layer (Android / iOS)**: Responsible for CameraX lifecycle, preview rendering, GPU Media3 dynamic texture overlays (parallelogram spans, overlay visibility, camera controls), and recording video files.
- **Dart Business Logic Layer (Flutter)**: Responsible for network/socket IO operations (`http://185.48.228.171:21741`), state management, data parsing (`InfoModel`, `RealtimeModel`, `RiderModel`, `HorseModel`, `FinalModel`), and user interactions.

---

## Architecture Diagram & Channel Communication

```
┌──────────────────────────────────────────────────────────┐
│                   DART / FLUTTER LAYER                   │
│  - Socket.IO Client Connection                           │
│  - Event Data Parsing & State Management                 │
│  - Business Logic Rules & Settings                       │
└──────────────────────────┬───────────────────────────────┘
                           │
                 MethodChannel / EventChannel
                           │
┌──────────────────────────▼───────────────────────────────┐
│                   NATIVE ANDROID LAYER                   │
│  - CameraX (Preview & Video Capture)                     │
│  - Media3Effect / OverlayEffect                          │
│  - Native Parallelogram Text Overlays & UI Controls       │
└──────────────────────────────────────────────────────────┘
```

---

## MethodChannel & EventChannel Specifications

### 1. MethodChannel: `ch.zeitmessungen.equestre/camera`
Handles control methods between Dart and Native Android:

- `startRecording(Map params)` -> Starts video recording with destination path options.
- `stopRecording()` -> Stops current video recording.
- `updateOverlaySettings(Map settings)` -> Updates overlay visibility preferences (e.g. `showPenalties`, `showTime`, `showHorseName`).
- `updateOverlayData(Map overlayData)` -> Streams parsed realtime model data from Dart directly into native overlay renderer.

### 2. EventChannel: `ch.zeitmessungen.equestre/camera_events`
Emits native camera events back to Dart:
- `onRecordingStarted(String path)`
- `onRecordingStopped(String path, Long durationMs)`
- `onCameraError(String errorCode, String errorMessage)`

---

## Overlay Data Structure (Dart -> Native)

```json
{
  "riderName": "Manish Sharma",
  "horseName": "Thunderbolt",
  "horseNumber": "01",
  "penalties": "0.00",
  "time": "00:01:23.456",
  "rank": "1",
  "isLive": true
}
```

---

## Native Implementation Strategy
The native side implements an `OverlayDataProvider` interface. When `updateOverlayData` is called via MethodChannel or local state update, the values are fed into the `OverlayEffect` texture overlays in `StartRecordingActivity`.
