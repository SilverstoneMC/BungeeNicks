package me.jasonhorkles.bungeenicks.commands;

import me.jasonhorkles.bungeenicks.BungeeNicks;
import me.jasonhorkles.bungeenicks.ConfigurationManager;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Nickname extends Command implements TabExecutor {
    public Nickname() {
        super("nickname", "bungeenicks.nickname", "nick");
    }

    private final BungeeAudiences audience = BungeeNicks.getAdventure();

    public void execute(CommandSender sender, String[] args) {
        ProxiedPlayer player = null;
        if (sender instanceof ProxiedPlayer) player = (ProxiedPlayer) sender;

        if (args.length == 0) {
            if (sender.hasPermission("bungeenicks.nickname.others"))
                if (sender.hasPermission("bungeenicks.nickname.colors")) audience.sender(sender).sendMessage(
                    Component.text("Usage: /nick <nickname | reset | colors> [player]", NamedTextColor.RED));
                else audience.sender(sender).sendMessage(
                    Component.text("Usage: /nick <nickname | reset> [player]", NamedTextColor.RED));
            else if (sender.hasPermission("bungeenicks.nickname.colors")) audience.sender(sender)
                .sendMessage(Component.text("Usage: /nick <nickname | reset | colors>", NamedTextColor.RED));
            else audience.sender(sender)
                    .sendMessage(Component.text("Usage: /nick <nickname | reset>", NamedTextColor.RED));
            return;
        }

        boolean differentTarget = false;
        if (sender.hasPermission("bungeenicks.nickname.others") && args.length > 1) {
            ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[1]);
            if (target == null) {
                audience.sender(sender)
                    .sendMessage(Component.text("That player isn't online!").color(NamedTextColor.RED));
                return;
            }
            player = target;
            differentTarget = true;
        }

        // Reset nickname
        if (args[0].equalsIgnoreCase("reset")) {
            if (setNickname(player, sender, null, null)) return;

            if (differentTarget) {
                audience.sender(sender).sendMessage(Component.text("You have reset ", NamedTextColor.GREEN)
                    .append(Component.text(player.getName())).append(Component.text("'s nickname.")));
                audience.player(player)
                    .sendMessage(Component.text("Your nickname was reset.", NamedTextColor.GREEN));
            } else audience.sender(sender)
                .sendMessage(Component.text("Your nickname has been reset.", NamedTextColor.GREEN));
            return;
        }

        // Colors help
        if (args[0].equalsIgnoreCase("colors") && (sender.hasPermission(
            "bungeenicks.nickname.colors") || sender.hasPermission("bungeenicks.nickname.formats"))) {
            audience.sender(sender).sendMessage(Component.text("Click ", NamedTextColor.DARK_GREEN).append(
                    Component.text("here", NamedTextColor.DARK_AQUA, TextDecoration.UNDERLINED)
                        .clickEvent(ClickEvent.openUrl("https://docs.advntr.dev/minimessage/format.html#color")))
                .append(Component.text(" to see available formatting.", NamedTextColor.DARK_GREEN)));
            return;
        }

        // Whether or not to allow colors/decorations
        TagResolver.Builder allowedTags = TagResolver.builder();
        if (sender.hasPermission("bungeenicks.nickname.colors"))
            allowedTags.resolvers(StandardTags.color(), StandardTags.rainbow(), StandardTags.gradient());
        if (sender.hasPermission("bungeenicks.nickname.decorations"))
            allowedTags.resolver(StandardTags.decorations());
        allowedTags.resolver(StandardTags.reset());

        MiniMessage miniMessage = MiniMessage.builder().tags(allowedTags.build()).build();
        String strippedName = miniMessage.stripTags(args[0]);

        // Check if nickname is alphanumeric
        if (!sender.hasPermission("bungeenicks.nickname.bypass.alphanumeric"))
            if (!strippedName.matches("^[a-zA-Z0-9]+$")) {
                audience.sender(sender)
                    .sendMessage(Component.text("Nicknames must be alphanumeric!", NamedTextColor.RED));
                return;
            }

        // Check if nickname is too long
        if (!sender.hasPermission("bungeenicks.nickname.bypass.length")) {
            int maxLength = ConfigurationManager.config.getInt("max-length");
            if (strippedName.length() > maxLength) {
                audience.sender(sender).sendMessage(
                    Component.text("Nicknames cannot be longer than " + maxLength + " characters!",
                        NamedTextColor.RED));
                return;
            }
        }

        String nickname = LegacyComponentSerializer.builder().useUnusualXRepeatedCharacterHexFormat()
            .hexColors().build().serialize(miniMessage.deserialize(args[0]));

        if (setNickname(player, sender, nickname, MiniMessage.miniMessage().stripTags(args[0]))) return;

        if (differentTarget) {
            audience.sender(sender).sendMessage(
                Component.text("You have set ", NamedTextColor.GREEN).append(Component.text(player.getName()))
                    .append(Component.text("'s nickname to ")).append(miniMessage.deserialize(args[0])));
            audience.player(player).sendMessage(
                Component.text("Your nickname was changed to ", NamedTextColor.GREEN)
                    .append(miniMessage.deserialize(args[0])));
        } else audience.sender(sender).sendMessage(
            Component.text("Your nickname has been set to ", NamedTextColor.GREEN)
                .append(miniMessage.deserialize(args[0])));
    }

    private boolean setNickname(@Nullable ProxiedPlayer player, CommandSender sender, @Nullable String nickname, @Nullable String strippedName) {
        if (player == null) {
            audience.sender(sender)
                .sendMessage(Component.text("You must be a player to do that!", NamedTextColor.RED));
            return true;
        }

        if (nickname == null) player.setDisplayName(player.getName());
        else setDisplayName(player, nickname);
        ConfigurationManager.data.set("nicknames." + player.getUniqueId().toString(), nickname);
        ConfigurationManager.data.set("stripped-nicknames." + player.getUniqueId().toString(),
            player.getName() + ":" + strippedName);
        new ConfigurationManager().saveData();
        return false;
    }

    public void setDisplayName(ProxiedPlayer player, String nickname) {
        // Add the extra space at the end to get minimessage to set the color even if there is no text in the prefix
        String prefix = LegacyComponentSerializer.builder().useUnusualXRepeatedCharacterHexFormat()
            .hexColors().build().serialize(
                MiniMessage.miniMessage().deserialize(ConfigurationManager.config.getString("prefix") + " "))
            .replaceAll(" $", "");
        
        String suffix = LegacyComponentSerializer.builder().useUnusualXRepeatedCharacterHexFormat()
            .hexColors().build().serialize(
                MiniMessage.miniMessage().deserialize(ConfigurationManager.config.getString("suffix")));

        player.setDisplayName(prefix + nickname + "§r" + suffix);
    }


    final List<String> arguments = new ArrayList<>();

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (sender.hasPermission("bungeenicks.nickname.colors") || sender.hasPermission(
            "bungeenicks.nickname.decorations")) {
            if (!arguments.contains("colors")) arguments.add("colors");
        } else arguments.remove("colors");

        if (!arguments.contains("reset")) arguments.add("reset");

        List<String> arguments2 = new ArrayList<>();
        if (sender.hasPermission("bungeenicks.nickname.others"))
            for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers())
                arguments2.add(player.getName());

        List<String> result = new ArrayList<>();
        switch (args.length) {
            case 1 -> {
                for (String a : arguments)
                    if (a.toLowerCase().startsWith(args[0].toLowerCase())) result.add(a);
            }

            case 2 -> {
                for (String a : arguments2)
                    if (a.toLowerCase().startsWith(args[1].toLowerCase())) result.add(a);
            }
        }
        return result;
    }
}
