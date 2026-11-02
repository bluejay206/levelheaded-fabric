# shared — version-banded Fabric code

Place Fabric code that is identical across targets here, then compose it into each target's source set.

Suggested bands:

- `shared/common/` — Fabric code that is the same on 1.21.1 and 26.x
- `shared/pre-26/` — 1.21.x API shapes only
- `shared/mc26/` — 26.2 and 26.3 API shapes
- `shared/mc263/` — 26.3-only deltas, when that target is added

For now the 26.2 Fabric sources live directly in `mc262/`. Move files into `shared/` when a second target needs them and the implementation is identical.
