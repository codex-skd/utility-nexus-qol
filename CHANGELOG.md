# Changelog

All notable changes to this project will be documented in this file.

## [1.1.3] - 2026-09-15

### Fixed
- **Recipe Conflict Selector: preference not applied inside Tom's Storage's Crafting Terminal**:
  picking a non-default result in the selector panel sent the preference to the server, but the
  Crafting Terminal kept crafting whatever recipe it had already resolved. Tom's Storage's
  `CraftingTerminalBlockEntity` caches its resolved recipe (`currentRecipe`) and only calls
  `getRecipe()` again — the method our `MixinCraftingTerminalBlockEntity` hooks into — when that
  cached recipe stops matching the grid. Since picking a different result doesn't change the
  grid contents, the cached recipe kept matching and `getRecipe()` was never called again, so our
  mixin never got a chance to apply the new preference. `RecipeSelectNetwork#handleSetPreferred`
  already forced a recompute for vanilla `CraftingMenu`/`InventoryMenu` via `slotsChanged(...)`
  but had no equivalent for Tom's Storage's `CraftingTerminalMenu`. It now also calls
  `menu.clickMenuButton(player, 1)` for that menu class — the same button Tom's Storage's own
  Polymorph hook (`CraftingTerminalMenu#clickMenuButton`) uses to invalidate `currentRecipe` and
  force `getRecipe()` to run again.

### Changed
- **Mod icon / CurseForge logo**: replaced with a higher-resolution version of the existing
  artwork.

## [1.1.2] - 2026-09-15

### Fixed
- **`RecipeSelectClient` crash on any non-container screen init**: the client-side Recipe
  Conflict Selector's `ScreenEvent.Init.Post`, `ScreenEvent.Render.Post` and
  `ScreenEvent.MouseButtonPressed.Pre` listeners blindly cast `event.getScreen()` to
  `AbstractContainerScreen<?>` for every screen in the game, not just crafting/container
  screens. When a non-container screen initialized as a child of another screen — e.g.
  `com.mojang.realmsclient.gui.screens.RealmsNotificationsScreen`, created inside
  `TitleScreen#init` — the cast threw a `ClassCastException` and crashed the client on the
  main menu. All three listeners now use an `instanceof` pattern match and return early for
  any screen that isn't an `AbstractContainerScreen`.

## [1.1.1] - 2026-09-15

### Fixed
- **`toms_storage` optional dependency**: removed the `versionRange="[21.1,)"` from the
  `toms_storage` entry in `neoforge.mods.toml`. That range had been copy-pasted from the
  `waystones` dependency above it, whose versioning tracks the Minecraft version (`21.1.x`).
  Tom's Storage uses its own scheme (`2.x.x`) and never matched that range, so NeoForge
  flagged any real Tom's Storage install (e.g. `2.4.2`) as incompatible and blocked pack
  startup — even though the dependency is `optional` and the compat layer only ever does a
  runtime `ModList.get().isLoaded("toms_storage")` check with no compile-time API link. The
  entry now declares `type="optional"` with no version constraint, matching how the compat
  layer actually works.

## [1.1.0] - 2026-09-14

### Added
- **Recipe Conflict Selector — Tom's Storage compat** *(server + client, optional)*: the
  overlay now also works inside Tom's Storage's Crafting Terminal (`toms_storage`). A new
  `TomsStorageRecipeSelectorAdapter` recognises the Crafting Terminal screen/menu by class
  name (no compile-time dependency on Tom's Storage) and reuses the same 3×3 grid layout as
  vanilla. A new server-side `MixinCraftingTerminalBlockEntity` (non-required mixin config,
  inert when `toms_storage` isn't installed) injects into
  `CraftingTerminalBlockEntity#getRecipe` to substitute the player's stored preference when
  it still matches the grid, leaving Tom's Storage's own resolution untouched otherwise.
  Reuses Tom's Storage's existing "which player is interacting" button-click hook (the same
  one its bundled Polymorph widget uses) instead of requiring Polymorph to be installed.
  Double-gated by `type="optional"` in `neoforge.mods.toml` and
  `ModList.get().isLoaded("toms_storage")`.
- **Polymorph interop**: when the Polymorph mod is loaded, the Recipe Conflict
  Selector overlay is automatically suppressed on every screen it would otherwise cover
  (vanilla `CraftingScreen` / `InventoryScreen`, and any third-party adapter such as Tom's
  Storage's) to avoid two competing recipe-selector UIs on the same result slot. Polymorph's
  own widgets already cover those screens natively where a mod ships its own Polymorph
  compat. `PolymorphCompat` utility class for cached runtime detection via
  `ModList.get().isLoaded("polymorph")`.

### Changed
- **Recipe Conflict Selector**: replaced hardcoded `instanceof CraftingScreen` /
  `InventoryScreen` checks with a small adapter-based screen registry
  (`RecipeSelectorScreenAdapter` + `RecipeSelectorScreens`), making the overlay
  extensible to third-party crafting screens without touching `RecipeSelectClient`. The
  adapter interface also gained an `onScreenOpened(...)` hook for one-time client→server
  handshaking (used by the Tom's Storage adapter above). The two vanilla adapters (3×3
  crafting table + 2×2 inventory grid) are pre-registered; a public
  `RecipeSelectorScreens.register()` method lets future compat modules plug in without
  modifying `RecipeSelectClient`.

## [1.0.0] - 2026-09-09

First stable release for **Minecraft 1.21.1 / NeoForge 21.1.249** (Java 21). Same code as
`0.0.0-beta.3`; promoted to stable after in-game testing and running in the *(Develop) Mystical
Realms* modded-server pack.

### Summary of the beta line

- **beta.1** — **Waystone Beacon Beam** *(client-side, optional)*: renders a vanilla
  `BeaconRenderer` beam over every Waystones block within render distance. Ported from
  `tower_waystone` (26.2). Waystones blocks are detected by registry namespace only (no link
  against the Waystones API); double-gated by `[waystoneBeam] enabled` and a runtime
  `ModList.get().isLoaded("waystones")` check, so it is a complete no-op without Waystones.
  26.2 → 1.21.1 port: `SubmitCustomGeometryEvent` → `RenderLevelStageEvent`, `Identifier` →
  `ResourceLocation`, `ARGB` → `FastColor.ARGB32`, hand-rolled JSON config → native
  `ModConfigSpec` TOML.
- **beta.2** — replaced the placeholder mod icon with the real 256×256 artwork (drops the jar
  from ~1.17 MB to ~145 KB).
- **beta.3** — **Recipe Conflict Selector** *(server + client)*: when two or more crafting recipes
  produce different results from the same grid contents, a swap-arrows button appears above the
  result slot in the crafting table and 2×2 inventory grid; it opens a panel of result icons, and
  the chosen result is applied (preview + real craft), remembered for the session and synced to
  the server. Enabled by default; disable via `[recipeSelect] enabled = false` in
  `config/utility_nexus/qol/recipe-select.toml`. Choice not persisted to disk.

### Notes

- No code change relative to `0.0.0-beta.3`. Verified: `./gradlew clean build` is green; the
  Recipe Conflict Selector was tested in-game.

## [0.0.0-beta.3]

### Added
- **Recipe Conflict Selector** *(server + client, common config)*: when two or more
  crafting recipes produce different results from the same grid contents (a common
  conflict between mods), a small swap-arrows button appears above the result slot
  in the crafting table and 2×2 inventory grid. Clicking it opens a single bordered
  panel of result icons above the button; the selected result is outlined, and
  hovering an icon shows its tooltip. Clicking an icon applies the choice
  (preview + real craft) and remembers it for the session; the choice is synced to
  the server so the actual craft respects it.
  The choice is not persisted to disk. Enabled by default; disable via
  `[recipeSelect] enabled = false` in `config/utility_nexus/qol/recipe-select.toml`.

## [0.0.0-beta.2]

### Changed
- Replaced the placeholder mod icon with the real project artwork (256x256).
  This also drops the built jar from ~1.17 MB to ~145 KB — the placeholder icon
  was almost the entire file size.

## [0.0.0-beta.1]

### Added
- Initial project setup.
- **Waystone Beacon Beam** *(client-side, optional)*: renders a vanilla
  `BeaconRenderer` beam over every Waystones block within render distance.
  Ported from the equivalent feature in `tower_waystone` (NeoForge 26.2), where it
  was out of place — that repo is about generating waystone tower structures, not
  cosmetic rendering.
  - Waystones blocks are detected purely by registry namespace (`waystones:*` with
    a path containing `waystone`), so the mod never links against the Waystones API.
  - Config lives in `config/utility_nexus/qol/config.toml` under `[waystoneBeam]`
    (`enabled` default `true`, `beamHeightBlocks` default `100`, RGB colour default
    `80 / 200 / 255`), registered as a `CLIENT` `ModConfigSpec` — same nested
    `config/utility_nexus/<area>/` layout used by `utility_nexus_admin` and
    `utility_nexus_fixes`.
  - Double-gated: the client event handlers are only registered when
    `ModList.get().isLoaded("waystones")` is true, and the renderer/scanner also
    re-check `enabled` + mod presence on every pass. With Waystones absent the
    feature is a complete no-op.
  - 26.2 → 1.21.1 API port: `SubmitCustomGeometryEvent` → `RenderLevelStageEvent`,
    `Identifier` → `ResourceLocation`, `net.minecraft.util.ARGB` →
    `FastColor.ARGB32`, hand-rolled JSON config → native `ModConfigSpec` TOML.
