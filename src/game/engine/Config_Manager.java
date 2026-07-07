package game.engine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;

public class Config_Manager {
    public static boolean debug_enabled = false;
    public static boolean show_hitboxes = false;
    public static boolean god_mode = false;
    public static boolean infinite_spells = false;

    // Settings
    public static int sfx_volume = 80;
    public static int music_volume = 80;
    public static int fps_limit = 60;
    public static boolean fullscreen = false;

    // Keybinds (default keyboard keycodes)
    public static int key_up = 38;     // Up Arrow
    public static int key_down = 40;   // Down Arrow
    public static int key_left = 37;   // Left Arrow
    public static int key_right = 39;  // Right Arrow
    public static int key_shoot = 90;  // Z
    public static int key_spell = 88;  // X
    public static int key_focus = 16;  // Shift

    public static void load_config(String file_path) {
        File file = new File(file_path);
        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#") || line.startsWith(";")) {
                    continue;
                }
                if (line.startsWith("[") && line.endsWith("]")) {
                    continue;
                }
                int eq_index = line.indexOf('=');
                if (eq_index > 0) {
                    String key = line.substring(0, eq_index).trim().toLowerCase();
                    String value = line.substring(eq_index + 1).trim();
                    boolean val_bool = value.equalsIgnoreCase("true") || value.equals("1");

                    switch (key) {
                        case "enabled":
                            debug_enabled = val_bool;
                            break;
                        case "show_hitboxes":
                            show_hitboxes = val_bool;
                            break;
                        case "god_mode":
                            god_mode = val_bool;
                            break;
                        case "infinite_spells":
                            infinite_spells = val_bool;
                            break;
                        case "sfx_volume":
                            sfx_volume = Integer.parseInt(value);
                            break;
                        case "music_volume":
                            music_volume = Integer.parseInt(value);
                            break;
                        case "fps_limit":
                            fps_limit = Integer.parseInt(value);
                            break;
                        case "fullscreen":
                            fullscreen = val_bool;
                            break;
                        case "key_up":
                            key_up = Integer.parseInt(value);
                            break;
                        case "key_down":
                            key_down = Integer.parseInt(value);
                            break;
                        case "key_left":
                            key_left = Integer.parseInt(value);
                            break;
                        case "key_right":
                            key_right = Integer.parseInt(value);
                            break;
                        case "key_shoot":
                            key_shoot = Integer.parseInt(value);
                            break;
                        case "key_spell":
                            key_spell = Integer.parseInt(value);
                            break;
                        case "key_focus":
                            key_focus = Integer.parseInt(value);
                            break;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("failed to load configuration: " + e.getMessage());
        }
    }

    public static void save_config(String file_path) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file_path))) {
            writer.write("[debug]\n");
            writer.write("enabled = " + debug_enabled + "\n");
            writer.write("show_hitboxes = " + show_hitboxes + "\n");
            writer.write("god_mode = " + god_mode + "\n");
            writer.write("infinite_spells = " + infinite_spells + "\n\n");

            writer.write("[settings]\n");
            writer.write("sfx_volume = " + sfx_volume + "\n");
            writer.write("music_volume = " + music_volume + "\n");
            writer.write("fps_limit = " + fps_limit + "\n");
            writer.write("fullscreen = " + fullscreen + "\n\n");

            writer.write("[keybinds]\n");
            writer.write("key_up = " + key_up + "\n");
            writer.write("key_down = " + key_down + "\n");
            writer.write("key_left = " + key_left + "\n");
            writer.write("key_right = " + key_right + "\n");
            writer.write("key_shoot = " + key_shoot + "\n");
            writer.write("key_spell = " + key_spell + "\n");
            writer.write("key_focus = " + key_focus + "\n");
        } catch (IOException e) {
            System.err.println("failed to save configuration: " + e.getMessage());
        }
    }
}
