package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class FNukeCommand extends BaseCommand {

    private static final int TNT_COUNT = 50;
    private static final double SPREAD_RADIUS = 12;
    private static final double HEIGHT_MIN = 20;
    private static final double HEIGHT_RANGE = 15;

    private static final Set<UUID> fnukeTnts =
            Collections.synchronizedSet(new HashSet<>());

    public FNukeCommand(NovaEssentials plugin) {
        super(plugin, "fnuke");
    }

    public static boolean isFnukeTnt(UUID uuid) {
        return fnukeTnts.contains(uuid);
    }

    public static void removeFnukeTnt(UUID uuid) {
        fnukeTnts.remove(uuid);
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.fnuke")) return;
        if (!requirePlayer(sender)) return;

        Player player = (Player) sender;
        World world = player.getWorld();
        Location center = player.getLocation().clone().add(0, HEIGHT_MIN, 0);
        Random random = new Random();

        for (int i = 0; i < TNT_COUNT; i++) {
            double rx = (random.nextDouble() - 0.5) * 2 * SPREAD_RADIUS;
            double ry = random.nextDouble() * HEIGHT_RANGE;
            double rz = (random.nextDouble() - 0.5) * 2 * SPREAD_RADIUS;
            Entity tnt = world.spawnEntity(center.clone().add(rx, ry, rz), EntityType.TNT);
            fnukeTnts.add(tnt.getUniqueId());
        }

        msg.send(sender, "fnuke.launched");
    }
}
