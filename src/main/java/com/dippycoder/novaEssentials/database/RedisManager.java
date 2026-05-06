package com.dippycoder.novaEssentials.database;

import com.dippycoder.novaEssentials.NovaEssentials;
import com.dippycoder.novaEssentials.manager.PlayerSettingsManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Location;
import org.bukkit.World;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.*;
import java.util.logging.Level;

public class RedisManager {

    private static final Gson GSON = new Gson();
    private static final String KEY_WARPS  = "novaess:warps";
    private static final String KEY_SPAWN  = "novaess:spawn";
    private static final String KEY_FROZEN = "novaess:frozen";

    private final NovaEssentials plugin;
    private JedisPool pool;

    public RedisManager(NovaEssentials plugin) {
        this.plugin = plugin;
    }

    // ── Lifecycle ─────────────────────────────────────────────

    public void connect() {
        var cfg = plugin.getConfigManager();
        JedisPoolConfig pcfg = new JedisPoolConfig();
        pcfg.setMaxTotal(8);
        pcfg.setMaxIdle(4);
        pcfg.setMinIdle(1);

        String host = cfg.getRedisHost();
        int port = cfg.getRedisPort();
        String password = cfg.getRedisPassword();
        int db = cfg.getRedisDatabase();

        pool = (password != null && !password.isBlank())
                ? new JedisPool(pcfg, host, port, 2000, password, db)
                : new JedisPool(pcfg, host, port, 2000, null, db);

        try (var jedis = pool.getResource()) {
            jedis.ping();
            plugin.getLogger().info("Connected to Redis at " + host + ":" + port + "/" + db);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to connect to Redis: " + e.getMessage());
            pool.close();
            pool = null;
        }
    }

    public void disconnect() {
        if (pool != null && !pool.isClosed()) pool.close();
    }

    public boolean isConnected() {
        return pool != null && !pool.isClosed();
    }

    // ── Location helpers ──────────────────────────────────────

    private static String locToStr(Location loc) {
        return loc.getWorld().getName() + ","
                + loc.getX() + "," + loc.getY() + "," + loc.getZ() + ","
                + loc.getYaw() + "," + loc.getPitch();
    }

    private Location strToLoc(String s) {
        String[] p = s.split(",", 6);
        if (p.length < 6) return null;
        World world = plugin.getServer().getWorld(p[0]);
        if (world == null) return null;
        return new Location(world,
                Double.parseDouble(p[1]), Double.parseDouble(p[2]), Double.parseDouble(p[3]),
                Float.parseFloat(p[4]), Float.parseFloat(p[5]));
    }

    // ── Low-level helpers ─────────────────────────────────────

    private void set(String key, String value) {
        if (pool == null) return;
        try (var jedis = pool.getResource()) {
            jedis.set(key, value);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Redis SET failed for " + key, e);
        }
    }

    private String get(String key) {
        if (pool == null) return null;
        try (var jedis = pool.getResource()) {
            return jedis.get(key);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Redis GET failed for " + key, e);
            return null;
        }
    }

    private void del(String key) {
        if (pool == null) return;
        try (var jedis = pool.getResource()) {
            jedis.del(key);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Redis DEL failed for " + key, e);
        }
    }

    private void sadd(String key, String member) {
        if (pool == null) return;
        try (var jedis = pool.getResource()) {
            jedis.sadd(key, member);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Redis SADD failed for " + key, e);
        }
    }

    private void srem(String key, String member) {
        if (pool == null) return;
        try (var jedis = pool.getResource()) {
            jedis.srem(key, member);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Redis SREM failed for " + key, e);
        }
    }

    private Set<String> smembers(String key) {
        if (pool == null) return Set.of();
        try (var jedis = pool.getResource()) {
            return jedis.smembers(key);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Redis SMEMBERS failed for " + key, e);
            return Set.of();
        }
    }

    // ── Homes ─────────────────────────────────────────────────

    public Map<String, Location> loadHomes(UUID uuid) {
        String raw = get("novaess:homes:" + uuid);
        if (raw == null) return new LinkedHashMap<>();
        Map<String, String> data = GSON.fromJson(raw, new TypeToken<Map<String, String>>(){}.getType());
        Map<String, Location> homes = new LinkedHashMap<>();
        data.forEach((name, locStr) -> {
            Location loc = strToLoc(locStr);
            if (loc != null) homes.put(name, loc);
        });
        return homes;
    }

    public void saveHome(UUID uuid, String name, Location loc) {
        String key = "novaess:homes:" + uuid;
        String raw = get(key);
        Map<String, String> data = raw != null
                ? GSON.fromJson(raw, new TypeToken<Map<String, String>>(){}.getType())
                : new LinkedHashMap<>();
        data.put(name, locToStr(loc));
        set(key, GSON.toJson(data));
    }

    public void deleteHome(UUID uuid, String name) {
        String key = "novaess:homes:" + uuid;
        String raw = get(key);
        if (raw == null) return;
        Map<String, String> data = GSON.fromJson(raw, new TypeToken<Map<String, String>>(){}.getType());
        data.remove(name);
        set(key, GSON.toJson(data));
    }

    // ── Warps ─────────────────────────────────────────────────

    public Map<String, Location> loadWarps() {
        String raw = get(KEY_WARPS);
        if (raw == null) return new LinkedHashMap<>();
        Map<String, String> data = GSON.fromJson(raw, new TypeToken<Map<String, String>>(){}.getType());
        Map<String, Location> warps = new LinkedHashMap<>();
        data.forEach((name, locStr) -> {
            Location loc = strToLoc(locStr);
            if (loc != null) warps.put(name, loc);
        });
        return warps;
    }

    public void saveWarp(String name, Location loc) {
        String raw = get(KEY_WARPS);
        Map<String, String> data = raw != null
                ? GSON.fromJson(raw, new TypeToken<Map<String, String>>(){}.getType())
                : new LinkedHashMap<>();
        data.put(name, locToStr(loc));
        set(KEY_WARPS, GSON.toJson(data));
    }

    public void deleteWarp(String name) {
        String raw = get(KEY_WARPS);
        if (raw == null) return;
        Map<String, String> data = GSON.fromJson(raw, new TypeToken<Map<String, String>>(){}.getType());
        data.remove(name);
        set(KEY_WARPS, GSON.toJson(data));
    }

    // ── Blocks ────────────────────────────────────────────────

    public Set<UUID> loadBlockedPlayers(UUID uuid) {
        Set<UUID> blocked = new HashSet<>();
        smembers("novaess:blocks:" + uuid).forEach(s -> blocked.add(UUID.fromString(s)));
        return blocked;
    }

    public void addBlock(UUID uuid, UUID blocked) {
        sadd("novaess:blocks:" + uuid, blocked.toString());
    }

    public void removeBlock(UUID uuid, UUID blocked) {
        srem("novaess:blocks:" + uuid, blocked.toString());
    }

    // ── Mutes ─────────────────────────────────────────────────

    public DatabaseManager.MuteInfo loadMute(UUID uuid) {
        String raw = get("novaess:mutes:" + uuid);
        return raw != null ? GSON.fromJson(raw, DatabaseManager.MuteInfo.class) : null;
    }

    public void saveMute(UUID uuid, String mutedBy, String reason, long until) {
        set("novaess:mutes:" + uuid,
                GSON.toJson(new DatabaseManager.MuteInfo(mutedBy, reason, until)));
    }

    public void deleteMute(UUID uuid) {
        del("novaess:mutes:" + uuid);
    }

    // ── Kit Cooldowns ─────────────────────────────────────────

    public Map<String, Long> loadKitCooldowns(UUID uuid) {
        String raw = get("novaess:kit_cooldowns:" + uuid);
        if (raw == null) return new HashMap<>();
        return GSON.fromJson(raw, new TypeToken<Map<String, Long>>(){}.getType());
    }

    public void saveKitCooldown(UUID uuid, String kit, long lastUse) {
        String key = "novaess:kit_cooldowns:" + uuid;
        String raw = get(key);
        Map<String, Long> data = raw != null
                ? GSON.fromJson(raw, new TypeToken<Map<String, Long>>(){}.getType())
                : new HashMap<>();
        data.put(kit, lastUse);
        set(key, GSON.toJson(data));
    }

    // ── Spawn ─────────────────────────────────────────────────

    public Location loadSpawn() {
        String raw = get(KEY_SPAWN);
        return raw != null ? strToLoc(raw) : null;
    }

    public void saveSpawn(Location loc) {
        set(KEY_SPAWN, locToStr(loc));
    }

    // ── Frozen ────────────────────────────────────────────────

    public Set<UUID> loadAllFrozen() {
        Set<UUID> uuids = new HashSet<>();
        smembers(KEY_FROZEN).forEach(s -> uuids.add(UUID.fromString(s)));
        return uuids;
    }

    public void addFrozen(UUID uuid) {
        sadd(KEY_FROZEN, uuid.toString());
    }

    public void removeFrozen(UUID uuid) {
        srem(KEY_FROZEN, uuid.toString());
    }

    // ── Player Settings ───────────────────────────────────────

    public PlayerSettingsManager.PlayerSettings loadPlayerSettings(UUID uuid) {
        String raw = get("novaess:settings:" + uuid);
        return raw != null ? GSON.fromJson(raw, PlayerSettingsManager.PlayerSettings.class) : null;
    }

    public void savePlayerSettings(UUID uuid, PlayerSettingsManager.PlayerSettings s) {
        set("novaess:settings:" + uuid, GSON.toJson(s));
    }
}
