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
import pl.skyrise.mCPhone.models.SimType;
import pl.skyrise.mCPhone.utils.ColorUtils;
import pl.skyrise.mCPhone.utils.NumberGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Komenda /phoneadmin - komendy administracyjne
 */
public class PhoneAdminCommand implements CommandExecutor, TabCompleter {

    private final MCPhone plugin;

    public PhoneAdminCommand(MCPhone plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("mcphone.admin")) {
            sender.sendMessage(plugin.getMessagesConfig().noPermission());
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload" -> handleReload(sender);
            case "give" -> handleGive(sender, args);
            case "number" -> handleNumber(sender, args);
            case "simshop" -> handleSimShop(sender, args);
            case "info" -> handleInfo(sender, args);
            default -> sendHelp(sender);
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ColorUtils.colorize("&5&l═══ MCPhone Admin ═══"));
        sender.sendMessage(ColorUtils.colorize("&d/phoneadmin reload &7- Przeładuj konfigurację"));
        sender.sendMessage(ColorUtils.colorize("&d/phoneadmin give phone <gracz> &7- Daj telefon"));
        sender.sendMessage(ColorUtils.colorize("&d/phoneadmin give sim <gracz> <typ> &7- Daj kartę SIM"));
        sender.sendMessage(ColorUtils.colorize("&d/phoneadmin number set <gracz> <numer> &7- Ustaw numer"));
        sender.sendMessage(ColorUtils.colorize("&d/phoneadmin number remove <gracz> &7- Usuń numer"));
        sender.sendMessage(ColorUtils.colorize("&d/phoneadmin simshop add &7- Dodaj punkt sprzedaży"));
        sender.sendMessage(ColorUtils.colorize("&d/phoneadmin simshop remove <id> &7- Usuń punkt sprzedaży"));
        sender.sendMessage(ColorUtils.colorize("&d/phoneadmin info <gracz> &7- Informacje o graczu"));
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("mcphone.admin.reload")) {
            sender.sendMessage(plugin.getMessagesConfig().noPermission());
            return;
        }

        plugin.reload();
        sender.sendMessage(plugin.getMessagesConfig().reloadSuccess());
    }

    private void handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mcphone.admin.give")) {
            sender.sendMessage(plugin.getMessagesConfig().noPermission());
            return;
        }

        if (args.length < 3) {
            sender.sendMessage(plugin.getMessagesConfig().invalidArgs("/phoneadmin give <phone/sim> <gracz> [typ]"));
            return;
        }

        String type = args[1].toLowerCase();
        Player target = Bukkit.getPlayer(args[2]);

        if (target == null) {
            sender.sendMessage(plugin.getMessagesConfig().playerNotFound(args[2]));
            return;
        }

        switch (type) {
            case "phone" -> {
                if (plugin.getPhoneManager().hasPhone(target)) {
                    sender.sendMessage(plugin.getMessagesConfig().phoneAlreadyHas());
                    return;
                }
                plugin.getPhoneManager().givePhone(target);
                target.sendMessage(plugin.getMessagesConfig().phoneReceived());
                sender.sendMessage(plugin.getMessagesConfig().adminGivePhone(target.getName()));
            }
            case "sim" -> {
                if (args.length < 4) {
                    sender.sendMessage(plugin.getMessagesConfig().invalidArgs("/phoneadmin give sim <gracz> <typ>"));
                    return;
                }
                String simType = args[3].toLowerCase();
                SimType sim = plugin.getSimManager().getSimType(simType);
                if (sim == null) {
                    sender.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getPrefix() +
                        "&cNieznany typ karty SIM: " + simType));
                    return;
                }
                plugin.getSimManager().giveSimCard(target, simType);
                sender.sendMessage(plugin.getMessagesConfig().adminGiveSim(sim.getName(), target.getName()));
            }
            default -> sender.sendMessage(plugin.getMessagesConfig().invalidArgs("/phoneadmin give <phone/sim> <gracz>"));
        }
    }

    private void handleNumber(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mcphone.admin.number")) {
            sender.sendMessage(plugin.getMessagesConfig().noPermission());
            return;
        }

        if (args.length < 3) {
            sender.sendMessage(plugin.getMessagesConfig().invalidArgs("/phoneadmin number <set/remove> <gracz> [numer]"));
            return;
        }

        String action = args[1].toLowerCase();
        Player target = Bukkit.getPlayer(args[2]);
        UUID targetUUID;

        if (target != null) {
            targetUUID = target.getUniqueId();
        } else {
            targetUUID = Bukkit.getOfflinePlayer(args[2]).getUniqueId();
        }

        PhoneUser user = plugin.getPhoneManager().getOrCreateUser(targetUUID);

        switch (action) {
            case "set" -> {
                if (args.length < 4) {
                    sender.sendMessage(plugin.getMessagesConfig().invalidArgs("/phoneadmin number set <gracz> <numer>"));
                    return;
                }
                String number = args[3];
                if (!NumberGenerator.isValid(number)) {
                    number = NumberGenerator.format(number);
                }
                if (plugin.getPhoneManager().isNumberTaken(number)) {
                    sender.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getPrefix() +
                        "&cNumer " + number + " jest już zajęty!"));
                    return;
                }
                user.setPhoneNumber(number);
                user.setSimActive(true);
                plugin.getPhoneManager().saveUser(user);
                sender.sendMessage(plugin.getMessagesConfig().adminSetNumber(number, args[2]));
            }
            case "remove" -> {
                user.setPhoneNumber(null);
                user.setSimActive(false);
                user.setSimType(null);
                plugin.getPhoneManager().saveUser(user);
                sender.sendMessage(plugin.getMessagesConfig().adminRemoveNumber(args[2]));
            }
            default -> sender.sendMessage(plugin.getMessagesConfig().invalidArgs("/phoneadmin number <set/remove> <gracz>"));
        }
    }

    private void handleSimShop(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessagesConfig().playerOnly());
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(plugin.getMessagesConfig().invalidArgs("/phoneadmin simshop <add/remove/list>"));
            return;
        }

        String action = args[1].toLowerCase();

        switch (action) {
            case "add" -> {
                String id = "shop_" + System.currentTimeMillis();
                String name = "&ePunkt Sprzedaży SIM";
                int radius = plugin.getConfigManager().getSimShopRadius();
                List<String> availableSims = plugin.getSimManager().getAllSimTypes().stream()
                    .map(SimType::getId)
                    .collect(Collectors.toList());

                plugin.getSimManager().addSimShop(id, name, player.getLocation(), radius, availableSims);
                sender.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getPrefix() +
                    "&aDodano punkt sprzedaży SIM na twojej pozycji! ID: " + id));
            }
            case "remove" -> {
                if (args.length < 3) {
                    sender.sendMessage(plugin.getMessagesConfig().invalidArgs("/phoneadmin simshop remove <id>"));
                    return;
                }
                String id = args[2];
                if (plugin.getSimManager().getSimShop(id) == null) {
                    sender.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getPrefix() +
                        "&cNie znaleziono punktu sprzedaży o ID: " + id));
                    return;
                }
                plugin.getSimManager().removeSimShop(id);
                sender.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getPrefix() +
                    "&cUsunięto punkt sprzedaży: " + id));
            }
            case "list" -> {
                sender.sendMessage(ColorUtils.colorize("&5&l═══ Punkty sprzedaży SIM ═══"));
                plugin.getSimManager().getAllSimShops().forEach(shop -> {
                    sender.sendMessage(ColorUtils.colorize("&d" + shop.getId() + " &7- " + shop.getName() +
                        " &8(" + shop.getLocation().getBlockX() + ", " +
                        shop.getLocation().getBlockY() + ", " +
                        shop.getLocation().getBlockZ() + ")"));
                });
            }
            default -> sender.sendMessage(plugin.getMessagesConfig().invalidArgs("/phoneadmin simshop <add/remove/list>"));
        }
    }

    private void handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.getMessagesConfig().invalidArgs("/phoneadmin info <gracz>"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        UUID targetUUID;
        String targetName = args[1];

        if (target != null) {
            targetUUID = target.getUniqueId();
            targetName = target.getName();
        } else {
            targetUUID = Bukkit.getOfflinePlayer(args[1]).getUniqueId();
        }

        PhoneUser user = plugin.getPhoneManager().getUser(targetUUID);

        sender.sendMessage(ColorUtils.colorize("&5&l═══ Informacje o " + targetName + " ═══"));

        if (user == null) {
            sender.sendMessage(ColorUtils.colorize("&7Brak danych o tym graczu."));
            return;
        }

        sender.sendMessage(ColorUtils.colorize("&dNumer telefonu: &f" + (user.getPhoneNumber() != null ? user.getPhoneNumber() : "Brak")));
        sender.sendMessage(ColorUtils.colorize("&dTyp SIM: &f" + (user.getSimType() != null ? user.getSimType() : "Brak")));
        sender.sendMessage(ColorUtils.colorize("&dSIM aktywna: &f" + (user.isSimActive() ? "&aTak" : "&cNie")));
        sender.sendMessage(ColorUtils.colorize("&dDzienne SMS: &f" + user.getDailySmsCount()));
        sender.sendMessage(ColorUtils.colorize("&dZainstalowane aplikacje: &f" + user.getInstalledApps().size()));
        sender.sendMessage(ColorUtils.colorize("&dKontakty: &f" + user.getContacts().size()));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("mcphone.admin")) {
            return List.of();
        }

        if (args.length == 1) {
            return filterStartsWith(Arrays.asList("reload", "give", "number", "simshop", "info"), args[0]);
        }

        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "give" -> { return filterStartsWith(Arrays.asList("phone", "sim"), args[1]); }
                case "number" -> { return filterStartsWith(Arrays.asList("set", "remove"), args[1]); }
                case "simshop" -> { return filterStartsWith(Arrays.asList("add", "remove", "list"), args[1]); }
                case "info" -> { return getOnlinePlayerNames(args[1]); }
            }
        }

        if (args.length == 3) {
            switch (args[0].toLowerCase()) {
                case "give", "number" -> { return getOnlinePlayerNames(args[2]); }
                case "simshop" -> {
                    if (args[1].equalsIgnoreCase("remove")) {
                        return filterStartsWith(
                            plugin.getSimManager().getAllSimShops().stream()
                                .map(s -> s.getId())
                                .collect(Collectors.toList()),
                            args[2]
                        );
                    }
                }
            }
        }

        if (args.length == 4) {
            if (args[0].equalsIgnoreCase("give") && args[1].equalsIgnoreCase("sim")) {
                return filterStartsWith(
                    plugin.getSimManager().getAllSimTypes().stream()
                        .map(SimType::getId)
                        .collect(Collectors.toList()),
                    args[3]
                );
            }
        }

        return List.of();
    }

    private List<String> filterStartsWith(List<String> options, String prefix) {
        return options.stream()
            .filter(s -> s.toLowerCase().startsWith(prefix.toLowerCase()))
            .collect(Collectors.toList());
    }

    private List<String> getOnlinePlayerNames(String prefix) {
        return Bukkit.getOnlinePlayers().stream()
            .map(Player::getName)
            .filter(name -> name.toLowerCase().startsWith(prefix.toLowerCase()))
            .collect(Collectors.toList());
    }
}
