package pl.skyrise.mCPhone.models;

import org.bukkit.Material;

import java.util.List;

/**
 * Model typu karty SIM (z konfiguracji)
 */
public class SimType {

    private final String id;
    private final String name;
    private final String description;
    private final Material material;
    private final int customModelData;
    private final String color;
    private final double price;
    private final int dailySmsLimit; // -1 = bez limitu
    private final boolean callsEnabled;
    private final boolean internetEnabled;
    private final List<String> lore;

    public SimType(String id, String name, String description, Material material, int customModelData,
                   String color, double price, int dailySmsLimit, boolean callsEnabled,
                   boolean internetEnabled, List<String> lore) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.material = material;
        this.customModelData = customModelData;
        this.color = color;
        this.price = price;
        this.dailySmsLimit = dailySmsLimit;
        this.callsEnabled = callsEnabled;
        this.internetEnabled = internetEnabled;
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

    public String getColor() {
        return color;
    }

    public double getPrice() {
        return price;
    }

    public int getDailySmsLimit() {
        return dailySmsLimit;
    }

    public boolean hasUnlimitedSms() {
        return dailySmsLimit < 0;
    }

    public boolean areCallsEnabled() {
        return callsEnabled;
    }

    public boolean isInternetEnabled() {
        return internetEnabled;
    }

    public List<String> getLore() {
        return lore;
    }

    @Override
    public String toString() {
        return "SimType{id='" + id + "', name='" + name + "', price=" + price + "}";
    }
}
