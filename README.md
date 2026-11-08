# LevelHeaded 0.3.1 — multi-version release

One repository, shared pure core, one Gradle subproject per Minecraft version.

```
core/          pure Java survey math (no Minecraft imports)
shared/        version-banded Fabric code (empty until targets share files)
mc1211/        Minecraft 1.21.1 (Yarn, Java 21)
mc262/         Minecraft 26.2 (Mojang, Java 25)
mc263/         Minecraft 26.3 (Mojang, Java 25, SDL3 input)
```

Release 0.3.1 is the first multi-version cut. The same mod version ships for all three targets.

## Build

From the repository root (where the Gradle wrapper lives):

```
./gradlew :mc1211:build
./gradlew :mc262:build
./gradlew :mc263:build
./gradlew buildAll
```

Or `prepareBuild.bat -1211 | -263 | -all`.

## Layout rules

- Core never imports Minecraft. That is the Bedrock-portable line.
- One file, one layer. If 26.2 and 26.3 share a class, it belongs in `shared/mc26`, not in both targets.
- `gradle.properties` and `fabric.mod.json` stay per target.
- Tags stay per target (`v0.3.1+1.21.1`, `v0.3.1+26.2`, `v0.3.1+26.3`). `main` is the multi-version tree.

## 0.3.1 contents

- Shared core: SurveyEngine, DistanceScale, Geodesic (Vincenty), projections (WebMercator, Equirectangular, EqualEarth), units, angle format.
- Fabric layer per target: Rod, Transit, vanilla Spyglass scope, occupy, live HUD (azimuth/bearing, VA, scaled Hz/ΔN/ΔE, cut-fill), PNEZD CSV shot log, `/lh` and `/lhc` command trees.
- mc263: SDL3 input (`InputConstants.Type.KEYBOARD`, `InputConstants.KEY_*`). No GLFW.
- Wrapper: Gradle 9.7.0.
