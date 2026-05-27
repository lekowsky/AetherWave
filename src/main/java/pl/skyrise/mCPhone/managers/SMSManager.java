package pl.skyrise.mCPhone.managers;

import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pl.skyrise.mCPhone.MCPhone;
import pl.skyrise.mCPhone.api.PhoneNotification;
import pl.skyrise.mCPhone.api.SMSMessage;
import pl.skyrise.mCPhone.config.ConfigManager;
import pl.skyrise.mCPhone.config.MessagesConfig;
import pl.skyrise.mCPhone.models.PhoneUser;
import pl.skyrise.mCPhone.models.SimType;
import pl.skyrise.mCPhone.utils.ColorUtils;

import java.time.Duration;
import java.util.*;

/**
 * Manager wiadomości SMS
 */
public class SMSManager {

    private final MCPhone plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public SMSManager(MCPhone plugin) {
        this.plugin = plugin;
    }

    /**
     * Wysyła wiadomość SMS
     */
    public boolean sendSMS(UUID senderUUID, UUID receiverUUID, String message) {
        ConfigManager config = plugin.getConfigManager();
        MessagesConfig messages = plugin.getMessagesConfig();
        
        Player sender = Bukkit.getPlayer(senderUUID);
        Player receiver = Bukkit.getPlayer(receiverUUID);
        
        // Sprawdź czy nie wysyła do siebie
        if (senderUUID.equals(receiverUUID)) {
            if (sender != null) {
                sender.sendMessage(messages.smsSelfSend());
            }
            return false;
        }
        
        // Sprawdź wymaganie karty SIM
        if (config.isSmsRequireSim()) {
            PhoneUser senderUser = plugin.getPhoneManager().getUser(senderUUID);
            if (senderUser == null || !senderUser.hasActiveSim()) {
                if (sender != null) {
                    sender.sendMessage(messages.smsNoSimRequired());
                }
                return false;
            }
            
            // Sprawdź limit SMS
            SimType simType = plugin.getSimConfig().getSimType(senderUser.getSimType());
            if (simType != null && !simType.hasUnlimitedSms()) {
                // Resetuj licznik jeśli minął dzień
                if (senderUser.shouldResetSmsCount()) {
                    senderUser.resetSmsCount();
                }
                
                if (senderUser.getDailySmsCount() >= simType.getDailySmsLimit()) {
                    if (sender != null) {
                        sender.sendMessage(ColorUtils.colorize(config.getPrefix() + 
                            "&cOsiągnięto dzienny limit SMS (" + simType.getDailySmsLimit() + ")!"));
                    }
                    return false;
                }
            }
        }
        
        // Sprawdź cooldown
        int cooldownTime = config.getSmsCooldown();
        if (cooldownTime > 0 && sender != null) {
            long lastSent = cooldowns.getOrDefault(senderUUID, 0L);
            long now = System.currentTimeMillis();
            long diff = (now - lastSent) / 1000;
            
            if (diff < cooldownTime) {
                sender.sendMessage(messages.smsCooldown((int) (cooldownTime - diff)));
                return false;
            }
            cooldowns.put(senderUUID, now);
        }
        
        // Sprawdź długość wiadomości
        if (message.length() > config.getSmsMaxLength()) {
            if (sender != null) {
                sender.sendMessage(messages.smsTooLong(config.getSmsMaxLength()));
            }
            return false;
        }
        
        // Utwórz wiadomość
        SMSMessage smsMessage = new SMSMessage(senderUUID, receiverUUID, message);
        
        // Zapisz wiadomość
        if (config.isSmsSaveHistory()) {
            plugin.getDatabaseManager().saveSMS(smsMessage);
        }
        
        // Zwiększ licznik SMS nadawcy
        PhoneUser senderUser = plugin.getPhoneManager().getUser(senderUUID);
        if (senderUser != null) {
            senderUser.incrementSmsCount();
            plugin.getPhoneManager().saveUser(senderUser);
        }
        
        // Pobierz nazwy
        String senderName = getSenderName(senderUUID, receiverUUID);
        String receiverName = getReceiverName(senderUUID, receiverUUID);
        
        // Wyślij potwierdzenie do nadawcy
        if (sender != null) {
            sender.sendMessage(messages.smsSent(receiverName, message));
        }
        
        // Wyślij powiadomienie do odbiorcy
        if (receiver != null) {
            // Wiadomość na chacie
            if (config.isChatNotifications()) {
                receiver.sendMessage(messages.smsReceived(senderName, message));
            }
            
            // Dźwięk
            receiver.playSound(receiver.getLocation(), config.getSmsSound(), 
                config.getSmsSoundVolume(), config.getSmsSoundPitch());
            
            // Tytuł
            if (config.isTitleNotifications()) {
                Title title = Title.title(
                    ColorUtils.toComponent(messages.smsReceivedTitle()),
                    ColorUtils.toComponent(messages.smsReceivedSubtitle(senderName)),
                    Title.Times.times(
                        Duration.ofMillis(config.getTitleFadeIn() * 50L),
                        Duration.ofMillis(config.getTitleStay() * 50L),
                        Duration.ofMillis(config.getTitleFadeOut() * 50L)
                    )
                );
                receiver.showTitle(title);
            }
        }
        
        // Dodaj powiadomienie do telefonu
        PhoneNotification notification = new PhoneNotification(
            "sms",
            "✉ SMS od " + senderName,
            message.length() > 30 ? message.substring(0, 30) + "..." : message
        );
        plugin.getNotificationManager().sendNotification(receiverUUID, notification);
        
        return true;
    }

    /**
     * Pobiera nazwę nadawcy (z kontaktów lub nick)
     */
    private String getSenderName(UUID senderUUID, UUID viewerUUID) {
        PhoneUser viewer = plugin.getPhoneManager().getUser(viewerUUID);
        PhoneUser sender = plugin.getPhoneManager().getUser(senderUUID);
        
        if (viewer != null && sender != null && sender.getPhoneNumber() != null) {
            String contactName = viewer.getContactNameOrNumber(sender.getPhoneNumber());
            if (!contactName.equals(sender.getPhoneNumber())) {
                return contactName;
            }
        }
        
        Player senderPlayer = Bukkit.getPlayer(senderUUID);
        return senderPlayer != null ? senderPlayer.getName() : "Nieznany";
    }

    /**
     * Pobiera nazwę odbiorcy (z kontaktów lub nick)
     */
    private String getReceiverName(UUID senderUUID, UUID receiverUUID) {
        PhoneUser sender = plugin.getPhoneManager().getUser(senderUUID);
        PhoneUser receiver = plugin.getPhoneManager().getUser(receiverUUID);
        
        if (sender != null && receiver != null && receiver.getPhoneNumber() != null) {
            String contactName = sender.getContactNameOrNumber(receiver.getPhoneNumber());
            if (!contactName.equals(receiver.getPhoneNumber())) {
                return contactName;
            }
        }
        
        Player receiverPlayer = Bukkit.getPlayer(receiverUUID);
        return receiverPlayer != null ? receiverPlayer.getName() : "Nieznany";
    }

    /**
     * Pobiera konwersację między dwoma graczami
     */
    public List<SMSMessage> getConversation(UUID player1, UUID player2, int limit) {
        return plugin.getDatabaseManager().loadConversation(player1, player2, limit);
    }

    /**
     * Pobiera listę partnerów konwersacji gracza
     */
    public List<UUID> getConversationPartners(UUID player) {
        return plugin.getDatabaseManager().getConversationPartners(player);
    }

    /**
     * Pobiera ostatnią wiadomość z konwersacji
     */
    public SMSMessage getLastMessage(UUID player1, UUID player2) {
        return plugin.getDatabaseManager().getLastMessage(player1, player2);
    }

    /**
     * Pobiera ilość nieprzeczytanych wiadomości
     */
    public int getUnreadCount(UUID player) {
        return plugin.getDatabaseManager().getUnreadCount(player);
    }

    /**
     * Oznacza wiadomości jako przeczytane
     */
    public void markAsRead(UUID player, UUID sender) {
        plugin.getDatabaseManager().markAsRead(player, sender);
    }
}
