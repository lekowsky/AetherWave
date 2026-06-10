package pl.skyrise.vendingMachine.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import pl.skyrise.vendingMachine.VendingMachine;
import pl.skyrise.vendingMachine.util.ColorUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class ChatInputListener implements Listener {

    private final VendingMachine plugin;
    private static final Map<UUID, Consumer<String>> pendingInputs = new HashMap<>();

    public ChatInputListener(VendingMachine plugin) {
        this.plugin = plugin;
    }

    public static void requestInput(Player player, String prompt, Consumer<String> callback) {
        pendingInputs.put(player.getUniqueId(), callback);
        player.sendMessage(VendingMachine.getInstance().getPrefix() + ColorUtil.color(prompt));
        player.sendMessage(ColorUtil.color("&7Wpisz &ecancel &7aby anulować."));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Consumer<String> callback = pendingInputs.remove(event.getPlayer().getUniqueId());
        if (callback == null) return;

        event.setCancelled(true);
        String message = event.getMessage().trim();

        if (message.equalsIgnoreCase("cancel")) {
            Bukkit.getScheduler().runTask(plugin, () ->
                    event.getPlayer().sendMessage(plugin.getPrefix() + ColorUtil.color("&cAnulowano.")));
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> callback.accept(message));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        pendingInputs.remove(event.getPlayer().getUniqueId());
    }
}