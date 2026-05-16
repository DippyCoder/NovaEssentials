package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import com.dippycoder.novaEssentials.util.DurationUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class MuteCommand extends BaseCommand {

    public MuteCommand(NovaEssentials plugin) {
        super(plugin, "mute");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.mute")) return;
        if (args.length == 0) {
            msg.send(sender, "general.invalid-args", "usage", "/" + label + " <player> [duration] [reason]");
            return;
        }

        Player target = findPlayer(sender, args[0]);
        if (target == null) return;

        if (plugin.getMuteManager().isMuted(target.getUniqueId())) {
            msg.send(sender, "chat.already-muted", "player", target.getName());
            return;
        }

        long until;
        int reasonStart = 2;

        if (args.length >= 2) {
            try {
                until = DurationUtil.parseMillis(args[1]);
                if (until != -1) until = System.currentTimeMillis() + until;
            } catch (NumberFormatException e) {
                // Treat second arg as beginning of reason (no duration given)
                until = -1;
                reasonStart = 1;
            }
        } else {
            until = -1; // permanent
        }

        String reason = args.length > reasonStart
                ? String.join(" ", Arrays.copyOfRange(args, reasonStart, args.length))
                : "No reason given";

        plugin.getMuteManager().mute(target.getUniqueId(), sender.getName(), reason, until);

        boolean permanent = until == -1;
        if (permanent) {
            msg.send(sender, "chat.muted-permanent-player", "player", target.getName());
            msg.send(target, "chat.notify-muted-permanent", "sender", sender.getName());
        } else {
            String formatted = DurationUtil.format(until - System.currentTimeMillis());
            msg.send(sender, "chat.muted-player", "player", target.getName(), "duration", formatted);
            msg.send(target, "chat.notify-muted", "duration", formatted, "sender", sender.getName());
        }
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) return filterPrefix(onlinePlayerNames(sender), args[0]);
        if (args.length == 2) return filterPrefix(List.of("1h", "30m", "7d", "perm"), args[1]);
        return List.of();
    }
}
