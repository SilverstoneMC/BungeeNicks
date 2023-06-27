package me.jasonhorkles.bungeenicks;

import me.jasonhorkles.bungeenicks.commands.Nickname;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class JoinEvent implements Listener {
    @EventHandler
    public void onServerConnect(ServerConnectEvent event) {
        ProxiedPlayer player = event.getPlayer();
        String nickname = ConfigurationManager.data.getString("nicknames." + player.getUniqueId().toString());
        if (!nickname.isBlank()) new Nickname().setDisplayName(player, nickname);
    }
}
