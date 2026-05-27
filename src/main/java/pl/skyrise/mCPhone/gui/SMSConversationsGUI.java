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

import java.util.List;
import java.util.UUID;

/**
 * GUI listy konwersacji SMS
 */
public class SMSConversationsGUI implements InventoryHolder {

    private final MCPhone plugin;
    private final Player player;
    private final PhoneUser user;
    private final Inventory inventory;
    private int currentPage = 0;

    private static final int[] CONVERSATION_SLOTS = {
            3, 4, 5, 12, 13, 14, 21, 22, 23, 30, 31, 32, 39, 40, 41
    };

    public SMSConversationsGUI(MCPhone plugin, Player player, PhoneUser user) {
        this.plugin = plugin;
        this.player = player;
        this.user = user;

        GuiConfig guiConfig = plugin.getGuiConfig();
        this.inventory = Bukkit.createInventory(this, guiConfig.getPhoneGuiSize(),
                guiConfig.getTitleComponent());

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

        // Konwersacje
        List<UUID> partners = plugin.getSmsManager().getConversationPartners(player.getUniqueId());

        int startIndex = currentPage * CONVERSATION_SLOTS.length;
        int endIndex = Math.min(startIndex + CONVERSATION_SLOTS.length, partners.size());

        for (int i = startIndex; i < endIndex; i++) {
            int slotIndex = i - startIndex;
            if (slotIndex < CONVERSATION_SLOTS.length) {
                UUID partnerUUID = partners.get(i);
                ItemStack convItem = createConversationItem(partnerUUID);
                inventory.setItem(CONVERSATION_SLOTS[slotIndex], convItem);
            }
        }

        // Nawigacja dolna
        int totalPages = (int) Math.ceil((double) partners.size() / CONVERSATION_SLOTS.length);

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

    private ItemStack createConversationItem(UUID partnerUUID) {
        // Pobierz nazwę partnera
        String partnerName = getPartnerName(partnerUUID);

        // Pobierz ostatnią wiadomość
        SMSMessage lastMsg = plugin.getSmsManager().getLastMessage(player.getUniqueId(), partnerUUID);
        String lastMsgPreview = "Brak wiadomości";
        if (lastMsg != null) {
            String msg = lastMsg.getMessage();
            if (msg.length() > 30) {
                msg = msg.substring(0, 30) + "...";
            }
            boolean isSent = lastMsg.getSender().equals(player.getUniqueId());
            lastMsgPreview = (isSent ? "Ty: " : partnerName + ": ") + msg;
        }

        // Sprawdź nieprzeczytane
        List<SMSMessage> unread = plugin.getSmsManager().getConversation(player.getUniqueId(), partnerUUID, 100);
        int unreadCount = (int) unread.stream()
                .filter(m -> m.getReceiver().equals(player.getUniqueId()) && !m.isRead())
                .count();

        Material material = unreadCount > 0 ? Material.PAPER : Material.FILLED_MAP;
        String nameSuffix = unreadCount > 0 ? " &c(" + unreadCount + " nowe)" : "";

        return new ItemBuilder(material)
                .name("&f" + partnerName + nameSuffix)
                .lore(
                        "&7Ostatnia wiadomość:",
                        "&8" + lastMsgPreview,
                        "",
                        "&eKliknij aby otworzyć"
                )
                .data("partner_uuid", partnerUUID.toString())
                .data("partner_name", partnerName)
                .build();
    }

    private String getPartnerName(UUID partnerUUID) {
        // Sprawdź w kontaktach
        PhoneUser partnerUser = plugin.getPhoneManager().getUser(partnerUUID);
        if (partnerUser != null && partnerUser.getPhoneNumber() != null) {
            String contactName = user.getContactNameOrNumber(partnerUser.getPhoneNumber());
            if (!contactName.equals(partnerUser.getPhoneNumber())) {
                return contactName;
            }
        }

        // Sprawdź nick gracza
        Player partner = Bukkit.getPlayer(partnerUUID);
        if (partner != null) {
            return partner.getName();
        }

        return Bukkit.getOfflinePlayer(partnerUUID).getName();
    }

    public void open() {
        player.openInventory(inventory);
    }

    public void nextPage() {
        List<UUID> partners = plugin.getSmsManager().getConversationPartners(player.getUniqueId());
        int totalPages = (int) Math.ceil((double) partners.size() / CONVERSATION_SLOTS.length);
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

    public Player getPlayer() {
        return player;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
