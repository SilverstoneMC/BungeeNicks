package me.jasonhorkles.bungeenicks.commands;

import me.jasonhorkles.bungeenicks.BungeeNicks;
import me.jasonhorkles.bungeenicks.ConfigurationManager;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class Reload extends Command {
    public Reload() {
        super("bungeenicksreload", "bungeenicks.reload", "bnrl");
    }

    private final BungeeAudiences audience = BungeeNicks.getAdventure();

    public void execute(CommandSender sender, String[] args) {
        ConfigurationManager.config = new ConfigurationManager().loadFile("config.yml");
        ConfigurationManager.data = new ConfigurationManager().loadFile("data.yml");

        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            String nickname = ConfigurationManager.data.getString(
                "nicknames." + player.getUniqueId().toString());
            if (!nickname.isBlank()) player.setDisplayName(nickname);
        }

        audience.sender(sender)
            .sendMessage(Component.text("BungeeNicks reloaded!").color(NamedTextColor.GREEN));
    }
}
