package pl.skyrise.mCPhone.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import pl.skyrise.mCPhone.MCPhone;
import pl.skyrise.mCPhone.config.GuiConfig;
import pl.skyrise.mCPhone.models.Contact;
import pl.skyrise.mCPhone.models.PhoneUser;
import pl.skyrise.mCPhone.utils.ColorUtils;
import pl.skyrise.mCPhone.utils.ItemBuilder;

import java.util.List;

/**
 * GUI kontaktów
 */
public class ContactsGUI implements InventoryHolder {

    private final MCPhone plugin;
    private final Player player;
    private final PhoneUser user;
    private final Inventory inventory;
    private int currentPage = 0;

    private static final int[] CONTACT_SLOTS = {
            3, 4, 5, 12, 13, 14, 21, 22, 23, 30, 31, 32, 39, 40, 41
    };

    public ContactsGUI(MCPhone plugin, Player player, PhoneUser user) {
        this.plugin = plugin;
        this.player = player;
        this.user = user;

        GuiConfig guiConfig = plugin.getGuiConfig();
        this.inventory = Bukkit.createInventory(this,
                guiConfig.getPhoneGuiSize(),
                ColorUtils.toComponent(guiConfig.getEffectiveTitle()));

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

        // Kontakty
        List<Contact> contacts = user.getContacts();
        int startIndex = currentPage * CONTACT_SLOTS.length;
        int endIndex = Math.min(startIndex + CONTACT_SLOTS.length, contacts.size());

        for (int i = startIndex; i < endIndex; i++) {
            int slotIndex = i - startIndex;
            if (slotIndex < CONTACT_SLOTS.length) {
                Contact contact = contacts.get(i);
                ItemStack contactItem = createContactItem(contact);
                inventory.setItem(CONTACT_SLOTS[slotIndex], contactItem);
            }
        }

        // Przycisk dodawania kontaktu - ostatni slot ekranu zastąp przyciskiem
        ItemStack addButton = new ItemBuilder(guiConfig.getContactsAddButtonMaterial())
                .name(guiConfig.getContactsAddButtonDisplayName())
                .data("action", "add_contact")
                .build();
        inventory.setItem(41, addButton);

        // Nawigacja dolna
        int totalPages = (int) Math.ceil((double) contacts.size() / CONTACT_SLOTS.length);

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

        // Prawy - następna strona (jeśli jest)
        if (totalPages > 1 && currentPage < totalPages - 1) {
            ItemStack nextPage = new ItemBuilder(guiConfig.getNavRightMaterial())
                    .name(guiConfig.getNavRightDisplayName())
                    .lore(guiConfig.getNavRightLore())
                    .data("nav_action", "next_page")
                    .build();
            inventory.setItem(guiConfig.getNavRightSlot(), nextPage);
        }
    }

    private ItemStack createContactItem(Contact contact) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();

        // Ustaw głowę gracza jeśli znany
        if (contact.getPlayerUUID() != null) {
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(contact.getPlayerUUID()));
        }

        meta.displayName(ColorUtils.toComponent("&f" + contact.getName()));
        meta.lore(List.of(
                ColorUtils.toComponent("&7Numer: &f" + contact.getPhoneNumber()),
                ColorUtils.toComponent(""),
                ColorUtils.toComponent("&eLKM &7- Wyślij SMS"),
                ColorUtils.toComponent("&ePKM &7- Edytuj kontakt"),
                ColorUtils.toComponent("&cSHIFT+PKM &7- Usuń kontakt")
        ));

        // Dodaj dane do PDC
        item.setItemMeta(meta);

        return new ItemBuilder(item)
                .data("contact_number", contact.getPhoneNumber())
                .data("contact_name", contact.getName())
                .build();
    }

    public void open() {
        player.openInventory(inventory);
    }

    public void nextPage() {
        int totalPages = (int) Math.ceil((double) user.getContacts().size() / CONTACT_SLOTS.length);
        if (currentPage < totalPages - 1) {
            currentPage++;
            setupGUI();
        }
    }

    public void prevPage() {
        if (currentPage > 0) {
            currentPage--;
            setupGUI();
        }
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
