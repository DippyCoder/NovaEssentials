package com.dippycoder.novaEssentials.database;

import com.dippycoder.novaEssentials.NovaEssentials;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;

import static java.sql.DriverManager.getConnection;

public class DatabaseManager {

    private final NovaEssentials plugin;

    private File dbFolder;

    private Connection homesConn;
    private Connection warpsConn;
    private Connection blocksConn;
    private Connection mutesConn;
    private Connection kitsConn;
    private Connection spawnConn;
    private Connection frozenConn;

    public DatabaseManager(NovaEssentials plugin) {
        this.plugin = plugin;
    }

    // ── Lifecycle ─────────────────────────────────────────────

    public void connect() {
        dbFolder = new File(plugin.getDataFolder(), "db");
        dbFolder.mkdirs();

        homesConn  = open("homes.db",  """
                CREATE TABLE IF NOT EXISTS homes (
                    uuid  TEXT NOT NULL,
                    name  TEXT NOT NULL,
                    world TEXT NOT NULL,
                    x     REAL NOT NULL,
                    y     REAL NOT NULL,
                    z     REAL NOT NULL,
                    yaw   REAL NOT NULL,
                    pitch REAL NOT NULL,
                    PRIMARY KEY (uuid, name)
                )""");

        warpsConn  = open("warps.db",  """
                CREATE TABLE IF NOT EXISTS warps (
                    name  TEXT NOT NULL PRIMARY KEY,
                    world TEXT NOT NULL,
                    x     REAL NOT NULL,
                    y     REAL NOT NULL,
                    z     REAL NOT NULL,
                    yaw   REAL NOT NULL,
                    pitch REAL NOT NULL
                )""");

        blocksConn = open("blocks.db", """
                CREATE TABLE IF NOT EXISTS blocks (
                    uuid         TEXT NOT NULL,
                    blocked_uuid TEXT NOT NULL,
                    PRIMARY KEY (uuid, blocked_uuid)
                )""");

        mutesConn  = open("mutes.db",  """
                CREATE TABLE IF NOT EXISTS mutes (
                    uuid     TEXT NOT NULL PRIMARY KEY,
                    muted_by TEXT NOT NULL,
                    reason   TEXT,
                    until    INTEGER NOT NULL
                )""");

        kitsConn   = open("kits.db",   """
                CREATE TABLE IF NOT EXISTS kit_cooldowns (
                    uuid     TEXT NOT NULL,
                    kit      TEXT NOT NULL,
                    last_use INTEGER NOT NULL,
                    PRIMARY KEY (uuid, kit)
                )""");

        spawnConn  = open("spawn.db",  """
                CREATE TABLE IF NOT EXISTS spawn (
                    id    INTEGER PRIMARY KEY DEFAULT 1 CHECK (id = 1),
                    world TEXT NOT NULL,
                    x     REAL NOT NULL,
                    y     REAL NOT NULL,
                    z     REAL NOT NULL,
                    yaw   REAL NOT NULL,
                    pitch REAL NOT NULL
                )""");

        frozenConn = open("frozen.db", """
                CREATE TABLE IF NOT EXISTS frozen (
                    uuid TEXT NOT NULL PRIMARY KEY
                )""");

        plugin.getLogger().info("Connected to SQLite databases.");
    }

    public void disconnect() {
        close(homesConn,  "homes.db");
        close(warpsConn,  "warps.db");
        close(blocksConn, "blocks.db");
        close(mutesConn,  "mutes.db");
        close(kitsConn,   "kits.db");
        close(spawnConn,  "spawn.db");
        close(frozenConn, "frozen.db");
    }

    private Connection open(String fileName, String createSql) {
        try {
            File file = new File(dbFolder, fileName);
            Connection conn = getConnection("jdbc:sqlite:" + file.getAbsolutePath());
            conn.createStatement().execute(createSql);
            return conn;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to open " + fileName + ": " + e.getMessage());
            return null;
        }
    }

    private void close(Connection conn, String name) {
        if (conn == null) return;
        try {
            if (!conn.isClosed()) conn.close();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Error closing " + name + ": " + e.getMessage());
        }
    }

    // ── Homes ────────────────────────────────────────────────

    public Map<String, Location> loadHomes(UUID uuid) {
        Map<String, Location> homes = new LinkedHashMap<>();
        if (homesConn == null) return homes;
        try (PreparedStatement ps = homesConn.prepareStatement(
                "SELECT name, world, x, y, z, yaw, pitch FROM homes WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                World world = plugin.getServer().getWorld(rs.getString("world"));
                if (world == null) continue;
                homes.put(rs.getString("name"), new Location(world,
                        rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"),
                        rs.getFloat("yaw"), rs.getFloat("pitch")));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load homes for " + uuid, e);
        }
        return homes;
    }

    public void saveHome(UUID uuid, String name, Location loc) {
        if (homesConn == null) return;
        try (PreparedStatement ps = homesConn.prepareStatement(
                "INSERT OR REPLACE INTO homes (uuid, name, world, x, y, z, yaw, pitch) VALUES (?,?,?,?,?,?,?,?)")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, name);
            ps.setString(3, loc.getWorld().getName());
            ps.setDouble(4, loc.getX());
            ps.setDouble(5, loc.getY());
            ps.setDouble(6, loc.getZ());
            ps.setFloat(7, loc.getYaw());
            ps.setFloat(8, loc.getPitch());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save home " + name + " for " + uuid, e);
        }
    }

    public void deleteHome(UUID uuid, String name) {
        if (homesConn == null) return;
        try (PreparedStatement ps = homesConn.prepareStatement(
                "DELETE FROM homes WHERE uuid = ? AND name = ?")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, name);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to delete home.", e);
        }
    }

    // ── Warps ────────────────────────────────────────────────

    public Map<String, Location> loadWarps() {
        Map<String, Location> warps = new LinkedHashMap<>();
        if (warpsConn == null) return warps;
        try (Statement stmt = warpsConn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name, world, x, y, z, yaw, pitch FROM warps")) {
            while (rs.next()) {
                World world = plugin.getServer().getWorld(rs.getString("world"));
                if (world == null) continue;
                warps.put(rs.getString("name"), new Location(world,
                        rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"),
                        rs.getFloat("yaw"), rs.getFloat("pitch")));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load warps.", e);
        }
        return warps;
    }

    public void saveWarp(String name, Location loc) {
        if (warpsConn == null) return;
        try (PreparedStatement ps = warpsConn.prepareStatement(
                "INSERT OR REPLACE INTO warps (name, world, x, y, z, yaw, pitch) VALUES (?,?,?,?,?,?,?)")) {
            ps.setString(1, name);
            ps.setString(2, loc.getWorld().getName());
            ps.setDouble(3, loc.getX());
            ps.setDouble(4, loc.getY());
            ps.setDouble(5, loc.getZ());
            ps.setFloat(6, loc.getYaw());
            ps.setFloat(7, loc.getPitch());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save warp " + name, e);
        }
    }

    public void deleteWarp(String name) {
        if (warpsConn == null) return;
        try (PreparedStatement ps = warpsConn.prepareStatement(
                "DELETE FROM warps WHERE name = ?")) {
            ps.setString(1, name);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to delete warp.", e);
        }
    }

    // ── Blocks ───────────────────────────────────────────────

    public Set<UUID> loadBlockedPlayers(UUID uuid) {
        Set<UUID> blocked = new HashSet<>();
        if (blocksConn == null) return blocked;
        try (PreparedStatement ps = blocksConn.prepareStatement(
                "SELECT blocked_uuid FROM blocks WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) blocked.add(UUID.fromString(rs.getString("blocked_uuid")));
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load blocked players for " + uuid, e);
        }
        return blocked;
    }

    public void addBlock(UUID uuid, UUID blocked) {
        if (blocksConn == null) return;
        try (PreparedStatement ps = blocksConn.prepareStatement(
                "INSERT OR IGNORE INTO blocks (uuid, blocked_uuid) VALUES (?, ?)")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, blocked.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to add block.", e);
        }
    }

    public void removeBlock(UUID uuid, UUID blocked) {
        if (blocksConn == null) return;
        try (PreparedStatement ps = blocksConn.prepareStatement(
                "DELETE FROM blocks WHERE uuid = ? AND blocked_uuid = ?")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, blocked.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to remove block.", e);
        }
    }

    // ── Mutes ────────────────────────────────────────────────

    public record MuteInfo(String mutedBy, String reason, long until) {
        public boolean isPermanent() { return until == -1; }
        public boolean isExpired()   { return !isPermanent() && System.currentTimeMillis() > until; }
    }

    public MuteInfo loadMute(UUID uuid) {
        if (mutesConn == null) return null;
        try (PreparedStatement ps = mutesConn.prepareStatement(
                "SELECT muted_by, reason, until FROM mutes WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return new MuteInfo(rs.getString("muted_by"), rs.getString("reason"), rs.getLong("until"));
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load mute for " + uuid, e);
        }
        return null;
    }

    public void saveMute(UUID uuid, String mutedBy, String reason, long until) {
        if (mutesConn == null) return;
        try (PreparedStatement ps = mutesConn.prepareStatement(
                "INSERT OR REPLACE INTO mutes (uuid, muted_by, reason, until) VALUES (?,?,?,?)")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, mutedBy);
            ps.setString(3, reason);
            ps.setLong(4, until);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save mute.", e);
        }
    }

    public void deleteMute(UUID uuid) {
        if (mutesConn == null) return;
        try (PreparedStatement ps = mutesConn.prepareStatement(
                "DELETE FROM mutes WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to delete mute.", e);
        }
    }

    // ── Kit Cooldowns ─────────────────────────────────────────

    public Map<String, Long> loadKitCooldowns(UUID uuid) {
        Map<String, Long> cooldowns = new HashMap<>();
        if (kitsConn == null) return cooldowns;
        try (PreparedStatement ps = kitsConn.prepareStatement(
                "SELECT kit, last_use FROM kit_cooldowns WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) cooldowns.put(rs.getString("kit"), rs.getLong("last_use"));
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load kit cooldowns for " + uuid, e);
        }
        return cooldowns;
    }

    public void saveKitCooldown(UUID uuid, String kit, long lastUse) {
        if (kitsConn == null) return;
        try (PreparedStatement ps = kitsConn.prepareStatement(
                "INSERT OR REPLACE INTO kit_cooldowns (uuid, kit, last_use) VALUES (?,?,?)")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, kit);
            ps.setLong(3, lastUse);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save kit cooldown.", e);
        }
    }

    // ── Spawn ─────────────────────────────────────────────────

    public Location loadSpawn() {
        if (spawnConn == null) return null;
        try (Statement stmt = spawnConn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT world, x, y, z, yaw, pitch FROM spawn WHERE id = 1")) {
            if (rs.next()) {
                World world = plugin.getServer().getWorld(rs.getString("world"));
                if (world == null) return null;
                return new Location(world,
                        rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"),
                        rs.getFloat("yaw"), rs.getFloat("pitch"));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load spawn.", e);
        }
        return null;
    }

    public void saveSpawn(Location loc) {
        if (spawnConn == null) return;
        try (PreparedStatement ps = spawnConn.prepareStatement(
                "INSERT OR REPLACE INTO spawn (id, world, x, y, z, yaw, pitch) VALUES (1,?,?,?,?,?,?)")) {
            ps.setString(1, loc.getWorld().getName());
            ps.setDouble(2, loc.getX());
            ps.setDouble(3, loc.getY());
            ps.setDouble(4, loc.getZ());
            ps.setFloat(5, loc.getYaw());
            ps.setFloat(6, loc.getPitch());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save spawn.", e);
        }
    }

    // ── Frozen ────────────────────────────────────────────────

    public Set<UUID> loadAllFrozen() {
        Set<UUID> uuids = new HashSet<>();
        if (frozenConn == null) return uuids;
        try (Statement stmt = frozenConn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT uuid FROM frozen")) {
            while (rs.next()) uuids.add(UUID.fromString(rs.getString("uuid")));
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load frozen players.", e);
        }
        return uuids;
    }

    public void addFrozen(UUID uuid) {
        if (frozenConn == null) return;
        try (PreparedStatement ps = frozenConn.prepareStatement(
                "INSERT OR IGNORE INTO frozen (uuid) VALUES (?)")) {
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to persist freeze for " + uuid, e);
        }
    }

    public void removeFrozen(UUID uuid) {
        if (frozenConn == null) return;
        try (PreparedStatement ps = frozenConn.prepareStatement(
                "DELETE FROM frozen WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to remove freeze for " + uuid, e);
        }
    }
}
