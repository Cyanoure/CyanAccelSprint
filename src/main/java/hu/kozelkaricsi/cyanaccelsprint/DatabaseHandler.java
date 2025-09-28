package hu.kozelkaricsi.cyanaccelsprint;

import java.sql.*;
import java.util.UUID;

public class DatabaseHandler {
    private Connection connection;
    private Main plugin;

    public DatabaseHandler(Main plugin) {
        this.plugin = plugin;
        this.init();
    }

    public void init() {
        try {
            String databasePath = plugin.getDataFolder() + "/data.db";
            connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
            try (PreparedStatement stmt = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS players (uuid TEXT PRIMARY KEY, sprint_enabled BOOLEAN DEFAULT NULL, last_seen INTEGER NOT NULL DEFAULT (strftime('%s','now')))"
            )) {
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updatePlayerTimestamp(UUID uuid) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "UPDATE players SET last_seen=(strftime('%s','now')) WHERE uuid=?"
        )) {
            stmt.setString(1, uuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setPlayerSprintState(UUID uuid, boolean newState) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT OR REPLACE INTO players (uuid, last_seen, sprint_enabled) VALUES (?, (strftime('%s','now')), ?)"
        )) {
            stmt.setString(1, uuid.toString());
            stmt.setBoolean(2, newState);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean getPlayerSprintState(UUID uuid) {
        boolean state = plugin.config.getBoolean("enable-by-default", true);

        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT sprint_enabled FROM players WHERE uuid=?"
        )) {
            stmt.setString(1, uuid.toString());
            ResultSet result = stmt.executeQuery();
            if (result != null && result.next()) {
                state = result.getBoolean(1);
                result.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return state;
    }
}
