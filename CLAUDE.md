# VisualOdometry GUI

Desktop GUI (2014, modernized 2026) for BoofCV visual odometry. Two toolkits live side by side:
JavaFX (`config.ui=JavaFx`, the default) and the legacy Swing UI (still available as an
alternative). Quarkus is used as CDI/config runtime only — there is no web endpoint; the "app" is
the desktop UI itself.

## Build & run

- Java 25
- Build with plain `mvn <goal>`. Root `settings.json`/`settings.yaml` are the application's
  saved settings (gitignored user state), read/written by the in-app Load/Save Settings.
- Don't run `quarkus dev` expecting a headless service: startup opens desktop windows.

## Conventions

- Logging: `io.quarkus.logging.Log` (`Log.errorf`, `Log.warnf`, ...). Never `printStackTrace`
  or `System.err` — and never swallow exceptions silently: two GUI bugs stayed invisible for
  years behind `catch (Exception ignored)`.
- Lombok with **fluent accessors** (`lombok.config`): settings expose `device().type()`,
  `type(value)` — not `getType()`.
- Layering: `models` must not import `core` or `gui`. Settings are pure persisted data;
  anything asked to the hardware (device lists, resolutions) goes through the
  `core.integration.discovery.DeviceDiscovery` singletons — never back into settings.
  Core→GUI goes ONLY through `core.rendering.RenderSink` (impls:
  `gui.swing.rendering.SwingRenderSink`, `gui.fx.rendering.FxRenderSink`): core never touches
  widgets, dialogs or GUI state. Runtime-negotiated values reach settings via
  `core.rendering.SettingsSync` (mutation core-side, widget refresh via sink).
  Each toolkit keeps its own GUI state in its own package (`gui.swing.state.GuiState`,
  `gui.fx.state.GuiState`); `context.state.State` holds core runtime signals only
  (remaining deliberate exception: it references the core `BufferedCamera` handle).
  App-identity strings shared by both UIs live in `models.constants.AppConstants`;
  Swing-only constants are in `gui.swing.GuiConstants`. `javax.swing` appears nowhere
  in core/factory/models/utilities (`java.awt` imaging types like `BufferedImage` are
  fine: they are data, not widgets).
- No absolute machine-local paths in committed files; no hardcoded default asset paths —
  video/calibration lists start empty and users add paths via GUI.
- English only, always: never Italian (nor any other language) in code, comments, commit
  messages, docs or identifiers.
- Commits: conventional one-liner ONLY, no body, no trailers/footers. Exception: a commit
  spanning multiple significant features may add max 1-2 super concise bullets (≤130 chars).

## Settings persistence (why it's shaped this way)

- Two formats via Jackson: JSON (primary) and YAML, switchable in-app (choice is session-only,
  never persisted). Field-based mapping (fluent Lombok accessors are not bean getters);
  deserialization goes through the no-arg constructors, so fields missing from the file keep
  their defaults. Legacy XStream XML and Java-serialized `.dat` were dropped in July 2026.
- Resolutions persist as raw `targetWidth`/`targetHeight` ints. `DeviceResolution` (named
  standards: QVGA, HD, ...) and `CustomResolution` are presentation-only, reconstructed on read.
- An empty persisted device path means "first available": healed at GUI level via discovery.

## GUI architecture traps

### JavaFX (default, `gui.fx`)

- Package-by-feature under `gui.fx.features.{shell, sidebar, toolbar, trajectory, video}`; the sidebar
  splits into `settings.{input,image,tracker,visualodometry,chart}` and `telemetry.{processing,
  odometry,tracking,framerate,trackedpoints}`. Each feature is **MVVM**: an FXML view (under
  `resources/gui/fx/...`) + a `XxxController` (thin, `@Dependent @Unremovable` CDI bean, binding only)
  + a hand-rolled `XxxViewModel` exposing JavaFX properties that live-commit into the domain settings.
  Trajectory/telemetry that the core drives are **humble views** fed via the event stream, not MVVM forms.
- Core→GUI is the same `RenderSink` rule as Swing: `gui.fx.rendering.FxRenderSink` emits pure-data
  `gui.fx.state.TrajectoryEvent`s (and coalesced status/frame ops) onto the FX thread; the sink never
  touches widgets. `gui.fx.state.GuiState` holds the FX-side handlers/state; `FxApplication` is the
  composition root. Reusable, app-blind widgets live in `gui.fx.shared` (`charting`, `components`,
  `behaviors`, `converters`); toolkit-agnostic math stays in `utilities` (`OdometryMathUtils`).
- Charts: `gui.fx.shared.charting.TrajectoryChart` wraps a `LineChart` over two `StableTicksAxis` with
  vendored **gillius/jfxutils** pan/zoom (`shared.charting.jfxutils`, Apache headers kept, dir-scoped
  `lombok.config` for the fluent-accessor trap). AUTO vs MANUAL mode is tracked by each axis'
  `autoRanging` flag (single source of truth). The per-axis initial zoom is an explicit choice — a fixed
  `scale`, or `autoScaleXZ`/`autoScaleY` for auto-range (NO magic value; `scale` is always literal).

### Swing (legacy alternative, `gui.swing`)

- Swing is organized package-by-feature under `gui.swing.features.{controlpanel, dashboard, video}`
  as **humble views**: each `XxxView` owns its widgets as private typed fields and exposes intent
  methods (`load()`, `show*()`, `setRunning()`, ...) — widgets never leave the view. The two windows
  are **facades that own their `JFrame`** and compose sub-views: `dashboard.DashboardView` →
  `trajectory` + `info.InfoView` → the `info.{processing,odometry,tracking,fps,buffer,trackedpoints}`
  sections; `controlpanel.ControlPanelView` → `toolbar` + `settings.SettingsView` → per-section views,
  and it owns the app-level dialogs (`showErrorDialog`/`showConfirmDialog`, parented to its frame).
  `SwingApplication` is only the composition root (creates the two window facades + video view, wires
  base look-and-feel); `SwingRenderSink` and the vo commands (`toolbar.VoController`) talk to views
  through intents, never to raw widgets — no bare `JFrame` anywhere in `GuiState`/sink.
  `gui.swing.state.GuiState` holds exactly the window facades (`controlPanelView`, `dashboardView`)
  plus `videoView`, all typed: the old string-keyed `guiComponents` map and the loose `mainFrame` are
  GONE — do not reintroduce them.
- Pure, dumb, reusable widgets live in `gui.swing.shared` (`components/`, `editors/`, `listeners/`,
  `renderers/`): they never import `AppContext`/settings/sink — they take injected callbacks
  (`Consumer`/`Supplier`), so the feature wires e.g. `voSettings::type` and the widget stays app-blind.
  Feature-specific Swing helpers (a domain `ListCellRenderer`, a feature's mouse/selection listeners)
  colocate with their feature, NOT in `shared` (e.g. `features.dashboard.info.trackedpoints`). Pure
  telemetry math is toolkit-agnostic in `utilities` (`OdometryMathUtils`).
- Toolbar/settings commands are the command pattern (button → `VoController`/
  `SettingsMenuController`/view intent) — do NOT reintroduce string dispatch on a function
  name, nor Swing `actionCommand` (defaults to the component's label text).
- The device path/resolution combos are populated via `DeviceDiscovery`, with the
  `DeviceResolution` enum as fallback when the device can't be queried. Capture-time
  nearest-resolution adjustment (app-level in `BoofCvCamera`, kernel-level for V4L)
  stays as the safety net — keep both layers.

## Platform constraints (macOS ARM)

- DeviceType: BoofCv (default) > OpenCv > V4L4J (deprecation candidate). Both BoofCv and OpenCv
  capture work on Apple Silicon; V4L4J does not (Linux-only). BoofCv capture works only via the
  eduramiba native driver (sarxos drop-in; BridJ/stock sarxos are dead, no ARM64).
- OpenCv path: per-platform opencv/openblas classifier natives (macosx-arm64 included, so OpenCv
  capture runs on Apple Silicon too), never javacv-platform (~1 GB).
  OpenCV has no device/resolution enumeration by design (ids are bare capture indices, granted
  size read back from the first frame) — deliberately NOT mapped onto the other drivers'
  enumeration: order mismatch would open the wrong camera silently. Keep the stacks decoupled.
- V4L4J is Linux-only (`/dev/video*`); on macOS its code paths must fail soft. The app does
  NOT load its natives: the v4l4j jar calls `System.loadLibrary("v4l4j")` itself, so on Linux
  `libv4l4j.so`/`libvideo.so` must be in a system lib dir or in `-Djava.library.path` at launch.
- Calibrations are YAML-only (`CalibrationIO`); legacy XMLs were converted in July 2026.
