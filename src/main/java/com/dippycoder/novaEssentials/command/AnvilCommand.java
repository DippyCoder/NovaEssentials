package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class AnvilCommand extends BaseCommand {

    public AnvilCommand(NovaEssentials plugin) {
        super(plugin, "anvil");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.anvil")) return;
        if (!requirePlayer(sender)) return;

        Player viewer = (Player) sender;

        if (args.length == 0) {
            viewer.openAnvil(viewer.getLocation(), true);
            msg.send(sender, "anvil.opened-self");
        } else {
            if (!requirePermission(sender, "cmd.anvil.other")) return;
            Player target = findPlayer(sender, args[0]);
            if (target == null) return;
            target.openAnvil(target.getLocation(), true);
            msg.send(sender, "anvil.opened-other", "player", target.getName());
        }
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1 && sender.hasPermission(plugin.getConfigManager().getPermission("cmd.anvil.other"))) {
            return filterPrefix(onlinePlayerNames(sender), args[0]);
        }
        return List.of();
    }
}
