package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class UnfreezeCommand extends BaseCommand {

    public UnfreezeCommand(NovaEssentials plugin) {
        super(plugin, "unfreeze");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.unfreeze")) return;

        if (args.length == 0) {
            msg.send(sender, "general.invalid-args", "usage", "/" + label + " <player>");
            return;
        }

        Player target = findPlayer(sender, args[0]);
        if (target == null) return;

        if (!plugin.getFreezeManager().isFrozen(target)) {
            msg.send(sender, "freeze.not-frozen", "player", target.getName());
            return;
        }

        plugin.getFreezeManager().unfreeze(target);
        msg.send(sender, "freeze.unfrozen", "player", target.getName());

        String senderName = sender instanceof Player p ? p.getName() : "Console";
        msg.send(target, "freeze.notify-unfrozen", "sender", senderName);
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) return filterPrefix(onlinePlayerNames(sender), args[0]);
        return List.of();
    }
}
