# Starfield Ship Builder Implementation

## What Starfield's Ship Builder Does

### Core Features
1. **Part Browser** - Left panel with categories (Cockpit, Hull, Engine, etc.)
2. **Part Selection** - Click a part to enter placement mode
3. **3D Placement** - Click in 3D space to place the part
4. **Snap System** - Parts automatically snap to attachment points
5. **Validation** - Real-time feedback if ship can fly
6. **Stats Display** - Mass, mobility, cargo, crew, etc.
7. **Color Picker** - Customize ship colors
8. **Flight Check** - Green checkmark when ship is valid

### UI Layout
- **Left Panel**: Part categories + scrollable part list
- **Center**: 3D ship view with camera controls
- **Right Panel**: Ship stats + validation
- **Bottom**: Controls hint bar

### Controls
- **Left Click** - Select part / Place part
- **Right Click** - Cancel / Delete part
- **Mouse Drag** - Rotate camera
- **Scroll** - Zoom camera
- **Tab** - Cycle through parts
- **R** - Rotate part before placing
- **ESC** - Exit builder

## Implementation Plan

### Phase 1: Clean UI (DONE)
- ✅ Three-panel layout
- ✅ Part categories at top
- ✅ Scrollable part list
- ✅ Stats panel on right

### Phase 2: Part Placement System (TO DO)
1. Create simple click-to-place system
2. No complex transforms - just position + rotation
3. Grid snapping (1 unit grid)
4. Visual ghost preview

### Phase 3: Basic Validation (TO DO)
1. Check for required parts (cockpit + engine)
2. Display red X or green checkmark
3. Simple "Can Fly" / "Cannot Fly" status

### Phase 4: Camera Controls (DONE)
- ✅ Right-drag to orbit
- ✅ Scroll to zoom
- ✅ Auto-center on ship

## Current Status

The original `ShipBuilderScreen.java` is functional and has:
- ✅ Parts panel
- ✅ 3D rendering
- ✅ Camera controls
- ✅ Preset ships work
- ✅ Basic UI

## What's Missing

1. **Click-to-Place** - Need proper part selection → placement flow
2. **Visual Feedback** - Ghost preview of part before placing
3. **Validation Display** - Clear "Can Fly" indicator
4. **Part Rotation** - Rotate parts with R key
5. **Better Snapping** - Grid + attachment point snapping

## Recommended Approach

**KEEP IT SIMPLE:**

1. Use the working `ShipBuilderScreen.java` as base
2. Add click-to-place: Select part → enters placement mode → click to place
3. Add ghost preview: Semi-transparent part follows cursor
4. Add validation indicator: Big green checkmark or red X
5. Add rotation: Press R to rotate ghost 90 degrees

**DON'T:**
- Over-complicate transforms
- Try to do perfect Starfield parity
- Add features before basics work
- Use complex snap point math

## Next Steps

1. Test current `ShipBuilderScreen` in-game
2. Identify what's broken
3. Fix ONE thing at a time
4. Add features incrementally
5. Test after each change

## Notes

- Press H to open ship builder
- Original system had working presets
- Focus on making it usable, not perfect
- Starfield took years to build - we need basics first