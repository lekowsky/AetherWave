package pl.skyrise.mCPhone.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import pl.skyrise.mCPhone.MCPhone;
import pl.skyrise.mCPhone.gui.SimShopGUI;
import pl.skyrise.mCPhone.models.PhoneUser;
import pl.skyrise.mCPhone.models.SimShop;
import pl.skyrise.mCPhone.models.SimType;
import pl.skyrise.mCPhone.utils.ColorUtils;
import pl.skyrise.mCPhone.utils.ItemBuilder;

/**
 * Listener obsługujący karty SIM i punkty sprzedaży
 */
public class SimListener implements Listener {

    private final MCPhone plugin;

    public SimListener(MCPhone plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        // Sprawdź aktywację karty SIM
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {

            // Metoda 1: Karta SIM w głównej ręce + SHIFT
            if (ItemBuilder.isSimCard(mainHand) && player.isSneaking()) {
                event.setCancelled(true);
                // Sprawdź czy gracz ma telefon gdziekolwiek w ekwipunku
                if (plugin.getPhoneManager().hasPhone(player)) {
                    activateSimCard(player, mainHand);
                } else {
                    player.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getPrefix() +
                            "&cMusisz mieć telefon w ekwipunku aby aktywować kartę SIM!"));
                }
                return;
            }

            // Metoda 2: Karta SIM w głównej ręce, telefon w drugiej
            if (ItemBuilder.isSimCard(mainHand) && ItemBuilder.isPhone(offHand)) {
                event.setCancelled(true);
                activateSimCard(player, mainHand);
                return;
            }

            // Metoda 3: Telefon w głównej ręce, karta SIM w drugiej
            if (ItemBuilder.isPhone(mainHand) && ItemBuilder.isSimCard(offHand)) {
                event.setCancelled(true);
                activateSimCard(player, offHand);
                return;
            }
        }

        // Sprawdź interakcję z punktem sprzedaży SIM
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            Block block = event.getClickedBlock();
            Material shopBlock = plugin.getConfigManager().getSimShopBlock();

            if (block.getType() == shopBlock) {
                SimShop shop = plugin.getSimManager().getNearestShop(block.getLocation(), 2);
                if (shop != null) {
                    event.setCancelled(true);
                    new SimShopGUI(plugin, player, shop).open();
                }
            }
        }
    }

    private void activateSimCard(Player player, ItemStack simCardItem) {
        String simTypeId = ItemBuilder.getSimType(simCardItem);
        String phoneNumber = ItemBuilder.getSimNumber(simCardItem);

        if (simTypeId == null || phoneNumber == null) {
            player.sendMessage(plugin.getMessagesConfig().simInvalidCard());
            return;
        }

        SimType simType = plugin.getSimManager().getSimType(simTypeId);
        if (simType == null) {
            player.sendMessage(plugin.getMessagesConfig().simInvalidCard());
            return;
        }

        PhoneUser user = plugin.getPhoneManager().getOrCreateUser(player.getUniqueId());

        // Sprawdź czy już ma aktywną kartę SIM
        if (user.hasActiveSim()) {
            player.sendMessage(plugin.getMessagesConfig().simAlreadyActive());
            return;
        }

        // Aktywuj kartę SIM
        if (plugin.getSimManager().activateSim(player, simTypeId, phoneNumber)) {
            // Usuń kartę SIM z ekwipunku
            simCardItem.setAmount(simCardItem.getAmount() - 1);
            player.sendMessage(plugin.getMessagesConfig().simActivated(phoneNumber));
        } else {
            player.sendMessage(plugin.getMessagesConfig().simInvalidCard());
        }
    }
}
