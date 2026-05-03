package com.dippycoder.novaEssentials.manager;

import com.dippycoder.novaEssentials.NovaEssentials;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.List;
import java.util.logging.Level;

public class DiscordWebhookManager {

    private final NovaEssentials plugin;
    private final HttpClient http = HttpClient.newHttpClient();

    private boolean enabled;
    private String webhookUrl;
    private boolean eventBan;
    private boolean eventUnban;
    private boolean eventKick;
    private boolean eventCaptchaCreated;
    private boolean eventCaptchaFailed;

    public DiscordWebhookManager(NovaEssentials plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        File file = new File(plugin.getDataFolder(), "webhooks.yml");
        if (!file.exists()) plugin.saveResource("webhooks.yml", false);

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        enabled          = cfg.getBoolean("enabled", false);
        webhookUrl       = cfg.getString("webhook-url", "");
        eventBan            = cfg.getBoolean("events.ban", true);
        eventUnban          = cfg.getBoolean("events.unban", true);
        eventKick           = cfg.getBoolean("events.kick", true);
        eventCaptchaCreated = cfg.getBoolean("events.captcha-created", true);
        eventCaptchaFailed  = cfg.getBoolean("events.captcha-failed", true);
    }

    // ── Public event methods ──────────────────────────────────

    public void onBan(String player, String reason, String duration, String bannedBy) {
        if (!enabled || !eventBan) return;
        sendEmbed("🔨 Player Banned", 0xFF4444,
                List.of(
                        field("Player",    player,    true),
                        field("Reason",    reason,    true),
                        field("Duration",  duration,  true),
                        field("Banned by", bannedBy,  true)
                ));
    }

    public void onUnban(String player, String unbannedBy) {
        if (!enabled || !eventUnban) return;
        sendEmbed("✅ Player Unbanned", 0x44FF88,
                List.of(
                        field("Player",      player,     true),
                        field("Unbanned by", unbannedBy, true)
                ));
    }

    public void onKick(String player, String reason, String kickedBy) {
        if (!enabled || !eventKick) return;
        sendEmbed("👢 Player Kicked", 0xFF9900,
                List.of(
                        field("Player",    player,   true),
                        field("Reason",    reason,   true),
                        field("Kicked by", kickedBy, true)
                ));
    }

    public void onCaptchaCreated(String player, String issuedBy) {
        if (!enabled || !eventCaptchaCreated) return;
        sendEmbed("🔒 Captcha Issued", 0x4499FF,
                List.of(
                        field("Player",    player,   true),
                        field("Issued by", issuedBy, true)
                ));
    }

    public void onCaptchaFailed(String player, String actualCode, List<String> failedAttempts) {
        if (!enabled || !eventCaptchaFailed) return;
        String attempts = failedAttempts.isEmpty() ? "none" : String.join(", ", failedAttempts);
        sendEmbed("🚫 Captcha Failed — Player Banned", 0xCC2222,
                List.of(
                        field("Player",          player,     false),
                        field("Actual code",     actualCode, true),
                        field("Failed attempts", attempts,   true)
                ));
    }

    public void onCaptchaDodge(String player, String actualCode) {
        if (!enabled || !eventCaptchaFailed) return;
        sendEmbed("🚪 Captcha Dodged — Player Banned", 0xAA0000,
                List.of(
                        field("Player",      player,     true),
                        field("Actual code", actualCode, true),
                        field("Reason",      "Disconnected to avoid captcha", false)
                ));
    }

    // ── Internals ─────────────────────────────────────────────

    private record Field(String name, String value, boolean inline) {}

    private Field field(String name, String value, boolean inline) {
        return new Field(name, value, inline);
    }

    private void sendEmbed(String title, int color, List<Field> fields) {
        if (webhookUrl == null || webhookUrl.isBlank()) return;

        StringBuilder json = new StringBuilder();
        json.append("{\"embeds\":[{");
        json.append("\"title\":\"").append(escape(title)).append("\",");
        json.append("\"color\":").append(color).append(",");
        json.append("\"timestamp\":\"").append(Instant.now()).append("\",");
        json.append("\"fields\":[");
        for (int i = 0; i < fields.size(); i++) {
            Field f = fields.get(i);
            json.append("{\"name\":\"").append(escape(f.name())).append("\",");
            json.append("\"value\":\"").append(escape(f.value())).append("\",");
            json.append("\"inline\":").append(f.inline()).append("}");
            if (i < fields.size() - 1) json.append(",");
        }
        json.append("]}]}");

        String body = json.toString();
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(webhookUrl))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();
                http.send(request, HttpResponse.BodyHandlers.discarding());
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Discord webhook failed: " + e.getMessage());
            }
        });
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }
}
