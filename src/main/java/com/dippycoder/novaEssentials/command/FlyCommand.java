package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class FlyCommand extends BaseCommand {

    public FlyCommand(NovaEssentials plugin) {
        super(plugin, "fly");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.fly")) return;

        if (args.length == 0) {
            if (!requirePlayer(sender)) return;
            Player player = (Player) sender;
            boolean nowFlying = plugin.getFlyManager().toggle(player);
            msg.send(sender, nowFlying ? "fly.enabled" : "fly.disabled");
        } else {
            if (!requirePermission(sender, "cmd.fly.other")) return;
            Player target = findPlayer(sender, args[0]);
            if (target == null) return;
            boolean nowFlying = plugin.getFlyManager().toggle(target);
            msg.send(sender, nowFlying ? "fly.other-enabled" : "fly.other-disabled",
                    "player", target.getName());
            if (!target.equals(sender)) {
                msg.send(target, nowFlying ? "fly.notify-on" : "fly.notify-off",
                        "sender", sender.getName());
            }
        }
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1 && sender.hasPermission(plugin.getConfigManager().getPermission("cmd.fly.other"))) {
            return filterPrefix(onlinePlayerNames(sender), args[0]);
        }
        return List.of();
    }
}
