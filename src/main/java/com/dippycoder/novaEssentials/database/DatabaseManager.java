package com.dippycoder.novaEssentials.database;

import com.dippycoder.novaEssentials.NovaEssentials;
import com.dippycoder.novaEssentials.manager.PlayerSettingsManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;

public class DatabaseManager {

    public enum DbType { SQLITE, MYSQL, REDIS }

    private final NovaEssentials plugin;
    private DbType dbType;

    // SQLite — one persistent connection per logical database
    private File dbFolder;
    private Connection homesConn, warpsConn, blocksConn, mutesConn,
                       kitsConn, spawnConn, frozenConn, settingsConn, playtimeConn;

    // MySQL — HikariCP connection pool
    private HikariDataSource mysqlPool;

    // Redis — standalone backend
    private RedisManager redis;

    public DatabaseManager(NovaEssentials plugin) {
        this.plugin = plugin;
    }

    // ── Lifecycle ─────────────────────────────────────────────

    public void connect() {
        String typeStr = plugin.getConfigManager().getDatabaseType();
        dbType = switch (typeStr.toLowerCase()) {
            case "mysql" -> DbType.MYSQL;
            case "redis" -> DbType.REDIS;
            default      -> DbType.SQLITE;
        };

        switch (dbType) {
            case MYSQL  -> setupMySQL();
            case REDIS  -> setupRedis();
            default     -> setupSQLite();
        }
    }

    public void disconnect() {
        switch (dbType) {
            case SQLITE -> {
                closeSQLite(homesConn,    "homes.db");
                closeSQLite(warpsConn,    "warps.db");
                closeSQLite(blocksConn,   "blocks.db");
                closeSQLite(mutesConn,    "mutes.db");
                closeSQLite(kitsConn,     "kits.db");
                closeSQLite(spawnConn,    "spawn.db");
                closeSQLite(frozenConn,   "frozen.db");
                closeSQLite(settingsConn, "settings.db");
                closeSQLite(playtimeConn, "playtime.db");
            }
            case MYSQL -> { if (mysqlPool != null && !mysqlPool.isClosed()) mysqlPool.close(); }
            case REDIS -> { if (redis != null) redis.disconnect(); }
        }
    }

    // ── SQLite setup ──────────────────────────────────────────

    private void setupSQLite() {
        dbFolder = new File(plugin.getDataFolder(), "db");
        dbFolder.mkdirs();

        homesConn = openSQLite("homes.db", """
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

        warpsConn = openSQLite("warps.db", """
                CREATE TABLE IF NOT EXISTS warps (
                    name  TEXT NOT NULL PRIMARY KEY,
                    world TEXT NOT NULL,
                    x     REAL NOT NULL,
                    y     REAL NOT NULL,
                    z     REAL NOT NULL,
                    yaw   REAL NOT NULL,
                    pitch REAL NOT NULL
                )""");

        blocksConn = openSQLite("blocks.db", """
                CREATE TABLE IF NOT EXISTS blocks (
                    uuid         TEXT NOT NULL,
                    blocked_uuid TEXT NOT NULL,
                    PRIMARY KEY (uuid, blocked_uuid)
                )""");

        mutesConn = openSQLite("mutes.db", """
                CREATE TABLE IF NOT EXISTS mutes (
                    uuid     TEXT NOT NULL PRIMARY KEY,
                    muted_by TEXT NOT NULL,
                    reason   TEXT,
                    until    INTEGER NOT NULL
                )""");

        kitsConn = openSQLite("kits.db", """
                CREATE TABLE IF NOT EXISTS kit_cooldowns (
                    uuid     TEXT NOT NULL,
                    kit      TEXT NOT NULL,
                    last_use INTEGER NOT NULL,
                    PRIMARY KEY (uuid, kit)
                )""");

        spawnConn = openSQLite("spawn.db", """
                CREATE TABLE IF NOT EXISTS spawn (
                    id    INTEGER PRIMARY KEY DEFAULT 1 CHECK (id = 1),
                    world TEXT NOT NULL,
                    x     REAL NOT NULL,
                    y     REAL NOT NULL,
                    z     REAL NOT NULL,
                    yaw   REAL NOT NULL,
                    pitch REAL NOT NULL
                )""");

        frozenConn = openSQLite("frozen.db", """
                CREATE TABLE IF NOT EXISTS frozen (
                    uuid TEXT NOT NULL PRIMARY KEY
                )""");

        settingsConn = openSQLite("settings.db", """
                CREATE TABLE IF NOT EXISTS player_settings (
                    uuid           TEXT    NOT NULL PRIMARY KEY,
                    allow_tpa      INTEGER NOT NULL DEFAULT 1,
                    allow_tpahere  INTEGER NOT NULL DEFAULT 1,
                    tpauto         INTEGER NOT NULL DEFAULT 0,
                    preferred_lang TEXT,
                    sounds_enabled INTEGER NOT NULL DEFAULT 1,
                    hide_chat      INTEGER NOT NULL DEFAULT 0,
                    allow_msg      INTEGER NOT NULL DEFAULT 1,
                    allow_payments INTEGER NOT NULL DEFAULT 1,
                    allow_balance  INTEGER NOT NULL DEFAULT 1
                )""");

        playtimeConn = openSQLite("playtime.db", """
                CREATE TABLE IF NOT EXISTS playtime (
                    uuid TEXT NOT NULL PRIMARY KEY,
                    ms   INTEGER NOT NULL DEFAULT 0
                )""");

        plugin.getLogger().info("Connected to SQLite databases.");
    }

    private Connection openSQLite(String fileName, String createSql) {
        try {
            File file = new File(dbFolder, fileName);
            Connection conn = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
            conn.createStatement().execute(createSql);
            return conn;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to open " + fileName + ": " + e.getMessage());
            return null;
        }
    }

    private void closeSQLite(Connection conn, String name) {
        if (conn == null) return;
        try {
            if (!conn.isClosed()) conn.close();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Error closing " + name + ": " + e.getMessage());
        }
    }

    // ── MySQL setup ───────────────────────────────────────────

    private void setupMySQL() {
        var cfg = plugin.getConfigManager();
        HikariConfig hcfg = new HikariConfig();
        hcfg.setJdbcUrl("jdbc:mysql://" + cfg.getMysqlHost() + ":" + cfg.getMysqlPort()
                + "/" + cfg.getMysqlDatabase()
                + "?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=utf8");
        hcfg.setUsername(cfg.getMysqlUsername());
        hcfg.setPassword(cfg.getMysqlPassword());
        hcfg.setMaximumPoolSize(cfg.getMysqlPoolSize());
        hcfg.setPoolName("NovaEssentials-MySQL");
        hcfg.addDataSourceProperty("cachePrepStmts", "true");
        hcfg.addDataSourceProperty("prepStmtCacheSize", "250");
        hcfg.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        try {
            mysqlPool = new HikariDataSource(hcfg);
            createMySQLTables();
            plugin.getLogger().info("Connected to MySQL at "
                    + cfg.getMysqlHost() + ":" + cfg.getMysqlPort()
                    + "/" + cfg.getMysqlDatabase());
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE,
                    "Failed to connect to MySQL (" + e.getMessage() + "). Falling back to SQLite.");
            if (mysqlPool != null && !mysqlPool.isClosed()) mysqlPool.close();
            dbType = DbType.SQLITE;
            setupSQLite();
        }
    }

    private void createMySQLTables() throws SQLException {
        try (Connection conn = mysqlPool.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS homes (
                        uuid  VARCHAR(36) NOT NULL,
                        name  VARCHAR(64) NOT NULL,
                        world VARCHAR(64) NOT NULL,
                        x     DOUBLE NOT NULL,
                        y     DOUBLE NOT NULL,
                        z     DOUBLE NOT NULL,
                        yaw   FLOAT NOT NULL,
                        pitch FLOAT NOT NULL,
                        PRIMARY KEY (uuid, name)
                    )""");
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS warps (
                        name  VARCHAR(64) NOT NULL PRIMARY KEY,
                        world VARCHAR(64) NOT NULL,
                        x     DOUBLE NOT NULL,
                        y     DOUBLE NOT NULL,
                        z     DOUBLE NOT NULL,
                        yaw   FLOAT NOT NULL,
                        pitch FLOAT NOT NULL
                    )""");
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS blocks (
                        uuid         VARCHAR(36) NOT NULL,
                        blocked_uuid VARCHAR(36) NOT NULL,
                        PRIMARY KEY (uuid, blocked_uuid)
                    )""");
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS mutes (
                        uuid     VARCHAR(36) NOT NULL PRIMARY KEY,
                        muted_by VARCHAR(64) NOT NULL,
                        reason   TEXT,
                        until    BIGINT NOT NULL
                    )""");
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS kit_cooldowns (
                        uuid     VARCHAR(36) NOT NULL,
                        kit      VARCHAR(64) NOT NULL,
                        last_use BIGINT NOT NULL,
                        PRIMARY KEY (uuid, kit)
                    )""");
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS spawn (
                        id    TINYINT NOT NULL DEFAULT 1,
                        world VARCHAR(64) NOT NULL,
                        x     DOUBLE NOT NULL,
                        y     DOUBLE NOT NULL,
                        z     DOUBLE NOT NULL,
                        yaw   FLOAT NOT NULL,
                        pitch FLOAT NOT NULL,
                        PRIMARY KEY (id)
                    )""");
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS frozen (
                        uuid VARCHAR(36) NOT NULL PRIMARY KEY
                    )""");
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS player_settings (
                        uuid           VARCHAR(36) NOT NULL PRIMARY KEY,
                        allow_tpa      TINYINT NOT NULL DEFAULT 1,
                        allow_tpahere  TINYINT NOT NULL DEFAULT 1,
                        tpauto         TINYINT NOT NULL DEFAULT 0,
                        preferred_lang VARCHAR(8),
                        sounds_enabled TINYINT NOT NULL DEFAULT 1,
                        hide_chat      TINYINT NOT NULL DEFAULT 0,
                        allow_msg      TINYINT NOT NULL DEFAULT 1,
                        allow_payments TINYINT NOT NULL DEFAULT 1,
                        allow_balance  TINYINT NOT NULL DEFAULT 1
                    )""");
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS playtime (
                        uuid VARCHAR(36) NOT NULL PRIMARY KEY,
                        ms   BIGINT NOT NULL DEFAULT 0
                    )""");
        }
    }

    // ── Redis setup ───────────────────────────────────────────

    private void setupRedis() {
        redis = new RedisManager(plugin);
        redis.connect();
        if (!redis.isConnected()) {
            plugin.getLogger().severe("Failed to connect to Redis. Falling back to SQLite.");
            dbType = DbType.SQLITE;
            setupSQLite();
        }
    }

    // ── SQL connection helpers ────────────────────────────────

    private Connection acquire(String table) throws SQLException {
        if (dbType == DbType.MYSQL) return mysqlPool.getConnection();
        return switch (table) {
            case "homes"    -> homesConn;
            case "warps"    -> warpsConn;
            case "blocks"   -> blocksConn;
            case "mutes"    -> mutesConn;
            case "kits"     -> kitsConn;
            case "spawn"    -> spawnConn;
            case "frozen"   -> frozenConn;
            case "settings" -> settingsConn;
            case "playtime" -> playtimeConn;
            default -> throw new IllegalStateException("Unknown table: " + table);
        };
    }

    /** Returns a MySQL connection to the pool; no-op for SQLite persistent connections. */
    private void release(Connection conn) {
        if (dbType == DbType.MYSQL && conn != null) {
            try { conn.close(); } catch (SQLException ignored) {}
        }
    }

    private boolean mysql() { return dbType == DbType.MYSQL; }

    // ── Homes ─────────────────────────────────────────────────

    public Map<String, Location> loadHomes(UUID uuid) {
        if (dbType == DbType.REDIS) return redis.loadHomes(uuid);

        Map<String, Location> homes = new LinkedHashMap<>();
        Connection conn = null;
        try {
            conn = acquire("homes");
            if (conn == null) return homes;
            try (PreparedStatement ps = conn.prepareStatement(
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
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load homes for " + uuid, e);
        } finally {
            release(conn);
        }
        return homes;
    }

    public void saveHome(UUID uuid, String name, Location loc) {
        if (dbType == DbType.REDIS) { redis.saveHome(uuid, name, loc); return; }

        String sql = mysql()
                ? "INSERT INTO homes (uuid,name,world,x,y,z,yaw,pitch) VALUES (?,?,?,?,?,?,?,?) " +
                  "ON DUPLICATE KEY UPDATE world=VALUES(world),x=VALUES(x),y=VALUES(y)," +
                  "z=VALUES(z),yaw=VALUES(yaw),pitch=VALUES(pitch)"
                : "INSERT OR REPLACE INTO homes (uuid,name,world,x,y,z,yaw,pitch) VALUES (?,?,?,?,?,?,?,?)";

        Connection conn = null;
        try {
            conn = acquire("homes");
            if (conn == null) return;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                ps.setString(2, name);
                ps.setString(3, loc.getWorld().getName());
                ps.setDouble(4, loc.getX());
                ps.setDouble(5, loc.getY());
                ps.setDouble(6, loc.getZ());
                ps.setFloat(7, loc.getYaw());
                ps.setFloat(8, loc.getPitch());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save home " + name + " for " + uuid, e);
        } finally {
            release(conn);
        }
    }

    public void deleteHome(UUID uuid, String name) {
        if (dbType == DbType.REDIS) { redis.deleteHome(uuid, name); return; }

        Connection conn = null;
        try {
            conn = acquire("homes");
            if (conn == null) return;
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM homes WHERE uuid = ? AND name = ?")) {
                ps.setString(1, uuid.toString());
                ps.setString(2, name);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to delete home.", e);
        } finally {
            release(conn);
        }
    }

    // ── Warps ─────────────────────────────────────────────────

    public Map<String, Location> loadWarps() {
        if (dbType == DbType.REDIS) return redis.loadWarps();

        Map<String, Location> warps = new LinkedHashMap<>();
        Connection conn = null;
        try {
            conn = acquire("warps");
            if (conn == null) return warps;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                         "SELECT name, world, x, y, z, yaw, pitch FROM warps")) {
                while (rs.next()) {
                    World world = plugin.getServer().getWorld(rs.getString("world"));
                    if (world == null) continue;
                    warps.put(rs.getString("name"), new Location(world,
                            rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"),
                            rs.getFloat("yaw"), rs.getFloat("pitch")));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load warps.", e);
        } finally {
            release(conn);
        }
        return warps;
    }

    public void saveWarp(String name, Location loc) {
        if (dbType == DbType.REDIS) { redis.saveWarp(name, loc); return; }

        String sql = mysql()
                ? "INSERT INTO warps (name,world,x,y,z,yaw,pitch) VALUES (?,?,?,?,?,?,?) " +
                  "ON DUPLICATE KEY UPDATE world=VALUES(world),x=VALUES(x),y=VALUES(y)," +
                  "z=VALUES(z),yaw=VALUES(yaw),pitch=VALUES(pitch)"
                : "INSERT OR REPLACE INTO warps (name,world,x,y,z,yaw,pitch) VALUES (?,?,?,?,?,?,?)";

        Connection conn = null;
        try {
            conn = acquire("warps");
            if (conn == null) return;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, name);
                ps.setString(2, loc.getWorld().getName());
                ps.setDouble(3, loc.getX());
                ps.setDouble(4, loc.getY());
                ps.setDouble(5, loc.getZ());
                ps.setFloat(6, loc.getYaw());
                ps.setFloat(7, loc.getPitch());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save warp " + name, e);
        } finally {
            release(conn);
        }
    }

    public void deleteWarp(String name) {
        if (dbType == DbType.REDIS) { redis.deleteWarp(name); return; }

        Connection conn = null;
        try {
            conn = acquire("warps");
            if (conn == null) return;
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM warps WHERE name = ?")) {
                ps.setString(1, name);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to delete warp.", e);
        } finally {
            release(conn);
        }
    }

    // ── Blocks ────────────────────────────────────────────────

    public Set<UUID> loadBlockedPlayers(UUID uuid) {
        if (dbType == DbType.REDIS) return redis.loadBlockedPlayers(uuid);

        Set<UUID> blocked = new HashSet<>();
        Connection conn = null;
        try {
            conn = acquire("blocks");
            if (conn == null) return blocked;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT blocked_uuid FROM blocks WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                ResultSet rs = ps.executeQuery();
                while (rs.next()) blocked.add(UUID.fromString(rs.getString("blocked_uuid")));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load blocked players for " + uuid, e);
        } finally {
            release(conn);
        }
        return blocked;
    }

    public void addBlock(UUID uuid, UUID blocked) {
        if (dbType == DbType.REDIS) { redis.addBlock(uuid, blocked); return; }

        String sql = mysql()
                ? "INSERT IGNORE INTO blocks (uuid, blocked_uuid) VALUES (?, ?)"
                : "INSERT OR IGNORE INTO blocks (uuid, blocked_uuid) VALUES (?, ?)";
        Connection conn = null;
        try {
            conn = acquire("blocks");
            if (conn == null) return;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                ps.setString(2, blocked.toString());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to add block.", e);
        } finally {
            release(conn);
        }
    }

    public void removeBlock(UUID uuid, UUID blocked) {
        if (dbType == DbType.REDIS) { redis.removeBlock(uuid, blocked); return; }

        Connection conn = null;
        try {
            conn = acquire("blocks");
            if (conn == null) return;
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM blocks WHERE uuid = ? AND blocked_uuid = ?")) {
                ps.setString(1, uuid.toString());
                ps.setString(2, blocked.toString());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to remove block.", e);
        } finally {
            release(conn);
        }
    }

    // ── Mutes ─────────────────────────────────────────────────

    public record MuteInfo(String mutedBy, String reason, long until) {
        public boolean isPermanent() { return until == -1; }
        public boolean isExpired()   { return !isPermanent() && System.currentTimeMillis() > until; }
    }

    public MuteInfo loadMute(UUID uuid) {
        if (dbType == DbType.REDIS) return redis.loadMute(uuid);

        Connection conn = null;
        try {
            conn = acquire("mutes");
            if (conn == null) return null;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT muted_by, reason, until FROM mutes WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next())
                    return new MuteInfo(
                            rs.getString("muted_by"), rs.getString("reason"), rs.getLong("until"));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load mute for " + uuid, e);
        } finally {
            release(conn);
        }
        return null;
    }

    public void saveMute(UUID uuid, String mutedBy, String reason, long until) {
        if (dbType == DbType.REDIS) { redis.saveMute(uuid, mutedBy, reason, until); return; }

        String sql = mysql()
                ? "INSERT INTO mutes (uuid,muted_by,reason,until) VALUES (?,?,?,?) " +
                  "ON DUPLICATE KEY UPDATE muted_by=VALUES(muted_by),reason=VALUES(reason),until=VALUES(until)"
                : "INSERT OR REPLACE INTO mutes (uuid,muted_by,reason,until) VALUES (?,?,?,?)";
        Connection conn = null;
        try {
            conn = acquire("mutes");
            if (conn == null) return;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                ps.setString(2, mutedBy);
                ps.setString(3, reason);
                ps.setLong(4, until);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save mute.", e);
        } finally {
            release(conn);
        }
    }

    public void deleteMute(UUID uuid) {
        if (dbType == DbType.REDIS) { redis.deleteMute(uuid); return; }

        Connection conn = null;
        try {
            conn = acquire("mutes");
            if (conn == null) return;
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM mutes WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to delete mute.", e);
        } finally {
            release(conn);
        }
    }

    // ── Kit Cooldowns ─────────────────────────────────────────

    public Map<String, Long> loadKitCooldowns(UUID uuid) {
        if (dbType == DbType.REDIS) return redis.loadKitCooldowns(uuid);

        Map<String, Long> cooldowns = new HashMap<>();
        Connection conn = null;
        try {
            conn = acquire("kits");
            if (conn == null) return cooldowns;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT kit, last_use FROM kit_cooldowns WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                ResultSet rs = ps.executeQuery();
                while (rs.next()) cooldowns.put(rs.getString("kit"), rs.getLong("last_use"));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load kit cooldowns for " + uuid, e);
        } finally {
            release(conn);
        }
        return cooldowns;
    }

    public void saveKitCooldown(UUID uuid, String kit, long lastUse) {
        if (dbType == DbType.REDIS) { redis.saveKitCooldown(uuid, kit, lastUse); return; }

        String sql = mysql()
                ? "INSERT INTO kit_cooldowns (uuid,kit,last_use) VALUES (?,?,?) " +
                  "ON DUPLICATE KEY UPDATE last_use=VALUES(last_use)"
                : "INSERT OR REPLACE INTO kit_cooldowns (uuid,kit,last_use) VALUES (?,?,?)";
        Connection conn = null;
        try {
            conn = acquire("kits");
            if (conn == null) return;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                ps.setString(2, kit);
                ps.setLong(3, lastUse);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save kit cooldown.", e);
        } finally {
            release(conn);
        }
    }

    // ── Spawn ─────────────────────────────────────────────────

    public Location loadSpawn() {
        if (dbType == DbType.REDIS) return redis.loadSpawn();

        Connection conn = null;
        try {
            conn = acquire("spawn");
            if (conn == null) return null;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                         "SELECT world, x, y, z, yaw, pitch FROM spawn WHERE id = 1")) {
                if (rs.next()) {
                    World world = plugin.getServer().getWorld(rs.getString("world"));
                    if (world == null) return null;
                    return new Location(world,
                            rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"),
                            rs.getFloat("yaw"), rs.getFloat("pitch"));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load spawn.", e);
        } finally {
            release(conn);
        }
        return null;
    }

    public void saveSpawn(Location loc) {
        if (dbType == DbType.REDIS) { redis.saveSpawn(loc); return; }

        String sql = mysql()
                ? "INSERT INTO spawn (id,world,x,y,z,yaw,pitch) VALUES (1,?,?,?,?,?,?) " +
                  "ON DUPLICATE KEY UPDATE world=VALUES(world),x=VALUES(x),y=VALUES(y)," +
                  "z=VALUES(z),yaw=VALUES(yaw),pitch=VALUES(pitch)"
                : "INSERT OR REPLACE INTO spawn (id,world,x,y,z,yaw,pitch) VALUES (1,?,?,?,?,?,?)";
        Connection conn = null;
        try {
            conn = acquire("spawn");
            if (conn == null) return;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, loc.getWorld().getName());
                ps.setDouble(2, loc.getX());
                ps.setDouble(3, loc.getY());
                ps.setDouble(4, loc.getZ());
                ps.setFloat(5, loc.getYaw());
                ps.setFloat(6, loc.getPitch());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save spawn.", e);
        } finally {
            release(conn);
        }
    }

    // ── Frozen ────────────────────────────────────────────────

    public Set<UUID> loadAllFrozen() {
        if (dbType == DbType.REDIS) return redis.loadAllFrozen();

        Set<UUID> uuids = new HashSet<>();
        Connection conn = null;
        try {
            conn = acquire("frozen");
            if (conn == null) return uuids;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT uuid FROM frozen")) {
                while (rs.next()) uuids.add(UUID.fromString(rs.getString("uuid")));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load frozen players.", e);
        } finally {
            release(conn);
        }
        return uuids;
    }

    public void addFrozen(UUID uuid) {
        if (dbType == DbType.REDIS) { redis.addFrozen(uuid); return; }

        String sql = mysql()
                ? "INSERT IGNORE INTO frozen (uuid) VALUES (?)"
                : "INSERT OR IGNORE INTO frozen (uuid) VALUES (?)";
        Connection conn = null;
        try {
            conn = acquire("frozen");
            if (conn == null) return;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to persist freeze for " + uuid, e);
        } finally {
            release(conn);
        }
    }

    public void removeFrozen(UUID uuid) {
        if (dbType == DbType.REDIS) { redis.removeFrozen(uuid); return; }

        Connection conn = null;
        try {
            conn = acquire("frozen");
            if (conn == null) return;
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM frozen WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to remove freeze for " + uuid, e);
        } finally {
            release(conn);
        }
    }

    // ── Player Settings ───────────────────────────────────────

    public PlayerSettingsManager.PlayerSettings loadPlayerSettings(UUID uuid) {
        if (dbType == DbType.REDIS) return redis.loadPlayerSettings(uuid);

        Connection conn = null;
        try {
            conn = acquire("settings");
            if (conn == null) return null;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT allow_tpa,allow_tpahere,tpauto,preferred_lang,sounds_enabled," +
                    "hide_chat,allow_msg,allow_payments,allow_balance " +
                    "FROM player_settings WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    PlayerSettingsManager.PlayerSettings s = new PlayerSettingsManager.PlayerSettings();
                    s.allowTpa      = rs.getInt("allow_tpa")      == 1;
                    s.allowTpaHere  = rs.getInt("allow_tpahere")  == 1;
                    s.tpauto        = rs.getInt("tpauto")         == 1;
                    s.preferredLang = rs.getString("preferred_lang");
                    s.soundsEnabled = rs.getInt("sounds_enabled") == 1;
                    s.hideChat      = rs.getInt("hide_chat")      == 1;
                    s.allowMsg      = rs.getInt("allow_msg")      == 1;
                    s.allowPayments = rs.getInt("allow_payments") == 1;
                    s.allowBalance  = rs.getInt("allow_balance")  == 1;
                    return s;
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load settings for " + uuid, e);
        } finally {
            release(conn);
        }
        return null;
    }

    public void savePlayerSettings(UUID uuid, PlayerSettingsManager.PlayerSettings s) {
        if (dbType == DbType.REDIS) { redis.savePlayerSettings(uuid, s); return; }

        String sql = mysql()
                ? "INSERT INTO player_settings " +
                  "(uuid,allow_tpa,allow_tpahere,tpauto,preferred_lang," +
                  "sounds_enabled,hide_chat,allow_msg,allow_payments,allow_balance) VALUES (?,?,?,?,?,?,?,?,?,?) " +
                  "ON DUPLICATE KEY UPDATE " +
                  "allow_tpa=VALUES(allow_tpa),allow_tpahere=VALUES(allow_tpahere)," +
                  "tpauto=VALUES(tpauto),preferred_lang=VALUES(preferred_lang)," +
                  "sounds_enabled=VALUES(sounds_enabled),hide_chat=VALUES(hide_chat)," +
                  "allow_msg=VALUES(allow_msg),allow_payments=VALUES(allow_payments)," +
                  "allow_balance=VALUES(allow_balance)"
                : "INSERT OR REPLACE INTO player_settings " +
                  "(uuid,allow_tpa,allow_tpahere,tpauto,preferred_lang,sounds_enabled," +
                  "hide_chat,allow_msg,allow_payments,allow_balance) VALUES (?,?,?,?,?,?,?,?,?,?)";
        Connection conn = null;
        try {
            conn = acquire("settings");
            if (conn == null) return;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                ps.setInt(2, s.allowTpa      ? 1 : 0);
                ps.setInt(3, s.allowTpaHere  ? 1 : 0);
                ps.setInt(4, s.tpauto        ? 1 : 0);
                ps.setString(5, s.preferredLang);
                ps.setInt(6, s.soundsEnabled ? 1 : 0);
                ps.setInt(7, s.hideChat      ? 1 : 0);
                ps.setInt(8, s.allowMsg      ? 1 : 0);
                ps.setInt(9, s.allowPayments ? 1 : 0);
                ps.setInt(10, s.allowBalance ? 1 : 0);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save settings for " + uuid, e);
        } finally {
            release(conn);
        }
    }

    // ── Playtime ──────────────────────────────────────────────

    public long loadPlaytime(UUID uuid) {
        if (dbType == DbType.REDIS) {
            String raw = redis.getPlaytime(uuid);
            return raw != null ? Long.parseLong(raw) : 0L;
        }

        Connection conn = null;
        try {
            conn = acquire("playtime");
            if (conn == null) return 0;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT ms FROM playtime WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) return rs.getLong("ms");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load playtime for " + uuid, e);
        } finally {
            release(conn);
        }
        return 0;
    }

    public void savePlaytime(UUID uuid, long ms) {
        if (dbType == DbType.REDIS) { redis.savePlaytime(uuid, ms); return; }

        String sql = mysql()
                ? "INSERT INTO playtime (uuid,ms) VALUES (?,?) ON DUPLICATE KEY UPDATE ms=VALUES(ms)"
                : "INSERT OR REPLACE INTO playtime (uuid,ms) VALUES (?,?)";
        Connection conn = null;
        try {
            conn = acquire("playtime");
            if (conn == null) return;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                ps.setLong(2, ms);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save playtime for " + uuid, e);
        } finally {
            release(conn);
        }
    }

    // ── Accessors ─────────────────────────────────────────────

    public DbType getDbType() { return dbType; }
}
