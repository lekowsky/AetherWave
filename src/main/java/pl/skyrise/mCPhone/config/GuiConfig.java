package pl.skyrise.mCPhone.config;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import pl.skyrise.mCPhone.MCPhone;
import pl.skyrise.mCPhone.utils.ColorUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Manager konfiguracji GUI (gui.yml)
 * NAPRAWIONY - obsługa title-offset i title-format
 */
public class GuiConfig {

    private final MCPhone plugin;
    private File file;
    private FileConfiguration config;

    // Phone GUI
    private String phoneGuiTitle;
    private String nexoTitle;
    private int phoneGuiSize;
    private int titleOffset;        // NOWE: przesunięcie w pikselach
    private String titleFormat;     // NOWE: "legacy", "minimessage", "raw"

    // Frame
    private List<Integer> frameSlots;
    private Material frameMaterial;
    private int frameCustomModelData;
    private String frameDisplayName;

    // Screen
    private List<Integer> screenSlots;
    private Material screenBackgroundMaterial;
    private int screenBackgroundCustomModelData;
    private String screenBackgroundDisplayName;

    // Navigation
    private int navLeftSlot;
    private Material navLeftMaterial;
    private String navLeftDisplayName;
    private List<String> navLeftLore;

    private int navHomeSlot;
    private Material navHomeMaterial;
    private String navHomeDisplayName;
    private List<String> navHomeLore;

    private int navRightSlot;
    private Material navRightMaterial;
    private String navRightDisplayName;
    private List<String> navRightLore;

    // Contacts GUI
    private String contactsGuiTitle;
    private int contactsGuiSize;
    private int contactsAddButtonSlot;
    private Material contactsAddButtonMaterial;
    private String contactsAddButtonDisplayName;

    // SMS GUI
    private String smsGuiTitle;
    private int smsGuiSize;
    private int smsComposeButtonSlot;
    private Material smsComposeButtonMaterial;
    private String smsComposeButtonDisplayName;

    // SMS Conversations GUI
    private String smsConversationsGuiTitle;
    private int smsConversationsGuiSize;

    // AppStore GUI
    private String appstoreGuiTitle;
    private int appstoreGuiSize;

    // Settings GUI
    private String settingsGuiTitle;
    private int settingsGuiSize;

    // SIM Shop GUI
    private String simShopGuiTitle;
    private int simShopGuiSize;

    public GuiConfig(MCPhone plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        file = new File(plugin.getDataFolder(), "gui.yml");
        if (!file.exists()) {
            plugin.saveResource("gui.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
        loadConfig();
    }

    private void loadConfig() {
        // Phone GUI
        ConfigurationSection phoneGui = config.getConfigurationSection("phone-gui");
        if (phoneGui != null) {
            phoneGuiTitle = phoneGui.getString("title", "MCPhone");
            nexoTitle = phoneGui.getString("nexo-title", "");

            // =======================================
            // NOWE OPCJE
            // =======================================
            titleOffset = phoneGui.getInt("title-offset", 0);
            titleFormat = phoneGui.getString("title-format", "raw");

            phoneGuiSize = phoneGui.getInt("size", 54);

            // Frame
            ConfigurationSection frame = phoneGui.getConfigurationSection("frame");
            if (frame != null) {
                frameMaterial = getMaterial(frame.getString("material", "BLACK_STAINED_GLASS_PANE"));
                frameCustomModelData = frame.getInt("custom-model-data", 0);
                frameDisplayName = frame.getString("display-name", " ");
                frameSlots = frame.getIntegerList("slots");
            }

            // Screen
            ConfigurationSection screen = phoneGui.getConfigurationSection("screen");
            if (screen != null) {
                screenSlots = screen.getIntegerList("slots");
                ConfigurationSection background = screen.getConfigurationSection("background");
                if (background != null) {
                    screenBackgroundMaterial = getMaterial(background.getString("material", "GRAY_STAINED_GLASS_PANE"));
                    screenBackgroundCustomModelData = background.getInt("custom-model-data", 0);
                    screenBackgroundDisplayName = background.getString("display-name", " ");
                }
            }

            // Navigation
            ConfigurationSection nav = phoneGui.getConfigurationSection("navigation");
            if (nav != null) {
                ConfigurationSection left = nav.getConfigurationSection("left");
                if (left != null) {
                    navLeftSlot = left.getInt("slot", 48);
                    navLeftMaterial = getMaterial(left.getString("material", "ARROW"));
                    navLeftDisplayName = left.getString("display-name", "&7◄ Poprzednia strona");
                    navLeftLore = left.getStringList("lore");
                }

                ConfigurationSection home = nav.getConfigurationSection("home");
                if (home != null) {
                    navHomeSlot = home.getInt("slot", 49);
                    navHomeMaterial = getMaterial(home.getString("material", "NETHER_STAR"));
                    navHomeDisplayName = home.getString("display-name", "&f⌂ Pulpit");
                    navHomeLore = home.getStringList("lore");
                }

                ConfigurationSection right = nav.getConfigurationSection("right");
                if (right != null) {
                    navRightSlot = right.getInt("slot", 50);
                    navRightMaterial = getMaterial(right.getString("material", "ARROW"));
                    navRightDisplayName = right.getString("display-name", "&7Następna strona ►");
                    navRightLore = right.getStringList("lore");
                }
            }
        }

        // Contacts GUI
        ConfigurationSection contactsGui = config.getConfigurationSection("contacts-gui");
        if (contactsGui != null) {
            contactsGuiTitle = contactsGui.getString("title", "☎ Kontakty");
            contactsGuiSize = contactsGui.getInt("size", 54);

            ConfigurationSection addButton = contactsGui.getConfigurationSection("add-button");
            if (addButton != null) {
                contactsAddButtonSlot = addButton.getInt("slot", 49);
                contactsAddButtonMaterial = getMaterial(addButton.getString("material", "LIME_DYE"));
                contactsAddButtonDisplayName = addButton.getString("display-name", "&a✚ Dodaj kontakt");
            }
        }

        // SMS GUI
        ConfigurationSection smsGui = config.getConfigurationSection("sms-gui");
        if (smsGui != null) {
            smsGuiTitle = smsGui.getString("title", "✉ Wiadomości - {contact}");
            smsGuiSize = smsGui.getInt("size", 54);

            ConfigurationSection composeButton = smsGui.getConfigurationSection("compose-button");
            if (composeButton != null) {
                smsComposeButtonSlot = composeButton.getInt("slot", 49);
                smsComposeButtonMaterial = getMaterial(composeButton.getString("material", "WRITABLE_BOOK"));
                smsComposeButtonDisplayName = composeButton.getString("display-name", "&e✎ Napisz wiadomość");
            }
        }

        // SMS Conversations GUI
        ConfigurationSection smsConvGui = config.getConfigurationSection("sms-conversations-gui");
        if (smsConvGui != null) {
            smsConversationsGuiTitle = smsConvGui.getString("title", "✉ Wiadomości");
            smsConversationsGuiSize = smsConvGui.getInt("size", 54);
        }

        // AppStore GUI
        ConfigurationSection appstoreGui = config.getConfigurationSection("appstore-gui");
        if (appstoreGui != null) {
            appstoreGuiTitle = appstoreGui.getString("title", "🛒 Sklep z aplikacjami");
            appstoreGuiSize = appstoreGui.getInt("size", 54);
        }

        // Settings GUI
        ConfigurationSection settingsGui = config.getConfigurationSection("settings-gui");
        if (settingsGui != null) {
            settingsGuiTitle = settingsGui.getString("title", "⚙ Ustawienia");
            settingsGuiSize = settingsGui.getInt("size", 54);
        }

        // SIM Shop GUI
        ConfigurationSection simShopGui = config.getConfigurationSection("sim-shop-gui");
        if (simShopGui != null) {
            simShopGuiTitle = simShopGui.getString("title", "💳 Sklep z kartami SIM");
            simShopGuiSize = simShopGui.getInt("size", 27);
        }
    }

    private Material getMaterial(String name) {
        try {
            return Material.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Material.STONE;
        }
    }

    // =====================================================
    // NOWA KLUCZOWA METODA
    // =====================================================

    /**
     * Zwraca gotowy Component tytułu GUI z obsługą offsetu.
     *
     * Używa:
     * - nexo-title jeśli ustawiony (priorytet)
     * - title jako fallback
     * - title-offset do dodania negative space
     * - title-format do wyboru parsera
     *
     * @return Component gotowy do Bukkit.createInventory()
     */
    public Component getTitleComponent() {
        String title = getEffectiveTitle();
        return ColorUtils.toTitleComponent(title, titleOffset, titleFormat);
    }

    // =====================================================
    // Gettery
    // =====================================================

    public String getPhoneGuiTitle() {
        return phoneGuiTitle;
    }

    public String getNexoTitle() {
        return nexoTitle;
    }

    public boolean useNexo() {
        return nexoTitle != null && !nexoTitle.isEmpty();
    }

    public String getEffectiveTitle() {
        return useNexo() ? nexoTitle : phoneGuiTitle;
    }

    public int getTitleOffset() {
        return titleOffset;
    }

    public String getTitleFormat() {
        return titleFormat;
    }

    public int getPhoneGuiSize() {
        return phoneGuiSize;
    }

    public List<Integer> getFrameSlots() {
        return frameSlots != null ? frameSlots : new ArrayList<>();
    }

    public Material getFrameMaterial() {
        return frameMaterial;
    }

    public int getFrameCustomModelData() {
        return frameCustomModelData;
    }

    public String getFrameDisplayName() {
        return frameDisplayName;
    }

    public List<Integer> getScreenSlots() {
        return screenSlots != null ? screenSlots : new ArrayList<>();
    }

    public Material getScreenBackgroundMaterial() {
        return screenBackgroundMaterial;
    }

    public int getScreenBackgroundCustomModelData() {
        return screenBackgroundCustomModelData;
    }

    public String getScreenBackgroundDisplayName() {
        return screenBackgroundDisplayName;
    }

    public int getNavLeftSlot() {
        return navLeftSlot;
    }

    public Material getNavLeftMaterial() {
        return navLeftMaterial;
    }

    public String getNavLeftDisplayName() {
        return navLeftDisplayName;
    }

    public List<String> getNavLeftLore() {
        return navLeftLore != null ? navLeftLore : new ArrayList<>();
    }

    public int getNavHomeSlot() {
        return navHomeSlot;
    }

    public Material getNavHomeMaterial() {
        return navHomeMaterial;
    }

    public String getNavHomeDisplayName() {
        return navHomeDisplayName;
    }

    public List<String> getNavHomeLore() {
        return navHomeLore != null ? navHomeLore : new ArrayList<>();
    }

    public int getNavRightSlot() {
        return navRightSlot;
    }

    public Material getNavRightMaterial() {
        return navRightMaterial;
    }

    public String getNavRightDisplayName() {
        return navRightDisplayName;
    }

    public List<String> getNavRightLore() {
        return navRightLore != null ? navRightLore : new ArrayList<>();
    }

    // Contacts GUI gettery
    public String getContactsGuiTitle() {
        return contactsGuiTitle;
    }

    public int getContactsGuiSize() {
        return contactsGuiSize;
    }

    public int getContactsAddButtonSlot() {
        return contactsAddButtonSlot;
    }

    public Material getContactsAddButtonMaterial() {
        return contactsAddButtonMaterial;
    }

    public String getContactsAddButtonDisplayName() {
        return contactsAddButtonDisplayName;
    }

    // SMS GUI gettery
    public String getSmsGuiTitle() {
        return smsGuiTitle;
    }

    public int getSmsGuiSize() {
        return smsGuiSize;
    }

    public int getSmsComposeButtonSlot() {
        return smsComposeButtonSlot;
    }

    public Material getSmsComposeButtonMaterial() {
        return smsComposeButtonMaterial;
    }

    public String getSmsComposeButtonDisplayName() {
        return smsComposeButtonDisplayName;
    }

    public String getSmsConversationsGuiTitle() {
        return smsConversationsGuiTitle;
    }

    public int getSmsConversationsGuiSize() {
        return smsConversationsGuiSize;
    }

    // AppStore GUI gettery
    public String getAppstoreGuiTitle() {
        return appstoreGuiTitle;
    }

    public int getAppstoreGuiSize() {
        return appstoreGuiSize;
    }

    // Settings GUI gettery
    public String getSettingsGuiTitle() {
        return settingsGuiTitle;
    }

    public int getSettingsGuiSize() {
        return settingsGuiSize;
    }

    // SIM Shop GUI gettery
    public String getSimShopGuiTitle() {
        return simShopGuiTitle;
    }

    public int getSimShopGuiSize() {
        return simShopGuiSize;
    }
}