import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

public class Game_App extends JFrame {
    private final CardLayout card_layout;
    private final JPanel main_container;

    private static final int width = 1280;
    private static final int height = 720;

    private Input_Manager input_manager;
    private Game_Engine engine;
    private Timer game_timer;
    private int selected_character = 1; // 1 = Historia, 2 = Mira

    public Game_App() {
        setTitle("Phantasm of Xydos: Andromeda I");
        setSize(width, height);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Load config
        Config_Manager.load_config("config.ini");

        // Set global fullscreen key dispatcher
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_F11) {
                    Config_Manager.fullscreen = !Config_Manager.fullscreen;
                    apply_fullscreen_state();
                    Config_Manager.save_config("config.ini");
                    return true;
                }
                return false;
            }
        });

        // Apply loaded fullscreen setting
        if (Config_Manager.fullscreen) {
            apply_fullscreen_state();
        }

        card_layout = new CardLayout();
        main_container = new JPanel(card_layout);

        input_manager = new Input_Manager();

        // Register panels
        main_container.add(create_menu_panel(), "menu");
        main_container.add(create_char_select_panel(), "char_select");
        main_container.add(new Game_Canvas(), "gameplay");
        main_container.add(create_high_scores_panel(), "scores");
        main_container.add(create_settings_panel(), "settings");

        add(main_container);
        card_layout.show(main_container, "menu");
    }

    private void apply_fullscreen_state() {
        dispose(); // Terminate window temporarily to change frame decorations
        if (Config_Manager.fullscreen) {
            setUndecorated(true);
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        } else {
            setUndecorated(false);
            setExtendedState(JFrame.NORMAL);
            setSize(width, height);
            setLocationRelativeTo(null);
        }
        setVisible(true);
    }

    private JPanel create_menu_panel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(10, 10, 25));
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(Color.WHITE);
                for (int i = 0; i < 50; i++) {
                    int x = (int)(Math.random() * getWidth());
                    int y = (int)(Math.random() * getHeight());
                    g.fillOval(x, y, 2, 2);
                }
            }
        };
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 15, 10, 15);

        JLabel title_label = new JLabel("PHANTASM OF XYDOS", JLabel.CENTER);
        title_label.setFont(new Font("Consolas", Font.BOLD, 46));
        title_label.setForeground(new Color(0, 206, 209));

        JLabel subtitle_label = new JLabel("Andromeda I - Ashes of Divinity", JLabel.CENTER);
        subtitle_label.setFont(new Font("Consolas", Font.ITALIC, 20));
        subtitle_label.setForeground(new Color(138, 43, 226));

        JButton play_button = create_styled_button("START OPERATION");
        JButton settings_button = create_styled_button("SYSTEM SETTINGS");
        JButton scores_button = create_styled_button("HIGH ARCHIVES");
        JButton exit_button = create_styled_button("SYSTEM SHUTDOWN");

        play_button.addActionListener(e -> card_layout.show(main_container, "char_select"));
        settings_button.addActionListener(e -> card_layout.show(main_container, "settings"));
        scores_button.addActionListener(e -> {
            main_container.add(create_high_scores_panel(), "scores");
            card_layout.show(main_container, "scores");
        });
        exit_button.addActionListener(e -> {
            input_manager.shutdown_controller();
            System.exit(0);
        });

        panel.add(title_label, gbc);
        panel.add(subtitle_label, gbc);
        gbc.insets = new Insets(20, 15, 8, 15);
        panel.add(play_button, gbc);
        gbc.insets = new Insets(8, 15, 8, 15);
        panel.add(settings_button, gbc);
        panel.add(scores_button, gbc);
        panel.add(exit_button, gbc);

        return panel;
    }

    private JPanel create_char_select_panel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(10, 10, 25));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setLayout(new BorderLayout());

        JLabel header = new JLabel("SELECT PILOT PROTOCOL", JLabel.CENTER);
        header.setFont(new Font("Consolas", Font.BOLD, 32));
        header.setForeground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(30, 10, 10, 10));
        panel.add(header, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(1, 2, 40, 20));
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(20, 100, 20, 100));

        // Historia card
        JPanel hist_card = new JPanel(new BorderLayout());
        hist_card.setBackground(new Color(30, 15, 15));
        hist_card.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
        JTextArea hist_desc = new JTextArea("\n HISTORIA KOURA\n Vessel of Thunder\n\n - Hitbox: 4.0px\n - Lightning fire (CAS-8 straight lines)\n - Auto melee spear slash (Double Strike)\n - Spell: Lagtanis Karvista\n   (Throws boss-seeking giant spears;\n    does not clear screen bullets;\n    grants invulnerability)");
        hist_desc.setFont(new Font("Consolas", Font.PLAIN, 15));
        hist_desc.setForeground(Color.WHITE);
        hist_desc.setEditable(false);
        hist_desc.setOpaque(false);
        JButton select_hist = create_styled_button("ACTIVATE HISTORIA");
        select_hist.addActionListener(e -> start_new_game(1));
        hist_card.add(hist_desc, BorderLayout.CENTER);
        hist_card.add(select_hist, BorderLayout.SOUTH);

        // Mira card
        JPanel mira_card = new JPanel(new BorderLayout());
        mira_card.setBackground(new Color(15, 30, 15));
        mira_card.setBorder(BorderFactory.createLineBorder(Color.GREEN, 2));
        JTextArea mira_desc = new JTextArea("\n MIRA KOURA\n Empathy Wind Weaver\n\n - Hitbox: 2.5px\n - Homing, spread, and straight wind currents\n - Spell: Daiki's Sanctuary\n   (Invulnerable, clears all bullets,\n    damages active bosses on screen)");
        mira_desc.setFont(new Font("Consolas", Font.PLAIN, 15));
        mira_desc.setForeground(Color.WHITE);
        mira_desc.setEditable(false);
        mira_desc.setOpaque(false);
        JButton select_mira = create_styled_button("ACTIVATE MIRA");
        select_mira.addActionListener(e -> start_new_game(2));
        mira_card.add(mira_desc, BorderLayout.CENTER);
        mira_card.add(select_mira, BorderLayout.SOUTH);

        grid.add(hist_card);
        grid.add(mira_card);
        panel.add(grid, BorderLayout.CENTER);

        JButton back_btn = create_styled_button("RETURN TO COMMAND");
        back_btn.addActionListener(e -> card_layout.show(main_container, "menu"));
        JPanel footer_panel = new JPanel();
        footer_panel.setOpaque(false);
        footer_panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 40, 10));
        footer_panel.add(back_btn);
        panel.add(footer_panel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel create_settings_panel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(15, 15, 25));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setLayout(new BorderLayout());

        JLabel header = new JLabel("SYSTEM SETTINGS", JLabel.CENTER);
        header.setFont(new Font("Consolas", Font.BOLD, 32));
        header.setForeground(new Color(0, 206, 209));
        header.setBorder(BorderFactory.createEmptyBorder(30, 20, 10, 20));
        panel.add(header, BorderLayout.NORTH);

        JPanel settings_grid = new JPanel(new GridLayout(6, 2, 20, 15));
        settings_grid.setOpaque(false);
        settings_grid.setBorder(BorderFactory.createEmptyBorder(20, 200, 20, 200));

        // SFX Volume Control
        JLabel sfx_lbl = new JLabel("SFX VOLUME: " + Config_Manager.sfx_volume);
        sfx_lbl.setFont(new Font("Consolas", Font.BOLD, 16));
        sfx_lbl.setForeground(Color.WHITE);
        JSlider sfx_slider = new JSlider(0, 100, Config_Manager.sfx_volume);
        sfx_slider.setOpaque(false);
        sfx_slider.addChangeListener(e -> {
            Config_Manager.sfx_volume = sfx_slider.getValue();
            sfx_lbl.setText("SFX VOLUME: " + Config_Manager.sfx_volume);
        });

        // Music Volume Control
        JLabel music_lbl = new JLabel("MUSIC VOLUME: " + Config_Manager.music_volume);
        music_lbl.setFont(new Font("Consolas", Font.BOLD, 16));
        music_lbl.setForeground(Color.WHITE);
        JSlider music_slider = new JSlider(0, 100, Config_Manager.music_volume);
        music_slider.setOpaque(false);
        music_slider.addChangeListener(e -> {
            Config_Manager.music_volume = music_slider.getValue();
            music_lbl.setText("MUSIC VOLUME: " + Config_Manager.music_volume);
            Sound_Player.update_music_volume();
        });

        // FPS Limit Control
        JLabel fps_lbl = new JLabel("FPS CAP TARGET:");
        fps_lbl.setFont(new Font("Consolas", Font.BOLD, 16));
        fps_lbl.setForeground(Color.WHITE);
        String[] fps_options = {"30", "60", "120", "Unlimited"};
        JComboBox<String> fps_box = new JComboBox<>(fps_options);
        if (Config_Manager.fps_limit == 30) fps_box.setSelectedIndex(0);
        else if (Config_Manager.fps_limit == 60) fps_box.setSelectedIndex(1);
        else if (Config_Manager.fps_limit == 120) fps_box.setSelectedIndex(2);
        else fps_box.setSelectedIndex(3);
        fps_box.addActionListener(e -> {
            String selected = (String) fps_box.getSelectedItem();
            if (selected.equals("Unlimited")) {
                Config_Manager.fps_limit = 1000;
            } else {
                Config_Manager.fps_limit = Integer.parseInt(selected);
            }
        });

        // Fullscreen Control
        JLabel fs_lbl = new JLabel("DISPLAY FULLSCREEN:");
        fs_lbl.setFont(new Font("Consolas", Font.BOLD, 16));
        fs_lbl.setForeground(Color.WHITE);
        JCheckBox fs_chk = new JCheckBox("", Config_Manager.fullscreen);
        fs_chk.setOpaque(false);
        fs_chk.addActionListener(e -> {
            Config_Manager.fullscreen = fs_chk.isSelected();
            apply_fullscreen_state();
        });

        // Keyboard Rebind Button
        JLabel rebind_lbl = new JLabel("KEYBOARD REBINDS:");
        rebind_lbl.setFont(new Font("Consolas", Font.BOLD, 16));
        rebind_lbl.setForeground(Color.WHITE);
        JButton rebind_btn = new JButton("CONFIGURE KEYS");
        rebind_btn.setFont(new Font("Consolas", Font.BOLD, 14));
        rebind_btn.setForeground(Color.WHITE);
        rebind_btn.setBackground(new Color(40, 40, 80));
        rebind_btn.addActionListener(e -> open_rebind_dialog());

        // Gamepad Info
        JLabel ctrl_lbl = new JLabel("GAMEPAD STATUS:");
        ctrl_lbl.setFont(new Font("Consolas", Font.BOLD, 16));
        ctrl_lbl.setForeground(Color.WHITE);
        JLabel ctrl_status = new JLabel("SDL Gamepad Driver Active.");
        ctrl_status.setFont(new Font("Consolas", Font.ITALIC, 14));
        ctrl_status.setForeground(new Color(0, 255, 127));

        settings_grid.add(sfx_lbl);
        settings_grid.add(sfx_slider);
        settings_grid.add(music_lbl);
        settings_grid.add(music_slider);
        settings_grid.add(fps_lbl);
        settings_grid.add(fps_box);
        settings_grid.add(fs_lbl);
        settings_grid.add(fs_chk);
        settings_grid.add(rebind_lbl);
        settings_grid.add(rebind_btn);
        settings_grid.add(ctrl_lbl);
        settings_grid.add(ctrl_status);

        panel.add(settings_grid, BorderLayout.CENTER);

        JButton save_btn = create_styled_button("SAVE AND RETURN");
        save_btn.addActionListener(e -> {
            Config_Manager.save_config("config.ini");
            card_layout.show(main_container, "menu");
        });
        JPanel footer_panel = new JPanel();
        footer_panel.setOpaque(false);
        footer_panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 40, 10));
        footer_panel.add(save_btn);
        panel.add(footer_panel, BorderLayout.SOUTH);

        return panel;
    }

    private void open_rebind_dialog() {
        JDialog dialog = new JDialog(this, "KEYBOARD CONFIGURATION", true);
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(8, 2, 10, 10));
        dialog.getContentPane().setBackground(new Color(20, 20, 35));

        add_rebind_row(dialog, "MOVE UP", () -> Config_Manager.key_up, val -> Config_Manager.key_up = val);
        add_rebind_row(dialog, "MOVE DOWN", () -> Config_Manager.key_down, val -> Config_Manager.key_down = val);
        add_rebind_row(dialog, "MOVE LEFT", () -> Config_Manager.key_left, val -> Config_Manager.key_left = val);
        add_rebind_row(dialog, "MOVE RIGHT", () -> Config_Manager.key_right, val -> Config_Manager.key_right = val);
        add_rebind_row(dialog, "WEAPON SHOOT", () -> Config_Manager.key_shoot, val -> Config_Manager.key_shoot = val);
        add_rebind_row(dialog, "CAST SPELL", () -> Config_Manager.key_spell, val -> Config_Manager.key_spell = val);
        add_rebind_row(dialog, "FOCUS MODE", () -> Config_Manager.key_focus, val -> Config_Manager.key_focus = val);

        JButton done_btn = new JButton("DONE");
        done_btn.setFont(new Font("Consolas", Font.BOLD, 14));
        done_btn.addActionListener(e -> dialog.dispose());
        dialog.add(new JLabel(""));
        dialog.add(done_btn);
        dialog.setVisible(true);
    }

    private interface Key_Getter { int get(); }
    private interface Key_Setter { void set(int val); }

    private void add_rebind_row(JDialog dialog, String action_name, Key_Getter getter, Key_Setter setter) {
        JLabel action_lbl = new JLabel("  " + action_name, JLabel.LEFT);
        action_lbl.setFont(new Font("Consolas", Font.BOLD, 14));
        action_lbl.setForeground(Color.WHITE);

        JButton key_btn = new JButton(KeyEvent.getKeyText(getter.get()));
        key_btn.setFont(new Font("Consolas", Font.PLAIN, 12));
        key_btn.addActionListener(e -> {
            key_btn.setText("PRESS ANY KEY...");
            key_btn.requestFocusInWindow();
            key_btn.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent evt) {
                    setter.set(evt.getKeyCode());
                    key_btn.setText(KeyEvent.getKeyText(evt.getKeyCode()));
                    key_btn.removeKeyListener(this);
                }
            });
        });

        dialog.add(action_lbl);
        dialog.add(key_btn);
    }

    private JPanel create_high_scores_panel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(10, 10, 25));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setLayout(new BorderLayout());

        JLabel header = new JLabel("HIGH SCORE ARCHIVES", JLabel.CENTER);
        header.setFont(new Font("Consolas", Font.BOLD, 32));
        header.setForeground(new Color(138, 43, 226));
        header.setBorder(BorderFactory.createEmptyBorder(30, 20, 10, 20));
        panel.add(header, BorderLayout.NORTH);

        List<Database_Connector.High_Score_Entry> list_data = Database_Connector.get_top_scores(10);
        DefaultListModel<String> model = new DefaultListModel<>();
        for (Database_Connector.High_Score_Entry entry : list_data) {
            model.addElement(String.format("  %-15s %-12s %09d", entry.player_name, entry.character_name, entry.score));
        }

        JList<String> list = new JList<>(model);
        list.setFont(new Font("Consolas", Font.PLAIN, 16));
        list.setForeground(Color.WHITE);
        list.setBackground(new Color(20, 20, 45));
        JScrollPane scroll = new JScrollPane(list);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder(20, 150, 20, 150));
        panel.add(scroll, BorderLayout.CENTER);

        JButton back_btn = create_styled_button("RETURN TO MAIN PANEL");
        back_btn.addActionListener(e -> card_layout.show(main_container, "menu"));
        JPanel footer_panel = new JPanel();
        footer_panel.setOpaque(false);
        footer_panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 40, 10));
        footer_panel.add(back_btn);
        panel.add(footer_panel, BorderLayout.SOUTH);

        return panel;
    }

    private JButton create_styled_button(String label) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("Consolas", Font.BOLD, 15));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(30, 30, 60));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(Color.CYAN, 1));
        btn.setPreferredSize(new Dimension(280, 45));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(0, 206, 209));
                btn.setForeground(Color.BLACK);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(30, 30, 60));
                btn.setForeground(Color.WHITE);
            }
        });
        return btn;
    }

    private void start_new_game(int char_choice) {
        selected_character = char_choice;
        input_manager.clear();
        engine = new Game_Engine(selected_character, input_manager);

        card_layout.show(main_container, "gameplay");

        Component current_comp = main_container.getComponent(2);
        current_comp.requestFocusInWindow();

        if (game_timer != null && game_timer.isRunning()) {
            game_timer.stop();
        }

        // Calculate custom frame delay
        int timer_delay = 16; // default 60 FPS
        if (Config_Manager.fps_limit > 0) {
            timer_delay = 1000 / Config_Manager.fps_limit;
            if (timer_delay < 1) timer_delay = 1;
        }

        game_timer = new Timer(timer_delay, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (engine.return_to_title_requested) {
                    game_timer.stop();
                    Sound_Player.stop_music();
                    card_layout.show(main_container, "menu");
                } else if (engine.game_over) {
                    game_timer.stop();
                    handle_game_over();
                } else if (engine.game_win) {
                    game_timer.stop();
                    handle_game_win();
                } else {
                    engine.update_game();
                    current_comp.repaint();
                }
            }
        });
        game_timer.start();
    }

    private void handle_game_over() {
        JOptionPane.showMessageDialog(this, "DESTRUCTION DETECTED. Protocol Aborted.", "XYDOS FAILSAFE TRIGGERED", JOptionPane.ERROR_MESSAGE);
        card_layout.show(main_container, "menu");
    }

    private void handle_game_win() {
        String p_name = JOptionPane.showInputDialog(this, "Divine Conquest Complete! Enter codename:", "SYSTEM RESTORED", JOptionPane.PLAIN_MESSAGE);
        if (p_name != null && !p_name.trim().isEmpty()) {
            String char_name = selected_character == 1 ? "Historia" : "Mira";
            Database_Connector.save_score(p_name.trim(), char_name, engine.score);
        }
        card_layout.show(main_container, "menu");
    }

    private class Game_Canvas extends JPanel {
        private final java.util.Map<String, BufferedImage> portrait_cache = new java.util.HashMap<>();

        public Game_Canvas() {
            setFocusable(true);
            addKeyListener(input_manager);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (engine != null) {
                Graphics2D g2d = (Graphics2D) g;
                engine.draw_game(g2d);

                // ==========================================
                // 1. Render Left Sidebar Column (0 to 320 px)
                // ==========================================
                g2d.setColor(new Color(15, 10, 25));
                g2d.fillRect(0, 0, 320, 720);

                // Draw Vertical text representation (Stage Name & Title)
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Consolas", Font.BOLD, 22));
                g2d.drawString("STAGE " + engine.stage_manager.current_stage, 40, 80);

                g2d.setFont(new Font("Consolas", Font.ITALIC, 14));
                g2d.setColor(new Color(0, 206, 209));
                String stg_name = engine.stage_manager.stage_name != null ? engine.stage_manager.stage_name : "UNKNOWN SECTOR";
                g2d.drawString(stg_name, 40, 110);

                // Simple gate vertical borders for aesthetics
                g2d.setColor(new Color(138, 43, 226, 80));
                g2d.setStroke(new java.awt.BasicStroke(2));
                g2d.drawRect(20, 150, 280, 500);
                g2d.drawLine(20, 250, 300, 250);
                g2d.drawLine(20, 350, 300, 350);
                g2d.drawLine(20, 450, 300, 450);
                g2d.drawLine(20, 550, 300, 550);

                // Rotated banner
                java.awt.geom.AffineTransform orig = g2d.getTransform();
                g2d.translate(80, 480);
                g2d.rotate(-Math.PI / 2);
                g2d.setFont(new Font("Consolas", Font.BOLD, 28));
                g2d.setColor(new Color(255, 255, 255, 100));
                g2d.drawString("SCARLET BORDER", 0, 0);
                g2d.setTransform(orig);

                // ==========================================
                // 2. Render Golden Play Area Borders (320 to 960 px)
                // ==========================================
                g2d.setColor(new Color(212, 175, 55)); // Gold border
                g2d.setStroke(new java.awt.BasicStroke(3));
                g2d.drawRect(320, 0, 640, 720);

                // ==========================================
                // 3. Render Boss Details HUD (Top of Play Field)
                // ==========================================
                Enemy_Entity active_boss = null;
                for (Enemy_Entity e : engine.enemies) {
                    if (e.is_active && e.is_boss) {
                        active_boss = e;
                        break;
                    }
                }
                if (active_boss != null) {
                    // Boss Name
                    g2d.setFont(new Font("Consolas", Font.BOLD, 15));
                    g2d.setColor(Color.RED);
                    g2d.drawString(active_boss.boss_name, 350, 40);

                    // Health Bar background
                    g2d.setColor(Color.DARK_GRAY);
                    g2d.fillRect(350, 50, 580, 12);

                    // Health Bar fill
                    double hp_pct = (double) active_boss.health / active_boss.max_health;
                    g2d.setColor(Color.RED);
                    g2d.fillRect(350, 50, (int)(580 * hp_pct), 12);

                    g2d.setColor(Color.WHITE);
                    g2d.setStroke(new java.awt.BasicStroke(1));
                    g2d.drawRect(350, 50, 580, 12);
                }

                // ==========================================
                // 4. Render Right Sidebar Column (960 to 1280 px)
                // ==========================================
                g2d.setColor(new Color(15, 10, 25));
                g2d.fillRect(960, 0, 320, 720);

                // Challenge Mode subtitle
                g2d.setFont(new Font("Consolas", Font.BOLD, 12));
                g2d.setColor(Color.GRAY);
                g2d.drawString("〈 Challenge Mode 〉", 1000, 50);

                g2d.setFont(new Font("Consolas", Font.BOLD, 22));
                g2d.setColor(new Color(138, 43, 226));
                g2d.drawString("NORMAL", 1000, 80);

                // Scores
                g2d.setFont(new Font("Consolas", Font.PLAIN, 18));
                g2d.setColor(Color.WHITE);
                g2d.drawString("Hi Score  999999990", 1000, 130);
                g2d.drawString("Score     " + String.format("%09d", engine.score), 1000, 160);

                // Lives (Hearts)
                g2d.drawString("Player   ", 1000, 210);
                g2d.setColor(Color.RED);
                for (int i = 0; i < engine.player.xydos_count; i++) {
                    g2d.drawString("♥", 1100 + i * 20, 210);
                }

                // Spells (Stars)
                g2d.setColor(Color.WHITE);
                g2d.drawString("Spell    ", 1000, 250);
                g2d.setColor(Color.GREEN);
                for (int i = 0; i < engine.player.spell_count; i++) {
                    g2d.drawString("★", 1100 + i * 20, 250);
                }

                // Power Level (Gauge bar)
                g2d.setColor(Color.WHITE);
                g2d.drawString("Power    ", 1000, 290);
                g2d.setColor(Color.DARK_GRAY);
                g2d.fillRect(1100, 278, 120, 14);
                
                double power_val = engine.player.power_level;
                g2d.setColor(Color.MAGENTA);
                g2d.fillRect(1100, 278, (int)(120 * (power_val / 4.0)), 14);
                g2d.setColor(Color.WHITE);
                g2d.drawRect(1100, 278, 120, 14);
                
                g2d.setFont(new Font("Consolas", Font.BOLD, 10));
                String p_text = power_val >= 4.0 ? "MAX" : String.format("%.1f", power_val);
                g2d.drawString(p_text, 1145, 289);

                // Graze
                g2d.setFont(new Font("Consolas", Font.PLAIN, 18));
                g2d.drawString("Graze    ", 1000, 330);
                int graze_bonus = engine.player.get_graze_damage_bonus();
                g2d.drawString(String.format("%03d (+%d)", (int)(engine.player.bravery_gauge * 2.16), graze_bonus), 1100, 330);

                // Bravery Gauge (Gold/Orange progress bar)
                g2d.drawString("Bravery  ", 1000, 370);
                g2d.setColor(Color.DARK_GRAY);
                g2d.fillRect(1100, 358, 120, 14);

                double bravery_pct = engine.player.bravery_gauge;
                Color bravery_color = new Color(255, (int)(255 - (bravery_pct * 1.55)), 0);
                g2d.setColor(bravery_color);
                g2d.fillRect(1100, 358, (int)(120 * (bravery_pct / 100.0)), 14);
                g2d.setColor(Color.WHITE);
                g2d.drawRect(1100, 358, 120, 14);

                g2d.setFont(new Font("Consolas", Font.BOLD, 10));
                g2d.drawString((int)bravery_pct + "%", 1145, 369);

                // Phantasm Logo at the bottom
                g2d.setFont(new Font("Consolas", Font.BOLD, 24));
                g2d.setColor(new Color(138, 43, 226, 80));
                g2d.drawString("XYDOS ANDROMEDA", 1000, 600);
                g2d.setFont(new Font("Consolas", Font.ITALIC, 11));
                g2d.drawString("DEVELOPER SYSTEM RESTORE v2.1", 1000, 620);

                // ==========================================
                // 5. Render Dialogue Overlay (within Play Area)
                // ==========================================
                if (engine.stage_manager.is_in_dialogue && engine.stage_manager.current_dialogue != null) {
                    Stage_Manager.Stage_Event dialogue = engine.stage_manager.current_dialogue;

                    int shake_x = 0;
                    int shake_y = 0;
                    if (dialogue.shakiness > 0 && !engine.stage_manager.is_dialogue_finished) {
                        shake_x = (int) ((Math.random() - 0.5) * dialogue.shakiness * 2);
                        shake_y = (int) ((Math.random() - 0.5) * dialogue.shakiness * 2);
                    }

                    int box_x = 340 + shake_x;
                    int box_y = 520 + shake_y;
                    int box_w = 600;
                    int box_h = 160;

                    // Draw dialogue box
                    g2d.setColor(Color.BLACK);
                    g2d.fillRect(box_x, box_y, box_w, box_h);
                    g2d.setColor(Color.WHITE);
                    g2d.setStroke(new java.awt.BasicStroke(3));
                    g2d.drawRect(box_x, box_y, box_w, box_h);

                    // Portrait Load
                    BufferedImage portrait = null;
                    if (dialogue.expression != null && !dialogue.expression.isEmpty()) {
                        String port_key = dialogue.speaker + "_" + dialogue.expression;
                        if (portrait_cache.containsKey(port_key)) {
                            portrait = portrait_cache.get(port_key);
                        } else {
                            try {
                                File port_file = new File("assets/portraits/" + dialogue.speaker + "_" + dialogue.expression + ".png");
                                if (port_file.exists()) {
                                    portrait = javax.imageio.ImageIO.read(port_file);
                                    portrait_cache.put(port_key, portrait);
                                } else {
                                    portrait_cache.put(port_key, null);
                                }
                            } catch (Exception e) {
                                portrait_cache.put(port_key, null);
                            }
                        }
                    }

                    // Render portrait
                    if (portrait != null) {
                        g2d.drawImage(portrait, box_x + 20, box_y + 20, 120, 120, null);
                    }

                    int text_offset_x = portrait != null ? 160 : 30;
                    int line_w = portrait != null ? 410 : 540;

                    // Draw Speaker name
                    g2d.setFont(new Font("Consolas", Font.BOLD, 20));
                    g2d.setColor(new Color(0, 206, 209));
                    g2d.drawString(dialogue.speaker, box_x + text_offset_x, box_y + 40);

                    // Draw typed text
                    g2d.setFont(new Font("Consolas", Font.PLAIN, 16));
                    g2d.setColor(Color.WHITE);
                    
                    String typed_text = dialogue.text.substring(0, engine.stage_manager.dialogue_char_index);
                    int text_y = box_y + 80;
                    FontMetrics fm = g2d.getFontMetrics();
                    String[] words = typed_text.split(" ");
                    StringBuilder current_line = new StringBuilder();
                    
                    for (String word : words) {
                        if (fm.stringWidth(current_line.toString() + word) < line_w) {
                            current_line.append(word).append(" ");
                        } else {
                            g2d.drawString(current_line.toString(), box_x + text_offset_x, text_y);
                            text_y += 24;
                            current_line = new StringBuilder(word + " ");
                        }
                    }
                    g2d.drawString(current_line.toString(), box_x + text_offset_x, text_y);

                    // skip guide
                    g2d.setFont(new Font("Consolas", Font.ITALIC, 11));
                    g2d.setColor(Color.GRAY);
                    g2d.drawString("[Z] ADVANCE  |  [SPACE] SKIP", box_x + box_w - 200, box_y + box_h - 15);
                }

                // ==========================================
                // 6. Draw Pause Overlay Menus
                // ==========================================
                if (engine.is_paused) {
                    g2d.setColor(new Color(0, 0, 0, 180));
                    g2d.fillRect(320, 0, 640, 720);

                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Consolas", Font.BOLD, 36));
                    String pause_txt = "GAME PAUSED";
                    FontMetrics fm = g2d.getFontMetrics();
                    g2d.drawString(pause_txt, 320 + (640 - fm.stringWidth(pause_txt)) / 2, 280);

                    g2d.setFont(new Font("Consolas", Font.PLAIN, 20));
                    
                    // Option 0: Resume
                    String opt0 = "RESUME OPERATION";
                    if (engine.pause_selection == 0) {
                        g2d.setColor(Color.CYAN);
                        g2d.drawString("> " + opt0 + " <", 320 + (640 - g2d.getFontMetrics().stringWidth("> " + opt0 + " <")) / 2, 360);
                    } else {
                        g2d.setColor(Color.GRAY);
                        g2d.drawString(opt0, 320 + (640 - g2d.getFontMetrics().stringWidth(opt0)) / 2, 360);
                    }

                    // Option 1: Return to Title
                    String opt1 = "RETURN TO COMMAND DECK";
                    if (engine.pause_selection == 1) {
                        g2d.setColor(Color.RED);
                        g2d.drawString("> " + opt1 + " <", 320 + (640 - g2d.getFontMetrics().stringWidth("> " + opt1 + " <")) / 2, 410);
                    } else {
                        g2d.setColor(Color.GRAY);
                        g2d.drawString(opt1, 320 + (640 - g2d.getFontMetrics().stringWidth(opt1)) / 2, 410);
                    }

                    g2d.setFont(new Font("Consolas", Font.ITALIC, 13));
                    g2d.setColor(Color.WHITE);
                    String help_txt = "[UP / DOWN] NAVIGATE  |  [Z] SELECT";
                    fm = g2d.getFontMetrics();
                    g2d.drawString(help_txt, 320 + (640 - fm.stringWidth(help_txt)) / 2, 480);
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Game_App app = new Game_App();
            app.setVisible(true);
        });
    }
}
