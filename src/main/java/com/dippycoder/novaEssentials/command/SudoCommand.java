package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class SudoCommand extends BaseCommand {

    public SudoCommand(NovaEssentials plugin) {
        super(plugin, "sudo");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.sudo")) return;
        if (args.length < 2) {
            msg.send(sender, "general.invalid-args",
                    "usage", "/" + label + " <player> <message|/command>");
            return;
        }

        Player target = findPlayer(sender, args[0]);
        if (target == null) return;

        String action = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));

        if (action.startsWith("/")) {
            plugin.getServer().dispatchCommand(target, action.substring(1));
        } else {
            target.chat(action);
        }

        msg.send(sender, "sudo.executed",
                "player", target.getName(), "action", action);
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) return filterPrefix(onlinePlayerNames(sender), args[0]);
        return List.of();
    }
}
