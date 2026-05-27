package pl.skyrise.mCPhone;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import pl.skyrise.mCPhone.api.MCPhoneAPI;
import pl.skyrise.mCPhone.api.MCPhoneProvider;
import pl.skyrise.mCPhone.commands.PhoneAdminCommand;
import pl.skyrise.mCPhone.commands.PhoneCommand;
import pl.skyrise.mCPhone.commands.SMSCommand;
import pl.skyrise.mCPhone.config.*;
import pl.skyrise.mCPhone.database.DatabaseManager;
import pl.skyrise.mCPhone.listeners.GUIListener;
import pl.skyrise.mCPhone.listeners.PhoneListener;
import pl.skyrise.mCPhone.listeners.SimListener;
import pl.skyrise.mCPhone.managers.*;

public class MCPhone extends JavaPlugin {

    private static MCPhone instance;
    
    // Konfiguracja
    private ConfigManager configManager;
    private MessagesConfig messagesConfig;
    private GuiConfig guiConfig;
    private AppsConfig appsConfig;
    private SimConfig simConfig;
    
    // Baza danych
    private DatabaseManager databaseManager;
    
    // Managery
    private PhoneManager phoneManager;
    private SMSManager smsManager;
    private ContactManager contactManager;
    private AppManager appManager;
    private SimManager simManager;
    private NotificationManager notificationManager;
    
    // Ekonomia (opcjonalna)
    private Economy economy;
    private boolean economyEnabled = false;

    @Override
    public void onEnable() {
        instance = this;
        
        // Logo w konsoli
        getLogger().info("═══════════════════════════════════════════");
        getLogger().info("   MCPhone v" + getDescription().getVersion());
        getLogger().info("   Autor: SkyRise");
        getLogger().info("   Platforma: Purpur 1.21.10");
        getLogger().info("═══════════════════════════════════════════");
        
        // Ładowanie konfiguracji
        loadConfigs();
        
        // Inicjalizacja bazy danych
        initDatabase();
        
        // Inicjalizacja managerów
        initManagers();
        
        // Rejestracja listenerów
        registerListeners();
        
        // Rejestracja komend
        registerCommands();
        
        // Hookowanie do Vault (opcjonalne)
        setupEconomy();
        
        // Inicjalizacja API
        MCPhoneProvider.register(createAPI());
        
        // Auto-save task
        startAutoSave();
        
        getLogger().info("MCPhone został pomyślnie włączony!");
    }

    @Override
    public void onDisable() {
        // Zapisanie danych
        if (databaseManager != null) {
            databaseManager.saveAll();
            databaseManager.close();
        }
        
        // Wyrejestrowanie API
        MCPhoneProvider.unregister();
        
        getLogger().info("MCPhone został wyłączony!");
    }
    
    private void loadConfigs() {
        // Zapisanie domyślnych plików konfiguracyjnych
        saveDefaultConfig();
        saveResource("messages.yml", false);
        saveResource("gui.yml", false);
        saveResource("apps.yml", false);
        saveResource("simcards.yml", false);
        
        // Ładowanie konfiguracji
        configManager = new ConfigManager(this);
        messagesConfig = new MessagesConfig(this);
        guiConfig = new GuiConfig(this);
        appsConfig = new AppsConfig(this);
        simConfig = new SimConfig(this);
        
        getLogger().info("Załadowano pliki konfiguracyjne.");
    }
    
    private void initDatabase() {
        databaseManager = new DatabaseManager(this);
        databaseManager.init();
        getLogger().info("Zainicjalizowano bazę danych (" + configManager.getDatabaseType() + ").");
    }
    
    private void initManagers() {
        phoneManager = new PhoneManager(this);
        contactManager = new ContactManager(this);
        smsManager = new SMSManager(this);
        appManager = new AppManager(this);
        simManager = new SimManager(this);
        notificationManager = new NotificationManager(this);
        
        // Ładowanie danych
        phoneManager.loadAll();
        appManager.loadDefaultApps();
        appManager.loadStoreApps();
        simManager.loadSimShops();
        
        getLogger().info("Zainicjalizowano managery.");
    }
    
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PhoneListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        getServer().getPluginManager().registerEvents(new SimListener(this), this);
        getLogger().info("Zarejestrowano listenery.");
    }
    
    private void registerCommands() {
        getCommand("phone").setExecutor(new PhoneCommand(this));
        getCommand("sms").setExecutor(new SMSCommand(this));
        getCommand("phoneadmin").setExecutor(new PhoneAdminCommand(this));
        getLogger().info("Zarejestrowano komendy.");
    }
    
    private void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().info("Vault nie został znaleziony - ekonomia wyłączona.");
            return;
        }
        
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            getLogger().info("Nie znaleziono pluginu ekonomii - ekonomia wyłączona.");
            return;
        }
        
        economy = rsp.getProvider();
        economyEnabled = true;
        getLogger().info("Połączono z Vault - ekonomia włączona.");
    }
    
    private MCPhoneAPI createAPI() {
        return new MCPhoneAPIImpl(this);
    }
    
    private void startAutoSave() {
        int interval = configManager.getAutoSaveInterval() * 20; // sekundy na ticki
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            if (databaseManager != null) {
                databaseManager.saveAll();
                if (configManager.isDebug()) {
                    getLogger().info("[Debug] Auto-save wykonany.");
                }
            }
        }, interval, interval);
    }
    
    public void reload() {
        reloadConfig();
        configManager.reload();
        messagesConfig.reload();
        guiConfig.reload();
        appsConfig.reload();
        simConfig.reload();
        
        appManager.loadDefaultApps();
        appManager.loadStoreApps();
        simManager.loadSimShops();
        
        getLogger().info("Konfiguracja została przeładowana.");
    }
    
    // Gettery
    public static MCPhone getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public MessagesConfig getMessagesConfig() {
        return messagesConfig;
    }
    
    public GuiConfig getGuiConfig() {
        return guiConfig;
    }
    
    public AppsConfig getAppsConfig() {
        return appsConfig;
    }
    
    public SimConfig getSimConfig() {
        return simConfig;
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    public PhoneManager getPhoneManager() {
        return phoneManager;
    }
    
    public SMSManager getSmsManager() {
        return smsManager;
    }
    
    public ContactManager getContactManager() {
        return contactManager;
    }
    
    public AppManager getAppManager() {
        return appManager;
    }
    
    public SimManager getSimManager() {
        return simManager;
    }
    
    public NotificationManager getNotificationManager() {
        return notificationManager;
    }
    
    public Economy getEconomy() {
        return economy;
    }
    
    public boolean isEconomyEnabled() {
        return economyEnabled;
    }
}
