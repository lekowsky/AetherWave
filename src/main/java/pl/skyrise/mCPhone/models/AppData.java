package pl.skyrise.mCPhone.models;

import org.bukkit.Material;

import java.util.List;

/**
 * Dane aplikacji z konfiguracji
 */
public class AppData {

    private final String id;
    private final String name;
    private final String description;
    private final Material material;
    private final int customModelData;
    private final int position;
    private final String category;
    private final boolean builtIn;
    private final double price;
    private final List<String> lore;

    public AppData(String id, String name, String description, Material material, int customModelData,
                   int position, String category, boolean builtIn, double price, List<String> lore) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.material = material;
        this.customModelData = customModelData;
        this.position = position;
        this.category = category;
        this.builtIn = builtIn;
        this.price = price;
        this.lore = lore;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Material getMaterial() {
        return material;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public int getPosition() {
        return position;
    }

    public String getCategory() {
        return category;
    }

    public boolean isBuiltIn() {
        return builtIn;
    }

    public double getPrice() {
        return price;
    }

    public boolean isFree() {
        return price <= 0;
    }

    public List<String> getLore() {
        return lore;
    }

    @Override
    public String toString() {
        return "AppData{id='" + id + "', name='" + name + "'}";
    }
}
