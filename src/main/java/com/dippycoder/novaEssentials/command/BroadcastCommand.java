package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class BroadcastCommand extends BaseCommand {

    private final MiniMessage mm = MiniMessage.miniMessage();

    public BroadcastCommand(NovaEssentials plugin) {
        super(plugin, "broadcast");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.broadcast")) return;
        if (args.length == 0) {
            msg.send(sender, "general.invalid-args", "usage", "/" + label + " <message>");
            return;
        }

        String rawMessage = String.join(" ", args);
        Component messageComponent;

        if (sender.hasPermission(plugin.getConfigManager().getPermission("cmd.broadcast.minimessage"))) {
            messageComponent = mm.deserialize(rawMessage);
        } else {
            messageComponent = Component.text(rawMessage);
        }

        // Broadcast: divider → formatted message → divider
        for (var online : plugin.getServer().getOnlinePlayers()) {
            online.sendMessage(msg.get(online, "broadcast.divider"));
            online.sendMessage(msg.get(online, "broadcast.format", "message", messageComponent));
            online.sendMessage(msg.get(online, "broadcast.divider"));
        }
        plugin.getServer().getConsoleSender().sendMessage(
                msg.get(plugin.getServer().getConsoleSender(), "broadcast.divider"));
        plugin.getServer().getConsoleSender().sendMessage(
                msg.get(plugin.getServer().getConsoleSender(), "broadcast.format",
                        "message", messageComponent));
        plugin.getServer().getConsoleSender().sendMessage(
                msg.get(plugin.getServer().getConsoleSender(), "broadcast.divider"));
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
