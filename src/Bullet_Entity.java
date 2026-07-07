import java.awt.Color;

public class Bullet_Entity {
    public double pos_x;
    public double pos_y;
    public double vel_x;
    public double vel_y;
    public double radius;
    public int bullet_type; // 0 = Player, 1 = Enemy Normal, 2 = Enemy Homing, 3 = Boss Spell, 4 = Player Homing
    public boolean is_active;
    public int damage;
    public Color color;

    // Additional fields for advanced behaviors (e.g. homing or circular movement)
    public boolean is_homing;
    public double speed;
    public double angle;
    public double rotation_speed;
    public boolean grazed;

    public Bullet_Entity() {
        this.is_active = false;
    }

    public void init(double x, double y, double vx, double vy, double r, int type, int dmg, Color c) {
        this.pos_x = x;
        this.pos_y = y;
        this.vel_x = vx;
        this.vel_y = vy;
        this.radius = r;
        this.bullet_type = type;
        this.damage = dmg;
        this.color = c;
        this.is_active = true;
        this.is_homing = false;
        this.speed = 0;
        this.angle = 0;
        this.rotation_speed = 0;
        this.grazed = false;
    }

    public void update() {
        if (!is_active) return;
        pos_x += vel_x;
        pos_y += vel_y;

        // Deactivate if far outside play field boundaries
        if (pos_x < 270 || pos_x > 1010 || pos_y < -50 || pos_y > 770) {
            is_active = false;
        }
    }
}
