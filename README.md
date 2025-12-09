# AstralFrontier

An AAA-quality open-world space exploration RPG featuring seamless transitions between interstellar travel and planetary exploration.

## Features

- **Procedural Universe**: 1:1 scale procedural galaxy with 1000+ star systems
- **6DOF Flight**: Full Newtonian physics with 6 degrees of freedom
- **Planet Exploration**: Land on planets and explore on foot
- **Multiplayer**: Server-authoritative co-op for 2-8 players
- **Deep RPG Systems**: Inventory, quests, skills, and progression
- **Combat**: Space dogfighting and FPS ground combat

## Technical Stack

- **Engine**: Java 17+ / LibGDX 1.12+
- **Physics**: Bullet Physics
- **Networking**: KryoNet
- **Target**: Desktop (Windows, Linux, macOS) @ 60 FPS

## Project Structure

```
astral-frontier/
├── core/           # Platform-independent game logic
├── desktop/        # Desktop launcher (LWJGL3)
├── server/         # Dedicated server
└── assets/         # Game assets (models, textures, data)
```

## Building

### Prerequisites

- Java 17 or higher
- Gradle 8.5+

### Running the Game

```bash
# Run desktop version
./gradlew desktop:run

# Build JAR
./gradlew desktop:jar
```

### Running Dedicated Server

```bash
./gradlew server:run
```

## Controls

### Spaceflight
| Key | Action |
|-----|--------|
| W/S | Forward/Reverse thrust |
| A/D | Strafe left/right |
| Q/E | Thrust up/down |
| Mouse | Pitch/Yaw |
| Space+A/D | Roll |
| Shift | Boost |
| Ctrl | Brake |
| Tab | Toggle camera view |

### On Foot (FPS)
| Key | Action |
|-----|--------|
| WASD | Movement |
| Space | Jump |
| Shift | Sprint |
| Ctrl | Crouch |
| E/F | Interact |
| Mouse1/2 | Fire/Aim |

### Universal
| Key | Action |
|-----|--------|
| ESC | Menu |
| I | Inventory |
| M | Map |
| J | Quest log |
| F1-F4 | Debug overlays |

## Architecture

The game uses an Entity-Component-System (ECS) architecture:

- **Entities**: Game objects (ships, players, asteroids)
- **Components**: Data containers (Transform, RigidBody, Ship)
- **Systems**: Logic processors (Physics, Render, Combat)

Key systems:
- `PhysicsSystem`: Bullet physics simulation
- `RenderSystem`: Deferred rendering with PBR
- `NetworkSystem`: Client prediction & server reconciliation
- `GameLogicSystem`: Ship controls, player updates

## Development Phases

1. ✅ Project Setup
2. ✅ Core Loop & Input
3. ✅ 3D Space & Starfield
4. ✅ Ship Flight & Physics
5. ✅ Procedural Generation
6. 🔲 Planet Landing
7. 🔲 FPS Mode
8. 🔲 Inventory & Combat
9. 🔲 Quests & Dialogue
10. 🔲 Multiplayer

## License

Copyright (c) 2025 - All Rights Reserved

## Credits

- Game Design & Development: Matrix Agent
- Engine: LibGDX (https://libgdx.com)
- Physics: Bullet Physics
