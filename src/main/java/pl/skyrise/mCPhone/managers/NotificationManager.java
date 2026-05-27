package pl.skyrise.mCPhone.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pl.skyrise.mCPhone.MCPhone;
import pl.skyrise.mCPhone.api.PhoneNotification;
import pl.skyrise.mCPhone.config.ConfigManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Manager powiadomień
 */
public class NotificationManager {

    private final MCPhone plugin;

    public NotificationManager(MCPhone plugin) {
        this.plugin = plugin;
    }

    /**
     * Wysyła powiadomienie do gracza
     */
    public void sendNotification(UUID playerUUID, PhoneNotification notification) {
        // Zapisz do bazy
        plugin.getDatabaseManager().saveNotification(playerUUID, notification);

        // Wyślij powiadomienie jeśli gracz jest online
        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null && player.isOnline()) {
            ConfigManager config = plugin.getConfigManager();

            // Dźwięk
            player.playSound(player.getLocation(), config.getSmsSound(),
                config.getSmsSoundVolume(), config.getSmsSoundPitch());
        }
    }

    /**
     * Pobiera wszystkie powiadomienia gracza
     */
    public List<PhoneNotification> getNotifications(UUID playerUUID) {
        return plugin.getDatabaseManager().loadNotifications(playerUUID);
    }

    /**
     * Pobiera nieprzeczytane powiadomienia
     */
    public List<PhoneNotification> getUnreadNotifications(UUID playerUUID) {
        return getNotifications(playerUUID).stream()
            .filter(n -> !n.isRead())
            .collect(Collectors.toList());
    }

    /**
     * Pobiera ilość nieprzeczytanych powiadomień
     */
    public int getUnreadCount(UUID playerUUID) {
        return (int) getNotifications(playerUUID).stream()
            .filter(n -> !n.isRead())
            .count();
    }

    /**
     * Oznacza powiadomienie jako przeczytane
     */
    public void markAsRead(UUID playerUUID, String notificationId) {
        plugin.getDatabaseManager().markNotificationAsRead(playerUUID, notificationId);
    }

    /**
     * Oznacza wszystkie powiadomienia jako przeczytane
     */
    public void markAllAsRead(UUID playerUUID) {
        for (PhoneNotification notification : getNotifications(playerUUID)) {
            if (!notification.isRead()) {
                markAsRead(playerUUID, notification.getId());
            }
        }
    }

    /**
     * Czyści wszystkie powiadomienia gracza
     */
    public void clearNotifications(UUID playerUUID) {
        plugin.getDatabaseManager().clearNotifications(playerUUID);
    }

    /**
     * Pobiera powiadomienia dla konkretnej aplikacji
     */
    public List<PhoneNotification> getNotificationsForApp(UUID playerUUID, String appId) {
        return getNotifications(playerUUID).stream()
            .filter(n -> appId.equals(n.getAppId()))
            .collect(Collectors.toList());
    }
}
