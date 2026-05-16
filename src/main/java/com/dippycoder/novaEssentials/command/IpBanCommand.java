package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import com.dippycoder.novaEssentials.util.DurationUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.BanList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class IpBanCommand extends BaseCommand {

    public IpBanCommand(NovaEssentials plugin) {
        super(plugin, "ipban");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.ipban")) return;
        if (args.length < 2) {
            msg.send(sender, "general.invalid-args",
                    "usage", "/" + label + " <player|ip> <duration|perm> [reason]");
            return;
        }

        String input = args[0];
        String ip;

        if (input.matches("\\d+\\.\\d+\\.\\d+\\.\\d+") || input.contains(":")) {
            ip = input;
        } else {
            Player onlinePlayer = plugin.getServer().getPlayerExact(input);
            if (onlinePlayer == null) {
                msg.send(sender, "ipban.player-not-online", "player", input);
                return;
            }
            if (onlinePlayer.getAddress() == null) {
                msg.send(sender, "general.player-not-found", "player", input);
                return;
            }
            ip = onlinePlayer.getAddress().getAddress().getHostAddress();
        }

        long durationMs;
        try {
            durationMs = DurationUtil.parseMillis(args[1]);
        } catch (NumberFormatException e) {
            msg.send(sender, "general.invalid-number", "value", args[1]);
            return;
        }

        String reason = args.length > 2
                ? String.join(" ", Arrays.copyOfRange(args, 2, args.length))
                : "IP banned by staff";

        boolean permanent = durationMs == -1;
        String durationStr = permanent ? "permanent" : DurationUtil.format(durationMs);
        Date expiry = permanent ? null : new Date(System.currentTimeMillis() + durationMs);

        @SuppressWarnings({"unchecked", "rawtypes"})
        BanList ipBanList = plugin.getServer().getBanList(BanList.Type.IP);
        ipBanList.addBan(ip, reason, expiry, sender.getName());

        final String finalIp = ip;
        for (Player online : plugin.getServer().getOnlinePlayers()) {
            if (online.getAddress() != null
                    && finalIp.equals(online.getAddress().getAddress().getHostAddress())) {
                Component banScreen = msg.get(online, "ban.ban-screen",
                        "reason", reason, "duration", durationStr, "sender", sender.getName());
                online.kick(banScreen);
            }
        }

        if (permanent) {
            msg.send(sender, "ipban.banned-permanent", "ip", ip);
        } else {
            msg.send(sender, "ipban.banned", "ip", ip, "duration", durationStr);
        }
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>(onlinePlayerNames(sender));
            suggestions.add("0.0.0.0");
            return filterPrefix(suggestions, args[0]);
        }
        if (args.length == 2) {
            return filterPrefix(List.of("1h", "1d", "7d", "30d", "perm"), args[1]);
        }
        return List.of();
    }
}
