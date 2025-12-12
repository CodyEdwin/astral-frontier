# Quick Ship Change Guide

## How to Change the Player's Ship Class

### Option 1: Direct Ship Class (Recommended for Testing)

Open `core/src/main/java/com/astral/screens/GameScreen.java` and modify the `createWorld()` method:

```java
private void createWorld() {
    // Create player ship using factory with specific class
    playerEntity = playerShipFactory.createPlayerShip(
        world, 
        physicsSystem, 
        ShipComponent.ShipClass.INTERCEPTOR  // Change this!
    );
    
    // ... rest of method
}
```

### Available Ship Classes

Replace `INTERCEPTOR` with any of these:

- `ShipComponent.ShipClass.FIGHTER` - Fast, agile dogfighter (default)
- `ShipComponent.ShipClass.INTERCEPTOR` - Lightning-fast pursuit craft
- `ShipComponent.ShipClass.BOMBER` - Heavy assault platform
- `ShipComponent.ShipClass.FREIGHTER` - Large cargo hauler
- `ShipComponent.ShipClass.EXPLORER` - Long-range scout
- `ShipComponent.ShipClass.CORVETTE` - Multi-role patrol ship
- `ShipComponent.ShipClass.CRUISER` - Heavy combat vessel
- `ShipComponent.ShipClass.CARRIER` - Capital ship

### Option 2: Change Default in PlayerShipFactory

Open `core/src/main/java/com/astral/screens/factories/PlayerShipFactory.java`:

```java
public Entity createPlayerShip(World world, PhysicsSystem physicsSystem) {
    return createPlayerShip(world, physicsSystem, ShipComponent.ShipClass.CRUISER);
    //                                            Change this! ^^^^^^^^^^^^
}
```

### Option 3: Runtime Selection (Future Feature)

For future implementation, add a ship selection screen or menu:

```java
// Example ship selection UI
switch (selectedShipIndex) {
    case 0: return ShipComponent.ShipClass.FIGHTER;
    case 1: return ShipComponent.ShipClass.INTERCEPTOR;
    case 2: return ShipComponent.ShipClass.BOMBER;
    // etc.
}
```

### Testing Different Ships

1. Change the ship class in one of the locations above
2. Run the game: `./gradlew desktop:run`
3. Test the handling - each ship feels different!
4. Check stats with F1 debug overlay

### Ship Quick Reference

| Class       | Speed | Agility | Hull | Cargo | Best For              |
|-------------|-------|---------|------|-------|-----------------------|
| Fighter     | ★★★★☆ | ★★★★★   | ★★★☆☆| -     | Combat, dogfighting   |
| Interceptor | ★★★★★ | ★★★★★   | ★★☆☆☆| -     | Speed, pursuit        |
| Bomber      | ★★☆☆☆ | ★★☆☆☆   | ★★★★☆| -     | Heavy weapons         |
| Freighter   | ★★☆☆☆ | ★☆☆☆☆   | ★★★★☆| ★★★★★ | Trading, cargo        |
| Explorer    | ★★★★☆ | ★★★★☆   | ★★★☆☆| -     | Exploration, range    |
| Corvette    | ★★★☆☆ | ★★★☆☆   | ★★★★☆| -     | Balanced, versatile   |
| Cruiser     | ★★☆☆☆ | ★★☆☆☆   | ★★★★★| -     | Heavy combat          |
| Carrier     | ★☆☆☆☆ | ★☆☆☆☆   | ★★★★★| ★★★★★ | Capital operations    |

### Custom Colors (Advanced)

To customize ship colors, modify `buildShipFromClass()` in PlayerShipFactory:

```java
// Change these RGB values (0.0 to 1.0)
Color primary = new Color(0.8f, 0.2f, 0.2f, 1f);    // Red
Color secondary = new Color(0.1f, 0.1f, 0.1f, 1f);  // Dark gray
Color accent = new Color(1.0f, 0.5f, 0.0f, 1f);     // Orange

shipBuilder.setColors(primary, secondary, accent);
```

### Common Color Schemes

**Military Gray:**
```java
Color primary = new Color(0.5f, 0.5f, 0.55f, 1f);
Color secondary = new Color(0.3f, 0.3f, 0.35f, 1f);
Color accent = new Color(0.2f, 0.6f, 0.9f, 1f);
```

**Crimson:**
```java
Color primary = new Color(0.8f, 0.1f, 0.1f, 1f);
Color secondary = new Color(0.2f, 0.0f, 0.0f, 1f);
Color accent = new Color(1.0f, 0.3f, 0.0f, 1f);
```

**Deep Space Blue:**
```java
Color primary = new Color(0.2f, 0.3f, 0.6f, 1f);
Color secondary = new Color(0.1f, 0.15f, 0.3f, 1f);
Color accent = new Color(0.4f, 0.7f, 1.0f, 1f);
```

**Stealth Black:**
```java
Color primary = new Color(0.15f, 0.15f, 0.2f, 1f);
Color secondary = new Color(0.05f, 0.05f, 0.08f, 1f);
Color accent = new Color(0.3f, 0.0f, 0.0f, 1f);
```

**Golden:**
```java
Color primary = new Color(0.8f, 0.7f, 0.3f, 1f);
Color secondary = new Color(0.4f, 0.35f, 0.15f, 1f);
Color accent = new Color(1.0f, 0.9f, 0.5f, 1f);
```

### Notes

- Changes require recompiling the game
- Ship class affects handling, speed, and stats
- All ship classes use the modular ship building system
- See `SHIPBUILDER_INTEGRATION.md` for detailed information