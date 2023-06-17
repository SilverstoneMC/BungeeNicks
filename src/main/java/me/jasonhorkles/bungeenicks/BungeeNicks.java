package me.jasonhorkles.bungeenicks;

import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeNicks extends Plugin implements Listener {
    private static BungeeNicks plugin;
    private static BungeeAudiences adventure;

    @Override
    public void onEnable() {
        plugin = this;
        new ConfigurationManager().initialize();
        adventure = BungeeAudiences.create(this);

        getProxy().getPluginManager().registerCommand(this, new ReloadCommand());
        getProxy().getPluginManager().registerCommand(this, new NickCommand());

        getProxy().getPluginManager().registerListener(this, new JoinEvent());
    }

    @Override
    public void onDisable() {
        if (adventure != null) {
            adventure.close();
            adventure = null;
        }
    }

    public static BungeeAudiences getAdventure() {
        if (adventure == null)
            throw new IllegalStateException("Cannot retrieve audience provider while plugin is not enabled");
        return adventure;
    }

    public static BungeeNicks getPlugin() {
        return plugin;
    }
}