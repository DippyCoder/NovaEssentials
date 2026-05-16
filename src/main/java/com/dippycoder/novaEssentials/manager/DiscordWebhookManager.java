package com.dippycoder.novaEssentials.manager;

import com.dippycoder.novaEssentials.NovaEssentials;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    private EmbedConfig embedBan;
    private EmbedConfig embedUnban;
    private EmbedConfig embedKick;
    private EmbedConfig embedCaptchaCreated;
    private EmbedConfig embedCaptchaFailed;
    private EmbedConfig embedCaptchaDodge;

    public DiscordWebhookManager(NovaEssentials plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        File file = new File(plugin.getDataFolder(), "webhooks.yml");
        if (!file.exists()) plugin.saveResource("webhooks.yml", false);

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        enabled             = cfg.getBoolean("enabled", false);
        webhookUrl          = cfg.getString("webhook-url", "");
        eventBan            = cfg.getBoolean("events.ban", true);
        eventUnban          = cfg.getBoolean("events.unban", true);
        eventKick           = cfg.getBoolean("events.kick", true);
        eventCaptchaCreated = cfg.getBoolean("events.captcha-created", true);
        eventCaptchaFailed  = cfg.getBoolean("events.captcha-failed", true);

        embedBan            = loadEmbedConfig(cfg, "ban");
        embedUnban          = loadEmbedConfig(cfg, "unban");
        embedKick           = loadEmbedConfig(cfg, "kick");
        embedCaptchaCreated = loadEmbedConfig(cfg, "captcha-created");
        embedCaptchaFailed  = loadEmbedConfig(cfg, "captcha-failed");
        embedCaptchaDodge   = loadEmbedConfig(cfg, "captcha-dodge");
    }

    // ── Public event methods ──────────────────────────────────

    public void onBan(String player, String reason, String duration, String bannedBy) {
        if (!enabled || !eventBan) return;
        sendEmbedFromConfig(embedBan, Map.of(
                "player", player, "reason", reason,
                "duration", duration, "banned_by", bannedBy));
    }

    public void onUnban(String player, String unbannedBy) {
        if (!enabled || !eventUnban) return;
        sendEmbedFromConfig(embedUnban, Map.of(
                "player", player, "unbanned_by", unbannedBy));
    }

    public void onKick(String player, String reason, String kickedBy) {
        if (!enabled || !eventKick) return;
        sendEmbedFromConfig(embedKick, Map.of(
                "player", player, "reason", reason, "kicked_by", kickedBy));
    }

    public void onCaptchaCreated(String player, String issuedBy) {
        if (!enabled || !eventCaptchaCreated) return;
        sendEmbedFromConfig(embedCaptchaCreated, Map.of(
                "player", player, "issued_by", issuedBy));
    }

    public void onCaptchaFailed(String player, String actualCode, List<String> failedAttempts) {
        if (!enabled || !eventCaptchaFailed) return;
        String attempts = failedAttempts.isEmpty() ? "none" : String.join(", ", failedAttempts);
        sendEmbedFromConfig(embedCaptchaFailed, Map.of(
                "player", player, "actual_code", actualCode, "failed_attempts", attempts));
    }

    public void onCaptchaDodge(String player, String actualCode) {
        if (!enabled || !eventCaptchaFailed) return;
        sendEmbedFromConfig(embedCaptchaDodge, Map.of(
                "player", player, "actual_code", actualCode));
    }

    // ── Config loading ────────────────────────────────────────

    private record FieldTemplate(String name, String value, boolean inline) {}
    private record EmbedConfig(String title, int color, List<FieldTemplate> fields) {}

    private EmbedConfig loadEmbedConfig(YamlConfiguration cfg, String key) {
        String path   = "embeds." + key;
        String title  = cfg.getString(path + ".title", key);
        int    color  = parseColor(cfg.getString(path + ".color", "FFFFFF"));

        List<FieldTemplate> fields = new ArrayList<>();
        List<?> raw = cfg.getList(path + ".fields", List.of());
        for (Object obj : raw) {
            if (!(obj instanceof Map<?, ?> raw2)) continue;
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) raw2;
            String name    = String.valueOf(map.getOrDefault("name",   ""));
            String value   = String.valueOf(map.getOrDefault("value",  ""));
            boolean inline = Boolean.parseBoolean(String.valueOf(map.getOrDefault("inline", "true")));
            fields.add(new FieldTemplate(name, value, inline));
        }
        return new EmbedConfig(title, color, fields);
    }

    private static int parseColor(String hex) {
        if (hex == null || hex.isBlank()) return 0xFFFFFF;
        try {
            return (int) Long.parseLong(hex.replace("#", ""), 16);
        } catch (NumberFormatException e) {
            return 0xFFFFFF;
        }
    }

    // ── Sending ───────────────────────────────────────────────

    private void sendEmbedFromConfig(EmbedConfig cfg, Map<String, String> vars) {
        if (webhookUrl == null || webhookUrl.isBlank()) return;

        String title = substitute(cfg.title(), vars);

        StringBuilder json = new StringBuilder();
        json.append("{\"embeds\":[{");
        json.append("\"title\":\"").append(escape(title)).append("\",");
        json.append("\"color\":").append(cfg.color()).append(",");
        json.append("\"timestamp\":\"").append(Instant.now()).append("\",");
        json.append("\"fields\":[");

        List<FieldTemplate> fields = cfg.fields();
        for (int i = 0; i < fields.size(); i++) {
            FieldTemplate f = fields.get(i);
            json.append("{\"name\":\"").append(escape(substitute(f.name(), vars))).append("\",");
            json.append("\"value\":\"").append(escape(substitute(f.value(), vars))).append("\",");
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

    private static String substitute(String template, Map<String, String> vars) {
        String result = template;
        for (Map.Entry<String, String> entry : vars.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }
}
