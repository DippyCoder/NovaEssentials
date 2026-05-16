package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class RainCommand extends BaseCommand {

    public RainCommand(NovaEssentials plugin) {
        super(plugin, "rain");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.weather")) return;
        if (!requirePlayer(sender)) return;

        World world = ((Player) sender).getWorld();
        world.setThundering(false);
        world.setStorm(true);
        world.setWeatherDuration(12000);
        msg.send(sender, "weather.rain");
    }
}
