import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Font;
import java.util.List;

public class Enemy_Entity {
    public double pos_x;
    public double pos_y;
    public double vel_x;
    public double vel_y;
    public double radius;
    public int health;
    public int max_health;
    public boolean is_active;
    public boolean is_boss;
    public String boss_name;
    public int score_value;

    protected int shoot_cooldown = 0;
    protected int pattern_timer = 0;
    public int boss_phase = 1;

    public Enemy_Entity(double x, double y, double r, int hp, boolean boss, String name, int score) {
        this.pos_x = x;
        this.pos_y = y;
        this.radius = r;
        this.health = hp;
        this.max_health = hp;
        this.is_active = true;
        this.is_boss = boss;
        this.boss_name = name;
        this.score_value = score;
    }

    public void update_enemy(Bullet_Pool pool, double player_x, double player_y, List<Stage_Manager.Boss_Spell> spells) {
        if (!is_active) return;
        pos_x += vel_x;
        pos_y += vel_y;
        pattern_timer++;

        // Bound checking for non-bosses (clamped to widescreen play field)
        if (!is_boss) {
            if (pos_y > 750 || pos_y < -150 || pos_x < 250 || pos_x > 1030) {
                is_active = false;
            }
        }

        if (shoot_cooldown > 0) {
            shoot_cooldown--;
        }

        execute_patterns(pool, player_x, player_y, spells);
    }

    public void take_damage(int amount) {
        if (!is_active) return;
        health -= amount;
        if (health <= 0) {
            is_active = false;
        }
    }

    protected void execute_patterns(Bullet_Pool pool, double player_x, double player_y, List<Stage_Manager.Boss_Spell> spells) {
        if (is_boss) {
            execute_boss_patterns(pool, player_x, player_y, spells);
            return;
        }

        // Simple default enemy: drift down, shoot straight down at player location sometimes
        if (shoot_cooldown == 0) {
            pool.acquire_bullet(pos_x, pos_y, 0, 4, 6, 1, 1, Color.YELLOW);
            shoot_cooldown = 60 + (int)(Math.random() * 60);
        }
    }

    protected void execute_boss_patterns(Bullet_Pool pool, double player_x, double player_y, List<Stage_Manager.Boss_Spell> spells) {
        // Threshold check for boss phase transitions
        if (health < max_health * 0.4) {
            boss_phase = 3;
        } else if (health < max_health * 0.7) {
            boss_phase = 2;
        }

        // Try to execute custom spell from stage file first
        Stage_Manager.Boss_Spell active_spell = null;
        for (Stage_Manager.Boss_Spell s : spells) {
            if (s.phase == boss_phase) {
                active_spell = s;
                break;
            }
        }

        if (active_spell != null) {
            // Apply standard sweep movement
            vel_x = Math.sin(pattern_timer * 0.03) * 2.5;
            vel_y = (pos_y < 150) ? 1.0 : 0;

            if (shoot_cooldown == 0) {
                String pattern = active_spell.pattern_type;
                Color col = active_spell.color;
                double spd = active_spell.bullet_speed;

                if (pattern.equals("spiral")) {
                    int arms = 4;
                    for (int a = 0; a < arms; a++) {
                        double angle = (pattern_timer * 0.08) + (a * Math.PI / 2);
                        pool.acquire_bullet(pos_x, pos_y, Math.cos(angle) * spd, Math.sin(angle) * spd, 7, 1, 1, col);
                    }
                } else if (pattern.equals("ring_spread")) {
                    int num = 18;
                    for (int i = 0; i < num; i++) {
                        double angle = (2 * Math.PI / num) * i + (pattern_timer * 0.04);
                        pool.acquire_bullet(pos_x, pos_y, Math.cos(angle) * spd, Math.sin(angle) * spd, 8, 1, 1, col);
                    }
                } else if (pattern.equals("concentric_circles")) {
                    int num = 24;
                    for (int i = 0; i < num; i++) {
                        double angle = (2 * Math.PI / num) * i;
                        pool.acquire_bullet(pos_x, pos_y, Math.cos(angle) * spd, Math.sin(angle) * spd, 7, 1, 1, col);
                        pool.acquire_bullet(pos_x, pos_y, Math.cos(angle) * (spd * 1.5), Math.sin(angle) * (spd * 1.5), 7, 1, 1, col);
                    }
                } else if (pattern.equals("chaos_bloom")) {
                    double angle = Math.random() * Math.PI * 2;
                    pool.acquire_bullet(pos_x, pos_y, Math.cos(angle) * spd, Math.sin(angle) * spd, 8, 1, 1, col);
                }
                shoot_cooldown = active_spell.shoot_cooldown;
            }
            return;
        }

        // Fallback hardcoded defaults if no spells configured
        if (boss_name.equals("Victoria Koura")) {
            if (boss_phase == 1) {
                vel_x = Math.sin(pattern_timer * 0.02) * 2;
                vel_y = (pos_y < 150) ? 1.0 : 0;
                if (shoot_cooldown == 0) {
                    int num_bullets = 16;
                    for (int i = 0; i < num_bullets; i++) {
                        double angle = (2 * Math.PI / num_bullets) * i + (pattern_timer * 0.05);
                        pool.acquire_bullet(pos_x, pos_y, Math.cos(angle) * 3, Math.sin(angle) * 3, 8, 1, 1, Color.RED);
                    }
                    shoot_cooldown = 35;
                }
            } else {
                vel_x = Math.sin(pattern_timer * 0.04) * 3;
                vel_y = (pos_y > 150) ? -1.0 : (pos_y < 120 ? 1.0 : 0);
                if (shoot_cooldown == 0) {
                    double angle_center = Math.atan2(player_y - pos_y, player_x - pos_x);
                    int num_bullets = 8;
                    for (int i = 0; i < num_bullets; i++) {
                        double angle = angle_center - 0.5 + (i * 1.0 / num_bullets);
                        pool.acquire_bullet(pos_x, pos_y, Math.cos(angle) * 4, Math.sin(angle) * 4, 6, 1, 1, Color.ORANGE);
                    }
                    pool.acquire_bullet(pos_x, pos_y, Math.cos(angle_center) * 8, Math.sin(angle_center) * 8, 12, 1, 1, Color.WHITE);
                    shoot_cooldown = 20;
                }
            }
        } else if (boss_name.equals("Queen Fenria & Xelisa")) {
            vel_x = Math.cos(pattern_timer * 0.03) * 2.5;
            vel_y = (pos_y < 150) ? 1.0 : 0;
            if (boss_phase == 1) {
                if (shoot_cooldown == 0) {
                    int arms = 4;
                    for (int a = 0; a < arms; a++) {
                        double angle = (pattern_timer * 0.08) + (a * Math.PI / 2);
                        pool.acquire_bullet(pos_x, pos_y, Math.cos(angle) * 3.5, Math.sin(angle) * 3.5, 7, 1, 1, new Color(138, 43, 226));
                    }
                    shoot_cooldown = 10;
                }
            } else {
                if (shoot_cooldown == 0) {
                    int num_bullets = 24;
                    for (int i = 0; i < num_bullets; i++) {
                        double angle = (2 * Math.PI / num_bullets) * i;
                        pool.acquire_bullet(pos_x, pos_y, Math.cos(angle) * 2.5, Math.sin(angle) * 2.5, 8, 1, 1, Color.MAGENTA);
                    }
                    shoot_cooldown = 45;
                }
            }
        } else if (boss_name.equals("Goddess Cyria")) {
            vel_x = Math.sin(pattern_timer * 0.05) * 3.5;
            vel_y = (pos_y < 160) ? 1.5 : 0;
            if (boss_phase == 1) {
                if (shoot_cooldown == 0) {
                    for (int d = -2; d <= 2; d++) {
                        double angle = Math.PI / 2 + d * 0.3 + Math.sin(pattern_timer * 0.1) * 0.2;
                        pool.acquire_bullet(pos_x, pos_y, Math.cos(angle) * 4.5, Math.sin(angle) * 4.5, 6, 1, 1, Color.CYAN);
                    }
                    shoot_cooldown = 8;
                }
            } else {
                if (shoot_cooldown == 0) {
                    int num_bullets = 30;
                    for (int i = 0; i < num_bullets; i++) {
                        double angle = (2 * Math.PI / num_bullets) * i;
                        pool.acquire_bullet(pos_x, pos_y, Math.cos(angle) * 2.0, Math.sin(angle) * 2.0, 7, 1, 1, Color.PINK);
                        pool.acquire_bullet(pos_x, pos_y, Math.cos(angle) * 4.0, Math.sin(angle) * 4.0, 7, 1, 1, Color.BLUE);
                    }
                    shoot_cooldown = 50;
                }
            }
        }
    }

    public void draw_enemy(Graphics2D g2d) {
        if (!is_active) return;

        if (is_boss) {
            g2d.setColor(Color.RED);
            g2d.fillOval((int)(pos_x - radius), (int)(pos_y - radius), (int)(radius * 2), (int)(radius * 2));
            g2d.setColor(Color.WHITE);
            g2d.drawOval((int)(pos_x - radius), (int)(pos_y - radius), (int)(radius * 2), (int)(radius * 2));

            // Draw Boss Health Bar
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillRect(50, 40, 700, 15);
            g2d.setColor(Color.RED);
            int bar_width = (int)(700.0 * health / max_health);
            g2d.fillRect(50, 40, bar_width, 15);
            g2d.setColor(Color.WHITE);
            g2d.drawRect(50, 40, 700, 15);

            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            g2d.drawString(boss_name + " (Phase " + boss_phase + ")", 50, 35);
        } else {
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillOval((int)(pos_x - radius), (int)(pos_y - radius), (int)(radius * 2), (int)(radius * 2));
            g2d.setColor(Color.DARK_GRAY);
            g2d.drawOval((int)(pos_x - radius), (int)(pos_y - radius), (int)(radius * 2), (int)(radius * 2));
        }
    }
}
