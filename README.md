# LevelHeaded — multi-version layout

One repository, shared pure core, one Gradle subproject per Minecraft version.

```
core/          pure Java survey math (no Minecraft imports)
shared/        version-banded Fabric code (empty until a second target shares files)
mc262/         Minecraft 26.2 Fabric target
mc1211/        Minecraft 1.21.1 Fabric target (not yet included; see mc1211/README.md)
```

## Build

From the repository root (where the Gradle wrapper lives):

```
./gradlew :mc262:build
./gradlew :mc262:runClient
./gradlew buildAll
```

## Layout rules

- Core never imports Minecraft. That is the Bedrock-portable line.
- One file, one layer. If 26.2 and 26.3 share a class, it belongs in `shared/mc26`, not in both targets.
- `gradle.properties` and `fabric.mod.json` stay per target.
- Tags stay per target (`v0.3.1+26.2`, `v0.3.1+1.21.1`). `main` is the multi-version tree.

## What changed from the single-project tree

- `me.bluejay.levelheaded.core.*` moved to the `:core` project.
- `RaycastRange` stays in the Fabric layer (`mc262`) because it reads server view distance.
- `SurveyEngine` holds its own `DistanceScale`. `SurveyState` injects `SurveyState.SCALE` at startup.
- Everything else (items, blocks, commands, HUD, keybinds, CSV) is in `mc262`.
