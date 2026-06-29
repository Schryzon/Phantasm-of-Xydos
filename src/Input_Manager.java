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
    public boolean skip = false;

    // Pause triggers
    public boolean pause_pressed = false;
    public boolean pause_triggered = false;
    private boolean prev_pause_pressed = false;

    // Controller polling manager (polled dynamically if loaded)
    private Object controller_manager = null;
    private boolean controller_initialized = false;

    public Input_Manager() {
        init_controller();
    }

    private void init_controller() {
        try {
            Class<?> manager_class = Class.forName("com.studiohartman.jamepad.ControllerManager");
            controller_manager = manager_class.getDeclaredConstructor().newInstance();
            manager_class.getMethod("initSDLGamepad").invoke(controller_manager);
            controller_initialized = true;
            System.out.println("[INFO] Controller system initialized via Jamepad.");
        } catch (Throwable e) {
            System.out.println("[INFO] Gamepad support disabled or Jamepad library unavailable: " + e.getMessage());
        }
    }

    public void poll_controller() {
        if (!controller_initialized || controller_manager == null) {
            return;
        }
        try {
            Class<?> manager_class = controller_manager.getClass();
            manager_class.getMethod("update").invoke(controller_manager);

            Object state = manager_class.getMethod("getState", int.class).invoke(controller_manager, 0);
            Class<?> state_class = state.getClass();

            boolean is_connected = (boolean) state_class.getField("isConnected").get(state);
            if (is_connected) {
                up = (boolean) state_class.getField("dpadUp").get(state) || (float) state_class.getField("leftStickY").get(state) > 0.4f;
                down = (boolean) state_class.getField("dpadDown").get(state) || (float) state_class.getField("leftStickY").get(state) < -0.4f;
                left = (boolean) state_class.getField("dpadLeft").get(state) || (float) state_class.getField("leftStickX").get(state) < -0.4f;
                right = (boolean) state_class.getField("dpadRight").get(state) || (float) state_class.getField("leftStickX").get(state) > 0.4f;
                shoot = (boolean) state_class.getField("a").get(state);
                spell = (boolean) state_class.getField("b").get(state) || (boolean) state_class.getField("x").get(state);
                focus = (float) state_class.getField("rightTrigger").get(state) > 0.2f || (boolean) state_class.getField("rb").get(state);
                
                // Track start button
                pause_pressed = (boolean) state_class.getField("start").get(state);
            }
        } catch (Exception e) {
            // fail silently
        }
    }

    public void update_pause_state() {
        pause_triggered = pause_pressed && !prev_pause_pressed;
        prev_pause_pressed = pause_pressed;
    }

    public void shutdown_controller() {
        if (controller_initialized && controller_manager != null) {
            try {
                controller_manager.getClass().getMethod("quitSDLGamepad").invoke(controller_manager);
            } catch (Exception ignored) {}
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        handle_key(e.getKeyCode(), true);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        handle_key(e.getKeyCode(), false);
    }

    private void handle_key(int key_code, boolean pressed) {
        if (key_code == KeyEvent.VK_SPACE) {
            skip = pressed;
        } else if (key_code == KeyEvent.VK_ESCAPE) {
            pause_pressed = pressed;
        } else if (key_code == Config_Manager.key_up) {
            up = pressed;
        } else if (key_code == Config_Manager.key_down) {
            down = pressed;
        } else if (key_code == Config_Manager.key_left) {
            left = pressed;
        } else if (key_code == Config_Manager.key_right) {
            right = pressed;
        } else if (key_code == Config_Manager.key_shoot) {
            shoot = pressed;
        } else if (key_code == Config_Manager.key_spell) {
            spell = pressed;
        } else if (key_code == Config_Manager.key_focus) {
            focus = pressed;
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
        skip = false;
        pause_pressed = false;
        pause_triggered = false;
        prev_pause_pressed = false;
    }
}
