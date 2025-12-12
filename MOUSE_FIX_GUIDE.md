# Mouse Control Fix Guide

## Issue: Mouse Gets Stuck at Center When Pressing H

### Problem
When opening the Ship Builder (pressing H), the mouse cursor gets locked at the center of the screen and cannot move freely.

### Root Cause
The game's InputSystem locks the mouse cursor for flight controls. When switching to the Ship Builder, the cursor needs to be explicitly unlocked.

### Solution Implemented

#### 1. GameScreen.java - openShipBuilder()
```java
private void openShipBuilder() {
    // Unlock mouse cursor before switching to ship builder
    inputSystem.setMouseLocked(false);
    Gdx.input.setCursorCatched(false);

    ShipBuilderScreen builderScreen = new ShipBuilderScreen(game, this);
    game.setScreen(builderScreen);
    Gdx.app.log("GameScreen", "Opening ship builder...");
}
```

**What it does:**
- `inputSystem.setMouseLocked(false)` - Unlocks the InputSystem's mouse lock
- `Gdx.input.setCursorCatched(false)` - Makes the cursor visible and free to move

#### 2. GameScreen.java - resume()
```java
@Override
public void resume() {
    // Re-lock mouse cursor when returning from ship builder or other screens
    if (inputSystem != null) {
        Gdx.input.setInputProcessor(inputSystem);
        inputSystem.setMouseLocked(true);
        Gdx.input.setCursorCatched(false);
    }
    Gdx.app.log("GameScreen", "Game resumed - mouse locked");
}
```

**What it does:**
- When returning to the game from the Ship Builder
- Re-locks the mouse for flight controls
- Restores the InputSystem as the input processor

#### 3. ShipBuilderScreen.java - show()
```java
@Override
public void show() {
    // ... initialization code ...
    
    // Make sure mouse cursor is visible
    Gdx.input.setCursorCatched(false);
    
    Gdx.app.log("ShipBuilderScreen", "Ship builder ready!");
}
```

**What it does:**
- Ensures cursor is visible when Ship Builder opens
- Sets up custom input processor for UI interaction

## Testing Checklist

- [x] Mouse is locked in game (flight mode)
- [x] Pressing H unlocks mouse
- [x] Mouse moves freely in Ship Builder
- [x] Can click UI buttons in Ship Builder
- [x] Pressing ESC returns to game
- [x] Mouse re-locks in game after returning

## Common Issues

### Issue: Mouse Still Locked After Opening Builder
**Solution:** Restart the game. The fix requires the code changes to be compiled.

### Issue: Mouse Not Locking When Returning to Game
**Solution:** Make sure the `resume()` method in GameScreen is properly implemented.

### Issue: Mouse Visible But Can't Click
**Solution:** The input processor might not be set. Check that ShipBuilderScreen's setupInput() is called.

## How Mouse Locking Works

### In Flight (GameScreen)
```
Mouse State: LOCKED (for flight controls)
├── Cursor: Hidden/Centered
├── Movement: Controls ship rotation
└── InputProcessor: InputSystem (flight controls)
```

### In Ship Builder (ShipBuilderScreen)
```
Mouse State: UNLOCKED (for UI interaction)
├── Cursor: Visible/Free
├── Movement: UI interaction
└── InputProcessor: Custom InputAdapter (UI controls)
```

### Transition Flow
```
GameScreen (Mouse Locked)
    ↓ Press H
    ↓ Call openShipBuilder()
    ↓ Unlock mouse
    ↓
ShipBuilderScreen (Mouse Unlocked)
    ↓ Press ESC
    ↓ Call exitBuilder()
    ↓ Return to GameScreen
    ↓ Call resume()
    ↓ Lock mouse
    ↓
GameScreen (Mouse Locked)
```

## Debug Commands

### Check Mouse State
Add this to your code to debug mouse state:
```java
Gdx.app.log("MouseDebug", "Cursor Caught: " + Gdx.input.isCursorCatched());
Gdx.app.log("MouseDebug", "Mouse X: " + Gdx.input.getX());
Gdx.app.log("MouseDebug", "Mouse Y: " + Gdx.input.getY());
```

### Force Unlock (Emergency)
If mouse gets stuck, add this hotkey in GameScreen:
```java
if (Gdx.input.isKeyJustPressed(Input.Keys.U)) {
    Gdx.input.setCursorCatched(false);
    inputSystem.setMouseLocked(false);
    Gdx.app.log("Debug", "Mouse force unlocked!");
}
```

## Related Files

- `core/src/main/java/com/astral/screens/GameScreen.java`
- `core/src/main/java/com/astral/screens/shipbuilder/ShipBuilderScreen.java`
- `core/src/main/java/com/astral/systems/InputSystem.java`

## Additional Notes

### Why This Happens
LibGDX uses `setCursorCatched()` to lock the cursor for first-person controls. When switching screens, this state needs to be explicitly managed.

### Best Practices
1. **Always unlock before switching to UI screens**
2. **Always re-lock when returning to game screens**
3. **Check cursor state in show()/resume() methods**
4. **Log state changes for debugging**

### Input Processor Priority
```
Screen changes → New InputProcessor is set
If not explicitly set → No input handling
Result → Mouse appears stuck (no processor to handle input)
```

## Quick Fix Summary

**If mouse gets stuck:**
1. Check that `openShipBuilder()` calls `setCursorCatched(false)`
2. Check that `resume()` calls `setCursorCatched(false)` (paradoxically needed)
3. Check that ShipBuilderScreen sets up its input processor
4. Restart game after code changes

**Status: ✅ FIXED**

The mouse control issue has been resolved with proper state management during screen transitions.

---

**Last Updated:** 2025
**Version:** 1.0
**Status:** Fixed and Tested