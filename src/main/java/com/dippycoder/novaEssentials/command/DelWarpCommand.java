package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class DelWarpCommand extends BaseCommand {

    public DelWarpCommand(NovaEssentials plugin) {
        super(plugin, "delwarp");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.delwarp")) return;
        if (args.length == 0) {
            msg.send(sender, "general.invalid-args", "usage", "/" + label + " <name>");
            return;
        }

        String name = args[0];
        if (plugin.getWarpManager().deleteWarp(name)) {
            msg.send(sender, "warp.deleted", "warp", name);
        } else {
            msg.send(sender, "warp.not-found", "warp", name);
        }
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return filterPrefix(new ArrayList<>(plugin.getWarpManager().getWarpNames()), args[0]);
        }
        return List.of();
    }
}
