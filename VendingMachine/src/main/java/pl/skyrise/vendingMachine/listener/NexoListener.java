package pl.skyrise.vendingMachine.listener;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import pl.skyrise.vendingMachine.VendingMachine;
import pl.skyrise.vendingMachine.gui.MachineGUI;
import pl.skyrise.vendingMachine.model.MachinePlacement;
import pl.skyrise.vendingMachine.model.MachineTemplate;
import pl.skyrise.vendingMachine.util.ColorUtil;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NexoListener implements Listener {

    private final VendingMachine plugin;
    private boolean nexoAvailable;
    private final Map<UUID, Long> interactCooldowns = new HashMap<>();

    public NexoListener(VendingMachine plugin) {
        this.plugin = plugin;
        registerNexoEvents();
    }

    private void registerNexoEvents() {
        try {
            Class<?> placeEventClass = tryLoadClass("com.nexomc.nexo.api.events.furniture.NexoFurniturePlaceEvent");
            Class<?> interactEventClass = tryLoadClass("com.nexomc.nexo.api.events.furniture.NexoFurnitureInteractEvent");
            Class<?> breakEventClass = tryLoadClass("com.nexomc.nexo.api.events.furniture.NexoFurnitureBreakEvent");

            if (interactEventClass == null) {
                plugin.getLogger().warning("Nexo classes not found - integration disabled.");
                nexoAvailable = false;
                return;
            }

            nexoAvailable = true;

            if (placeEventClass != null) {
                registerEvent(placeEventClass, EventPriority.MONITOR, this::handlePlaceReflection);
                plugin.getLogger().info("Nexo PLACE event registered - auto-registration enabled!");
            } else {
                plugin.getLogger().warning("NexoFurniturePlaceEvent not found - auto-registration disabled.");
            }

            registerEvent(interactEventClass, EventPriority.NORMAL, this::handleInteractReflection);

            if (breakEventClass != null) {
                registerEvent(breakEventClass, EventPriority.MONITOR, this::handleBreakReflection);
            }

            plugin.getLogger().info("Nexo events registered successfully!");

        } catch (Throwable t) {
            plugin.getLogger().warning("Failed to register Nexo events: " + t.getMessage());
            nexoAvailable = false;
        }
    }

    @SuppressWarnings("unchecked")
    private void registerEvent(Class<?> eventClass, EventPriority priority, EventHandlerCallback callback) {
        Bukkit.getPluginManager().registerEvent(
                (Class<? extends Event>) eventClass,
                this,
                priority,
                (EventExecutor) (listener, event) -> {
                    try {
                        callback.handle(event);
                    } catch (Throwable t) {
                        plugin.getLogger().warning("Error in Nexo event handler: " + t.getMessage());
                    }
                },
                plugin
        );
    }

    // ==================== POSTAWIENIE FURNITURE ====================

    private void handlePlaceReflection(Event event) throws Exception {
        String nexoItemId = getMechanicItemID(event);
        if (nexoItemId == null) return;

        String templateName = plugin.getNexoManager().getTemplateName(nexoItemId);
        if (templateName == null) return;

        if (!plugin.getNexoManager().isAutoRegister()) return;

        MachineTemplate template = plugin.getMachineManager().getTemplate(templateName);
        if (template == null) {
            plugin.getLogger().warning("Furniture '" + nexoItemId + "' mapuje na nieistniejący szablon '" + templateName + "'");
            return;
        }

        Method getBaseEntityMethod = event.getClass().getMethod("getBaseEntity");
        Entity baseEntity = (Entity) getBaseEntityMethod.invoke(event);
        if (baseEntity == null) return;

        Location blockLoc = baseEntity.getLocation().getBlock().getLocation();

        Player player = null;
        try {
            Method getPlayerMethod = event.getClass().getMethod("getPlayer");
            player = (Player) getPlayerMethod.invoke(event);
        } catch (NoSuchMethodException ignored) {}

        if (plugin.getPlacementManager().getPlacement(blockLoc) != null) return;

        MachinePlacement newPlacement = plugin.getPlacementManager().place(
                templateName, blockLoc, nexoItemId,
                player != null ? player.getUniqueId() : null);

        if (newPlacement != null) {
            plugin.getPlacementManager().registerEntityUUID(newPlacement, baseEntity.getUniqueId().toString());
            plugin.getDataManager().savePlacements();

            plugin.getLogger().info("Auto-registered automat '" + templateName + "' at " + blockLoc);

            if (player != null) {
                player.sendMessage(plugin.getPrefix() + ColorUtil.color(
                        plugin.getConfig().getString("messages.placed", "&aPostawiono automat (&e{template}&a).")
                                .replace("{template}", templateName)));
            }
        }
    }

    // ==================== INTERAKCJA ====================

    private void handleInteractReflection(Event event) throws Exception {
        Method getPlayerMethod = event.getClass().getMethod("getPlayer");
        Player player = (Player) getPlayerMethod.invoke(event);

        // Tylko główna ręka (ignoruj off-handę)
        try {
            Method getHandMethod = event.getClass().getMethod("getHand");
            Object hand = getHandMethod.invoke(event);
            if (hand != null && !hand.toString().equals("HAND")) return;
        } catch (Throwable ignored) {}

        // Cooldown anty-spam
        long cooldownMs = plugin.getConfig().getLong("interact-cooldown-ms", 500);
        long now = System.currentTimeMillis();
        Long last = interactCooldowns.get(player.getUniqueId());
        if (last != null && (now - last) < cooldownMs) {
            cancelEvent(event);
            return;
        }
        interactCooldowns.put(player.getUniqueId(), now);

        Method getBaseEntityMethod = event.getClass().getMethod("getBaseEntity");
        Entity baseEntity = (Entity) getBaseEntityMethod.invoke(event);
        if (baseEntity == null) return;

        String entityUUID = baseEntity.getUniqueId().toString();
        Location blockLoc = baseEntity.getLocation().getBlock().getLocation();

        MachinePlacement placement = plugin.getPlacementManager().getPlacementByEntityUUID(entityUUID);

        if (placement == null) {
            placement = plugin.getPlacementManager().getPlacement(blockLoc);
            if (placement != null && placement.getNexoEntityUUID() == null) {
                plugin.getPlacementManager().registerEntityUUID(placement, entityUUID);
                plugin.getDataManager().savePlacements();
            }
        }

        if (placement == null) {
            String nexoItemId = getMechanicItemID(event);
            if (nexoItemId != null && plugin.getNexoManager().isAutoRegister()) {
                String templateName = plugin.getNexoManager().getTemplateName(nexoItemId);
                if (templateName != null && plugin.getMachineManager().getTemplate(templateName) != null) {
                    placement = plugin.getPlacementManager().place(
                            templateName, blockLoc, nexoItemId, player.getUniqueId());
                    if (placement != null) {
                        plugin.getPlacementManager().registerEntityUUID(placement, entityUUID);
                        plugin.getDataManager().savePlacements();
                        plugin.getLogger().info("Late auto-registered Nexo '" + nexoItemId +
                                "' as template '" + templateName + "'");
                    }
                }
            }
        }

        if (placement != null) {
            openMachine(player, placement);
            cancelEvent(event);
        }
    }

    // ==================== ZNISZCZENIE ====================

    private void handleBreakReflection(Event event) throws Exception {
        Method getBaseEntityMethod = event.getClass().getMethod("getBaseEntity");
        Entity baseEntity = (Entity) getBaseEntityMethod.invoke(event);
        if (baseEntity == null) return;

        String entityUUID = baseEntity.getUniqueId().toString();
        MachinePlacement placement = plugin.getPlacementManager().getPlacementByEntityUUID(entityUUID);

        if (placement == null) {
            Location blockLoc = baseEntity.getLocation().getBlock().getLocation();
            placement = plugin.getPlacementManager().getPlacement(blockLoc);
        }

        if (placement == null) return;

        Player player = null;
        try {
            Method getPlayerMethod = event.getClass().getMethod("getPlayer");
            player = (Player) getPlayerMethod.invoke(event);
        } catch (NoSuchMethodException ignored) {}

        if (!plugin.getConfig().getBoolean("protection.allow-player-break", false)) {
            if (player != null && !player.hasPermission("vendingmachine.delete")) {
                cancelEvent(event);
                player.sendMessage(plugin.getPrefix() + ColorUtil.color(
                        plugin.getConfig().getString("messages.no-permission", "&cBrak uprawnień!")));
                return;
            }
        }

        plugin.getPlacementManager().removeByPlacement(placement);
        plugin.getLogger().info("Removed placement at " + placement.getLocation());

        if (player != null) {
            player.sendMessage(plugin.getPrefix() + ColorUtil.color(
                    plugin.getConfig().getString("messages.removed", "&cUsunięto automat.")));
        }
    }

    // ==================== POMOCNICZE ====================

    private String getMechanicItemID(Event event) {
        try {
            Method getMechanicMethod = event.getClass().getMethod("getMechanic");
            Object mechanic = getMechanicMethod.invoke(event);
            if (mechanic == null) return null;

            String[] possibleMethods = {"getItemID", "itemID", "getItemId", "itemId"};
            for (String methodName : possibleMethods) {
                try {
                    Method m = mechanic.getClass().getMethod(methodName);
                    Object result = m.invoke(mechanic);
                    if (result != null) return result.toString();
                } catch (NoSuchMethodException ignored) {}
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private void cancelEvent(Event event) {
        try {
            Method setCancelledMethod = event.getClass().getMethod("setCancelled", boolean.class);
            setCancelledMethod.invoke(event, true);
        } catch (Exception ignored) {}
    }

    private Class<?> tryLoadClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private void openMachine(Player player, MachinePlacement placement) {
        MachineTemplate template = plugin.getMachineManager().getTemplate(placement.getTemplateName());
        if (template == null) {
            player.sendMessage(plugin.getPrefix() + ColorUtil.color("&cSzablon nie znaleziony!"));
            return;
        }

        if (!template.isEnabled()) {
            player.sendMessage(plugin.getPrefix() + ColorUtil.color(
                    plugin.getConfig().getString("messages.disabled", "&cWylaczony!")));
            return;
        }

        if (!template.getPermission().isEmpty() && !player.hasPermission(template.getPermission())) {
            player.sendMessage(plugin.getPrefix() + ColorUtil.color(
                    plugin.getConfig().getString("messages.no-permission", "&cBrak uprawnien!")));
            return;
        }

        try {
            String soundName = plugin.getConfig().getString("sounds.open", "UI_BUTTON_CLICK");
            player.playSound(player.getLocation(), Sound.valueOf(soundName), 0.5f, 1.0f);
        } catch (Exception ignored) {}

        new MachineGUI(plugin, template, player, placement).open();
    }

    public boolean isNexoAvailable() {
        return nexoAvailable;
    }

    @FunctionalInterface
    private interface EventHandlerCallback {
        void handle(Event event) throws Exception;
    }
}