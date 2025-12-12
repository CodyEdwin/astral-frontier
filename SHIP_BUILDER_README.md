# Starfield-Style Ship Builder

Press **H** in-game to open the ship builder.

## Exact Starfield Workflow

1. **Browse Parts** - Select category tabs (1-6) at top
2. **Select Part** - Click a part from the list or use UP/DOWN arrows
3. **Enter Placement Mode** - Press ENTER or click the part
4. **Position in 3D** - Move mouse to position part (snaps to grid)
5. **Rotate** - Press R to rotate 45 degrees
6. **Confirm** - Click to place, or press SPACE
7. **Cancel** - Right-click to cancel placement

## Controls

### Browsing Mode
- **1-6** - Switch category (Cockpit/Hull/Engines/Wings/Weapons/Systems)
- **TAB** - Cycle through categories
- **UP/DOWN** - Select part in list
- **ENTER** or **CLICK** - Start placing selected part
- **Right Drag** - Rotate camera around ship
- **Scroll** - Zoom camera (or scroll parts list when over left panel)
- **C** - Clear ship and reset to fighter
- **ESC** - Exit builder

### Placement Mode
- **Mouse Move** - Position part (follows cursor on ground plane)
- **R** - Rotate part 45 degrees
- **CLICK** or **SPACE** - Confirm placement
- **Right Click** or **ESC** - Cancel placement

## UI Layout

**Top Bar** - Category tabs with numbers (1-6)
**Left Panel (250px)** - Scrollable parts list with interactive cards
**Center** - 3D ship view with grid (larger viewing area)
**Right Panel (250px)** - Ship stats and validation status
**Bottom** - Context-sensitive instructions

## Categories

1. **Cockpit** - Command modules (required for flight)
2. **Hull** - Forward, mid, aft, tail, nose sections
3. **Engines** - Small, medium, large, nacelle, afterburner
4. **Wings** - Delta, swept, stub, variable geometry
5. **Weapons** - Weapon mounts and turrets
6. **Systems** - Shield, fuel, cargo, sensors

## Ship Stats Display

- **Mass** - Total ship weight in kg
- **Hull** - Structural integrity points
- **Thrust** - Engine power in Newtons
- **T/W** - Thrust-to-weight ratio (must be > 0.3 to fly)

## Validation

**FLIGHT READY** (Green) - Ship is valid and can fly
**CANNOT FLY** (Red) - Missing required parts or insufficient thrust

Required for flight:
- At least one cockpit
- At least one engine
- Thrust-to-weight ratio > 0.3

## Features

✅ Starfield-style workflow (browse → select → place)
✅ State machine (BROWSING / PLACING modes)
✅ Grid-based snapping (1 unit grid)
✅ Part rotation with R key
✅ Real-time validation
✅ Credit system (parts cost 200-15,000 credits)
✅ Ghost preview (green wireframe box shows placement)
✅ Category tabs at top
✅ Clean 3-panel layout
✅ Context-sensitive instructions
✅ Interactive part cards with hover effects
✅ Click or keyboard to select parts
✅ Shows part description and stats in cards
✅ Visual selection highlighting
✅ Scroll indicator shows position in list
✅ Mouse-clickable everywhere
✅ Real 3D part preview (shows actual part model, not wireframe)

## How It Works

The builder uses a **state machine**:

- **BROWSING** - Looking at parts, rotating camera, selecting parts
- **PLACING** - Selected a part, positioning it with mouse, can rotate with R

When you select a part (ENTER or click), it enters PLACING mode. The actual 3D part model follows your mouse cursor with transparency so you can see exactly what you're placing. Click to place it, right-click to cancel.

## Technical Details

- Parts snap to 1-unit grid on ground plane (Y=0)
- Ray casting from mouse to ground for positioning
- Ghost part rendered as actual 3D model with transparency
- Ship starts with fighter preset
- Camera orbits around ship center
- Real-time validation on every placement
- Ghost model rotates with R key in real-time

## Status

## New Features

✅ **Improved Part Cards** - Each part shows as a card with:
  - Part name (green if affordable, red if too expensive)
  - Description (truncated to fit)
  - Cost in credits
  - Key stats (Mass, Thrust, Hull)
  
✅ **Mouse Interaction** - Click anywhere:
  - Click part cards to select and place
  - Hover over cards to highlight them
  - Selected card has blue border
  
✅ **Better Scrolling**:
  - Scroll wheel over left panel to browse parts
  - Shows "X-Y of Z" indicator at bottom
  - Smooth card-based layout

✅ **Visual Feedback**:
  - Hover effect on cards (darker background)
  - Selected card highlighted in blue
  - Affordable/unaffordable shown with colors

## Status

✅ **Working and tested**
✅ Compiles successfully
✅ Integrated into game (press H)
✅ Exact Starfield workflow implemented
✅ Enhanced UI with interactive cards