package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DAO
{
    private static final String DEFAULT_URL = System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/digitalize");
    private static final String DEFAULT_USER = System.getenv().getOrDefault("DB_USER", "postgres");
    private static final String DEFAULT_PASS = System.getenv().getOrDefault("DB_PASS", "postgres");

    public static Connection getConnection()
    {
        try
        {
            return DriverManager.getConnection(DEFAULT_URL, DEFAULT_USER, DEFAULT_PASS);
        }
        catch (SQLException e)
        {
            throw new RuntimeException("DB connection error", e);
        }
    }

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
                // ignora
            }
        }
    }
}
