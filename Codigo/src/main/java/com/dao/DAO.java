package com.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DAO {

    public static Connection getConnection() throws SQLException {
        // Tenta pegar as variáveis de ambiente do Azure
        String url = System.getenv("DB_URL");
        String user = System.getenv("DB_USER");
        String pass = System.getenv("DB_PASS");

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
