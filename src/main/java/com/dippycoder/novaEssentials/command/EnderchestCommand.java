package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class EnderchestCommand extends BaseCommand {

    public EnderchestCommand(NovaEssentials plugin) {
        super(plugin, "ec");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.ec")) return;
        if (!requirePlayer(sender)) return;

        Player viewer = (Player) sender;

        if (args.length == 0) {
            viewer.openInventory(viewer.getEnderChest());
            msg.send(sender, "ec.opened-self");
        } else {
            if (!requirePermission(sender, "cmd.ec.other")) return;
            Player target = findPlayer(sender, args[0]);
            if (target == null) return;
            viewer.openInventory(target.getEnderChest());
            msg.send(sender, "ec.opened-other", "player", target.getName());
        }
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1 && sender.hasPermission(plugin.getConfigManager().getPermission("cmd.ec.other"))) {
            return filterPrefix(onlinePlayerNames(sender), args[0]);
        }
        return List.of();
    }
}
