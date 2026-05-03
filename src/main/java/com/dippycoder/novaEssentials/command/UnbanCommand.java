package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import net.kyori.adventure.text.Component;
import org.bukkit.BanList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class UnbanCommand extends BaseCommand {

    public UnbanCommand(NovaEssentials plugin) {
        super(plugin, "unban");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.unban")) return;
        if (args.length == 0) {
            msg.send(sender, "general.invalid-args", "usage", "/" + label + " <player>");
            return;
        }

        String targetName = args[0];
        @SuppressWarnings({"unchecked", "rawtypes"})
        BanList banList = plugin.getServer().getBanList(BanList.Type.NAME);

        if (!banList.isBanned(targetName)) {
            msg.send(sender, "ban.not-banned", "player", targetName);
            return;
        }

        banList.pardon(targetName);
        msg.send(sender, "ban.unbanned", "player", targetName);

        Component staffMsg = msg.get(sender, "ban.staff-unban",
                "player", targetName, "sender", sender.getName());
        for (Player online : plugin.getServer().getOnlinePlayers()) {
            if (online.hasPermission(plugin.getConfigManager().getPermission("cmd.unban"))) {
                online.sendMessage(staffMsg);
            }
        }
        plugin.getServer().getConsoleSender().sendMessage(staffMsg);
        plugin.getDiscordWebhookManager().onUnban(targetName, sender.getName());
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
