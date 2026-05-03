package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class GamemodeCommand extends BaseCommand {

    public GamemodeCommand(NovaEssentials plugin) {
        super(plugin, "gm");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.gm")) return;
        if (args.length == 0) {
            msg.send(sender, "general.invalid-args", "usage", "/gm <mode> [player]");
            return;
        }

        GameMode mode = parseMode(args[0]);
        if (mode == null) {
            msg.send(sender, "gamemode.invalid-mode", "mode", args[0]);
            return;
        }

        if (args.length == 1) {
            if (!requirePlayer(sender)) return;
            Player player = (Player) sender;
            player.setGameMode(mode);
            // Manage fly state consistency
            if (mode == GameMode.SURVIVAL || mode == GameMode.ADVENTURE) {
                if (!plugin.getFlyManager().hasPluginFly(player)) {
                    player.setAllowFlight(false);
                    player.setFlying(false);
                }
            }
            msg.send(sender, "gamemode.changed-self", "mode", friendlyName(mode));
        } else {
            if (!requirePermission(sender, "cmd.gm.other")) return;
            Player target = findPlayer(sender, args[1]);
            if (target == null) return;
            target.setGameMode(mode);
            if (mode == GameMode.SURVIVAL || mode == GameMode.ADVENTURE) {
                if (!plugin.getFlyManager().hasPluginFly(target)) {
                    target.setAllowFlight(false);
                    target.setFlying(false);
                }
            }
            msg.send(sender, "gamemode.changed-other",
                    "player", target.getName(), "mode", friendlyName(mode));
            if (!target.equals(sender)) {
                msg.send(target, "gamemode.notify",
                        "mode", friendlyName(mode),
                        "sender", sender.getName());
            }
        }
    }

    private GameMode parseMode(String input) {
        return switch (input.toLowerCase()) {
            case "survival", "s", "0" -> GameMode.SURVIVAL;
            case "creative", "c", "1" -> GameMode.CREATIVE;
            case "adventure", "a", "2" -> GameMode.ADVENTURE;
            case "spectator", "sp", "3" -> GameMode.SPECTATOR;
            default -> null;
        };
    }

    private String friendlyName(GameMode mode) {
        return switch (mode) {
            case SURVIVAL   -> "Survival";
            case CREATIVE   -> "Creative";
            case ADVENTURE  -> "Adventure";
            case SPECTATOR  -> "Spectator";
        };
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return filterPrefix(List.of("survival", "creative", "adventure", "spectator"), args[0]);
        }
        if (args.length == 2 && sender.hasPermission(plugin.getConfigManager().getPermission("cmd.gm.other"))) {
            return filterPrefix(onlinePlayerNames(sender), args[1]);
        }
        return List.of();
    }
}
