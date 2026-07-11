# VisualOdometry GUI

Swing GUI (2014, modernized 2026) for BoofCV visual odometry. Quarkus is used as CDI/config
runtime only — there is no web endpoint; the "app" is the Swing UI itself.

## Build & run

- Java 25
- Build with plain `mvn <goal>`. Root `settings.json`/`settings.yaml` are the application's
  saved settings (gitignored user state), read/written by the in-app Load/Save Settings.
- Don't run `quarkus dev` expecting a headless service: startup opens Swing windows.

## Conventions

- Logging: `io.quarkus.logging.Log` (`Log.errorf`, `Log.warnf`, ...). Never `printStackTrace`
  or `System.err` — and never swallow exceptions silently: two GUI bugs stayed invisible for
  years behind `catch (Exception ignored)`.
- Lombok with **fluent accessors** (`lombok.config`): settings expose `device().type()`,
  `type(value)` — not `getType()`.
- Layering: `models` must not import `core` or `gui`. Settings are pure persisted data;
  anything asked to the hardware (device lists, resolutions) goes through the
  `core.integration.discovery.DeviceDiscovery` singletons — never back into settings.
  Deliberate exception: `context.state.State` is the app's shared runtime blackboard
  and references gui/core types by design.
- No absolute machine-local paths in committed files; no hardcoded default asset paths —
  video/calibration lists start empty and users add paths via GUI.
- Commits: conventional one-liner + short bullets (max 130 chars each), no trailers/footers.

## Settings persistence (why it's shaped this way)

- Two formats via Jackson: JSON (primary) and YAML, switchable in-app (choice is session-only,
  never persisted). Field-based mapping (fluent Lombok accessors are not bean getters);
  deserialization goes through the no-arg constructors, so fields missing from the file keep
  their defaults. Legacy XStream XML and Java-serialized `.dat` were dropped in July 2026.
- Resolutions persist as raw `targetWidth`/`targetHeight` ints. `DeviceResolution` (named
  standards: QVGA, HD, ...) and `CustomResolution` are presentation-only, reconstructed on read.
- An empty persisted device path means "first available": healed at GUI level via discovery.

## GUI architecture traps

- `GuiApplication` is a single large class. Components are shared through a string-keyed
  `guiComponents` map: a `get` with a key nobody `put` returns null and typically dies inside
  a swallowed catch. When touching keys, cross-check both sides.
- `MainButtonListener` dispatches on the function name passed to its constructor — do not
  reintroduce dispatch via Swing `actionCommand` (defaults to the component's label text).
- The device path/resolution combos are populated via `DeviceDiscovery`, with the
  `DeviceResolution` enum as fallback when the device can't be queried. Capture-time
  nearest-resolution adjustment (app-level in `BoofCvCamera`, kernel-level for V4L)
  stays as the safety net — keep both layers.

## Platform constraints (macOS ARM)

- DeviceType: BoofCv (default) > OpenCv > V4L4J (deprecation candidate). BoofCv capture works
  only via the eduramiba native driver (sarxos drop-in; BridJ/stock sarxos are dead, no ARM64).
- OpenCv path: per-platform opencv/openblas classifier natives, never javacv-platform (~1 GB).
  OpenCV has no device/resolution enumeration by design (ids are bare capture indices, granted
  size read back from the first frame) — deliberately NOT mapped onto the other drivers'
  enumeration: order mismatch would open the wrong camera silently. Keep the stacks decoupled.
- V4L4J is Linux-only (`/dev/video*`); on macOS its code paths must fail soft. The app does
  NOT load its natives: the v4l4j jar calls `System.loadLibrary("v4l4j")` itself, so on Linux
  `libv4l4j.so`/`libvideo.so` must be in a system lib dir or in `-Djava.library.path` at launch.
- Calibrations are YAML-only (`CalibrationIO`); legacy XMLs were converted in July 2026.
