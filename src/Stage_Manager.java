import java.util.List;
import java.util.ArrayList;
import java.awt.Color;

public class Stage_Manager {
    public int current_stage = 1;
    public double scroll_y = 0;
    public boolean boss_spawned = false;
    public boolean stage_complete = false;

    private int spawn_timer = 0;

    public void update_stage(List<Enemy_Entity> enemies) {
        if (stage_complete) return;

        scroll_y += 1.0; // Scrolling background speed

        // Spawning wave patterns before the boss
        if (scroll_y < 2000) {
            spawn_timer++;
            if (spawn_timer % 80 == 0) {
                // Spawn a wave of 3-4 enemies at the top
                int count = 3 + (int)(Math.random() * 2);
                for (int i = 0; i < count; i++) {
                    double x = 100 + (600.0 / (count - 1)) * i + (Math.random() * 30 - 15);
                    double y = -50 - (Math.random() * 40);
                    Enemy_Entity e = new Enemy_Entity(x, y, 15, 8 + current_stage * 4, false, "", 100);
                    e.vel_y = 2.0 + (Math.random() * 1.5);
                    e.vel_x = (Math.random() - 0.5) * 1.0;
                    enemies.add(e);
                }
            }
        } else if (!boss_spawned) {
            // Spawn Stage Boss!
            boss_spawned = true;
            Enemy_Entity boss = null;
            if (current_stage == 1) {
                boss = new Enemy_Entity(400, -80, 45, 1200, true, "Victoria Koura", 5000);
            } else if (current_stage == 2) {
                boss = new Enemy_Entity(400, -80, 50, 2000, true, "Queen Fenria & Xelisa", 10000);
            } else {
                boss = new Enemy_Entity(400, -80, 55, 3000, true, "Goddess Cyria", 20000);
            }
            boss.vel_y = 2.0;
            enemies.add(boss);
            Sound_Player.play_music("boss");
        } else {
            // Check if boss is dead
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

    public void next_stage() {
        current_stage++;
        scroll_y = 0;
        boss_spawned = false;
        stage_complete = false;
        spawn_timer = 0;
        Sound_Player.play_music("stage");
    }
}
