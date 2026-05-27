package pl.skyrise.mCPhone.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import pl.skyrise.mCPhone.MCPhone;
import pl.skyrise.mCPhone.api.SMSMessage;
import pl.skyrise.mCPhone.config.GuiConfig;
import pl.skyrise.mCPhone.models.PhoneUser;
import pl.skyrise.mCPhone.utils.ColorUtils;
import pl.skyrise.mCPhone.utils.ItemBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * GUI konwersacji SMS
 */
public class SMSGUI implements InventoryHolder {

    private final MCPhone plugin;
    private final Player player;
    private final PhoneUser user;
    private final UUID partnerUUID;
    private final String partnerName;
    private final Inventory inventory;
    private int currentPage = 0;

    private static final int[] MESSAGE_SLOTS = {
            3, 4, 5, 12, 13, 14, 21, 22, 23, 30, 31, 32, 39, 40, 41
    };

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    public SMSGUI(MCPhone plugin, Player player, PhoneUser user, UUID partnerUUID, String partnerName) {
        this.plugin = plugin;
        this.player = player;
        this.user = user;
        this.partnerUUID = partnerUUID;
        this.partnerName = partnerName;

        GuiConfig guiConfig = plugin.getGuiConfig();
        this.inventory = Bukkit.createInventory(this,
                guiConfig.getPhoneGuiSize(),
                ColorUtils.toComponent(guiConfig.getEffectiveTitle()));

        // Oznacz wiadomości jako przeczytane
        plugin.getSmsManager().markAsRead(player.getUniqueId(), partnerUUID);

        setupGUI();
    }

    private void setupGUI() {
        inventory.clear();
        GuiConfig guiConfig = plugin.getGuiConfig();

        // Ramka telefonu - tylko gdy nie używamy Nexo
        if (!guiConfig.useNexo()) {
            ItemStack frame = new ItemBuilder(guiConfig.getFrameMaterial())
                    .name(guiConfig.getFrameDisplayName())
                    .customModelData(guiConfig.getFrameCustomModelData())
                    .build();
            for (int slot : guiConfig.getFrameSlots()) {
                if (slot >= 0 && slot < inventory.getSize()) {
                    inventory.setItem(slot, frame);
                }
            }
        }

        // Wiadomości
        List<SMSMessage> allMessages = plugin.getSmsManager().getConversation(
                player.getUniqueId(), partnerUUID, plugin.getConfigManager().getSmsMaxHistory());

        // Odwróć kolejność (najstarsze na górze)
        List<SMSMessage> messages = new ArrayList<>(allMessages);
        java.util.Collections.reverse(messages);

        int startIndex = currentPage * MESSAGE_SLOTS.length;
        int endIndex = Math.min(startIndex + MESSAGE_SLOTS.length, messages.size());

        for (int i = startIndex; i < endIndex; i++) {
            int slotIndex = i - startIndex;
            if (slotIndex < MESSAGE_SLOTS.length) {
                SMSMessage msg = messages.get(i);
                ItemStack msgItem = createMessageItem(msg);
                inventory.setItem(MESSAGE_SLOTS[slotIndex], msgItem);
            }
        }

        // Przycisk napisania wiadomości - na ostatnim slocie ekranu
        ItemStack composeButton = new ItemBuilder(guiConfig.getSmsComposeButtonMaterial())
                .name(guiConfig.getSmsComposeButtonDisplayName())
                .data("action", "compose")
                .data("partner_uuid", partnerUUID.toString())
                .build();
        inventory.setItem(41, composeButton);

        // Nawigacja dolna
        int totalPages = (int) Math.ceil((double) messages.size() / MESSAGE_SLOTS.length);

        // Lewy - powrót
        ItemStack backButton = new ItemBuilder(guiConfig.getNavLeftMaterial())
                .name("&7◄ Powrót")
                .lore("&8Kliknij aby wrócić")
                .data("nav_action", "back")
                .build();
        inventory.setItem(guiConfig.getNavLeftSlot(), backButton);

        // Środkowy - pulpit
        ItemStack homeButton = new ItemBuilder(guiConfig.getNavHomeMaterial())
                .name(guiConfig.getNavHomeDisplayName())
                .lore(guiConfig.getNavHomeLore())
                .data("nav_action", "home")
                .build();
        inventory.setItem(guiConfig.getNavHomeSlot(), homeButton);

        // Prawy - następna strona
        if (totalPages > 1 && currentPage < totalPages - 1) {
            ItemStack nextPage = new ItemBuilder(guiConfig.getNavRightMaterial())
                    .name(guiConfig.getNavRightDisplayName())
                    .lore(guiConfig.getNavRightLore())
                    .data("nav_action", "next_page")
                    .build();
            inventory.setItem(guiConfig.getNavRightSlot(), nextPage);
        }
    }

    private ItemStack createMessageItem(SMSMessage msg) {
        boolean isSent = msg.getSender().equals(player.getUniqueId());
        Material material = isSent ? Material.LIME_STAINED_GLASS_PANE : Material.LIGHT_BLUE_STAINED_GLASS_PANE;

        String prefix = isSent ? "&a➤ Ty" : "&b◄ " + partnerName;
        String dateStr = DATE_FORMAT.format(new Date(msg.getTimestamp()));

        // Podziel długą wiadomość na linie
        String message = msg.getMessage();
        List<String> messageLore = new ArrayList<>();

        int maxLineLength = 40;
        while (message.length() > maxLineLength) {
            int breakIndex = message.lastIndexOf(' ', maxLineLength);
            if (breakIndex == -1) breakIndex = maxLineLength;
            messageLore.add("&f" + message.substring(0, breakIndex));
            message = message.substring(breakIndex).trim();
        }
        if (!message.isEmpty()) {
            messageLore.add("&f" + message);
        }

        messageLore.add("");
        messageLore.add("&8" + dateStr);

        return new ItemBuilder(material)
                .name(prefix)
                .lore(messageLore)
                .build();
    }

    public void open() {
        player.openInventory(inventory);
    }

    public void nextPage() {
        List<SMSMessage> messages = plugin.getSmsManager().getConversation(
                player.getUniqueId(), partnerUUID, plugin.getConfigManager().getSmsMaxHistory());
        int totalPages = (int) Math.ceil((double) messages.size() / MESSAGE_SLOTS.length);
        if (currentPage < totalPages - 1) {
            currentPage++;
            setupGUI();
        }
    }

    public void prevPage() {
        if (currentPage > 0) {
            currentPage--;
            setupGUI();
        }
    }

    public void refresh() {
        setupGUI();
    }

    public UUID getPartnerUUID() {
        return partnerUUID;
    }

    public String getPartnerName() {
        return partnerName;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
