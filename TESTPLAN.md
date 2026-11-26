# LevelHeaded Test Plan

Release under test: 0.3.2 multi-version  
Targets: mc1211 (1.21.1 Yarn), mc262 (26.2 Mojang), mc263 (26.3 Mojang)

## Scope

| Target | Version | Mappings | Input |
|--------|---------|----------|-------|
| mc1211 | 1.21.1 | Yarn | GLFW. `wasPressed` / `isPressed`. Ctrl via `hasControlDown()` |
| mc262 | 26.2 | Mojang | GLFW. `consumeClick` / `isDown`. Ctrl = bit 2 |
| mc263 | 26.3 | Mojang | SDL. `shortcutKey()`. Ctrl = `0x00C0`, Shift = `0x0003` |

**In scope**

- Occupy (Transit, `/lhc occupy`, Points → Occupy)
- HUD (rod, spyglass raycast, azimuth / bearing, units)
- V saves the live shot to the point list, rate-limited
- Description (G)
- Commands (`/lh`, `/lhc`)
- Data Collector: Configuration, Points, Job
- Per-world persistence (`levelheaded/worlds/<name>/`)
- Multi-select, scroll, highlight
- Shot rate limiter

**Out of scope / deferred**

- P hotkey (removed). Direct CSV path (`ShotLog`, `/lhc rod|scope`) kept but not a primary path. Removal TBD.
- Zoom hotkey (removed, mixin artifact)
- Projection numeric check against a real projection mod
- Field-book item
- In-world point visualization
- Import duplicate and field-error handling
- Bedrock

**Remaining hotkeys:** V (save point), G (description).

---

## BVT — Build Verification

Run on every build, every target. Fail any of these and stop the FVT for that target.

### B1 — Build and launch

1. `./gradlew :mc1211:build`, `:mc262:build`, `:mc263:build` succeed.
2. `runClient` starts without a crash.
3. A new world loads. No `NoClassDefFoundError` for `SurveyEngine` or any core class.
4. Creative inventory shows the LevelHeaded tab: Rod, Transit, Data Collector.
5. Spyglass is the vanilla item.

### B2 — Core occupy path

1. Place a Transit. Right-click it.
2. Chat or action bar shows `Occupied TRANSIT#1 …`.
3. Data Collector → Points. The transit point is listed with description `TRANSIT#n`.
4. HUD appears and shows azimuth and distances.

### B3 — Point save

1. With occupy set and Rod in hand, press V once.
2. Action bar shows `Saved point N → TOPO`.
3. Points list gains one row.
4. Press P. Nothing happens. No new CSV from the key.

### B4 — Screens open

1. Right-click Data Collector. Hub shows Configuration, Points, Job, Close.
2. Configuration opens and closes. No scissor crash on mc262/mc263.
3. Points opens. Text is visible on all targets. Back returns.
4. Job opens. Back returns.

### B5 — Persistence smoke

1. Save a point. Quit to title. Rejoin the same world. Point and occupy are restored.
2. Create a second world. Points list is empty. First world is unchanged.

**Pass:** all five on the target. Any crash, missing item, or empty list after a confirmed save is a BVT fail.

---

## FVT — Functional Verification

### F1 — Occupy

| # | Step | Expected |
|---|------|----------|
| F1.1 | Right-click Transit | New point `TRANSIT#n`, description set, occupy set, message shown |
| F1.2 | Right-click Transit again | New id, previous point remains, occupy moves |
| F1.3 | `/lhc occupy` | Occupy set at player position, list unchanged |
| F1.4 | `/lhc occupy clear` | Occupy cleared, list unchanged, HUD stops measuring |
| F1.5 | Points → select a point → Occupy | Occupy moves to that point (N = −Z restored) |
| F1.6 | `/lhc reset` | Occupy cleared, list wiped, world files gone |

### F2 — HUD

| # | Step | Expected |
|---|------|----------|
| F2.1 | Hold Rod after occupy | HUD shows azimuth, Hz, ΔN, ΔE, cut/fill. Fine movement changes values |
| F2.2 | Hold Spyglass, look at a block | HUD uses the exact raycast hit, not the block center |
| F2.3 | Config or `/lhc hud angle bearing` | Switches to quadrant bearing |
| F2.4 | `azimuth` / `toggle` | Switches back / toggles |
| F2.5 | `/lhc hud off` then `rod` / `scope` | Hidden, then restored |
| F2.6 | Units m → ft → ch → mi → m | Values and suffix change. Status line matches |

### F3 — Scale and projection

| # | Step | Expected |
|---|------|----------|
| F3.1 | Scale = Grid | Distances match raw block distance |
| F3.2 | Scale = Ground, factor 1.0000 | Same as grid |
| F3.3 | Ground factor 0.5 | Horizontal distances halve |
| F3.4 | Scale = Geodesic | Status line shows geodesic |
| F3.5 | Switch projection | No crash. Status line shows the id. Numeric check deferred |

### F4 — Description

| # | Step | Expected |
|---|------|----------|
| F4.1 | Press G | Description screen opens |
| F4.2 | Enter text, confirm | Next V point uses that description |
| F4.3 | Clear description, press V | Point gets default (`rod` / `scope`) |
| F4.4 | G while a screen is open | Does not stack a second screen |

### F5 — Shot rate (V only)

| # | Step | Expected |
|---|------|----------|
| F5.1 | Default 60 Hz, hold V for 2 s | About 120 points. No burst after release |
| F5.2 | Set rate to 1 | About 1 point per second while held |
| F5.3 | Set rate to 0 | Unlimited. Release stops immediately |
| F5.4 | Tap V once at any rate | Exactly one point |
| F5.5 | Hold V, release | Saving stops on release. No buffered shots |

### F6 — CSV (commands only)

P is unbound. These confirm the leftover path still works, not that it is primary.

| # | Step | Expected |
|---|------|----------|
| F6.1 | `/lhc rod` with occupy | Row appended to `shots_*.csv`, 2 decimal places |
| F6.2 | `/lhc scope` on a block | Row uses exact hit position |
| F6.3 | `/lhc reset` then `/lhc rod` | New file, not appended |
| F6.4 | No occupy, `/lhc rod` | Rejected, no row |
| F6.5 | Press P | No action, no file |

### F7 — Points list

| # | Step | Expected |
|---|------|----------|
| F7.1 | Open Points with items | Header shows count and selected count. Text visible |
| F7.2 | Click a row | Only that row highlighted (solid bar + white text) |
| F7.3 | Shift-click another row | Range selected |
| F7.4 | Ctrl-click a selected row | That row deselects, others stay. mc263 uses SDL bits |
| F7.5 | Ctrl-click an unselected row | Added to selection |
| F7.6 | Ctrl+A | All selected |
| F7.7 | Arrow up/down | Selection moves, list follows |
| F7.8 | Home / End / Page Up / Page Down | Jump to start, end, page |
| F7.9 | Mouse wheel | View scrolls, selection does not change |
| F7.10 | Drag scrollbar | Selection jumps to the track position |
| F7.11 | Delete with multi-select | All highlighted rows removed, list and file update |
| F7.12 | Occupy with multi-select | Occupies the primary row only |

### F8 — Job screen

| # | Step | Expected |
|---|------|----------|
| F8.1 | New | List empty, occupy cleared, status confirms |
| F8.2 | Export | Path field prefilled. Save writes the current list |
| F8.3 | Import that file into a non-empty list | Rows appended. Duplicate handling deferred |
| F8.4 | Save as | Writes a CSV. In-memory list unchanged |
| F8.5 | Load | Replaces the list. Occupy file rewritten |
| F8.6 | Cancel on the path screen | Returns to Job, nothing changed |
| F8.7 | Bad path on Load | Status shows the failure, list unchanged |

### F9 — Persistence and worlds

| # | Step | Expected |
|---|------|----------|
| F9.1 | Add points, quit, rejoin same world | Restored from `levelheaded/worlds/<name>/` |
| F9.2 | Join a different world | Empty list, no occupy |
| F9.3 | Return to the first world | Original points back |
| F9.4 | `/lhc reset` | That world’s folder cleared. Others untouched |
| F9.5 | Multiplayer (if available) | Keyed by server address |

### F10 — Commands

| # | Step | Expected |
|---|------|----------|
| F10.1 | `/lhc` and `/lhc status` | Occupy, units, scale, HUD mode |
| F10.2 | `/lhc rod` and `/lhc scope` | Measure and report. Scope uses raycast |
| F10.3 | `/lh` units, scale, projection, hud angle, reset | Match the client commands where they overlap |
| F10.4 | `/lhc` on a third-party server | Works without the server-side mod |

### F11 — Target-specific

| # | mc1211 | mc262 | mc263 |
|---|--------|-------|-------|
| F11.1 | V point, G description. P and Z unbound | Same, GLFW | Same, SDL. Rebind in Controls if `options.txt` is stale |
| F11.2 | `drawText` | Text color must include `0xFF` alpha | Same as mc262 |
| F11.3 | Ctrl = `hasControlDown()` | Ctrl = bit 2 | Ctrl = `0x00C0`, Shift = `0x0003` |
| F11.4 | Config opens | No zero-height scissor | Same as mc262 |
| F11.5 | Transit point from block `onUse` | Transit point from client `UseBlockCallback` | Same as mc262 |
| F11.6 | Arrows via GLFW key code | Arrows via `event.key()` | Arrows via `event.shortcutKey()` |

### F12 — Removed keys

| # | Step | Expected |
|---|------|----------|
| F12.1 | Press P | No CSV, no point, no message |
| F12.2 | Press Z | No zoom, no FOV change |
| F12.3 | Controls → LevelHeaded | Only Save Point and Set Description listed |

---

## Execution

1. BVT on mc1211, then mc262, then mc263. Stop a target on any BVT fail.
2. FVT on each target that passed BVT. Record pass/fail per case id.
3. F5: count points over a timed hold. Compare to `rate × seconds` within about 10%.
4. F9: confirm `levelheaded/worlds/<sanitized-name>/points.csv` and `occupy.txt`.
5. Keep one log per target with the build zip.

## Exit criteria

All BVT pass on all three targets. All FVT cases pass, or have a logged deferral:

- Projection numeric check
- Import duplicate and field-error handling
- Direct CSV path removal (TBD)
- Field-book item
- In-world visualization
