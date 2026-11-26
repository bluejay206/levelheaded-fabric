# Changelog

## 0.3.2

Point management moves into the Data Collector. The direct-to-CSV hotkey and the zoom hotkey are removed.

### Added

- `PointStore` and `ControlPoint` in core. One list. Purpose of a point is its description.
- Data Collector item. Right-click opens the hub: Configuration, Points, Job.
- Configuration screen: units, scale, projection, HUD angle, shot rate.
- Points screen: single list, multi-select (click, Shift-range, Ctrl-toggle, Ctrl+A), keyboard navigation (arrows, Home, End, Page Up, Page Down), mouse-wheel scroll that does not move the selection, drag scrollbar, solid highlight bar.
- Job screen: New, Load, Save as, Import, Export. Path entry is in-game (no AWT).
- `JobStore`: point list and occupy persist per world under `levelheaded/worlds/<name>/`. Cleared on reset or Job → New. Does not bleed across worlds.
- Shot rate limiter. Default 60 Hz, `0` = unlimited. Uses client tick time. Hold V to repeat, release stops immediately.
- Transit right-click creates a `TRANSIT#n` point and sets occupy. On 26.x this runs from a client `UseBlockCallback` because `useWithoutItem` is server-side.
- V saves the live shot to the point list (`SAVE_POINT`).

### Changed

- Primary point path is the Data Collector. V is the only remaining shot hotkey.
- Occupy feedback on 26.x is a chat line (`sendSystemMessage`), because the action bar does not render under a screen.
- Points text on 26.x uses ARGB colors (`0xFF……`) so glyphs are visible.
- mc263 Ctrl/Shift detection uses SDL modifier bits (`0x00C0` / `0x0003`).

### Removed

- P hotkey (`SAVE_SHOT`). Direct CSV from a key is gone. `ShotLog` and `/lhc rod|scope` remain until that path is removed.
- Z hotkey (zoom). It was a leftover from the removed FOV mixin.

### Fixed

- Config screen zero-height scissor crash on 26.x (label buttons were 14 px).
- Transit point not appearing in the list on 26.x (server-only block use).
- Shot rate not applied, and held keys buffering into a burst after release.
- Ctrl-click not toggling on mc263.
- Point list and occupy bleeding into a new world.

## 0.3.1

First multi-version cut. Same mod version for mc1211, mc262, and mc263.

### Added

- Multi-version Gradle layout: `core/` (pure Java 21), `mc1211/` (1.21.1 Yarn), `mc262/` (26.2 Mojang), `mc263/` (26.3 Mojang, SDL3).
- Shared core: SurveyEngine, DistanceScale, Geodesic (Vincenty), projections (WebMercator, Equirectangular, EqualEarth), units, angle format.
- Fabric layer per target: Rod, Transit, vanilla Spyglass scope, occupy, live HUD (azimuth/bearing, VA, scaled Hz/ΔN/ΔE, cut-fill), PNEZD CSV shot log, `/lh` and `/lhc` command trees.
- Core nested as a jar-in-jar so live worlds load.
- `prepareBuild.bat` stages per-target folders in the source zip.

### Notes

- mc263 uses SDL3 (`InputConstants.Type.KEYBOARD`, `InputConstants.KEY_*`). No GLFW.
- Wrapper is Gradle 9.7.0.
- Projection numeric check against a real projection mod is deferred.
