package ghostyplaytime.listeners;

import ghostyplaytime.GhostyPlaytime;
import ghostyplaytime.LanguageManager;
import ghostyplaytime.gui.AdminGUI;
import ghostyplaytime.gui.PlayerGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class GUIListener implements Listener {

    private final GhostyPlaytime plugin;
    private final PlayerGUI playerGUI;
    private final AdminGUI adminGUI;

    public GUIListener(GhostyPlaytime plugin) {
        this.plugin = plugin;
        this.playerGUI = new PlayerGUI(plugin);
        this.adminGUI = new AdminGUI(plugin);
    }

    public PlayerGUI getPlayerGUI() { return playerGUI; }
    public AdminGUI getAdminGUI()   { return adminGUI; }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Inventory inv = event.getInventory();
        String title = ChatColor.stripColor(event.getView().getTitle());

        LanguageManager lang = plugin.getLanguageManager();
        String playerGUITitle   = ChatColor.stripColor(lang.get("gui.player.title"));
        String adminListTitle   = ChatColor.stripColor(lang.get("gui.admin.all-title"));
        String adminDetailTitle = ChatColor.stripColor(lang.get("gui.admin.title"));

        boolean isPlayerGUI   = title.equals(playerGUITitle);
        boolean isAdminList   = title.equals(adminListTitle);
        boolean isAdminDetail = title.equals(adminDetailTitle);

        if (!isPlayerGUI && !isAdminList && !isAdminDetail) return;

        event.setCancelled(true);

        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(inv)) return;
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        int slot = event.getSlot();

        // ── Player GUI ──────────────────────────────────────────
        if (isPlayerGUI) {
            if (clicked.getType() == Material.BARRIER) {
                player.closeInventory();
            }
        }

        // ── Admin List GUI ──────────────────────────────────────
        else if (isAdminList) {
            int currentPage = adminGUI.getAdminPage(player.getUniqueId());
            List<Map.Entry<UUID, Long>> allPlayers = plugin.getPlaytimeManager().getAllSorted();
            int totalPages = Math.max(1, (int) Math.ceil(allPlayers.size() / (double) 45));

            if (clicked.getType() == Material.BARRIER) {
                player.closeInventory();
                adminGUI.clearState(player.getUniqueId());
            } else if (slot == 45 && clicked.getType() == Material.ARROW && currentPage > 0) {
                adminGUI.openList(player, currentPage - 1);
            } else if (slot == 53 && clicked.getType() == Material.ARROW && currentPage < totalPages - 1) {
                adminGUI.openList(player, currentPage + 1);
            } else if (slot < 45 && clicked.getType() == Material.PLAYER_HEAD) {
                SkullMeta meta = (SkullMeta) clicked.getItemMeta();
                if (meta != null && meta.getOwningPlayer() != null) {
                    adminGUI.openDetail(player, meta.getOwningPlayer().getUniqueId());
                }
            }
        }

        // ── Admin Detail GUI ────────────────────────────────────
        else if (isAdminDetail) {
            if (slot == 45 && clicked.getType() == Material.ARROW) {
                adminGUI.openList(player, adminGUI.getAdminPage(player.getUniqueId()));
            } else if (clicked.getType() == Material.BARRIER) {
                player.closeInventory();
                adminGUI.clearState(player.getUniqueId());
            }
        }
    }
}
