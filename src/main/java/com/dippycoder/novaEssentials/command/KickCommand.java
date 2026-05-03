package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class KickCommand extends BaseCommand {

    public KickCommand(NovaEssentials plugin) {
        super(plugin, "kick");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.kick")) return;
        if (args.length == 0) {
            msg.send(sender, "general.invalid-args", "usage", "/" + label + " <player> [reason]");
            return;
        }

        Player target = findPlayer(sender, args[0]);
        if (target == null) return;

        String reason = args.length > 1
                ? String.join(" ", Arrays.copyOfRange(args, 1, args.length))
                : "Kicked by staff";

        Component kickScreen = msg.get(target, "kick.kick-screen",
                "reason", reason, "sender", sender.getName());
        target.kick(kickScreen);

        msg.send(sender, "kick.kicked", "player", target.getName());

        // Notify staff
        Component staffMsg = msg.get(sender, "kick.staff-broadcast",
                "player", target.getName(), "sender", sender.getName(), "reason", reason);
        for (Player online : plugin.getServer().getOnlinePlayers()) {
            if (online.hasPermission(plugin.getConfigManager().getPermission("cmd.kick"))) {
                online.sendMessage(staffMsg);
            }
        }
        plugin.getServer().getConsoleSender().sendMessage(staffMsg);
        plugin.getDiscordWebhookManager().onKick(target.getName(), reason, sender.getName());
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) return filterPrefix(onlinePlayerNames(sender), args[0]);
        return List.of();
    }
}
