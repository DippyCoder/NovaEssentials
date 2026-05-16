package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import com.dippycoder.novaEssentials.manager.TpaManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class TpAcceptCommand extends BaseCommand {

    public TpAcceptCommand(NovaEssentials plugin) {
        super(plugin, "tpaccept");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.tpaccept")) return;
        if (!requirePlayer(sender)) return;

        Player receiver = (Player) sender;
        TpaManager tpa = plugin.getTpaManager();

        if (!tpa.hasPendingRequest(receiver)) {
            msg.send(sender, "tpa.no-request");
            return;
        }

        TpaManager.TpaRequest request = tpa.removeRequest(receiver);
        Player requester = plugin.getServer().getPlayer(request.sender());

        if (requester == null) {
            msg.send(sender, "msg.player-offline");
            return;
        }

        // For /tpahere: receiver (acceptor) teleports TO requester. For /tpa: requester teleports TO receiver.
        Player teleporting   = request.here() ? receiver  : requester;
        Player destination   = request.here() ? requester : receiver;

        int delay = plugin.getConfigManager().getTeleportDelay();
        msg.send(receiver, "tpa.accepted-receiver", "player", requester.getName());

        if (delay > 0) {
            msg.send(requester, "tpa.accepted-sender",
                    "player", receiver.getName(), "delay", delay);
        } else {
            msg.send(requester, "tpa.accepted-instant", "player", receiver.getName());
        }

        tpa.startDelayedTeleport(teleporting, destination.getLocation());
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
