package pl.skyrise.mCPhone.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import pl.skyrise.mCPhone.MCPhone;
import pl.skyrise.mCPhone.config.GuiConfig;
import pl.skyrise.mCPhone.models.PhoneUser;
import pl.skyrise.mCPhone.models.SimType;
import pl.skyrise.mCPhone.utils.ColorUtils;
import pl.skyrise.mCPhone.utils.ItemBuilder;

/**
 * GUI ustawień telefonu
 */
public class SettingsGUI implements InventoryHolder {

    private final MCPhone plugin;
    private final Player player;
    private final PhoneUser user;
    private final Inventory inventory;

    public SettingsGUI(MCPhone plugin, Player player, PhoneUser user) {
        this.plugin = plugin;
        this.player = player;
        this.user = user;

        GuiConfig guiConfig = plugin.getGuiConfig();
        this.inventory = Bukkit.createInventory(this, guiConfig.getPhoneGuiSize(),
                guiConfig.getTitleComponent());

        setupGUI();
    }

    private void setupGUI() {
        inventory.clear();
        GuiConfig guiConfig = plugin.getGuiConfig();

        // Ramka telefonu - tylko gdy nie używamy Nexo
        if (!guiConfig.useNexo()) {
            ItemStack frame = new ItemBuilder(guiConfig.getFrameMaterial())
                    .name(guiConfig.getFrameDisplayName())
                    .customModelData(guiConfig.getFrameCustomModelData())
                    .build();
            for (int slot : guiConfig.getFrameSlots()) {
                if (slot >= 0 && slot < inventory.getSize()) {
                    inventory.setItem(slot, frame);
                }
            }
        }

        // Informacje o karcie SIM
        String phoneNumber = user.getPhoneNumber() != null ? user.getPhoneNumber() : "Brak";
        String simTypeName = "Brak";
        String simStatus = user.hasActiveSim() ? "&aAktywna" : "&cNieaktywna";

        if (user.getSimType() != null) {
            SimType simType = plugin.getSimManager().getSimType(user.getSimType());
            if (simType != null) {
                simTypeName = simType.getName();
            }
        }

        java.util.List<String> simLore = new java.util.ArrayList<>();
        simLore.add("&7Typ: &f" + simTypeName);
        simLore.add("&7Status: " + simStatus);
        simLore.add("&7Numer: &f" + phoneNumber);
        simLore.add("");
        simLore.add("&7SMS dzisiaj: &f" + user.getDailySmsCount());

        if (!user.hasActiveSim()) {
            simLore.add("");
            simLore.add("&eKliknij aby aktywować kartę SIM");
            simLore.add("&7(musisz mieć kartę SIM w ekwipunku)");
        }

        // Sloty ekranu: 3,4,5, 12,13,14, 21,22,23, 30,31,32, 39,40,41

        // Rząd 1 - Karta SIM, Powiadomienia, O telefonie
        ItemStack simInfo = new ItemBuilder(Material.PAPER)
                .name("&b📱 Karta SIM")
                .lore(simLore)
                .data("action", "activate_sim")
                .build();
        inventory.setItem(3, simInfo);

        int unreadNotifications = plugin.getNotificationManager().getUnreadCount(player.getUniqueId());
        ItemStack notifications = new ItemBuilder(Material.BELL)
                .name("&e🔔 Powiadomienia")
                .lore(
                        "&7Nieprzeczytane: &f" + unreadNotifications,
                        "",
                        "&8Kliknij aby zobaczyć"
                )
                .data("action", "notifications")
                .build();
        inventory.setItem(4, notifications);

        ItemStack phoneInfo = new ItemBuilder(Material.BOOK)
                .name("&f📋 O telefonie")
                .lore(
                        "&7Wersja: &fMCPhone " + plugin.getDescription().getVersion(),
                        "&7Platforma: &fPurpur 1.21.10",
                        "&7Autor: &fSkyRise",
                        "",
                        "&7Baza danych: &f" + plugin.getConfigManager().getDatabaseType()
                )
                .build();
        inventory.setItem(5, phoneInfo);

        // Rząd 2 - Kontakty, Wiadomości, Aplikacje
        int contactsCount = user.getContacts().size();
        ItemStack contacts = new ItemBuilder(Material.PLAYER_HEAD)
                .name("&f☎ Kontakty")
                .lore(
                        "&7Ilość kontaktów: &f" + contactsCount,
                        "",
                        "&8Kliknij aby otworzyć"
                )
                .data("action", "contacts")
                .build();
        inventory.setItem(12, contacts);

        int unreadSms = plugin.getSmsManager().getUnreadCount(player.getUniqueId());
        ItemStack messages = new ItemBuilder(Material.PAPER)
                .name("&f✉ Wiadomości")
                .lore(
                        "&7Nieprzeczytane: &f" + unreadSms,
                        "",
                        "&8Kliknij aby otworzyć"
                )
                .data("action", "messages")
                .build();
        inventory.setItem(13, messages);

        ItemStack manageApps = new ItemBuilder(Material.CHEST)
                .name("&d📦 Zainstalowane aplikacje")
                .lore(
                        "&7Ilość: &f" + user.getInstalledApps().size(),
                        "",
                        "&8Kliknij aby zarządzać"
                )
                .data("action", "manage_apps")
                .build();
        inventory.setItem(14, manageApps);

        // Rząd 3 - Wyczyść powiadomienia
        ItemStack clearNotifications = new ItemBuilder(Material.CAULDRON)
                .name("&c🗑 Wyczyść powiadomienia")
                .lore(
                        "&7Usuń wszystkie powiadomienia",
                        "",
                        "&cKliknij aby wyczyścić"
                )
                .data("action", "clear_notifications")
                .build();
        inventory.setItem(21, clearNotifications);

        // Nawigacja dolna
        // Lewy - powrót
        ItemStack backButton = new ItemBuilder(guiConfig.getNavLeftMaterial())
                .name("&7◄ Powrót")
                .lore("&8Kliknij aby wrócić")
                .data("nav_action", "back")
                .build();
        inventory.setItem(guiConfig.getNavLeftSlot(), backButton);

        // Środkowy - pulpit
        ItemStack homeButton = new ItemBuilder(guiConfig.getNavHomeMaterial())
                .name(guiConfig.getNavHomeDisplayName())
                .lore(guiConfig.getNavHomeLore())
                .data("nav_action", "home")
                .build();
        inventory.setItem(guiConfig.getNavHomeSlot(), homeButton);
    }

    public void open() {
        player.openInventory(inventory);
    }

    public void refresh() {
        setupGUI();
    }

    public Player getPlayer() {
        return player;
    }

    public PhoneUser getUser() {
        return user;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
