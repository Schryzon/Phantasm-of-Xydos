# Phantasm of Xydos: Andromeda I - The Divine Shadows

A retro 2D vertical-scrolling Touhou-style bullet hell (Danmaku) game built entirely in **Java Swing and AWT/Java 2D**, featuring customizable scripting, visual novel cutscenes, settings UI, and gamepad support.

---

## Gameplay Features

### Playable Protocols
- **Historia Koura** (Vessel of Thunder):
  - High offense build with straight CAS-8 lightning bolts.
  - Automatic close-up melee Spear of Lagta slashes dealing massive damage to nearby enemies. Follows a `Left` -> `Right` -> `Left` -> `Pause` combination rhythm.
  - *Spell: Lagtanis Karvista* (Temporary invulnerability, sequentially throwing 7 giant boss-seeking spears with massive 35px hitboxes).
- **Mira Koura** (Empathy Wind Weaver):
  - High utility build with a smaller hitbox (2.5px) for extreme dodging.
  - Multi-directional Gale Wind Blades combining spread, straight, and homing wind currents.
  - *Spell: Daiki's Sanctuary* (Full-screen bullet wipe, temporary invulnerability, and moderate damage to all active bosses).

### Features
- **Power Up Progression**: Weapons shoot more bullets and deal higher damage as red `P` power items are collected.
- **Deltarune-style Grazing**: Flashes a stylized yellow lightning bolt (**Thunder** emblem) for Historia or a cyan/green rotating swirl (**Wind** emblem) for Mira when bullets graze the player.
- **Choose Difficulty Setting**: Pick from 5 difficulty modes (**Rookie**, **Trooper**, **Elite**, **Android**, **CyroN**) which dynamically scale bullet speed, shoot rate, and enemy squadron density.
- **Custom Stage Scripting (`.stage`)**: Fully customizable levels loaded from declarative files specifying scrolling speeds, background images, BGM paths, custom boss spells, and timelines.
- **Dialogue Engine**: Continuous typing dialogue bars at the bottom with visuals novel cutscenes, expression portraits, text click sounds, frame vibration shakiness, and a `Space` skip shortcut.
- **Settings Panel**: Independent SFX and Music decibel volume sliders, FPS target lock, and custom key rebinding.
- **Controller Support**: Reflection-based Jamepad controller polling supporting D-pad/Analog sticks and trigger buttons.
- **High Scores Archiving**: MySQL database connectivity with a flat-file database (`local_scores.txt`) fallback.

---

## File Structure

```
Phantasm-of-Xydos/
├── Story/                  <- Narrative timeline chronicles
├── stages/                 <- Stage configuration script folder
│   ├── STAGE_SPECIFICATION.md <- beatmap formatting syntax rules
│   ├── stage1.stage        <- Stage 1 timelines (Victoria Koura boss)
│   ├── stage2.stage        <- Stage 2 timelines (Queen Fenria & Xelisa boss)
│   └── stage3.stage        <- Stage 3 timelines (Goddess Cyria boss)
├── lib/                    <- Dependency libraries (.jar)
├── src/                    <- Source root
│   ├── game/
│   │   ├── app/
│   │   │   └── Game_App.java       <- JFrame window and card layout coordinator
│   │   ├── engine/
│   │   │   ├── Game_Engine.java    <- Precise timing game loop (physics & draw ticks)
│   │   │   ├── Input_Manager.java  <- Multi-key and controller state poller
│   │   │   ├── Stage_Manager.java  <- timeline scripting and cutscene parser
│   │   │   ├── Sound_Player.java   <- Decibel audio clip player
│   │   │   ├── Config_Manager.java <- INI settings reader & writer
│   │   │   └── Database_Connector.java <- MySQL score records archives
│   │   └── entities/
│   │       ├── Player_Character.java <- Base template class for pilots
│   │       ├── Historia_Character.java <- Thunder pilot weapons & spells
│   │       ├── Mira_Character.java   <- Wind pilot weapons & spells
│   │       ├── Enemy_Entity.java     <- Drones & boss AI patterns
│   │       ├── Bullet_Entity.java    <- Projectile instance
│   │       ├── Bullet_Pool.java      <- Memory-recycled bullet arrays
│   │       └── Item_Drop.java        <- Red Power and Blue Score drops
│   └── Custom_DSA/          <- Custom Data Structures library driving stage timeline dialogues
│       ├── Lists/           <- Linked list collections
│       ├── Nodes/           <- Singly/Doubly Node templates
│       ├── Stack_Queue/     <- Custom Linked_Queue and Linked_Stack classes
│       └── Graph/           <- Custom generic graph collections
├── config.ini              <- Local configuration properties
├── build.bat               <- Compiler (resolves dependencies automatically)
└── run.bat                 <- Game launcher
```

---

## Setup & Execution

### Prerequisites
- Java Development Kit (JDK) 8 or higher.
- Internet connection (on the first build run to resolve libraries).

### Steps
1. Double-click **`build.bat`** (or execute `.\build.bat` in Terminal) to automatically resolve jar libraries and compile package directories into `bin/`.
2. Double-click **`run.bat`** (or execute `.\run.bat` in Terminal) to start the game.

### Controls
- **Arrow Keys / WASD** — Pilot Movement
- **Z** — Fire Primary Weapon / Advance Dialogue
- **X** — Trigger Spell (requires Spell charge)
- **Left Shift** — Focus mode (slows speed down and renders hitbox circle)
- **Space** — Skip active dialogue cutscene

---

## Legal Disclaimer & License

### Disclaimer
This project is a fan-made creation inspired by Team Shanghai Alice's *Touhou Project* and classic bullet hell games. It is built strictly for educational, research, and non-commercial purposes. All intellectual property, trademarks, and copyright rights relating to *Touhou Project* (including sound effects, characters, and assets) are owned by ZUN (Team Shanghai Alice) and their respective owners. No copyright infringement is intended. The author and contributors are not affiliated with, endorsed by, or in any way associated with ZUN or Team Shanghai Alice.

### License
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for the full text.

Copyright (c) 2026 I Nyoman Widiyasa Jayananda
