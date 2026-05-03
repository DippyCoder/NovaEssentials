package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import com.dippycoder.novaEssentials.util.DurationUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public class ChatCommand extends BaseCommand {

    private boolean paused = false;
    private BukkitTask unpauseTask = null;

    public ChatCommand(NovaEssentials plugin) {
        super(plugin, "chat");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.chat")) return;
        if (args.length == 0) {
            msg.send(sender, "general.invalid-args", "usage", "/" + label + " <pause|unpause> [duration]");
            return;
        }

        switch (args[0].toLowerCase()) {
            case "pause" -> {
                paused = true;
                if (unpauseTask != null) {
                    unpauseTask.cancel();
                    unpauseTask = null;
                }
                if (args.length >= 2) {
                    long durationMs;
                    try {
                        durationMs = DurationUtil.parseMillis(args[1]);
                    } catch (NumberFormatException e) {
                        msg.send(sender, "general.invalid-number", "value", args[1]);
                        return;
                    }
                    if (durationMs > 0) {
                        String formatted = DurationUtil.format(durationMs);
                        plugin.getServer().broadcast(msg.get(sender, "chat.paused-timed",
                                "duration", formatted));
                        unpauseTask = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                            paused = false;
                            unpauseTask = null;
                            plugin.getServer().broadcast(msg.get(sender, "chat.resumed"));
                        }, durationMs / 50);
                        return;
                    }
                }
                plugin.getServer().broadcast(msg.get(sender, "chat.paused"));
            }
            case "unpause", "resume" -> {
                paused = false;
                if (unpauseTask != null) {
                    unpauseTask.cancel();
                    unpauseTask = null;
                }
                plugin.getServer().broadcast(msg.get(sender, "chat.resumed"));
            }
            default -> msg.send(sender, "general.invalid-args",
                    "usage", "/" + label + " <pause|unpause> [duration]");
        }
    }

    public boolean isChatPaused() { return paused; }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) return filterPrefix(List.of("pause", "unpause"), args[0]);
        return List.of();
    }
}
