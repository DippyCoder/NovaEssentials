package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class EffectCommand extends BaseCommand {

    public EffectCommand(NovaEssentials plugin) {
        super(plugin, "effect");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.effect")) return;

        if (args.length < 2) {
            msg.send(sender, "general.invalid-args",
                    "usage", "/" + label + " <player> <effect|clear> [duration] [amplifier]");
            return;
        }

        Player target = findPlayer(sender, args[0]);
        if (target == null) return;

        // /effect <player> clear
        if (args[1].equalsIgnoreCase("clear")) {
            target.getActivePotionEffects().forEach(e -> target.removePotionEffect(e.getType()));
            msg.send(sender, "effect.cleared", "player", target.getName());
            return;
        }

        if (args.length < 4) {
            msg.send(sender, "general.invalid-args",
                    "usage", "/" + label + " <player> <effect> <duration> <amplifier>");
            return;
        }

        // Resolve effect type
        PotionEffectType effectType = Registry.EFFECT.get(
                NamespacedKey.minecraft(args[1].toLowerCase().replace("-", "_").replace(" ", "_")));
        if (effectType == null) {
            msg.send(sender, "effect.invalid", "effect", args[1]);
            return;
        }

        int durationSeconds;
        try {
            durationSeconds = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            msg.send(sender, "general.invalid-number", "value", args[2]);
            return;
        }

        int amplifier;
        try {
            amplifier = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            msg.send(sender, "general.invalid-number", "value", args[3]);
            return;
        }

        // Duration <= 0 means clear that specific effect
        if (durationSeconds <= 0) {
            target.removePotionEffect(effectType);
            msg.send(sender, "effect.cleared", "player", target.getName());
            return;
        }

        int durationTicks = durationSeconds * 20;
        PotionEffect effect = new PotionEffect(effectType, durationTicks, amplifier);
        target.addPotionEffect(effect);

        boolean isSelf = sender instanceof Player p && p.equals(target);
        if (isSelf) {
            msg.send(sender, "effect.applied-self",
                    "effect", friendlyEffectName(effectType),
                    "level", amplifier,
                    "duration", durationSeconds);
        } else {
            msg.send(sender, "effect.applied",
                    "player", target.getName(),
                    "effect", friendlyEffectName(effectType),
                    "level", amplifier,
                    "duration", durationSeconds);
            msg.send(target, "effect.applied-self",
                    "effect", friendlyEffectName(effectType),
                    "level", amplifier,
                    "duration", durationSeconds);
        }
    }

    private String friendlyEffectName(PotionEffectType type) {
        String key = type.getKey().getKey();
        String[] parts = key.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return sb.toString();
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return filterPrefix(onlinePlayerNames(sender), args[0]);
        }
        if (args.length == 2) {
            List<String> effectNames = new ArrayList<>();
            effectNames.add("clear");
            Registry.EFFECT.forEach(e -> effectNames.add(e.getKey().getKey()));
            return filterPrefix(effectNames, args[1]);
        }
        if (args.length == 3) {
            return filterPrefix(List.of("5", "30", "60", "300", "9999999"), args[2]);
        }
        if (args.length == 4) {
            return filterPrefix(List.of("0", "1", "2", "3", "4"), args[3]);
        }
        return List.of();
    }
}
