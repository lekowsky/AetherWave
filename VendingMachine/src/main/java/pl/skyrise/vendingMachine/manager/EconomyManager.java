package pl.skyrise.vendingMachine.manager;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import pl.skyrise.vendingMachine.VendingMachine;

public class EconomyManager {

    private final VendingMachine plugin;
    private Economy economy;

    public EconomyManager(VendingMachine plugin) {
        this.plugin = plugin;
    }

    public boolean setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return economy != null;
    }

    public double getBalance(Player p) { return economy.getBalance(p); }
    public boolean hasEnough(Player p, double a) { return economy.has(p, a); }
    public boolean withdraw(Player p, double a) { return economy.withdrawPlayer(p, a).transactionSuccess(); }
    public boolean deposit(Player p, double a) { return economy.depositPlayer(p, a).transactionSuccess(); }
    public String getCurrencySymbol() { return plugin.getConfig().getString("currency-symbol", "$"); }
}