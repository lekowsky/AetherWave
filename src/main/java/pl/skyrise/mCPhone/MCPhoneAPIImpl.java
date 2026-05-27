package pl.skyrise.mCPhone;

import org.bukkit.entity.Player;
import pl.skyrise.mCPhone.api.MCPhoneAPI;
import pl.skyrise.mCPhone.api.PhoneApp;
import pl.skyrise.mCPhone.api.PhoneNotification;
import pl.skyrise.mCPhone.api.SMSMessage;
import pl.skyrise.mCPhone.models.PhoneUser;

import java.util.List;
import java.util.UUID;

/**
 * Implementacja API MCPhone
 */
public class MCPhoneAPIImpl implements MCPhoneAPI {

    private final MCPhone plugin;

    public MCPhoneAPIImpl(MCPhone plugin) {
        this.plugin = plugin;
    }

    // ═══════════════════════════════════════════
    // REJESTRACJA APLIKACJI
    // ═══════════════════════════════════════════

    @Override
    public boolean registerApp(PhoneApp app) {
        return plugin.getAppManager().registerExternalApp(app);
    }

    @Override
    public boolean unregisterApp(String appId) {
        return plugin.getAppManager().unregisterExternalApp(appId);
    }

    @Override
    public List<PhoneApp> getRegisteredApps() {
        return plugin.getAppManager().getAllApps();
    }

    @Override
    public boolean isAppRegistered(String appId) {
        return plugin.getAppManager().isAppRegistered(appId);
    }

    // ═══════════════════════════════════════════
    // SYSTEM WIADOMOŚCI SMS
    // ═══════════════════════════════════════════

    @Override
    public boolean sendSMS(UUID sender, UUID receiver, String message) {
        return plugin.getSmsManager().sendSMS(sender, receiver, message);
    }

    @Override
    public boolean sendSMS(UUID sender, String phoneNumber, String message) {
        UUID receiver = plugin.getPhoneManager().getPlayerByNumber(phoneNumber);
        if (receiver == null) {
            return false;
        }
        return plugin.getSmsManager().sendSMS(sender, receiver, message);
    }

    @Override
    public List<SMSMessage> getMessageHistory(UUID player1, UUID player2, int limit) {
        return plugin.getSmsManager().getConversation(player1, player2, limit);
    }

    // ═══════════════════════════════════════════
    // SYSTEM NUMERÓW TELEFONÓW
    // ═══════════════════════════════════════════

    @Override
    public String getPhoneNumber(UUID playerUUID) {
        PhoneUser user = plugin.getPhoneManager().getUser(playerUUID);
        return user != null ? user.getPhoneNumber() : null;
    }

    @Override
    public boolean setPhoneNumber(UUID playerUUID, String phoneNumber) {
        PhoneUser user = plugin.getPhoneManager().getOrCreateUser(playerUUID);
        if (plugin.getPhoneManager().isNumberTaken(phoneNumber)) {
            return false;
        }
        user.setPhoneNumber(phoneNumber);
        plugin.getPhoneManager().saveUser(user);
        return true;
    }

    @Override
    public boolean hasActiveSIM(UUID playerUUID) {
        PhoneUser user = plugin.getPhoneManager().getUser(playerUUID);
        return user != null && user.hasActiveSim();
    }

    @Override
    public UUID getPlayerByNumber(String phoneNumber) {
        return plugin.getPhoneManager().getPlayerByNumber(phoneNumber);
    }

    // ═══════════════════════════════════════════
    // SYSTEM POWIADOMIEŃ
    // ═══════════════════════════════════════════

    @Override
    public void sendNotification(UUID playerUUID, PhoneNotification notification) {
        plugin.getNotificationManager().sendNotification(playerUUID, notification);
    }

    @Override
    public List<PhoneNotification> getUnreadNotifications(UUID playerUUID) {
        return plugin.getNotificationManager().getUnreadNotifications(playerUUID);
    }

    @Override
    public void clearNotifications(UUID playerUUID) {
        plugin.getNotificationManager().clearNotifications(playerUUID);
    }

    // ═══════════════════════════════════════════
    // SYSTEM TELEFONÓW
    // ═══════════════════════════════════════════

    @Override
    public boolean hasPhone(Player player) {
        return plugin.getPhoneManager().hasPhone(player);
    }

    @Override
    public void givePhone(Player player) {
        plugin.getPhoneManager().givePhone(player);
    }

    @Override
    public void openPhone(Player player) {
        plugin.getPhoneManager().openPhone(player);
    }
}
