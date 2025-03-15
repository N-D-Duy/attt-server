package org.duynguyen.models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

import org.duynguyen.constants.SQLStatement;
import org.duynguyen.database.DbManager;
import org.duynguyen.network.Service;
import org.duynguyen.network.Session;
import org.duynguyen.server.ServerManager;
import org.duynguyen.utils.Log;
import org.duynguyen.utils.Utils;

import lombok.Getter;


public class Client {
    public Session session;
    @Getter
    public Service service;
    public int id;
    public String username;
    public String password;
    public boolean isLoadFinish;
    public boolean isCleaned;
    public ArrayList<String> IPAddress;
    private boolean saving;


    public Client(Session client, String username, String password) {
        this.session = client;
        this.service = client.getService();
        this.username = username;
        this.password = password;
    }

    public void cleanUp() {
        this.isCleaned = true;
        this.session = null;
        this.service = null;
        Log.debug("clean user " + this.username);
    }

    public void login() {
        try {
            if (username.equals("-1") && password.equals("asfaf")) {
                Log.info("Login admin");
                return;
            }
            Pattern p = Pattern.compile("^[a-zA-Z0-9]+$|^[a-zA-Z0-9._%+-]+@gmail\\.com$");
            Matcher m1 = p.matcher(username);
            if (!m1.find()) {
                service.serverDialog("Account name contains invalid characters!");
                return;
            }
            HashMap<String, Object> map = getClientDataMap();
            if (map == null) {
                service.serverDialog("Invalid username or password.");
                return;
            }

            this.id = (int) (map.get("id"));
            this.IPAddress = new ArrayList<>();
            Object obj = map.get("ip_address");
            if (obj != null) {
                String str = obj.toString();
                if (!str.isEmpty()) {
                    JSONArray jArr = (JSONArray) JSONValue.parse(str);
                    for (Object o : jArr) {
                        IPAddress.add(o.toString());
                    }
                }
            }
            if (!IPAddress.contains(session.IPAddress)) {
                IPAddress.add(session.IPAddress);
            }

            synchronized (ServerManager.getClients()) {
                Client existingClient = ServerManager.findClientByUsername(this.username);
                if (existingClient != null && !existingClient.isCleaned) {
                    service.serverDialog("Account is already logged in.");
                    if (existingClient.session != null && existingClient.session.getService() != null) {
                        existingClient.service.serverDialog("Someone is trying to log in to your account!");
                    }
                    Utils.setTimeout(() -> {
                        try {
                            if (!existingClient.isCleaned) {
                                existingClient.session.closeMessage();
                            }
                        } catch (Exception e) {
                            Log.error("Close old session err", e);
                        } finally {
                            ServerManager.removeClient(existingClient);
                        }
                    }, 1000);

                    Utils.setTimeout(() -> {
                        try {
                            if (!this.isCleaned) {
                                this.session.closeMessage();
                            }
                        } catch (Exception e) {
                            Log.error("Close current session err", e);
                        } finally {
                            ServerManager.removeClient(this);
                        }
                    }, 1000);
                    return;
                }
                ServerManager.addClient(this);
            }
            this.isLoadFinish = true;
        } catch (Exception ex) {
            Log.error("login err", ex);
        }
    }

    public void register() {
        try {
            HashMap<String, Object> map = getClientDataMap();
            if (map != null) {
                service.serverMessage("Email already exists.");
                return;
            }

            Pattern p = Pattern.compile("^[a-zA-Z0-9]+$|^[a-zA-Z0-9._%+-]+@gmail\\.com$");
            Matcher m1 = p.matcher(username);
            if (!m1.find()) {
                service.serverMessage("Invalid username or email.");
                return;
            }
            try (Connection conn = DbManager.getInstance().getConnection();
                 PreparedStatement stmt = conn
                         .prepareStatement(SQLStatement.REGISTER)) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
//                    service.registerSuccess();
                    Log.info("Register success");
                } else {
                    Log.info("Register failed");
                }
            }
        } catch (Exception e) {
            Log.error("register err", e);
        }
    }


    @SuppressWarnings("unchecked")
    public synchronized void saveData() {
        try {
            Log.info("saving data user: " + username);
            if (isLoadFinish && !saving) {
                saving = true;
                try {
                    JSONArray list = new JSONArray();
                    list.addAll(IPAddress);
                    String jList = list.toJSONString();

                    try (Connection conn = DbManager.getInstance().getConnection()) {
                        try (PreparedStatement stmt = conn.prepareStatement(
                                SQLStatement.SAVE_DATA)) {
                            stmt.setString(1, jList);
                            stmt.setInt(2, this.id);
                            stmt.executeUpdate();
                        }
                    }
                } finally {
                    saving = false;
                }
            }
        } catch (Exception e) {
            Log.error("save data user: " + username);
        }
    }

    public HashMap<String, Object> getClientDataMap() {
        try (
                Connection conn = DbManager.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(SQLStatement.GET_USERS, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY)
        ) {
            stmt.setString(1, this.username);

            try (ResultSet data = stmt.executeQuery()) {
                ArrayList<HashMap<String, Object>> list = DbManager.getInstance().convertResultSetToList(data);
                if (list.isEmpty()) {
                    return null;
                }
                HashMap<String, Object> map = list.get(0);
                if (map != null) {
                    String passwordHash = (String) map.get("password");
                    if (!passwordHash.equals(password)) {
                        return null;
                    }
                }
                return map;
            }
        } catch (SQLException e) {
            Log.error("getUserMap() err", e);
        }
        return null;
    }
}
