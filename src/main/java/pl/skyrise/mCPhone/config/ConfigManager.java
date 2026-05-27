package pl.skyrise.mCPhone.config;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import pl.skyrise.mCPhone.MCPhone;

import java.util.List;

/**
 * Główny manager konfiguracji (config.yml)
 */
public class ConfigManager {

    private final MCPhone plugin;
    private FileConfiguration config;

    // Cache wartości
    private String prefix;
    private String language;
    private boolean debug;
    private int autoSaveInterval;

    // Baza danych
    private String databaseType;
    private String mysqlHost;
    private int mysqlPort;
    private String mysqlDatabase;
    private String mysqlUsername;
    private String mysqlPassword;
    private String mysqlTablePrefix;
    private int mysqlPoolSize;

    // Telefon
    private Material phoneMaterial;
    private int phoneCustomModelData;
    private String phoneDisplayName;
    private List<String> phoneLore;
    private boolean phoneUnbreakable;
    private boolean phoneDroppable;

    // Format numeru
    private String phoneNumberFormat;
    private int phoneNumberLength;
    private String phoneCountryPrefix;

    // Powiadomienia
    private Sound smsSound;
    private float smsSoundVolume;
    private float smsSoundPitch;
    private boolean chatNotifications;
    private boolean titleNotifications;
    private int titleFadeIn;
    private int titleStay;
    private int titleFadeOut;

    // SMS
    private int smsMaxLength;
    private boolean smsSaveHistory;
    private int smsMaxHistory;
    private int smsCooldown;
    private boolean smsRequireSim;

    // AppStore
    private boolean appstoreEnabled;
    private boolean appstoreEconomyEnabled;
    private String appstoreEconomyPlugin;

    // Punkty sprzedaży SIM
    private boolean simShopsEnabled;
    private Material simShopBlock;
    private int simShopRadius;

    public ConfigManager(MCPhone plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();

        // Ogólne
        prefix = config.getString("general.prefix", "&7[&5MCPhone&7] ");
        language = config.getString("general.language", "pl");
        debug = config.getBoolean("general.debug", false);
        autoSaveInterval = config.getInt("general.auto-save-interval", 300);

        // Baza danych
        databaseType = config.getString("database.type", "YAML").toUpperCase();
        mysqlHost = config.getString("database.mysql.host", "localhost");
        mysqlPort = config.getInt("database.mysql.port", 3306);
        mysqlDatabase = config.getString("database.mysql.database", "mcphone");
        mysqlUsername = config.getString("database.mysql.username", "root");
        mysqlPassword = config.getString("database.mysql.password", "password");
        mysqlTablePrefix = config.getString("database.mysql.table-prefix", "mcphone_");
        mysqlPoolSize = config.getInt("database.mysql.pool-size", 10);

        // Telefon
        phoneMaterial = Material.valueOf(config.getString("phone.material", "IRON_INGOT").toUpperCase());
        phoneCustomModelData = config.getInt("phone.custom-model-data", 10001);
        phoneDisplayName = config.getString("phone.display-name", "&5☎ &dTelefon");
        phoneLore = config.getStringList("phone.lore");
        phoneUnbreakable = config.getBoolean("phone.unbreakable", true);
        phoneDroppable = config.getBoolean("phone.droppable", false);

        // Format numeru
        phoneNumberFormat = config.getString("phone-number.format", "###-###-###");
        phoneNumberLength = config.getInt("phone-number.length", 9);
        phoneCountryPrefix = config.getString("phone-number.country-prefix", "+48");

        // Powiadomienia
        try {
            smsSound = Sound.valueOf(config.getString("notifications.sms-sound", "BLOCK_NOTE_BLOCK_BELL").toUpperCase());
        } catch (IllegalArgumentException e) {
            smsSound = Sound.BLOCK_NOTE_BLOCK_BELL;
        }
        smsSoundVolume = (float) config.getDouble("notifications.sms-sound-volume", 1.0);
        smsSoundPitch = (float) config.getDouble("notifications.sms-sound-pitch", 1.5);
        chatNotifications = config.getBoolean("notifications.chat-notifications", true);
        titleNotifications = config.getBoolean("notifications.title-notifications", true);
        titleFadeIn = config.getInt("notifications.title-fade-in", 10);
        titleStay = config.getInt("notifications.title-stay", 40);
        titleFadeOut = config.getInt("notifications.title-fade-out", 10);

        // SMS
        smsMaxLength = config.getInt("sms.max-length", 256);
        smsSaveHistory = config.getBoolean("sms.save-history", true);
        smsMaxHistory = config.getInt("sms.max-history", 100);
        smsCooldown = config.getInt("sms.cooldown", 1);
        smsRequireSim = config.getBoolean("sms.require-sim", true);

        // AppStore
        appstoreEnabled = config.getBoolean("appstore.enabled", true);
        appstoreEconomyEnabled = config.getBoolean("appstore.economy-enabled", false);
        appstoreEconomyPlugin = config.getString("appstore.economy-plugin", "Vault");

        // Punkty sprzedaży SIM
        simShopsEnabled = config.getBoolean("sim-shops.enabled", true);
        try {
            simShopBlock = Material.valueOf(config.getString("sim-shops.shop-block", "LODESTONE").toUpperCase());
        } catch (IllegalArgumentException e) {
            simShopBlock = Material.LODESTONE;
        }
        simShopRadius = config.getInt("sim-shops.interaction-radius", 3);
    }

    // Gettery
    public String getPrefix() { return prefix; }
    public String getLanguage() { return language; }
    public boolean isDebug() { return debug; }
    public int getAutoSaveInterval() { return autoSaveInterval; }

    public String getDatabaseType() { return databaseType; }
    public String getMysqlHost() { return mysqlHost; }
    public int getMysqlPort() { return mysqlPort; }
    public String getMysqlDatabase() { return mysqlDatabase; }
    public String getMysqlUsername() { return mysqlUsername; }
    public String getMysqlPassword() { return mysqlPassword; }
    public String getMysqlTablePrefix() { return mysqlTablePrefix; }
    public int getMysqlPoolSize() { return mysqlPoolSize; }

    public Material getPhoneMaterial() { return phoneMaterial; }
    public int getPhoneCustomModelData() { return phoneCustomModelData; }
    public String getPhoneDisplayName() { return phoneDisplayName; }
    public List<String> getPhoneLore() { return phoneLore; }
    public boolean isPhoneUnbreakable() { return phoneUnbreakable; }
    public boolean isPhoneDroppable() { return phoneDroppable; }

    public String getPhoneNumberFormat() { return phoneNumberFormat; }
    public int getPhoneNumberLength() { return phoneNumberLength; }
    public String getPhoneCountryPrefix() { return phoneCountryPrefix; }

    public Sound getSmsSound() { return smsSound; }
    public float getSmsSoundVolume() { return smsSoundVolume; }
    public float getSmsSoundPitch() { return smsSoundPitch; }
    public boolean isChatNotifications() { return chatNotifications; }
    public boolean isTitleNotifications() { return titleNotifications; }
    public int getTitleFadeIn() { return titleFadeIn; }
    public int getTitleStay() { return titleStay; }
    public int getTitleFadeOut() { return titleFadeOut; }

    public int getSmsMaxLength() { return smsMaxLength; }
    public boolean isSmsSaveHistory() { return smsSaveHistory; }
    public int getSmsMaxHistory() { return smsMaxHistory; }
    public int getSmsCooldown() { return smsCooldown; }
    public boolean isSmsRequireSim() { return smsRequireSim; }

    public boolean isAppstoreEnabled() { return appstoreEnabled; }
    public boolean isAppstoreEconomyEnabled() { return appstoreEconomyEnabled; }
    public String getAppstoreEconomyPlugin() { return appstoreEconomyPlugin; }

    public boolean isSimShopsEnabled() { return simShopsEnabled; }
    public Material getSimShopBlock() { return simShopBlock; }
    public int getSimShopRadius() { return simShopRadius; }
}
