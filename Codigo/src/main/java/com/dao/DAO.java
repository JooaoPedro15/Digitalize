package com.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Classe base para acesso ao banco de dados.
 *
 * Variaveis de ambiente (opcionais):
 *  - DB_URL  (ex.: jdbc:postgresql://localhost:5433/digitalize)
 *  - DB_USER (ex.: postgres)
 *  - DB_PASS (ex.: postgres)
 */
public class DAO
{
    /**
     * Retorna uma nova conexao com o banco de dados.
     * Lanca SQLException para o chamador tratar/logar corretamente.
     */
    public static Connection getConnection() throws SQLException
    {
        String url  = System.getenv().getOrDefault("DB_URL",  "jdbc:postgresql://localhost:5433/digitalize");
        String user = System.getenv().getOrDefault("DB_USER", "postgres");
        String pass = System.getenv().getOrDefault("DB_PASS", "postgres");
        return DriverManager.getConnection(url, user, pass);
    }

    /**
     * Fecha a conexao (ignora excecao no close).
     */
    public static void close(Connection c)
    {
        if (c != null)
        {
            try
            {
                c.close();
            }
            catch (SQLException e)
            {
                // log opcional
            }
        }
    }
}
