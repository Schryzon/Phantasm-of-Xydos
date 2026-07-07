import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

public class Historia_Character extends Player_Character {
    private int shoot_cooldown = 0;
    private int slash_cooldown = 0;
    
    // Slash visual status
    private boolean is_slashing_right = false;
    private double slash_angle_right = 0;
    private double target_slash_rx = 0;
    private double target_slash_ry = 0;

    private boolean is_slashing_left = false;
    private double slash_angle_left = 0;
    private double target_slash_lx = 0;
    private double target_slash_ly = 0;

    // Slashes Step state machine
    private int slash_combo_step = 0;
    private int active_slash_timer = 0;

    // Spell throw parameters
    private int spears_to_throw = 0;
    private int spear_throw_timer = 0;

    public Historia_Character(double x, double y) {
        super(x, y);
        this.speed_normal = 5.0;
        this.speed_focused = 2.0;
        this.hitbox_radius = 4.0;
    }

    @Override
    public void update_player(Bullet_Pool pool) {
        super.update_player(pool);

        // Update sequential spear throw if spell is active
        if (spears_to_throw > 0) {
            spear_throw_timer--;
            if (spear_throw_timer <= 0) {
                double angle = -Math.PI / 2 + (Math.random() - 0.5) * 0.4;
                double vx = Math.cos(angle) * 8;
                double vy = Math.sin(angle) * 8;
                // Type 4: Player homing bullet. Hitbox radius 35.0, damage 120
                Bullet_Entity b = pool.acquire_bullet(pos_x, pos_y - 20, vx, vy, 35.0, 4, 120, Color.RED);
                if (b != null) {
                    b.is_homing = true;
                    b.speed = 10;
                }
                spears_to_throw--;
                spear_throw_timer = 14; // spacing between throws (~230ms)
            }
        }
    }

    @Override
    public void fire_weapon(Bullet_Pool pool, boolean focused, List<Enemy_Entity> enemies) {
        if (shoot_cooldown > 0) {
            shoot_cooldown--;
        }

        // Decay visual slash timer
        if (active_slash_timer > 0) {
            active_slash_timer--;
            if (active_slash_timer <= 0) {
                is_slashing_left = false;
                is_slashing_right = false;
            }
        }

        // Automatic close-range spear slash (uncontrollable by the player)
        if (slash_cooldown > 0) {
            slash_cooldown--;
        } else {
            // Check for close enemies
            Enemy_Entity closest = get_closest_enemy(enemies, 85.0);
            if (closest != null) {
                int bonus = get_graze_damage_bonus();
                // Determine slash damage based on power level
                int slash_dmg = 15 + bonus;
                if (power_level >= 2.0 && power_level < 3.0) {
                    slash_dmg = 25 + bonus;
                } else if (power_level >= 3.0 && power_level < 4.0) {
                    slash_dmg = 35 + bonus;
                } else if (power_level >= 4.0) {
                    slash_dmg = 50 + bonus;
                }

                closest.take_damage(slash_dmg);

                // Combo Step execution: Left -> Right -> Left -> Pause -> Right -> Left -> Right -> Pause
                boolean is_left_slash = (slash_combo_step == 0 || slash_combo_step == 2 || slash_combo_step == 4);
                if (is_left_slash) {
                    is_slashing_left = true;
                    is_slashing_right = false;
                    slash_angle_left = Math.random() * Math.PI * 2;
                    target_slash_lx = closest.pos_x;
                    target_slash_ly = closest.pos_y;
                } else {
                    is_slashing_right = true;
                    is_slashing_left = false;
                    slash_angle_right = Math.random() * Math.PI * 2;
                    target_slash_rx = closest.pos_x;
                    target_slash_ry = closest.pos_y;
                }

                // Set cooldown: Step 2 and 5 end with a 24 frame (~400ms) pause, others are 10 frames (~160ms)
                if (slash_combo_step == 2 || slash_combo_step == 5) {
                    slash_cooldown = 24;
                } else {
                    slash_cooldown = 10;
                }

                Sound_Player.play_sound("slash");
                active_slash_timer = 8;
                slash_combo_step = (slash_combo_step + 1) % 6;
            }
        }

        if (shoot_cooldown == 0) {
            int bonus = get_graze_damage_bonus();
            int ipower = (int) power_level;
            
            // Firing weapon with power level bullet count progression
            if (focused) {
                // High concentrated bolts
                if (power_level < 2.0) {
                    pool.acquire_bullet(pos_x, pos_y - 12, 0, -12, 4, 0, 2 + bonus, Color.CYAN);
                } else if (power_level >= 2.0 && power_level < 3.0) {
                    pool.acquire_bullet(pos_x - 6, pos_y - 10, 0, -12, 4, 0, 3 + bonus, Color.CYAN);
                    pool.acquire_bullet(pos_x + 6, pos_y - 10, 0, -12, 4, 0, 3 + bonus, Color.CYAN);
                } else if (power_level >= 3.0 && power_level < 4.0) {
                    pool.acquire_bullet(pos_x - 10, pos_y - 8, 0, -12, 4, 0, 4 + bonus, Color.CYAN);
                    pool.acquire_bullet(pos_x, pos_y - 12, 0, -13, 4, 0, 4 + bonus, Color.CYAN);
                    pool.acquire_bullet(pos_x + 10, pos_y - 8, 0, -12, 4, 0, 4 + bonus, Color.CYAN);
                } else { // MAX Power
                    pool.acquire_bullet(pos_x - 12, pos_y - 8, 0, -13, 5, 0, 5 + bonus, Color.CYAN);
                    pool.acquire_bullet(pos_x - 4, pos_y - 12, 0, -14, 5, 0, 5 + bonus, Color.CYAN);
                    pool.acquire_bullet(pos_x + 4, pos_y - 12, 0, -14, 5, 0, 5 + bonus, Color.CYAN);
                    pool.acquire_bullet(pos_x + 12, pos_y - 8, 0, -13, 5, 0, 5 + bonus, Color.CYAN);
                }
            } else {
                // Wider standard lines
                if (power_level < 2.0) {
                    pool.acquire_bullet(pos_x, pos_y - 12, 0, -10, 5, 0, 2 + bonus, Color.CYAN);
                } else if (power_level >= 2.0 && power_level < 3.0) {
                    pool.acquire_bullet(pos_x - 8, pos_y - 5, 0, -10, 5, 0, 2 + bonus, Color.CYAN);
                    pool.acquire_bullet(pos_x + 8, pos_y - 5, 0, -10, 5, 0, 2 + bonus, Color.CYAN);
                } else if (power_level >= 3.0 && power_level < 4.0) {
                    pool.acquire_bullet(pos_x - 12, pos_y - 5, -0.5, -10, 5, 0, 3 + bonus, Color.CYAN);
                    pool.acquire_bullet(pos_x, pos_y - 12, 0, -11, 6, 0, 4 + bonus, Color.CYAN);
                    pool.acquire_bullet(pos_x + 12, pos_y - 5, 0.5, -10, 5, 0, 3 + bonus, Color.CYAN);
                } else { // MAX Power
                    pool.acquire_bullet(pos_x - 16, pos_y - 5, -1.0, -9, 5, 0, 4 + bonus, Color.CYAN);
                    pool.acquire_bullet(pos_x - 6, pos_y - 10, -0.3, -11, 6, 0, 5 + bonus, Color.CYAN);
                    pool.acquire_bullet(pos_x + 6, pos_y - 10, 0.3, -11, 6, 0, 5 + bonus, Color.CYAN);
                    pool.acquire_bullet(pos_x + 16, pos_y - 5, 1.0, -9, 5, 0, 4 + bonus, Color.CYAN);
                }
            }
            Sound_Player.play_sound("shoot");
            shoot_cooldown = 6;
        }
    }

    private Enemy_Entity get_closest_enemy(List<Enemy_Entity> enemies, double max_dist) {
        Enemy_Entity closest = null;
        double min_dist = max_dist;
        for (Enemy_Entity e : enemies) {
            if (!e.is_active) continue;
            double dist = Math.hypot(e.pos_x - pos_x, e.pos_y - pos_y);
            if (dist < min_dist) {
                min_dist = dist;
                closest = e;
            }
        }
        return closest;
    }

    @Override
    public void activate_spell(Bullet_Pool pool, List<Enemy_Entity> enemies) {
        if (spell_count <= 0 && !Config_Manager.infinite_spells) return;
        if (!Config_Manager.infinite_spells) {
            spell_count--;
        }
        spell_active = true;
        spell_timer = 150; // Invulnerable for 2.5 seconds
        Sound_Player.play_sound("spell");

        // Setup sequential spear throw of 7 giant homing spears
        spears_to_throw = 7;
        spear_throw_timer = 0; // throw first spear immediately
    }

    @Override
    public void draw_character(Graphics2D g2d, boolean focused) {
        // Draw Historia (Red/Gold chassis)
        g2d.setColor(Color.RED);
        g2d.fillRect((int)(pos_x - 15), (int)(pos_y - 15), 30, 30);
        g2d.setColor(Color.ORANGE);
        g2d.drawRect((int)(pos_x - 15), (int)(pos_y - 15), 30, 30);

        // Spear representation
        g2d.setColor(Color.YELLOW);
        g2d.drawLine((int)pos_x, (int)pos_y, (int)pos_x, (int)(pos_y - 35));

        // Draw active right slash effect
        if (is_slashing_right) {
            g2d.setColor(new Color(255, 140, 0, 180)); // Orange-red right slash
            g2d.drawArc((int)(pos_x - 60), (int)(pos_y - 60), 120, 120, (int)(Math.toDegrees(slash_angle_right) - 45), 90);
            g2d.drawLine((int)pos_x, (int)pos_y, (int)target_slash_rx, (int)target_slash_ry);
        }

        // Draw active left slash effect
        if (is_slashing_left) {
            g2d.setColor(new Color(255, 215, 0, 180)); // Gold left slash
            g2d.drawArc((int)(pos_x - 60), (int)(pos_y - 60), 120, 120, (int)(Math.toDegrees(slash_angle_left) + 135), 90);
            g2d.drawLine((int)pos_x, (int)pos_y, (int)target_slash_lx, (int)target_slash_ly);
        }

        // Draw hit box if focused or debug mode
        if (focused || Config_Manager.show_hitboxes) {
            g2d.setColor(Color.RED);
            g2d.fillOval((int)(pos_x - hitbox_radius), (int)(pos_y - hitbox_radius), (int)(hitbox_radius * 2), (int)(hitbox_radius * 2));
            g2d.setColor(Color.WHITE);
            g2d.drawOval((int)(pos_x - hitbox_radius), (int)(pos_y - hitbox_radius), (int)(hitbox_radius * 2), (int)(hitbox_radius * 2));
        }
    }
}
