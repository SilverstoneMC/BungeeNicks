package me.jasonhorkles.bungeenicks;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class ConfigurationManager {
    public static Configuration config;
    public static Configuration data;

    private final BungeeNicks plugin = BungeeNicks.getPlugin();

    public void initialize() {
        config = loadFile("config.yml");
        data = loadFile("data.yml");
    }

    public Configuration loadFile(String fileName) {
        if (!plugin.getDataFolder().exists())
            //noinspection ResultOfMethodCallIgnored
            plugin.getDataFolder().mkdir();

        File file = new File(plugin.getDataFolder(), fileName);

        if (!file.exists()) try (InputStream in = plugin.getResourceAsStream(fileName)) {
            Files.copy(in, file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            return ConfigurationProvider.getProvider(YamlConfiguration.class)
                .load(new File(plugin.getDataFolder(), fileName));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void saveData() {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class)
                .save(data, new File(plugin.getDataFolder(), "data.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
