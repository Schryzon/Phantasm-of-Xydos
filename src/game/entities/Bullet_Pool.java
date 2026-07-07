package game.entities;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Bullet_Pool {
    private final Bullet_Entity[] pool;
    private int next_available_index = 0;
    private final int capacity;

    public Bullet_Pool(int capacity) {
        this.capacity = capacity;
        this.pool = new Bullet_Entity[capacity];
        for (int i = 0; i < capacity; i++) {
            pool[i] = new Bullet_Entity();
        }
    }

    public Bullet_Entity acquire_bullet(double x, double y, double vx, double vy, double r, int type, int dmg, Color c) {
        // Look for the next inactive bullet starting from next_available_index
        for (int i = 0; i < capacity; i++) {
            int index = (next_available_index + i) % capacity;
            if (!pool[index].is_active) {
                pool[index].init(x, y, vx, vy, r, type, dmg, c);
                next_available_index = (index + 1) % capacity;
                return pool[index];
            }
        }
        // Fallback: recycle oldest (overwrite)
        Bullet_Entity recycled = pool[next_available_index];
        recycled.init(x, y, vx, vy, r, type, dmg, c);
        next_available_index = (next_available_index + 1) % capacity;
        return recycled;
    }

    public List<Bullet_Entity> get_active_bullets() {
        List<Bullet_Entity> active = new ArrayList<>();
        for (int i = 0; i < capacity; i++) {
            if (pool[i].is_active) {
                active.add(pool[i]);
            }
        }
        return active;
    }

    public void clear() {
        for (int i = 0; i < capacity; i++) {
            pool[i].is_active = false;
        }
        next_available_index = 0;
    }
}
