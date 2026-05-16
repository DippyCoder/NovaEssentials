package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import com.dippycoder.novaEssentials.gui.HomeGui;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

public class HomeCommand extends BaseCommand {

    public HomeCommand(NovaEssentials plugin) {
        super(plugin, "home");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.home")) return;
        if (!requirePlayer(sender)) return;

        Player player = (Player) sender;

        if (args.length > 0 && args[0].equalsIgnoreCase("list")) {
            if (plugin.getConfigManager().isHomeGuiEnabled()) {
                HomeGui.open(player, plugin, 0);
            } else {
                listHomes(player);
            }
            return;
        }

        String name = args.length > 0 ? args[0]
                : plugin.getConfigManager().getDefaultHomeName();

        Location home = plugin.getHomeManager().getHome(player, name);
        if (home == null) {
            msg.send(sender, "home.not-found", "home", name);
            return;
        }
        player.teleport(home);
        msg.send(sender, "home.teleported", "home", name);
    }

    private void listHomes(Player player) {
        Set<String> homes = plugin.getHomeManager().getHomeNames(player);
        if (homes.isEmpty()) {
            msg.send(player, "home.no-homes");
            return;
        }
        msg.send(player, "home.list", "homes", String.join(", ", homes));
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1 && sender instanceof Player p) {
            List<String> completions = new ArrayList<>(plugin.getHomeManager().getHomeNames(p));
            completions.add("list");
            return filterPrefix(completions, args[0]);
        }
        return List.of();
    }
}
