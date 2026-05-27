package pl.skyrise.mCPhone.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import pl.skyrise.mCPhone.MCPhone;
import pl.skyrise.mCPhone.utils.ColorUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Manager wiadomości (messages.yml)
 */
public class MessagesConfig {

    private final MCPhone plugin;
    private File file;
    private FileConfiguration config;

    // Cache wiadomości
    private final Map<String, String> messages = new HashMap<>();

    public MessagesConfig(MCPhone plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
        loadMessages();
    }

    private void loadMessages() {
        messages.clear();
        loadSection("general");
        loadSection("phone");
        loadSection("sms");
        loadSection("contacts");
        loadSection("sim");
        loadSection("appstore");
        loadSection("admin");
    }

    private void loadSection(String section) {
        if (config.isConfigurationSection(section)) {
            for (String key : config.getConfigurationSection(section).getKeys(false)) {
                String fullKey = section + "." + key;
                messages.put(fullKey, config.getString(fullKey, ""));
            }
        }
    }

    /**
     * Pobiera wiadomość z prefixem pluginu.
     */
    public String get(String key) {
        String prefix = plugin.getConfigManager().getPrefix();
        String message = messages.getOrDefault(key, "&cBrak wiadomości: " + key);
        return ColorUtils.colorize(prefix + message);
    }

    /**
     * Pobiera wiadomość bez prefixu.
     */
    public String getRaw(String key) {
        String message = messages.getOrDefault(key, "&cBrak wiadomości: " + key);
        return ColorUtils.colorize(message);
    }

    /**
     * Pobiera wiadomość z placeholderami.
     */
    public String get(String key, Map<String, String> placeholders) {
        String message = get(key);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return message;
    }

    /**
     * Pobiera wiadomość z placeholderami bez prefixu.
     */
    public String getRaw(String key, Map<String, String> placeholders) {
        String message = getRaw(key);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return message;
    }

    // Wiadomości ogólne
    public String noPermission() { return get("general.no-permission"); }
    public String playerOnly() { return get("general.player-only"); }
    public String reloadSuccess() { return get("general.reload-success"); }
    public String invalidArgs(String usage) { return get("general.invalid-args", Map.of("usage", usage)); }
    public String playerNotFound(String player) { return get("general.player-not-found", Map.of("player", player)); }
    public String playerOffline(String player) { return get("general.player-offline", Map.of("player", player)); }

    // Wiadomości telefonu
    public String phoneReceived() { return get("phone.received"); }
    public String phoneAlreadyHas() { return get("phone.already-has"); }
    public String phoneNoPhone() { return get("phone.no-phone"); }
    public String phoneOpened() { return get("phone.opened"); }
    public String phoneNoSim() { return get("phone.no-sim"); }

    // Wiadomości SMS
    public String smsSent(String receiver, String message) {
        return get("sms.sent", Map.of("receiver", receiver, "message", message));
    }
    public String smsReceived(String sender, String message) {
        return get("sms.received", Map.of("sender", sender, "message", message));
    }
    public String smsReceivedTitle() { return getRaw("sms.received-title"); }
    public String smsReceivedSubtitle(String sender) {
        return getRaw("sms.received-subtitle", Map.of("sender", sender));
    }
    public String smsNoSimRequired() { return get("sms.no-sim-required"); }
    public String smsSelfSend() { return get("sms.self-send"); }
    public String smsCooldown(int time) { return get("sms.cooldown", Map.of("time", String.valueOf(time))); }
    public String smsTooLong(int max) { return get("sms.too-long", Map.of("max", String.valueOf(max))); }
    public String smsNumberNotFound(String number) { return get("sms.number-not-found", Map.of("number", number)); }

    // Wiadomości kontaktów
    public String contactsAdded(String name, String number) {
        return get("contacts.added", Map.of("name", name, "number", number));
    }
    public String contactsRemoved(String name) { return get("contacts.removed", Map.of("name", name)); }
    public String contactsEdited(String name) { return get("contacts.edited", Map.of("name", name)); }
    public String contactsAlreadyExists() { return get("contacts.already-exists"); }
    public String contactsNotFound() { return get("contacts.not-found"); }
    public String contactsListEmpty() { return get("contacts.list-empty"); }

    // Wiadomości karty SIM
    public String simActivated(String number) { return get("sim.activated", Map.of("number", number)); }
    public String simAlreadyActive() { return get("sim.already-active"); }
    public String simPurchased(String type) { return get("sim.purchased", Map.of("type", type)); }
    public String simNoMoney(String price) { return get("sim.no-money", Map.of("price", price)); }
    public String simDeactivated() { return get("sim.deactivated"); }
    public String simInvalidCard() { return get("sim.invalid-card"); }

    // Wiadomości sklepu
    public String appstoreInstalled(String app) { return get("appstore.installed", Map.of("app", app)); }
    public String appstoreUninstalled(String app) { return get("appstore.uninstalled", Map.of("app", app)); }
    public String appstoreAlreadyInstalled(String app) { return get("appstore.already-installed", Map.of("app", app)); }
    public String appstoreNotInstalled(String app) { return get("appstore.not-installed", Map.of("app", app)); }
    public String appstorePurchased(String app, String price) {
        return get("appstore.purchased", Map.of("app", app, "price", price));
    }

    // Wiadomości administracyjne
    public String adminGivePhone(String player) { return get("admin.give-phone", Map.of("player", player)); }
    public String adminGiveSim(String type, String player) {
        return get("admin.give-sim", Map.of("type", type, "player", player));
    }
    public String adminSetNumber(String number, String player) {
        return get("admin.set-number", Map.of("number", number, "player", player));
    }
    public String adminRemoveNumber(String player) { return get("admin.remove-number", Map.of("player", player)); }
}
