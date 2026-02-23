package ghostyplaytime;

import ghostyplaytime.commands.PlaytimeAdminCommand;
import ghostyplaytime.commands.PlaytimeCommand;
import ghostyplaytime.listeners.GUIListener;
import ghostyplaytime.listeners.PlayerListener;
import org.bukkit.plugin.java.JavaPlugin;

public class GhostyPlaytime extends JavaPlugin {

    private static GhostyPlaytime instance;
    private PlaytimeManager playtimeManager;
    private LanguageManager languageManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.languageManager = new LanguageManager(this);
        this.playtimeManager = new PlaytimeManager(this);

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);

        PlaytimeCommand ptCmd = new PlaytimeCommand(this);
        getCommand("playtime").setExecutor(ptCmd);
        getCommand("playtime").setTabCompleter(ptCmd);

        PlaytimeAdminCommand ptaCmd = new PlaytimeAdminCommand(this);
        getCommand("playtimeadmin").setExecutor(ptaCmd);
        getCommand("playtimeadmin").setTabCompleter(ptaCmd);

        getLogger().info(languageManager.getRaw("plugin-enabled"));
    }

    @Override
    public void onDisable() {
        if (playtimeManager != null) {
            playtimeManager.saveAll();
        }
        getLogger().info("GhostyPlaytime disabled!");
    }

    public void reload() {
        reloadConfig();
        languageManager.reload();
    }

    public static GhostyPlaytime getInstance() {
        return instance;
    }

    public PlaytimeManager getPlaytimeManager() {
        return playtimeManager;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }
}
