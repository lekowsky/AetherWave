package pl.skyrise.mCPhone.database;

import pl.skyrise.mCPhone.MCPhone;
import pl.skyrise.mCPhone.api.PhoneNotification;
import pl.skyrise.mCPhone.api.SMSMessage;
import pl.skyrise.mCPhone.models.PhoneUser;

import java.util.List;
import java.util.UUID;

/**
 * Manager bazy danych - obsługuje YAML lub MySQL
 */
public class DatabaseManager {

    private final MCPhone plugin;
    private Database database;

    public DatabaseManager(MCPhone plugin) {
        this.plugin = plugin;
    }

    public void init() {
        String type = plugin.getConfigManager().getDatabaseType();
        
        if ("MYSQL".equalsIgnoreCase(type)) {
            database = new MySQLDatabase(plugin);
        } else {
            database = new YamlDatabase(plugin);
        }
        
        database.init();
    }

    public void close() {
        if (database != null) {
            database.close();
        }
    }

    public void saveAll() {
        if (database != null) {
            database.saveAll();
        }
    }

    // ============================================
    // Użytkownicy telefonów
    // ============================================

    public PhoneUser loadUser(UUID uuid) {
        return database.loadUser(uuid);
    }

    public void saveUser(PhoneUser user) {
        database.saveUser(user);
    }

    public List<PhoneUser> loadAllUsers() {
        return database.loadAllUsers();
    }

    public UUID getPlayerByNumber(String phoneNumber) {
        return database.getPlayerByNumber(phoneNumber);
    }

    public boolean isNumberTaken(String phoneNumber) {
        return database.isNumberTaken(phoneNumber);
    }

    // ============================================
    // Wiadomości SMS
    // ============================================

    public void saveSMS(SMSMessage message) {
        database.saveSMS(message);
    }

    public List<SMSMessage> loadConversation(UUID player1, UUID player2, int limit) {
        return database.loadConversation(player1, player2, limit);
    }

    public List<UUID> getConversationPartners(UUID player) {
        return database.getConversationPartners(player);
    }

    public SMSMessage getLastMessage(UUID player1, UUID player2) {
        return database.getLastMessage(player1, player2);
    }

    public int getUnreadCount(UUID player) {
        return database.getUnreadCount(player);
    }

    public void markAsRead(UUID player, UUID sender) {
        database.markAsRead(player, sender);
    }

    // ============================================
    // Powiadomienia
    // ============================================

    public void saveNotification(UUID player, PhoneNotification notification) {
        database.saveNotification(player, notification);
    }

    public List<PhoneNotification> loadNotifications(UUID player) {
        return database.loadNotifications(player);
    }

    public void clearNotifications(UUID player) {
        database.clearNotifications(player);
    }

    public void markNotificationAsRead(UUID player, String notificationId) {
        database.markNotificationAsRead(player, notificationId);
    }
}
