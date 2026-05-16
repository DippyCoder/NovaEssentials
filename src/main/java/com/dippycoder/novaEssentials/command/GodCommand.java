package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class GodCommand extends BaseCommand {

    public GodCommand(NovaEssentials plugin) {
        super(plugin, "god");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.god")) return;

        if (args.length == 0) {
            if (!requirePlayer(sender)) return;
            Player player = (Player) sender;
            boolean nowGod = plugin.getGodManager().toggle(player);
            msg.send(sender, nowGod ? "god.enabled" : "god.disabled");
        } else {
            if (!requirePermission(sender, "cmd.god.other")) return;
            Player target = findPlayer(sender, args[0]);
            if (target == null) return;
            boolean nowGod = plugin.getGodManager().toggle(target);
            msg.send(sender, nowGod ? "god.other-enabled" : "god.other-disabled",
                    "player", target.getName());
            if (!target.equals(sender)) {
                msg.send(target, nowGod ? "god.notify-on" : "god.notify-off",
                        "sender", sender.getName());
            }
        }
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1 && sender.hasPermission(plugin.getConfigManager().getPermission("cmd.god.other"))) {
            return filterPrefix(onlinePlayerNames(sender), args[0]);
        }
        return List.of();
    }
}
