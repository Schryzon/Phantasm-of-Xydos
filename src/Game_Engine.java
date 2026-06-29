import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Game_Engine {
    public Player_Character player;
    public final Bullet_Pool bullet_pool;
    public final List<Enemy_Entity> enemies;
    public final Stage_Manager stage_manager;
    public final Input_Manager input_manager;
    public int score = 0;
    public boolean game_over = false;
    public boolean game_win = false;
    public boolean is_paused = false;

    // Background Image cache
    private BufferedImage bg_image = null;
    private String loaded_bg_path = "";

    // Procedural stars for parallax background
    private final List<Star> stars = new ArrayList<>();

    private static class Star {
        double x, y, speed;
        Star() {
            x = Math.random() * 800;
            y = Math.random() * 800;
            speed = 1.0 + Math.random() * 3.0;
        }
        void update() {
            y += speed;
            if (y > 800) {
                y = 0;
                x = Math.random() * 800;
            }
        }
    }

    public Game_Engine(int char_choice, Input_Manager input) {
        this.bullet_pool = new Bullet_Pool(3000);
        this.enemies = new ArrayList<>();
        this.stage_manager = new Stage_Manager();
        this.input_manager = input;

        if (char_choice == 1) {
            this.player = new Historia_Character(400, 650);
        } else {
            this.player = new Mira_Character(400, 650);
        }

        for (int i = 0; i < 80; i++) {
            stars.add(new Star());
        }

        Sound_Player.play_music("stage");
    }

    public void update_game() {
        input_manager.poll_controller();
        input_manager.update_pause_state();
        if (input_manager.pause_triggered) {
            is_paused = !is_paused;
        }

        if (is_paused) return;
        if (game_over || game_win) return;

        // Dialogue pause state (freeze action, allow inputs to advance dialogues)
        if (stage_manager.is_in_dialogue) {
            stage_manager.update_dialogue_typing();
            if (input_manager.shoot) {
                stage_manager.advance_dialogue();
                input_manager.clear();
            }
            if (input_manager.skip) {
                stage_manager.skip_dialogue();
                input_manager.clear();
            }
            return;
        }

        // Dynamically load background image if it changed
        if (!stage_manager.bg_path.equals(loaded_bg_path)) {
            loaded_bg_path = stage_manager.bg_path;
            if (!loaded_bg_path.isEmpty()) {
                try {
                    bg_image = ImageIO.read(new File(loaded_bg_path));
                } catch (Exception e) {
                    bg_image = null;
                    System.err.println("[WARNING] failed loading stage background image: " + loaded_bg_path);
                }
            } else {
                bg_image = null;
            }
        }



        // 1. Update background stars
        for (Star s : stars) {
            s.update();
        }

        // 2. Update player inputs & movements
        double speed = input_manager.focus ? player.speed_focused : player.speed_normal;
        if (input_manager.up) player.pos_y -= speed;
        if (input_manager.down) player.pos_y += speed;
        if (input_manager.left) player.pos_x -= speed;
        if (input_manager.right) player.pos_x += speed;

        // Keep player in bounds
        if (player.pos_x < 20) player.pos_x = 20;
        if (player.pos_x > 780) player.pos_x = 780;
        if (player.pos_y < 50) player.pos_y = 50;
        if (player.pos_y > 750) player.pos_y = 750;

        player.update_player();

        if (input_manager.shoot) {
            player.fire_weapon(bullet_pool, input_manager.focus, enemies);
        }

        if (input_manager.spell && !player.spell_active) {
            player.activate_spell(bullet_pool, enemies);
        }

        // 3. Update stage waves
        stage_manager.update_stage(enemies);

        // 4. Update enemies & their bullets
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy_Entity e = enemies.get(i);
            if (!e.is_active) {
                if (e.health <= 0) {
                    score += e.score_value;
                }
                enemies.remove(i);
                continue;
            }
            e.update_enemy(bullet_pool, player.pos_x, player.pos_y, stage_manager.boss_spells);
        }

        // 5. Update active bullets & homing steering
        List<Bullet_Entity> active_bullets = bullet_pool.get_active_bullets();
        for (Bullet_Entity b : active_bullets) {
            if (!b.is_active) continue;

            // Homing steering logic
            if (b.is_homing) {
                Enemy_Entity target = null;
                double min_dist = Double.MAX_VALUE;
                // Find closest active enemy
                for (Enemy_Entity e : enemies) {
                    if (!e.is_active) continue;
                    double dist = Math.hypot(e.pos_x - b.pos_x, e.pos_y - b.pos_y);
                    if (dist < min_dist) {
                        min_dist = dist;
                        target = e;
                    }
                }

                if (target != null) {
                    double target_angle = Math.atan2(target.pos_y - b.pos_y, target.pos_x - b.pos_x);
                    b.vel_x = Math.cos(target_angle) * b.speed;
                    b.vel_y = Math.sin(target_angle) * b.speed;
                }
            }

            b.update();
        }

        // 6. Collision detection
        check_collisions(active_bullets);

        // 7. Check Stage Cleared transitions
        if (stage_manager.stage_complete) {
            if (stage_manager.current_stage < 3) {
                stage_manager.next_stage();
                bullet_pool.clear();
                enemies.clear();
            } else {
                game_win = true;
                Sound_Player.stop_music();
            }
        }

        // 8. Check Game Over
        if (player.xydos_count < 0) {
            game_over = true;
            Sound_Player.stop_music();
        }
    }

    private void check_collisions(List<Bullet_Entity> active_bullets) {
        for (Bullet_Entity b : active_bullets) {
            if (!b.is_active) continue;

            if (b.bullet_type == 0 || b.bullet_type == 4) {
                // Player bullet hitting enemies
                for (Enemy_Entity e : enemies) {
                    if (!e.is_active) continue;
                    double dist = Math.hypot(e.pos_x - b.pos_x, e.pos_y - b.pos_y);
                    if (dist < (e.radius + b.radius)) {
                        e.take_damage(b.damage);
                        b.is_active = false;
                        score += 10;
                        break;
                    }
                }
            } else {
                // Enemy bullet hitting player (only if player is not invulnerable)
                if (!player.is_invulnerable && !player.spell_active && !Config_Manager.god_mode) {
                    double dist = Math.hypot(player.pos_x - b.pos_x, player.pos_y - b.pos_y);
                    if (dist < (player.hitbox_radius + b.radius)) {
                        player.hit();
                        b.is_active = false;
                        break;
                    } else if (dist < 30.0) {
                        // Graze detection!
                        player.bravery_gauge += 1.5;
                        if (player.bravery_gauge > 100.0) {
                            player.bravery_gauge = 100.0;
                        }
                        Sound_Player.play_sound("graze");
                    }
                }
            }
        }

        // Check body collisions with enemies
        if (!player.is_invulnerable && !player.spell_active && !Config_Manager.god_mode) {
            for (Enemy_Entity e : enemies) {
                if (!e.is_active) continue;
                double dist = Math.hypot(player.pos_x - e.pos_x, player.pos_y - e.pos_y);
                if (dist < (player.hitbox_radius + e.radius)) {
                    player.hit();
                    if (!e.is_boss) {
                        e.take_damage(999);
                    }
                    break;
                }
            }
        }
    }

    public void draw_game(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fill background color
        Color bg_color = Color.BLACK;
        try {
            bg_color = Color.decode(stage_manager.bg_color_hex);
        } catch (Exception ignored) {}
        g2d.setColor(bg_color);
        g2d.fillRect(0, 0, 800, 800);

        // Tile background image if loaded
        if (bg_image != null) {
            int img_h = bg_image.getHeight();
            int img_w = bg_image.getWidth();
            int offset_y = (int) (stage_manager.scroll_y % img_h);
            for (int y = -img_h; y < 800 + img_h; y += img_h) {
                g2d.drawImage(bg_image, 0, y + offset_y, 800, img_h, null);
            }
        }

        // Draw parallax background stars
        g2d.setColor(Color.LIGHT_GRAY);
        // Limit active star count to config
        int render_stars = Math.min(stars.size(), stage_manager.star_count);
        for (int i = 0; i < render_stars; i++) {
            Star s = stars.get(i);
            g2d.fillOval((int)s.x, (int)s.y, 2, 2);
        }

        // Draw active bullets
        for (Bullet_Entity b : bullet_pool.get_active_bullets()) {
            if (b.is_active) {
                g2d.setColor(b.color);
                g2d.fillOval((int)(b.pos_x - b.radius), (int)(b.pos_y - b.radius), (int)(b.radius * 2), (int)(b.radius * 2));
            }
        }

        // Draw active enemies
        for (Enemy_Entity e : enemies) {
            e.draw_enemy(g2d);
        }

        // Draw player character
        player.draw_character(g2d, input_manager.focus);
    }
}
