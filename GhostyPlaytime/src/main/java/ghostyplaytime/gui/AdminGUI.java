package ghostyplaytime.gui;

import ghostyplaytime.GhostyPlaytime;
import ghostyplaytime.LanguageManager;
import ghostyplaytime.PlaytimeManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Admin GUI Layout (54 slots):
 *
 * Rows 0-4 (slots 0-44): Player heads (up to 45 per page), sorted by playtime desc.
 * Row 5 (nav bar):
 *   Slot 45: Previous page
 *   Slot 48: Close
 *   Slot 49: Page info
 *   Slot 53: Next page
 */
public class AdminGUI {

    private static final int CONTENT_SLOTS = 45;
    private static final int SLOT_PREV = 45;
    private static final int SLOT_CLOSE = 48;
    private static final int SLOT_PAGE_INFO = 49;
    private static final int SLOT_NEXT = 53;

    private final Map<UUID, Integer> adminPages = new HashMap<>();
    private final Map<UUID, UUID> adminDetailTarget = new HashMap<>();

    private final GhostyPlaytime plugin;

    public AdminGUI(GhostyPlaytime plugin) {
        this.plugin = plugin;
    }

    public void openList(Player admin, int page) {
        LanguageManager lang = plugin.getLanguageManager();
        PlaytimeManager pm = plugin.getPlaytimeManager();

        List<Map.Entry<UUID, Long>> allPlayers = pm.getAllSorted();
        int totalPages = Math.max(1, (int) Math.ceil(allPlayers.size() / (double) CONTENT_SLOTS));
        page = Math.max(0, Math.min(page, totalPages - 1));
        adminPages.put(admin.getUniqueId(), page);
        adminDetailTarget.remove(admin.getUniqueId());

        String title = lang.get("gui.admin.all-title");
        Inventory inv = Bukkit.createInventory(null, 54, title);

        // Nav bar glass panes
        Material border = getBorderMaterial();
        ItemStack borderItem = PlayerGUI.createItem(border, " ", new ArrayList<>());
        for (int i = 45; i < 54; i++) {
            inv.setItem(i, borderItem);
        }

        // Content
        int startIndex = page * CONTENT_SLOTS;
        for (int i = 0; i < CONTENT_SLOTS; i++) {
            int playerIndex = startIndex + i;
            if (playerIndex >= allPlayers.size()) break;

            Map.Entry<UUID, Long> entry = allPlayers.get(playerIndex);
            String name = pm.getPlayerName(entry.getKey());
            String time = pm.formatTime(entry.getValue());
            int rank = playerIndex + 1;

            ItemStack head = PlayerGUI.createSkull(entry.getKey(),
                    lang.get("gui.admin.player-head.name", "%player%", name),
                    lang.getLore("gui.admin.player-head.lore",
                            "%player%", name,
                            "%time%", time,
                            "%rank%", String.valueOf(rank)));
            inv.setItem(i, head);
        }

        String pageStr = String.valueOf(page + 1);
        String maxPageStr = String.valueOf(totalPages);
        String totalStr = String.valueOf(allPlayers.size());

        // Prev
        if (page > 0) {
            inv.setItem(SLOT_PREV, PlayerGUI.createItem(Material.ARROW,
                    lang.get("gui.admin.prev-page.name", "%page%", String.valueOf(page), "%max_page%", maxPageStr),
                    lang.getLore("gui.admin.prev-page.lore", "%page%", String.valueOf(page), "%max_page%", maxPageStr)));
        }

        // Page info
        inv.setItem(SLOT_PAGE_INFO, PlayerGUI.createItem(Material.PAPER,
                lang.get("gui.admin.page-info.name", "%page%", pageStr, "%max_page%", maxPageStr),
                lang.getLore("gui.admin.page-info.lore", "%page%", pageStr, "%max_page%", maxPageStr, "%total%", totalStr)));

        // Next
        if (page < totalPages - 1) {
            inv.setItem(SLOT_NEXT, PlayerGUI.createItem(Material.ARROW,
                    lang.get("gui.admin.next-page.name", "%page%", String.valueOf(page + 2), "%max_page%", maxPageStr),
                    lang.getLore("gui.admin.next-page.lore", "%page%", String.valueOf(page + 2), "%max_page%", maxPageStr)));
        }

        // Close
        inv.setItem(SLOT_CLOSE, PlayerGUI.createItem(Material.BARRIER,
                lang.get("gui.admin.close.name"),
                lang.getLore("gui.admin.close.lore")));

        admin.openInventory(inv);
    }

    public void openDetail(Player admin, UUID targetUUID) {
        LanguageManager lang = plugin.getLanguageManager();
        PlaytimeManager pm = plugin.getPlaytimeManager();

        adminDetailTarget.put(admin.getUniqueId(), targetUUID);

        String targetName = pm.getPlayerName(targetUUID);
        String formattedTime = pm.formatTime(pm.getPlaytime(targetUUID));
        int rank = pm.getRank(targetUUID);
        String rankStr = rank < 0 ? "?" : String.valueOf(rank);
        boolean isOnline = Bukkit.getPlayer(targetUUID) != null;
        String status = isOnline ? lang.get("gui.admin.status-online") : lang.get("gui.admin.status-offline");

        String title = lang.get("gui.admin.title");
        Inventory inv = Bukkit.createInventory(null, 54, title);

        // Full glass border
        Material border = getBorderMaterial();
        ItemStack borderItem = PlayerGUI.createItem(border, " ", new ArrayList<>());
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, borderItem);
        }

        // Player head in center
        inv.setItem(22, PlayerGUI.createSkull(targetUUID,
                lang.get("gui.admin.detail-head.name", "%player%", targetName),
                lang.getLore("gui.admin.detail-head.lore",
                        "%player%", targetName,
                        "%time%", formattedTime,
                        "%rank%", rankStr,
                        "%status%", status)));

        // Back
        inv.setItem(45, PlayerGUI.createItem(Material.ARROW,
                lang.get("gui.admin.back.name"),
                lang.getLore("gui.admin.back.lore")));

        // Close
        inv.setItem(49, PlayerGUI.createItem(Material.BARRIER,
                lang.get("gui.admin.close.name"),
                lang.getLore("gui.admin.close.lore")));

        admin.openInventory(inv);
    }

    public int getAdminPage(UUID uuid) {
        return adminPages.getOrDefault(uuid, 0);
    }

    public void clearState(UUID uuid) {
        adminPages.remove(uuid);
        adminDetailTarget.remove(uuid);
    }

    private Material getBorderMaterial() {
        String colorName = plugin.getConfig().getString("gui.border-color", "GRAY");
        try {
            return Material.valueOf(colorName.toUpperCase() + "_STAINED_GLASS_PANE");
        } catch (IllegalArgumentException e) {
            return Material.GRAY_STAINED_GLASS_PANE;
        }
    }
}
