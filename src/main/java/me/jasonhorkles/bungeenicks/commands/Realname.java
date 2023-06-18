package me.jasonhorkles.bungeenicks.commands;

import me.jasonhorkles.bungeenicks.BungeeNicks;
import me.jasonhorkles.bungeenicks.ConfigurationManager;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class Realname extends Command {
    public Realname() {
        super("realname", "bungeenicks.realname");
    }

    private final BungeeAudiences audience = BungeeNicks.getAdventure();

    public void execute(CommandSender sender, String[] args) {
        boolean playerFound = false;
        for (String uuid : ConfigurationManager.data.getSection("stripped-nicknames").getKeys()) {
            String strippedNickname = ConfigurationManager.data.getString("stripped-nicknames." + uuid);
            if (strippedNickname.toLowerCase().replaceFirst(".*:", "").startsWith(args[0].toLowerCase())) {
                String nickname = ConfigurationManager.data.getString("nicknames." + uuid);
                audience.sender(sender).sendMessage(
                    LegacyComponentSerializer.legacySection().deserialize(nickname).append(
                        Component.text("'s real name is " + strippedNickname.replaceFirst(":.*", "") + ".",
                            NamedTextColor.GREEN)));
                playerFound = true;
                break;
            }
        }

        if (!playerFound) audience.sender(sender).sendMessage(
            Component.text("No player found with the nickname " + args[0] + ".", NamedTextColor.RED));
    }
}
