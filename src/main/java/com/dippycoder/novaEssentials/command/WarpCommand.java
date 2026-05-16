package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import com.dippycoder.novaEssentials.gui.WarpGui;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class WarpCommand extends BaseCommand {

    public WarpCommand(NovaEssentials plugin) {
        super(plugin, "warp");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.warp")) return;

        if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
            if (plugin.getConfigManager().isWarpGuiEnabled()) {
                if (!requirePlayer(sender)) return;
                WarpGui.open((Player) sender, plugin, 0);
            } else {
                var names = plugin.getWarpManager().getWarpNames();
                if (names.isEmpty()) {
                    msg.send(sender, "warp.no-warps");
                } else {
                    msg.send(sender, "warp.list", "warps", String.join(", ", names));
                }
            }
            return;
        }

        if (!requirePlayer(sender)) return;
        Player player = (Player) sender;
        String name = args[0];
        Location warp = plugin.getWarpManager().getWarp(name);
        if (warp == null) {
            msg.send(sender, "warp.not-found", "warp", name);
            return;
        }
        if (plugin.getTeleportDelayManager().startDelayedTeleport(player, warp, "warp")) {
            msg.send(sender, "warp.teleported", "warp", name);
        }
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> opts = new ArrayList<>(plugin.getWarpManager().getWarpNames());
            opts.add("list");
            return filterPrefix(opts, args[0]);
        }
        return List.of();
    }
}
