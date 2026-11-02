# mc1211 — Minecraft 1.21.1 Fabric target

This directory is the 1.21.1 version project. It is not included in `settings.gradle` yet.

To bring it online:

1. Copy the existing 1.21.1 tree (the `/1.21.1` branch or folder) into `mc1211/src/`.
2. Add a `mc1211/build.gradle` modeled on `mc262/build.gradle`, with 1.21.1 mappings, Fabric API, and Java 21.
3. Add `mc1211/gradle.properties` with `minecraft_version=1.21.1` and the matching loader / API.
4. Uncomment `include 'mc1211'` in the root `settings.gradle`.
5. Put any 1.21.1-only API shims (old `keyPressed(int,int,int)`, `client.setScreen`) in `mc1211/src` or in `shared/pre-26/` once a shared layer is needed.

The pure core (`:core`) is already version-free and is consumed by every target.
