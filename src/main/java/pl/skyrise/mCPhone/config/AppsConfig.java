package pl.skyrise.mCPhone.config;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import pl.skyrise.mCPhone.MCPhone;
import pl.skyrise.mCPhone.models.AppData;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manager konfiguracji aplikacji (apps.yml)
 */
public class AppsConfig {

    private final MCPhone plugin;
    private File file;
    private FileConfiguration config;

    private final Map<String, AppData> defaultApps = new HashMap<>();
    private final Map<String, AppData> storeApps = new HashMap<>();
    private final Map<String, CategoryData> categories = new HashMap<>();

    public AppsConfig(MCPhone plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        file = new File(plugin.getDataFolder(), "apps.yml");
        if (!file.exists()) {
            plugin.saveResource("apps.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
        loadConfig();
    }

    private void loadConfig() {
        defaultApps.clear();
        storeApps.clear();
        categories.clear();

        // Ładowanie domyślnych aplikacji
        ConfigurationSection defaultAppsSection = config.getConfigurationSection("default-apps");
        if (defaultAppsSection != null) {
            for (String key : defaultAppsSection.getKeys(false)) {
                ConfigurationSection appSection = defaultAppsSection.getConfigurationSection(key);
                if (appSection != null) {
                    AppData app = loadAppData(appSection, true);
                    defaultApps.put(app.getId(), app);
                }
            }
        }

        // Ładowanie aplikacji ze sklepu
        ConfigurationSection storeAppsSection = config.getConfigurationSection("store-apps");
        if (storeAppsSection != null) {
            for (String key : storeAppsSection.getKeys(false)) {
                ConfigurationSection appSection = storeAppsSection.getConfigurationSection(key);
                if (appSection != null) {
                    AppData app = loadAppData(appSection, false);
                    storeApps.put(app.getId(), app);
                }
            }
        }

        // Ładowanie kategorii
        ConfigurationSection categoriesSection = config.getConfigurationSection("categories");
        if (categoriesSection != null) {
            for (String key : categoriesSection.getKeys(false)) {
                ConfigurationSection catSection = categoriesSection.getConfigurationSection(key);
                if (catSection != null) {
                    CategoryData category = new CategoryData(
                        catSection.getString("id", key),
                        catSection.getString("name", key),
                        getMaterial(catSection.getString("material", "NETHER_STAR"))
                    );
                    categories.put(category.getId(), category);
                }
            }
        }
    }

    private AppData loadAppData(ConfigurationSection section, boolean builtIn) {
        return new AppData(
            section.getString("id", "unknown"),
            section.getString("name", "&fAplikacja"),
            section.getString("description", "Brak opisu"),
            getMaterial(section.getString("material", "STONE")),
            section.getInt("custom-model-data", 0),
            section.getInt("position", -1),
            section.getString("category", "other"),
            builtIn || section.getBoolean("built-in", false),
            section.getDouble("price", 0),
            section.getStringList("lore")
        );
    }

    private Material getMaterial(String name) {
        try {
            return Material.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Material.STONE;
        }
    }

    public Map<String, AppData> getDefaultApps() {
        return new HashMap<>(defaultApps);
    }

    public Map<String, AppData> getStoreApps() {
        return new HashMap<>(storeApps);
    }

    public Map<String, CategoryData> getCategories() {
        return new HashMap<>(categories);
    }

    public AppData getApp(String id) {
        if (defaultApps.containsKey(id)) {
            return defaultApps.get(id);
        }
        return storeApps.get(id);
    }

    public List<AppData> getAllApps() {
        List<AppData> all = new ArrayList<>(defaultApps.values());
        all.addAll(storeApps.values());
        return all;
    }

    /**
     * Dane kategorii aplikacji
     */
    public static class CategoryData {
        private final String id;
        private final String name;
        private final Material material;

        public CategoryData(String id, String name, Material material) {
            this.id = id;
            this.name = name;
            this.material = material;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public Material getMaterial() { return material; }
    }
}
