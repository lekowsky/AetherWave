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
import pl.skyrise.mCPhone.utils.ItemBuilder;

import java.util.List;

/**
 * Główne GUI telefonu (pulpit)
 * NAPRAWIONE:
 *   - prevPage() i nextPage() teraz poprawnie otwierają nowe inventory
 *   - Używa getTitleComponent() z obsługą MiniMessage shift
 */
public class PhoneGUI implements InventoryHolder {

    private final MCPhone plugin;
    private final Player player;
    private final PhoneUser user;
    private Inventory inventory;  // ← ZMIANA: nie final! Musimy móc tworzyć nowe.
    private int currentPage = 0;

    public PhoneGUI(MCPhone plugin, Player player, PhoneUser user) {
        this.plugin = plugin;
        this.player = player;
        this.user = user;
        buildInventory();
    }

    /**
     * Tworzy NOWE inventory (potrzebne przy zmianie strony).
     * Inventory w Minecraft nie może zmieniać tytułu po stworzeniu,
     * więc przy każdym prevPage/nextPage tworzymy nowe.
     */
    private void buildInventory() {
        GuiConfig guiConfig = plugin.getGuiConfig();
        // Tworzymy nowe inventory z poprawnym tytułem
        this.inventory = Bukkit.createInventory(
                this,
                guiConfig.getPhoneGuiSize(),
                guiConfig.getTitleComponent()
        );
        setupGUI();
    }

    private void setupGUI() {
        inventory.clear();
        GuiConfig guiConfig = plugin.getGuiConfig();

        // Ramka telefonu (gdy nie używamy Nexo)
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

        // Tło ekranu (puste sloty)
        ItemStack bg = new ItemBuilder(guiConfig.getScreenBackgroundMaterial())
                .name(guiConfig.getScreenBackgroundDisplayName())
                .customModelData(guiConfig.getScreenBackgroundCustomModelData())
                .build();
        for (int slot : guiConfig.getScreenSlots()) {
            if (slot >= 0 && slot < inventory.getSize()) {
                inventory.setItem(slot, bg);
            }
        }

        // Aplikacje na pulpicie (paginacja)
        List<AppData> apps = plugin.getAppManager().getDesktopApps(player);
        List<Integer> screenSlots = guiConfig.getScreenSlots();
        int appsPerPage = screenSlots.size();
        int startIndex = currentPage * appsPerPage;
        int endIndex = Math.min(startIndex + appsPerPage, apps.size());

        for (int i = startIndex; i < endIndex; i++) {
            AppData app = apps.get(i);
            int slotIndex = i - startIndex;
            if (slotIndex < screenSlots.size()) {
                int slot = screenSlots.get(slotIndex);
                ItemStack appItem = new ItemBuilder(app.getMaterial())
                        .name(app.getDisplayName())
                        .lore(app.getLore())
                        .customModelData(app.getCustomModelData())
                        .data("app_id", app.getId())
                        .build();
                inventory.setItem(slot, appItem);
            }
        }

        // Nawigacja
        setupNavigation(apps, appsPerPage);
    }

    private void setupNavigation(List<AppData> apps, int appsPerPage) {
        GuiConfig guiConfig = plugin.getGuiConfig();
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
        } else {
            ItemStack leftDisabled = new ItemBuilder(guiConfig.getFrameMaterial())
                    .name("&8◄")
                    .customModelData(guiConfig.getFrameCustomModelData())
                    .build();
            inventory.setItem(guiConfig.getNavLeftSlot(), leftDisabled);
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
        } else {
            ItemStack rightDisabled = new ItemBuilder(guiConfig.getFrameMaterial())
                    .name("&8►")
                    .customModelData(guiConfig.getFrameCustomModelData())
                    .build();
            inventory.setItem(guiConfig.getNavRightSlot(), rightDisabled);
        }
    }

    public void open() {
        player.openInventory(inventory);
    }

    // =========================================================
    // NAPRAWIONE - prevPage() i nextPage()
    // =========================================================

    /**
     * Poprzednia strona.
     * NAPRAWKA: buildInventory() tworzy nowe inventory i setupGUI() je wypełnia.
     * Następnie player.openInventory() OTWIERA nowe inventory dla gracza.
     * Bez open() gracz widziałby stare inventory bez zmian!
     */
    public void nextPage() {
        List<AppData> apps = plugin.getAppManager().getDesktopApps(player);
        int appsPerPage = plugin.getGuiConfig().getScreenSlots().size();
        int totalPages = (int) Math.ceil((double) apps.size() / appsPerPage);

        if (currentPage < totalPages - 1) {
            currentPage++;
            buildInventory(); // ← tworzy nowe inventory
            open();           // ← KLUCZOWE: otwiera nowe inventory dla gracza!
        }
    }

    /**
     * Następna strona.
     * NAPRAWKA: identyczna jak nextPage().
     */
    public void prevPage() {
        if (currentPage > 0) {
            currentPage--;
            buildInventory(); // ← tworzy nowe inventory
            open();           // ← KLUCZOWE: otwiera nowe inventory dla gracza!
        }
    }

    public void goHome() {
        currentPage = 0;
        buildInventory();
        open();
    }

    public int getCurrentPage() { return currentPage; }
    public Player getPlayer() { return player; }
    public PhoneUser getUser() { return user; }

    @Override
    public Inventory getInventory() { return inventory; }
}