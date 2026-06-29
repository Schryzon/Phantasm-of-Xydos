import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Database_Connector {
    private static final String db_url = "jdbc:mysql://localhost:3306/phantasm_xydos?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String db_user = "root";
    private static final String db_pass = "";
    private static boolean use_mysql = false;

    public static class High_Score_Entry {
        public String player_name;
        public String character_name;
        public int score;
        public String date_time;

        public High_Score_Entry(String player_name, String character_name, int score, String date_time) {
            this.player_name = player_name;
            this.character_name = character_name;
            this.score = score;
            this.date_time = date_time;
        }
    }

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Attempt short-lived connection check
            try (Connection conn = DriverManager.getConnection(db_url, db_user, db_pass)) {
                use_mysql = true;
                // Initialize database schema
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS phantasm_xydos");
                    stmt.executeUpdate("USE phantasm_xydos");
                    stmt.executeUpdate("CREATE TABLE IF NOT EXISTS high_scores (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY," +
                            "player_name VARCHAR(50)," +
                            "character_name VARCHAR(50)," +
                            "score INT," +
                            "date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                            ")");
                }
            }
        } catch (Exception e) {
            System.out.println("MySQL database connection failed. Falling back to local file system: " + e.getMessage());
            use_mysql = false;
        }
    }

    public static void save_score(String player_name, String character_name, int score) {
        if (use_mysql) {
            try (Connection conn = DriverManager.getConnection(db_url, db_user, db_pass)) {
                String sql = "INSERT INTO high_scores (player_name, character_name, score) VALUES (?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, player_name);
                    pstmt.setString(2, character_name);
                    pstmt.setInt(3, score);
                    pstmt.executeUpdate();
                    return;
                }
            } catch (Exception e) {
                System.out.println("MySQL database save failed: " + e.getMessage() + ". Falling back to local file.");
            }
        }
        save_score_local(player_name, character_name, score);
    }

    public static List<High_Score_Entry> get_top_scores(int limit) {
        List<High_Score_Entry> scores = new ArrayList<>();
        if (use_mysql) {
            try (Connection conn = DriverManager.getConnection(db_url, db_user, db_pass)) {
                String sql = "SELECT player_name, character_name, score, date_time FROM high_scores ORDER BY score DESC LIMIT ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, limit);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            scores.add(new High_Score_Entry(
                                    rs.getString("player_name"),
                                    rs.getString("character_name"),
                                    rs.getInt("score"),
                                    rs.getString("date_time")
                            ));
                        }
                        return scores;
                    }
                }
            } catch (Exception e) {
                System.out.println("MySQL database load failed: " + e.getMessage() + ". Falling back to local file.");
            }
        }
        return get_top_scores_local(limit);
    }

    private static void save_score_local(String player_name, String character_name, int score) {
        File file = new File("local_scores.txt");
        try (FileWriter writer = new FileWriter(file, true)) {
            String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
            writer.write(player_name + "," + character_name + "," + score + "," + timestamp + "\n");
        } catch (IOException e) {
            System.err.println("failed to write local scores: " + e.getMessage());
        }
    }

    private static List<High_Score_Entry> get_top_scores_local(int limit) {
        List<High_Score_Entry> scores = new ArrayList<>();
        File file = new File("local_scores.txt");
        if (!file.exists()) {
            return scores;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    try {
                        String p_name = parts[0];
                        String c_name = parts[1];
                        int p_score = Integer.parseInt(parts[2]);
                        String ts = parts[3];
                        scores.add(new High_Score_Entry(p_name, c_name, p_score, ts));
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (IOException e) {
            System.err.println("failed to read local scores: " + e.getMessage());
        }

        Collections.sort(scores, new Comparator<High_Score_Entry>() {
            @Override
            public int compare(High_Score_Entry o1, High_Score_Entry o2) {
                return Integer.compare(o2.score, o1.score);
            }
        });

        if (scores.size() > limit) {
            return scores.subList(0, limit);
        }
        return scores;
    }
}
