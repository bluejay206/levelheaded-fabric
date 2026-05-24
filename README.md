# LevelHeaded

A realistic surveying mod for Minecraft 1.21.1 (Fabric).

## Features

- **Surveyor's Rod** and **Survey Scope** with real-time HUD
- Bearing, Azimuth, Zenith Angle (0° = straight up), Slope Distance, Horizontal Distance, ΔN/ΔE, Cut/Fill
- **Level Station** block — right-click to set occupy point.
- Press **V** in SHOT mode to save shots → clean PNEZD CSV export (in Meters)
- Press **H** to toggle HUD on/off
- `/lh` commands for session management and status

## Installation

1. Install **Fabric Loader 0.18.6+** for Minecraft 1.21.1
2. Download the latest `.jar` from [Releases](https://github.com/bluejay206/LevelHeaded-fabric/releases)
3. Place it in your `mods` folder

## Usage

- Place a **Level Station** and right-click it to set the occupy point.
- Hold the **Rod** or **Scope** → HUD appears automatically.
- Right-click while holding the tool to lock a shot.
- Press **V** to save the shot to CSV.
- Press **H** to hide/show the HUD.

## Commands
- /lh help                        - Show command list
- /lh new                         - Start a new CSV session
- /lh status                      - Show current occupy point
- /lh reset                       - Clear occupy point
- /lh units <meters|feet|chains>  - Display units in HUD


## Downloads

See the [Releases](https://github.com/bluejay206/levelleaded-fabric/releases) page.

## License

[CC0 1.0 Universal](LICENSE)
