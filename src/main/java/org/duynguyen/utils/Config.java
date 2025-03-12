package org.duynguyen.utils;


import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import lombok.Getter;

@Getter
public class Config {
    @Getter
    private static final Config instance = new Config();

    private boolean showLog;
    private int port;

    //mysql
    private String dbHost;
    private int dbPort;
    private String dbUser;
    private String dbPassword;
    private String dbName;
    private String dbDriver;
    private int dbMinConnections;
    private int dbMaxConnections;
    private int dbConnectionTimeout;
    private int dbLeakDetectionThreshold;
    private int dbIdleTimeout;

    public boolean load(){
        try{
            FileInputStream input = new FileInputStream(new File("config.properties"));
            Properties props = new Properties();
            props.load(new InputStreamReader(input, StandardCharsets.UTF_8));
            props.forEach((t, u) -> {
                Log.info(String.format("Config - %s: %s", t, u));
            });
            port = Integer.parseInt(props.getProperty("server.port"));
            dbDriver = props.getProperty("db.driver");
            dbHost = props.getProperty("db.host");
            dbPort = Integer.parseInt(props.getProperty("db.port"));
            dbUser = props.getProperty("db.user");
            dbPassword = props.getProperty("db.password");
            dbName = props.getProperty("db.dbname");
            dbMaxConnections = Integer.parseInt(props.getProperty("db.maxconnections"));
            dbMinConnections = Integer.parseInt(props.getProperty("db.minconnections"));
            dbConnectionTimeout = Integer.parseInt(props.getProperty("db.connectionTimeout"));
            dbLeakDetectionThreshold = Integer.parseInt(props.getProperty("db.leakDetectionThreshold"));
            dbIdleTimeout = Integer.parseInt(props.getProperty("db.idleTimeout"));
            showLog = Boolean.parseBoolean(props.getProperty("server.log.display"));
        } catch (Exception e){
            Log.error("Config file not found");
            return false;
        }
        return true;
    }

    public String getJdbcUrl() {
        return "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName;
    }
}
