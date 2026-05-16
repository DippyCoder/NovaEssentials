package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class SetWarpCommand extends BaseCommand {

    public SetWarpCommand(NovaEssentials plugin) {
        super(plugin, "setwarp");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.setwarp")) return;
        if (!requirePlayer(sender)) return;
        if (args.length == 0) {
            msg.send(sender, "general.invalid-args", "usage", "/" + label + " <name>");
            return;
        }

        Player player = (Player) sender;
        String name = args[0];
        plugin.getWarpManager().setWarp(name, player.getLocation());
        msg.send(sender, "warp.set", "warp", name);
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
