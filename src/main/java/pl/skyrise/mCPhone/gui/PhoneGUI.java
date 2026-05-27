package pl.skyrise.mCPhone.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import pl.skyrise.mCPhone.MCPhone;
import pl.skyrise.mCPhone.config.GuiConfig;
import pl.skyrise.mCPhone.models.AppData;
import pl.skyrise.mCPhone.models.PhoneUser;
import pl.skyrise.mCPhone.utils.ColorUtils;
import pl.skyrise.mCPhone.utils.ItemBuilder;

import java.util.List;

/**
 * Główne GUI telefonu (pulpit)
 */
public class PhoneGUI implements InventoryHolder {

    private final MCPhone plugin;
    private final Player player;
    private final PhoneUser user;
    private final Inventory inventory;
    private int currentPage = 0;

    public PhoneGUI(MCPhone plugin, Player player, PhoneUser user) {
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
            ItemStack frameItem = new ItemBuilder(guiConfig.getFrameMaterial())
                    .name(guiConfig.getFrameDisplayName())
                    .customModelData(guiConfig.getFrameCustomModelData())
                    .build();

            for (int slot : guiConfig.getFrameSlots()) {
                if (slot >= 0 && slot < inventory.getSize()) {
                    inventory.setItem(slot, frameItem);
                }
            }
        }

        // Tło ekranu - tylko gdy nie używamy Nexo
        if (!guiConfig.useNexo()) {
            ItemStack backgroundItem = new ItemBuilder(guiConfig.getScreenBackgroundMaterial())
                    .name(guiConfig.getScreenBackgroundDisplayName())
                    .customModelData(guiConfig.getScreenBackgroundCustomModelData())
                    .build();

            for (int slot : guiConfig.getScreenSlots()) {
                if (slot >= 0 && slot < inventory.getSize()) {
                    inventory.setItem(slot, backgroundItem);
                }
            }
        }

        // Ikony aplikacji
        List<AppData> apps = plugin.getAppManager().getDesktopApps(player);
        List<Integer> screenSlots = guiConfig.getScreenSlots();
        int appsPerPage = screenSlots.size();
        int startIndex = currentPage * appsPerPage;
        int endIndex = Math.min(startIndex + appsPerPage, apps.size());

        for (int i = startIndex; i < endIndex; i++) {
            int slotIndex = i - startIndex;
            if (slotIndex < screenSlots.size()) {
                AppData app = apps.get(i);
                ItemStack appIcon = plugin.getAppManager().createAppIcon(app);
                inventory.setItem(screenSlots.get(slotIndex), appIcon);
            }
        }

        // Przyciski nawigacyjne
        int totalPages = (int) Math.ceil((double) apps.size() / appsPerPage);

        // Lewy przycisk (poprzednia strona)
        if (currentPage > 0) {
            ItemStack leftButton = new ItemBuilder(guiConfig.getNavLeftMaterial())
                    .name(guiConfig.getNavLeftDisplayName())
                    .lore(guiConfig.getNavLeftLore())
                    .customModelData(guiConfig.getFrameCustomModelData())
                    .data("nav_action", "prev_page")
                    .build();
            inventory.setItem(guiConfig.getNavLeftSlot(), leftButton);
        }

        // Środkowy przycisk (home)
        ItemStack homeButton = new ItemBuilder(guiConfig.getNavHomeMaterial())
                .name(guiConfig.getNavHomeDisplayName())
                .lore(guiConfig.getNavHomeLore())
                .customModelData(guiConfig.getFrameCustomModelData())
                .data("nav_action", "home")
                .build();
        inventory.setItem(guiConfig.getNavHomeSlot(), homeButton);

        // Prawy przycisk (następna strona)
        if (currentPage < totalPages - 1) {
            ItemStack rightButton = new ItemBuilder(guiConfig.getNavRightMaterial())
                    .name(guiConfig.getNavRightDisplayName())
                    .lore(guiConfig.getNavRightLore())
                    .customModelData(guiConfig.getFrameCustomModelData())
                    .data("nav_action", "next_page")
                    .build();
            inventory.setItem(guiConfig.getNavRightSlot(), rightButton);
        }
    }

    public void open() {
        player.openInventory(inventory);
    }

    public void nextPage() {
        List<AppData> apps = plugin.getAppManager().getDesktopApps(player);
        int appsPerPage = plugin.getGuiConfig().getScreenSlots().size();
        int totalPages = (int) Math.ceil((double) apps.size() / appsPerPage);

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

    public void goHome() {
        currentPage = 0;
        setupGUI();
    }

    public int getCurrentPage() {
        return currentPage;
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
