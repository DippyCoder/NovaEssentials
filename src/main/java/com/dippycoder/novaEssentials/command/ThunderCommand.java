package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ThunderCommand extends BaseCommand {

    public ThunderCommand(NovaEssentials plugin) {
        super(plugin, "thunder");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.weather")) return;
        if (!requirePlayer(sender)) return;

        World world = ((Player) sender).getWorld();
        world.setStorm(true);
        world.setThundering(true);
        world.setWeatherDuration(12000);
        world.setThunderDuration(12000);
        msg.send(sender, "weather.thunder");
    }
}
