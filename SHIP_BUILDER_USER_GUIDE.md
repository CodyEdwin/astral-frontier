# Ship Builder System - User Guide

## Overview

The Ship Builder is an in-game system that allows you to design, customize, and modify your spaceship. You can access it at any time during gameplay to change your ship's appearance, stats, and capabilities.

## Accessing the Ship Builder

### From Game Screen
Press **H** (Hangar) at any time during gameplay to open the Ship Builder interface.

### Navigation
- The Ship Builder opens in a new screen with a 3D preview of your ship
- Your game will be paused while you're in the builder
- Press **Exit** or **ESC** to return to the game

## Interface Overview

The Ship Builder interface consists of several panels:

### 1. Toolbar (Top)
- **SHIP BUILDER** - Title
- **Presets** - Load pre-designed ship templates
- **Clear All** - Remove all parts from current ship
- **Save** - Save your current design
- **Apply to Ship** - Apply your design to your active ship
- **Exit** - Close the builder and return to game

### 2. Parts Panel (Left)
Displays all available ship parts organized by category:

#### Hull Parts
- **Cockpit** - Command center with pilot seat
- **Nose Section** - Aerodynamic front
- **Forward Hull** - Front body section
- **Mid Hull** - Central body section
- **Aft Hull** - Rear body section
- **Tail Section** - Back end

#### Wing Parts
- **Standard Wing** - Balanced lift and maneuverability
- **Swept Wing** - High-speed design
- **Delta Wing** - Stable, wide stance
- **Stub Wing** - Minimal profile, weapon mounts
- **Variable Geometry Wing** - Adaptive configuration

#### Engine Parts
- **Small Engine** - 40,000N thrust, lightweight
- **Medium Engine** - 80,000N thrust, balanced
- **Large Engine** - 150,000N thrust, heavy duty
- **Engine Nacelle** - Integrated engine housing
- **Afterburner Module** - Boost capability

#### Weapon Mounts
- **Small Weapon Mount** - Light weapons (6 max)
- **Medium Weapon Mount** - Medium weapons (4 max)
- **Large Weapon Mount** - Heavy weapons (2 max)
- **Turret Mount** - 360° rotation (2 max)
- **Missile Pod** - Missile systems (4 max)

#### Utility Parts
- **Sensor Array** - Enhanced detection
- **Communication Antenna** - Long-range comms
- **Cargo Pod** - Extra storage space
- **External Fuel Tank** - Extended range
- **Shield Generator** - Energy shields

#### Structural Parts
- **Structural Strut** - Framework support
- **Section Connector** - Join hull sections
- **Wing Pylon** - Weapon/equipment mount
- **Stabilizer Fin** - Flight stability

#### Decorative Parts
- **Hull Stripe** - Visual customization
- **Ship Emblem** - Personal insignia
- **Navigation Light** - Running lights
- **Exhaust Vent** - Engine detail

### 3. 3D Preview (Center)
- Real-time 3D view of your ship
- Interactive camera controls
- Grid reference for positioning
- Axis indicators (X=Red, Y=Green, Z=Blue)

### 4. Stats Panel (Right)
Displays current ship statistics:
- **Parts** - Total number of parts
- **Mass** - Total ship mass (kg)
- **Hull** - Hull integrity points
- **Shield** - Shield strength points
- **Thrust** - Total thrust output (N)
- **Fuel** - Fuel capacity (L)
- **T/W Ratio** - Thrust-to-weight ratio

Also includes:
- **Color Pickers** - Customize ship colors
- **Snap to Grid** - Toggle grid snapping

### 5. Instructions Bar (Bottom)
Quick reference for controls and shortcuts

## Controls

### Camera Controls
- **Middle Mouse Button + Drag** - Rotate camera around ship
- **Mouse Wheel** - Zoom in/out
- Distance range: 5-100 units
- Automatically orbits around ship center

### Part Placement
1. **Select a Part** - Click on any part in the Parts Panel
2. **Position the Part** - Use movement controls
   - **W** - Move forward (+Z)
   - **S** - Move backward (-Z)
   - **A** - Move left (-X)
   - **D** - Move right (+X)
   - **SPACE** - Move up (+Y)
   - **LEFT SHIFT** - Move down (-Y)
3. **Place the Part** - Left click to confirm placement
4. **Cancel** - Right click or select a different part

### Part Removal
- **Right Click** - Remove the last placed part
- **Clear All** button - Remove all parts

### Grid Snapping
- **Snap to Grid Checkbox** - Enable/disable snapping
- Grid size: 0.5 units
- Snapping rounds position to nearest grid point
- Helps align parts precisely

## Building Your Ship

### Quick Start - Using Presets

1. Press **H** to open Ship Builder
2. Click **Presets** button
3. Choose a template:
   - **Fighter** - Fast, agile combat ship
   - **Interceptor** - Ultra-fast pursuit craft
   - **Assault** - Heavy weapons platform
   - **Freighter** - Large cargo hauler
   - **Scout** - Long-range explorer
4. The preset will load instantly
5. Modify as desired
6. Click **Apply to Ship**
7. Exit the builder

### Custom Building from Scratch

#### Step 1: Start with a Hull
```
1. Click "Clear All" to start fresh
2. Select "Cockpit" from Hull Parts
3. Position at (0, 0, 0) - this is your command center
4. Left click to place
```

#### Step 2: Add Body Sections
```
1. Select "Forward Hull"
2. Position at (0, 0, -2) - behind cockpit
3. Left click to place
4. Select "Mid Hull"
5. Position at (0, 0, -5)
6. Left click to place
7. Select "Aft Hull"
8. Position at (0, 0, -8)
9. Left click to place
```

#### Step 3: Add Wings
```
1. Select "Swept Wing"
2. Position at (2, 0, -5) - right side
3. Left click to place
4. Position at (-2, 0, -5) - left side (mirror)
5. Left click to place
```

#### Step 4: Add Engines
```
1. Select "Medium Engine"
2. Position at (1, 0, -10) - right engine
3. Left click to place
4. Position at (-1, 0, -10) - left engine
5. Left click to place
```

#### Step 5: Add Weapons
```
1. Select "Small Weapon Mount"
2. Position at (1.5, -0.2, 2) - right gun
3. Left click to place
4. Position at (-1.5, -0.2, 2) - left gun
5. Left click to place
```

#### Step 6: Review Stats
- Check the Stats Panel on the right
- Ensure Thrust > Mass for good performance
- Add more engines if T/W ratio is below 0.5
- Balance Hull vs Speed trade-offs

#### Step 7: Apply and Test
1. Click **Save** to save your design
2. Click **Apply to Ship** to use it
3. Exit the builder
4. Test fly your creation!

## Customization

### Colors

Change your ship's appearance with custom colors:

1. Click **Primary** button in Stats Panel
2. Choose a color from the palette
3. Repeat for **Secondary** and **Accent** colors

**Color Schemes:**
- Primary: Main hull color
- Secondary: Panel details and sections
- Accent: Lights and highlights

**Popular Color Combinations:**
- Military: Gray + Dark Gray + Blue
- Crimson: Red + Dark Red + Orange
- Stealth: Black + Dark Gray + Red
- Explorer: Blue + Navy + Light Blue
- Golden: Gold + Bronze + Yellow
- Neon: Any + Black + Bright accent

### Symmetry

Create balanced ships using mirrored placement:

1. Place a part on one side (e.g., X = 2)
2. Place the same part on opposite side (X = -2)
3. Keeps ship balanced for flight
4. Important for wings and engines

## Understanding Stats

### Mass
- **What it affects**: Acceleration, handling, fuel efficiency
- **Lower is better for**: Speed, agility
- **Higher is better for**: Stability, durability
- **Typical range**: 5,000-50,000 kg

### Hull Points (HP)
- **What it is**: Damage capacity before destruction
- **How to increase**: Add more hull sections
- **Typical range**: 100-2,500 HP

### Shield Points
- **What it is**: Regenerating energy barrier
- **How to increase**: Add Shield Generator utility parts
- **Typical range**: 50-1,500 HP

### Thrust (Newtons)
- **What it is**: Engine power output
- **How to increase**: Add more/larger engines
- **Typical range**: 100,000-1,000,000 N

### Thrust-to-Weight Ratio (T/W)
- **What it is**: Thrust divided by mass
- **Minimum for flight**: 0.2-0.3
- **Good performance**: 0.5-1.0
- **Excellent performance**: 1.0+
- **Fighter jets**: 1.2-1.5

### Fuel Capacity
- **What it is**: How long you can fly
- **How to increase**: Add Fuel Tank utility parts
- **Typical range**: 200-2,000 L

## Best Practices

### Ship Design Principles

1. **Start with Purpose**
   - Combat? Focus on weapons and agility
   - Trading? Maximize cargo space
   - Exploration? Balance range and sensors

2. **Balance Your Design**
   - Equal parts on both sides
   - Engines aligned with center of mass
   - Wings provide stability

3. **Thrust-to-Weight is Key**
   - Always aim for T/W > 0.5
   - More engines = better acceleration
   - But also more mass

4. **Don't Overdo It**
   - More parts = more mass
   - More mass = slower ship
   - Find the right balance

5. **Test and Iterate**
   - Save your design first
   - Apply and test fly
   - Come back and adjust
   - Repeat until perfect

### Common Mistakes to Avoid

❌ **Too Many Hull Sections**
- Makes ship heavy and slow
- Use enough for structure, not more

❌ **Insufficient Thrust**
- Can't accelerate properly
- Add more engines or reduce mass

❌ **Unbalanced Design**
- Ship lists or spins in flight
- Mirror parts on both sides

❌ **No Fuel Storage**
- Short operational range
- Add external tanks for long missions

❌ **Forgetting to Apply**
- Changes won't take effect
- Always click "Apply to Ship" before exiting

## Tips and Tricks

### Efficient Builder Workflow
1. Load a similar preset as starting point
2. Modify only what you need
3. Use Snap to Grid for clean alignment
4. Save frequently
5. Name your designs (future feature)

### Visual Design Tips
- Use accent colors sparingly for highlights
- Dark secondary colors create depth
- Bright primary colors stand out in space
- Navigation lights should contrast hull color

### Performance Optimization
- Remove decorative parts if ship is too heavy
- One large engine often beats two small ones
- Stub wings are lighter than delta wings
- External fuel tanks can be jettisoned (future feature)

### Advanced Techniques
- Asymmetric designs can work (harder to fly)
- Multiple small engines are easier to lose (combat)
- Large cargo ships need more fuel
- Scout ships benefit from minimal mass

## Preset Ships Guide

### Fighter
- **Role**: Dogfighting, patrols
- **Strengths**: Speed, maneuverability
- **Weaknesses**: Light armor
- **Best for**: Players who like combat

### Interceptor
- **Role**: Fast attack, pursuit
- **Strengths**: Top speed, acceleration
- **Weaknesses**: Very light armor
- **Best for**: Hit-and-run tactics

### Assault
- **Role**: Heavy combat
- **Strengths**: Firepower, armor
- **Weaknesses**: Slow, less agile
- **Best for**: Frontal assaults

### Freighter
- **Role**: Trading, cargo hauling
- **Strengths**: Huge cargo capacity
- **Weaknesses**: Slow, poor handling
- **Best for**: Economic gameplay

### Scout
- **Role**: Exploration, recon
- **Strengths**: Range, sensors, speed
- **Weaknesses**: Light weapons
- **Best for**: Discovery and mapping

## Troubleshooting

### "My ship won't move!"
- **Problem**: T/W ratio too low
- **Solution**: Add more engines or remove parts

### "Ship spins uncontrollably"
- **Problem**: Unbalanced design
- **Solution**: Mirror parts equally on both sides

### "Can't see my ship in preview"
- **Problem**: Camera too close/far
- **Solution**: Use mouse wheel to adjust zoom

### "Changes didn't apply"
- **Problem**: Forgot to click Apply
- **Solution**: Click "Apply to Ship" before exiting

### "Ship disappeared after applying"
- **Problem**: No hull parts placed
- **Solution**: Load a preset or add hull sections

### "Too many parts of this type"
- **Problem**: Exceeded part limit
- **Solution**: Check part max count, remove excess

## Keyboard Reference

| Key | Action |
|-----|--------|
| H | Open Ship Builder (from game) |
| ESC | Exit Builder |
| W/A/S/D | Move part position |
| SPACE | Move part up |
| SHIFT | Move part down |
| Left Click | Place selected part |
| Right Click | Remove last part |
| Middle Mouse + Drag | Rotate camera |
| Mouse Wheel | Zoom camera |

## Future Features (Planned)

- [ ] Save/Load custom designs with names
- [ ] Share designs with other players
- [ ] Part rotation controls (Q/E keys)
- [ ] 3D part selection (click on ship)
- [ ] Undo/Redo functionality
- [ ] Copy/Paste parts
- [ ] Part variants and skins
- [ ] Procedural part generation
- [ ] Damage visualization
- [ ] Performance comparison tool
- [ ] Build cost system
- [ ] Part unlocking progression

## Support

For issues or questions:
- Check diagnostics with F1 key in-game
- Review game logs in project directory
- See SHIPBUILDER_INTEGRATION.md for technical details

## Version

Ship Builder System v1.0
Last Updated: 2025
Part of Astral Frontier Game