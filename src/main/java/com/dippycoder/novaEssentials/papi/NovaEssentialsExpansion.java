package com.dippycoder.novaEssentials.papi;

import com.dippycoder.novaEssentials.NovaEssentials;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Provides %novaess_<placeholder>% placeholders via PlaceholderAPI.
 *
 * Static (per requesting player):
 *   %novaess_is_vanished%           — true/false
 *   %novaess_is_god%                — true/false
 *   %novaess_is_flying%             — true/false (plugin fly)
 *   %novaess_is_muted%              — true/false
 *   %novaess_home_count%            — number of homes
 *   %novaess_home_limit%            — max homes allowed (∞ if unlimited)
 *   %novaess_visible_players%       — count of players visible to requesting player
 *
 * Dynamic (target player by name):
 *   %novaess_is_afk_<player>%       — true/false
 *   %novaess_afk_message_<player>%  — AFK message or empty
 *   %novaess_blocked_players_<player>% — count of players blocked by <player>
 *   %novaess_tpa_requests_<player>% — true/false (has pending incoming request)
 *   %novaess_mute_status_<player>%  — muted / unmuted
 *   %novaess_god_mode_<player>%     — true/false
 *   %novaess_vanish_status_<player>% — true/false
 */
public class NovaEssentialsExpansion extends PlaceholderExpansion {

    private final NovaEssentials plugin;

    public NovaEssentialsExpansion(NovaEssentials plugin) {
        this.plugin = plugin;
    }

    @Override public @NotNull String getIdentifier() { return "novaess"; }
    @Override public @NotNull String getAuthor()     { return "DippyCoder"; }
    @Override public @NotNull String getVersion()    { return plugin.getDescription().getVersion(); }
    @Override public boolean persist()               { return true; }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "";

        String lp = params.toLowerCase();

        // ── Static placeholders ───────────────────────────────
        switch (lp) {
            case "is_vanished" -> { return String.valueOf(plugin.getVanishManager().isVanished(player)); }
            case "is_god"      -> { return String.valueOf(plugin.getGodManager().isGod(player)); }
            case "is_flying"   -> { return String.valueOf(plugin.getFlyManager().hasPluginFly(player)); }
            case "is_muted"    -> { return String.valueOf(plugin.getMuteManager().isMuted(player.getUniqueId())); }
            case "home_count"  -> { return String.valueOf(plugin.getHomeManager().getHomeCount(player)); }
            case "home_limit"  -> {
                int limit = plugin.getConfigManager().getHomeLimit(player);
                return limit == -1 ? "∞" : String.valueOf(limit);
            }
            case "visible_players" -> {
                boolean canSeeVanished = plugin.getVanishManager().canSee(player);
                long count = plugin.getServer().getOnlinePlayers().stream()
                        .filter(p -> !plugin.getVanishManager().isVanished(p) || canSeeVanished)
                        .count();
                return String.valueOf(count);
            }
        }

        // ── Dynamic placeholders (player-name suffix) ─────────
        if (lp.startsWith("is_afk_")) {
            Player target = plugin.getServer().getPlayerExact(params.substring(7));
            return target == null ? "false" : String.valueOf(plugin.getAfkManager().isAfk(target));
        }

        if (lp.startsWith("afk_message_")) {
            Player target = plugin.getServer().getPlayerExact(params.substring(12));
            if (target == null) return "";
            String msg = plugin.getAfkManager().getAfkMessage(target);
            return msg != null ? msg : "";
        }

        if (lp.startsWith("blocked_players_")) {
            Player target = plugin.getServer().getPlayerExact(params.substring(16));
            if (target == null) return "0";
            return String.valueOf(plugin.getBlockManager().getBlockedCount(target.getUniqueId()));
        }

        if (lp.startsWith("tpa_requests_")) {
            Player target = plugin.getServer().getPlayerExact(params.substring(13));
            return target == null ? "false"
                    : String.valueOf(plugin.getTpaManager().hasPendingRequest(target));
        }

        if (lp.startsWith("mute_status_")) {
            Player target = plugin.getServer().getPlayerExact(params.substring(12));
            if (target == null) return "unmuted";
            return plugin.getMuteManager().isMuted(target.getUniqueId()) ? "muted" : "unmuted";
        }

        if (lp.startsWith("god_mode_")) {
            Player target = plugin.getServer().getPlayerExact(params.substring(9));
            return target == null ? "false" : String.valueOf(plugin.getGodManager().isGod(target));
        }

        if (lp.startsWith("vanish_status_")) {
            Player target = plugin.getServer().getPlayerExact(params.substring(14));
            return target == null ? "false"
                    : String.valueOf(plugin.getVanishManager().isVanished(target));
        }

        // ── Playtime placeholders ─────────────────────────────
        if (lp.startsWith("playtime")) {
            if (!plugin.getConfigManager().isPlaytimeEnabled()) return "";
            long ms = plugin.getPlaytimeManager().getPlaytime(player.getUniqueId());
            return switch (lp) {
                case "playtime"         -> plugin.getPlaytimeManager().format(ms);
                case "playtime_seconds" -> String.valueOf(ms / 1000);
                case "playtime_minutes" -> String.valueOf(ms / 60000);
                case "playtime_hours"   -> String.valueOf(ms / 3600000);
                case "playtime_days"    -> String.valueOf(ms / 86400000);
                default                 -> null;
            };
        }

        return null;
    }
}
