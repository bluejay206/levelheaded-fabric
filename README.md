# LevelHeaded

Surveying tools for Minecraft 26.2 (Fabric). Version 0.30.

## Features

- Surveyor's Rod and Transit Station
- Scope is the vanilla Spyglass: with no occupy it is vanilla; with occupy set, the survey HUD shows and V saves
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

- Place a Transit and right-click it to set the occupy point, or use `/lhc occupy`.
- Hold the Rod for a position shot, or the Spyglass for a raycast shot.
- Press V to append the shot to the current CSV.
- `/lh reset` or `/lhc reset` clears occupy and starts a new CSV on the next V.

## Commands

`/lh` is registered on the server. `/lhc` is client-only, so it works on a third-party SMP that does not have the mod.

### `/lh`

| Command | What it does |
| --- | --- |
| `/lh` | Status: occupy, units, scale |
| `/lh status` | Same as `/lh` |
| `/lh help` | Command list |
| `/lh reset` | Clear occupy |
| `/lh units` | Show display units |
| `/lh units <m\|ft\|ch\|mi>` | Set units (also accepts meter/feet/chain/mile) |
| `/lh scale` | Show the current scale |
| `/lh scale grid` | Grid scale: 1 block = 1 m |
| `/lh scale geodesic` | WGS84 Vincenty distance after the active projection inverse |
| `/lh scale world` | Show projected metres per block |
| `/lh scale world <m>` | Set projected metres per block |
| `/lh scale projection` | Show the active projection |
| `/lh scale projection <id>` | Set projection: `webmercator`, `equirectangular`, `equalearth` |
| `/lh scale ground <factor>` | Ground scale with a local factor |
| `/lh scale ground lat <deg>` | Ground factor from latitude (`cos(φ)`) |
| `/lh scale ground here` | Ground factor from your current Z |
| `/lh scale ground z <Z>` | Ground factor from a block Z |
| `/lh client` | Points at the client-only `/lhc` tree |

Projection aliases: `wm` / `mercator` / `3857` for Web Mercator, `eqc` / `equirect` / `platecarree` for Equirectangular, `ee` / `eqearth` for Equal Earth.

### `/lhc` (client-only commands, if not running on a server)

| Command | What it does |
| --- | --- |
| `/lhc` | Status |
| `/lhc status` | Same as `/lhc` |
| `/lhc reset` | Clear occupy |
| `/lhc occupy` | Set occupy at your position |
| `/lhc occupy here` | Same as `/lhc occupy` |
| `/lhc occupy clear` | Clear occupy |
| `/lhc rod` | Measure player position vs occupy |
| `/lhc scope` | Measure the raycast target vs occupy |
| `/lhc hud` | Show the HUD mode |
| `/lhc hud off` | HUD off (items still drive it when held) |
| `/lhc hud rod` | Force the rod HUD |
| `/lhc hud scope` | Force the scope HUD |

## Keys

| Key | Action |
| --- | --- |
| V | Save the live shot displayed in HUD to `shots_<timestamp>.csv` (PNEZD: Point, Northing, Easting, Elevation, Description) |

The CSV is written to `<gameDir>/levelheaded/`. The first V creates the file; later presses append. Reset starts a new file on the next V.

## License

[CC0 1.0 Universal](LICENSE)
