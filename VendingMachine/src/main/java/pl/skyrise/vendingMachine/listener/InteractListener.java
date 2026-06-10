package pl.skyrise.vendingMachine.listener;

import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import pl.skyrise.vendingMachine.VendingMachine;
import pl.skyrise.vendingMachine.gui.MachineGUI;
import pl.skyrise.vendingMachine.model.MachinePlacement;
import pl.skyrise.vendingMachine.model.MachineTemplate;
import pl.skyrise.vendingMachine.util.ColorUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InteractListener implements Listener {

    private final VendingMachine plugin;
    private final Map<UUID, Long> interactCooldowns = new HashMap<>();

    public InteractListener(VendingMachine plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null) return;

        Player player = event.getPlayer();

        MachinePlacement placement = plugin.getPlacementManager().getPlacement(block.getLocation());
        if (placement == null) return;

        event.setCancelled(true);

        // Tylko główna ręka
        if (event.getHand() != EquipmentSlot.HAND) return;

        // Cooldown anty-spam
        long cooldownMs = plugin.getConfig().getLong("interact-cooldown-ms", 500);
        long now = System.currentTimeMillis();
        Long last = interactCooldowns.get(player.getUniqueId());
        if (last != null && (now - last) < cooldownMs) return;
        interactCooldowns.put(player.getUniqueId(), now);

        MachineTemplate template = plugin.getMachineManager().getTemplate(placement.getTemplateName());
        if (template == null) {
            player.sendMessage(plugin.getPrefix() + ColorUtil.color("&cSzablon nie istnieje!"));
            return;
        }

        if (!template.isEnabled()) {
            player.sendMessage(plugin.getPrefix() + ColorUtil.color(
                    plugin.getConfig().getString("messages.disabled", "&cAutomat wyłączony!")));
            return;
        }

        if (!template.getPermission().isEmpty() && !player.hasPermission(template.getPermission())) {
            player.sendMessage(plugin.getPrefix() + ColorUtil.color(
                    plugin.getConfig().getString("messages.no-permission", "&cBrak uprawnień!")));
            return;
        }

        if (!player.hasPermission("vendingmachine.use")) {
            player.sendMessage(plugin.getPrefix() + ColorUtil.color(
                    plugin.getConfig().getString("messages.no-permission", "&cBrak uprawnień!")));
            return;
        }

        try {
            String soundName = plugin.getConfig().getString("sounds.open", "UI_BUTTON_CLICK");
            player.playSound(player.getLocation(), Sound.valueOf(soundName), 0.5f, 1.0f);
        } catch (Exception ignored) {}

        new MachineGUI(plugin, template, player, placement).open();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;

        MachinePlacement placement = plugin.getPlacementManager().getPlacement(event.getBlock().getLocation());
        if (placement == null) return;

        if (!plugin.getConfig().getBoolean("protection.allow-player-break", false)) {
            if (!event.getPlayer().hasPermission("vendingmachine.delete")) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(plugin.getPrefix() + ColorUtil.color(
                        plugin.getConfig().getString("messages.no-permission", "&cBrak uprawnień!")));
                return;
            }
        }

        plugin.getPlacementManager().remove(event.getBlock().getLocation());
        event.getPlayer().sendMessage(plugin.getPrefix() + ColorUtil.color(
                plugin.getConfig().getString("messages.removed", "&cUsunięto!")));
    }
}