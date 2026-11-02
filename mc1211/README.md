# mc1211 — Minecraft 1.21.1 Fabric target

Included in `settings.gradle`. Depends on `:core`.

## Status

Gradle scaffolding is in place. Sources are not yet ported.

The `1.21.1` branch is the old 0.2.6 tree (`me.bluejay.client`, own math, Yarn). It does not use `:core` and cannot be copied in as-is.

## To finish

Port the current `mc262` Fabric layer to 1.21.1 API shapes:

- Java 21, Yarn mappings, Loom 1.15
- `keyPressed(int, int, int)` instead of `KeyEvent`
- `client.setScreen` instead of `gui.setScreen`
- Any other 1.21.1 API deltas

Put version-only files in `mc1211/src`. Put code shared with 26.2 in `shared/common` or `shared/pre-26` once a second consumer exists.

Until sources exist, `./gradlew :mc1211:build` configures but has no entrypoints to compile.
