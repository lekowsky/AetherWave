package pl.skyrise.mCPhone.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
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

import java.util.ArrayList;
import java.util.List;

/**
 * GUI sklepu z aplikacjami (AppStore)
 */
public class AppStoreGUI implements InventoryHolder {

    private final MCPhone plugin;
    private final Player player;
    private final PhoneUser user;
    private final Inventory inventory;
    private int currentPage = 0;
    private String currentCategory = "all";

    private static final int[] APP_SLOTS = {
            3, 4, 5, 12, 13, 14, 21, 22, 23, 30, 31, 32, 39, 40, 41
    };

    public AppStoreGUI(MCPhone plugin, Player player, PhoneUser user) {
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

        // Aplikacje ze sklepu
        List<AppData> apps = getFilteredApps();

        int startIndex = currentPage * APP_SLOTS.length;
        int endIndex = Math.min(startIndex + APP_SLOTS.length, apps.size());

        for (int i = startIndex; i < endIndex; i++) {
            int slotIndex = i - startIndex;
            if (slotIndex < APP_SLOTS.length) {
                AppData app = apps.get(i);
                ItemStack appItem = createAppItem(app);
                inventory.setItem(APP_SLOTS[slotIndex], appItem);
            }
        }

        // Nawigacja dolna
        int totalPages = (int) Math.ceil((double) apps.size() / APP_SLOTS.length);

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

        // Jeśli brak aplikacji w sklepie
        if (apps.isEmpty()) {
            ItemStack noApps = new ItemBuilder(Material.BARRIER)
                    .name("&7Brak aplikacji")
                    .lore(
                            "&7W tej kategorii nie ma",
                            "&7żadnych aplikacji.",
                            "",
                            "&8Zewnętrzne pluginy mogą",
                            "&8rejestrować aplikacje przez API."
                    )
                    .build();
            inventory.setItem(22, noApps);
        }
    }

    private List<AppData> getFilteredApps() {
        List<AppData> allApps = plugin.getAppManager().getStoreApps();

        if (currentCategory.equals("all")) {
            return allApps;
        }

        List<AppData> filtered = new ArrayList<>();
        for (AppData app : allApps) {
            if (app.getCategory().equalsIgnoreCase(currentCategory)) {
                filtered.add(app);
            }
        }
        return filtered;
    }

    private ItemStack createAppItem(AppData app) {
        boolean installed = user.hasAppInstalled(app.getId());

        List<String> lore = new ArrayList<>();
        lore.add("&7" + app.getDescription());
        lore.add("");
        lore.add("&7Kategoria: &f" + getCategoryName(app.getCategory()));

        if (installed) {
            lore.add("&7Status: &aZainstalowana");
            lore.add("");
            lore.add("&cKliknij aby odinstalować");
        } else {
            if (app.isFree()) {
                lore.add("&7Cena: &aDarmowa");
            } else {
                lore.add("&7Cena: &e" + app.getPrice() + "$");
            }
            lore.add("");
            lore.add("&eKliknij aby zainstalować");
        }

        return new ItemBuilder(app.getMaterial())
                .name(app.getName())
                .lore(lore)
                .customModelData(app.getCustomModelData())
                .data("app_id", app.getId())
                .data("app_installed", installed ? "true" : "false")
                .build();
    }

    private String getCategoryName(String categoryId) {
        return switch (categoryId.toLowerCase()) {
            case "all" -> "Wszystkie";
            case "communication" -> "Komunikacja";
            case "tools" -> "Narzędzia";
            case "navigation" -> "Nawigacja";
            case "games" -> "Gry";
            case "system" -> "System";
            default -> categoryId;
        };
    }

    public void open() {
        player.openInventory(inventory);
    }

    public void nextPage() {
        List<AppData> apps = getFilteredApps();
        int totalPages = (int) Math.ceil((double) apps.size() / APP_SLOTS.length);
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

    public void nextCategory() {
        String[] categories = {"all", "communication", "tools", "navigation", "games", "system"};
        int currentIndex = -1;
        for (int i = 0; i < categories.length; i++) {
            if (categories[i].equals(currentCategory)) {
                currentIndex = i;
                break;
            }
        }
        currentCategory = categories[(currentIndex + 1) % categories.length];
        currentPage = 0;
        setupGUI();
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
