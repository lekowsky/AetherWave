package pl.skyrise.mCPhone.managers;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.skyrise.mCPhone.MCPhone;
import pl.skyrise.mCPhone.config.ConfigManager;
import pl.skyrise.mCPhone.gui.PhoneGUI;
import pl.skyrise.mCPhone.models.PhoneUser;
import pl.skyrise.mCPhone.utils.ItemBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manager telefonów
 */
public class PhoneManager {

    private final MCPhone plugin;
    private final Map<UUID, PhoneUser> users = new HashMap<>();

    public PhoneManager(MCPhone plugin) {
        this.plugin = plugin;
    }

    /**
     * Ładuje wszystkich użytkowników z bazy danych
     */
    public void loadAll() {
        users.clear();
        for (PhoneUser user : plugin.getDatabaseManager().loadAllUsers()) {
            users.put(user.getUuid(), user);
        }
        plugin.getLogger().info("Załadowano " + users.size() + " użytkowników telefonów.");
    }

    /**
     * Pobiera użytkownika (z cache lub bazy danych)
     */
    public PhoneUser getUser(UUID uuid) {
        if (users.containsKey(uuid)) {
            return users.get(uuid);
        }
        PhoneUser user = plugin.getDatabaseManager().loadUser(uuid);
        if (user != null) {
            users.put(uuid, user);
        }
        return user;
    }

    /**
     * Pobiera lub tworzy użytkownika
     */
    public PhoneUser getOrCreateUser(UUID uuid) {
        PhoneUser user = getUser(uuid);
        if (user == null) {
            user = new PhoneUser(uuid);
            users.put(uuid, user);
            saveUser(user);
        }
        return user;
    }

    /**
     * Zapisuje użytkownika do bazy danych
     */
    public void saveUser(PhoneUser user) {
        users.put(user.getUuid(), user);
        plugin.getDatabaseManager().saveUser(user);
    }

    /**
     * Sprawdza czy gracz ma telefon w ekwipunku
     */
    public boolean hasPhone(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (ItemBuilder.isPhone(item)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Daje graczowi telefon
     */
    public void givePhone(Player player) {
        ConfigManager config = plugin.getConfigManager();
        
        ItemStack phone = new ItemBuilder(config.getPhoneMaterial())
            .name(config.getPhoneDisplayName())
            .lore(config.getPhoneLore())
            .customModelData(config.getPhoneCustomModelData())
            .unbreakable(config.isPhoneUnbreakable())
            .hideFlags()
            .asPhone()
            .build();
        
        player.getInventory().addItem(phone);
        
        // Utwórz profil użytkownika jeśli nie istnieje
        getOrCreateUser(player.getUniqueId());
    }

    /**
     * Otwiera GUI telefonu dla gracza
     */
    public void openPhone(Player player) {
        PhoneUser user = getOrCreateUser(player.getUniqueId());
        PhoneGUI gui = new PhoneGUI(plugin, player, user);
        gui.open();
    }

    /**
     * Sprawdza czy numer telefonu jest już zajęty
     */
    public boolean isNumberTaken(String phoneNumber) {
        // Sprawdź w cache
        for (PhoneUser user : users.values()) {
            if (phoneNumber.equals(user.getPhoneNumber())) {
                return true;
            }
        }
        // Sprawdź w bazie danych
        return plugin.getDatabaseManager().isNumberTaken(phoneNumber);
    }

    /**
     * Pobiera UUID gracza po numerze telefonu
     */
    public UUID getPlayerByNumber(String phoneNumber) {
        // Sprawdź w cache
        for (PhoneUser user : users.values()) {
            if (phoneNumber.equals(user.getPhoneNumber())) {
                return user.getUuid();
            }
        }
        // Sprawdź w bazie danych
        return plugin.getDatabaseManager().getPlayerByNumber(phoneNumber);
    }

    /**
     * Tworzy przedmiot telefonu
     */
    public ItemStack createPhoneItem() {
        ConfigManager config = plugin.getConfigManager();
        
        return new ItemBuilder(config.getPhoneMaterial())
            .name(config.getPhoneDisplayName())
            .lore(config.getPhoneLore())
            .customModelData(config.getPhoneCustomModelData())
            .unbreakable(config.isPhoneUnbreakable())
            .hideFlags()
            .asPhone()
            .build();
    }
}
