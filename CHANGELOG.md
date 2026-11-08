# Changelog

## 0.3.1 — multi-version release

First release that builds for more than one Minecraft version from one tree.

### Targets

| Target | Minecraft | Mappings | Java | Notes |
|--------|-----------|----------|------|-------|
| `:mc1211` | 1.21.1 | Yarn | 21 | fabric-loom-remap |
| `:mc262` | 26.2 | Mojang | 25 | GLFW input |
| `:mc263` | 26.3 | Mojang | 25 | SDL3 input |

### Shared

- Pure core (`:core`, Java 21): SurveyEngine, DistanceScale (GRID / GROUND / GEODESIC), Vincenty geodesic, brand-agnostic projections (WebMercator, Equirectangular, EqualEarth), units, angle format.
- Occupy via Transit or `/lhc occupy`. Measure via V or `/lhc rod|scope`.
- Live HUD: azimuth or quadrant bearing, zenith (VA), scaled horizontal / ΔN / ΔE, unscaled cut-fill.
- CSV shot log (PNEZD, 2 decimal places). `/lh reset` and `/lhc reset` clear occupy and start a new file.
- `/lh` and `/lhc` trees: units, scale, projection, hud angle, reset.
- Client-only `/lhc` works on third-party SMP.
- Spyglass uses the exact raycast hit, not the block center.
- Gradle wrapper 9.7.0. `prepareBuild.bat` stages each target into its own zip folder (src, libs, logs, shots).

### mc263

- New target, scaffolded from mc262.
- Minecraft 26.3, Fabric loader 0.19.5, Loom 1.18-SNAPSHOT, Fabric API 0.162.0+26.3.
- Input moved from GLFW to SDL3: `InputConstants.Type.KEYBOARD` and `InputConstants.KEY_*`.
- Keys verified (zoom, save shot, description).

### Not in this release

- Spyglass FOV zoom (key is bound; client mixin is empty).
- Organic / ground filter (abandoned).
- Projection smoke test against a Tellus / OSM world (code is in core; not yet verified in-game).
