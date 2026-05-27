package pl.skyrise.mCPhone.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.skyrise.mCPhone.MCPhone;
import pl.skyrise.mCPhone.models.PhoneUser;
import pl.skyrise.mCPhone.utils.NumberGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Komenda /sms - szybkie wysyłanie SMS
 */
public class SMSCommand implements CommandExecutor, TabCompleter {

    private final MCPhone plugin;

    public SMSCommand(MCPhone plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessagesConfig().playerOnly());
            return true;
        }

        if (!player.hasPermission("mcphone.sms")) {
            player.sendMessage(plugin.getMessagesConfig().noPermission());
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(plugin.getMessagesConfig().invalidArgs("/sms <gracz/numer> <wiadomość>"));
            return true;
        }

        String target = args[0];
        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) messageBuilder.append(" ");
            messageBuilder.append(args[i]);
        }
        String message = messageBuilder.toString();

        // Znajdź odbiorcę
        UUID receiverUUID = findReceiver(target);

        if (receiverUUID == null) {
            // Sprawdź czy to numer telefonu
            if (NumberGenerator.isValid(target)) {
                player.sendMessage(plugin.getMessagesConfig().smsNumberNotFound(target));
            } else {
                player.sendMessage(plugin.getMessagesConfig().playerNotFound(target));
            }
            return true;
        }

        // Wyślij SMS
        plugin.getSmsManager().sendSMS(player.getUniqueId(), receiverUUID, message);

        return true;
    }

    private UUID findReceiver(String target) {
        // Sprawdź czy to nick gracza
        Player targetPlayer = Bukkit.getPlayer(target);
        if (targetPlayer != null) {
            return targetPlayer.getUniqueId();
        }

        // Sprawdź czy to numer telefonu
        if (NumberGenerator.isValid(target)) {
            return plugin.getPhoneManager().getPlayerByNumber(target);
        }

        // Sprawdź offline graczy po nazwie
        UUID offlineUUID = Bukkit.getOfflinePlayer(target).getUniqueId();
        PhoneUser offlineUser = plugin.getPhoneManager().getUser(offlineUUID);
        if (offlineUser != null && offlineUser.getPhoneNumber() != null) {
            return offlineUUID;
        }

        return null;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return List.of();
        }

        if (args.length == 1) {
            // Podpowiedzi: gracze online + kontakty
            List<String> suggestions = new ArrayList<>();

            // Gracze online
            suggestions.addAll(Bukkit.getOnlinePlayers().stream()
                .filter(p -> !p.equals(player))
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList()));

            // Kontakty gracza
            PhoneUser user = plugin.getPhoneManager().getUser(player.getUniqueId());
            if (user != null) {
                suggestions.addAll(user.getContacts().stream()
                    .map(c -> c.getName())
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList()));
            }

            return suggestions;
        }

        return List.of();
    }
}
