package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class SpeedCommand extends BaseCommand {

    public SpeedCommand(NovaEssentials plugin) {
        super(plugin, "speed");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.speed")) return;
        if (args.length == 0) {
            msg.send(sender, "general.invalid-args", "usage", "/" + label + " <0-10> [player]");
            return;
        }

        float speed;
        try {
            float raw = Float.parseFloat(args[0]);
            if (raw < 0 || raw > 10) {
                msg.send(sender, "speed.invalid");
                return;
            }
            speed = raw / 10f; // 0.0 – 1.0
        } catch (NumberFormatException e) {
            msg.send(sender, "general.invalid-number", "value", args[0]);
            return;
        }

        if (args.length == 1) {
            if (!requirePlayer(sender)) return;
            applySpeed((Player) sender, speed);
            msg.send(sender, "speed.set-self", "speed", args[0]);
        } else {
            if (!requirePermission(sender, "cmd.speed.other")) return;
            Player target = findPlayer(sender, args[1]);
            if (target == null) return;
            applySpeed(target, speed);
            msg.send(sender, "speed.set-other", "player", target.getName(), "speed", args[0]);
            if (!target.equals(sender)) {
                msg.send(target, "speed.notify", "speed", args[0], "sender", sender.getName());
            }
        }
    }

    private void applySpeed(Player player, float speed) {
        if (player.getGameMode() == GameMode.CREATIVE
                || player.getGameMode() == GameMode.SPECTATOR
                || plugin.getFlyManager().hasPluginFly(player)) {
            player.setFlySpeed(speed == 0 ? 0.001f : speed);
        } else {
            player.setWalkSpeed(speed == 0 ? 0.001f : speed * 0.4f);
        }
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) return List.of("1", "5", "10");
        if (args.length == 2 && sender.hasPermission(plugin.getConfigManager().getPermission("cmd.speed.other"))) {
            return filterPrefix(onlinePlayerNames(sender), args[1]);
        }
        return List.of();
    }
}
