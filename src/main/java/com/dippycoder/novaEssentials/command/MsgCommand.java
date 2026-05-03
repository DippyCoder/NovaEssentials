package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class MsgCommand extends BaseCommand {

    public MsgCommand(NovaEssentials plugin) {
        super(plugin, "msg");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.msg")) return;
        if (args.length < 2) {
            msg.send(sender, "general.invalid-args", "usage", "/" + label + " <player> <message>");
            return;
        }

        Player target = findPlayer(sender, args[0]);
        if (target == null) return;

        if (sender instanceof Player sPlayer) {
            if (plugin.getBlockManager().isBlockedBy(sPlayer, target)) {
                msg.send(sender, "msg.blocked-by", "player", target.getName());
                return;
            }
            if (plugin.getBlockManager().isBlocked(sPlayer, target)) {
                msg.send(sender, "msg.player-blocked", "player", target.getName());
                return;
            }
        }

        String message = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));

        // Send to sender
        msg.send(sender, "msg.sent", "player", target.getName(), "message", message);
        // Send to receiver
        msg.send(target, "msg.received",
                "player", sender instanceof Player p ? p.getName() : sender.getName(),
                "message", message);

        // Record for /r
        if (sender instanceof Player sPlayer) {
            plugin.getMsgManager().recordMessage(sPlayer.getUniqueId(), target.getUniqueId());
        }

        // Spy
        String senderName = sender instanceof Player p ? p.getName() : sender.getName();
        for (Player online : plugin.getServer().getOnlinePlayers()) {
            if (online.equals(sender) || online.equals(target)) continue;
            if (online.hasPermission(plugin.getConfigManager().getPermission("cmd.msg.spy"))) {
                msg.send(online, "msg.spy",
                        "sender", senderName,
                        "receiver", target.getName(),
                        "message", message);
            }
        }
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) return filterPrefix(onlinePlayerNames(sender), args[0]);
        return List.of();
    }
}
