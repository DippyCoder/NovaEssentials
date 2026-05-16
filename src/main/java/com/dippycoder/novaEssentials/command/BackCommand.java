package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class BackCommand extends BaseCommand {

    public BackCommand(NovaEssentials plugin) {
        super(plugin, "back");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.back")) return;
        if (!requirePlayer(sender)) return;

        Player player = (Player) sender;
        Location loc = plugin.getBackManager().getLastDeathLocation(player);
        if (loc == null) {
            msg.send(sender, "back.no-location");
            return;
        }
        if (plugin.getTeleportDelayManager().startDelayedTeleport(player, loc, "back")) {
            msg.send(sender, "back.teleported");
        }
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
