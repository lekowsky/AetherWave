package pl.skyrise.mCPhone.utils;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import pl.skyrise.mCPhone.MCPhone;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Builder do tworzenia przedmiotów
 */
public class ItemBuilder {

    private final ItemStack item;
    private final ItemMeta meta;

    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder(ItemStack item) {
        this.item = item.clone();
        this.meta = this.item.getItemMeta();
    }

    public ItemBuilder name(String name) {
        meta.displayName(ColorUtils.toComponent(name));
        return this;
    }

    public ItemBuilder lore(String... lore) {
        meta.lore(Arrays.stream(lore)
            .map(ColorUtils::toComponent)
            .collect(Collectors.toList()));
        return this;
    }

    public ItemBuilder lore(List<String> lore) {
        meta.lore(lore.stream()
            .map(ColorUtils::toComponent)
            .collect(Collectors.toList()));
        return this;
    }

    public ItemBuilder addLore(String line) {
        List<net.kyori.adventure.text.Component> currentLore = meta.lore();
        if (currentLore == null) {
            currentLore = new ArrayList<>();
        }
        currentLore.add(ColorUtils.toComponent(line));
        meta.lore(currentLore);
        return this;
    }

    public ItemBuilder amount(int amount) {
        item.setAmount(amount);
        return this;
    }

    public ItemBuilder customModelData(int data) {
        if (data > 0) {
            meta.setCustomModelData(data);
        }
        return this;
    }

    public ItemBuilder unbreakable(boolean unbreakable) {
        meta.setUnbreakable(unbreakable);
        return this;
    }

    public ItemBuilder flags(ItemFlag... flags) {
        meta.addItemFlags(flags);
        return this;
    }

    public ItemBuilder hideFlags() {
        meta.addItemFlags(ItemFlag.values());
        return this;
    }

    public ItemBuilder enchant(Enchantment enchantment, int level) {
        meta.addEnchant(enchantment, level, true);
        return this;
    }

    public ItemBuilder glow() {
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        return this;
    }

    /**
     * Dodaje dane do przedmiotu używając PersistentDataContainer
     */
    public ItemBuilder data(String key, String value) {
        NamespacedKey nsKey = new NamespacedKey(MCPhone.getInstance(), key);
        meta.getPersistentDataContainer().set(nsKey, PersistentDataType.STRING, value);
        return this;
    }

    public ItemBuilder data(String key, int value) {
        NamespacedKey nsKey = new NamespacedKey(MCPhone.getInstance(), key);
        meta.getPersistentDataContainer().set(nsKey, PersistentDataType.INTEGER, value);
        return this;
    }

    /**
     * Oznacza przedmiot jako telefon
     */
    public ItemBuilder asPhone() {
        return data("mcphone_item", "phone");
    }

    /**
     * Oznacza przedmiot jako kartę SIM
     */
    public ItemBuilder asSimCard(String typeId, String phoneNumber) {
        return data("mcphone_item", "sim")
            .data("sim_type", typeId)
            .data("sim_number", phoneNumber);
    }

    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }

    // ============================================
    // Statyczne metody pomocnicze
    // ============================================

    /**
     * Sprawdza czy przedmiot jest telefonem MCPhone
     */
    public static boolean isPhone(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        NamespacedKey key = new NamespacedKey(MCPhone.getInstance(), "mcphone_item");
        String value = item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
        return "phone".equals(value);
    }

    /**
     * Sprawdza czy przedmiot jest kartą SIM
     */
    public static boolean isSimCard(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        NamespacedKey key = new NamespacedKey(MCPhone.getInstance(), "mcphone_item");
        String value = item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
        return "sim".equals(value);
    }

    /**
     * Pobiera typ karty SIM z przedmiotu
     */
    public static String getSimType(ItemStack item) {
        if (!isSimCard(item)) return null;
        NamespacedKey key = new NamespacedKey(MCPhone.getInstance(), "sim_type");
        return item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
    }

    /**
     * Pobiera numer telefonu z karty SIM
     */
    public static String getSimNumber(ItemStack item) {
        if (!isSimCard(item)) return null;
        NamespacedKey key = new NamespacedKey(MCPhone.getInstance(), "sim_number");
        return item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
    }

    /**
     * Pobiera dane z przedmiotu
     */
    public static String getData(ItemStack item, String key) {
        if (item == null || !item.hasItemMeta()) return null;
        NamespacedKey nsKey = new NamespacedKey(MCPhone.getInstance(), key);
        return item.getItemMeta().getPersistentDataContainer().get(nsKey, PersistentDataType.STRING);
    }
}
