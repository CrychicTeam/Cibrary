# Pibrary Pack Layout

[中文说明](PACKS.zh-CN.md)

This file describes how `Pibrary` should stay organized during the split from the old monolith to the new repo family.

## Permanent In This Repo

These responsibilities stay in `Pibrary`:
1. shared core contracts;
2. service location and public base abstractions;
3. common config, diagnostics, registry, state, and targeting boundaries;
4. entity lifecycle, vehicle lifecycle, projectile lifecycle, projectile tracing, projectile impact runtime, and JEI-neutral recipe-viewer contracts for the optional compat layer;
5. minimal render-core bridge hooks that expose host-side refresh and extracted-state boundaries;
6. migration-safe root mod bootstrap.

Living services are explicit activation points: descriptor discovery may be generated, but runtime attachment and sync are driven by registered service handles.

## Independent Foundation Repos

These stay outside `Pibrary` even if `Pibrary` depends on their concepts:
1. `PiNet`
2. `PiSerializeKit`
3. `PiKubeJSCompat`
4. `PiJEICompat`
5. `PiDataGraph`

They are independent because they are broadly reusable and should not require the whole `Pibrary` stack.

## Engine Repos

These belong to engine families, not the root core repo:
1. camera systems;
2. UI / HUD / animated screen systems;
3. story / dialogue / mission systems;
4. render bridge, geo render, player animation, and other visual runtime systems;
5. entity FX and data-graph gameplay engines.

## Legacy Code

The legacy monolith is preserved under:
`restore/legacy-monolith-20260331`

The active rule is:
1. keep it available for migration;
2. do not treat it as the target architecture;
3. extract reusable parts into stable APIs before moving implementations into independent repos.
4. move object-counter and graph gameplay logic into `PiDataGraph`, not back into `Pibrary`.
