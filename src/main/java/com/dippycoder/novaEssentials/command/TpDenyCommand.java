package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import com.dippycoder.novaEssentials.manager.TpaManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class TpDenyCommand extends BaseCommand {

    public TpDenyCommand(NovaEssentials plugin) {
        super(plugin, "tpdeny");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.tpdeny")) return;
        if (!requirePlayer(sender)) return;

        Player receiver = (Player) sender;
        TpaManager tpa = plugin.getTpaManager();

        if (!tpa.hasPendingRequest(receiver)) {
            msg.send(sender, "tpa.no-request");
            return;
        }

        TpaManager.TpaRequest request = tpa.removeRequest(receiver);
        Player requester = plugin.getServer().getPlayer(request.sender());

        msg.send(receiver, "tpa.denied-receiver",
                "player", requester != null ? requester.getName() : "?");
        if (requester != null) {
            msg.send(requester, "tpa.denied-sender", "player", receiver.getName());
        }
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
