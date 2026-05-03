package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class ReplyCommand extends BaseCommand {

    public ReplyCommand(NovaEssentials plugin) {
        super(plugin, "r");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.msg")) return;
        if (!requirePlayer(sender)) return;
        if (args.length == 0) {
            msg.send(sender, "general.invalid-args", "usage", "/" + label + " <message>");
            return;
        }

        Player player = (Player) sender;
        UUID targetUuid = plugin.getMsgManager().getLastMessaged(player.getUniqueId());
        if (targetUuid == null) {
            msg.send(sender, "msg.no-reply");
            return;
        }

        Player target = plugin.getServer().getPlayer(targetUuid);
        if (target == null) {
            msg.send(sender, "msg.player-offline");
            return;
        }

        if (plugin.getBlockManager().isBlockedBy(player, target)) {
            msg.send(sender, "msg.blocked-by", "player", target.getName());
            return;
        }
        if (plugin.getBlockManager().isBlocked(player, target)) {
            msg.send(sender, "msg.player-blocked", "player", target.getName());
            return;
        }

        String message = String.join(" ", args);

        msg.send(sender, "msg.sent", "player", target.getName(), "message", message);
        msg.send(target, "msg.received", "player", player.getName(), "message", message);

        plugin.getMsgManager().recordMessage(player.getUniqueId(), target.getUniqueId());

        for (Player online : plugin.getServer().getOnlinePlayers()) {
            if (online.equals(player) || online.equals(target)) continue;
            if (online.hasPermission(plugin.getConfigManager().getPermission("cmd.msg.spy"))) {
                msg.send(online, "msg.spy",
                        "sender", player.getName(),
                        "receiver", target.getName(),
                        "message", message);
            }
        }
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
