# Astral Frontier - Game Design Document (GDD)

## Overview
Astral Frontier is an open-world space exploration RPG built on LibGDX, evolving into a highly modular, progression-driven experience. Players pilot customizable ships across a vast, procedurally generated universe, exploring planets, accumulating wealth through trade and discovery, and achieving win conditions focused on peaceful exploration and prosperity. Violence is discouraged; "bad blood" (aggressive actions) leads to negative consequences like reputation loss, faction hostility, or economic penalties. The game emphasizes modularity, with complex class hierarchies for extensibility, allowing 10x improvements in depth without altering core ship flying controls.

## Core Philosophy
- **Win Conditions**: Primary path is peaceful exploration and wealth accumulation (e.g., amass 1 billion credits through trade/discovery, unlock all planets). Alternative: If players engage in violence ("bad blood"), shift to a "consequence" mode with harder challenges (e.g., bounties, restricted access), but no direct "win" – focus on redemption or survival.
- **Modularity**: Every system uses interfaces, factories, and registries for plugin-like extensions. Aim for 50+ classes per major system to enable detailed customization and future mods.
- **Progression**: XP from exploration/trade; levels grant perks like better scanners or trade bonuses. No combat XP unless in "bad blood" mode.

## 1. Modular Universe Framework & Starsystem Overhaul
### Goals
Rewrite the universe to be infinitely scalable, with rich economies, factions, and events. Ship controls remain unchanged (6DOF throttle/strafe/yaw/pitch/roll, boost/brake), but add autopilot, trade routes, and dynamic quests.

### Key Systems
- **UniverseGenerator System** (20+ classes):
  - `IUniverseGenerator`: Interface for generation strategies.
  - `ProceduralUniverseGenerator`: Implements seed-based creation.
  - `UniverseSeedManager`: Handles seeds for reproducibility.
  - `UniverseConfig`: JSON-based settings for scale/density.
  - `UniverseFactory`: Dependency injection for generators.
  - `UniversePluginLoader`: Loads custom plugins (e.g., modded galaxies).
  - `UniverseEventDispatcher`: Manages events like supernovas or invasions.
  - `UniverseStatisticsTracker`: Tracks player progress (systems visited, wealth).

- **Starsystem System** (25+ classes):
  - `IStarsystem`: Interface for system data.
  - `StarsystemImpl`: Concrete implementation with planets/stations.
  - `StarsystemManager`: Loads/unloads systems with chunking.
  - `StarsystemRenderer`: Handles visual rendering.
  - `StarsystemEconomy`: Manages markets/trade.
  - `StarsystemFactionController`: Controls faction presence/wars.
  - `StarsystemQuestHub`: Generates dynamic quests.
  - `StarsystemNavigation`: Autopilot and route planning.
  - `StarsystemResourceDeposits`: Procedural resources (minerals, gases).
  - `StarsystemAnomalyGenerator`: Creates black holes, wormholes.
  - `StarsystemPopulationSimulator`: Simulates NPC activities.
  - `StarsystemWeatherSystem`: Space weather effects.
  - `StarsystemSecurityLevel`: Determines danger/safety.
  - `StarsystemDiscoveryTracker`: Logs explorations.

- **Progression System** (15+ classes):
  - `IProgressionManager`: Tracks player advancement.
  - `ProgressionMilestone`: Defines goals (e.g., visit 100 planets).
  - `ProgressionRewardHandler`: Grants perks/credits.
  - `ReputationSystem`: Faction relations; bad blood lowers reputation.
  - `WealthTracker`: Monitors credits/assets.
  - `AchievementRegistry`: Unlockable achievements.
  - `ProgressionUIUpdater`: Updates HUD with progress.

### Improvements
- 10x content: 10,000+ systems with unique economies/factions.
- Events: Trade caravans, faction alliances, exploration bonuses.
- Integration: Ties into ECS via `UniverseSystem` (extends `GameSystem`).

## 2. AAA Inventory, Item, and Equipment System (FPS Mode)
### Goals
Deep, customizable inventory for first-person exploration, focusing on trade and utility over combat.

### Key Systems
- **Item System** (30+ classes):
  - `IItem`: Base interface.
  - `ItemType`: Enum for categories (tool, resource, artifact).
  - `ItemRarity`: Common to Mythical with stat modifiers.
  - `ItemProperty`: Durability, weight, value.
  - `ItemRegistry`: Central registry for all items.
  - `ItemFactory`: Creates items from templates.
  - `ItemModifier`: Enchantments/procedural traits.
  - `ItemStack`: Handles quantities.
  - `ItemSerializer`: Save/load support.
  - `ItemTooltipGenerator`: UI tooltips.
  - `ItemCraftingRecipe`: Defines recipes.
  - `ItemVendor`: NPC traders.
  - `ItemDiscovery`: Unlocks via exploration.

- **Inventory System** (20+ classes):
  - `IInventory`: Interface for storage.
  - `InventoryGrid`: 50x50 expandable grid.
  - `InventoryManager`: Handles operations (add/remove/sort).
  - `InventoryUI`: Drag-drop interface.
  - `InventoryFilter`: Search/sort by type/rarity.
  - `InventoryCapacity`: Weight/value limits.
  - `InventorySync`: Network sync (future).
  - `InventoryBackup`: Auto-save.

- **Equipment System** (25+ classes):
  - `IEquipment`: Interface for gear.
  - `EquipmentSlot`: Head, chest, legs, feet, hands, accessories (x4).
  - `EquipmentStats`: Modifiers (speed, carry capacity, scan range).
  - `EquipmentManager`: Equips/unequips items.
  - `EquipmentDurability`: Wear and tear.
  - `EquipmentUpgrade`: Crafting enhancements.
  - `EquipmentSetBonus`: Full sets grant extras.
  - `EquipmentUI`: Visual equip screen.
  - `EquipmentPersistence`: Saves loadouts.
  - `EquipmentRestriction`: Class/race limits (if added).

### Improvements
- 10x depth: 500+ items, complex crafting trees.
- FPS Integration: Equipment boosts exploration (e.g., better scanners for hidden resources).

## 3. AAA Planetary Generation System
### Goals
Vast, interactive planets with biomes, structures, and ecosystems, built on chunking.

### Key Systems
- **Terrain System** (30+ classes):
  - `ITerrainGenerator`: Interface for generation.
  - `TerrainChunk`: 64x64 units with LOD.
  - `TerrainBiomeSelector`: Chooses biomes.
  - `TerrainHeightMap`: Noise-based elevation.
  - `TerrainTextureBlender`: Seamless textures.
  - `TerrainDetailLayer`: Micro-features (rocks, grass).
  - `TerrainErosionSimulator`: Realistic weathering.
  - `TerrainLODManager`: Performance optimization.
  - `TerrainCollisionMesh`: Physics integration.

- **Biome System** (25+ classes):
  - `IBiome`: Interface for environments.
  - `BiomeConfig`: Settings for flora/fauna.
  - `BiomeFloraGenerator`: Plants/trees.
  - `BiomeFaunaSpawner`: Animals/NPCs.
  - `BiomeWeatherController`: Rain, storms.
  - `BiomeResourceSpawner`: Deposits.
  - `BiomeStructurePlacer`: Ruins, camps.
  - `BiomeTransitionHandler`: Smooth edges.
  - `BiomeModLoader`: Custom biomes.

- **Structure System** (20+ classes):
  - `IStructureGenerator`: Interface.
  - `StructureTemplate`: Prefabs.
  - `StructurePlacer`: Positioning logic.
  - `StructureInterior`: Rooms/loot.
  - `StructureQuestTrigger`: Events.
  - `StructureDestruction`: Damage models.
  - `StructureLOD`: Detail levels.

- **Planet System** (15+ classes):
  - `IPlanet`: Interface.
  - `PlanetSurfaceManager`: Chunk streaming.
  - `PlanetAtmosphereRenderer`: Sky effects.
  - `PlanetDayNightCycle`: Time system.
  - `PlanetEcosystem`: Interconnected life.
  - `PlanetExplorationTracker`: Discovery logs.

### Improvements
- 10x scale: Planets with 1000+ km radius, infinite exploration.
- Interactivity: Mining, building, quests.

## Technical Architecture
- **ECS Integration**: All systems extend `GameSystem` with modular components.
- **Modularity**: Strict adherence to sub-package structure for maximum extensibility:
  - `inventory.core`, `inventory.items`, `inventory.equipment`, `inventory.crafting`, `inventory.skills`, `inventory.ui`
  - `planetary.core`, `planetary.terrain`, `planetary.biomes`, `planetary.structures`, `planetary.resources`
  - `universe.core`, `universe.generation`, `universe.economy`, `universe.factions`
  - NO EXCEPTIONS: All future development MUST follow this packaging for modularity, with interfaces for plugin-like extensions.
- **Procedural Asset Generation**: MOST IMPORTANT - All assets (weapons, textures, models, UI icons) MUST be generated programmatically or loaded by code. No external asset files; everything created on-the-fly for infinite variety and moddability. This is a core pillar of the game.
- **Building**: Use Gradle for builds (`gradle desktop:build`). Ensure all classes compile without errors; fix imports and dependencies immediately.
- **Performance**: Async generation, pooling, GPU acceleration.
- **Data**: JSON/YAML configs; Kryo for saves.

## Development Roadmap
1. GDD Creation & README Update (Week 1).
2. Universe Rewrite (Weeks 2-4).
3. Inventory/Equipment (Weeks 5-7).
4. Planetary Generation (Weeks 8-10).
5. Testing & Polish (Weeks 11-12).

This GDD serves as the blueprint for modular, complex development.