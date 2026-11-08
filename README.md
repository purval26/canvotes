# Canvotes

> *"An infinite space for finite distractions."*

Canvotes is a native Android infinite-canvas workspace designed for focused thinking, stylus-first handwriting, sketching, text editing, shape creation, and PDF annotation. Built strictly according to the **Master Product & Engineering Specification**, Canvotes provides a minimal, editorial, monochromatic interface that prioritizes uninterrupted flow over application clutter.

---

## ✦ Key Features

* **Infinite Canvas Engine**:
  * Seamless world-to-screen coordinate system supporting unbounded navigation.
  * Dynamically rendered polka-dot grid with zoom-invariant dot sizing and subsampling to maintain smooth frame rates.
  * Hardware-accelerated two-finger pan and pinch-to-zoom.

* **Stylus & Handwriting Engine**:
  * Specialized pointer discrimination separating stylus input from finger touches.
  * Real-time pressure sensitivity support for natural, dynamic stroke variation.
  * Multi-point quadratic Bezier curve interpolation and jitter smoothing.
  * Four native drawing tools: **Pen**, **Pencil**, **Highlighter** (translucent monochrome tint), and **Eraser** (vector segment collision).
  * Robust palm rejection heuristic filtering out accidental wrist and large-surface touch contacts.

* **Canvas Objects**:
  * **Textboxes**: Free-placement typography with inline bold, italic, and size controls.
  * **Vector Shapes**: Rectangles, rounded rectangles, circles, ellipses, straight lines, and directional arrows.
  * **Images**: Import via Android Photo Picker / SAF with aspect ratio locking and resize handles.
  * **PDF Annotation**: Non-destructive annotation layers on top of imported multi-page documents.

* **Monochromatic Visual Identity**:
  * Pure black (`#000000`), pure white (`#FFFFFF`), and calibrated neutral grays.
  * Comprehensive Light Mode, Dark Mode, and System Theme synchronization.
  * Zero colorful gradients or intrusive UI ornamentation.

* **Local-First Architecture & Persistence**:
  * SQLite/Room database for relational metadata (notebooks, pages, counts, timestamps).
  * Atomic JSON serialization for canvas documents (`CanvasDocument` schema).
  * Isolated app-internal filesystem storage (`files/assets`) for media binaries.
  * Automatic background debounced saving (800ms quiet window) with crash resilience.
  * Full Command/History pattern with multi-level Undo and Redo.

---

## ✦ Architecture Overview

Canvotes adheres to **Clean Architecture + MVVM**:

```
app/src/main/java/com/canvotes/
├── CanvotesApplication.kt        # Application container & dependency locator
├── MainActivity.kt               # Single Activity host with edge-to-edge support
├── core/
│   ├── model/                    # CanvasDocument, Stroke, Shape, Textbox, Viewport
│   ├── math/                     # Coordinate transforms, raycasting, Bezier smoothing
│   └── history/                  # Command pattern Undo / Redo engine
├── design/
│   ├── theme/                    # Monochromatic colors, typography, theme composables
│   └── components/               # Monochromatic buttons, dialogs, cards
├── data/
│   ├── local/                    # Room Database, Notebook & Page DAOs, Entities
│   ├── preferences/              # DataStore repository (theme, default tools, palm rejection)
│   ├── storage/                  # DocumentStorageManager (atomic JSON), FileAssetManager
│   └── repository/               # Repository implementations
├── domain/
│   ├── model/                    # Domain entities (Notebook, Page)
│   └── repository/               # Domain repository interfaces
├── feature/
│   ├── home/                     # Workspace dashboard, notebook grid, search
│   ├── notebook/                 # Notebook detail, page cards, reordering
│   ├── canvas/                   # Infinite canvas engine, stylus processor, toolbar
│   └── settings/                 # Appearance, stylus calibration, about screen
└── navigation/                   # Type-safe Compose navigation routes & NavHost
```

---

## ✦ Building the APK via GitHub Actions

This repository is pre-configured with a production-ready **GitHub Actions CI/CD pipeline** located at [`.github/workflows/build-apk.yml`](.github/workflows/build-apk.yml).

### Automated CI Workflow
Every `push` to `main` and each `pull_request` automatically executes:
1. Environment initialization on `ubuntu-latest` with Java 17 Temurin and the official Android SDK.
2. Automated execution of unit test suites (`./gradlew testDebugUnitTest`).
3. Compilation and assembly of the debug APK (`./gradlew assembleDebug`).
4. Assembly of the unsigned release APK (`./gradlew assembleRelease`).
5. Artifact publishing via `actions/upload-artifact@v4`.

### Downloading the Built APK
1. Push your changes or repository to GitHub:
   ```bash
   git remote add origin https://github.com/<your-username>/canvotes.git
   git push -u origin main
   ```
2. Navigate to the **Actions** tab on your GitHub repository.
3. Select the latest workflow run: **"Build Canvotes Android APK"**.
4. Scroll down to the **Artifacts** section at the bottom of the summary page.
5. Click **`canvotes-debug-apk`** to download the compiled `.apk` file directly to your phone or computer.

### Manual Workflow Dispatch
You can also manually trigger a fresh APK build at any time:
1. Go to **Actions** → **"Build Canvotes Android APK"**.
2. Click **"Run workflow"** → select branch `main` → click **"Run workflow"**.

---

## ✦ Local Building & Testing

To run unit tests locally:
```bash
./gradlew testDebugUnitTest
```

To build the debug APK locally (when Android SDK is available):
```bash
./gradlew assembleDebug
```

Output APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## ✦ Verification Documents
* [`GDD.md`](GDD.md) — Product & Design Specification
* [`ARCHITECTURE.md`](ARCHITECTURE.md) — Architectural Blueprint
* [`IMPLEMENTATION_PLAN.md`](IMPLEMENTATION_PLAN.md) — Sprint Tracking
* [`CHANGELOG.md`](CHANGELOG.md) — Version Release History
* [`VERIFICATION_REPORT.md`](VERIFICATION_REPORT.md) — Quality & Testing Report
