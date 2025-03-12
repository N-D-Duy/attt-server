package org.duynguyen.constants;

public class SQLStatement {
    public static final String GET_USERS = "SELECT * FROM users WHERE username = ?";
    public static final String SAVE_DATA = "UPDATE `users` SET `ip_address` = ? WHERE `id` = ? LIMIT 1;";
    public static final String REGISTER = "INSERT INTO users (username, password) VALUES (?, ?);";
}
