package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class SummonCommand extends BaseCommand {

    public SummonCommand(NovaEssentials plugin) {
        super(plugin, "summon");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.summon")) return;
        if (!requirePlayer(sender)) return;
        if (args.length < 1) {
            msg.send(sender, "general.invalid-args", "usage", "/" + label + " <entity> [x y z] [amount]");
            return;
        }

        EntityType entityType;
        try {
            entityType = EntityType.valueOf(args[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            msg.send(sender, "summon.invalid-entity", "entity", args[0]);
            return;
        }

        Player player = (Player) sender;
        Location base = player.getLocation();
        Location spawnLoc = base.clone();
        int amount = 1;

        if (args.length >= 4) {
            try {
                double x = Double.parseDouble(args[1]);
                double y = Double.parseDouble(args[2]);
                double z = Double.parseDouble(args[3]);
                spawnLoc = new Location(base.getWorld(), x, y, z);
                if (args.length >= 5) {
                    amount = Integer.parseInt(args[4]);
                }
            } catch (NumberFormatException e) {
                try {
                    amount = Integer.parseInt(args[1]);
                } catch (NumberFormatException ex) {
                    msg.send(sender, "general.invalid-number", "value", args[1]);
                    return;
                }
            }
        } else if (args.length == 2) {
            try {
                amount = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                msg.send(sender, "general.invalid-number", "value", args[1]);
                return;
            }
        }

        amount = Math.min(Math.max(1, amount), 100);

        for (int i = 0; i < amount; i++) {
            spawnLoc.getWorld().spawnEntity(spawnLoc, entityType);
        }

        msg.send(sender, "summon.summoned",
                "entity", entityType.name().toLowerCase().replace("_", " "),
                "amount", String.valueOf(amount));
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> names = Arrays.stream(EntityType.values())
                    .filter(e -> e != EntityType.UNKNOWN && e != EntityType.PLAYER)
                    .map(e -> e.name().toLowerCase())
                    .toList();
            return filterPrefix(names, args[0]);
        }
        return List.of();
    }
}
