package pl.skyrise.mCPhone.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import pl.skyrise.mCPhone.MCPhone;
import pl.skyrise.mCPhone.utils.ItemBuilder;

/**
 * Listener obsługujący interakcje z telefonem
 */
public class PhoneListener implements Listener {

    private final MCPhone plugin;

    public PhoneListener(MCPhone plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // Sprawdź czy to telefon i prawy przycisk
        if (item == null) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        if (ItemBuilder.isPhone(item)) {
            event.setCancelled(true);

            if (!player.hasPermission("mcphone.use")) {
                player.sendMessage(plugin.getMessagesConfig().noPermission());
                return;
            }

            // Otwórz telefon
            plugin.getPhoneManager().openPhone(player);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();

        // Sprawdź czy to telefon i czy można go wyrzucić
        if (ItemBuilder.isPhone(item) && !plugin.getConfigManager().isPhoneDroppable()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Załaduj dane gracza
        plugin.getPhoneManager().getOrCreateUser(player.getUniqueId());
    }
}
