package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class SunCommand extends BaseCommand {

    public SunCommand(NovaEssentials plugin) {
        super(plugin, "sun");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.weather")) return;
        if (!requirePlayer(sender)) return;

        World world = ((Player) sender).getWorld();
        world.setStorm(false);
        world.setThundering(false);
        world.setWeatherDuration(Integer.MAX_VALUE);
        msg.send(sender, "weather.sun");
    }
}
