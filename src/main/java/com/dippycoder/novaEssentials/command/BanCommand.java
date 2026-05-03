package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import com.dippycoder.novaEssentials.util.DurationUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.BanList;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class BanCommand extends BaseCommand {

    public BanCommand(NovaEssentials plugin) {
        super(plugin, "ban");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.ban")) return;
        if (args.length < 2) {
            msg.send(sender, "general.invalid-args",
                    "usage", "/" + label + " <player> <duration|perm> [reason]");
            return;
        }

        String targetName = args[0];
        @SuppressWarnings("deprecation")
        OfflinePlayer target = plugin.getServer().getOfflinePlayer(targetName);

        @SuppressWarnings({"unchecked", "rawtypes"})
        BanList banList = plugin.getServer().getBanList(BanList.Type.NAME);

        if (banList.isBanned(targetName)) {
            msg.send(sender, "ban.already-banned", "player", targetName);
            return;
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
                : "Banned by staff";

        Date expiry = durationMs == -1 ? null : new Date(System.currentTimeMillis() + durationMs);
        banList.addBan(targetName, reason, expiry, sender.getName());

        // Kick if online
        Player onlineTarget = plugin.getServer().getPlayerExact(targetName);
        if (onlineTarget != null) {
            boolean permanent = durationMs == -1;
            String durationStr = permanent ? "permanent" : DurationUtil.format(durationMs);
            Component banScreen = msg.get(onlineTarget, "ban.ban-screen",
                    "reason", reason, "duration", durationStr, "sender", sender.getName());
            onlineTarget.kick(banScreen);
        }

        boolean permanent = durationMs == -1;
        String durationStr = permanent ? "permanent" : DurationUtil.format(durationMs);

        if (permanent) {
            msg.send(sender, "ban.banned-permanent", "player", targetName);
        } else {
            msg.send(sender, "ban.banned", "player", targetName, "duration", durationStr);
        }

        // Staff notification — each recipient gets the message in their own locale
        msg.broadcastToPermission(plugin.getConfigManager().getPermission("cmd.ban"),
                "ban.staff-broadcast",
                "player", targetName, "sender", sender.getName(),
                "duration", durationStr, "reason", reason);
        plugin.getDiscordWebhookManager().onBan(targetName, reason, durationStr, sender.getName());
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) return filterPrefix(onlinePlayerNames(sender), args[0]);
        if (args.length == 2) return filterPrefix(List.of("1h", "1d", "7d", "30d", "perm"), args[1]);
        return List.of();
    }
}
