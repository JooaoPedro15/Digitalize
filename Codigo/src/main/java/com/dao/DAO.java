package com.dao;

import com.config.EnvConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DAO {

    public static Connection getConnection() throws SQLException {
        // Tenta pegar as variáveis de ambiente do Azure
        String url = EnvConfig.get("DB_URL");
        String user = EnvConfig.get("DB_USER");
        String pass = EnvConfig.get("DB_PASS");

        // Fallback para execucao local sem expor senha no repositorio.
        if (url == null || url.isEmpty()) {
            url = "jdbc:postgresql://localhost:5432/digitalize";
            if (user == null || user.isEmpty()) {
                user = "postgres";
            }
            if (pass == null) {
                pass = "";
            }
        }

        return DriverManager.getConnection(url, user, pass);
    }

    public static void close(Connection c) {
        if (c != null) {
            try {
                c.close();
            } catch (SQLException e) {
                // Silencioso
            }
        }
    }
}
