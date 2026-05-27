package pl.skyrise.mCPhone.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import pl.skyrise.mCPhone.MCPhone;
import pl.skyrise.mCPhone.models.PhoneUser;
import pl.skyrise.mCPhone.utils.ColorUtils;
import pl.skyrise.mCPhone.utils.ItemBuilder;

import java.util.UUID;

/**
 * GUI do dodawania nowego kontaktu - wybór gracza z listy online
 */
public class AddContactGUI implements InventoryHolder {

    private final MCPhone plugin;
    private final Player player;
    private final PhoneUser user;
    private final Inventory inventory;
    private int currentPage = 0;

    private static final int[] PLAYER_SLOTS = {
            3, 4, 5, 12, 13, 14, 21, 22, 23, 30, 31, 32, 39, 40, 41
    };

    public AddContactGUI(MCPhone plugin, Player player, PhoneUser user) {
        this.plugin = plugin;
        this.player = player;
        this.user = user;

        pl.skyrise.mCPhone.config.GuiConfig guiConfig = plugin.getGuiConfig();
        this.inventory = Bukkit.createInventory(this, guiConfig.getPhoneGuiSize(),
                guiConfig.getTitleComponent());

        setupGUI();
    }

    private void setupGUI() {
        inventory.clear();
        pl.skyrise.mCPhone.config.GuiConfig guiConfig = plugin.getGuiConfig();

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

        // Lista graczy online z numerem telefonu
        java.util.List<Player> playersWithPhone = new java.util.ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.equals(player)) continue;
            PhoneUser pUser = plugin.getPhoneManager().getUser(p.getUniqueId());
            if (pUser != null && pUser.getPhoneNumber() != null && !user.hasContact(pUser.getPhoneNumber())) {
                playersWithPhone.add(p);
            }
        }

        int startIndex = currentPage * PLAYER_SLOTS.length;
        int endIndex = Math.min(startIndex + PLAYER_SLOTS.length, playersWithPhone.size());

        for (int i = startIndex; i < endIndex; i++) {
            int slotIndex = i - startIndex;
            if (slotIndex < PLAYER_SLOTS.length) {
                Player p = playersWithPhone.get(i);
                PhoneUser pUser = plugin.getPhoneManager().getUser(p.getUniqueId());
                ItemStack playerItem = createPlayerItem(p, pUser);
                inventory.setItem(PLAYER_SLOTS[slotIndex], playerItem);
            }
        }

        // Nawigacja dolna
        int totalPages = (int) Math.ceil((double) playersWithPhone.size() / PLAYER_SLOTS.length);

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

        // Prawy - następna strona
        if (totalPages > 1 && currentPage < totalPages - 1) {
            ItemStack nextPage = new ItemBuilder(guiConfig.getNavRightMaterial())
                    .name(guiConfig.getNavRightDisplayName())
                    .lore(guiConfig.getNavRightLore())
                    .data("nav_action", "next_page")
                    .build();
            inventory.setItem(guiConfig.getNavRightSlot(), nextPage);
        }

        // Jeśli brak graczy
        if (playersWithPhone.isEmpty()) {
            ItemStack noPlayers = new ItemBuilder(Material.BARRIER)
                    .name("&cBrak graczy do dodania")
                    .lore(
                            "&7Żaden inny gracz online",
                            "&7nie ma aktywnego numeru telefonu",
                            "&7lub już masz go w kontaktach."
                    )
                    .build();
            inventory.setItem(22, noPlayers);
        }
    }

    private ItemStack createPlayerItem(Player p, PhoneUser pUser) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        org.bukkit.inventory.meta.SkullMeta meta = (org.bukkit.inventory.meta.SkullMeta) skull.getItemMeta();
        meta.setOwningPlayer(p);
        meta.displayName(ColorUtils.toComponent("&f" + p.getName()));
        meta.lore(java.util.List.of(
                ColorUtils.toComponent("&7Numer: &f" + pUser.getPhoneNumber()),
                ColorUtils.toComponent(""),
                ColorUtils.toComponent("&eKliknij aby dodać do kontaktów")
        ));
        skull.setItemMeta(meta);

        return new ItemBuilder(skull)
                .data("target_uuid", p.getUniqueId().toString())
                .data("target_name", p.getName())
                .data("target_number", pUser.getPhoneNumber())
                .build();
    }

    public void open() {
        player.openInventory(inventory);
    }

    public void nextPage() {
        currentPage++;
        setupGUI();
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
