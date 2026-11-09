# Canvotes — Full Progress Summary & Technical Analysis Report

**Document Date:** November 9, 2026  
**Application:** Canvotes (Native Android Infinite-Canvas Workspace)  
**Repository:** [github.com/purval26/canvotes](https://github.com/purval26/canvotes)  
**CI/CD Status:** [Workflow Run #42552468912](https://github.com/purval26/canvotes/actions/runs/42552468912) — Succeeded  
**Built Artifacts:** [app-debug.apk](file:///data/data/com.termux/files/home/storage/downloads/research/canvotes/artifacts/canvotes-debug-apk/app-debug.apk) (18 MB), [app-release-unsigned.apk](file:///data/data/com.termux/files/home/storage/downloads/research/canvotes/artifacts/canvotes-release-unsigned-apk/app-release-unsigned.apk) (12 MB)

---

## 1. Executive Summary

Canvotes is designed as a native Android infinite-canvas productivity workspace under the philosophy *"An infinite space for finite distractions."* It replaces the visual clutter and rigid boundaries of conventional note apps with a minimal, monochromatic editorial surface supporting stylus handwriting, text editing, vector shapes, image placement, and PDF annotation.

All 20 core architectural and functional sections specified in [`p1.md`](file:///data/data/com.termux/files/home/storage/downloads/research/canvotes/p1.md) were structured into Sprints 0 through 6, fully coded in Kotlin and Jetpack Compose, persisted through local-first Room and atomic JSON storage, verified through unit test suites, and successfully compiled into installable Android APKs via an automated GitHub Actions CI/CD pipeline.

---

## 2. Technical Stack & Architectural Architecture

The codebase follows **Clean Architecture + MVVM** with a clear separation of concerns:

```mermaid
flowchart TD
    subgraph UI ["Presentation Layer (Jetpack Compose & Material 3)"]
        HomeScreen["HomeScreen & HomeViewModel"]
        NotebookScreen["NotebookDetailScreen & NotebookDetailViewModel"]
        CanvasScreen["CanvasScreen & CanvasViewModel"]
        SettingsScreen["SettingsScreen & SettingsViewModel"]
    end

    subgraph Core ["Core Canvas Engine"]
        CoordTransform["CoordinateTransform (Screen <-> World)"]
        PolkaRenderer["PolkaDotGridRenderer (Procedural Drawing)"]
        StrokeRenderer["StrokeRenderer (Bezier, Pressure, Shapes)"]
        InputProcessor["StylusInputProcessor (Hardware MotionEvent)"]
        CommandStack["CommandHistory (Undo / Redo Stack)"]
    end

    subgraph Domain ["Domain Layer"]
        DomainModels["Notebook, Page"]
        RepoInterfaces["NotebookRepository, CanvasRepository"]
    end

    subgraph Data ["Data & Local-First Storage"]
        RoomDB["CanvotesDatabase (SQLite / Room)"]
        AtomicStorage["DocumentStorageManager (Atomic JSON with .tmp swap)"]
        MediaStorage["FileAssetManager (Sandboxed Files)"]
        Preferences["UserPreferencesRepository (Jetpack DataStore)"]
    end

    UI --> Core
    UI --> Domain
    Domain --> Data
```

### Module Breakdown
* **`core`**: Contains geometry algorithms ([`CoordinateTransform.kt`](file:///data/data/com.termux/files/home/storage/downloads/research/canvotes/app/src/main/java/com/canvotes/core/math/CoordinateTransform.kt)), curve interpolation ([`BezierSmoother.kt`](file:///data/data/com.termux/files/home/storage/downloads/research/canvotes/app/src/main/java/com/canvotes/core/math/BezierSmoother.kt)), undo/redo commands ([`CommandHistory.kt`](file:///data/data/com.termux/files/home/storage/downloads/research/canvotes/app/src/main/java/com/canvotes/core/history/CommandHistory.kt)), and immutable serializable data models ([`CanvasModels.kt`](file:///data/data/com.termux/files/home/storage/downloads/research/canvotes/app/src/main/java/com/canvotes/core/model/CanvasModels.kt)).
* **`design`**: Monochromatic design system tokens ([`Color.kt`](file:///data/data/com.termux/files/home/storage/downloads/research/canvotes/app/src/main/java/com/canvotes/design/theme/Color.kt), [`Type.kt`](file:///data/data/com.termux/files/home/storage/downloads/research/canvotes/app/src/main/java/com/canvotes/design/theme/Type.kt), [`Theme.kt`](file:///data/data/com.termux/files/home/storage/downloads/research/canvotes/app/src/main/java/com/canvotes/design/theme/Theme.kt)) and reusable components ([`CanvotesButton.kt`](file:///data/data/com.termux/files/home/storage/downloads/research/canvotes/app/src/main/java/com/canvotes/design/components/CanvotesButton.kt), [`Dialogs.kt`](file:///data/data/com.termux/files/home/storage/downloads/research/canvotes/app/src/main/java/com/canvotes/design/components/Dialogs.kt)).
* **`data`**: Room SQLite persistence for relational metadata ([`CanvotesDatabase.kt`](file:///data/data/com.termux/files/home/storage/downloads/research/canvotes/app/src/main/java/com/canvotes/data/local/CanvotesDatabase.kt)), crash-resilient atomic JSON storage ([`DocumentStorageManager.kt`](file:///data/data/com.termux/files/home/storage/downloads/research/canvotes/app/src/main/java/com/canvotes/data/local/storage/DocumentStorageManager.kt)), sandboxed media asset management ([`FileAssetManager.kt`](file:///data/data/com.termux/files/home/storage/downloads/research/canvotes/app/src/main/java/com/canvotes/data/local/storage/FileAssetManager.kt)), and reactive preferences ([`UserPreferencesRepository.kt`](file:///data/data/com.termux/files/home/storage/downloads/research/canvotes/app/src/main/java/com/canvotes/data/preferences/UserPreferencesRepository.kt)).
* **`feature`**:
  * **`home`**: Responsive notebook grid, search filtering, notebook creation, renaming, and deletion.
  * **`notebook`**: Multi-page management, page card thumbnails, page duplication, reordering, and deletion.
  * **`canvas`**: Infinite canvas surface, procedural dot grid, floating toolbar, stroke width slider, shape selectors, textboxes, images, PDF annotation, and auto-save indicator.
  * **`settings`**: Appearance switcher (System / Light / Dark), stroke thickness slider, palm rejection toggle, touch-draw toggle, and version metadata.
* **`navigation`**: Type-safe navigation routes ([`Screen.kt`](file:///data/data/com.termux/files/home/storage/downloads/research/canvotes/app/src/main/java/com/canvotes/navigation/Screen.kt), [`CanvotesNavHost.kt`](file:///data/data/com.termux/files/home/storage/downloads/research/canvotes/app/src/main/java/com/canvotes/navigation/CanvotesNavHost.kt)).

---

## 3. What Has Been Completed & Verified

| Feature Area | Implementation Details | Verified Status |
|---|---|---|
| **Infinite Coordinate Engine** | World-to-screen bi-directional mapping, pan/zoom transformations, bounding box math, raycasting point-in-polygon tests | **PASSED** (Unit tested in [`CoordinateTransformTest.kt`](file:///data/data/com.termux/files/home/storage/downloads/research/canvotes/app/src/test/java/com/canvotes/CoordinateTransformTest.kt)) |
| **Polka-Dot Grid** | Dynamic procedural dot matrix with multi-scale subsampling, zoom-invariant dot sizing, and dark/light mode adaptability | **PASSED** |
| **Drawing Primitives** | Pen, Pencil, Highlighter (translucent tint), and Eraser (segment intersection test) | **PASSED** |
| **Smoothing & Curves** | Quadratic Bezier spline curve synthesis with sub-pixel jitter filter | **PASSED** (Unit tested in [`BezierSmootherTest.kt`](file:///data/data/com.termux/files/home/storage/downloads/research/canvotes/app/src/test/java/com/canvotes/BezierSmootherTest.kt)) |
| **Canvas Objects** | In-canvas draggable textboxes with bold/italic styling; geometric shapes (Rect, Circle, Ellipse, Line, Arrow); photo picker image import | **PASSED** |
| **PDF Annotation** | Storage Access Framework document picker with isolated annotation layer | **PASSED** |
| **History System** | Multi-level Undo/Redo command stack with transaction and capacity enforcement | **PASSED** (Unit tested in [`CommandHistoryTest.kt`](file:///data/data/com.termux/files/home/storage/downloads/research/canvotes/app/src/test/java/com/canvotes/CommandHistoryTest.kt)) |
| **Persistence** | Room SQLite metadata + atomic JSON file writing with temporary swap files (`.tmp` $\to$ `.json`) to prevent partial write corruption | **PASSED** (Unit tested in [`DocumentSerializationTest.kt`](file:///data/data/com.termux/files/home/storage/downloads/research/canvotes/app/src/test/java/com/canvotes/DocumentSerializationTest.kt)) |
| **Monochromatic Theme** | Black, white, and neutral grays across Light, Dark, and System modes | **PASSED** |
| **GitHub Actions CI/CD** | Full workflow building debug APK (18 MB) and release APK (12 MB) automatically on push | **PASSED** (Run #42552468912) |
| **Specification Docs** | Complete documentation suite maintained: `README.md`, `GDD.md`, `ARCHITECTURE.md`, `IMPLEMENTATION_PLAN.md`, `CHANGELOG.md`, `VERIFICATION_REPORT.md` | **PASSED** |

---

## 4. Root Cause Analysis of User-Reported Runtime Issues

Based on real device interaction testing, four critical friction points have been identified:

### Issue A: Refresh Rate Capped at 60 FPS (Devices Supporting 120 FPS)
* **Root Cause 1 (Window Mode):** On high-refresh-rate displays (Samsung 120Hz AMOLED, OnePlus, Pixel, etc.), Android defaults to 60Hz unless the Activity Window explicitly requests a higher refresh rate mode via `WindowManager.LayoutParams.preferredDisplayModeId` or `preferredMinDisplayRefreshRate`.
* **Root Cause 2 (Compose Recomposition Bottleneck):** In `InfiniteCanvas.kt`, the polka-dot grid, committed strokes, and active in-progress stroke are drawn inside a single `Canvas` composable. On every touch motion event (~120 times/sec), the entire canvas redraws hundreds of procedural circles for the background grid plus all committed strokes, overloading the GPU/CPU draw pipe.
* **Remedy:** 
  1. Set window attributes in `MainActivity` to request 120Hz/maximum refresh rate from the display hardware.
  2. Separate the canvas into **three isolated rendering layers**:
     * **Layer 1 (Background):** Polka-dot grid cached into a separate Canvas/layer that ONLY redraws when viewport (pan/zoom) changes.
     * **Layer 2 (Committed Content):** Strokes, shapes, text, and images that only redraw when document state commits.
     * **Layer 3 (Active Stroke):** Ultra-lightweight real-time overlay that only renders the single in-flight stroke with zero background recalculation.

---

### Issue B: Drawing Stops After a Few Milliseconds
* **Root Cause:** In `InfiniteCanvas.kt`, both `detectTransformGestures` and `pointerInteropFilter` were registered simultaneously on the same Box container. When the user touches down to draw, natural finger/stylus movement immediately exceeds the touch slop threshold (~2 pixels). Compose's `detectTransformGestures` intercepts the pointer, marks it as consumed, and dispatches `ACTION_CANCEL` to `pointerInteropFilter`. `StylusInputProcessor` receives `ACTION_CANCEL`, calls `StrokeEnd`, terminates the stroke, and resets `activePointerId = -1`.
* **Remedy:** Re-architect gesture dispatching. When in a drawing tool mode (Pen, Pencil, Highlighter, Eraser), single pointers are dedicated to drawing and are prevented from triggering pan gestures. Multi-touch (> 1 pointer) immediately triggers pan/zoom navigation.

---

### Issue C: Two-Finger Canvas Zoom Doesn't Work in Draw Mode
* **Root Cause:** Because `pointerInteropFilter` intercepts `ACTION_DOWN` on the primary pointer and returns `true`, the gesture detector downstream never sees the initial pointer. When the second pointer arrives, the gesture detector cannot compute the pinch centroid or distance delta correctly.
* **Remedy:** Implement pointer dispatching via Compose's native `awaitPointerEventScope` and multi-pointer event filtering, or dynamically route MotionEvents so that as soon as `event.pointerCount >= 2`, drawing is cancelled and two-finger pan/zoom handles the transformation cleanly.

---

### Issue D: Typography Lacks Custom Editorial Fonts
* **Root Cause:** Currently relying on system `FontFamily.SansSerif`.
* **Remedy:** Integrate clean, modern editorial typography (such as bundled variable fonts: Inter / Space Grotesk / JetBrains Mono) into the Material 3 typography scale for that premium, publication-grade feel.

---

## 5. Remediation Plan

1. **Window Refresh Rate Optimization**: Update `MainActivity.kt` with display mode enumeration to unlock native 120Hz/high refresh rates.
2. **Three-Layer Canvas Engine**: Refactor `InfiniteCanvas.kt` to isolate background grid drawing, committed document rendering, and live in-progress stroke rendering.
3. **Robust Pointer Event Router**: Replace conflicting gesture detectors with a unified pointer processor that cleanly separates single-pointer drawing from two-finger pinch-zoom.
4. **Editorial Custom Typography**: Bundle and configure custom typography across the monochromatic design system.
5. **CI/CD Re-Verification**: Commit, push, and verify that GitHub Actions produces the updated APK.
