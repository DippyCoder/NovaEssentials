package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class HealCommand extends BaseCommand {

    public HealCommand(NovaEssentials plugin) {
        super(plugin, "heal");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.heal")) return;

        if (args.length == 0) {
            if (!requirePlayer(sender)) return;
            heal((Player) sender);
            msg.send(sender, "heal.self");
        } else {
            if (!requirePermission(sender, "cmd.heal.other")) return;
            Player target = findPlayer(sender, args[0]);
            if (target == null) return;
            heal(target);
            msg.send(sender, "heal.other", "player", target.getName());
            if (!target.equals(sender)) msg.send(target, "heal.notify", "sender", sender.getName());
        }
    }

    private void heal(Player player) {
        var healthAttr = player.getAttribute(Attribute.MAX_HEALTH);
        double maxHealth = healthAttr != null ? healthAttr.getValue() : 20.0;
        if (healthAttr != null) player.setHealth(maxHealth);
        player.setFoodLevel(20);
        player.setSaturation(20f);
        player.setFireTicks(0);
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1 && sender.hasPermission(plugin.getConfigManager().getPermission("cmd.heal.other"))) {
            return filterPrefix(onlinePlayerNames(sender), args[0]);
        }
        return List.of();
    }
}
