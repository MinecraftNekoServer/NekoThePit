package cn.charlotte.pit.database;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.*;
import cn.charlotte.pit.data.sub.PlayerEnderChest;
import cn.charlotte.pit.data.sub.PlayerInv;
import cn.charlotte.pit.util.chat.CC;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.slf4j.Logger;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * MySQL database implementation for ThePit plugin
 */
public class MySQL {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(MySQL.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final ExecutorService executor = Executors.newFixedThreadPool(5);

    private String host;
    private int port;
    private String database;
    private String username;
    private String password;

    public void connect() {
        log.info("Connecting to MySQL database...");

        this.host = ThePit.getInstance().getPitConfig().getMySQLAddress();
        this.port = ThePit.getInstance().getPitConfig().getMySQLPort();
        this.database = ThePit.getInstance().getPitConfig().getMySQLDatabase();
        this.username = ThePit.getInstance().getPitConfig().getMySQLUser();
        this.password = ThePit.getInstance().getPitConfig().getMySQLPassword();

        try {
            // Test connection first
            String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true", host, port, database);
            try (Connection testConnection = DriverManager.getConnection(url, username, password)) {
                // Create tables if they don't exist
                createTables(testConnection);
            }
            
            log.info("Connected to MySQL database!");
        } catch (SQLException e) {
            log.error("Failed to connect to MySQL database", e);
            throw new RuntimeException("Failed to connect to MySQL database", e);
        }
    }

    private Connection createConnection() throws SQLException {
        String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true", host, port, database);
        return DriverManager.getConnection(url, username, password);
    }

    private void createTables(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        
        // Create players table
        stmt.execute("CREATE TABLE IF NOT EXISTS players (" +
                "uuid VARCHAR(36) PRIMARY KEY NOT NULL," +
                "lowerName VARCHAR(16) UNIQUE NOT NULL," +
                "playerData JSON NOT NULL," +
                "lastLoginTime BIGINT," +
                "lastLogoutTime BIGINT," +
                "totalPlayedTime BIGINT," +
                "registerTime BIGINT NOT NULL" +
                ")");

        // Create indexes
        try {
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_uuid ON players(uuid)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_lowerName ON players(lowerName)");
        } catch (SQLException e) {
            // Index might already exist, ignore
        }

        // Create trade table
        stmt.execute("CREATE TABLE IF NOT EXISTS trade (" +
                "tradeUuid VARCHAR(36) PRIMARY KEY NOT NULL," +
                "playerA VARCHAR(36) NOT NULL," +
                "playerB VARCHAR(36) NOT NULL," +
                "playerAName VARCHAR(16) NOT NULL," +
                "playerBName VARCHAR(16) NOT NULL," +
                "completeTime BIGINT," +
                "tradeData JSON NOT NULL," +
                "timeStamp BIGINT NOT NULL" +
                ")");

        try {
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_playerA ON trade(playerA)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_playerB ON trade(playerB)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_tradeUuid ON trade(tradeUuid)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_completeTime ON trade(completeTime)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_timeStamp ON trade(timeStamp)");
        } catch (SQLException e) {
            // Index might already exist, ignore
        }

        // Create mail table
        stmt.execute("CREATE TABLE IF NOT EXISTS mail (" +
                "uuid VARCHAR(36) PRIMARY KEY NOT NULL," +
                "name VARCHAR(16) NOT NULL," +
                "nameLower VARCHAR(16) NOT NULL," +
                "mailData JSON NOT NULL" +
                ")");

        try {
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_mail_uuid ON mail(uuid)");
        } catch (SQLException e) {
            // Index might already exist, ignore
        }

        // Create inventory backup table
        stmt.execute("CREATE TABLE IF NOT EXISTS inv (" +
                "backupUuid VARCHAR(36) PRIMARY KEY NOT NULL," +
                "uuid VARCHAR(36) NOT NULL," +
                "timeStamp BIGINT NOT NULL," +
                "backupData JSON NOT NULL" +
                ")");

        try {
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_inv_uuid ON inv(uuid)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_backup_timeStamp ON inv(timeStamp)");
        } catch (SQLException e) {
            // Index might already exist, ignore
        }

        // Create CDK table
        stmt.execute("CREATE TABLE IF NOT EXISTS cdk (" +
                "cdk VARCHAR(255) PRIMARY KEY NOT NULL," +
                "cdkData JSON NOT NULL," +
                "createTime BIGINT NOT NULL" +
                ")");

        // Create reward table
        stmt.execute("CREATE TABLE IF NOT EXISTS reward (" +
                "id VARCHAR(36) PRIMARY KEY NOT NULL," +
                "rewardData JSON NOT NULL," +
                "createTime BIGINT NOT NULL" +
                ")");

        // Create event_queue table
        stmt.execute("CREATE TABLE IF NOT EXISTS event_queue (" +
                "id VARCHAR(255) PRIMARY KEY NOT NULL," +
                "queueData JSON NOT NULL" +
                ")");

        stmt.close();
    }

    public CompletableFuture<PlayerProfile> loadPlayerProfileByUuid(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                conn = createConnection();
                String sql = "SELECT playerData, lastLoginTime, lastLogoutTime, totalPlayedTime, registerTime FROM players WHERE uuid = ?";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, uuid.toString());
                
                rs = stmt.executeQuery();
                if (rs.next()) {
                    String playerDataJson = rs.getString("playerData");
                    PlayerProfile profile = objectMapper.readValue(playerDataJson, PlayerProfile.class);
                    
                    // Set additional fields that are stored separately
                    profile.setLastLoginTime(rs.getLong("lastLoginTime"));
                    profile.setLastLogoutTime(rs.getLong("lastLogoutTime"));
                    profile.setTotalPlayedTime(rs.getLong("totalPlayedTime"));
                    profile.setRegisterTime(rs.getLong("registerTime"));
                    
                    // Load mail data for the profile
                    loadMail(profile, uuid);
                    
                    // Load inventory backups
                    loadInventoryBackups(profile, uuid);
                    
                    return profile;
                }
                
                return null;
            } catch (Exception e) {
                log.error("Error loading player profile by UUID: " + uuid, e);
                return null;
            } finally {
                try {
                    if (rs != null) rs.close();
                    if (stmt != null) stmt.close();
                    if (conn != null) conn.close();
                } catch (SQLException e) {
                    log.warn("Error closing resources", e);
                }
            }
        }, executor);
    }

    public void savePlayerProfile(PlayerProfile profile, Player player) {
        executor.execute(() -> {
            Connection conn = null;
            PreparedStatement stmt = null;
            try {
                // Update inventory if player is provided
                if (player != null) {
                    profile.setInventory(cn.charlotte.pit.util.inventory.InventoryUtil.playerInventoryFromPlayer(player));
                }
                
                // Prepare backup and save it
                final long now = System.currentTimeMillis();
                final PlayerInvBackup backup = new PlayerInvBackup();
                backup.setUuid(profile.getUuid());
                backup.setTimeStamp(now);
                backup.setBackupUuid(UUID.randomUUID().toString());
                backup.setInv(profile.getInventory());
                backup.setChest(profile.getEnderChest());
                backup.setTimeStamp(System.currentTimeMillis());
                
                backup.save();

                String sql = "INSERT INTO players (uuid, lowerName, playerData, lastLoginTime, lastLogoutTime, totalPlayedTime, registerTime) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE " +
                        "playerData = VALUES(playerData), " +
                        "lastLoginTime = VALUES(lastLoginTime), " +
                        "lastLogoutTime = VALUES(lastLogoutTime), " +
                        "totalPlayedTime = VALUES(totalPlayedTime)";

                conn = getConnection();
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, profile.getUuid());
                stmt.setString(2, profile.getLowerName());
                
                // Serialize the entire profile to JSON
                String playerDataJson = objectMapper.writeValueAsString(profile);
                stmt.setString(3, playerDataJson);
                
                stmt.setLong(4, profile.getLastLoginTime());
                stmt.setLong(5, profile.getLastLogoutTime());
                stmt.setLong(6, profile.getTotalPlayedTime());
                stmt.setLong(7, profile.getRegisterTime());
                
                stmt.executeUpdate();
            } catch (Exception e) {
                log.error("Error saving player profile: " + profile.getUuid(), e);
            } finally {
                try {
                    if (stmt != null) stmt.close();
                    if (conn != null) conn.close();
                } catch (SQLException e) {
                    log.warn("Error closing resources", e);
                }
            }
        });
    }

    public CompletableFuture<PlayerProfile> loadPlayerProfileByName(String name) {
        return CompletableFuture.supplyAsync(() -> {
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                conn = getConnection();
                String sql = "SELECT playerData, lastLoginTime, lastLogoutTime, totalPlayedTime, registerTime FROM players WHERE lowerName = ?";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, name.toLowerCase());
                
                rs = stmt.executeQuery();
                if (rs.next()) {
                    String playerDataJson = rs.getString("playerData");
                    PlayerProfile profile = objectMapper.readValue(playerDataJson, PlayerProfile.class);
                    
                    // Set additional fields that are stored separately
                    profile.setLastLoginTime(rs.getLong("lastLoginTime"));
                    profile.setLastLogoutTime(rs.getLong("lastLogoutTime"));
                    profile.setTotalPlayedTime(rs.getLong("totalPlayedTime"));
                    profile.setRegisterTime(rs.getLong("registerTime"));
                    
                    return profile;
                }
                
                return null;
            } catch (Exception e) {
                log.error("Error loading player profile by name: " + name, e);
                return null;
            } finally {
                try {
                    if (rs != null) rs.close();
                    if (stmt != null) stmt.close();
                    if (conn != null) conn.close();
                } catch (SQLException e) {
                    log.warn("Error closing resources", e);
                }
            }
        }, executor);
    }

    public void saveTradeData(TradeData tradeData) {
        executor.execute(() -> {
            Connection conn = null;
            PreparedStatement stmt = null;
            try {
                String sql = "INSERT INTO trade (tradeUuid, playerA, playerB, playerAName, playerBName, completeTime, tradeData, timeStamp) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE " +
                        "completeTime = VALUES(completeTime), tradeData = VALUES(tradeData)";

                conn = getConnection();
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, tradeData.getTradeUuid());
                stmt.setString(2, tradeData.getPlayerA());
                stmt.setString(3, tradeData.getPlayerB());
                stmt.setString(4, tradeData.getPlayerAName());
                stmt.setString(5, tradeData.getPlayerBName());
                stmt.setLong(6, tradeData.getCompleteTime());
                
                String tradeDataJson = "{}"; // Use empty JSON for now
                try {
                    tradeDataJson = objectMapper.writeValueAsString(tradeData);
                } catch (Exception e) {
                    log.error("Error serializing trade data", e);
                }
                stmt.setString(7, tradeDataJson);
                
                stmt.setLong(8, System.currentTimeMillis());
                
                stmt.executeUpdate();
            } catch (Exception e) {
                log.error("Error saving trade data: " + tradeData.getTradeUuid(), e);
            } finally {
                try {
                    if (stmt != null) stmt.close();
                    if (conn != null) conn.close();
                } catch (SQLException e) {
                    log.warn("Error closing resources", e);
                }
            }
        });
    }

    public void saveMailData(PlayerMailData mailData) {
        executor.execute(() -> {
            Connection conn = null;
            PreparedStatement stmt = null;
            try {
                String sql = "INSERT INTO mail (uuid, name, nameLower, mailData) " +
                        "VALUES (?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE " +
                        "name = VALUES(name), nameLower = VALUES(nameLower), mailData = VALUES(mailData)";

                conn = getConnection();
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, mailData.getUuid());
                stmt.setString(2, mailData.getName());
                stmt.setString(3, mailData.getNameLower());
                
                String mailDataJson = "{}"; // Use empty JSON for now
                try {
                    mailDataJson = objectMapper.writeValueAsString(mailData);
                } catch (Exception e) {
                    log.error("Error serializing mail data", e);
                }
                stmt.setString(4, mailDataJson);
                
                stmt.executeUpdate();
            } catch (Exception e) {
                log.error("Error saving mail data for UUID: " + mailData.getUuid(), e);
            } finally {
                try {
                    if (stmt != null) stmt.close();
                    if (conn != null) conn.close();
                } catch (SQLException e) {
                    log.warn("Error closing resources", e);
                }
            }
        });
    }

    public void saveInventoryBackup(PlayerInvBackup backup) {
        executor.execute(() -> {
            Connection conn = null;
            PreparedStatement stmt = null;
            try {
                String sql = "INSERT INTO inv (backupUuid, uuid, timeStamp, backupData) " +
                        "VALUES (?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE " +
                        "timeStamp = VALUES(timeStamp), backupData = VALUES(backupData)";

                conn = getConnection();
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, backup.getBackupUuid());
                stmt.setString(2, backup.getUuid());
                stmt.setLong(3, backup.getTimeStamp());
                
                String backupDataJson = "{}"; // Use empty JSON for now
                try {
                    backupDataJson = objectMapper.writeValueAsString(backup);
                } catch (Exception e) {
                    log.error("Error serializing backup data", e);
                }
                stmt.setString(4, backupDataJson);
                
                stmt.executeUpdate();
            } catch (Exception e) {
                log.error("Error saving inventory backup: " + backup.getBackupUuid(), e);
            } finally {
                try {
                    if (stmt != null) stmt.close();
                    if (conn != null) conn.close();
                } catch (SQLException e) {
                    log.warn("Error closing resources", e);
                }
            }
        });
    }

    public void saveCdkData(CDKData cdkData) {
        executor.execute(() -> {
            Connection conn = null;
            PreparedStatement stmt = null;
            try {
                String sql = "INSERT INTO cdk (cdk, cdkData, createTime) " +
                        "VALUES (?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE " +
                        "cdkData = VALUES(cdkData), createTime = VALUES(createTime)";

                conn = getConnection();
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, cdkData.getCdk());
                
                String cdkDataJson = "{}"; // Use empty JSON for now
                try {
                    cdkDataJson = objectMapper.writeValueAsString(cdkData);
                } catch (Exception e) {
                    log.error("Error serializing CDK data", e);
                }
                stmt.setString(2, cdkDataJson);
                
                stmt.setLong(3, System.currentTimeMillis());
                
                stmt.executeUpdate();
            } catch (Exception e) {
                log.error("Error saving CDK data: " + cdkData.getCdk(), e);
            } finally {
                try {
                    if (stmt != null) stmt.close();
                    if (conn != null) conn.close();
                } catch (SQLException e) {
                    log.warn("Error closing resources", e);
                }
            }
        });
    }

    public void saveRewardData(FixedRewardData rewardData) {
        executor.execute(() -> {
            Connection conn = null;
            PreparedStatement stmt = null;
            try {
                String sql = "INSERT INTO reward (id, rewardData, createTime) " +
                        "VALUES (?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE " +
                        "rewardData = VALUES(rewardData), createTime = VALUES(createTime)";

                conn = getConnection();
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, rewardData.getId());
                
                String rewardDataJson = "{}"; // Use empty JSON for now
                try {
                    rewardDataJson = objectMapper.writeValueAsString(rewardData);
                } catch (Exception e) {
                    log.error("Error serializing reward data", e);
                }
                stmt.setString(2, rewardDataJson);
                
                stmt.setLong(3, System.currentTimeMillis());
                
                stmt.executeUpdate();
            } catch (Exception e) {
                log.error("Error saving reward data: " + rewardData.getId(), e);
            } finally {
                try {
                    if (stmt != null) stmt.close();
                    if (conn != null) conn.close();
                } catch (SQLException e) {
                    log.warn("Error closing resources", e);
                }
            }
        });
    }

    public void saveEventQueueData(EventQueue eventQueue) {
        executor.execute(() -> {
            Connection conn = null;
            PreparedStatement stmt = null;
            try {
                String sql = "INSERT INTO event_queue (id, queueData) " +
                        "VALUES (?, ?) " +
                        "ON DUPLICATE KEY UPDATE " +
                        "queueData = VALUES(queueData)";

                conn = getConnection();
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, eventQueue.getId());
                
                String eventQueueJson = "{}"; // Use empty JSON for now
                try {
                    eventQueueJson = objectMapper.writeValueAsString(eventQueue);
                } catch (Exception e) {
                    log.error("Error serializing event queue data", e);
                }
                stmt.setString(2, eventQueueJson);
                
                stmt.executeUpdate();
            } catch (Exception e) {
                log.error("Error saving event queue data: " + eventQueue.getId(), e);
            } finally {
                try {
                    if (stmt != null) stmt.close();
                    if (conn != null) conn.close();
                } catch (SQLException e) {
                    log.warn("Error closing resources", e);
                }
            }
        });
    }

    private void loadMail(PlayerProfile playerProfile, UUID uuid) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            String sql = "SELECT mailData FROM mail WHERE uuid = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, uuid.toString());
            
            rs = stmt.executeQuery();
            if (rs.next()) {
                String mailDataJson = rs.getString("mailData");
                PlayerMailData mailData = objectMapper.readValue(mailDataJson, PlayerMailData.class);
                
                if (mailData == null) {
                    mailData = new PlayerMailData();
                    mailData.setUuid(uuid.toString());
                    mailData.setName(playerProfile.getPlayerName());
                    mailData.setNameLower(playerProfile.getLowerName());
                }
                
                mailData.cleanUp();
                playerProfile.setMailData(mailData);
            } else {
                // Create new mail data if none exists
                PlayerMailData mailData = new PlayerMailData();
                mailData.setUuid(uuid.toString());
                mailData.setName(playerProfile.getPlayerName());
                mailData.setNameLower(playerProfile.getLowerName());
                mailData.cleanUp();
                playerProfile.setMailData(mailData);
            }
        } catch (Exception e) {
            log.error("Error loading mail for player: " + uuid, e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                log.warn("Error closing resources", e);
            }
        }
    }

    private void loadInventoryBackups(PlayerProfile profile, UUID uuid) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            String sql = "SELECT backupData FROM inv WHERE uuid = ? ORDER BY timeStamp DESC";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, uuid.toString());
            
            rs = stmt.executeQuery();
            long lastTime = 0;
            while (rs.next()) {
                String backupDataJson = rs.getString("backupData");
                PlayerInvBackup backup = objectMapper.readValue(backupDataJson, PlayerInvBackup.class);
                
                // Remove backups that are too close together in time (same as MongoDB version)
                if (Math.abs(backup.getTimeStamp() - lastTime) < 10 * 60 * 1000) {
                    // Remove this backup from database using a separate connection
                    deleteBackup(backup.getBackupUuid());
                    continue;
                }
                
                lastTime = backup.getTimeStamp();
                profile.getInvBackups().add(backup);
            }
        } catch (Exception e) {
            log.error("Error loading inventory backups for player: " + uuid, e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                log.warn("Error closing resources", e);
            }
        }
    }

    private void deleteBackup(String backupUuid) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = getConnection();
            String deleteSql = "DELETE FROM inv WHERE backupUuid = ?";
            stmt = conn.prepareStatement(deleteSql);
            stmt.setString(1, backupUuid);
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Error deleting backup: " + backupUuid, e);
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                log.warn("Error closing resources", e);
            }
        }
    }

    public void close() {
        // Since we're creating new connections for each operation, we don't need to close a shared connection
        log.info("MySQL connection management closed!");
    }

    public boolean isConnected() {
        try {
            // Test the connection by creating and closing a temporary connection
            try (Connection testConn = getConnection()) {
                return testConn.isValid(1);
            }
        } catch (SQLException e) {
            return false;
        }
    }

        // This method returns a new connection each time, not a shared one
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            String.format("jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true", host, port, database), 
            username, password
        );
    }

    // Getters for data collections (simulating MongoDB collection access)
    public PlayerProfileCollection getPlayerProfileCollection() {
        return new PlayerProfileCollection();
    }

    // For backward compatibility
    public PlayerProfileCollection getProfileCollection() {
        return new PlayerProfileCollection();
    }

    public TradeDataCollection getTradeCollection() {
        return new TradeDataCollection();
    }

    public PlayerMailDataCollection getMailCollection() {
        return new PlayerMailDataCollection();
    }

    public PlayerInvBackupCollection getInvCollection() {
        return new PlayerInvBackupCollection();
    }

    public CDKDataCollection getCdkCollection() {
        return new CDKDataCollection();
    }

    public FixedRewardDataCollection getRewardCollection() {
        return new FixedRewardDataCollection();
    }

    public EventQueueCollection getEventQueueCollection() {
        return new EventQueueCollection();
    }

    // Inner classes to maintain API compatibility with existing MongoDB usage
    public class PlayerProfileCollection {
        public CompletableFuture<PlayerProfile> find(UUID uuid) {
            return MySQL.this.loadPlayerProfileByUuid(uuid);
        }
        
        public CompletableFuture<PlayerProfile> find(String lowerName) {
            return MySQL.this.loadPlayerProfileByName(lowerName);
        }
        
        public void save(PlayerProfile profile, Player player) {
            MySQL.this.savePlayerProfile(profile, player);
        }
    }

    public class TradeDataCollection {
        public void save(TradeData tradeData) {
            MySQL.this.saveTradeData(tradeData);
        }
    }

    public class PlayerMailDataCollection {
        public CompletableFuture<PlayerMailData> find(UUID uuid) {
            return MySQL.this.loadMailByUuid(uuid);
        }
        
        public void save(PlayerMailData mailData) {
            MySQL.this.saveMailData(mailData);
        }
    }

    public class PlayerInvBackupCollection {
        public void save(PlayerInvBackup backup) {
            MySQL.this.saveInventoryBackup(backup);
        }
    }

    public class CDKDataCollection {
        public void save(CDKData cdkData) {
            MySQL.this.saveCdkData(cdkData);
        }
    }

    public class FixedRewardDataCollection {
        public void save(FixedRewardData rewardData) {
            MySQL.this.saveRewardData(rewardData);
        }
    }

    public class EventQueueCollection {
        public void save(EventQueue eventQueue) {
            MySQL.this.saveEventQueueData(eventQueue);
        }
    }
    
    public CompletableFuture<PlayerMailData> loadMailByUuid(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                conn = getConnection();
                String sql = "SELECT mailData FROM mail WHERE uuid = ?";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, uuid.toString());
                
                rs = stmt.executeQuery();
                if (rs.next()) {
                    String mailDataJson = rs.getString("mailData");
                    return objectMapper.readValue(mailDataJson, PlayerMailData.class);
                }
                
                return null;
            } catch (Exception e) {
                log.error("Error loading mail by UUID: " + uuid, e);
                return null;
            } finally {
                try {
                    if (rs != null) rs.close();
                    if (stmt != null) stmt.close();
                    if (conn != null) conn.close();
                } catch (SQLException e) {
                    log.warn("Error closing resources", e);
                }
            }
        }, executor);
    }
}