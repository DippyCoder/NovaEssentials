package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class StrikeCommand extends BaseCommand {

    public StrikeCommand(NovaEssentials plugin) {
        super(plugin, "strike");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.strike")) return;

        if (args.length == 0) {
            if (!requirePlayer(sender)) return;
            Player player = (Player) sender;
            player.getWorld().strikeLightning(player.getLocation());
            msg.send(sender, "strike.self");
        } else {
            Player target = findPlayer(sender, args[0]);
            if (target == null) return;
            target.getWorld().strikeLightning(target.getLocation());
            msg.send(sender, "strike.struck", "player", target.getName());
        }
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) return filterPrefix(onlinePlayerNames(sender), args[0]);
        return List.of();
    }
}
