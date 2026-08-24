# Immersive Weathering Sampler

A Better Content Forge 1.20.1 integration that gives Immersive Weathering a
bounded unloaded-time endpoint update.

The mod does not tick unloaded chunks and does not replay missed ticks. Each
dimension keeps four cumulative exposure counters (clear/rain and day/night).
A chunk stores its last counter snapshot. When that chunk returns, the sampler
subtracts the snapshots, converts the elapsed exposure into at-least-one-event
probabilities, scans existing block states once, and applies a shuffled set of
direct endpoint changes.

Normal loaded Immersive Weathering behavior remains unchanged. Unloaded
Activity remains installed for block entities and its other supported systems.
The sampler deliberately excludes historical entities, item drops, fluid
updates, lightning, player-triggered mechanics, and recursive growth from newly
created blocks.

## Configuration

`config/immersive_weathering_sampler-common.toml` controls the minimum unloaded
interval, probability density multiplier, and debug logging. A newly installed
world initializes chunk snapshots on first observation, so no time before the
mod was installed is applied retroactively.

## Validation

```sh
./gradlew verifyFast --no-daemon
./gradlew verifyFull --no-daemon
```
