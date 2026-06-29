import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;

public class Config_Manager {
    public static boolean debug_enabled = false;
    public static boolean show_hitboxes = false;
    public static boolean god_mode = false;
    public static boolean infinite_spells = false;

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
                    String value = line.substring(eq_index + 1).trim().toLowerCase();
                    boolean val_bool = value.equals("true") || value.equals("1");

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
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("failed to load configuration: " + e.getMessage());
        }
    }
}
