package ghostyplaytime.listeners;

import ghostyplaytime.GhostyPlaytime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final GhostyPlaytime plugin;

    public PlayerListener(GhostyPlaytime plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Ensure player is initialized in the map (returns 0 for new players)
        plugin.getPlaytimeManager().getPlaytime(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Async save on disconnect to prevent data loss
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin,
                () -> plugin.getPlaytimeManager().saveAll());
    }
}
