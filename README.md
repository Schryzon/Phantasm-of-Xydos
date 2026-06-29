# Phantasm of Xydos: Andromeda I

A retro 2D vertical-scrolling Touhou-style bullet hell (Danmaku) game built entirely in **Java Swing and AWT/Java 2D**, featuring customizable scripting, visual novel cutscenes, settings UI, and gamepad support.

---

## 🎮 Gameplay Features

### Playable Protocols
- **Historia Koura** (Vessel of Thunder):
  - High offense build with straight CAS-8 lightning bolts.
  - Automatic close-up melee Spear of Lagta slashes dealing massive damage to nearby enemies.
  - *Spell: Lagtanis Karvista* (Temporary invulnerability, unleashing a barrage of giant homing spears prioritizing bosses. Note: does *not* clear screen bullets).
- **Mira Koura** (Empathy Wind Weaver):
  - High utility build with a smaller hitbox (2.5px) for extreme dodging.
  - Multi-directional Gale Wind Blades combining spread, straight, and homing wind currents.
  - *Spell: Daiki's Sanctuary* (Full-screen bullet wipe, temporary invulnerability, and moderate damage to all active bosses).

### Features
- **Custom Stage Scripting (`.stage`)**: Fully customizable levels loaded from declarative beatmap-like files specifying scrolling speeds, background images (`bg_path`), BGM paths, custom boss spells, and enemy timelines.
- **Undertale-style Dialogues**: Visual novel dialogue boxes typing text out character-by-character with visual shakiness (vibrations) and speaker expression portraits (`assets/portraits/[speaker]_[expression].png`).
- **Comprehensive Settings Panel**:
  - Independent volume sliders for Music and SFX.
  - Target FPS limit selector (30, 60, 120, Unlimited) adjusting loops dynamically.
  - Interactive Keyboard rebinding mapping.
- **Controller Support**: Reflection-based controller polling utilizing Jamepad (D-pad/Analog move, A shoots, B/X casts spells, Right Trigger/Shoulder focuses).
- **High Scores Archiving**: MySQL connectivity creating score records, falling back dynamically to a flat-file database (`local_scores.txt`) if the server is offline.

---

## 🛠️ File Structure

```
Phantasm-of-Xydos/
├── Story/                  <- Original chronological narrative documents
├── stages/                 <- Stage configuration script folder
│   ├── STAGE_SPECIFICATION.md <- Complete syntax rules for level scripting
│   ├── stage1.stage        <- Stage 1 timelines (Victoria Koura boss)
│   ├── stage2.stage        <- Stage 2 timelines (Queen Fenria & Xelisa boss)
│   └── stage3.stage        <- Stage 3 timelines (Goddess Cyria boss)
├── lib/                    <- Dependency libraries (Jamepad, native-lib-loader, mysql jar)
├── src/                    <- Source root
│   ├── Game_App.java       <- Main JFrame panel coordinator
│   ├── Game_Engine.java    <- Active loop updating rendering and physics ticks
│   ├── Input_Manager.java  <- Multi-key and gamepad tracking
│   ├── Player_Character.java <- Playable bases
│   ├── Historia_Character.java <- Historia specific weapons and spells
│   ├── Mira_Character.java <- Mira specific weapons and spells
│   ├── Enemy_Entity.java   <- General drones and boss logic
│   ├── Bullet_Entity.java  <- Projectile structures
│   ├── Bullet_Pool.java    <- Memory-reusable bullet arrays
│   ├── Stage_Manager.java  <- Script events parser
│   ├── Sound_Player.java   <- Audio decibel controls
│   ├── Config_Manager.java <- Serializer for config.ini settings
│   └── Database_Connector.java <- MySQL CRUD methods
├── config.ini              <- Debug switches and settings properties
├── build.bat               <- Compiler (downloads Jamepad & MySQL connectors automatically)
└── run.bat                 <- Launch script
```

---

## 🚀 Setup & Execution

### Prerequisites
- Java Development Kit (JDK) 8 or higher.
- Internet connection (on the first run of the builder to download jar dependencies).

### Steps
1. Double-click **`build.bat`** (or execute `.\build.bat` in Terminal) to automatically resolve jar libraries from Maven Central and compile source files into `bin/`.
2. Double-click **`run.bat`** (or execute `.\run.bat` in Terminal) to start the game.

### Controls
- **Arrow Keys / WASD** — Movement
- **Z** — Shoot / Advance dialogue
- **X** — Cast Spell (requires Spell charge)
- **Left Shift** — Focus Mode (slows down movement speed and displays character hitbox)
- **Space** — Skip active dialogue cutscene
