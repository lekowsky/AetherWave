package pl.skyrise.mCPhone.managers;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.skyrise.mCPhone.MCPhone;
import pl.skyrise.mCPhone.models.PhoneUser;
import pl.skyrise.mCPhone.models.SimShop;
import pl.skyrise.mCPhone.models.SimType;
import pl.skyrise.mCPhone.utils.ItemBuilder;
import pl.skyrise.mCPhone.utils.NumberGenerator;

import java.util.*;

/**
 * Manager kart SIM
 */
public class SimManager {

    private final MCPhone plugin;
    private final Map<String, SimShop> simShops = new HashMap<>();

    public SimManager(MCPhone plugin) {
        this.plugin = plugin;
    }

    /**
     * Ładuje punkty sprzedaży z konfiguracji
     */
    public void loadSimShops() {
        simShops.clear();
        simShops.putAll(plugin.getSimConfig().getSimShops());
        plugin.getLogger().info("Załadowano " + simShops.size() + " punktów sprzedaży SIM.");
    }

    /**
     * Pobiera typ karty SIM
     */
    public SimType getSimType(String typeId) {
        return plugin.getSimConfig().getSimType(typeId);
    }

    /**
     * Pobiera wszystkie typy kart SIM
     */
    public List<SimType> getAllSimTypes() {
        return plugin.getSimConfig().getAllSimTypes();
    }

    /**
     * Tworzy przedmiot karty SIM
     */
    public ItemStack createSimCard(String typeId) {
        SimType type = getSimType(typeId);
        if (type == null) return null;

        String phoneNumber = NumberGenerator.generateUnique();

        return new ItemBuilder(type.getMaterial())
            .name(type.getName())
            .lore(type.getLore())
            .customModelData(type.getCustomModelData())
            .asSimCard(typeId, phoneNumber)
            .build();
    }

    /**
     * Tworzy przedmiot karty SIM z określonym numerem
     */
    public ItemStack createSimCard(String typeId, String phoneNumber) {
        SimType type = getSimType(typeId);
        if (type == null) return null;

        return new ItemBuilder(type.getMaterial())
            .name(type.getName())
            .lore(type.getLore())
            .customModelData(type.getCustomModelData())
            .asSimCard(typeId, phoneNumber)
            .build();
    }

    /**
     * Daje graczowi kartę SIM
     */
    public void giveSimCard(Player player, String typeId) {
        ItemStack simCard = createSimCard(typeId);
        if (simCard != null) {
            player.getInventory().addItem(simCard);
        }
    }

    /**
     * Aktywuje kartę SIM dla gracza
     */
    public boolean activateSim(Player player, String typeId, String phoneNumber) {
        PhoneUser user = plugin.getPhoneManager().getOrCreateUser(player.getUniqueId());

        // Sprawdź czy numer jest zajęty
        if (plugin.getPhoneManager().isNumberTaken(phoneNumber)) {
            // Numer jest zajęty przez kogoś innego
            UUID currentOwner = plugin.getPhoneManager().getPlayerByNumber(phoneNumber);
            if (!currentOwner.equals(player.getUniqueId())) {
                return false;
            }
        }

        user.setSimType(typeId);
        user.setPhoneNumber(phoneNumber);
        user.setSimActive(true);
        user.resetSmsCount();

        plugin.getPhoneManager().saveUser(user);
        return true;
    }

    /**
     * Dezaktywuje kartę SIM gracza
     */
    public void deactivateSim(Player player) {
        PhoneUser user = plugin.getPhoneManager().getUser(player.getUniqueId());
        if (user == null) return;

        user.setSimActive(false);
        plugin.getPhoneManager().saveUser(user);
    }

    /**
     * Sprawdza czy gracz ma aktywną kartę SIM
     */
    public boolean hasActiveSim(UUID playerUUID) {
        PhoneUser user = plugin.getPhoneManager().getUser(playerUUID);
        return user != null && user.hasActiveSim();
    }

    /**
     * Pobiera typ aktywnej karty SIM gracza
     */
    public SimType getActiveSimType(UUID playerUUID) {
        PhoneUser user = plugin.getPhoneManager().getUser(playerUUID);
        if (user == null || !user.hasActiveSim()) return null;
        return getSimType(user.getSimType());
    }

    /**
     * Dodaje punkt sprzedaży SIM
     */
    public void addSimShop(String id, String name, Location location, int radius, List<String> availableSims) {
        SimShop shop = new SimShop(id, name, location, radius, availableSims);
        simShops.put(id, shop);
        plugin.getSimConfig().saveShop(shop);
    }

    /**
     * Usuwa punkt sprzedaży SIM
     */
    public void removeSimShop(String id) {
        simShops.remove(id);
        plugin.getSimConfig().removeShop(id);
    }

    /**
     * Pobiera punkt sprzedaży po ID
     */
    public SimShop getSimShop(String id) {
        return simShops.get(id);
    }

    /**
     * Pobiera wszystkie punkty sprzedaży
     */
    public Collection<SimShop> getAllSimShops() {
        return simShops.values();
    }

    /**
     * Pobiera najbliższy punkt sprzedaży
     */
    public SimShop getNearestShop(Location location, double maxDistance) {
        SimShop nearest = null;
        double nearestDist = maxDistance;

        for (SimShop shop : simShops.values()) {
            if (!shop.getLocation().getWorld().equals(location.getWorld())) continue;

            double dist = shop.getLocation().distance(location);
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = shop;
            }
        }

        return nearest;
    }

    /**
     * Sprawdza czy gracz jest w zasięgu punktu sprzedaży
     */
    public SimShop getShopInRange(Player player) {
        for (SimShop shop : simShops.values()) {
            if (shop.isInRange(player.getLocation())) {
                return shop;
            }
        }
        return null;
    }

    /**
     * Kupuje kartę SIM dla gracza (z ekonomią)
     */
    public boolean purchaseSim(Player player, String typeId) {
        SimType type = getSimType(typeId);
        if (type == null) return false;

        double price = type.getPrice();

        // Sprawdź ekonomię
        if (plugin.isEconomyEnabled() && price > 0) {
            if (!plugin.getEconomy().has(player, price)) {
                player.sendMessage(plugin.getMessagesConfig().simNoMoney(String.valueOf(price)));
                return false;
            }
            plugin.getEconomy().withdrawPlayer(player, price);
        }

        giveSimCard(player, typeId);
        player.sendMessage(plugin.getMessagesConfig().simPurchased(type.getName()));
        return true;
    }
}
