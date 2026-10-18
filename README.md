# LevelHeaded

Surveying tools for Minecraft 26.2 (Fabric). Version 0.30.

## Features

- Surveyor's Rod and Transit Station
- Scope is the vanilla Spyglass: no occupy, it is vanilla; occupy set, the survey HUD and V save
- Live HUD: bearing, azimuth, horizontal distance, ΔN/ΔE, cut/fill
- Grid, ground, and geodesic scale, with Web Mercator, Equirectangular, and Equal Earth projections
- `/lh` and client-only `/lhc` command trees
- V saves the live shot to a PNEZD CSV (`shots_<timestamp>.csv`)

## Requirements

- Minecraft 26.2
- Fabric Loader 0.19.3+
- Fabric API
- Java 25

## Usage

- Place a Transit and right-click it to set the occupy point, or use `/lh occupy` / `/lhc occupy`.
- Hold the Rod for a position shot, or the Spyglass for a raycast shot.
- Press V to append the shot to the current CSV.
- `/lh reset` or `/lhc reset` clears occupy and starts a new CSV on the next V.

## License

[CC0 1.0 Universal](LICENSE)
