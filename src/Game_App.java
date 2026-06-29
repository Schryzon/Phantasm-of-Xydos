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

    private static final int width = 800;
    private static final int height = 800;

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
                g.setColor(new Color(15, 15, 30));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setLayout(new BorderLayout());

        JLabel header = new JLabel("SELECT COMBAT PROTOCOL", JLabel.CENTER);
        header.setFont(new Font("Consolas", Font.BOLD, 32));
        header.setForeground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(40, 20, 20, 20));
        panel.add(header, BorderLayout.NORTH);

        JPanel chars_box = new JPanel(new GridLayout(1, 2, 40, 0));
        chars_box.setOpaque(false);
        chars_box.setBorder(BorderFactory.createEmptyBorder(20, 50, 40, 50));

        JPanel hist_card = create_char_card("HISTORIA KOURA", "Vessel of Thunder (Lagta)",
                new String[]{
                        "Primary: Straight Lightning bolts",
                        "Close-up: Automatic Spear slash",
                        "Spell: Lagtanis Karvista (homing spears & invulnerability)",
                        "Hitbox: 4.0px"
                }, new Color(180, 40, 40));
        
        JPanel mira_card = create_char_card("MIRA KOURA", "Empathy Wind Weaver (Daiki)",
                new String[]{
                        "Primary: Homing/Spread/Straight wind blades",
                        "Spell: Daiki's Sanctuary (bullet wipe + boss damage)",
                        "Hitbox: 2.5px (highly agile)",
                        "Power: High utility"
                }, new Color(40, 180, 40));

        JButton select_hist = create_styled_button("INITIALIZE HISTORIA");
        select_hist.addActionListener(e -> start_new_game(1));
        hist_card.add(select_hist, BorderLayout.SOUTH);

        JButton select_mira = create_styled_button("INITIALIZE MIRA");
        select_mira.addActionListener(e -> start_new_game(2));
        mira_card.add(select_mira, BorderLayout.SOUTH);

        chars_box.add(hist_card);
        chars_box.add(mira_card);
        panel.add(chars_box, BorderLayout.CENTER);

        JButton back_btn = create_styled_button("RETURN TO MAIN SYSTEM");
        back_btn.addActionListener(e -> card_layout.show(main_container, "menu"));
        JPanel footer_panel = new JPanel();
        footer_panel.setOpaque(false);
        footer_panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 40, 10));
        footer_panel.add(back_btn);
        panel.add(footer_panel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel create_char_card(String name, String title, String[] details, Color border_color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(25, 25, 45));
        card.setBorder(BorderFactory.createLineBorder(border_color, 3));

        JPanel details_panel = new JPanel();
        details_panel.setOpaque(false);
        details_panel.setLayout(new BoxLayout(details_panel, BoxLayout.Y_AXIS));
        details_panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel name_lbl = new JLabel(name);
        name_lbl.setFont(new Font("Consolas", Font.BOLD, 22));
        name_lbl.setForeground(border_color);
        name_lbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title_lbl = new JLabel(title);
        title_lbl.setFont(new Font("Consolas", Font.ITALIC, 14));
        title_lbl.setForeground(Color.LIGHT_GRAY);
        title_lbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        details_panel.add(name_lbl);
        details_panel.add(Box.createRigidArea(new Dimension(0, 10)));
        details_panel.add(title_lbl);
        details_panel.add(Box.createRigidArea(new Dimension(0, 25)));

        for (String detail : details) {
            JLabel d_lbl = new JLabel("• " + detail);
            d_lbl.setFont(new Font("Consolas", Font.PLAIN, 12));
            d_lbl.setForeground(Color.WHITE);
            d_lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            details_panel.add(d_lbl);
            details_panel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        card.add(details_panel, BorderLayout.CENTER);
        return card;
    }

    private JPanel create_high_scores_panel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(10, 10, 20));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setLayout(new BorderLayout());

        JLabel header = new JLabel("HIGH ARCHIVES", JLabel.CENTER);
        header.setFont(new Font("Consolas", Font.BOLD, 36));
        header.setForeground(new Color(0, 206, 209));
        header.setBorder(BorderFactory.createEmptyBorder(40, 20, 20, 20));
        panel.add(header, BorderLayout.NORTH);

        JPanel list_panel = new JPanel();
        list_panel.setOpaque(false);
        list_panel.setLayout(new BoxLayout(list_panel, BoxLayout.Y_AXIS));
        list_panel.setBorder(BorderFactory.createEmptyBorder(20, 100, 20, 100));

        List<Database_Connector.High_Score_Entry> entries = Database_Connector.get_top_scores(10);
        if (entries.isEmpty()) {
            JLabel none = new JLabel("No records found in databases.", JLabel.CENTER);
            none.setFont(new Font("Consolas", Font.PLAIN, 16));
            none.setForeground(Color.WHITE);
            none.setAlignmentX(Component.CENTER_ALIGNMENT);
            list_panel.add(none);
        } else {
            for (int i = 0; i < entries.size(); i++) {
                Database_Connector.High_Score_Entry entry = entries.get(i);
                String label_text = String.format("%02d. %-15s [%-10s] %08d pts - %s",
                        i + 1, entry.player_name, entry.character_name, entry.score, entry.date_time);
                JLabel score_lbl = new JLabel(label_text);
                score_lbl.setFont(new Font("Monospaced", Font.PLAIN, 14));
                score_lbl.setForeground(Color.WHITE);
                score_lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
                list_panel.add(score_lbl);
                list_panel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }

        panel.add(list_panel, BorderLayout.CENTER);

        JButton back_btn = create_styled_button("RETURN TO MAIN SYSTEM");
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
        settings_grid.setBorder(BorderFactory.createEmptyBorder(20, 80, 20, 80));

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

        // Keybind controls (simple rebind selectors)
        JLabel rebind_lbl = new JLabel("KEYBOARD REBINDS:");
        rebind_lbl.setFont(new Font("Consolas", Font.BOLD, 16));
        rebind_lbl.setForeground(Color.WHITE);

        JButton rebind_btn = new JButton("CONFIGURE KEYS");
        rebind_btn.setFont(new Font("Consolas", Font.BOLD, 14));
        rebind_btn.setForeground(Color.WHITE);
        rebind_btn.setBackground(new Color(40, 40, 80));
        rebind_btn.addActionListener(e -> open_rebind_dialog());

        // Controller status info
        JLabel ctrl_lbl = new JLabel("GAMEPAD SUPPORT:");
        ctrl_lbl.setFont(new Font("Consolas", Font.BOLD, 16));
        ctrl_lbl.setForeground(Color.WHITE);
        JLabel ctrl_status = new JLabel("Jamepad active. Connect USB gamepad.");
        ctrl_status.setFont(new Font("Consolas", Font.ITALIC, 14));
        ctrl_status.setForeground(new Color(0, 255, 127));

        settings_grid.add(sfx_lbl);
        settings_grid.add(sfx_slider);
        settings_grid.add(music_lbl);
        settings_grid.add(music_slider);
        settings_grid.add(fps_lbl);
        settings_grid.add(fps_box);
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
        key_btn.setFont(new Font("Consolas", Font.BOLD, 12));
        key_btn.setBackground(new Color(35, 35, 60));
        key_btn.setForeground(Color.CYAN);

        key_btn.addActionListener(e -> {
            key_btn.setText("PRESS ANY KEY...");
            key_btn.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent ke) {
                    setter.set(ke.getKeyCode());
                    key_btn.setText(KeyEvent.getKeyText(ke.getKeyCode()));
                    key_btn.removeKeyListener(this);
                }
            });
        });

        dialog.add(action_lbl);
        dialog.add(key_btn);
    }

    private JButton create_styled_button(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Consolas", Font.BOLD, 16));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(30, 30, 60));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(new Color(0, 206, 209), 2));
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
                if (engine.game_over) {
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
        String p_name = JOptionPane.showInputDialog(this, "Mission Failure. Enter codename to upload archives:", "ARCHIVE SYNC", JOptionPane.PLAIN_MESSAGE);
        if (p_name != null && !p_name.trim().isEmpty()) {
            String char_name = selected_character == 1 ? "Historia" : "Mira";
            Database_Connector.save_score(p_name.trim(), char_name, engine.score);
        }
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

                // Render HUD
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Consolas", Font.BOLD, 16));
                g2d.drawString("SCORE: " + String.format("%08d", engine.score), 20, 30);
                
                String char_name = selected_character == 1 ? "HISTORIA" : "MIRA";
                g2d.drawString("PROTOCOL: " + char_name, 220, 30);
                
                g2d.drawString("STAGE: " + engine.stage_manager.current_stage, 440, 30);

                // Show Xydos (Lives)
                g2d.drawString("XYDOS (L): ", 20, 780);
                g2d.setColor(Color.GREEN);
                for (int i = 0; i < engine.player.xydos_count; i++) {
                    g2d.fillOval(120 + i * 20, 768, 12, 12);
                }

                // Show Spells
                g2d.setColor(Color.WHITE);
                g2d.drawString("SPELLS (B): ", 320, 780);
                g2d.setColor(Color.CYAN);
                for (int i = 0; i < engine.player.spell_count; i++) {
                    g2d.fillRect(430 + i * 20, 768, 12, 12);
                }

                // Show Power
                g2d.setColor(Color.WHITE);
                g2d.drawString("POWER: " + engine.player.power_level + "/4", 620, 780);

                // Draw Dialogue Overlay if active
                if (engine.stage_manager.is_in_dialogue && engine.stage_manager.current_dialogue != null) {
                    Stage_Manager.Stage_Event dialogue = engine.stage_manager.current_dialogue;

                    // Apply visual shakiness if configured (vibration offset)
                    int shake_x = 0;
                    int shake_y = 0;
                    if (dialogue.shakiness > 0 && !engine.stage_manager.is_dialogue_finished) {
                        shake_x = (int) ((Math.random() - 0.5) * dialogue.shakiness * 2);
                        shake_y = (int) ((Math.random() - 0.5) * dialogue.shakiness * 2);
                    }

                    int box_x = 50 + shake_x;
                    int box_y = 520 + shake_y;
                    int box_w = 700;
                    int box_h = 160;

                    // Draw dialogue container box (Undertale-style)
                    g2d.setColor(Color.BLACK);
                    g2d.fillRect(box_x, box_y, box_w, box_h);
                    g2d.setColor(Color.WHITE);
                    g2d.setStroke(new java.awt.BasicStroke(3));
                    g2d.drawRect(box_x, box_y, box_w, box_h);

                    // Dynamically load portrait
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

                    // Render portrait if found
                    if (portrait != null) {
                        g2d.drawImage(portrait, box_x + 20, box_y + 20, 120, 120, null);
                    }

                    int text_offset_x = portrait != null ? 160 : 30;
                    int line_w = portrait != null ? 510 : 640;

                    // Draw Speaker name
                    g2d.setFont(new Font("Consolas", Font.BOLD, 20));
                    g2d.setColor(new Color(0, 206, 209));
                    g2d.drawString(dialogue.speaker, box_x + text_offset_x, box_y + 40);

                    // Draw typed dialogue text
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

                    // Draw skip/advance guide
                    g2d.setFont(new Font("Consolas", Font.ITALIC, 11));
                    g2d.setColor(Color.GRAY);
                    g2d.drawString("[Z] ADVANCE  |  [SPACE] SKIP CUTSCENE", box_x + box_w - 240, box_y + box_h - 15);
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
