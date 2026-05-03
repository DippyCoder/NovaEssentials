package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class WorkbenchCommand extends BaseCommand {

    public WorkbenchCommand(NovaEssentials plugin) {
        super(plugin, "workbench");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.workbench")) return;
        if (!requirePlayer(sender)) return;

        Player viewer = (Player) sender;

        if (args.length == 0) {
            viewer.openWorkbench(viewer.getLocation(), true);
            msg.send(sender, "workbench.opened-self");
        } else {
            if (!requirePermission(sender, "cmd.workbench.other")) return;
            Player target = findPlayer(sender, args[0]);
            if (target == null) return;
            target.openWorkbench(target.getLocation(), true);
            msg.send(sender, "workbench.opened-other", "player", target.getName());
        }
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1 && sender.hasPermission(plugin.getConfigManager().getPermission("cmd.workbench.other"))) {
            return filterPrefix(onlinePlayerNames(sender), args[0]);
        }
        return List.of();
    }
}
