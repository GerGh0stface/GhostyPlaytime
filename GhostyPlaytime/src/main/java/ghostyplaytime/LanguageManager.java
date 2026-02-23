package ghostyplaytime;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class LanguageManager {

    private final GhostyPlaytime plugin;
    private FileConfiguration langConfig;
    private String language;

    public LanguageManager(GhostyPlaytime plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        this.language = plugin.getConfig().getString("language", "de");
        loadLangFile(language);
    }

    private void loadLangFile(String lang) {
        File langFolder = new File(plugin.getDataFolder(), "lang");
        if (!langFolder.exists()) langFolder.mkdirs();

        File langFile = new File(langFolder, lang + ".yml");

        // Extract default lang file from jar if not exists
        if (!langFile.exists()) {
            String resourcePath = "lang/" + lang + ".yml";
            InputStream in = plugin.getResource(resourcePath);
            if (in == null) {
                plugin.getLogger().warning("Language file '" + lang + ".yml' not found! Falling back to 'de'.");
                // Fallback
                langFile = new File(langFolder, "de.yml");
                in = plugin.getResource("lang/de.yml");
                if (in == null) {
                    plugin.getLogger().severe("Default language file not found in plugin jar!");
                    langConfig = new YamlConfiguration();
                    return;
                }
            }
            try {
                saveResource("lang/" + lang + ".yml", in, langFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Could not save language file: " + e.getMessage());
            }
        }

        // Also save bundled lang files if missing
        saveDefaultLangIfMissing(langFolder, "de");
        saveDefaultLangIfMissing(langFolder, "en");

        langConfig = YamlConfiguration.loadConfiguration(langFile);

        // Merge missing keys from jar
        InputStream defaultIn = plugin.getResource("lang/" + lang + ".yml");
        if (defaultIn == null) defaultIn = plugin.getResource("lang/de.yml");
        if (defaultIn != null) {
            try (InputStreamReader reader = new InputStreamReader(defaultIn, StandardCharsets.UTF_8)) {
                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(reader);
                boolean updated = false;
                for (String key : defaultConfig.getKeys(true)) {
                    if (!langConfig.contains(key)) {
                        langConfig.set(key, defaultConfig.get(key));
                        updated = true;
                    }
                }
                if (updated) {
                    langConfig.save(langFile);
                }
            } catch (IOException e) {
                plugin.getLogger().warning("Could not merge default lang keys: " + e.getMessage());
            }
        }
    }

    private void saveDefaultLangIfMissing(File folder, String lang) {
        File f = new File(folder, lang + ".yml");
        if (!f.exists()) {
            InputStream in = plugin.getResource("lang/" + lang + ".yml");
            if (in != null) {
                try {
                    saveResource("lang/" + lang + ".yml", in, f);
                } catch (IOException ignored) {}
            }
        }
    }

    private void saveResource(String resourcePath, InputStream in, File outFile) throws IOException {
        if (!outFile.getParentFile().exists()) outFile.getParentFile().mkdirs();
        try (OutputStream out = new FileOutputStream(outFile)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
        }
    }

    /**
     * Returns colored message with prefix.
     */
    public String getMessage(String key) {
        String prefix = color(langConfig.getString("prefix", "&8[&bGhostyPlaytime&8] &r"));
        String msg = langConfig.getString(key, "&cMissing lang key: " + key);
        return prefix + color(msg);
    }

    /**
     * Returns colored message without prefix.
     */
    public String get(String key) {
        return color(langConfig.getString(key, "&cMissing lang key: " + key));
    }

    /**
     * Returns raw (uncolored) string.
     */
    public String getRaw(String key) {
        return langConfig.getString(key, key);
    }

    /**
     * Returns colored message with placeholders replaced.
     */
    public String get(String key, String... replacements) {
        String msg = get(key);
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            msg = msg.replace(replacements[i], replacements[i + 1]);
        }
        return msg;
    }

    /**
     * Returns message with prefix and replacements.
     */
    public String getMessage(String key, String... replacements) {
        String msg = getMessage(key);
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            msg = msg.replace(replacements[i], replacements[i + 1]);
        }
        return msg;
    }

    /**
     * Returns a lore list from the lang config.
     */
    public List<String> getLore(String key) {
        List<String> lore = langConfig.getStringList(key);
        return lore.stream().map(this::color).collect(Collectors.toList());
    }

    /**
     * Returns a lore list with replacements applied.
     */
    public List<String> getLore(String key, String... replacements) {
        List<String> lore = getLore(key);
        return lore.stream().map(line -> {
            String result = line;
            for (int i = 0; i + 1 < replacements.length; i += 2) {
                result = result.replace(replacements[i], replacements[i + 1]);
            }
            return result;
        }).collect(Collectors.toList());
    }

    private String color(String text) {
        if (text == null) return "";
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public FileConfiguration getLangConfig() {
        return langConfig;
    }
}
