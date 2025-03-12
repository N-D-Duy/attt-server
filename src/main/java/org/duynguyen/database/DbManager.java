package org.duynguyen.database;

import org.duynguyen.utils.Config;
import org.duynguyen.utils.Log;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class DbManager {

    private static DbManager instance = null;
    private HikariDataSource hikariDataSource;

    public static DbManager getInstance() {
        if (instance == null) {
            instance = new DbManager();
        }
        return instance;
    }

    private DbManager() {}

    public boolean start() {
        if (this.hikariDataSource != null) {
            Log.warn("DB Connection Pool has already been created.");
            return false;
        } else {
            try {
                HikariConfig config = getHikariConfig();
                config.addDataSourceProperty("cachePrepStmts", "true");
                config.addDataSourceProperty("prepStmtCacheSize", "250");
                config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

                this.hikariDataSource = new HikariDataSource(config);
                Log.info("DB Connection Pool has been created.");
                return true;
            } catch (Exception e) {
                Log.error("DB Connection Pool creation failed: " + e.getMessage(), e);
                return false;
            }
        }
    }

    private static @NotNull HikariConfig getHikariConfig() {
        Config serverConfig = Config.getInstance();
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(serverConfig.getJdbcUrl());
        config.setDriverClassName(serverConfig.getDbDriver());
        config.setUsername(serverConfig.getDbUser());
        config.setPassword(serverConfig.getDbPassword());
        config.setMinimumIdle(serverConfig.getDbMinConnections());
        config.setMaximumPoolSize(serverConfig.getDbMaxConnections());
        config.setConnectionTimeout(serverConfig.getDbConnectionTimeout());
        config.setLeakDetectionThreshold(serverConfig.getDbLeakDetectionThreshold());
        config.setIdleTimeout(serverConfig.getDbIdleTimeout());
        return config;
    }

    public Connection getConnection() throws SQLException {
        if (hikariDataSource == null) {
            throw new SQLException("Connection pool is not initialized.");
        }
        return this.hikariDataSource.getConnection();
    }


    public ArrayList<HashMap<String, Object>> convertResultSetToList(ResultSet rs) {
        ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
        try {
            ResultSetMetaData resultSetMetaData = rs.getMetaData();
            int count = resultSetMetaData.getColumnCount();
            while (rs.next()) {
                HashMap<String, Object> map = new HashMap<>();
                int index = 1;
                while (index <= count) {
                    int type = resultSetMetaData.getColumnType(index);
                    String name = resultSetMetaData.getColumnName(index);
                    switch (type) {
                        case -5: {
                            map.put(name, rs.getLong(index));
                            break;
                        }
                        case 6: {
                            map.put(name, rs.getFloat(index));
                            break;
                        }
                        case 12, 1: {
                            map.put(name, rs.getString(index));
                            break;
                        }
                        case 4: {
                            map.put(name, rs.getInt(index));
                            break;
                        }
                        case 16: {
                            map.put(name, rs.getBoolean(index));
                            break;
                        }
                        case -7: {
                            map.put(name, rs.getByte(index));
                            break;
                        }

                        default: {
                            map.put(name, rs.getObject(index));
                            break;
                        }
                    }
                    ++index;
                }
                list.add(map);
            }
            rs.close();
        } catch (SQLException sQLException) {
            Log.error("convertResultSetToList ex: " + sQLException.getMessage());
        }
        return list;
    }

    public void shutdown() {
        try {
            if (this.hikariDataSource != null) {
                this.hikariDataSource.close();
                Log.info("DB Connection Pool is shutting down.");
            }
        } catch (Exception e) {
            Log.warn("Error shutting down DB Connection Pool: " + e.getMessage());
        }
    }

    public int update(String sql, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            return stmt.executeUpdate();
        } catch (SQLException e) {
            Log.error("update() EXCEPTION: " + e.getMessage(), e);
            return -1;
        }
    }
}

