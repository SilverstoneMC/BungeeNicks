package me.jasonhorkles.bungeenicks;

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

import java.util.ArrayList;
import java.util.List;

public class NickCommand extends Command implements TabExecutor {
    public NickCommand() {
        super("nickname", "bungeenicks.nickname", "nick");
    }

    private final BungeeAudiences audience = BungeeNicks.getAdventure();

    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer player)) {
            audience.sender(sender).sendMessage(
                Component.text("Sorry, but only players can do that.").color(NamedTextColor.RED));
            return;
        }

        if (args.length == 0) {
            if (sender.hasPermission("bungeenicks.nickname.others")) {
                audience.sender(sender).sendMessage(
                    Component.text("Usage: /nick <nickname | reset> [player]", NamedTextColor.RED));
                return;
            }

            audience.sender(sender)
                .sendMessage(Component.text("Usage: /nick <nickname | reset>", NamedTextColor.RED));
            return;
        }

        if (args[0].equalsIgnoreCase("reset")) {
            if (sender.hasPermission("bungeenicks.nickname.others") && args.length > 1) {
                ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[1]);
                if (target == null) {
                    audience.sender(sender)
                        .sendMessage(Component.text("That player isn't online!").color(NamedTextColor.RED));
                    return;
                }

                target.setDisplayName(target.getName());
                ConfigurationManager.data.set(target.getUniqueId().toString(), null);
                new ConfigurationManager().saveData();

                audience.sender(sender).sendMessage(Component.text("You have reset ", NamedTextColor.GREEN)
                    .append(Component.text(target.getName())).append(Component.text("'s nickname.")));
                return;
            }

            player.setDisplayName(player.getName());
            ConfigurationManager.data.set(player.getUniqueId().toString(), null);
            new ConfigurationManager().saveData();

            audience.sender(sender)
                .sendMessage(Component.text("Your nickname has been reset.", NamedTextColor.GREEN));
            return;
        }

        if (args[0].equalsIgnoreCase("colors") && sender.hasPermission("bungeenicks.nickname.colors")) {
            audience.sender(sender).sendMessage(Component.text("Click ", NamedTextColor.DARK_GREEN).append(
                    Component.text("here", NamedTextColor.DARK_AQUA, TextDecoration.UNDERLINED)
                        .clickEvent(ClickEvent.openUrl("https://docs.advntr.dev/minimessage/format.html#color")))
                .append(Component.text(" to see available formatting.", NamedTextColor.DARK_GREEN)));
            return;
        }

        TagResolver.Builder allowedTags = TagResolver.builder();
        if (sender.hasPermission("bungeenicks.nickname.colors"))
            allowedTags.resolvers(StandardTags.color(), StandardTags.rainbow(), StandardTags.gradient());
        if (sender.hasPermission("bungeenicks.nickname.decorations"))
            allowedTags.resolver(StandardTags.decorations());
        allowedTags.resolver(StandardTags.reset());

        MiniMessage miniMessage = MiniMessage.builder().tags(allowedTags.build()).build();

        String strippedName = miniMessage.stripTags(args[0], allowedTags.build());
        int maxLength = ConfigurationManager.config.getInt("max-length");
        if (strippedName.length() > maxLength) {
            audience.sender(sender).sendMessage(
                Component.text("Your nickname cannot be longer than " + maxLength + " characters!",
                    NamedTextColor.RED));
            return;
        }


        //todo other peoples nicknames
        String nickname = LegacyComponentSerializer.builder().useUnusualXRepeatedCharacterHexFormat()
            .hexColors().build().serialize(miniMessage.deserialize(args[0]));

        player.setDisplayName(nickname);
        ConfigurationManager.data.set(player.getUniqueId().toString(), nickname);
        new ConfigurationManager().saveData();

        audience.sender(sender).sendMessage(
            Component.text("Your nickname has been set to ", NamedTextColor.GREEN)
                .append(miniMessage.deserialize(args[0])).append(Component.text(".", NamedTextColor.GREEN)));
    }

    final List<String> arguments = new ArrayList<>();

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (sender.hasPermission("bungeenicks.nickname.colors")) {
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
