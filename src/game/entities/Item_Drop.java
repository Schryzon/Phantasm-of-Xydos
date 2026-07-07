package game.entities;

public class Item_Drop {
    public double pos_x;
    public double pos_y;
    public double vel_x = 0;
    public double vel_y = 1.5; // slow drift down
    public int type; // 1 = Power (Red 'P'), 2 = Score (Blue 'S')
    public boolean is_active = true;
    public boolean is_magnetized = false;

    public Item_Drop(double x, double y, int type) {
        this.pos_x = x;
        this.pos_y = y;
        this.type = type;
    }

    public void update(double player_x, double player_y) {
        if (!is_active) return;

        if (is_magnetized) {
            // Fly directly towards the player
            double dx = player_x - pos_x;
            double dy = player_y - pos_y;
            double dist = Math.hypot(dx, dy);
            if (dist > 2) {
                double speed = 10.0;
                vel_x = (dx / dist) * speed;
                vel_y = (dy / dist) * speed;
            } else {
                vel_x = 0;
                vel_y = 0;
            }
        } else {
            // Apply slight horizontal centering friction, drift down
            vel_x *= 0.95;
            vel_y = 1.5;
        }

        pos_x += vel_x;
        pos_y += vel_y;

        // Deactivate if it falls off bottom screen
        if (pos_y > 730) {
            is_active = false;
        }
    }
}
