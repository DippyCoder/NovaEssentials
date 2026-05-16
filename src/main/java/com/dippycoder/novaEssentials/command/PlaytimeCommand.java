package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class PlaytimeCommand extends BaseCommand {

    public PlaytimeCommand(NovaEssentials plugin) {
        super(plugin, "playtime");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.playtime")) return;
        if (!requirePlayer(sender)) return;

        Player player = (Player) sender;
        long totalMs = plugin.getPlaytimeManager().getPlaytime(player.getUniqueId());
        String formatted = plugin.getPlaytimeManager().format(totalMs);

        msg.send(sender, "playtime.display", "time", formatted);
    }
}
