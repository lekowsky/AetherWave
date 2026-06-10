package pl.skyrise.vendingMachine;

import org.bukkit.plugin.java.JavaPlugin;
import pl.skyrise.vendingMachine.command.VendingCommand;
import pl.skyrise.vendingMachine.listener.ChatInputListener;
import pl.skyrise.vendingMachine.listener.GUIListener;
import pl.skyrise.vendingMachine.listener.InteractListener;
import pl.skyrise.vendingMachine.listener.NexoListener;
import pl.skyrise.vendingMachine.manager.*;
import pl.skyrise.vendingMachine.util.ColorUtil;

import java.util.logging.Level;

public class VendingMachine extends JavaPlugin {

    private static VendingMachine instance;
    private MachineManager machineManager;
    private PlacementManager placementManager;
    private DataManager dataManager;
    private EconomyManager economyManager;
    private NexoManager nexoManager;
    private RestockManager restockManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        economyManager = new EconomyManager(this);
        if (!economyManager.setupEconomy()) {
            getLogger().log(Level.SEVERE, "Vault economy not found! Disabling.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        dataManager = new DataManager(this);
        machineManager = new MachineManager(this);
        placementManager = new PlacementManager(this);
        nexoManager = new NexoManager(this);
        restockManager = new RestockManager(this);

        dataManager.loadTemplates();
        dataManager.loadPlacements();

        // Uruchom timery restocku
        restockManager.startAll();

        VendingCommand cmd = new VendingCommand(this);
        getCommand("vendingmachine").setExecutor(cmd);
        getCommand("vendingmachine").setTabCompleter(cmd);

        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        getServer().getPluginManager().registerEvents(new InteractListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatInputListener(this), this);

        if (nexoManager.isNexoAvailable()) {
            try {
                getServer().getPluginManager().registerEvents(new NexoListener(this), this);
                getLogger().info("Nexo integration enabled!");
            } catch (Throwable t) {
                getLogger().warning("Failed to register Nexo listener: " + t.getMessage());
            }
        }

        getLogger().info("VendingMachine v2.1 enabled! Templates: " +
                machineManager.getAllTemplates().size() + ", Placements: " +
                placementManager.getAllPlacements().size());
    }

    @Override
    public void onDisable() {
        if (restockManager != null) restockManager.stopAll();
        if (dataManager != null) dataManager.saveAll();
        getLogger().info("VendingMachine disabled!");
    }

    public static VendingMachine getInstance() { return instance; }
    public MachineManager getMachineManager() { return machineManager; }
    public PlacementManager getPlacementManager() { return placementManager; }
    public DataManager getDataManager() { return dataManager; }
    public EconomyManager getEconomyManager() { return economyManager; }
    public NexoManager getNexoManager() { return nexoManager; }
    public RestockManager getRestockManager() { return restockManager; }

    public String getPrefix() {
        return ColorUtil.color(getConfig().getString("prefix", "&8[&6VM&8] &r"));
    }
}