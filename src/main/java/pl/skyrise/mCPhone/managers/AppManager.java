package pl.skyrise.mCPhone.managers;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.skyrise.mCPhone.MCPhone;
import pl.skyrise.mCPhone.api.PhoneApp;
import pl.skyrise.mCPhone.config.AppsConfig;
import pl.skyrise.mCPhone.models.AppData;
import pl.skyrise.mCPhone.models.PhoneUser;
import pl.skyrise.mCPhone.utils.ColorUtils;
import pl.skyrise.mCPhone.utils.ItemBuilder;

import java.util.*;

/**
 * Manager aplikacji
 */
public class AppManager {

    private final MCPhone plugin;
    private final Map<String, PhoneApp> registeredApps = new HashMap<>();
    private final Map<String, AppData> defaultApps = new HashMap<>();
    private final Map<String, AppData> storeApps = new HashMap<>();

    public AppManager(MCPhone plugin) {
        this.plugin = plugin;
    }

    /**
     * Ładuje domyślne aplikacje z konfiguracji
     */
    public void loadDefaultApps() {
        defaultApps.clear();
        defaultApps.putAll(plugin.getAppsConfig().getDefaultApps());
        plugin.getLogger().info("Załadowano " + defaultApps.size() + " domyślnych aplikacji.");
    }

    /**
     * Ładuje aplikacje ze sklepu z konfiguracji
     */
    public void loadStoreApps() {
        storeApps.clear();
        storeApps.putAll(plugin.getAppsConfig().getStoreApps());
        plugin.getLogger().info("Załadowano " + storeApps.size() + " aplikacji ze sklepu.");
    }

    /**
     * Rejestruje zewnętrzną aplikację (z API)
     */
    public boolean registerExternalApp(PhoneApp app) {
        if (app == null || app.getId() == null || app.getId().isEmpty()) {
            return false;
        }
        
        if (registeredApps.containsKey(app.getId()) || defaultApps.containsKey(app.getId()) || storeApps.containsKey(app.getId())) {
            plugin.getLogger().warning("Aplikacja o ID '" + app.getId() + "' jest już zarejestrowana!");
            return false;
        }
        
        registeredApps.put(app.getId(), app);
        plugin.getLogger().info("Zarejestrowano zewnętrzną aplikację: " + app.getId());
        return true;
    }

    /**
     * Wyrejestrowuje zewnętrzną aplikację
     */
    public boolean unregisterExternalApp(String appId) {
        if (registeredApps.remove(appId) != null) {
            plugin.getLogger().info("Wyrejestrowano zewnętrzną aplikację: " + appId);
            return true;
        }
        return false;
    }

    /**
     * Sprawdza czy aplikacja jest zarejestrowana
     */
    public boolean isAppRegistered(String appId) {
        return registeredApps.containsKey(appId) || defaultApps.containsKey(appId) || storeApps.containsKey(appId);
    }

    /**
     * Pobiera listę wszystkich aplikacji
     */
    public List<PhoneApp> getAllApps() {
        List<PhoneApp> apps = new ArrayList<>(registeredApps.values());
        // Konwertuj AppData na PhoneApp wrapper
        for (AppData data : defaultApps.values()) {
            apps.add(createAppWrapper(data));
        }
        for (AppData data : storeApps.values()) {
            apps.add(createAppWrapper(data));
        }
        return apps;
    }

    /**
     * Pobiera aplikacje dla pulpitu gracza (domyślne + zainstalowane)
     */
    public List<AppData> getDesktopApps(Player player) {
        PhoneUser user = plugin.getPhoneManager().getOrCreateUser(player.getUniqueId());
        List<AppData> apps = new ArrayList<>(defaultApps.values());
        
        // Dodaj zainstalowane aplikacje ze sklepu
        for (String appId : user.getInstalledApps()) {
            if (storeApps.containsKey(appId)) {
                apps.add(storeApps.get(appId));
            }
        }
        
        // Sortuj po pozycji
        apps.sort(Comparator.comparingInt(AppData::getPosition));
        
        return apps;
    }

    /**
     * Pobiera aplikacje dostępne w sklepie
     */
    public List<AppData> getStoreApps() {
        return new ArrayList<>(storeApps.values());
    }

    /**
     * Pobiera aplikacje zewnętrzne (z API)
     */
    public List<PhoneApp> getExternalApps() {
        return new ArrayList<>(registeredApps.values());
    }

    /**
     * Instaluje aplikację dla gracza
     */
    public boolean installApp(Player player, String appId) {
        if (!storeApps.containsKey(appId) && !registeredApps.containsKey(appId)) {
            return false;
        }
        
        PhoneUser user = plugin.getPhoneManager().getOrCreateUser(player.getUniqueId());
        
        if (user.hasAppInstalled(appId)) {
            return false;
        }
        
        user.installApp(appId);
        plugin.getPhoneManager().saveUser(user);
        
        // Wywołaj callback instalacji
        if (registeredApps.containsKey(appId)) {
            registeredApps.get(appId).onInstall(player);
        }
        
        return true;
    }

    /**
     * Odinstalowuje aplikację
     */
    public boolean uninstallApp(Player player, String appId) {
        // Nie można odinstalować wbudowanych aplikacji
        AppData appData = defaultApps.get(appId);
        if (appData != null && appData.isBuiltIn()) {
            return false;
        }
        
        PhoneUser user = plugin.getPhoneManager().getUser(player.getUniqueId());
        if (user == null || !user.hasAppInstalled(appId)) {
            return false;
        }
        
        user.uninstallApp(appId);
        plugin.getPhoneManager().saveUser(user);
        
        // Wywołaj callback odinstalowania
        if (registeredApps.containsKey(appId)) {
            registeredApps.get(appId).onUninstall(player);
        }
        
        return true;
    }

    /**
     * Otwiera aplikację
     */
    public void openApp(Player player, String appId) {
        // Sprawdź zewnętrzne aplikacje
        if (registeredApps.containsKey(appId)) {
            registeredApps.get(appId).onOpen(player);
            return;
        }
        
        // Wbudowane aplikacje są obsługiwane przez GUIListener
    }

    /**
     * Tworzy ikonę aplikacji
     */
    public ItemStack createAppIcon(AppData app) {
        return new ItemBuilder(app.getMaterial())
            .name(app.getName())
            .lore(app.getLore())
            .customModelData(app.getCustomModelData())
            .data("app_id", app.getId())
            .build();
    }

    /**
     * Tworzy ikonę zewnętrznej aplikacji
     */
    public ItemStack createAppIcon(PhoneApp app) {
        if (app.getIcon() != null) {
            return app.getIcon();
        }
        return new ItemBuilder(Material.PAPER)
            .name(app.getName())
            .lore(app.getLore())
            .customModelData(app.getCustomModelData())
            .data("app_id", app.getId())
            .build();
    }

    /**
     * Pobiera dane aplikacji po ID
     */
    public AppData getAppData(String appId) {
        if (defaultApps.containsKey(appId)) {
            return defaultApps.get(appId);
        }
        return storeApps.get(appId);
    }

    /**
     * Tworzy wrapper PhoneApp dla AppData
     */
    private PhoneApp createAppWrapper(final AppData data) {
        return new PhoneApp() {
            @Override
            public String getId() { return data.getId(); }
            @Override
            public String getName() { return data.getName(); }
            @Override
            public String getDescription() { return data.getDescription(); }
            @Override
            public String getCategory() { return data.getCategory(); }
            @Override
            public ItemStack getIcon() { return createAppIcon(data); }
            @Override
            public void onOpen(Player player) { openApp(player, data.getId()); }
            @Override
            public double getPrice() { return data.getPrice(); }
            @Override
            public boolean isBuiltIn() { return data.isBuiltIn(); }
            @Override
            public List<String> getLore() { return data.getLore(); }
            @Override
            public int getPosition() { return data.getPosition(); }
            @Override
            public int getCustomModelData() { return data.getCustomModelData(); }
        };
    }
}
