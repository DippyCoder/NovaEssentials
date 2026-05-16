package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class BurnCommand extends BaseCommand {

    private static final int DEFAULT_DURATION = 10;
    private static final int MAX_DURATION = 300;

    public BurnCommand(NovaEssentials plugin) {
        super(plugin, "burn");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.burn")) return;
        if (!requirePlayer(sender)) return;

        Player senderPlayer = (Player) sender;
        Player target = senderPlayer;
        int durationSecs = DEFAULT_DURATION;

        if (args.length >= 1) {
            try {
                durationSecs = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                target = findPlayer(sender, args[0]);
                if (target == null) return;
                if (args.length >= 2) {
                    try {
                        durationSecs = Integer.parseInt(args[1]);
                    } catch (NumberFormatException ex) {
                        msg.send(sender, "general.invalid-number", "value", args[1]);
                        return;
                    }
                }
            }
        }

        durationSecs = Math.min(Math.max(1, durationSecs), MAX_DURATION);

        String durationStr = formatDuration(durationSecs);
        target.setFireTicks(durationSecs * 20);

        if (target == senderPlayer) {
            msg.send(sender, "burn.self", "duration", durationStr);
        } else {
            msg.send(sender, "burn.other", "player", target.getName(), "duration", durationStr);
            msg.send(target, "burn.notify", "sender", sender.getName(), "duration", durationStr);
        }

        final Player burnTarget = target;
        final int maxTicks = durationSecs * 20;
        final int[] elapsed = {0};

        new BukkitRunnable() {
            @Override
            public void run() {
                elapsed[0] += 4;
                if (elapsed[0] >= maxTicks || !burnTarget.isOnline()) {
                    cancel();
                    return;
                }
                burnTarget.setFireTicks(40);

                Block block = burnTarget.getLocation().getBlock();
                if (block.getType() == Material.AIR) {
                    block.setType(Material.FIRE);
                }
            }
        }.runTaskTimer(plugin, 4L, 4L);
    }

    private String formatDuration(int secs) {
        if (secs >= 60) return (secs / 60) + "m " + (secs % 60) + "s";
        return secs + "s";
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) return filterPrefix(onlinePlayerNames(sender), args[0]);
        if (args.length == 2) return filterPrefix(List.of("10", "30", "60", "120"), args[1]);
        return List.of();
    }
}
