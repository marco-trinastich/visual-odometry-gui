# Visual Odometry GUI

Visual Odometry GUI is a cross-platform **Java desktop application** that provides an interactive graphical front-end for **monocular visual odometry**, built on top of the **[BoofCV](https://boofcv.org/)** computer-vision library. It processes **video files** or **live camera feeds**, estimates the camera's frame-by-frame egomotion, and renders the tracked features and the reconstructed **2D trajectory** in real time — offering an approachable playground to experiment with and visualize visual odometry.

## Table of Contents

1. [Project Overview](#project-overview)
2. [Features](#features)
3. [Screenshots](#screenshots)
4. [Environment Setup](#environment-setup)
   - [Prerequisites](#prerequisites)
   - [Clone the Repository](#clone-the-repository)
   - [Dataset](#dataset)
   - [Setup Steps](#setup-steps)
   - [Configuration](#configuration)
5. [Usage](#usage)
   - [How to Run](#how-to-run)
   - [Typical Workflow](#typical-workflow)
   - [Settings Reference](#settings-reference)
6. [Development](#development)
   - [Project Structure](#project-structure)
   - [Target Platforms](#target-platforms)
   - [Debugging](#debugging)
   - [Improvements](#improvements)
7. [Extras](#extras)
   - [FAQs](#faqs)
8. [Acknowledgments](#acknowledgments)
9. [Support](#support)
10. [License](#license)

## Project Overview

Visual Odometry GUI wraps BoofCV's **plane-based monocular visual-odometry** estimators in a Swing desktop application, turning a computer-vision library into a visual, real-time **tracking and mapping** tool.

Given a video source (a recorded file, an image-sequence folder, or a live camera) and the matching camera **calibration**, the application:

- feeds each frame to the selected **feature tracker** (KLT or SURF based) and **visual-odometry estimator**;
- computes the incremental camera motion and accumulates the global **pose** across the sequence;
- displays the live video with the tracked features overlaid, and plots the reconstructed **trajectory** (top-down X/Z map plus a Y/altitude graph) together with live processing statistics.

The project is intended for **research, learning, and experimentation** in visual odometry, giving an immediate visual feel for how a monocular VO pipeline behaves on real data. It was born as a university **thesis project (2014)** on tracking and mapping systems, originally targeting BoofCV 0.19, and has since been modernized to a current toolchain (**Java 25**, **Quarkus**, BoofCV 1.x).

## Features

### Monocular Visual Odometry

- Two plane-based monocular estimators powered by **BoofCV**:
  - **MonoPlaneInfinity** — plane + points-at-infinity model;
  - **MonoPlaneOverhead** — synthetic overhead (bird's-eye) plane model.
- Fully configurable parameters per algorithm (inlier tolerances, RANSAC iterations, retire thresholds, cell size, …), with sensible defaults one click away.
- Placeholders for stereo and depth-based odometry types (not implemented yet).

### Feature Tracking

- Selectable tracker: **KLT (Standard)**, **KLT (Modern)**, **SURF (Standard)**, **SURF (Dda Two Pass)**, or a preconfigured **Default** preset.
- Tunable tracker parameters (template radius, pyramid levels, max features, detection thresholds, …).
- Live overlay of **active** and **newly spawned** tracks on the video preview.

### Flexible Input Sources

- **Video files** (MJPEG, MP4, AVI) and **image-sequence folders** for offline processing.
- **Live cameras** through two backends:
  - **BoofCV / webcam-capture** — cross-platform webcam access;
  - **V4L4J** — Video4Linux devices (`/dev/video*`, auto-discovered) with extra device controls.
- Camera **calibration files** (BoofCV YAML / XML intrinsics), with a history of recently used calibrations and videos persisted across sessions.
- Configurable acquisition resolution, image type (`GrayU8` / `GrayF32`), optional resize and frame skipping.

### Real-Time Visualization

- **Video panel** — current frame with tracked features overlaid (full-resolution or internal processed image).
- **Trajectory charts** — top-down **X/Z map** and **Y (altitude)** per frame or per second, with adjustable scales.
- **Info panel** — live statistics: processing status, FPS, inliers, track counts, buffer usage.

### Processing Controls

- Start, **pause/resume**, reset, stop, and clear the visual-odometry run from the toolbar.
- **Timed processing** mode to stop automatically after a given duration.
- The processing core runs on **dedicated worker threads** with a buffered camera pipeline, keeping the GUI responsive.

### Settings Persistence

- All settings can be saved to and loaded from disk in **XML** (XStream) or **serialized** format (`settings.xml` / `settings.dat`), switchable from the GUI.
- Only meaningful state is persisted (selections, parameters, and input-path history) — device lists and available types are rediscovered at runtime.
- One-click **reset to defaults**.

## Screenshots

![Visual Odometry GUI](assets/screenshots/Visual%20Odometry%20GUI.jpg)

_The main window: trajectory charts (X/Z map and Y/altitude), and processing statistics on the left; input, image, tracker, and visual-odometry settings on the center; live video preview with tracked features on the right._

## Environment Setup

### Prerequisites

- **JDK 25** (the project targets Java 25; any recent distribution works, e.g. Temurin or Corretto — easily installed via [SDKMAN!](https://sdkman.io/)).
- **Apache Maven 3.9+** (or use an IDE with Maven support).
- Any OS with a graphical desktop: **Windows**, **macOS**, or **Linux**. Live-camera support varies by platform — see [Target Platforms](#target-platforms).

### Clone the Repository

```bash
git clone https://github.com/marco-trinastich/visual-odometry-gui.git
cd visual-odometry-gui
```

### Dataset

Video datasets and calibration files are **not included in the repository** (the `assets/datasets/` folder is git-ignored to keep the repo lightweight). The app's default input lists point to:

```
assets/datasets/
├── boofcv/applet/vo/drc/          # BoofCV sample: left_mono.mjpeg + mono_plane.yaml
└── vogui/
    ├── calibrations/mono/         # Camera intrinsics (XML/YAML)
    └── media/                     # Recorded test videos / image sequences
```

To get started:

1. A ready-made sample (video + matching calibration) is available among the **[BoofCV example datasets](https://github.com/lessthanoptimal/BoofCV-Data)** — the classic `vo/drc` monocular sequence (`left_mono.mjpeg` with `mono_plane.yaml`) works out of the box when placed under `assets/datasets/boofcv/applet/vo/drc/`.
2. Alternatively, use **your own videos**: record a sequence with a camera you have calibrated (BoofCV provides [calibration tools](https://boofcv.org/index.php?title=Tutorial_Camera_Calibration)) and add both the video and the intrinsics file from the GUI — the paths are remembered in `settings.xml`.

> **Calibration matters:** monocular plane-based VO needs intrinsics that actually match the camera (and resolution) that produced the video. A mismatched calibration is the most common cause of poor or failing estimates.

### Setup Steps

The project is a standard **Maven** build with the **Quarkus** application framework:

```bash
# Compile and run the test suite
mvn clean install

# Or just build, skipping tests
mvn clean package -DskipTests
```

The build produces a Quarkus fast-jar under `target/quarkus-app/`.

### Configuration

- **Application settings (runtime):** everything is configurable from the GUI and persisted on demand to `settings.xml` (or `settings.dat`) in the working directory — see [Settings Persistence](#settings-persistence).
- **Framework configuration (build-time):** `src/main/resources/application.properties` defines the settings file name and the XStream deserialization allow-list:

  ```properties
  config.settings.file-name=settings
  config.settings.allowed-xml-classes=com.mtm.vogui.**,java.**,boofcv.**,org.apache.commons.**
  ```

- **Headless flag:** Quarkus dev mode forces `java.awt.headless=true` by default, which would prevent the Swing GUI from opening. The `quarkus-maven-plugin` is already configured with `-Djava.awt.headless=false` in `pom.xml` — keep it if you customize the build.

## Usage

### How to Run

```bash
# Development mode (live reload)
mvn quarkus:dev

# Or run the packaged application
mvn clean package -DskipTests
java -Djava.awt.headless=false -jar target/quarkus-app/quarkus-run.jar
```

The main window opens with the chart area/settings panels on the left and the video area on the right.

### Typical Workflow

1. **Input** — choose the source type (*Video* or *Device*), pick a video/sequence or a camera, and select the matching **calibration** file.
2. **Image** — optionally adjust the internal image type, resize, and frame skipping.
3. **Tracker** — pick a tracker type and tune its parameters (or keep the Default preset).
4. **Visual Odometry** — pick the VO algorithm and its parameters.
5. Press **Start** and watch the tracked features and the trajectory build up in real time; use **Pause / Reset / Stop / Clear** (or a **timed run**) to control the process.
6. **Save** your configuration from the toolbar to have it reloaded automatically at the next launch.

### Settings Reference

| Group               | What it controls                                                                                  |
| ------------------- | ------------------------------------------------------------------------------------------------- |
| **Input**           | Source type (video / device), video & calibration paths (with history), device type, resolution, preview. |
| **Image**           | Internal image type (`GrayU8` / `GrayF32`), keep-original vs. resize, frame skipping.              |
| **Tracker**         | Tracker type (KLT / SURF variants / Default) and per-tracker parameters; track overlay toggles.    |
| **Visual Odometry** | Algorithm (MonoPlaneInfinity / MonoPlaneOverhead) and per-algorithm parameters.                    |
| **Chart**           | Y-axis mode (per frame / per second) and X/Z & Y chart scales.                                     |

## Development

### Project Structure

```
├── src/main/java/com/mtm/vogui/
│   ├── VisualOdometryGui.java   # Quarkus main entry point
│   ├── core/          # Processing engine: setup, validation, processing loop, rendering
│   │   └── integration/   # Camera backends (BoofCV, V4L4J) and buffered pipeline
│   ├── factory/       # Tracker and visual-odometry factories (BoofCV wiring)
│   ├── gui/           # Swing application: components, listeners, renderers, editors
│   ├── models/        # Settings model, config mapping, enums, constants, interfaces
│   └── utilities/     # Shared helpers
├── src/main/resources/application.properties
├── src/test/java/     # Unit tests (settings save/load round-trip)
├── assets/
│   ├── datasets/      # Videos & calibrations (git-ignored, provided separately)
│   └── screenshots/   # Images used by this README
├── settings.xml       # The app's own persisted settings (not a Maven file!)
└── pom.xml            # Maven + Quarkus build definition
```

The application is CDI-managed (**Quarkus ArC / Jakarta CDI**): settings, GUI, and core engine are injected beans; **Lombok** keeps the model classes lean.

### Target Platforms

The GUI itself runs anywhere Java and Swing do (**Windows / macOS / Linux**, including ARM). Camera backends differ:

- **BoofCV / webcam-capture** — Windows, Linux, and Intel macOS. Webcam discovery is **not available on Apple Silicon** (the underlying BridJ library ships no macOS ARM64 natives), so on those machines use video-file input.
- **V4L4J** — **Linux only** (Video4Linux). Device nodes are discovered by scanning `/dev/video*`.

Video-file processing works on every platform.

### Debugging

- `mvn quarkus:dev` gives live reload and the Quarkus dev tooling; the Swing window opens thanks to the headless override in `pom.xml`.
- From an IDE, run the `VisualOdometryGui` main class (make sure `-Djava.awt.headless=false` is set if you launch through a Quarkus run configuration).
- Settings load/save problems are logged with their cause (e.g. `Error loading settings from: settings.xml (…)`); deleting a stale `settings.xml` regenerates it from defaults.

### Improvements

Potential future enhancements:

- **Stereo and depth-based odometry** (the type placeholders already exist in the GUI).
- **Trajectory export** (CSV/TUM format) for offline evaluation against ground truth.
- **Map persistence** and loop-closure experiments on top of the 2D map.
- **Headless/batch mode** for processing sequences without the GUI.

## Extras

### FAQs

**Q: The app logs `Error loading settings from: settings.xml` at startup.**
A: The settings file on disk is stale or corrupted (e.g. produced by an older version). Delete it — the app starts from defaults and recreates it at the next save.

**Q: No webcams appear in the device list on my Mac.**
A: On Apple Silicon the webcam-capture/BridJ stack has no native libraries, so discovery is disabled by design. Use video-file input, or a Linux/Windows/Intel-mac machine for live cameras.

**Q: The GUI never opens when running through Quarkus.**
A: Quarkus forces AWT headless mode by default. The provided `pom.xml` already overrides it for `quarkus:dev`; for packaged runs launch with `-Djava.awt.headless=false`.

**Q: The trajectory drifts or the estimator keeps failing.**
A: Check the calibration first — it must match the camera and resolution of the input. Then try increasing the tracker's max features or the RANSAC iterations, and make sure the scene has enough texture and a dominant ground plane (the estimators are plane-based).

## Acknowledgments

This project builds upon excellent open-source work:

- **[BoofCV](https://boofcv.org/)** — the computer-vision library by Peter Abeles providing the visual-odometry estimators, trackers, and calibration tooling.
- **[Quarkus](https://quarkus.io/)** — application framework and CDI container.
- **[webcam-capture](https://github.com/sarxos/webcam-capture)** and **[V4L4J](https://github.com/sarxos/v4l4j)** — live camera access.
- **[JavaCV](https://github.com/bytedeco/javacv)** — media decoding support.
- **[XStream](https://x-stream.github.io/)** — XML settings persistence.
- **[Lombok](https://projectlombok.org/)** — boilerplate reduction.

## Support

If you find this project useful, consider supporting its development:

- ⭐ Star the repository to show your appreciation.
- 💬 Share feedback or suggestions by opening an issue.
- ☕ [Buy me a coffee](https://buymeacoffee.com/mtmarco87) to support future updates and improvements.
- 🔵 BTC Address: `bc1qzy6e99pkeq00rsx8jptx93jv56s9ak2lz32e2d`
- 🟣 ETH Address: `0x38cf74ED056fF994342941372F8ffC5C45E6cF21`

## License

This project is licensed under the **GNU General Public License v3.0**. See the [LICENSE](LICENSE) file for the full text.

Copyright (c) 2014–2024 Marco Trinastich.
