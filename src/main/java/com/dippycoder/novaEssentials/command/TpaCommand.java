package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import com.dippycoder.novaEssentials.manager.PlayerSettingsManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class TpaCommand extends BaseCommand {

    public TpaCommand(NovaEssentials plugin) {
        super(plugin, "tpa");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.tpa")) return;
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

        // Check if target is accepting TPA requests
        PlayerSettingsManager sm = plugin.getPlayerSettingsManager();
        if (sm != null) {
            PlayerSettingsManager.PlayerSettings ts = sm.getSettings(target.getUniqueId());
            if (!ts.allowTpa) {
                msg.send(sender, "tpa.not-accepting", "player", target.getName());
                return;
            }
            // TPAUTO: auto-accept TPA (but NOT tpahere)
            if (ts.tpauto) {
                long remaining = plugin.getTeleportDelayManager()
                        .getRemainingCooldownSec(player, "tpa");
                if (remaining > 0) {
                    msg.send(sender, "tp.cooldown", "time", remaining);
                    return;
                }
                msg.send(sender, "tpa.sent", "player", target.getName());
                msg.send(target, "tpa.received", "player", player.getName());
                msg.send(player, "tpa.auto-accepted", "player", target.getName());
                plugin.getTeleportDelayManager()
                        .startDelayedTeleport(player, target.getLocation(), "tpa", false);
                return;
            }
        }

        plugin.getTpaManager().sendRequest(player, target);
        msg.send(sender, "tpa.sent", "player", target.getName());
        msg.send(target, "tpa.received", "player", player.getName());
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) return filterPrefix(onlinePlayerNames(sender), args[0]);
        return List.of();
    }
}
