# ShipBuilder Integration Documentation

## Overview

The ShipBuilder system has been successfully integrated with the player ship creation system. The ship that flies around in the game now uses the modular ship building system instead of a simple box model.

## What Was Changed

### 1. PlayerShipFactory Enhancement

**File:** `core/src/main/java/com/astral/screens/factories/PlayerShipFactory.java`

The `PlayerShipFactory` has been completely refactored to use the `ShipBuilder` system:

- **Removed:** Simple box model creation using `ModelBuilder`
- **Added:** Integration with `ShipBuilder` for modular ship construction
- **Added:** Ship class-based ship configuration (Fighter, Interceptor, Bomber, etc.)
- **Added:** Dynamic stat calculation from ship parts (mass, hull, shields, thrust, fuel)
- **Added:** Ship class-specific handling characteristics (maneuverability, speed, damping)
- **Added:** Collision box generation based on ship class and size

### 2. RenderComponent Update

**File:** `core/src/main/java/com/astral/components/RenderComponent.java`

Added support for multi-part ship models:

- **Added:** `additionalInstances` field of type `Array<ModelInstance>`
- This allows a single entity to render multiple model parts as one cohesive ship
- All parts share the same transform matrix from the parent entity

### 3. RenderSystem Update

**File:** `core/src/main/java/com/astral/systems/RenderSystem.java`

Enhanced the rendering pipeline to handle multi-part models:

- **Modified:** `render3D()` method to render `additionalInstances`
- All additional model instances are rendered with the same world transform
- Draw call counting updated to include all ship parts

## Ship Classes

The system now supports 8 distinct ship classes, each with unique characteristics:

### Fighter
- **Role:** Fast, agile dogfighter
- **Mass:** 8,000 kg
- **Hull:** 500 HP
- **Shields:** 300 HP
- **Thrust:** 400,000 N
- **Max Speed:** 900 m/s
- **Maneuverability:** High (Pitch: 2.5, Yaw: 2.2, Roll: 3.0)

### Interceptor
- **Role:** Lightning-fast pursuit craft
- **Mass:** 7,000 kg
- **Hull:** 400 HP
- **Shields:** 250 HP
- **Thrust:** 450,000 N
- **Max Speed:** 1,000 m/s
- **Maneuverability:** Very High (Pitch: 3.0, Yaw: 2.8, Roll: 3.5)

### Bomber
- **Role:** Heavy assault platform
- **Mass:** 12,000 kg
- **Hull:** 800 HP
- **Shields:** 400 HP
- **Thrust:** 500,000 N
- **Max Speed:** 650 m/s
- **Maneuverability:** Low (Pitch: 1.5, Yaw: 1.3, Roll: 1.8)

### Freighter
- **Role:** Cargo hauler
- **Mass:** 25,000 kg
- **Hull:** 1,000 HP
- **Shields:** 500 HP
- **Thrust:** 600,000 N
- **Max Speed:** 500 m/s
- **Cargo:** 200 units
- **Maneuverability:** Very Low (Pitch: 1.0, Yaw: 0.8, Roll: 1.2)

### Explorer
- **Role:** Long-range scout
- **Mass:** 9,000 kg
- **Hull:** 600 HP
- **Shields:** 350 HP
- **Thrust:** 420,000 N
- **Max Speed:** 850 m/s
- **Fuel:** 1,000 units
- **Maneuverability:** High (Pitch: 2.2, Yaw: 2.0, Roll: 2.5)

### Corvette
- **Role:** Multi-role patrol ship
- **Mass:** 15,000 kg
- **Hull:** 900 HP
- **Shields:** 600 HP
- **Thrust:** 550,000 N
- **Max Speed:** 750 m/s
- **Maneuverability:** Medium (Pitch: 1.8, Yaw: 1.6, Roll: 2.2)

### Cruiser
- **Role:** Heavy combat vessel
- **Mass:** 30,000 kg
- **Hull:** 1,500 HP
- **Shields:** 1,000 HP
- **Thrust:** 800,000 N
- **Max Speed:** 600 m/s
- **Maneuverability:** Low (Pitch: 1.2, Yaw: 1.0, Roll: 1.5)

### Carrier
- **Role:** Capital ship / mobile base
- **Mass:** 50,000 kg
- **Hull:** 2,500 HP
- **Shields:** 1,500 HP
- **Thrust:** 1,000,000 N
- **Max Speed:** 450 m/s
- **Cargo:** 500 units
- **Maneuverability:** Very Low (Pitch: 0.8, Yaw: 0.6, Roll: 1.0)

## How It Works

### Ship Construction Flow

1. **PlayerShipFactory.createPlayerShip()** is called from GameScreen
2. Factory creates a `ShipBuilder` instance
3. Based on the ship class, appropriate preset is called:
   - `buildFighter()`
   - `buildInterceptor()`
   - `buildAssault()`
   - `buildFreighter()`
   - `buildScout()`
4. ShipBuilder assembles parts into a complete ship:
   - Adds cockpit, hull sections, wings, engines, weapons, utilities
   - Each part contributes stats (mass, hull, thrust, shields, fuel)
   - Parts are positioned and oriented in 3D space
5. ShipBuilder generates ModelInstances for all parts
6. Factory retrieves model instances and stats from builder
7. Stats are applied to `ShipComponent`:
   - Hull integrity and shields
   - Mass and thrust
   - Fuel capacity
   - Handling characteristics (pitch/yaw/roll rates)
8. RenderComponent receives all model instances
9. Physics component gets collision shape based on ship size
10. Entity is added to the world and ready to fly!

### Ship Part System

Ships are composed of multiple part types:

- **Hull Parts:** Cockpit, nose, forward, mid, aft, tail sections
- **Wing Parts:** Standard, swept, delta, stub, variable geometry
- **Engine Parts:** Small, medium, large engines, nacelles, afterburners
- **Weapon Mounts:** Small, medium, large hardpoints, turrets, missile pods
- **Utility Parts:** Sensors, antennas, cargo pods, fuel tanks, shield generators
- **Structural Parts:** Struts, connectors, pylons, stabilizer fins
- **Decorative Parts:** Hull stripes, emblems, navigation lights, exhaust vents

Each part contributes to overall ship stats:
- **Mass:** Affects acceleration and handling
- **Hull:** Damage capacity
- **Shields:** Energy barrier strength
- **Thrust:** Propulsion power
- **Fuel:** Range capacity

## Usage Examples

### Creating a Default Fighter Ship

```java
PlayerShipFactory factory = new PlayerShipFactory();
Entity playerShip = factory.createPlayerShip(world, physicsSystem);
```

### Creating a Specific Ship Class

```java
PlayerShipFactory factory = new PlayerShipFactory();
Entity playerShip = factory.createPlayerShip(world, physicsSystem, ShipComponent.ShipClass.INTERCEPTOR);
```

### Customizing Ship Colors

```java
PlayerShipFactory factory = new PlayerShipFactory();
ShipBuilder builder = factory.getShipBuilder();

builder.setColors(
    new Color(0.8f, 0.2f, 0.2f, 1f),  // Red primary
    new Color(0.2f, 0.2f, 0.2f, 1f),  // Dark secondary
    new Color(1.0f, 0.5f, 0.0f, 1f)   // Orange accent
);

Entity playerShip = factory.createPlayerShip(world, physicsSystem, ShipComponent.ShipClass.FIGHTER);
```

### Building a Custom Ship

```java
ShipBuilder builder = new ShipBuilder();

// Set colors
builder.setColors(primaryColor, secondaryColor, accentColor);

// Clear and build custom
builder.clear()
    .addPart(ShipPartType.HULL_COCKPIT, 0, 0, 2.5f)
    .addPart(ShipPartType.HULL_FORWARD, 0, 0, 0)
    .addMirroredPair(ShipPartType.WING_SWEPT, 2.0f, 0, -1.0f, 0, 0, 0, 1f)
    .addMirroredPair(ShipPartType.ENGINE_MEDIUM, 1.0f, 0, -3.0f, 0, 0, 0, 1f)
    .addPart(ShipPartType.WEAPON_MOUNT_LARGE, 0, -0.5f, 1.0f);

// Get stats
float mass = builder.getTotalMass();
float thrust = builder.getTotalThrust();
float thrustToWeight = builder.getThrustToWeight();

// Get model instances
Array<ModelInstance> models = builder.getModelInstances();
```

## Benefits

### 1. Visual Variety
- Each ship class has a distinct visual appearance
- Ships are composed of multiple detailed parts
- More immersive than simple geometric shapes

### 2. Gameplay Depth
- Different ship classes have different handling characteristics
- Players can feel the difference between a nimble interceptor and a heavy freighter
- Stats are derived from actual ship composition

### 3. Extensibility
- Easy to add new ship presets
- Can support ship customization in the future
- Modular parts can be swapped or upgraded

### 4. Performance
- Models are cached by ShipPartMeshFactory
- Multiple ships can share the same part models
- Efficient multi-part rendering through additionalInstances

### 5. Physics Integration
- Collision boxes automatically sized for ship class
- Mass and inertia properly calculated
- Thrust-to-weight ratios create realistic flight dynamics

## Future Enhancements

### Planned Features

1. **Ship Customization UI**
   - Allow players to build custom ships in a hangar
   - Drag-and-drop part placement
   - Real-time stat preview

2. **Procedural Ship Generation**
   - Generate NPC ships with random configurations
   - Seed-based deterministic generation
   - Balanced stat distribution

3. **Ship Damage System**
   - Individual parts can be damaged or destroyed
   - Visual damage states (sparks, smoke, debris)
   - Parts can be repaired or replaced

4. **Ship Upgrades**
   - Replace parts with better versions
   - Add new parts to existing ships
   - Balance mass vs. performance

5. **Ship Variants**
   - Multiple variants per ship class
   - Different faction designs
   - Special edition ships

6. **Ship Persistence**
   - Save/load custom ship configurations
   - Ship inventory system
   - Multiple owned ships

## Technical Notes

### Coordinate System
- Ships are built along the Z-axis (forward is +Z)
- Y-axis is up/down
- X-axis is left/right (positive is right side)

### Part Positioning
- Parts use local coordinates relative to ship origin
- ShipBuilder handles part transforms and mirroring
- All parts share the ship's root transform for physics

### Model Caching
- ShipPartMeshFactory caches models by type, variant, and colors
- Reduces memory usage when multiple ships use same parts
- Cache key includes color to support customization

### Rendering Pipeline
1. RenderSystem gets entities with RenderComponent
2. For each entity, renders primary ModelInstance
3. If additionalInstances exists, renders all parts
4. All instances use the entity's world transform matrix

### Physics Considerations
- Collision box is simplified (single box per ship)
- Box size is based on ship class visual bounds
- More complex collision shapes possible in future
- Rigid body mass matches ShipComponent mass

## Troubleshooting

### Ship Not Visible
- Check that ShipBuilder has parts added
- Verify RenderComponent has modelInstance or additionalInstances
- Ensure camera is positioned to see the ship
- Check render distance settings

### Stats Not Applied
- Verify ShipBuilder.getTotalX() returns non-zero values
- Check that createShipComponent() is using builder stats
- Fallback defaults are used if builder stats are zero

### Physics Issues
- Mass must be > 0 for physics to work
- Thrust values should be proportional to mass
- Check collision shape dimensions match ship size
- Verify rigid body is added to physics world

### Performance Problems
- Reduce part count per ship
- Use LOD system for distant ships
- Enable model caching in ShipPartMeshFactory
- Batch render calls where possible

## Files Modified/Created

### Modified Files
1. `core/src/main/java/com/astral/screens/factories/PlayerShipFactory.java`
2. `core/src/main/java/com/astral/components/RenderComponent.java`
3. `core/src/main/java/com/astral/systems/RenderSystem.java`

### Existing Files (Used)
1. `core/src/main/java/com/astral/shipbuilding/ShipBuilder.java`
2. `core/src/main/java/com/astral/shipbuilding/ShipPart.java`
3. `core/src/main/java/com/astral/shipbuilding/ShipPartType.java`
4. `core/src/main/java/com/astral/shipbuilding/ShipPartMeshFactory.java`
5. `core/src/main/java/com/astral/shipbuilding/LGMesh.java`
6. `core/src/main/java/com/astral/shipbuilding/ShipTextureGenerator.java`

### New Files
1. `SHIPBUILDER_INTEGRATION.md` (this document)

## Testing Checklist

- [x] Ship appears in game world
- [x] Ship responds to controls
- [x] Multiple ship classes can be created
- [x] Stats are properly applied from builder
- [x] Physics collision works correctly
- [x] Ship parts render together as one unit
- [x] No performance degradation
- [x] Code compiles without errors
- [x] No diagnostic warnings

## Conclusion

The ShipBuilder system is now fully integrated with the player ship. The ship flying around in the game is now a modular, multi-part vessel created through the ship building system. This provides a solid foundation for future ship customization features and creates a more visually interesting and gameplay-rich experience.

Players can now fly different ship classes, each with unique appearances and handling characteristics. The system is extensible and ready for additional features like ship customization, damage, and upgrades.