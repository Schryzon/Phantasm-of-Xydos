import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

public class Mira_Character extends Player_Character {
    private int shoot_cooldown = 0;

    public Mira_Character(double x, double y) {
        super(x, y);
        this.speed_normal = 5.5;
        this.speed_focused = 2.2;
        this.hitbox_radius = 2.5;
    }

    @Override
    public void fire_weapon(Bullet_Pool pool, boolean focused, List<Enemy_Entity> enemies) {
        if (shoot_cooldown > 0) {
            shoot_cooldown--;
        }

        if (shoot_cooldown == 0) {
            int bonus = get_graze_damage_bonus();
            if (focused) {
                // Focus Mode: Concentrated straight fire (high DPS, narrow stream)
                pool.acquire_bullet(pos_x - 4, pos_y - 12, 0, -14, 3, 0, 2 + power_level + bonus, Color.GREEN);
                pool.acquire_bullet(pos_x + 4, pos_y - 12, 0, -14, 3, 0, 2 + power_level + bonus, Color.GREEN);
                pool.acquire_bullet(pos_x - 10, pos_y - 8, 0, -13, 3, 0, 1 + power_level + bonus, Color.GREEN);
                pool.acquire_bullet(pos_x + 10, pos_y - 8, 0, -13, 3, 0, 1 + power_level + bonus, Color.GREEN);
            } else {
                // Unfocused: Straight-fire + Spread-fire + Homing wind blades
                // 1. Straight-fire
                pool.acquire_bullet(pos_x, pos_y - 12, 0, -11, 4, 0, 2 + power_level + bonus, Color.GREEN);

                // 2. Spread-fire (angled outwards)
                pool.acquire_bullet(pos_x, pos_y - 8, -2, -10, 3.5, 0, 1 + power_level + bonus, Color.GREEN);
                pool.acquire_bullet(pos_x, pos_y - 8, 2, -10, 3.5, 0, 1 + power_level + bonus, Color.GREEN);
                pool.acquire_bullet(pos_x, pos_y - 5, -4, -9, 3, 0, 1 + bonus, Color.GREEN);
                pool.acquire_bullet(pos_x, pos_y - 5, 4, -9, 3, 0, 1 + bonus, Color.GREEN);

                // 3. Homing wind blades (type 4 - homing bullet)
                Bullet_Entity h1 = pool.acquire_bullet(pos_x - 15, pos_y, -1, -8, 3, 4, 1 + power_level + bonus, Color.WHITE);
                Bullet_Entity h2 = pool.acquire_bullet(pos_x + 15, pos_y, 1, -8, 3, 4, 1 + power_level + bonus, Color.WHITE);
                if (h1 != null) {
                    h1.is_homing = true;
                    h1.speed = 9;
                }
                if (h2 != null) {
                    h2.is_homing = true;
                    h2.speed = 9;
                }
            }
            shoot_cooldown = 7;
        }
    }

    @Override
    public void activate_spell(Bullet_Pool pool, List<Enemy_Entity> enemies) {
        if (spell_count <= 0 && !Config_Manager.infinite_spells) return;
        if (!Config_Manager.infinite_spells) {
            spell_count--;
        }
        spell_active = true;
        spell_timer = 120; // 2 seconds
        Sound_Player.play_sound("spell");

        // Clear all active enemy bullets
        for (Bullet_Entity b : pool.get_active_bullets()) {
            if (b.bullet_type != 0 && b.bullet_type != 4) {
                b.is_active = false;
            }
        }

        // Damage bosses on screen
        for (Enemy_Entity e : enemies) {
            if (e.is_active && e.is_boss) {
                e.take_damage(180);
            }
        }
    }

    @Override
    public void draw_character(Graphics2D g2d, boolean focused) {
        // Draw Mira (Green/Silver chassis)
        g2d.setColor(Color.GREEN);
        g2d.fillOval((int)(pos_x - 13), (int)(pos_y - 13), 26, 26);
        g2d.setColor(Color.WHITE);
        g2d.drawOval((int)(pos_x - 13), (int)(pos_y - 13), 26, 26);

        // Wings/Shield representation
        g2d.setColor(Color.CYAN);
        g2d.drawArc((int)(pos_x - 25), (int)(pos_y - 10), 50, 20, 0, 180);

        // Draw spell sanctuary effect if active
        if (spell_active) {
            g2d.setColor(new Color(0, 255, 127, 80));
            g2d.fillOval((int)(pos_x - 400), (int)(pos_y - 400), 800, 800);
            g2d.setColor(Color.GREEN);
            g2d.drawOval((int)(pos_x - 400), (int)(pos_y - 400), 800, 800);
        }

        // Draw hit box if focused or debug mode
        if (focused || Config_Manager.show_hitboxes) {
            g2d.setColor(Color.GREEN);
            g2d.fillOval((int)(pos_x - hitbox_radius), (int)(pos_y - hitbox_radius), (int)(hitbox_radius * 2), (int)(hitbox_radius * 2));
            g2d.setColor(Color.WHITE);
            g2d.drawOval((int)(pos_x - hitbox_radius), (int)(pos_y - hitbox_radius), (int)(hitbox_radius * 2), (int)(hitbox_radius * 2));
        }
    }
}
