package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class DelHomeCommand extends BaseCommand {

    public DelHomeCommand(NovaEssentials plugin) {
        super(plugin, "delhome");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.delhome")) return;
        if (!requirePlayer(sender)) return;
        if (args.length == 0) {
            msg.send(sender, "general.invalid-args", "usage", "/" + label + " <name>");
            return;
        }

        Player player = (Player) sender;
        String name = args[0];
        if (plugin.getHomeManager().deleteHome(player, name)) {
            msg.send(sender, "home.deleted", "home", name);
        } else {
            msg.send(sender, "home.not-found", "home", name);
        }
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1 && sender instanceof Player p) {
            return filterPrefix(List.copyOf(plugin.getHomeManager().getHomeNames(p)), args[0]);
        }
        return List.of();
    }
}
