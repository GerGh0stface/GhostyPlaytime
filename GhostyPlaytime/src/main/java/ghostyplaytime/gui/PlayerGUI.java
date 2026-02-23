package ghostyplaytime.gui;

import ghostyplaytime.GhostyPlaytime;
import ghostyplaytime.LanguageManager;
import ghostyplaytime.PlaytimeManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Player GUI Layout (54 slots):
 *
 * Row 0: [G][G][G][G][G][G][G][G][G]
 * Row 1: [G][G][G][G][HEAD][G][G][G][G]   <- Own head at slot 13
 * Row 2: [G][G][G][G][G][G][G][G][G]
 * Row 3: [G][G][G][G][LABEL][G][G][G][G]  <- Top label at slot 31
 * Row 4: [G][T1][G][T2][G][T3][G][T4][G] <- Top players
 * Row 5: [G][G][T5][G][G][G][CLOSE][G][G]
 */
public class PlayerGUI {

    private static final int SLOT_OWN_HEAD = 13;
    private static final int SLOT_TOP_LABEL = 31;
    private static final int[] SLOT_TOP_PLAYERS = {37, 39, 41, 43, 46};
    private static final int SLOT_CLOSE = 49;

    private final GhostyPlaytime plugin;

    public PlayerGUI(GhostyPlaytime plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        LanguageManager lang = plugin.getLanguageManager();
        PlaytimeManager pm = plugin.getPlaytimeManager();

        String title = lang.get("gui.player.title");
        Inventory inv = Bukkit.createInventory(null, 54, title);

        // Fill with border glass panes
        Material borderMaterial = getBorderMaterial();
        ItemStack border = createItem(borderMaterial, " ", new ArrayList<>());
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, border);
        }

        // Own head
        long ownTime = pm.getPlaytime(player.getUniqueId());
        int ownRank = pm.getRank(player.getUniqueId());
        String formattedTime = pm.formatTime(ownTime);
        String rankStr = ownRank < 0 ? "?" : String.valueOf(ownRank);

        ItemStack ownHead = createSkull(player.getUniqueId(),
                lang.get("gui.player.own-head.name",
                        "%player%", player.getName()),
                lang.getLore("gui.player.own-head.lore",
                        "%player%", player.getName(),
                        "%time%", formattedTime,
                        "%rank%", rankStr));
        inv.setItem(SLOT_OWN_HEAD, ownHead);

        // Top label
        int topAmount = plugin.getConfig().getInt("top.amount", 5);
        ItemStack topLabel = createItem(Material.NETHER_STAR,
                lang.get("gui.player.top-label.name"),
                lang.getLore("gui.player.top-label.lore"));
        inv.setItem(SLOT_TOP_LABEL, topLabel);

        // Top players
        List<Map.Entry<UUID, Long>> topPlayers = pm.getTopPlayers(topAmount);
        for (int i = 0; i < Math.min(topPlayers.size(), SLOT_TOP_PLAYERS.length); i++) {
            Map.Entry<UUID, Long> entry = topPlayers.get(i);
            String topName = pm.getPlayerName(entry.getKey());
            String topTime = pm.formatTime(entry.getValue());
            int rank = i + 1;

            ItemStack topHead = createSkull(entry.getKey(),
                    lang.get("gui.player.top-head.name",
                            "%rank%", String.valueOf(rank),
                            "%player%", topName),
                    lang.getLore("gui.player.top-head.lore",
                            "%rank%", String.valueOf(rank),
                            "%player%", topName,
                            "%time%", topTime));
            inv.setItem(SLOT_TOP_PLAYERS[i], topHead);
        }

        // Close button
        ItemStack closeBtn = createItem(Material.BARRIER,
                lang.get("gui.player.close.name"),
                lang.getLore("gui.player.close.lore"));
        inv.setItem(SLOT_CLOSE, closeBtn);

        player.openInventory(inv);
    }

    private Material getBorderMaterial() {
        String colorName = plugin.getConfig().getString("gui.border-color", "GRAY");
        try {
            return Material.valueOf(colorName.toUpperCase() + "_STAINED_GLASS_PANE");
        } catch (IllegalArgumentException e) {
            return Material.GRAY_STAINED_GLASS_PANE;
        }
    }

    public static ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static ItemStack createSkull(UUID uuid, String name, List<String> lore) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (meta != null) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            meta.setOwningPlayer(offlinePlayer);
            meta.setDisplayName(name);
            meta.setLore(lore);
            skull.setItemMeta(meta);
        }
        return skull;
    }
}
