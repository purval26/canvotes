# Changelog

All notable changes to the **Canvotes** project are documented here following [Keep a Changelog](https://keepachangelog.com/en/1.0.0/) and [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.0.0] - 2026-11-08

### Added
- **Infinite Canvas Engine**:
  - World-to-screen coordinate transformation matrix with continuous zoom ($0.1\times$ to $5.0\times$) and pan.
  - Dynamically drawn infinite polka-dot background grid with zoom-invariant dot sizing and anti-moiré subsampling.
  - Two-finger pan and pinch-to-zoom multi-touch navigation.
- **Stylus & Handwriting Engine**:
  - Distinction between stylus (`TOOL_TYPE_STYLUS`, `TOOL_TYPE_ERASER`) and finger contacts.
  - Pressure sensitivity with dynamic line thickness modulation.
  - Quadratic Bezier curve smoothing with sub-pixel jitter filter.
  - Four native drawing tools: Pen, Pencil, Highlighter, and Eraser.
  - Palm rejection heuristic rejecting large touch contact areas while writing.
- **Canvas Objects**:
  - In-canvas editable Textboxes with inline bold and italic toggles.
  - Geometric shape tools: Rectangle, Rounded Rectangle, Circle, Ellipse, Line, and Arrow.
  - Image import via Android Photo Picker / SAF with aspect ratio locking and corner resize handles.
  - Multi-page PDF import and annotation framework.
- **Workspace Organization**:
  - Home screen with monochromatic notebook card grid, search bar, and creation modal.
  - Notebook detail screen with page grid, reordering, duplication, and deletion.
  - Page quick-navigation bar with previous/next controls and auto-save status.
- **Local-First Architecture**:
  - SQLite / Room database for metadata tracking.
  - Atomic JSON document serialization with temporary swap files to prevent write corruption.
  - Sandboxed internal storage for imported image and PDF assets.
  - Reactive preferences management via Jetpack DataStore.
  - Command pattern Undo/Redo stack with deep state restoration.
- **Monochromatic Design System**:
  - Editorial, distraction-free visual identity using black, white, and calibrated neutral grays.
  - Full support for Light Mode, Dark Mode, and System Theme synchronization.
- **CI/CD & Automated Verification**:
  - GitHub Actions workflow (`.github/workflows/build-apk.yml`) for automated unit testing, debug APK assembly, and release APK assembly.
  - Comprehensive unit test suite covering coordinate mathematics, Bezier smoothing, command history, and document serialization.
