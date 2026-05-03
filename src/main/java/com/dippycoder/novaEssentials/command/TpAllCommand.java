package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class TpAllCommand extends BaseCommand {

    public TpAllCommand(NovaEssentials plugin) {
        super(plugin, "tpall");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.tpall")) return;
        if (!requirePlayer(sender)) return;

        Player sender_ = (Player) sender;
        Location dest = sender_.getLocation();
        int count = 0;

        for (Player online : plugin.getServer().getOnlinePlayers()) {
            if (online.equals(sender_)) continue;
            online.teleport(dest);
            msg.send(online, "tpall.notify", "sender", sender_.getName());
            count++;
        }
        msg.send(sender, "tpall.teleported", "count", count);
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
