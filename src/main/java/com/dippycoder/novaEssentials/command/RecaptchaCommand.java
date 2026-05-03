package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import com.dippycoder.novaEssentials.manager.CaptchaManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class RecaptchaCommand extends BaseCommand {

    public RecaptchaCommand(NovaEssentials plugin) {
        super(plugin, "recaptcha");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.recaptcha")) return;
        if (!requirePlayer(sender)) return;

        Player player = (Player) sender;
        CaptchaManager.CaptchaData data = plugin.getCaptchaManager().getActiveCaptcha(player);

        if (data == null) {
            msg.send(sender, "captcha.recaptcha-none");
            return;
        }

        plugin.getCaptchaManager().reissueCaptcha(player);
        msg.send(sender, "captcha.recaptcha-given", "tries", data.triesRemaining());
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
