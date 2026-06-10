package pl.skyrise.vendingMachine.manager;

import pl.skyrise.vendingMachine.VendingMachine;

import java.util.HashMap;
import java.util.Map;

public class NexoManager {

    private final VendingMachine plugin;
    private boolean nexoAvailable;
    private final Map<String, String> furnitureMappings;

    public NexoManager(VendingMachine plugin) {
        this.plugin = plugin;
        this.furnitureMappings = new HashMap<>();
        checkNexo();
        loadMappings();
    }

    private void checkNexo() {
        nexoAvailable = plugin.getServer().getPluginManager().getPlugin("Nexo") != null;
        if (nexoAvailable) plugin.getLogger().info("Nexo detected!");
        else plugin.getLogger().info("Nexo not found.");
    }

    public void loadMappings() {
        furnitureMappings.clear();
        if (!plugin.getConfig().getBoolean("nexo.enabled", true)) return;

        var section = plugin.getConfig().getConfigurationSection("nexo.furniture-mappings");
        if (section == null) return;

        for (String nexoId : section.getKeys(false)) {
            String templateName = section.getString(nexoId);
            furnitureMappings.put(nexoId, templateName);
            plugin.getLogger().info("Nexo mapping: " + nexoId + " -> '" + templateName + "'");
        }
    }

    public boolean isNexoAvailable() { return nexoAvailable; }
    public String getTemplateName(String nexoItemId) { return nexoItemId == null ? null : furnitureMappings.get(nexoItemId); }
    public Map<String, String> getFurnitureMappings() { return furnitureMappings; }
    public boolean isAutoRegister() { return plugin.getConfig().getBoolean("nexo.auto-register-furniture", true); }
    public boolean allowVanillaBlocks() { return plugin.getConfig().getBoolean("nexo.allow-vanilla-blocks", true); }
}