package com.dippycoder.novaEssentials.manager;

import com.dippycoder.novaEssentials.NovaEssentials;
import net.kyori.adventure.text.Component;
import org.bukkit.BanList;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class CaptchaManager {

    /**
     * Holds captcha state for an active challenge.
     *
     * @param code           the expected answer (uppercase alphanumeric)
     * @param triesRemaining how many wrong attempts remain before ban
     * @param mapId          the Bukkit MapView ID so we can remove the item later
     * @param failedCodes    list of incorrect answers submitted so far
     */
    public record CaptchaData(String code, int triesRemaining, int mapId, List<String> failedCodes) {
        public CaptchaData withTries(int tries, String failedInput) {
            var updated = new ArrayList<>(failedCodes);
            updated.add(failedInput);
            return new CaptchaData(code, tries, mapId, List.copyOf(updated));
        }
    }

    // Characters that are easy to read (no 0/O, 1/I/L)
    private static final String CHARSET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 5;
    private static final int MAX_TRIES = 3;

    private final NovaEssentials plugin;
    private final Map<UUID, CaptchaData> activeCaptchas = new HashMap<>();
    private final Random random = new Random();

    public CaptchaManager(NovaEssentials plugin) {
        this.plugin = plugin;
    }

    // ── Public API ────────────────────────────────────────────

    public boolean hasActiveCaptcha(Player player) {
        return activeCaptchas.containsKey(player.getUniqueId());
    }

    public CaptchaData getActiveCaptcha(Player player) {
        return activeCaptchas.get(player.getUniqueId());
    }

    public void giveCaptcha(Player player) {
        giveCaptcha(player, "NovaEssentials");
    }

    /**
     * Generates a new captcha, gives the player a map item, and freezes them.
     */
    public void giveCaptcha(Player player, String issuedBy) {
        // Clear any existing captcha first without penalty
        clearCaptcha(player);

        String code = generateCode();

        // Create a new MapView and register our renderer
        MapView view = plugin.getServer().createMap(player.getWorld());
        view.setScale(MapView.Scale.NORMAL);
        view.setTrackingPosition(false);

        // Remove default renderers
        for (MapRenderer r : view.getRenderers()) {
            view.removeRenderer(r);
        }
        view.addRenderer(new CaptchaRenderer(code));

        // Build the map item
        ItemStack mapItem = new ItemStack(Material.FILLED_MAP);
        MapMeta meta = (MapMeta) mapItem.getItemMeta();
        meta.setMapView(view);
        meta.displayName(Component.text("Captcha Verification"));
        mapItem.setItemMeta(meta);

        // Store captcha data
        int mapId = view.getId();
        activeCaptchas.put(player.getUniqueId(), new CaptchaData(code, MAX_TRIES, mapId, List.of()));

        // Freeze and give item
        plugin.getFreezeManager().freeze(player);
        player.getInventory().addItem(mapItem);

        plugin.getMessageManager().send(player, "captcha.given");
        plugin.getDiscordWebhookManager().onCaptchaCreated(player.getName(), issuedBy);
    }

    /**
     * Verifies the player's input against their active captcha.
     * Must be called on the main thread.
     */
    public void verify(Player player, String input) {
        CaptchaData data = activeCaptchas.get(player.getUniqueId());
        if (data == null) return;

        if (data.code().equalsIgnoreCase(input.trim())) {
            // Correct!
            removeCaptchaItem(player);
            activeCaptchas.remove(player.getUniqueId());
            plugin.getFreezeManager().unfreeze(player);
            plugin.getMessageManager().send(player, "captcha.solved");
        } else {
            int remaining = data.triesRemaining() - 1;
            CaptchaData updated = data.withTries(remaining, input.trim());
            if (remaining <= 0) {
                // Ban the player
                removeCaptchaItem(player);
                activeCaptchas.remove(player.getUniqueId());
                plugin.getFreezeManager().unfreeze(player);

                @SuppressWarnings({"unchecked", "rawtypes"})
                BanList banList = (BanList) plugin.getServer().getBanList(BanList.Type.NAME);
                banList.addBan(player.getName(), "Failed captcha verification", (java.util.Date) null, "NovaEssentials");

                Component banScreen = plugin.getMessageManager().get(player, "captcha.ban-screen");
                player.kick(banScreen);

                // Send full summary to staff: actual code + all failed attempts
                String attemptsStr = String.join(", ", updated.failedCodes());
                Component summary = plugin.getMessageManager().get(
                        plugin.getServer().getConsoleSender(), "captcha.fail-summary",
                        "player", player.getName(),
                        "code", data.code(),
                        "attempts", attemptsStr);
                for (Player online : plugin.getServer().getOnlinePlayers()) {
                    if (online.hasPermission(plugin.getConfigManager().getPermission("cmd.freeze"))) {
                        online.sendMessage(summary);
                    }
                }
                plugin.getServer().getConsoleSender().sendMessage(summary);
                plugin.getDiscordWebhookManager().onCaptchaFailed(
                        player.getName(), data.code(), updated.failedCodes());
            } else {
                // Wrong but still has tries
                activeCaptchas.put(player.getUniqueId(), updated);
                plugin.getMessageManager().send(player, "captcha.wrong", "tries", remaining);
            }
        }
    }

    /**
     * Clears a captcha without any penalty (also unfreezes if frozen by captcha).
     */
    public void clearCaptcha(Player player) {
        if (!activeCaptchas.containsKey(player.getUniqueId())) return;
        removeCaptchaItem(player);
        activeCaptchas.remove(player.getUniqueId());
        plugin.getFreezeManager().unfreeze(player);
    }

    /**
     * Replaces the current captcha with a new code without resetting the tries counter.
     * Used by /recaptcha.
     */
    public void reissueCaptcha(Player player) {
        CaptchaData old = activeCaptchas.get(player.getUniqueId());
        if (old == null) return;

        removeCaptchaItem(player);

        String newCode = generateCode();
        MapView view = plugin.getServer().createMap(player.getWorld());
        view.setScale(MapView.Scale.NORMAL);
        view.setTrackingPosition(false);
        for (MapRenderer r : view.getRenderers()) view.removeRenderer(r);
        view.addRenderer(new CaptchaRenderer(newCode));

        ItemStack mapItem = new ItemStack(Material.FILLED_MAP);
        MapMeta meta = (MapMeta) mapItem.getItemMeta();
        meta.setMapView(view);
        meta.displayName(Component.text("Captcha Verification"));
        mapItem.setItemMeta(meta);

        // Keep the existing tries and failed codes; only update code and mapId
        activeCaptchas.put(player.getUniqueId(),
                new CaptchaData(newCode, old.triesRemaining(), view.getId(), old.failedCodes()));
        player.getInventory().addItem(mapItem);
    }

    /**
     * Removes the captcha FILLED_MAP item from the player's inventory.
     */
    public void removeCaptchaItem(Player player) {
        CaptchaData data = activeCaptchas.get(player.getUniqueId());
        if (data == null) return;

        int targetMapId = data.mapId();
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item == null || item.getType() != Material.FILLED_MAP) continue;
            if (!(item.getItemMeta() instanceof MapMeta mapMeta)) continue;
            MapView view = mapMeta.getMapView();
            if (view != null && view.getId() == targetMapId) {
                player.getInventory().setItem(i, null);
                return;
            }
        }
    }

    // ── Internals ─────────────────────────────────────────────

    private String generateCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CHARSET.charAt(random.nextInt(CHARSET.length())));
        }
        return sb.toString();
    }

    // ── Inner renderer ────────────────────────────────────────

    private static class CaptchaRenderer extends MapRenderer {

        private static final int SCALE = 3;

        // Vibrant per-letter colors
        private static final Color[] LETTER_COLORS = {
            new Color(255, 90,  90),  // red
            new Color(255, 200, 70),  // yellow
            new Color(80,  255, 120), // green
            new Color(80,  210, 255), // cyan
            new Color(190, 100, 255), // purple
            new Color(255, 150, 70),  // orange
            new Color(255, 90,  210), // pink
        };

        // Design presets: {bg, noise, diag, wave}
        private static final int[][] DESIGNS = {
            {0x14_14_2D, 0x32_32_4E, 0x3C_3C_5F, 0x50_50_78},  // dark blue
            {0x2D_0A_0A, 0x4E_20_20, 0x5F_28_28, 0x78_3C_3C},  // dark red
            {0x0A_1A_0A, 0x20_3A_20, 0x28_4A_28, 0x38_60_38},  // dark green
        };

        private final String code;
        private boolean rendered = false;

        CaptchaRenderer(String code) {
            this.code = code;
        }

        @Override
        public void render(MapView view, MapCanvas canvas, Player player) {
            if (rendered) return;
            rendered = true;

            int design = Math.abs(code.hashCode()) % DESIGNS.length;
            int[] d = DESIGNS[design];

            byte bgColor     = MapPalette.matchColor(rgb(d[0]));
            byte noiseColor  = MapPalette.matchColor(rgb(d[1]));
            byte diagColor   = MapPalette.matchColor(rgb(d[2]));
            byte waveColor   = MapPalette.matchColor(rgb(d[3]));
            byte shadowColor = MapPalette.matchColor(new Color(5, 5, 12));

            // Background
            for (int x = 0; x < 128; x++)
                for (int y = 0; y < 128; y++)
                    canvas.setPixel(x, y, bgColor);

            Random rng = new Random(code.hashCode());

            // Dense noise dots
            for (int i = 0; i < 700; i++)
                canvas.setPixel(rng.nextInt(128), rng.nextInt(128), noiseColor);

            // Diagonal crossing lines (before text)
            for (int i = 0; i < 5; i++) {
                drawLine(canvas, rng.nextInt(64), rng.nextInt(128),
                        64 + rng.nextInt(64), rng.nextInt(128), diagColor);
            }

            // Per-char layout
            int charCellW = SCALE * 7; // extra spacing between letters
            int totalW = code.length() * charCellW - SCALE;
            int startX = (128 - totalW) / 2;
            int baseY = 46;

            // Precompute per-char angles using a dedicated RNG so both passes match
            Random angleRng = new Random(code.hashCode() ^ 0x1A2B3C4DL);
            double[] angles = new double[code.length()];
            for (int ci = 0; ci < angles.length; ci++)
                angles[ci] = (angleRng.nextDouble() - 0.5) * 0.45; // ±~13°

            // Shadow pass (all chars in dark, +2/+2 offset)
            for (int ci = 0; ci < code.length(); ci++) {
                char c = code.charAt(ci);
                org.bukkit.map.MapFont.CharacterSprite sprite =
                        org.bukkit.map.MinecraftFont.Font.getChar(c);
                if (sprite == null) continue;
                int waveY = (int) (Math.sin(ci * 1.5 + 0.3) * 10);
                drawCharRotated(canvas, sprite, startX + ci * charCellW + 2, baseY + waveY + 2,
                        shadowColor, angles[ci]);
            }

            // Main text pass — each letter in its own color, slightly rotated
            for (int ci = 0; ci < code.length(); ci++) {
                char c = code.charAt(ci);
                org.bukkit.map.MapFont.CharacterSprite sprite =
                        org.bukkit.map.MinecraftFont.Font.getChar(c);
                if (sprite == null) continue;
                int waveY = (int) (Math.sin(ci * 1.5 + 0.3) * 10);
                Color letterColor = LETTER_COLORS[Math.abs(c + ci) % LETTER_COLORS.length];
                byte color = MapPalette.matchColor(letterColor);
                drawCharRotated(canvas, sprite, startX + ci * charCellW, baseY + waveY,
                        color, angles[ci]);
            }

            // Wavy lines after text
            rng = new Random(code.hashCode() ^ 0xDEAD_BEEF);
            for (int li = 0; li < 4; li++) {
                double freq  = 0.10 + li * 0.05;
                double phase = rng.nextDouble() * Math.PI * 2;
                int centerY  = 38 + li * 16 + rng.nextInt(6);
                for (int x = 0; x < 128; x++) {
                    int y = centerY + (int) (Math.sin(x * freq + phase) * 7);
                    for (int dy2 = -1; dy2 <= 1; dy2++) {
                        int py = y + dy2;
                        if (py >= 0 && py < 128)
                            canvas.setPixel(x, py, waveColor);
                    }
                }
            }

            // Scatter extra noise on top
            rng = new Random(code.hashCode() ^ 0xCAFE);
            for (int i = 0; i < 200; i++)
                canvas.setPixel(rng.nextInt(128), rng.nextInt(128), noiseColor);
        }

        private static Color rgb(int packed) {
            return new Color((packed >> 16) & 0xFF, (packed >> 8) & 0xFF, packed & 0xFF);
        }

        private static void drawCharRotated(MapCanvas canvas,
                                            org.bukkit.map.MapFont.CharacterSprite sprite,
                                            int originX, int originY, byte color, double angle) {
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            double cx = sprite.getWidth()  * SCALE / 2.0;
            double cy = sprite.getHeight() * SCALE / 2.0;

            for (int row = 0; row < sprite.getHeight(); row++) {
                for (int col = 0; col < sprite.getWidth(); col++) {
                    if (!sprite.get(row, col)) continue;
                    for (int sy = 0; sy < SCALE; sy++) {
                        for (int sx = 0; sx < SCALE; sx++) {
                            double x = col * SCALE + sx - cx;
                            double y = row * SCALE + sy - cy;
                            int px = (int) Math.round(cos * x - sin * y + cx) + originX;
                            int py = (int) Math.round(sin * x + cos * y + cy) + originY;
                            if (px >= 0 && px < 128 && py >= 0 && py < 128)
                                canvas.setPixel(px, py, color);
                        }
                    }
                }
            }
        }

        private static void drawLine(MapCanvas canvas,
                                     int x1, int y1, int x2, int y2, byte color) {
            int dx = Math.abs(x2 - x1), sx = x1 < x2 ? 1 : -1;
            int dy = -Math.abs(y2 - y1), sy = y1 < y2 ? 1 : -1;
            int err = dx + dy;
            int x = x1, y = y1;
            for (int safety = 0; safety < 512; safety++) {
                for (int tx = -1; tx <= 1; tx++)
                    for (int ty = -1; ty <= 1; ty++) {
                        int px = x + tx, py = y + ty;
                        if (px >= 0 && px < 128 && py >= 0 && py < 128)
                            canvas.setPixel(px, py, color);
                    }
                if (x == x2 && y == y2) break;
                int e2 = 2 * err;
                if (e2 >= dy) { err += dy; x += sx; }
                if (e2 <= dx) { err += dx; y += sy; }
            }
        }
    }
}
