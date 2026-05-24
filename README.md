# LevelHeaded

A realistic surveying mod for Minecraft 1.21.1 (Fabric).

## Features

- **Surveyor's Rod** and **Survey Scope** with real-time HUD
- Bearing, Azimuth, Zenith Angle (0° = straight up), Slope Distance, Horizontal Distance, ΔN/ΔE, Cut/Fill
- **Transit Station** block — right-click to set occupy point (persistent & multiplayer safe)
- Press **V** in SHOT mode to save shots → clean PNEZD CSV export
- Press **H** to toggle HUD on/off
- `/ss` commands for session management and status

## Installation

1. Install **Fabric Loader 0.18.6+** for Minecraft 1.21.1
2. Download the latest `.jar` from [Releases](https://github.com/bluejay206/LevelHeaded-fabric/releases)
3. Place it in your `mods` folder

## Usage

- Place a **Transit Station** and right-click it to set the occupy point.
- Hold the **Rod** or **Scope** → HUD appears automatically.
- Right-click while holding the tool to lock a shot.
- Press **V** to save the shot to CSV.
- Press **H** to hide/show the HUD.

## Commands
- /ss help          - Show command list
- /ss new           - Start a new CSV session
- /ss status        - Show current occupy point
- /ss list [n]      - Show last N shots
- /ss clear         - Clear current session shots
- /ss reset         - Clear transit point
text

## Known Issues
- Scope uses placeholder of spyglass

## Downloads

See the [Releases](https://github.com/bluejay206/levelleaded-fabric/releases) page.

## License

[CC0 1.0 Universal](LICENSE)