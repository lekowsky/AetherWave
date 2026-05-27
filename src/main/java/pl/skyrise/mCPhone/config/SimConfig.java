package pl.skyrise.mCPhone.config;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import pl.skyrise.mCPhone.MCPhone;
import pl.skyrise.mCPhone.models.SimType;
import pl.skyrise.mCPhone.models.SimShop;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Manager konfiguracji kart SIM (simcards.yml)
 */
public class SimConfig {

    private final MCPhone plugin;
    private File file;
    private FileConfiguration config;

    private final Map<String, SimType> simTypes = new HashMap<>();
    private final Map<String, SimShop> simShops = new HashMap<>();

    public SimConfig(MCPhone plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        file = new File(plugin.getDataFolder(), "simcards.yml");
        if (!file.exists()) {
            plugin.saveResource("simcards.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
        loadConfig();
    }

    private void loadConfig() {
        simTypes.clear();
        simShops.clear();

        // Ładowanie typów kart SIM
        ConfigurationSection typesSection = config.getConfigurationSection("sim-types");
        if (typesSection != null) {
            for (String key : typesSection.getKeys(false)) {
                ConfigurationSection typeSection = typesSection.getConfigurationSection(key);
                if (typeSection != null) {
                    SimType type = loadSimType(typeSection);
                    simTypes.put(type.getId(), type);
                }
            }
        }

        // Ładowanie punktów sprzedaży
        ConfigurationSection shopsSection = config.getConfigurationSection("sim-shops");
        if (shopsSection != null) {
            for (String key : shopsSection.getKeys(false)) {
                ConfigurationSection shopSection = shopsSection.getConfigurationSection(key);
                if (shopSection != null) {
                    SimShop shop = loadSimShop(key, shopSection);
                    if (shop != null) {
                        simShops.put(shop.getId(), shop);
                    }
                }
            }
        }
    }

    private SimType loadSimType(ConfigurationSection section) {
        return new SimType(
            section.getString("id", "unknown"),
            section.getString("name", "&fKarta SIM"),
            section.getString("description", "Brak opisu"),
            getMaterial(section.getString("material", "PAPER")),
            section.getInt("custom-model-data", 0),
            section.getString("color", "&f"),
            section.getDouble("price", 0),
            section.getInt("daily-sms-limit", 50),
            section.getBoolean("calls-enabled", false),
            section.getBoolean("internet-enabled", false),
            section.getStringList("lore")
        );
    }

    private SimShop loadSimShop(String id, ConfigurationSection section) {
        String worldName = section.getString("world");
        if (worldName == null) return null;

        org.bukkit.World world = plugin.getServer().getWorld(worldName);
        if (world == null) return null;

        Location location = new Location(
            world,
            section.getDouble("x"),
            section.getDouble("y"),
            section.getDouble("z")
        );

        List<String> availableSims = section.getStringList("available-sims");
        if (availableSims.isEmpty()) {
            availableSims = new ArrayList<>(simTypes.keySet());
        }

        return new SimShop(
            id,
            section.getString("name", "&ePunkt Sprzedaży SIM"),
            location,
            section.getInt("radius", 3),
            availableSims
        );
    }

    private Material getMaterial(String name) {
        try {
            return Material.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Material.PAPER;
        }
    }

    public void saveShop(SimShop shop) {
        String path = "sim-shops." + shop.getId();
        config.set(path + ".name", shop.getName());
        config.set(path + ".world", shop.getLocation().getWorld().getName());
        config.set(path + ".x", shop.getLocation().getX());
        config.set(path + ".y", shop.getLocation().getY());
        config.set(path + ".z", shop.getLocation().getZ());
        config.set(path + ".radius", shop.getRadius());
        config.set(path + ".available-sims", shop.getAvailableSims());

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Nie można zapisać pliku simcards.yml: " + e.getMessage());
        }

        simShops.put(shop.getId(), shop);
    }

    public void removeShop(String id) {
        config.set("sim-shops." + id, null);
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Nie można zapisać pliku simcards.yml: " + e.getMessage());
        }
        simShops.remove(id);
    }

    public Map<String, SimType> getSimTypes() {
        return new HashMap<>(simTypes);
    }

    public SimType getSimType(String id) {
        return simTypes.get(id);
    }

    public Map<String, SimShop> getSimShops() {
        return new HashMap<>(simShops);
    }

    public SimShop getSimShop(String id) {
        return simShops.get(id);
    }

    public SimShop getNearestShop(Location location, double maxDistance) {
        SimShop nearest = null;
        double nearestDistance = maxDistance;

        for (SimShop shop : simShops.values()) {
            if (!shop.getLocation().getWorld().equals(location.getWorld())) continue;

            double distance = shop.getLocation().distance(location);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = shop;
            }
        }

        return nearest;
    }

    public List<SimType> getAllSimTypes() {
        return new ArrayList<>(simTypes.values());
    }
}
