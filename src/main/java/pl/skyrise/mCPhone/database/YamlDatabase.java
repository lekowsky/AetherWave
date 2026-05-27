package pl.skyrise.mCPhone.database;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import pl.skyrise.mCPhone.MCPhone;
import pl.skyrise.mCPhone.api.PhoneNotification;
import pl.skyrise.mCPhone.api.SMSMessage;
import pl.skyrise.mCPhone.models.Contact;
import pl.skyrise.mCPhone.models.PhoneUser;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementacja bazy danych YAML
 */
public class YamlDatabase implements Database {

    private final MCPhone plugin;
    private File dataFolder;
    private File usersFolder;
    private File messagesFolder;
    private File notificationsFolder;

    // Cache
    private final Map<UUID, PhoneUser> usersCache = new HashMap<>();
    private final Map<String, UUID> numberToUuidCache = new HashMap<>();

    public YamlDatabase(MCPhone plugin) {
        this.plugin = plugin;
    }

    @Override
    public void init() {
        dataFolder = new File(plugin.getDataFolder(), "data");
        usersFolder = new File(dataFolder, "users");
        messagesFolder = new File(dataFolder, "messages");
        notificationsFolder = new File(dataFolder, "notifications");

        dataFolder.mkdirs();
        usersFolder.mkdirs();
        messagesFolder.mkdirs();
        notificationsFolder.mkdirs();

        // Załaduj wszystkich użytkowników do cache
        loadAllUsersToCache();
    }

    @Override
    public void close() {
        saveAll();
    }

    @Override
    public void saveAll() {
        for (PhoneUser user : usersCache.values()) {
            saveUserToFile(user);
        }
    }

    private void loadAllUsersToCache() {
        File[] files = usersFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            try {
                String uuidStr = file.getName().replace(".yml", "");
                UUID uuid = UUID.fromString(uuidStr);
                PhoneUser user = loadUserFromFile(uuid);
                if (user != null) {
                    usersCache.put(uuid, user);
                    if (user.getPhoneNumber() != null) {
                        numberToUuidCache.put(user.getPhoneNumber(), uuid);
                    }
                }
            } catch (IllegalArgumentException ignored) {}
        }
    }

    // ============================================
    // Użytkownicy
    // ============================================

    @Override
    public PhoneUser loadUser(UUID uuid) {
        if (usersCache.containsKey(uuid)) {
            return usersCache.get(uuid);
        }
        PhoneUser user = loadUserFromFile(uuid);
        if (user != null) {
            usersCache.put(uuid, user);
            if (user.getPhoneNumber() != null) {
                numberToUuidCache.put(user.getPhoneNumber(), uuid);
            }
        }
        return user;
    }

    private PhoneUser loadUserFromFile(UUID uuid) {
        File file = new File(usersFolder, uuid.toString() + ".yml");
        if (!file.exists()) return null;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        
        String phoneNumber = config.getString("phone-number");
        String simType = config.getString("sim-type");
        boolean simActive = config.getBoolean("sim-active", false);
        int dailySmsCount = config.getInt("daily-sms-count", 0);
        long lastSmsReset = config.getLong("last-sms-reset", System.currentTimeMillis());
        
        Set<String> installedApps = new HashSet<>(config.getStringList("installed-apps"));
        
        List<Contact> contacts = new ArrayList<>();
        ConfigurationSection contactsSection = config.getConfigurationSection("contacts");
        if (contactsSection != null) {
            for (String key : contactsSection.getKeys(false)) {
                ConfigurationSection cs = contactsSection.getConfigurationSection(key);
                if (cs != null) {
                    String name = cs.getString("name");
                    String number = cs.getString("number");
                    String playerUuidStr = cs.getString("player-uuid");
                    UUID playerUuid = playerUuidStr != null ? UUID.fromString(playerUuidStr) : null;
                    contacts.add(new Contact(name, number, playerUuid));
                }
            }
        }

        return new PhoneUser(uuid, phoneNumber, simType, simActive, dailySmsCount, lastSmsReset, installedApps, contacts);
    }

    @Override
    public void saveUser(PhoneUser user) {
        usersCache.put(user.getUuid(), user);
        if (user.getPhoneNumber() != null) {
            numberToUuidCache.put(user.getPhoneNumber(), user.getUuid());
        }
        saveUserToFile(user);
    }

    private void saveUserToFile(PhoneUser user) {
        File file = new File(usersFolder, user.getUuid().toString() + ".yml");
        YamlConfiguration config = new YamlConfiguration();

        config.set("phone-number", user.getPhoneNumber());
        config.set("sim-type", user.getSimType());
        config.set("sim-active", user.isSimActive());
        config.set("daily-sms-count", user.getDailySmsCount());
        config.set("last-sms-reset", user.getLastSmsReset());
        config.set("installed-apps", new ArrayList<>(user.getInstalledApps()));

        int i = 0;
        for (Contact contact : user.getContacts()) {
            String path = "contacts." + i;
            config.set(path + ".name", contact.getName());
            config.set(path + ".number", contact.getPhoneNumber());
            if (contact.getPlayerUUID() != null) {
                config.set(path + ".player-uuid", contact.getPlayerUUID().toString());
            }
            i++;
        }

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Nie można zapisać pliku użytkownika: " + e.getMessage());
        }
    }

    @Override
    public List<PhoneUser> loadAllUsers() {
        return new ArrayList<>(usersCache.values());
    }

    @Override
    public UUID getPlayerByNumber(String phoneNumber) {
        return numberToUuidCache.get(phoneNumber);
    }

    @Override
    public boolean isNumberTaken(String phoneNumber) {
        return numberToUuidCache.containsKey(phoneNumber);
    }

    // ============================================
    // Wiadomości SMS
    // ============================================

    private String getConversationId(UUID player1, UUID player2) {
        String uuid1 = player1.toString();
        String uuid2 = player2.toString();
        return uuid1.compareTo(uuid2) < 0 ? uuid1 + "_" + uuid2 : uuid2 + "_" + uuid1;
    }

    @Override
    public void saveSMS(SMSMessage message) {
        String convId = getConversationId(message.getSender(), message.getReceiver());
        File file = new File(messagesFolder, convId + ".yml");
        YamlConfiguration config = file.exists() ? YamlConfiguration.loadConfiguration(file) : new YamlConfiguration();

        String path = "messages." + message.getId();
        config.set(path + ".sender", message.getSender().toString());
        config.set(path + ".receiver", message.getReceiver().toString());
        config.set(path + ".message", message.getMessage());
        config.set(path + ".timestamp", message.getTimestamp());
        config.set(path + ".read", message.isRead());

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Nie można zapisać wiadomości SMS: " + e.getMessage());
        }
    }

    @Override
    public List<SMSMessage> loadConversation(UUID player1, UUID player2, int limit) {
        String convId = getConversationId(player1, player2);
        File file = new File(messagesFolder, convId + ".yml");
        if (!file.exists()) return new ArrayList<>();

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection messagesSection = config.getConfigurationSection("messages");
        if (messagesSection == null) return new ArrayList<>();

        List<SMSMessage> messages = new ArrayList<>();
        for (String key : messagesSection.getKeys(false)) {
            ConfigurationSection ms = messagesSection.getConfigurationSection(key);
            if (ms != null) {
                SMSMessage msg = new SMSMessage(
                    key,
                    UUID.fromString(ms.getString("sender")),
                    UUID.fromString(ms.getString("receiver")),
                    ms.getString("message"),
                    ms.getLong("timestamp"),
                    ms.getBoolean("read", false)
                );
                messages.add(msg);
            }
        }

        // Sortuj po czasie i ogranicz
        messages.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
        if (limit > 0 && messages.size() > limit) {
            messages = messages.subList(0, limit);
        }

        return messages;
    }

    @Override
    public List<UUID> getConversationPartners(UUID player) {
        Set<UUID> partners = new HashSet<>();
        File[] files = messagesFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return new ArrayList<>();

        String playerStr = player.toString();
        for (File file : files) {
            String name = file.getName().replace(".yml", "");
            String[] parts = name.split("_");
            if (parts.length == 2) {
                if (parts[0].equals(playerStr)) {
                    partners.add(UUID.fromString(parts[1]));
                } else if (parts[1].equals(playerStr)) {
                    partners.add(UUID.fromString(parts[0]));
                }
            }
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
        int count = 0;
        for (UUID partner : getConversationPartners(player)) {
            List<SMSMessage> messages = loadConversation(player, partner, 100);
            for (SMSMessage msg : messages) {
                if (msg.getReceiver().equals(player) && !msg.isRead()) {
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    public void markAsRead(UUID player, UUID sender) {
        String convId = getConversationId(player, sender);
        File file = new File(messagesFolder, convId + ".yml");
        if (!file.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection messagesSection = config.getConfigurationSection("messages");
        if (messagesSection == null) return;

        for (String key : messagesSection.getKeys(false)) {
            ConfigurationSection ms = messagesSection.getConfigurationSection(key);
            if (ms != null && UUID.fromString(ms.getString("receiver")).equals(player)) {
                ms.set("read", true);
            }
        }

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Nie można zapisać stanu wiadomości: " + e.getMessage());
        }
    }

    // ============================================
    // Powiadomienia
    // ============================================

    @Override
    public void saveNotification(UUID player, PhoneNotification notification) {
        File file = new File(notificationsFolder, player.toString() + ".yml");
        YamlConfiguration config = file.exists() ? YamlConfiguration.loadConfiguration(file) : new YamlConfiguration();

        String path = "notifications." + notification.getId();
        config.set(path + ".app-id", notification.getAppId());
        config.set(path + ".title", notification.getTitle());
        config.set(path + ".message", notification.getMessage());
        config.set(path + ".timestamp", notification.getTimestamp());
        config.set(path + ".read", notification.isRead());

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Nie można zapisać powiadomienia: " + e.getMessage());
        }
    }

    @Override
    public List<PhoneNotification> loadNotifications(UUID player) {
        File file = new File(notificationsFolder, player.toString() + ".yml");
        if (!file.exists()) return new ArrayList<>();

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("notifications");
        if (section == null) return new ArrayList<>();

        List<PhoneNotification> notifications = new ArrayList<>();
        for (String key : section.getKeys(false)) {
            ConfigurationSection ns = section.getConfigurationSection(key);
            if (ns != null) {
                PhoneNotification notif = new PhoneNotification(
                    key,
                    ns.getString("app-id"),
                    ns.getString("title"),
                    ns.getString("message"),
                    ns.getLong("timestamp"),
                    ns.getBoolean("read", false)
                );
                notifications.add(notif);
            }
        }

        notifications.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
        return notifications;
    }

    @Override
    public void clearNotifications(UUID player) {
        File file = new File(notificationsFolder, player.toString() + ".yml");
        if (file.exists()) {
            file.delete();
        }
    }

    @Override
    public void markNotificationAsRead(UUID player, String notificationId) {
        File file = new File(notificationsFolder, player.toString() + ".yml");
        if (!file.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("notifications." + notificationId + ".read", true);

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Nie można zapisać stanu powiadomienia: " + e.getMessage());
        }
    }
}
