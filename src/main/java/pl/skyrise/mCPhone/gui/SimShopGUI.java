package pl.skyrise.mCPhone.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import pl.skyrise.mCPhone.MCPhone;
import pl.skyrise.mCPhone.config.GuiConfig;
import pl.skyrise.mCPhone.models.SimShop;
import pl.skyrise.mCPhone.models.SimType;
import pl.skyrise.mCPhone.utils.ColorUtils;
import pl.skyrise.mCPhone.utils.ItemBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI sklepu z kartami SIM
 */
public class SimShopGUI implements InventoryHolder {

    private final MCPhone plugin;
    private final Player player;
    private final SimShop shop;
    private final Inventory inventory;

    public SimShopGUI(MCPhone plugin, Player player, SimShop shop) {
        this.plugin = plugin;
        this.player = player;
        this.shop = shop;

        GuiConfig guiConfig = plugin.getGuiConfig();
        this.inventory = Bukkit.createInventory(this, guiConfig.getSimShopGuiSize(),
                ColorUtils.toTitleComponent(
                        guiConfig.getSimShopGuiTitle(),
                        guiConfig.getTitleOffset(),
                        guiConfig.getTitleFormat()
                ));

        setupGUI();
    }

    private static final int[] SIM_SLOTS = {
            3, 4, 5, 12, 13, 14, 21, 22, 23, 30, 31, 32, 39, 40, 41
    };

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

        // Dostępne karty SIM
        List<String> availableSims = shop.getAvailableSims();
        int slotIndex = 0;

        for (String simTypeId : availableSims) {
            if (slotIndex >= SIM_SLOTS.length) break;

            SimType simType = plugin.getSimManager().getSimType(simTypeId);
            if (simType == null) continue;

            ItemStack simItem = createSimItem(simType);
            inventory.setItem(SIM_SLOTS[slotIndex], simItem);
            slotIndex++;
        }

        // Nawigacja dolna - zamknij
        ItemStack closeButton = new ItemBuilder(guiConfig.getNavLeftMaterial())
                .name("&c← Zamknij")
                .data("action", "close")
                .build();
        inventory.setItem(guiConfig.getNavLeftSlot(), closeButton);

        ItemStack homeButton = new ItemBuilder(guiConfig.getNavHomeMaterial())
                .name(guiConfig.getNavHomeDisplayName())
                .lore(guiConfig.getNavHomeLore())
                .data("nav_action", "home")
                .build();
        inventory.setItem(guiConfig.getNavHomeSlot(), homeButton);
    }

    private ItemStack createSimItem(SimType simType) {
        List<String> lore = new ArrayList<>();
        lore.add("&7" + simType.getDescription());
        lore.add("");

        String limitStr = simType.hasUnlimitedSms() ? "&aBez limitu" : "&f" + simType.getDailySmsLimit() + "/dzień";
        lore.add("&7Limit SMS: " + limitStr);
        lore.add("&7Połączenia: " + (simType.areCallsEnabled() ? "&aTAK" : "&cNIE"));
        lore.add("&7Internet: " + (simType.isInternetEnabled() ? "&aTAK" : "&cNIE"));
        lore.add("");
        lore.add("&7Cena: &f" + simType.getPrice() + "$");
        lore.add("");
        lore.add("&eKliknij aby kupić");

        return new ItemBuilder(simType.getMaterial())
                .name(simType.getName())
                .lore(lore)
                .customModelData(simType.getCustomModelData())
                .data("sim_type", simType.getId())
                .data("action", "buy_sim")
                .build();
    }

    public void open() {
        player.openInventory(inventory);
    }

    public Player getPlayer() {
        return player;
    }

    public SimShop getShop() {
        return shop;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
