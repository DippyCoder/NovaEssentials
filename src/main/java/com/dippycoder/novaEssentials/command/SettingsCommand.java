package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import com.dippycoder.novaEssentials.gui.SettingsGui;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class SettingsCommand extends BaseCommand {

    public SettingsCommand(NovaEssentials plugin) {
        super(plugin, "settings");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.settings")) return;
        if (!requirePlayer(sender)) return;
        SettingsGui.open((Player) sender, plugin);
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
