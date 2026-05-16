package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class TimeCommand extends BaseCommand {

    public TimeCommand(NovaEssentials plugin) {
        super(plugin, "time");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.time")) return;

        // Resolve via label alias shortcuts
        String timeArg;
        if (args.length == 0) {
            timeArg = switch (label.toLowerCase()) {
                case "day"      -> "day";
                case "night"    -> "night";
                case "noon"     -> "noon";
                case "midnight" -> "midnight";
                default -> {
                    msg.send(sender, "general.invalid-args",
                            "usage", "/time <day|night|noon|midnight|value>");
                    yield null;
                }
            };
            if (timeArg == null) return;
        } else {
            timeArg = args[0];
        }

        long ticks = switch (timeArg.toLowerCase()) {
            case "day"      -> 1000L;
            case "noon"     -> 6000L;
            case "night"    -> 13000L;
            case "midnight" -> 18000L;
            default -> {
                try {
                    yield Long.parseLong(timeArg);
                } catch (NumberFormatException e) {
                    msg.send(sender, "time.invalid", "value", timeArg);
                    yield -1L;
                }
            }
        };
        if (ticks < 0) return;

        // Apply to player's world or all worlds for console
        if (sender instanceof Player player) {
            player.getWorld().setTime(ticks);
        } else {
            for (World world : plugin.getServer().getWorlds()) {
                world.setTime(ticks);
            }
        }
        msg.send(sender, "time.set", "time", timeArg);
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return filterPrefix(List.of("day", "night", "noon", "midnight"), args[0]);
        }
        return List.of();
    }
}
