package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import com.dippycoder.novaEssentials.gui.KitGui;
import com.dippycoder.novaEssentials.manager.KitManager;
import com.dippycoder.novaEssentials.util.DurationUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class KitCommand extends BaseCommand {

    public KitCommand(NovaEssentials plugin) {
        super(plugin, "kit");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.kit")) return;
        if (!requirePlayer(sender)) return;

        Player player = (Player) sender;
        KitManager kits = plugin.getKitManager();

        if (args.length == 0) {
            if (plugin.getConfigManager().isKitGuiEnabled()) {
                KitGui.open(player, plugin, 0);
            } else {
                listKits(player, kits);
            }
            return;
        }

        String kitName = args[0];

        if (!kits.kitExists(kitName)) {
            msg.send(sender, "kit.not-found", "kit", kitName);
            return;
        }

        String kitPerm = kits.getKitPermission(kitName);
        if (!kitPerm.isEmpty() && !player.hasPermission(kitPerm)) {
            msg.send(sender, "kit.no-perm", "kit", kitName);
            return;
        }

        long remaining = kits.getRemainingCooldown(player, kitName);
        if (remaining == -2) {
            msg.send(sender, "kit.one-time", "kit", kitName);
            return;
        }
        if (remaining > 0) {
            msg.send(sender, "kit.cooldown",
                    "kit", kitName,
                    "time", DurationUtil.format(remaining));
            return;
        }

        kits.giveKit(player, kitName);
        kits.recordUse(player, kitName);
        msg.send(sender, "kit.given", "kit", kitName);
    }

    private void listKits(Player player, KitManager kits) {
        Set<String> names = kits.getKitNames().stream()
                .filter(n -> {
                    String p = kits.getKitPermission(n);
                    return p.isEmpty() || player.hasPermission(p);
                })
                .collect(Collectors.toSet());

        if (names.isEmpty()) {
            msg.send(player, "kit.no-kits");
        } else {
            msg.send(player, "kit.list", "kits", String.join(", ", names));
        }
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return filterPrefix(new ArrayList<>(plugin.getKitManager().getKitNames()), args[0]);
        }
        return List.of();
    }
}
