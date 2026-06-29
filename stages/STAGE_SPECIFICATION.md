# Phantasm of Xydos — Stage File Specification

This document details the file syntax rules for custom `.stage` level config files used in the developer compilation system.

## File Extension
Files must end with `.stage` and be located under the `stages/` directory (e.g. `stages/stage1.stage`).

---

## 1. Structure Overview

Level files are split into three INI-style sections:
1. `[Metadata]` — Basic settings (music, backdrop style).
2. `[Spells]` — Configures the boss Danmaku patterns for specific combat phases.
3. `[Events]` — Sequential timeline events triggered at specific scrolling offsets.

Example layout:
```ini
[Metadata]
name = Schryza Sector Alpha
bgm = assets/stage_bgm.wav
bg_path = assets/schryza_bg.png
bg_color = #050515
star_count = 80

[Spells]
1,spiral,#8A2BE2,3.5,10
2,ring_spread,#FF00FF,2.5,45

[Events]
100,scroll_speed,1.2
150,spawn_enemy,300,-50,15,0.0,2.5,100
300,dialogue,Mira,"Historia, stay sharp!",35,0
2000,boss,Victoria Koura,1200,400,-80,45,5000
```

---

## 2. Section Rules

### 2.1 `[Metadata]`
Contains configuration keys:
- `name` (String): Display name of the stage.
- `bgm` (String): Relative path to loopable `.wav` music.
- `bg_path` (String): Optional path to a background image (`ImageIO` loaded).
- `bg_color` (Hex): Hex color value (e.g., `#0F0F2D`) for solid rendering.
- `star_count` (Integer): Total background stars spawned for depth.

### 2.2 `[Spells]`
Defines the boss's attack behaviors mapping to phase numbers:
- Format: `phase_id,pattern_type,color_hex,bullet_speed,shoot_cooldown`
- `pattern_type` values:
  - `spiral` — Fires curved arms spinning outwards.
  - `ring_spread` — Sweeps radial rings of projectiles.
  - `concentric_circles` — Overlapping circular waves.
  - `chaos_bloom` — Multi-colored random spreads.

### 2.3 `[Events]`
Chronological list of occurrences triggered when the viewport reaches the specified scroll offset (`scroll_y`).

#### Event Types:

1. **`scroll_speed`**:
   - Syntax: `offset,scroll_speed,value`
   - Example: `200,scroll_speed,2.5` (Sets dynamic scroll speed to 2.5).

2. **`spawn_enemy`**:
   - Syntax: `offset,spawn_enemy,x,y,hp,vel_x,vel_y,score`
   - Example: `500,spawn_enemy,320,-40,15,0.0,3.0,150`

3. **`dialogue`**:
   - Syntax: `offset,dialogue,speaker_name,expression,text_string,char_delay_ms,shakiness`
   - Example: `600,dialogue,Victoria,composed,"Your resistance is futile.",40,4`
   - *Behavior*: Pauses scrolling and action. Displays the character's portrait from `assets/portraits/[speaker]_[expression].png` on the left, typing out text on the right. Vibrates the box by `shakiness` offset values.

4. **`boss`**:
   - Syntax: `offset,boss,boss_name,hp,x,y,radius,score`
   - Example: `2000,boss,Victoria Koura,1200,400,-80,45,5000`
