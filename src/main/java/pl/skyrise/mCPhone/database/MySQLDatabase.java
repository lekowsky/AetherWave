package pl.skyrise.mCPhone.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import pl.skyrise.mCPhone.MCPhone;
import pl.skyrise.mCPhone.api.PhoneNotification;
import pl.skyrise.mCPhone.api.SMSMessage;
import pl.skyrise.mCPhone.config.ConfigManager;
import pl.skyrise.mCPhone.models.Contact;
import pl.skyrise.mCPhone.models.PhoneUser;

import java.sql.*;
import java.util.*;

/**
 * Implementacja bazy danych MySQL
 */
public class MySQLDatabase implements Database {

    private final MCPhone plugin;
    private HikariDataSource dataSource;
    private String tablePrefix;

    public MySQLDatabase(MCPhone plugin) {
        this.plugin = plugin;
    }

    @Override
    public void init() {
        ConfigManager config = plugin.getConfigManager();
        tablePrefix = config.getMysqlTablePrefix();

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:mysql://" + config.getMysqlHost() + ":" + config.getMysqlPort() + "/" + config.getMysqlDatabase());
        hikariConfig.setUsername(config.getMysqlUsername());
        hikariConfig.setPassword(config.getMysqlPassword());
        hikariConfig.setMaximumPoolSize(config.getMysqlPoolSize());
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        dataSource = new HikariDataSource(hikariConfig);
        createTables();
    }

    private void createTables() {
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            // Tabela użytkowników
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + tablePrefix + "users (" +
                "uuid VARCHAR(36) PRIMARY KEY," +
                "phone_number VARCHAR(20)," +
                "sim_type VARCHAR(50)," +
                "sim_active BOOLEAN DEFAULT FALSE," +
                "daily_sms_count INT DEFAULT 0," +
                "last_sms_reset BIGINT," +
                "installed_apps TEXT" +
                ")");

            // Tabela kontaktów
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + tablePrefix + "contacts (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "owner_uuid VARCHAR(36)," +
                "name VARCHAR(100)," +
                "phone_number VARCHAR(20)," +
                "player_uuid VARCHAR(36)," +
                "FOREIGN KEY (owner_uuid) REFERENCES " + tablePrefix + "users(uuid) ON DELETE CASCADE" +
                ")");

            // Tabela wiadomości SMS
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + tablePrefix + "messages (" +
                "id VARCHAR(36) PRIMARY KEY," +
                "sender_uuid VARCHAR(36)," +
                "receiver_uuid VARCHAR(36)," +
                "message TEXT," +
                "timestamp BIGINT," +
                "is_read BOOLEAN DEFAULT FALSE" +
                ")");

            // Tabela powiadomień
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + tablePrefix + "notifications (" +
                "id VARCHAR(36) PRIMARY KEY," +
                "player_uuid VARCHAR(36)," +
                "app_id VARCHAR(50)," +
                "title VARCHAR(100)," +
                "message TEXT," +
                "timestamp BIGINT," +
                "is_read BOOLEAN DEFAULT FALSE" +
                ")");

            // Indeksy
            try {
                stmt.executeUpdate("CREATE INDEX idx_phone_number ON " + tablePrefix + "users(phone_number)");
            } catch (SQLException ignored) {}
            try {
                stmt.executeUpdate("CREATE INDEX idx_messages_sender ON " + tablePrefix + "messages(sender_uuid)");
                stmt.executeUpdate("CREATE INDEX idx_messages_receiver ON " + tablePrefix + "messages(receiver_uuid)");
            } catch (SQLException ignored) {}

        } catch (SQLException e) {
            plugin.getLogger().severe("Błąd podczas tworzenia tabel MySQL: " + e.getMessage());
        }
    }

    @Override
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    @Override
    public void saveAll() {
        // W MySQL dane są zapisywane na bieżąco
    }

    // ============================================
    // Użytkownicy
    // ============================================

    @Override
    public PhoneUser loadUser(UUID uuid) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT * FROM " + tablePrefix + "users WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String phoneNumber = rs.getString("phone_number");
                String simType = rs.getString("sim_type");
                boolean simActive = rs.getBoolean("sim_active");
                int dailySmsCount = rs.getInt("daily_sms_count");
                long lastSmsReset = rs.getLong("last_sms_reset");
                String appsStr = rs.getString("installed_apps");
                Set<String> installedApps = appsStr != null && !appsStr.isEmpty() 
                    ? new HashSet<>(Arrays.asList(appsStr.split(","))) 
                    : new HashSet<>();

                List<Contact> contacts = loadContacts(conn, uuid);

                return new PhoneUser(uuid, phoneNumber, simType, simActive, dailySmsCount, lastSmsReset, installedApps, contacts);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Błąd podczas ładowania użytkownika: " + e.getMessage());
        }
        return null;
    }

    private List<Contact> loadContacts(Connection conn, UUID ownerUuid) throws SQLException {
        List<Contact> contacts = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
            "SELECT * FROM " + tablePrefix + "contacts WHERE owner_uuid = ?")) {
            ps.setString(1, ownerUuid.toString());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String name = rs.getString("name");
                String phoneNumber = rs.getString("phone_number");
                String playerUuidStr = rs.getString("player_uuid");
                UUID playerUuid = playerUuidStr != null ? UUID.fromString(playerUuidStr) : null;
                contacts.add(new Contact(name, phoneNumber, playerUuid));
            }
        }
        return contacts;
    }

    @Override
    public void saveUser(PhoneUser user) {
        try (Connection conn = dataSource.getConnection()) {
            // Zapisz użytkownika
            try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO " + tablePrefix + "users (uuid, phone_number, sim_type, sim_active, daily_sms_count, last_sms_reset, installed_apps) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE " +
                "phone_number = VALUES(phone_number), sim_type = VALUES(sim_type), sim_active = VALUES(sim_active), " +
                "daily_sms_count = VALUES(daily_sms_count), last_sms_reset = VALUES(last_sms_reset), installed_apps = VALUES(installed_apps)")) {
                ps.setString(1, user.getUuid().toString());
                ps.setString(2, user.getPhoneNumber());
                ps.setString(3, user.getSimType());
                ps.setBoolean(4, user.isSimActive());
                ps.setInt(5, user.getDailySmsCount());
                ps.setLong(6, user.getLastSmsReset());
                ps.setString(7, String.join(",", user.getInstalledApps()));
                ps.executeUpdate();
            }

            // Usuń stare kontakty i dodaj nowe
            try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM " + tablePrefix + "contacts WHERE owner_uuid = ?")) {
                ps.setString(1, user.getUuid().toString());
                ps.executeUpdate();
            }

            for (Contact contact : user.getContacts()) {
                try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO " + tablePrefix + "contacts (owner_uuid, name, phone_number, player_uuid) VALUES (?, ?, ?, ?)")) {
                    ps.setString(1, user.getUuid().toString());
                    ps.setString(2, contact.getName());
                    ps.setString(3, contact.getPhoneNumber());
                    ps.setString(4, contact.getPlayerUUID() != null ? contact.getPlayerUUID().toString() : null);
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Błąd podczas zapisywania użytkownika: " + e.getMessage());
        }
    }

    @Override
    public List<PhoneUser> loadAllUsers() {
        List<PhoneUser> users = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT uuid FROM " + tablePrefix + "users")) {
            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                PhoneUser user = loadUser(uuid);
                if (user != null) {
                    users.add(user);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Błąd podczas ładowania wszystkich użytkowników: " + e.getMessage());
        }
        return users;
    }

    @Override
    public UUID getPlayerByNumber(String phoneNumber) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT uuid FROM " + tablePrefix + "users WHERE phone_number = ?")) {
            ps.setString(1, phoneNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return UUID.fromString(rs.getString("uuid"));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Błąd podczas wyszukiwania gracza po numerze: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean isNumberTaken(String phoneNumber) {
        return getPlayerByNumber(phoneNumber) != null;
    }

    // ============================================
    // Wiadomości SMS
    // ============================================

    @Override
    public void saveSMS(SMSMessage message) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "INSERT INTO " + tablePrefix + "messages (id, sender_uuid, receiver_uuid, message, timestamp, is_read) VALUES (?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, message.getId());
            ps.setString(2, message.getSender().toString());
            ps.setString(3, message.getReceiver().toString());
            ps.setString(4, message.getMessage());
            ps.setLong(5, message.getTimestamp());
            ps.setBoolean(6, message.isRead());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Błąd podczas zapisywania SMS: " + e.getMessage());
        }
    }

    @Override
    public List<SMSMessage> loadConversation(UUID player1, UUID player2, int limit) {
        List<SMSMessage> messages = new ArrayList<>();
        String sql = "SELECT * FROM " + tablePrefix + "messages WHERE " +
            "(sender_uuid = ? AND receiver_uuid = ?) OR (sender_uuid = ? AND receiver_uuid = ?) " +
            "ORDER BY timestamp DESC LIMIT ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, player1.toString());
            ps.setString(2, player2.toString());
            ps.setString(3, player2.toString());
            ps.setString(4, player1.toString());
            ps.setInt(5, limit);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                messages.add(new SMSMessage(
                    rs.getString("id"),
                    UUID.fromString(rs.getString("sender_uuid")),
                    UUID.fromString(rs.getString("receiver_uuid")),
                    rs.getString("message"),
                    rs.getLong("timestamp"),
                    rs.getBoolean("is_read")
                ));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Błąd podczas ładowania konwersacji: " + e.getMessage());
        }
        return messages;
    }

    @Override
    public List<UUID> getConversationPartners(UUID player) {
        Set<UUID> partners = new HashSet<>();
        String sql = "SELECT DISTINCT CASE WHEN sender_uuid = ? THEN receiver_uuid ELSE sender_uuid END as partner " +
            "FROM " + tablePrefix + "messages WHERE sender_uuid = ? OR receiver_uuid = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, player.toString());
            ps.setString(2, player.toString());
            ps.setString(3, player.toString());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                partners.add(UUID.fromString(rs.getString("partner")));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Błąd podczas pobierania partnerów konwersacji: " + e.getMessage());
        }
        return new ArrayList<>(partners);
    }

    @Override
    public SMSMessage getLastMessage(UUID player1, UUID player2) {
        List<SMSMessage> messages = loadConversation(player1, player2, 1);
        return messages.isEmpty() ? null : messages.get(0);
    }

    @Override
    public int getUnreadCount(UUID player) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT COUNT(*) FROM " + tablePrefix + "messages WHERE receiver_uuid = ? AND is_read = FALSE")) {
            ps.setString(1, player.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Błąd podczas liczenia nieprzeczytanych: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public void markAsRead(UUID player, UUID sender) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "UPDATE " + tablePrefix + "messages SET is_read = TRUE WHERE receiver_uuid = ? AND sender_uuid = ?")) {
            ps.setString(1, player.toString());
            ps.setString(2, sender.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Błąd podczas oznaczania jako przeczytane: " + e.getMessage());
        }
    }

    // ============================================
    // Powiadomienia
    // ============================================

    @Override
    public void saveNotification(UUID player, PhoneNotification notification) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "INSERT INTO " + tablePrefix + "notifications (id, player_uuid, app_id, title, message, timestamp, is_read) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, notification.getId());
            ps.setString(2, player.toString());
            ps.setString(3, notification.getAppId());
            ps.setString(4, notification.getTitle());
            ps.setString(5, notification.getMessage());
            ps.setLong(6, notification.getTimestamp());
            ps.setBoolean(7, notification.isRead());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Błąd podczas zapisywania powiadomienia: " + e.getMessage());
        }
    }

    @Override
    public List<PhoneNotification> loadNotifications(UUID player) {
        List<PhoneNotification> notifications = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT * FROM " + tablePrefix + "notifications WHERE player_uuid = ? ORDER BY timestamp DESC")) {
            ps.setString(1, player.toString());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                notifications.add(new PhoneNotification(
                    rs.getString("id"),
                    rs.getString("app_id"),
                    rs.getString("title"),
                    rs.getString("message"),
                    rs.getLong("timestamp"),
                    rs.getBoolean("is_read")
                ));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Błąd podczas ładowania powiadomień: " + e.getMessage());
        }
        return notifications;
    }

    @Override
    public void clearNotifications(UUID player) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "DELETE FROM " + tablePrefix + "notifications WHERE player_uuid = ?")) {
            ps.setString(1, player.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Błąd podczas czyszczenia powiadomień: " + e.getMessage());
        }
    }

    @Override
    public void markNotificationAsRead(UUID player, String notificationId) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "UPDATE " + tablePrefix + "notifications SET is_read = TRUE WHERE player_uuid = ? AND id = ?")) {
            ps.setString(1, player.toString());
            ps.setString(2, notificationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Błąd podczas oznaczania powiadomienia: " + e.getMessage());
        }
    }
}
