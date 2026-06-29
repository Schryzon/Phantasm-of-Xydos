import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

public class Historia_Character extends Player_Character {
    private int shoot_cooldown = 0;
    private int slash_cooldown = 0;
    private double slash_angle = 0;
    private boolean is_slashing = false;
    private double target_slash_x = 0;
    private double target_slash_y = 0;

    public Historia_Character(double x, double y) {
        super(x, y);
        this.speed_normal = 5.0;
        this.speed_focused = 2.0;
        this.hitbox_radius = 4.0;
    }

    @Override
    public void fire_weapon(Bullet_Pool pool, boolean focused, List<Enemy_Entity> enemies) {
        if (shoot_cooldown > 0) {
            shoot_cooldown--;
        }

        // Automatic close-range spear slash (uncontrollable by the player)
        if (slash_cooldown > 0) {
            slash_cooldown--;
        } else {
            is_slashing = false;
            // Check for close enemies
            Enemy_Entity closest = null;
            double min_dist = 85.0; // melee range (85px)
            for (Enemy_Entity e : enemies) {
                if (!e.is_active) continue;
                double dist = Math.hypot(e.pos_x - pos_x, e.pos_y - pos_y);
                if (dist < min_dist) {
                    min_dist = dist;
                    closest = e;
                }
            }
            if (closest != null) {
                closest.take_damage(25 + power_level * 5);
                target_slash_x = closest.pos_x;
                target_slash_y = closest.pos_y;
                is_slashing = true;
                slash_cooldown = 15; // slash twice a second
                slash_angle = Math.random() * Math.PI * 2;
                Sound_Player.play_sound("slash");
            }
        }

        if (shoot_cooldown == 0) {
            // CAS-8 straight fire lightning bolts
            if (focused) {
                // High concentrated bolts
                pool.acquire_bullet(pos_x - 8, pos_y - 10, 0, -12, 4, 0, 3 + power_level, Color.CYAN);
                pool.acquire_bullet(pos_x + 8, pos_y - 10, 0, -12, 4, 0, 3 + power_level, Color.CYAN);
            } else {
                // Slightly offset straight lines
                pool.acquire_bullet(pos_x - 12, pos_y - 5, 0, -10, 5, 0, 2 + power_level, Color.CYAN);
                pool.acquire_bullet(pos_x, pos_y - 12, 0, -11, 6, 0, 3 + power_level, Color.CYAN);
                pool.acquire_bullet(pos_x + 12, pos_y - 5, 0, -10, 5, 0, 2 + power_level, Color.CYAN);
            }
            shoot_cooldown = 6;
        }
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

        // Spawn giant homing spears prioritizing bosses (these spears DO NOT clear bullets)
        for (int i = 0; i < 15; i++) {
            double angle = -Math.PI / 2 + (Math.random() - 0.5) * 0.8;
            double vx = Math.cos(angle) * 8;
            double vy = Math.sin(angle) * 8;
            // Type 4: Player homing bullet
            Bullet_Entity b = pool.acquire_bullet(pos_x, pos_y - 20, vx, vy, 12, 4, 60, Color.RED);
            if (b != null) {
                b.is_homing = true;
                b.speed = 10;
            }
        }
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

        // Draw active slash effect
        if (is_slashing && slash_cooldown > 5) {
            g2d.setColor(new Color(255, 215, 0, 180));
            g2d.drawArc((int)(pos_x - 60), (int)(pos_y - 60), 120, 120, (int)(Math.toDegrees(slash_angle) - 45), 90);
            g2d.drawLine((int)pos_x, (int)pos_y, (int)target_slash_x, (int)target_slash_y);
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
