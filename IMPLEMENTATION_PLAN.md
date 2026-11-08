# Canvotes — Implementation Plan & Sprint Roadmap

## Overview & Sprint Status

| Sprint | Objective | Status | Verification Criteria |
|---|---|---|---|
| **Sprint 0** | Foundation, Architecture, Gradle, Theme, CI/CD | **COMPLETED** | Gradle project compiles, CI workflow valid, Room & DataStore configured |
| **Sprint 1** | Home Screen & Notebook/Page Management | **COMPLETED** | Notebook CRUD, Page CRUD, Search, Foreign key cascades verified |
| **Sprint 2** | Infinite Canvas Engine & Polka-Dot Grid | **COMPLETED** | World transforms, zoom/pan bounds, dynamic dot renderer verified |
| **Sprint 3** | Stylus Input, Pressure, Smoothing, Undo/Redo | **COMPLETED** | Stylus vs finger routing, Bezier math, CommandHistory unit tests pass |
| **Sprint 4** | Canvas Objects (Textboxes, Shapes, Images) | **COMPLETED** | Textbox editing, shape previews, image aspect scaling, serialization pass |
| **Sprint 5** | PDF Import, Settings, Monochromatic Polish | **COMPLETED** | PDF asset management, DataStore preferences, Light/Dark styling |
| **Sprint 6** | Unit Testing, CI Verification & Release Setup | **COMPLETED** | Unit test suite execution, GitHub Actions workflow ready |

---

## Detailed Sprint Breakdown

### Sprint 0 — Foundation & CI/CD Setup
- [x] Configure Gradle 8.9 wrapper and dependencies (`libs.versions.toml`).
- [x] Configure Android Gradle Plugin 8.7.2, Kotlin 2.0.21, Jetpack Compose Compiler plugin.
- [x] Set up GitHub Actions CI workflow [`.github/workflows/build-apk.yml`](.github/workflows/build-apk.yml) for automated building and APK artifact publishing.
- [x] Establish Room database (`CanvotesDatabase`), DataStore preferences, and Clean Architecture modules.
- [x] Implement monochromatic design tokens (`Color.kt`, `Type.kt`, `Theme.kt`).

### Sprint 1 — Home & Organization
- [x] Create `NotebookEntity`, `PageEntity`, `NotebookDao`, `PageDao`.
- [x] Build `NotebookRepositoryImpl` with reactive Kotlin Flow queries.
- [x] Build `HomeScreen` with minimalist search and responsive notebook card grid.
- [x] Build `NotebookDetailScreen` with page cards, creation, renaming, and duplication.
- [x] Implement confirmation dialogs for safe notebook and page deletion.

### Sprint 2 — Infinite Canvas Engine
- [x] Implement world-to-screen and screen-to-world coordinate transformations in `CoordinateTransform.kt`.
- [x] Build `PolkaDotGridRenderer.kt` with procedural circular dot drawing and adaptive subsampling.
- [x] Implement multi-touch two-finger pan and pinch-to-zoom gestures in `InfiniteCanvas.kt`.
- [x] Build debounced auto-save pipeline (800ms quiet window) in `CanvasViewModel.kt`.

### Sprint 3 — Stylus Engine & History
- [x] Implement pointer discrimination in `StylusInputProcessor.kt` (stylus vs finger vs palm).
- [x] Implement quadratic Bezier smoothing and jitter filtering in `BezierSmoother.kt`.
- [x] Implement drawing tools: Pen, Pencil, Highlighter, and Eraser.
- [x] Implement Command Pattern in `CommandHistory.kt` with multi-level Undo/Redo.

### Sprint 4 — Canvas Objects & Interactions
- [x] Implement `TextboxOverlay.kt` with drag repositioning and bold/italic formatting.
- [x] Implement `StrokeRenderer.kt` supporting Rectangles, Rounded Rectangles, Circles, Ellipses, Lines, and Arrows.
- [x] Implement `ImageOverlay.kt` with Coil image loading and aspect ratio preserving resize handles.
- [x] Implement `DocumentStorageManager.kt` with atomic file swapping.

### Sprint 5 — PDF Support & Application Settings
- [x] Add PDF import support via Android document picker in `CanvasScreen.kt`.
- [x] Implement `SettingsScreen.kt` with theme selection (System / Light / Dark), stroke width slider, and palm rejection toggles.
- [x] Persist preferences reactively via `UserPreferencesRepository.kt`.
- [x] Refine monochromatic styling across all dialogs and menus.

### Sprint 6 — Testing & CI/CD Verification
- [x] Implement `CoordinateTransformTest.kt` verifying coordinate transforms and raycasting.
- [x] Implement `CommandHistoryTest.kt` verifying undo/redo execution and capacity constraints.
- [x] Implement `DocumentSerializationTest.kt` verifying JSON schema encoding/decoding.
- [x] Implement `BezierSmootherTest.kt` verifying jitter filter and smoothing algorithms.
- [x] Author comprehensive project documentation: `README.md`, `GDD.md`, `ARCHITECTURE.md`, `CHANGELOG.md`, `VERIFICATION_REPORT.md`.
