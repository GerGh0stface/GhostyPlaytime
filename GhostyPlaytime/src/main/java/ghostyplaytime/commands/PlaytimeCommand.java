package ghostyplaytime.commands;

import ghostyplaytime.GhostyPlaytime;
import ghostyplaytime.gui.PlayerGUI;
import ghostyplaytime.listeners.GUIListener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class PlaytimeCommand implements CommandExecutor, TabCompleter {

    private final GhostyPlaytime plugin;

    public PlaytimeCommand(GhostyPlaytime plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("player-only"));
            return true;
        }

        if (!player.hasPermission("ghostyplaytime.playtime")) {
            player.sendMessage(plugin.getLanguageManager().getMessage("no-permission"));
            return true;
        }

        for (org.bukkit.event.HandlerList list : org.bukkit.event.HandlerList.getHandlerLists()) {
            for (org.bukkit.plugin.RegisteredListener rl : list.getRegisteredListeners()) {
                if (rl.getListener() instanceof GUIListener guiListener) {
                    guiListener.getPlayerGUI().open(player);
                    return true;
                }
            }
        }

        new PlayerGUI(plugin).open(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}
