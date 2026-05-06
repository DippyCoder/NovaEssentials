package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class SpawnCommand extends BaseCommand {

    public SpawnCommand(NovaEssentials plugin) {
        super(plugin, "spawn");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.spawn")) return;
        if (!requirePlayer(sender)) return;

        Location spawn = plugin.getSpawnManager().getSpawn();
        if (spawn == null) {
            msg.send(sender, "spawn.not-set");
            return;
        }
        Player player = (Player) sender;
        if (plugin.getTeleportDelayManager().startDelayedTeleport(player, spawn, "spawn")) {
            msg.send(sender, "spawn.teleported");
        }
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
