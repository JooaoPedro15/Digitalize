package com.digitalize.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Classe base para acesso ao banco de dados.
 * 
 * Responsabilidades:
 * 1. Criar conexões com o banco usando JDBC.
 * 2. Fornecer método para fechar conexões de forma segura.
 * 
 * Observações:
 * - As credenciais e URL do banco podem ser definidas via variáveis de ambiente:
 *   DB_URL, DB_USER, DB_PASS
 * - Caso não existam, são usados valores padrão (localhost / postgres).
 */
public class DAO {

    // URL de conexão com o banco (padrão ou via variável de ambiente)
    private static final String DEFAULT_URL = System.getenv().getOrDefault(
            "DB_URL", "jdbc:postgresql://localhost:5432/digitalize");

    // Usuário do banco (padrão ou via variável de ambiente)
    private static final String DEFAULT_USER = System.getenv().getOrDefault(
            "DB_USER", "postgres");

    // Senha do banco (padrão ou via variável de ambiente)
    private static final String DEFAULT_PASS = System.getenv().getOrDefault(
            "DB_PASS", "postgres");

    /**
     * Retorna uma nova conexão com o banco de dados.
     * @return Connection válida
     * @throws RuntimeException caso ocorra algum erro ao conectar
     */
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(DEFAULT_URL, DEFAULT_USER, DEFAULT_PASS);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao conectar ao banco de dados", e);
        }
    }

    /**
     * Fecha a conexão passada como parâmetro, se não for nula.
     * Qualquer SQLException durante o fechamento é ignorada.
     * @param c Connection a ser fechada
     */
    public static void close(Connection c) {
        if (c != null) {
            try {
                c.close();
            } catch (SQLException e) {
                // Ignora exceção ao fechar a conexão
            }
        }
    }
}
