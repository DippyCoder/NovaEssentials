package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class UnmuteCommand extends BaseCommand {

    public UnmuteCommand(NovaEssentials plugin) {
        super(plugin, "unmute");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.unmute")) return;
        if (args.length == 0) {
            msg.send(sender, "general.invalid-args", "usage", "/" + label + " <player>");
            return;
        }

        Player target = findPlayer(sender, args[0]);
        if (target == null) return;

        if (!plugin.getMuteManager().isMuted(target.getUniqueId())) {
            msg.send(sender, "chat.not-muted", "player", target.getName());
            return;
        }

        plugin.getMuteManager().unmute(target.getUniqueId());
        msg.send(sender, "chat.unmuted-player", "player", target.getName());
        msg.send(target, "chat.notify-unmuted", "sender", sender.getName());
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) return filterPrefix(onlinePlayerNames(sender), args[0]);
        return List.of();
    }
}
