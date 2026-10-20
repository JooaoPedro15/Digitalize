package com.dao;

import com.config.EnvConfig;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DAO {

    public static Connection getConnection() throws SQLException {
        // Provedores como Neon e Render entregam uma unica URL postgresql://usuario:senha@host/banco
        String databaseUrl = EnvConfig.get("DATABASE_URL");
        if (databaseUrl != null && !databaseUrl.isBlank()) {
            return conectarPorDatabaseUrl(databaseUrl.trim());
        }

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

    /**
     * Converte postgresql://usuario:senha@host:porta/banco?params para o formato JDBC.
     */
    private static Connection conectarPorDatabaseUrl(String databaseUrl) throws SQLException {
        if (databaseUrl.startsWith("jdbc:")) {
            return DriverManager.getConnection(databaseUrl);
        }

        URI uri = URI.create(databaseUrl.replaceFirst("^postgres(ql)?://", "http://"));
        String user = null;
        String pass = null;
        if (uri.getRawUserInfo() != null) {
            String[] partes = uri.getRawUserInfo().split(":", 2);
            user = decode(partes[0]);
            pass = partes.length > 1 ? decode(partes[1]) : "";
        }

        String jdbcUrl = "jdbc:postgresql://" + uri.getHost()
                + (uri.getPort() > 0 ? ":" + uri.getPort() : "")
                + uri.getRawPath()
                + (uri.getRawQuery() != null ? "?" + uri.getRawQuery() : "");

        return DriverManager.getConnection(jdbcUrl, user, pass);
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
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
