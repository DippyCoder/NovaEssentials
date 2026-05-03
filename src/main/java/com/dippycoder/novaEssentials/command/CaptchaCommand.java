package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CaptchaCommand extends BaseCommand {

    public CaptchaCommand(NovaEssentials plugin) {
        super(plugin, "captcha");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.captcha")) return;

        if (args.length == 0) {
            // Give captcha to self
            if (!requirePlayer(sender)) return;
            Player self = (Player) sender;

            if (plugin.getCaptchaManager().hasActiveCaptcha(self)) {
                msg.send(sender, "captcha.already", "player", self.getName());
                return;
            }
            plugin.getCaptchaManager().giveCaptcha(self);
        } else {
            // Give captcha to target
            if (!requirePermission(sender, "cmd.captcha.other")) return;

            Player target = findPlayer(sender, args[0]);
            if (target == null) return;

            if (plugin.getCaptchaManager().hasActiveCaptcha(target)) {
                msg.send(sender, "captcha.already", "player", target.getName());
                return;
            }

            plugin.getCaptchaManager().giveCaptcha(target, sender.getName());
            msg.send(sender, "captcha.issued", "player", target.getName());
        }
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1 && sender.hasPermission(
                plugin.getConfigManager().getPermission("cmd.captcha.other"))) {
            return filterPrefix(onlinePlayerNames(sender), args[0]);
        }
        return List.of();
    }
}
