package pl.skyrise.mCPhone.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import pl.skyrise.mCPhone.MCPhone;
import pl.skyrise.mCPhone.gui.*;
import pl.skyrise.mCPhone.models.AppData;
import pl.skyrise.mCPhone.models.PhoneUser;
import pl.skyrise.mCPhone.utils.ColorUtils;
import pl.skyrise.mCPhone.utils.ItemBuilder;

import java.util.UUID;

/**
 * Listener obsługujący kliknięcia w GUI
 */
public class GUIListener implements Listener {

    private final MCPhone plugin;

    public GUIListener(MCPhone plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        InventoryHolder holder = event.getInventory().getHolder();
        if (holder == null) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) return;

        // Obsługa różnych GUI
        if (holder instanceof PhoneGUI gui) {
            event.setCancelled(true);
            handlePhoneGUI(player, gui, clicked, event.getSlot());
        } else if (holder instanceof ContactsGUI gui) {
            event.setCancelled(true);
            handleContactsGUI(player, gui, clicked, event);
        } else if (holder instanceof SMSGUI gui) {
            event.setCancelled(true);
            handleSMSGUI(player, gui, clicked);
        } else if (holder instanceof SMSConversationsGUI gui) {
            event.setCancelled(true);
            handleSMSConversationsGUI(player, gui, clicked);
        } else if (holder instanceof AppStoreGUI gui) {
            event.setCancelled(true);
            handleAppStoreGUI(player, gui, clicked);
        } else if (holder instanceof SettingsGUI gui) {
            event.setCancelled(true);
            handleSettingsGUI(player, gui, clicked);
        } else if (holder instanceof SimShopGUI gui) {
            event.setCancelled(true);
            handleSimShopGUI(player, gui, clicked);
        } else if (holder instanceof AddContactGUI gui) {
            event.setCancelled(true);
            handleAddContactGUI(player, gui, clicked);
        }
    }

    private void handlePhoneGUI(Player player, PhoneGUI gui, ItemStack clicked, int slot) {
        String navAction = ItemBuilder.getData(clicked, "nav_action");

        if (navAction != null) {
            switch (navAction) {
                case "prev_page" -> gui.prevPage();
                case "next_page" -> gui.nextPage();
                case "home" -> gui.goHome();
            }
            return;
        }

        // Sprawdź czy kliknięto aplikację
        String appId = ItemBuilder.getData(clicked, "app_id");
        if (appId != null) {
            openApp(player, gui.getUser(), appId);
        }
    }

    private void openApp(Player player, PhoneUser user, String appId) {
        switch (appId) {
            case "contacts" -> new ContactsGUI(plugin, player, user).open();
            case "sms" -> new SMSConversationsGUI(plugin, player, user).open();
            case "appstore" -> new AppStoreGUI(plugin, player, user).open();
            case "settings" -> new SettingsGUI(plugin, player, user).open();
            default -> {
                // Sprawdź zewnętrzne aplikacje lub aplikacje ze sklepu
                AppData app = plugin.getAppManager().getAppData(appId);
                if (app != null && !app.isBuiltIn()) {
                    // Sprawdź czy zainstalowana
                    if (!user.hasAppInstalled(appId) && !plugin.getAppsConfig().getDefaultApps().containsKey(appId)) {
                        player.sendMessage(plugin.getMessagesConfig().appstoreNotInstalled(app.getName()));
                        return;
                    }
                }
                plugin.getAppManager().openApp(player, appId);
            }
        }
    }

    private void handleContactsGUI(Player player, ContactsGUI gui, ItemStack clicked, InventoryClickEvent event) {
        // Nawigacja dolna (wspólna)
        String navAction = ItemBuilder.getData(clicked, "nav_action");
        if (navAction != null) {
            switch (navAction) {
                case "back" -> plugin.getPhoneManager().openPhone(player);
                case "home" -> plugin.getPhoneManager().openPhone(player);
                case "next_page" -> gui.nextPage();
            }
            return;
        }

        String action = ItemBuilder.getData(clicked, "action");
        if (action != null) {
            switch (action) {
                case "add_contact" -> {
                    new AddContactGUI(plugin, player, gui.getUser()).open();
                }
            }
            return;
        }

        // Kliknięcie na kontakt
        String contactNumber = ItemBuilder.getData(clicked, "contact_number");
        String contactName = ItemBuilder.getData(clicked, "contact_name");

        if (contactNumber != null && contactName != null) {
            UUID partnerUUID = plugin.getPhoneManager().getPlayerByNumber(contactNumber);

            if (event.isShiftClick() && event.isRightClick()) {
                // Usuń kontakt
                plugin.getContactManager().removeContact(player.getUniqueId(), contactNumber);
                player.sendMessage(plugin.getMessagesConfig().contactsRemoved(contactName));
                gui.refresh();
            } else if (event.isRightClick()) {
                // Edytuj kontakt
                player.closeInventory();
                player.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getPrefix() +
                        "&eWpisz nową nazwę dla kontaktu &f" + contactName + "&e:"));
                // TODO: Implementacja edycji przez chat
            } else if (event.isLeftClick() && partnerUUID != null) {
                // Otwórz SMS
                new SMSGUI(plugin, player, gui.getUser(), partnerUUID, contactName).open();
            }
        }
    }

    private void handleSMSGUI(Player player, SMSGUI gui, ItemStack clicked) {
        // Nawigacja dolna
        String navAction = ItemBuilder.getData(clicked, "nav_action");
        if (navAction != null) {
            switch (navAction) {
                case "back" -> new SMSConversationsGUI(plugin, player,
                        plugin.getPhoneManager().getUser(player.getUniqueId())).open();
                case "home" -> plugin.getPhoneManager().openPhone(player);
                case "next_page" -> gui.nextPage();
            }
            return;
        }

        String action = ItemBuilder.getData(clicked, "action");
        if (action != null) {
            if (action.equals("compose")) {
                player.closeInventory();
                player.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getPrefix() +
                        "&eUżyj: &f/sms " + gui.getPartnerName() + " <wiadomość>"));
            }
        }
    }

    private void handleSMSConversationsGUI(Player player, SMSConversationsGUI gui, ItemStack clicked) {
        // Nawigacja dolna
        String navAction = ItemBuilder.getData(clicked, "nav_action");
        if (navAction != null) {
            switch (navAction) {
                case "back" -> plugin.getPhoneManager().openPhone(player);
                case "home" -> plugin.getPhoneManager().openPhone(player);
                case "next_page" -> gui.nextPage();
            }
            return;
        }

        // Kliknięcie na konwersację
        String partnerUuidStr = ItemBuilder.getData(clicked, "partner_uuid");
        String partnerName = ItemBuilder.getData(clicked, "partner_name");

        if (partnerUuidStr != null && partnerName != null) {
            UUID partnerUUID = UUID.fromString(partnerUuidStr);
            new SMSGUI(plugin, player, plugin.getPhoneManager().getUser(player.getUniqueId()),
                    partnerUUID, partnerName).open();
        }
    }

    private void handleAppStoreGUI(Player player, AppStoreGUI gui, ItemStack clicked) {
        // Nawigacja dolna
        String navAction = ItemBuilder.getData(clicked, "nav_action");
        if (navAction != null) {
            switch (navAction) {
                case "back" -> plugin.getPhoneManager().openPhone(player);
                case "home" -> plugin.getPhoneManager().openPhone(player);
                case "next_page" -> gui.nextPage();
            }
            return;
        }

        String action = ItemBuilder.getData(clicked, "action");
        if (action != null) {
            switch (action) {
                case "change_category" -> gui.nextCategory();
                case "my_apps" -> {
                    // TODO: GUI zarządzania zainstalowanymi aplikacjami
                }
            }
            return;
        }

        // Kliknięcie na aplikację
        String appId = ItemBuilder.getData(clicked, "app_id");
        String installed = ItemBuilder.getData(clicked, "app_installed");

        if (appId != null) {
            AppData app = plugin.getAppManager().getAppData(appId);
            if (app == null) return;

            if ("true".equals(installed)) {
                // Odinstaluj
                if (plugin.getAppManager().uninstallApp(player, appId)) {
                    player.sendMessage(plugin.getMessagesConfig().appstoreUninstalled(app.getName()));
                    gui.refresh();
                }
            } else {
                // Zainstaluj (sprawdź cenę jeśli włączona ekonomia)
                if (plugin.isEconomyEnabled() && plugin.getConfigManager().isAppstoreEconomyEnabled() && app.getPrice() > 0) {
                    if (!plugin.getEconomy().has(player, app.getPrice())) {
                        player.sendMessage(plugin.getMessagesConfig().simNoMoney(String.valueOf(app.getPrice())));
                        return;
                    }
                    plugin.getEconomy().withdrawPlayer(player, app.getPrice());
                    player.sendMessage(plugin.getMessagesConfig().appstorePurchased(app.getName(), String.valueOf(app.getPrice())));
                }

                if (plugin.getAppManager().installApp(player, appId)) {
                    player.sendMessage(plugin.getMessagesConfig().appstoreInstalled(app.getName()));
                    gui.refresh();
                }
            }
        }
    }

    private void handleSettingsGUI(Player player, SettingsGUI gui, ItemStack clicked) {
        // Nawigacja dolna
        String navAction = ItemBuilder.getData(clicked, "nav_action");
        if (navAction != null) {
            switch (navAction) {
                case "back" -> plugin.getPhoneManager().openPhone(player);
                case "home" -> plugin.getPhoneManager().openPhone(player);
            }
            return;
        }

        String action = ItemBuilder.getData(clicked, "action");
        if (action == null) return;

        switch (action) {
            case "contacts" -> new ContactsGUI(plugin, player, gui.getUser()).open();
            case "messages" -> new SMSConversationsGUI(plugin, player, gui.getUser()).open();
            case "manage_apps" -> new AppStoreGUI(plugin, player, gui.getUser()).open();
            case "notifications" -> {
                // TODO: GUI powiadomień
            }
            case "clear_notifications" -> {
                plugin.getNotificationManager().clearNotifications(player.getUniqueId());
                player.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getPrefix() +
                        "&aWyczyszczono wszystkie powiadomienia!"));
                gui.refresh();
            }
            case "activate_sim" -> {
                if (gui.getUser().hasActiveSim()) {
                    player.sendMessage(plugin.getMessagesConfig().simAlreadyActive());
                    return;
                }
                // Szukaj karty SIM w ekwipunku
                ItemStack simCard = findSimCardInInventory(player);
                if (simCard == null) {
                    player.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getPrefix() +
                            "&cNie masz karty SIM w ekwipunku! Kup ją w punkcie sprzedaży."));
                    return;
                }
                // Aktywuj
                String simTypeId = ItemBuilder.getSimType(simCard);
                String phoneNumber = ItemBuilder.getSimNumber(simCard);
                if (simTypeId != null && phoneNumber != null) {
                    if (plugin.getSimManager().activateSim(player, simTypeId, phoneNumber)) {
                        simCard.setAmount(simCard.getAmount() - 1);
                        player.sendMessage(plugin.getMessagesConfig().simActivated(phoneNumber));
                        gui.refresh();
                    }
                } else {
                    player.sendMessage(plugin.getMessagesConfig().simInvalidCard());
                }
            }
        }
    }

    private ItemStack findSimCardInInventory(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (ItemBuilder.isSimCard(item)) {
                return item;
            }
        }
        return null;
    }

    private void handleSimShopGUI(Player player, SimShopGUI gui, ItemStack clicked) {
        // Nawigacja dolna
        String navAction = ItemBuilder.getData(clicked, "nav_action");
        if (navAction != null) {
            switch (navAction) {
                case "home" -> plugin.getPhoneManager().openPhone(player);
            }
            return;
        }

        String action = ItemBuilder.getData(clicked, "action");
        if ("close".equals(action)) {
            player.closeInventory();
            return;
        }

        if ("buy_sim".equals(action)) {
            String simType = ItemBuilder.getData(clicked, "sim_type");
            if (simType != null) {
                plugin.getSimManager().purchaseSim(player, simType);
                player.closeInventory();
            }
        }
    }

    private void handleAddContactGUI(Player player, AddContactGUI gui, ItemStack clicked) {
        // Nawigacja dolna
        String navAction = ItemBuilder.getData(clicked, "nav_action");
        if (navAction != null) {
            switch (navAction) {
                case "back" -> new ContactsGUI(plugin, player, gui.getUser()).open();
                case "home" -> plugin.getPhoneManager().openPhone(player);
                case "next_page" -> gui.nextPage();
            }
            return;
        }

        // Kliknięcie na gracza
        String targetName = ItemBuilder.getData(clicked, "target_name");
        String targetNumber = ItemBuilder.getData(clicked, "target_number");
        String targetUuidStr = ItemBuilder.getData(clicked, "target_uuid");

        if (targetName != null && targetNumber != null && targetUuidStr != null) {
            UUID targetUUID = UUID.fromString(targetUuidStr);

            // Dodaj kontakt
            if (plugin.getContactManager().addContact(player.getUniqueId(), targetName, targetNumber)) {
                player.sendMessage(plugin.getMessagesConfig().contactsAdded(targetName, targetNumber));
                // Wróć do kontaktów
                PhoneUser updatedUser = plugin.getPhoneManager().getUser(player.getUniqueId());
                new ContactsGUI(plugin, player, updatedUser).open();
            } else {
                player.sendMessage(plugin.getMessagesConfig().contactsAlreadyExists());
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // Można dodać logikę przy zamykaniu GUI
    }
}
