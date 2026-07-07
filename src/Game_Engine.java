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
    public final List<Item_Drop> items;
    public final Stage_Manager stage_manager;
    public final Input_Manager input_manager;
    public int score = 0;
    public static int current_difficulty = 1; // 0=Rookie, 1=Trooper, 2=Elite, 3=Android, 4=CyroN
    public boolean game_over = false;
    public boolean game_win = false;
    public boolean is_paused = false;
    public int pause_selection = 0; // 0 = RESUME, 1 = RETURN TO TITLE
    public boolean return_to_title_requested = false;

    public static double get_difficulty_speed_mult() {
        switch (current_difficulty) {
            case 0: return 0.7;  // Rookie
            case 1: return 1.0;  // Trooper
            case 2: return 1.35; // Elite
            case 3: return 1.7;  // Android
            case 4: return 2.1;  // CyroN
            default: return 1.0;
        }
    }

    public static double get_difficulty_cooldown_mult() {
        switch (current_difficulty) {
            case 0: return 1.4;  // Rookie
            case 1: return 1.0;  // Trooper
            case 2: return 0.8;  // Elite
            case 3: return 0.6;  // Android
            case 4: return 0.45; // CyroN
            default: return 1.0;
        }
    }

    // Background Image cache
    private BufferedImage bg_image = null;
    private String loaded_bg_path = "";

    // Procedural stars for parallax background
    private final List<Star> stars = new ArrayList<>();

    private static class Star {
        double x, y, speed;
        Star() {
            x = 320 + Math.random() * 640;
            y = Math.random() * 720;
            speed = 1.0 + Math.random() * 3.0;
        }
        void update() {
            y += speed;
            if (y > 720) {
                y = 0;
                x = 320 + Math.random() * 640;
            }
        }
    }

    public Game_Engine(int char_choice, Input_Manager input) {
        this.bullet_pool = new Bullet_Pool(3000);
        this.enemies = new ArrayList<>();
        this.items = new ArrayList<>();
        this.stage_manager = new Stage_Manager();
        this.input_manager = input;

        if (char_choice == 1) {
            this.player = new Historia_Character(640, 600);
        } else {
            this.player = new Mira_Character(640, 600);
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
            pause_selection = 0; // reset selection
        }

        if (is_paused) {
            // Check menu navigation inputs
            if (input_manager.up) {
                pause_selection = 0;
                input_manager.up = false; // consume
            } else if (input_manager.down) {
                pause_selection = 1;
                input_manager.down = false; // consume
            }
            if (input_manager.shoot) {
                input_manager.clear();
                if (pause_selection == 0) {
                    is_paused = false;
                } else {
                    return_to_title_requested = true;
                }
            }
            return;
        }

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

        // Keep player in bounds of 640x720 play area (centered at x:320-960)
        if (player.pos_x < 320 + 15) player.pos_x = 320 + 15;
        if (player.pos_x > 960 - 15) player.pos_x = 960 - 15;
        if (player.pos_y < 20) player.pos_y = 20;
        if (player.pos_y > 700) player.pos_y = 700;

        player.update_player(bullet_pool);

        if (input_manager.shoot) {
            player.fire_weapon(bullet_pool, input_manager.focus, enemies);
        }

        if (input_manager.spell && !player.spell_active) {
            player.activate_spell(bullet_pool, enemies);
        }

        // 3. Update stage waves (offset bounds adjusted to match center column)
        stage_manager.update_stage(enemies);

        // 4. Update enemies & their bullets
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy_Entity e = enemies.get(i);
            if (!e.is_active) {
                if (e.health <= 0) {
                    score += e.score_value;
                    // 60% chance to drop power-ups or score boxes
                    if (Math.random() < 0.6) {
                        int type = Math.random() < 0.4 ? 1 : 2; // 1=Power, 2=Score
                        items.add(new Item_Drop(e.pos_x, e.pos_y, type));
                    }
                }
                enemies.remove(i);
                continue;
            }
            e.update_enemy(bullet_pool, player.pos_x, player.pos_y, stage_manager.boss_spells);
        }

        // 5. Update items and magnet pull logic
        boolean auto_magnet = player.pos_y < 180; // top quarter collection line
        for (int i = items.size() - 1; i >= 0; i--) {
            Item_Drop item = items.get(i);
            if (!item.is_active) {
                items.remove(i);
                continue;
            }
            if (auto_magnet) {
                item.is_magnetized = true;
            }
            item.update(player.pos_x, player.pos_y);

            // Collide with player to collect
            double dist = Math.hypot(player.pos_x - item.pos_x, player.pos_y - item.pos_y);
            if (dist < 25.0) {
                item.is_active = false;
                if (item.type == 1) {
                    player.power_level += 0.1;
                    if (player.power_level > 4.0) {
                        player.power_level = 4.0;
                    }
                    Sound_Player.play_sound("powerup");
                } else {
                    score += 1000;
                    Sound_Player.play_sound("score");
                }
                items.remove(i);
            }
        }

        // 6. Update active bullets & homing steering
        List<Bullet_Entity> active_bullets = bullet_pool.get_active_bullets();
        for (Bullet_Entity b : active_bullets) {
            if (!b.is_active) continue;

            // Homing steering logic
            if (b.is_homing) {
                Enemy_Entity target = null;
                double min_dist = Double.MAX_VALUE;
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

        // 7. Collision detection
        check_collisions(active_bullets);

        // 8. Check Stage Cleared transitions
        if (stage_manager.stage_complete) {
            if (stage_manager.current_stage < 3) {
                stage_manager.next_stage();
                bullet_pool.clear();
                enemies.clear();
                items.clear();
            } else {
                game_win = true;
                Sound_Player.stop_music();
            }
        }

        // 9. Check Game Over
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
                // Enemy bullet hitting player
                if (!player.is_invulnerable && !player.spell_active && !Config_Manager.god_mode) {
                    double dist = Math.hypot(player.pos_x - b.pos_x, player.pos_y - b.pos_y);
                    if (dist < (player.hitbox_radius + b.radius)) {
                        player.hit();
                        b.is_active = false;
                        break;
                    } else if (dist < 30.0) {
                        // Graze detection!
                        if (!b.grazed) {
                            b.grazed = true;
                            player.bravery_gauge += 1.5;
                            if (player.bravery_gauge > 100.0) {
                                player.bravery_gauge = 100.0;
                            }
                            player.graze_emblem_timer = 15;
                            Sound_Player.play_sound("graze");
                        }
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

        // Fill background color of middle column
        Color bg_color = Color.BLACK;
        try {
            bg_color = Color.decode(stage_manager.bg_color_hex);
        } catch (Exception ignored) {}
        g2d.setColor(bg_color);
        g2d.fillRect(320, 0, 640, 720);

        // Set clipping mask for game field
        java.awt.Shape old_clip = g2d.getClip();
        g2d.setClip(320, 0, 640, 720);

        // Tile background image inside centered area if loaded
        if (bg_image != null) {
            int img_h = bg_image.getHeight();
            int img_w = bg_image.getWidth();
            int offset_y = (int) (stage_manager.scroll_y % img_h);
            for (int y = -img_h; y < 720 + img_h; y += img_h) {
                g2d.drawImage(bg_image, 320, y + offset_y, 640, img_h, null);
            }
        }

        // Draw parallax background stars
        g2d.setColor(Color.LIGHT_GRAY);
        int render_stars = Math.min(stars.size(), stage_manager.star_count);
        for (int i = 0; i < render_stars; i++) {
            Star s = stars.get(i);
            g2d.fillOval((int)s.x, (int)s.y, 2, 2);
        }

        // Draw items
        for (Item_Drop item : items) {
            if (item.is_active) {
                if (item.type == 1) {
                    g2d.setColor(Color.RED);
                    g2d.fillRect((int)item.pos_x - 6, (int)item.pos_y - 6, 12, 12);
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new java.awt.Font("Consolas", java.awt.Font.BOLD, 10));
                    g2d.drawString("P", (int)item.pos_x - 3, (int)item.pos_y + 4);
                } else {
                    g2d.setColor(Color.BLUE);
                    g2d.fillRect((int)item.pos_x - 6, (int)item.pos_y - 6, 12, 12);
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new java.awt.Font("Consolas", java.awt.Font.BOLD, 10));
                    g2d.drawString("S", (int)item.pos_x - 3, (int)item.pos_y + 4);
                }
            }
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

        // Reset clipping mask
        g2d.setClip(old_clip);
    }
}
