package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class SetHomeCommand extends BaseCommand {

    public SetHomeCommand(NovaEssentials plugin) {
        super(plugin, "sethome");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.sethome")) return;
        if (!requirePlayer(sender)) return;

        Player player = (Player) sender;
        String name = args.length > 0 ? args[0] : plugin.getConfigManager().getDefaultHomeName();

        int limit = plugin.getConfigManager().getHomeLimit(player);
        int current = plugin.getHomeManager().getHomeCount(player);
        boolean isNew = !plugin.getHomeManager().hasHome(player, name);

        if (isNew && limit != -1 && current >= limit) {
            msg.send(sender, "home.limit-reached", "limit", limit);
            return;
        }

        plugin.getHomeManager().setHome(player, name, player.getLocation());
        msg.send(sender, "home.set", "home", name);
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1 && sender instanceof Player p) {
            return filterPrefix(List.copyOf(plugin.getHomeManager().getHomeNames(p)), args[0]);
        }
        return List.of();
    }
}
