package ghostyplaytime;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PlaytimeManager {

    private final GhostyPlaytime plugin;
    private final Map<UUID, Long> playtimeMap = new ConcurrentHashMap<>();
    private File dataFile;
    private YamlConfiguration dataConfig;

    public PlaytimeManager(GhostyPlaytime plugin) {
        this.plugin = plugin;
        loadData();
        startTracking();
        startAutoSave();
    }

    private void loadData() {
        dataFile = new File(plugin.getDataFolder(), "playtime.yml");
        if (!dataFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create playtime.yml! " + e.getMessage());
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        for (String key : dataConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                playtimeMap.put(uuid, dataConfig.getLong(key));
            } catch (IllegalArgumentException ignored) {
                // Skip invalid keys
            }
        }
        plugin.getLogger().info("Loaded playtime data for " + playtimeMap.size() + " players.");
    }

    private void startTracking() {
        // Increment playtime for every online player every second
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                playtimeMap.merge(player.getUniqueId(), 1L, Long::sum);
            }
        }, 20L, 20L);
    }

    private void startAutoSave() {
        int intervalSeconds = plugin.getConfig().getInt("auto-save-interval", 300);
        long intervalTicks = intervalSeconds * 20L;
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::saveAll, intervalTicks, intervalTicks);
    }

    public void saveAll() {
        for (Map.Entry<UUID, Long> entry : playtimeMap.entrySet()) {
            dataConfig.set(entry.getKey().toString(), entry.getValue());
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save playtime.yml! " + e.getMessage());
        }
    }

    public long getPlaytime(UUID uuid) {
        return playtimeMap.getOrDefault(uuid, 0L);
    }

    public void setPlaytime(UUID uuid, long seconds) {
        playtimeMap.put(uuid, Math.max(0, seconds));
    }

    public void addPlaytime(UUID uuid, long seconds) {
        playtimeMap.merge(uuid, seconds, (a, b) -> Math.max(0, a + b));
    }

    /**
     * Returns top N players sorted by playtime descending.
     */
    public List<Map.Entry<UUID, Long>> getTopPlayers(int limit) {
        return playtimeMap.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Returns all players sorted by playtime descending.
     */
    public List<Map.Entry<UUID, Long>> getAllSorted() {
        return playtimeMap.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Returns the rank of a player (1-based). Returns -1 if not found.
     */
    public int getRank(UUID uuid) {
        List<Map.Entry<UUID, Long>> sorted = getAllSorted();
        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i).getKey().equals(uuid)) {
                return i + 1;
            }
        }
        return -1;
    }

    /**
     * Formats seconds into a human-readable string using language file.
     */
    public String formatTime(long totalSeconds) {
        LanguageManager lang = plugin.getLanguageManager();
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        String daySuffix = lang.getRaw("time-format.day-suffix");
        String hourSuffix = lang.getRaw("time-format.hour-suffix");
        String minuteSuffix = lang.getRaw("time-format.minute-suffix");
        String secondSuffix = lang.getRaw("time-format.second-suffix");

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append(daySuffix).append(" ");
        if (hours > 0 || days > 0) sb.append(hours).append(hourSuffix).append(" ");
        if (minutes > 0 || hours > 0 || days > 0) sb.append(minutes).append(minuteSuffix).append(" ");
        sb.append(seconds).append(secondSuffix);

        return sb.toString().trim();
    }

    /**
     * Gets the display name of a player (online or offline).
     */
    public String getPlayerName(UUID uuid) {
        Player online = Bukkit.getPlayer(uuid);
        if (online != null) return online.getName();
        OfflinePlayer offline = Bukkit.getOfflinePlayer(uuid);
        String name = offline.getName();
        return name != null ? name : "Unknown";
    }
}
