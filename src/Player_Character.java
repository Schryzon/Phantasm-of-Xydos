import java.awt.Graphics2D;
import java.util.List;

public abstract class Player_Character {
    public double pos_x;
    public double pos_y;
    public double speed_normal;
    public double speed_focused;
    public double hitbox_radius;
    public int xydos_count; // lives
    public int spell_count; // bombs
    public double power_level; // ranges from 1.0 to 4.0
    public boolean is_invulnerable = false;
    public int invulnerability_timer = 0;
    
    public boolean spell_active = false;
    public int spell_timer = 0;

    // Bravery Gauge (from bullet grazing)
    public double bravery_gauge = 0.0;
    
    public Player_Character(double x, double y) {
        this.pos_x = x;
        this.pos_y = y;
        this.xydos_count = 3;
        this.spell_count = 3;
        this.power_level = 1.0;
    }

    public void update_player() {
        if (invulnerability_timer > 0) {
            invulnerability_timer--;
            if (invulnerability_timer <= 0) {
                is_invulnerable = false;
            }
        }
        if (spell_active) {
            spell_timer--;
            if (spell_timer <= 0) {
                spell_active = false;
            }
        }

        // Decay bravery gauge naturally by 0.2 per tick
        bravery_gauge -= 0.2;
        if (bravery_gauge < 0.0) {
            bravery_gauge = 0.0;
        }
    }

    public void hit() {
        if (is_invulnerable || spell_active) return;
        xydos_count--;
        is_invulnerable = true;
        invulnerability_timer = 120; // 2 seconds at 60 FPS
        pos_x = 640; // Reset position
        pos_y = 600;
        bravery_gauge = 0.0; // Reset bravery gauge upon taking damage
    }

    public int get_graze_damage_bonus() {
        return (int) (bravery_gauge / 25.0); // max +4 damage at 100% bravery
    }

    public abstract void fire_weapon(Bullet_Pool pool, boolean focused, List<Enemy_Entity> enemies);
    public abstract void activate_spell(Bullet_Pool pool, List<Enemy_Entity> enemies);
    public abstract void draw_character(Graphics2D g2d, boolean focused);
}
