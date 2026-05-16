package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import com.dippycoder.novaEssentials.manager.PlayerSettingsManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/** /tpahere — request another player to teleport TO the sender. */
public class TpaHereCommand extends BaseCommand {

    public TpaHereCommand(NovaEssentials plugin) {
        super(plugin, "tpahere");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.tpahere")) return;
        if (!requirePlayer(sender)) return;
        if (args.length == 0) {
            msg.send(sender, "general.invalid-args", "usage", "/" + label + " <player>");
            return;
        }

        Player player = (Player) sender;
        Player target = findPlayer(sender, args[0]);
        if (target == null) return;

        if (target.equals(player)) {
            msg.send(sender, "general.invalid-args", "usage", "/" + label + " <player>");
            return;
        }

        if (plugin.getBlockManager().isBlockedBy(player, target)) {
            msg.send(sender, "tpa.blocked", "player", target.getName());
            return;
        }

        if (plugin.getTpaManager().hasSentRequest(player, target)) {
            msg.send(sender, "tpa.already-pending", "player", target.getName());
            return;
        }

        // Check if target is accepting tpahere requests
        PlayerSettingsManager sm = plugin.getPlayerSettingsManager();
        if (sm != null) {
            PlayerSettingsManager.PlayerSettings ts = sm.getSettings(target.getUniqueId());
            if (!ts.allowTpaHere) {
                msg.send(sender, "tpa.not-accepting-here", "player", target.getName());
                return;
            }
        }

        plugin.getTpaManager().sendHereRequest(player, target);
        msg.send(sender, "tpa.here-sent", "player", target.getName());
        msg.send(target, "tpa.here-received", "player", player.getName());
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) return filterPrefix(onlinePlayerNames(sender), args[0]);
        return List.of();
    }
}
