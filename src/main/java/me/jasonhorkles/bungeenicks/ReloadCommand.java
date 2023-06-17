package me.jasonhorkles.bungeenicks;

import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class ReloadCommand extends Command {
    public ReloadCommand() {
        super("bungeenicksreload", "bungeenicks.reload", "bnrl");
    }

    private final BungeeAudiences audience = BungeeNicks.getAdventure();

    public void execute(CommandSender sender, String[] args) {
        ConfigurationManager.config = new ConfigurationManager().loadFile("config.yml");
        ConfigurationManager.data = new ConfigurationManager().loadFile("data.yml");

        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            String nickname = ConfigurationManager.data.getString(player.getUniqueId().toString());
            if (!nickname.isBlank()) player.setDisplayName(nickname);
        }

        audience.sender(sender)
            .sendMessage(Component.text("BungeeNicks reloaded!").color(NamedTextColor.GREEN));
    }
}
