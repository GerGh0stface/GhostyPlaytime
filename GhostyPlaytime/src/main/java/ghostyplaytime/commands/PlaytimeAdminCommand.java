package ghostyplaytime.commands;

import ghostyplaytime.GhostyPlaytime;
import ghostyplaytime.LanguageManager;
import ghostyplaytime.gui.AdminGUI;
import ghostyplaytime.listeners.GUIListener;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;

public class PlaytimeAdminCommand implements CommandExecutor, TabCompleter {

    private final GhostyPlaytime plugin;

    public PlaytimeAdminCommand(GhostyPlaytime plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        LanguageManager lang = plugin.getLanguageManager();

        if (!sender.hasPermission("ghostyplaytime.admin")) {
            sender.sendMessage(lang.getMessage("no-permission"));
            return true;
        }

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(lang.getMessage("player-only"));
                return true;
            }
            getAdminGUI().openList(player, 0);
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("reload")) {
            plugin.reload();
            sender.sendMessage(lang.getMessage("plugin-reloaded"));
            return true;
        }

        if (sub.equals("set")) {
            if (args.length < 3) {
                sender.sendMessage(lang.getMessage("invalid-usage",
                        "%usage%", "/" + label + " set <Spieler> <Sekunden>"));
                return true;
            }
            UUID targetUUID = resolvePlayer(args[1]);
            if (targetUUID == null) {
                sender.sendMessage(lang.getMessage("player-not-found", "%player%", args[1]));
                return true;
            }
            long seconds;
            try {
                seconds = Long.parseLong(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage(lang.getMessage("invalid-number", "%input%", args[2]));
                return true;
            }
            plugin.getPlaytimeManager().setPlaytime(targetUUID, seconds);
            String playerName = plugin.getPlaytimeManager().getPlayerName(targetUUID);
            String formattedTime = plugin.getPlaytimeManager().formatTime(seconds);
            sender.sendMessage(lang.getMessage("admin.playtime-set",
                    "%player%", playerName,
                    "%time%", formattedTime));
            return true;
        }

        if (sub.equals("add")) {
            if (args.length < 3) {
                sender.sendMessage(lang.getMessage("invalid-usage",
                        "%usage%", "/" + label + " add <Spieler> <Sekunden>"));
                return true;
            }
            UUID targetUUID = resolvePlayer(args[1]);
            if (targetUUID == null) {
                sender.sendMessage(lang.getMessage("player-not-found", "%player%", args[1]));
                return true;
            }
            long seconds;
            try {
                seconds = Long.parseLong(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage(lang.getMessage("invalid-number", "%input%", args[2]));
                return true;
            }
            plugin.getPlaytimeManager().addPlaytime(targetUUID, seconds);
            String playerName = plugin.getPlaytimeManager().getPlayerName(targetUUID);
            String formattedTime = plugin.getPlaytimeManager().formatTime(seconds);
            sender.sendMessage(lang.getMessage("admin.playtime-added",
                    "%player%", playerName,
                    "%time%", formattedTime));
            return true;
        }

        if (!(sender instanceof Player admin)) {
            sender.sendMessage(lang.getMessage("player-only"));
            return true;
        }
        UUID targetUUID = resolvePlayer(args[0]);
        if (targetUUID == null) {
            sender.sendMessage(lang.getMessage("player-not-found", "%player%", args[0]));
            return true;
        }
        getAdminGUI().openDetail(admin, targetUUID);
        return true;
    }

    private UUID resolvePlayer(String name) {
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) return online.getUniqueId();

        @SuppressWarnings("deprecation")
        OfflinePlayer offline = Bukkit.getOfflinePlayer(name);
        if (offline.hasPlayedBefore() || offline.isOnline()) {
            return offline.getUniqueId();
        }
        return null;
    }

    private AdminGUI getAdminGUI() {
        for (org.bukkit.event.HandlerList list : org.bukkit.event.HandlerList.getHandlerLists()) {
            for (org.bukkit.plugin.RegisteredListener rl : list.getRegisteredListeners()) {
                if (rl.getListener() instanceof GUIListener guiListener) {
                    return guiListener.getAdminGUI();
                }
            }
        }
        return new AdminGUI(plugin);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("ghostyplaytime.admin")) return Collections.emptyList();

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> options = new ArrayList<>(Arrays.asList("reload", "set", "add"));
            Bukkit.getOnlinePlayers().forEach(p -> options.add(p.getName()));
            StringUtil.copyPartialMatches(args[0], options, completions);
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("add"))) {
            Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> StringUtil.startsWithIgnoreCase(name, args[1]))
                    .forEach(completions::add);
        } else if (args.length == 3 && (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("add"))) {
            completions.addAll(Arrays.asList("60", "3600", "86400"));
        }

        Collections.sort(completions);
        return completions;
    }
}
