package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class FeedCommand extends BaseCommand {

    public FeedCommand(NovaEssentials plugin) {
        super(plugin, "feed");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.feed")) return;

        if (args.length == 0) {
            if (!requirePlayer(sender)) return;
            feed((Player) sender);
            msg.send(sender, "feed.self");
        } else {
            if (!requirePermission(sender, "cmd.feed.other")) return;
            Player target = findPlayer(sender, args[0]);
            if (target == null) return;
            feed(target);
            msg.send(sender, "feed.other", "player", target.getName());
            if (!target.equals(sender)) msg.send(target, "feed.notify", "sender", sender.getName());
        }
    }

    private void feed(Player player) {
        player.setFoodLevel(20);
        player.setSaturation(20f);
        player.setExhaustion(0f);
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1 && sender.hasPermission(plugin.getConfigManager().getPermission("cmd.feed.other"))) {
            return filterPrefix(onlinePlayerNames(sender), args[0]);
        }
        return List.of();
    }
}
