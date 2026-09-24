# Utility Nexus QoL

Small optional quality-of-life tweaks for NeoForge, starting with a vanilla-style
beacon beam of light rising from every Waystones block so travel hubs stand out at
a distance.

A home for optional, low-footprint player-facing enhancements that do not fit the
server-ops focus of *Utility Nexus Admin* or the crash-patch focus of
*Utility Nexus Fixes*.

## Features

- **Waystone Beacon Beam** *(client-side, optional)*: renders a vanilla beacon-style
  light beam rising out of every Waystones block in view, so bases and travel hubs
  are visible from a distance. Configurable colour and height via
  `config/utility_nexus/qol/config.toml` (`[waystoneBeam]` section: `enabled`,
  `beamHeightBlocks`, `colorRed`/`colorGreen`/`colorBlue`).

  Requires the [Waystones](https://www.curseforge.com/minecraft/mc-mods/waystones)
  mod. The feature is gated twice — by the config flag **and** by a runtime
  `ModList` check — so with Waystones absent it never registers anything and never
  crashes.

- **Recipe Conflict Selector** *(server + client)*: when two or more crafting recipes
  produce different results from the same grid contents (a common conflict between
  mods), a small swap-arrows button appears above the result slot in the crafting
  table and the 2×2 inventory grid. Clicking it opens a panel of result icons; pick
  one to apply it (preview + real craft) and it is remembered for the session and
  synced to the server. Enabled by default; disable via `[recipeSelect] enabled = false`
  in `config/utility_nexus/qol/recipe-select.toml`.

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.249
- Java 21
- *(optional)* Waystones for NeoForge 1.21.1 — only needed for the Waystone Beacon Beam feature

## Installation

1. Install NeoForge 21.1.249 for Minecraft 1.21.1
2. Download the latest release from [CurseForge](https://www.curseforge.com/minecraft/mc-mods/utility-nexus-qol) or [GitLab](https://gitlab.com/stalking-dragons/minecraft/utility-nexus-qol/-/releases)
3. Place the JAR file in your `mods` folder
4. Launch Minecraft with the NeoForge profile

## Building from Source

```bash
./gradlew build
```

The built JAR will be in `build/libs/`.

To exercise the Waystone Beacon Beam with `./gradlew runClient`, drop
`waystones-neoforge-1.21.1-*.jar` and `balm-neoforge-1.21.1-*.jar` into `libs/`
and add matching `localRuntime files(...)` lines to `build.gradle`. Those jars are
git-ignored — they are not ours to redistribute.

## Links

- [GitLab Repository](https://gitlab.com/stalking-dragons/minecraft/utility-nexus-qol)
- [Issues](https://gitlab.com/stalking-dragons/minecraft/utility-nexus-qol/-/issues)
- [Releases](https://gitlab.com/stalking-dragons/minecraft/utility-nexus-qol/-/releases)

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
