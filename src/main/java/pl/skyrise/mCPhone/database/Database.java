package pl.skyrise.mCPhone.database;

import pl.skyrise.mCPhone.api.PhoneNotification;
import pl.skyrise.mCPhone.api.SMSMessage;
import pl.skyrise.mCPhone.models.PhoneUser;

import java.util.List;
import java.util.UUID;

/**
 * Interfejs bazy danych
 */
public interface Database {

    void init();
    void close();
    void saveAll();

    // Użytkownicy
    PhoneUser loadUser(UUID uuid);
    void saveUser(PhoneUser user);
    List<PhoneUser> loadAllUsers();
    UUID getPlayerByNumber(String phoneNumber);
    boolean isNumberTaken(String phoneNumber);

    // Wiadomości SMS
    void saveSMS(SMSMessage message);
    List<SMSMessage> loadConversation(UUID player1, UUID player2, int limit);
    List<UUID> getConversationPartners(UUID player);
    SMSMessage getLastMessage(UUID player1, UUID player2);
    int getUnreadCount(UUID player);
    void markAsRead(UUID player, UUID sender);

    // Powiadomienia
    void saveNotification(UUID player, PhoneNotification notification);
    List<PhoneNotification> loadNotifications(UUID player);
    void clearNotifications(UUID player);
    void markNotificationAsRead(UUID player, String notificationId);
}
