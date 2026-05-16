package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class GiantCommand extends BaseCommand {

    private static final double DEFAULT_GIANT_SIZE = 2.0;
    private static final double MIN_SIZE = 0.0625;
    private static final double MAX_SIZE = 16.0;

    public GiantCommand(NovaEssentials plugin) {
        super(plugin, "giant");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.giant")) return;

        if (args.length == 0) {
            msg.send(sender, "general.invalid-args", "usage", "/" + label + " <player> [size|reset]");
            return;
        }

        Player target = findPlayer(sender, args[0]);
        if (target == null) return;

        AttributeInstance attr = target.getAttribute(Attribute.SCALE);
        if (attr == null) {
            msg.send(sender, "size.not-supported");
            return;
        }

        // Handle reset
        if (args.length >= 2 && args[1].equalsIgnoreCase("reset")) {
            attr.setBaseValue(1.0);
            msg.send(sender, "size.reset", "player", target.getName());

            String senderName = sender instanceof Player p ? p.getName() : "Console";
            if (!target.equals(sender)) {
                msg.send(target, "size.notify-reset", "sender", senderName);
            }
            return;
        }

        double size = DEFAULT_GIANT_SIZE;
        if (args.length >= 2) {
            try {
                size = Double.parseDouble(args[1]);
            } catch (NumberFormatException e) {
                msg.send(sender, "size.invalid");
                return;
            }
            if (size < MIN_SIZE || size > MAX_SIZE) {
                msg.send(sender, "size.invalid");
                return;
            }
        }

        attr.setBaseValue(size);

        String sizeStr = formatSize(size);
        msg.send(sender, "size.giant", "player", target.getName(), "size", sizeStr);

        String senderName = sender instanceof Player p ? p.getName() : "Console";
        if (!target.equals(sender)) {
            msg.send(target, "size.notify", "size", sizeStr, "sender", senderName);
        }
    }

    private String formatSize(double size) {
        if (size == Math.floor(size)) {
            return String.valueOf((int) size);
        }
        return String.valueOf(size);
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) return filterPrefix(onlinePlayerNames(sender), args[0]);
        if (args.length == 2) return filterPrefix(List.of("2.0", "3.0", "5.0", "10.0", "reset"), args[1]);
        return List.of();
    }
}
