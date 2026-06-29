import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

        add(main_container);
        card_layout.show(main_container, "menu");
    }

    private JPanel create_menu_panel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Paint beautiful dark starry background
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
        gbc.insets = new Insets(15, 15, 15, 15);

        JLabel title_label = new JLabel("PHANTASM OF XYDOS", JLabel.CENTER);
        title_label.setFont(new Font("Consolas", Font.BOLD, 46));
        title_label.setForeground(new Color(0, 206, 209));

        JLabel subtitle_label = new JLabel("Andromeda I - Ashes of Divinity", JLabel.CENTER);
        subtitle_label.setFont(new Font("Consolas", Font.ITALIC, 20));
        subtitle_label.setForeground(new Color(138, 43, 226));

        JButton play_button = create_styled_button("START OPERATION");
        JButton scores_button = create_styled_button("HIGH ARCHIVES");
        JButton exit_button = create_styled_button("SYSTEM SHUTDOWN");

        play_button.addActionListener(e -> card_layout.show(main_container, "char_select"));
        scores_button.addActionListener(e -> {
            main_container.add(create_high_scores_panel(), "scores");
            card_layout.show(main_container, "scores");
        });
        exit_button.addActionListener(e -> System.exit(0));

        panel.add(title_label, gbc);
        panel.add(subtitle_label, gbc);
        gbc.insets = new Insets(30, 15, 10, 15);
        panel.add(play_button, gbc);
        gbc.insets = new Insets(10, 15, 10, 15);
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

        // Historia Select Card
        JPanel hist_card = create_char_card("HISTORIA KOURA", "Vessel of Thunder (Lagta)",
                new String[]{
                        "Primary: Straight Lightning bolts",
                        "Close-up: Automatic Spear slash",
                        "Spell: Lagtanis Karvista (invulnerability & homing spears)",
                        "Hitbox: 4.0px"
                }, new Color(180, 40, 40));
        
        // Mira Select Card
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

        // Switch panel to gameplay
        card_layout.show(main_container, "gameplay");
        
        // Grab focus
        Component current_comp = main_container.getComponent(2);
        current_comp.requestFocusInWindow();

        if (game_timer != null && game_timer.isRunning()) {
            game_timer.stop();
        }

        game_timer = new Timer(16, new ActionListener() {
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
