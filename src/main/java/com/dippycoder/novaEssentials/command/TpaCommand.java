package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class TpaCommand extends BaseCommand {

    public TpaCommand(NovaEssentials plugin) {
        super(plugin, "tpa");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.tpa")) return;
        if (!requirePlayer(sender)) return;
        if (args.length == 0) {
            msg.send(sender, "general.invalid-args", "usage", "/" + label + " <player>");
            return;
        }

        Player player = (Player) sender;
        Player target = findPlayer(sender, args[0]);
        if (target == null) return;

        if (target.equals(player)) {
            msg.send(sender, "general.invalid-args", "usage", "/" + label + " <player>");
            return;
        }

        // Check if sender is blocked
        if (plugin.getBlockManager().isBlockedBy(player, target)) {
            msg.send(sender, "tpa.blocked", "player", target.getName());
            return;
        }

        // Check for existing pending request
        if (plugin.getTpaManager().hasSentRequest(player, target)) {
            msg.send(sender, "tpa.already-pending", "player", target.getName());
            return;
        }

        plugin.getTpaManager().sendRequest(player, target);
        msg.send(sender, "tpa.sent", "player", target.getName());
        msg.send(target, "tpa.received", "player", player.getName());
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) return filterPrefix(onlinePlayerNames(sender), args[0]);
        return List.of();
    }
}
