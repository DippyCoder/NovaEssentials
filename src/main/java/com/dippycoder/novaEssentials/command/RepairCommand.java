package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class RepairCommand extends BaseCommand {

    public RepairCommand(NovaEssentials plugin) {
        super(plugin, "repair");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.repair")) return;

        if (args.length == 0) {
            if (!requirePlayer(sender)) return;
            Player player = (Player) sender;
            if (!repairHeld(player)) {
                msg.send(sender, "repair.no-item");
                return;
            }
            msg.send(sender, "repair.repaired");
            return;
        }

        if (args[0].equalsIgnoreCase("all")) {
            if (args.length == 1) {
                if (!requirePlayer(sender)) return;
                repairAll((Player) sender);
                msg.send(sender, "repair.all");
            } else {
                if (!requirePermission(sender, "cmd.repair.other")) return;
                Player target = findPlayer(sender, args[1]);
                if (target == null) return;
                repairAll(target);
                msg.send(sender, "repair.all-other", "player", target.getName());
                if (!target.equals(sender)) msg.send(target, "repair.notify-all", "sender", sender.getName());
            }
            return;
        }

        if (!requirePermission(sender, "cmd.repair.other")) return;
        Player target = findPlayer(sender, args[0]);
        if (target == null) return;
        if (!repairHeld(target)) {
            msg.send(sender, "repair.no-item");
            return;
        }
        msg.send(sender, "repair.repaired-other", "player", target.getName());
        if (!target.equals(sender)) msg.send(target, "repair.notify", "sender", sender.getName());
    }

    private boolean repairHeld(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) return false;
        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof Damageable damageable)) return false;
        damageable.setDamage(0);
        item.setItemMeta(meta);
        return true;
    }

    private void repairAll(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType().isAir()) continue;
            ItemMeta meta = item.getItemMeta();
            if (!(meta instanceof Damageable damageable)) continue;
            damageable.setDamage(0);
            item.setItemMeta(meta);
        }
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> opts = new ArrayList<>();
            opts.add("all");
            if (sender.hasPermission(plugin.getConfigManager().getPermission("cmd.repair.other"))) {
                opts.addAll(onlinePlayerNames(sender));
            }
            return filterPrefix(opts, args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("all")
                && sender.hasPermission(plugin.getConfigManager().getPermission("cmd.repair.other"))) {
            return filterPrefix(onlinePlayerNames(sender), args[1]);
        }
        return List.of();
    }
}
