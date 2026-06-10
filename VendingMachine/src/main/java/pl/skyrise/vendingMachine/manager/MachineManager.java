package pl.skyrise.vendingMachine.manager;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.skyrise.vendingMachine.VendingMachine;
import pl.skyrise.vendingMachine.model.MachineTemplate;
import pl.skyrise.vendingMachine.model.VendingItem;

import java.util.*;

public class MachineManager {

    private final VendingMachine plugin;
    private final Map<String, MachineTemplate> templates;

    public MachineManager(VendingMachine plugin) {
        this.plugin = plugin;
        this.templates = new LinkedHashMap<>();
    }

    public MachineTemplate createTemplate(String name) {
        if (templates.containsKey(name.toLowerCase())) return null;

        MachineTemplate template = new MachineTemplate(name);
        template.setRows(plugin.getConfig().getInt("default-template.rows", 5));
        template.setTitle(plugin.getConfig().getString("default-template.title", "&8Automat"));
        template.setFillEmpty(plugin.getConfig().getBoolean("default-template.fill-empty", false));

        try { template.setFillerMaterial(Material.valueOf(plugin.getConfig().getString("default-template.filler-material", "BLACK_STAINED_GLASS_PANE"))); }
        catch (Exception e) { template.setFillerMaterial(Material.BLACK_STAINED_GLASS_PANE); }
        template.setFillerName(plugin.getConfig().getString("default-template.filler-name", " "));
        template.setBorder(plugin.getConfig().getBoolean("default-template.border", false));

        templates.put(name.toLowerCase(), template);
        plugin.getDataManager().saveAll();
        return template;
    }

    public boolean deleteTemplate(String name) {
        MachineTemplate removed = templates.remove(name.toLowerCase());
        if (removed != null) { plugin.getDataManager().saveAll(); return true; }
        return false;
    }

    public MachineTemplate getTemplate(String name) { return templates.get(name.toLowerCase()); }
    public Collection<MachineTemplate> getAllTemplates() { return templates.values(); }
    public Set<String> getTemplateNames() { return templates.keySet(); }
    public void addTemplate(MachineTemplate t) { templates.put(t.getName().toLowerCase(), t); }

    public String generateItemId(MachineTemplate template) {
        int counter = 1;
        while (template.getItem("item_" + counter) != null) counter++;
        return "item_" + counter;
    }

    /**
     * Tworzy VendingItem z trzymanego itemu - zachowuje WSZYSTKIE dane (NBT, PDC, custom)
     * Dzięki temu działa z napojami z pluginów nawodnienia, Nexo itp.
     */
    public VendingItem createItemFromHand(Player player, MachineTemplate template, double price) {
        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem.getType() == Material.AIR) return null;

        String id = generateItemId(template);
        VendingItem vendingItem = new VendingItem(id);

        // KLUCZOWE: Użyj setFromItemStack które zapisuje pełen item
        vendingItem.setFromItemStack(handItem.clone());
        vendingItem.setPrice(price);

        int freeSlot = template.getNextFreeSlot();
        if (freeSlot == -1) return null;
        vendingItem.setSlot(freeSlot);
        return vendingItem;
    }

    private String formatMaterialName(Material material) {
        String name = material.name().replace("_", " ").toLowerCase();
        StringBuilder formatted = new StringBuilder();
        for (String word : name.split(" ")) {
            if (!word.isEmpty()) formatted.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
        }
        return formatted.toString().trim();
    }

    public void save() { plugin.getDataManager().saveAll(); }
}