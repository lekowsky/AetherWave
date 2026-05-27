package pl.skyrise.mCPhone.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pl.skyrise.mCPhone.MCPhone;

/**
 * Komenda /phone - otwiera telefon
 */
public class PhoneCommand implements CommandExecutor {

    private final MCPhone plugin;

    public PhoneCommand(MCPhone plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessagesConfig().playerOnly());
            return true;
        }

        if (!player.hasPermission("mcphone.use")) {
            player.sendMessage(plugin.getMessagesConfig().noPermission());
            return true;
        }

        // Sprawdź czy gracz ma telefon
        if (!plugin.getPhoneManager().hasPhone(player)) {
            player.sendMessage(plugin.getMessagesConfig().phoneNoPhone());
            return true;
        }

        // Otwórz telefon
        plugin.getPhoneManager().openPhone(player);

        // Wyślij wiadomość (jeśli skonfigurowana)
        String openedMsg = plugin.getMessagesConfig().phoneOpened();
        if (openedMsg != null && !openedMsg.isEmpty()) {
            player.sendMessage(openedMsg);
        }

        return true;
    }
}
