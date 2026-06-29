import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Stage_Manager {
    public int current_stage = 1;
    public double scroll_y = 0;
    public double scroll_speed = 1.0;
    public boolean boss_spawned = false;
    public boolean stage_complete = false;

    // Stage configuration parameters loaded from file
    public String stage_name = "Unknown Sector";
    public String bgm_path = "assets/stage_bgm.wav";
    public String bg_path = "";
    public String bg_color_hex = "#0A0A19";
    public int star_count = 80;

    // Events timeline
    public static class Stage_Event {
        public double offset;
        public String type; // "scroll_speed", "spawn_enemy", "dialogue", "boss"
        
        // Spawn/Boss parameters
        public double x, y, radius;
        public int hp;
        public double vel_x, vel_y;
        public int score;
        public String name;

        // Dialogue parameters
        public String speaker;
        public String expression;
        public String text;
        public int char_delay_ms;
        public int shakiness;
        
        // Internal flag
        public boolean triggered = false;
    }

    public static class Boss_Spell {
        public int phase;
        public String pattern_type; // "spiral", "ring_spread", "concentric_circles", "chaos_bloom"
        public Color color;
        public double bullet_speed;
        public int shoot_cooldown;
    }

    public List<Stage_Event> event_timeline = new ArrayList<>();
    public List<Boss_Spell> boss_spells = new ArrayList<>();
    
    // Dialogue engine states
    public List<Stage_Event> active_dialogues = new ArrayList<>();
    public boolean is_in_dialogue = false;
    public Stage_Event current_dialogue = null;
    public int dialogue_char_index = 0;
    public int dialogue_timer = 0;
    public boolean is_dialogue_finished = false;

    public Stage_Manager() {
        load_stage_file("stages/stage" + current_stage + ".stage");
    }

    public void load_stage_file(String file_path) {
        event_timeline.clear();
        boss_spells.clear();
        active_dialogues.clear();
        is_in_dialogue = false;
        current_dialogue = null;
        boss_spawned = false;
        stage_complete = false;
        scroll_y = 0;
        scroll_speed = 1.0;

        File file = new File(file_path);
        if (!file.exists()) {
            System.err.println("Stage file not found: " + file_path + ". Using fallback defaults.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            String section = "";
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#") || line.startsWith(";")) {
                    continue;
                }
                if (line.startsWith("[") && line.endsWith("]")) {
                    section = line.substring(1, line.length() - 1).toLowerCase();
                    continue;
                }

                if (section.equals("metadata")) {
                    int eq = line.indexOf('=');
                    if (eq > 0) {
                        String key = line.substring(0, eq).trim().toLowerCase();
                        String val = line.substring(eq + 1).trim();
                        switch (key) {
                            case "name":
                                stage_name = val;
                                break;
                            case "bgm":
                                bgm_path = val;
                                break;
                            case "bg_path":
                                bg_path = val;
                                break;
                            case "bg_color":
                                bg_color_hex = val;
                                break;
                            case "star_count":
                                star_count = Integer.parseInt(val);
                                break;
                        }
                    }
                } else if (section.equals("spells")) {
                    String[] parts = line.split(",");
                    if (parts.length >= 5) {
                        Boss_Spell spell = new Boss_Spell();
                        spell.phase = Integer.parseInt(parts[0].trim());
                        spell.pattern_type = parts[1].trim().toLowerCase();
                        spell.color = Color.decode(parts[2].trim());
                        spell.bullet_speed = Double.parseDouble(parts[3].trim());
                        spell.shoot_cooldown = Integer.parseInt(parts[4].trim());
                        boss_spells.add(spell);
                    }
                } else if (section.equals("events")) {
                    String[] parts = line.split(",", -1);
                    if (parts.length >= 3) {
                        double offset = Double.parseDouble(parts[0].trim());
                        String type = parts[1].trim().toLowerCase();
                        Stage_Event event = new Stage_Event();
                        event.offset = offset;
                        event.type = type;

                        switch (type) {
                            case "scroll_speed":
                                event.vel_y = Double.parseDouble(parts[2].trim()); // store scroll speed value here
                                break;
                            case "spawn_enemy":
                                if (parts.length >= 9) {
                                    event.x = Double.parseDouble(parts[2].trim());
                                    event.y = Double.parseDouble(parts[3].trim());
                                    event.hp = Integer.parseInt(parts[4].trim());
                                    event.vel_x = Double.parseDouble(parts[5].trim());
                                    event.vel_y = Double.parseDouble(parts[6].trim());
                                    event.score = Integer.parseInt(parts[7].trim());
                                    event.radius = 15; // default drone radius
                                }
                                break;
                            case "dialogue":
                               if (parts.length >= 7) {
                                   event.speaker = parts[2].trim();
                                   event.expression = parts[3].trim();
                                   String raw_text = parts[4].trim();
                                   if (raw_text.startsWith("\"") && raw_text.endsWith("\"")) {
                                       raw_text = raw_text.substring(1, raw_text.length() - 1);
                                   }
                                   event.text = raw_text;
                                   event.char_delay_ms = Integer.parseInt(parts[5].trim());
                                   event.shakiness = Integer.parseInt(parts[6].trim());
                               }
                               break;
                            case "boss":
                                if (parts.length >= 8) {
                                    event.name = parts[2].trim();
                                    event.hp = Integer.parseInt(parts[3].trim());
                                    event.x = Double.parseDouble(parts[4].trim());
                                    event.y = Double.parseDouble(parts[5].trim());
                                    event.radius = Double.parseDouble(parts[6].trim());
                                    event.score = Integer.parseInt(parts[7].trim());
                                }
                                break;
                        }
                        event_timeline.add(event);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("failed parsing stage file: " + e.getMessage());
            e.printStackTrace();
        }

        // Sort events chronologically by offset
        Collections.sort(event_timeline, new Comparator<Stage_Event>() {
            @Override
            public int compare(Stage_Event e1, Stage_Event e2) {
                return Double.compare(e1.offset, e2.offset);
            }
        });
    }

    public void update_stage(List<Enemy_Entity> enemies) {
        if (stage_complete || is_in_dialogue) return;

        scroll_y += scroll_speed;

        // Process timeline events matching current scroll position
        for (Stage_Event ev : event_timeline) {
            if (!ev.triggered && scroll_y >= ev.offset) {
                ev.triggered = true;
                trigger_event(ev, enemies);
            }
        }

        // Check if boss spawned and is now dead
        if (boss_spawned) {
            boolean boss_alive = false;
            for (Enemy_Entity e : enemies) {
                if (e.is_active && e.is_boss) {
                    boss_alive = true;
                    break;
                }
            }
            if (!boss_alive) {
                stage_complete = true;
            }
        }
    }

    private void trigger_event(Stage_Event ev, List<Enemy_Entity> enemies) {
        switch (ev.type) {
            case "scroll_speed":
                this.scroll_speed = ev.vel_y;
                break;
            case "spawn_enemy":
                Enemy_Entity drone = new Enemy_Entity(ev.x, ev.y, ev.radius, ev.hp, false, "", ev.score);
                drone.vel_x = ev.vel_x;
                drone.vel_y = ev.vel_y;
                enemies.add(drone);
                break;
            case "dialogue":
                active_dialogues.add(ev);
                if (!is_in_dialogue) {
                    start_dialogue_cutscene();
                }
                break;
            case "boss":
                boss_spawned = true;
                Enemy_Entity boss = new Enemy_Entity(ev.x, ev.y, ev.radius, ev.hp, true, ev.name, ev.score);
                // Assign stage custom spells from parsing
                enemies.add(boss);
                Sound_Player.play_music("boss");
                break;
        }
    }

    private void start_dialogue_cutscene() {
        is_in_dialogue = true;
        dialogue_char_index = 0;
        dialogue_timer = 0;
        is_dialogue_finished = false;
        current_dialogue = active_dialogues.remove(0);
    }

    public void advance_dialogue() {
        if (!is_in_dialogue || current_dialogue == null) return;

        if (!is_dialogue_finished) {
            // Speed up to end of string if button is pressed while typing
            dialogue_char_index = current_dialogue.text.length();
            is_dialogue_finished = true;
        } else {
            if (!active_dialogues.isEmpty()) {
                start_dialogue_cutscene();
            } else {
                // Done with dialogue chain
                is_in_dialogue = false;
                current_dialogue = null;
            }
        }
    }

    public void skip_dialogue() {
        if (!is_in_dialogue) return;
        active_dialogues.clear();
        is_in_dialogue = false;
        current_dialogue = null;
    }

    public void update_dialogue_typing() {
        if (!is_in_dialogue || current_dialogue == null || is_dialogue_finished) return;

        dialogue_timer++;
        // Speed scaling based on char_delay_ms (approx at 60 FPS tick)
        int ticks_per_char = Math.max(1, current_dialogue.char_delay_ms / 16);
        if (dialogue_timer >= ticks_per_char) {
            dialogue_timer = 0;
            dialogue_char_index++;
            if (dialogue_char_index >= current_dialogue.text.length()) {
                dialogue_char_index = current_dialogue.text.length();
                is_dialogue_finished = true;
            }
            Sound_Player.play_sound("text_click"); // play text beep
        }
    }

    public void next_stage() {
        current_stage++;
        if (current_stage <= 3) {
            load_stage_file("stages/stage" + current_stage + ".stage");
            Sound_Player.play_music("stage");
        } else {
            stage_complete = true;
        }
    }
}
