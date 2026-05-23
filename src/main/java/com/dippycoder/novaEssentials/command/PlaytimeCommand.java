package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class PlaytimeCommand extends BaseCommand {

    public PlaytimeCommand(NovaEssentials plugin) {
        super(plugin, "playtime");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.playtime")) return;

        if (!plugin.getConfigManager().isPlaytimeEnabled()) {
            msg.send(sender, "playtime.disabled");
            return;
        }

        if (args.length == 0) {
            if (!requirePlayer(sender)) return;
            Player player = (Player) sender;
            long totalMs = plugin.getPlaytimeManager().getPlaytime(player.getUniqueId());
            msg.send(sender, "playtime.display",
                    "player", player.getName(),
                    "time", plugin.getPlaytimeManager().format(totalMs));
        } else {
            if (!requirePermission(sender, "cmd.playtime.other")) return;
            String targetName = args[0];

            Player online = plugin.getServer().getPlayerExact(targetName);
            UUID uuid;
            String name;
            if (online != null) {
                uuid = online.getUniqueId();
                name = online.getName();
            } else {
                @SuppressWarnings("deprecation")
                OfflinePlayer offline = plugin.getServer().getOfflinePlayer(targetName);
                if (!offline.hasPlayedBefore()) {
                    msg.send(sender, "general.player-not-found", "player", targetName);
                    return;
                }
                uuid = offline.getUniqueId();
                name = offline.getName() != null ? offline.getName() : targetName;
            }

            long totalMs = plugin.getPlaytimeManager().getPlaytime(uuid);
            msg.send(sender, "playtime.display-other",
                    "player", name,
                    "time", plugin.getPlaytimeManager().format(totalMs));
        }
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1
                && sender.hasPermission(plugin.getConfigManager().getPermission("cmd.playtime.other"))) {
            return filterPrefix(onlinePlayerNames(sender), args[0]);
        }
        return Collections.emptyList();
    }
}
