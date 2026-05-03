package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class SetSpawnCommand extends BaseCommand {

    public SetSpawnCommand(NovaEssentials plugin) {
        super(plugin, "setspawn");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.setspawn")) return;
        if (!requirePlayer(sender)) return;

        Player player = (Player) sender;
        plugin.getSpawnManager().setSpawn(player.getLocation());
        msg.send(sender, "spawn.set");
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
