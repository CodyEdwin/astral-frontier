# Ship Builder - Quick Start Guide

## ✅ Status: FIXED AND WORKING!

The Ship Builder is now fully functional with a custom UI that doesn't require external skin files.

## 🚀 How to Use

### Opening the Ship Builder

1. **Launch the game**
2. **Press H** at any time during gameplay
3. The Ship Builder interface will open

### Interface Overview

```
┌─────────────────────────────────────────────────────────────┐
│ SHIP BUILDER    [Presets] [Clear] [Save] [Apply]     [Exit] │  ← Top Toolbar
├──────────┬─────────────────────────────────────┬─────────────┤
│          │                                     │             │
│  PARTS   │        3D SHIP PREVIEW              │   STATS     │
│  PANEL   │                                     │   PANEL     │
│          │    (Rotate with middle mouse)       │             │
│  [Hull]  │    (Zoom with scroll wheel)         │  Parts: 15  │
│  [Wings] │                                     │  Mass: 8000 │
│  [Engines]│                                     │  Hull: 500  │
│  [...]   │                                     │  Thrust: ... │
│          │                                     │  T/W: 0.8   │
└──────────┴─────────────────────────────────────┴─────────────┘
│ Instructions: WASD+Space/Shift: Move | Left Click: Place... │
└─────────────────────────────────────────────────────────────┘
```

### Quick Start - Load a Preset

1. Press **H** to open builder
2. Click **Presets** button (top toolbar)
3. Currently loads **Fighter** preset automatically
4. Click **Apply** to use this ship
5. Press **ESC** to exit
6. Your ship is now updated!

### Building Custom Ships

#### Step 1: Select a Part
- Scroll through the **Parts Panel** on the left
- Click any part name to select it
- Selected part will be highlighted in blue

#### Step 2: Position the Part
- Use **W/A/S/D** keys to move forward/left/back/right
- Use **Space** to move up
- Use **Shift** to move down
- Current position shown at bottom of screen

#### Step 3: Place the Part
- **Left Click** anywhere (not on UI) to place the part
- Part will be added to your ship
- Stats will update automatically

#### Step 4: Continue Building
- Select more parts and place them
- Use **Right Click** to remove the last placed part
- Build until satisfied with your design

#### Step 5: Apply Changes
1. Click **Save** to save your design
2. Click **Apply** to use this ship
3. Press **ESC** to exit

## 🎮 Controls Reference

### Mouse Controls
- **Left Click**: Place selected part (or click UI buttons)
- **Right Click**: Remove last placed part
- **Middle Mouse + Drag**: Rotate camera around ship
- **Mouse Wheel**: Zoom in/out (inside parts panel = scroll parts)

### Keyboard Controls
- **H**: Open Ship Builder (from game)
- **ESC**: Close Ship Builder
- **W**: Move part forward (+Z)
- **S**: Move part backward (-Z)
- **A**: Move part left (-X)
- **D**: Move part right (+X)
- **Space**: Move part up (+Y)
- **Left Shift**: Move part down (-Y)

## 📊 Understanding the Stats

### Parts
Total number of components in your ship

### Mass (kg)
- Weight of your ship
- Lower = faster, more agile
- Higher = slower, more stable

### Hull (HP)
- Structural damage capacity
- More hull parts = higher HP

### Shield (HP)
- Regenerating energy barrier
- Add Shield Generator parts to increase

### Thrust (N)
- Total engine power
- More engines = more thrust

### T/W Ratio (Thrust-to-Weight)
- **Most Important Stat!**
- Below 0.3: Won't fly well
- 0.5-1.0: Good performance
- Above 1.0: Excellent performance

**Rule of Thumb**: Always keep T/W above 0.5!

## 🛠️ Building Tips

### Starting a New Ship
1. Click **Clear** to remove all parts
2. Always start with **Cockpit** (from Hull parts)
3. Add **Hull sections** (Forward, Mid, Aft)
4. Add **Wings** for stability
5. Add **Engines** for thrust
6. Add **Weapons** if desired

### Balancing Your Ship
- Place equal parts on both sides (left and right)
- If ship spins in flight, it's unbalanced
- More engines = better acceleration
- More hull = more protection but also more weight

### Common Mistakes
❌ Too many hull parts = too heavy
❌ Not enough engines = can't accelerate
❌ Unbalanced (parts only on one side)
❌ Forgetting to click Apply before exiting

## 🎯 Example Quick Build

**Fast Fighter (30 seconds):**
1. Press H
2. Click Presets (loads Fighter)
3. Click Apply
4. Press ESC
5. Done!

**Custom Light Fighter (2 minutes):**
1. Press H
2. Click Clear
3. Select Cockpit → Place at (0, 0, 0) → Left Click
4. Select Hull Forward → Move back with S → Left Click
5. Select Swept Wing → Move right with D → Left Click
6. Select Swept Wing → Move left with A → Left Click
7. Select Medium Engine → Move back with S → Left Click
8. Check T/W ratio > 0.5
9. Click Apply
10. Press ESC
11. Test fly!

## 📦 Available Parts

### Hull (6 types)
- Cockpit - Command center
- Nose - Front section
- Forward - Front hull
- Mid - Center hull
- Aft - Rear hull
- Tail - Back section

### Wings (5 types)
- Standard - Balanced
- Swept - High speed
- Delta - Stable
- Stub - Lightweight
- Variable - Adaptive

### Engines (5 types)
- Small - 40,000N thrust
- Medium - 80,000N thrust
- Large - 150,000N thrust
- Nacelle - Integrated housing
- Afterburner - Boost module

### Weapons (5 types)
- Small Mount - Light weapons
- Medium Mount - Medium weapons
- Large Mount - Heavy weapons
- Turret - 360° rotation
- Missile Pod - Missiles

### Utility (5 types)
- Sensor Array - Detection
- Antenna - Communications
- Cargo Pod - Storage
- Fuel Tank - Extended range
- Shield Generator - Energy shields

### Structural (4 types)
- Strut - Framework
- Connector - Joints
- Pylon - Mounts
- Fin - Stabilizers

### Decorative (4 types)
- Stripe - Visual detail
- Emblem - Insignia
- Light - Running lights
- Vent - Engine detail

## 🐛 Troubleshooting

### "Ship Builder won't open"
- Make sure you pressed **H** key
- Check console for error messages

### "Can't move mouse/Camera won't rotate"
- **Hold down the MIDDLE mouse button** (scroll wheel click)
- While holding, drag your mouse to rotate
- A blue circle appears when middle mouse is active
- Release middle mouse to stop rotating
- Make sure you're clicking the scroll wheel, not just scrolling

### "Mouse cursor is invisible/locked"
- The cursor should be visible in the builder
- If not, press ESC to exit and try again
- Builder automatically unlocks the mouse cursor

### "Can't place parts"
- Make sure you selected a part (click in Parts Panel)
- Try left clicking away from UI panels
- Check if part is selected (highlighted in blue)

### "Ship spins uncontrollably in flight"
- Ship is unbalanced
- Make sure parts are mirrored on both sides
- Place equal wings/engines on left and right

### "Ship won't move fast"
- Check T/W ratio in Stats Panel
- Need more engines or less mass
- Remove some hull parts or add more engines

### "Changes didn't apply"
- Click the **Apply** button before exiting
- Configuration saves but won't take effect until applied

### "Can't scroll parts list"
- Use mouse wheel while hovering over Parts Panel
- Parts list is scrollable if there are many parts

### "Camera view is stuck/weird angle"
- Hold middle mouse and drag to adjust
- Scroll wheel to zoom in/out
- If still stuck, exit (ESC) and reopen (H)

## ⚡ Pro Tips

1. **Start Simple**: Begin with presets, then modify
2. **Balance is Key**: Mirror parts on both sides
3. **Watch the T/W**: Keep it above 0.5 for good flight
4. **Save Often**: Use Save button frequently
5. **Test and Iterate**: Apply, test fly, adjust, repeat
6. **Less is Sometimes More**: Don't add parts you don't need
7. **Engines at Back**: Place engines at rear for realistic thrust
8. **Grid Helps**: Positions snap to 0.5 unit grid automatically
9. **Camera Control**: MIDDLE mouse button (click scroll wheel) + drag to rotate
10. **Blue Circle**: Shows when middle mouse is active for camera rotation

## 🔮 Coming Soon

- Color customization (UI buttons ready, functionality pending)
- Multiple preset options (Fighter, Interceptor, Assault, etc.)
- Part rotation with Q/E keys
- Click to remove specific parts
- Undo/Redo functionality
- Named save slots
- Visual ghost part preview

## 📝 Notes

- The UI is custom-built and doesn't require external files
- All ships use the modular ShipBuilder system
- Changes persist between builder sessions
- Configurations can be saved and loaded
- Apply button needed for changes to take effect

## 🎉 You're Ready!

Press **H** in-game and start building your dream spaceship!

Remember: Start with Presets, then customize from there.

**Happy Building! 🚀**

---

**Version**: 1.1 (Fixed - No uiskin.json dependency)  
**Status**: ✅ WORKING  
**Last Updated**: 2025