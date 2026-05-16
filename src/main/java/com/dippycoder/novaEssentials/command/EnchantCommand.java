package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EnchantCommand extends BaseCommand {

    public EnchantCommand(NovaEssentials plugin) {
        super(plugin, "enchant");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.enchant")) return;
        if (!requirePlayer(sender)) return;
        if (args.length == 0) {
            msg.send(sender, "general.invalid-args", "usage", "/" + label + " <enchantment> [level]");
            return;
        }

        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            msg.send(sender, "enchant.no-item");
            return;
        }

        Enchantment ench = resolveEnchantment(args[0]);
        if (ench == null) {
            msg.send(sender, "enchant.invalid", "enchantment", args[0]);
            return;
        }

        int level;
        if (args.length >= 2) {
            try {
                level = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                msg.send(sender, "general.invalid-number", "value", args[1]);
                return;
            }
            if (level < 1) {
                msg.send(sender, "enchant.invalid-level", "max", ench.getMaxLevel());
                return;
            }
        } else {
            level = ench.getMaxLevel();
        }

        item.addUnsafeEnchantment(ench, level);
        msg.send(sender, "enchant.success",
                "enchantment", friendlyEnchName(ench),
                "level", level);
    }

    private Enchantment resolveEnchantment(String input) {
        // Try by key
        Enchantment ench = Enchantment.getByKey(NamespacedKey.minecraft(input.toLowerCase().replace(" ", "_")));
        if (ench != null) return ench;
        // Try by legacy name
        ench = Enchantment.getByName(input.toUpperCase());
        return ench;
    }

    private String friendlyEnchName(Enchantment ench) {
        String key = ench.getKey().getKey();
        return Arrays.stream(key.split("_"))
                .map(s -> Character.toUpperCase(s.charAt(0)) + s.substring(1))
                .collect(Collectors.joining(" "));
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return filterPrefix(
                    Arrays.stream(Enchantment.values())
                            .map(e -> e.getKey().getKey())
                            .collect(Collectors.toList()),
                    args[0]);
        }
        return List.of();
    }
}
