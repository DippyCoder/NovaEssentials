package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class AfkCommand extends BaseCommand {

    public AfkCommand(NovaEssentials plugin) {
        super(plugin, "afk");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.afk")) return;
        if (!requirePlayer(sender)) return;

        Player player = (Player) sender;
        String afkMessage = args.length > 0 ? String.join(" ", args) : null;

        boolean nowAfk = plugin.getAfkManager().toggle(player, afkMessage);

        if (nowAfk) {
            msg.send(sender, "afk.enabled");
            Component broadcast = afkMessage != null
                    ? msg.get(sender, "afk.broadcast-on-msg",
                        "player", player.getName(), "message", afkMessage)
                    : msg.get(sender, "afk.broadcast-on",
                        "player", player.getName());
            plugin.getServer().broadcast(broadcast);
        } else {
            msg.send(sender, "afk.disabled");
            plugin.getServer().broadcast(msg.get(sender, "afk.broadcast-off",
                    "player", player.getName()));
        }
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
