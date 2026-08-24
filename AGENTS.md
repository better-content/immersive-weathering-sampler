# AGENTS.md

## Scope

This repository contains the Better Content-owned Forge mod **Immersive Weathering Sampler**.

- Canonical mod ID: `immersive_weathering_sampler`
- Canonical artifact: `immersive-weathering-sampler-<version>.jar`
- Maven group: `com.bettercontent`
- Java runtime: 17
- Minecraft/Forge baseline: 1.20.1 / 47.4.13

## Commit discipline

Commit each coherent completed change after validation and push the current branch when a canonical remote is available.

## Validation

Run `./gradlew verifyFast --no-daemon` for deterministic checks. Runtime changes also require
`./gradlew verifyFull --no-daemon` before deployment.

Do not commit build outputs, runtime worlds, logs, IDE state, or downloaded dependency JARs.

