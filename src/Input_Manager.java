import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Input_Manager extends KeyAdapter {
    public boolean up = false;
    public boolean down = false;
    public boolean left = false;
    public boolean right = false;
    public boolean shoot = false;
    public boolean spell = false;
    public boolean focus = false;

    @Override
    public void keyPressed(KeyEvent e) {
        handle_key(e.getKeyCode(), true);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        handle_key(e.getKeyCode(), false);
    }

    private void handle_key(int key_code, boolean pressed) {
        switch (key_code) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                up = pressed;
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                down = pressed;
                break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                left = pressed;
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                right = pressed;
                break;
            case KeyEvent.VK_Z:
                shoot = pressed;
                break;
            case KeyEvent.VK_X:
                spell = pressed;
                break;
            case KeyEvent.VK_SHIFT:
                focus = pressed;
                break;
        }
    }

    public void clear() {
        up = false;
        down = false;
        left = false;
        right = false;
        shoot = false;
        spell = false;
        focus = false;
    }
}
