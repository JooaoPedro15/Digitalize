package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Classe DAO
 * -------------------------
 * Esta classe é responsável por gerenciar a conexão com o banco de dados PostgreSQL.
 * Segue o padrão DAO (Data Access Object), centralizando a configuração
 * da conexão para que outras classes possam utilizá-la sem duplicar código.
 *
 * Todas as classes que acessam o banco (como EmpresaDAO, CanalDAO, etc.)
 * utilizarão esta classe para abrir e fechar conexões.
 */
public class DAO {

    // Nome do driver JDBC para PostgreSQL
    private static final String DRIVER_NAME = "org.postgresql.Driver";

    // Configurações do servidor e banco
    private static final String SERVER_NAME = "localhost"; // endereço do servidor (localhost = máquina local)
    private static final int PORTA = 5432;                 // porta padrão do PostgreSQL
    private static final String DATABASE = "midiasocial";  // nome do banco (schema definido no script SQL)

    // URL completa de conexão com o banco PostgreSQL
    private static final String URL =
            "jdbc:postgresql://" + SERVER_NAME + ":" + PORTA + "/" + DATABASE;

    // Credenciais de acesso ao banco
    private static final String USER = "ti2cc";    // usuário configurado no PostgreSQL
    private static final String PASSWORD = "ti@cc"; // senha correspondente

    /** SERÁ SE PRECISA DE UM COMANDO ESPECIFICO PARA PODER ACESSAR COM PERFIS DIFERENTES? */



    /**
     * Método responsável por abrir a conexão com o banco de dados PostgreSQL.
     *
     * @return objeto Connection ativo se a conexão for bem-sucedida, ou null se falhar.
     */
    public static Connection getConnection() {
        try {
            // Carrega o driver JDBC do PostgreSQL
            Class.forName(DRIVER_NAME);

            // Tenta estabelecer a conexão
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Conexão com o banco de dados estabelecida com sucesso!");
            return connection;

        } catch (ClassNotFoundException e) {
            // Caso o driver não seja encontrado (verificar dependência no pom.xml)
            System.err.println("Erro: Driver JDBC do PostgreSQL não encontrado.");
            e.printStackTrace();

        } catch (SQLException e) {
            // Caso ocorra falha ao tentar conectar (usuário, senha ou URL incorretos)
            System.err.println("Erro ao conectar ao banco de dados PostgreSQL. Verifique as configurações.");
            e.printStackTrace();
        }

        // Retorna null se não conseguir abrir a conexão
        return null;
    }

    /**
     * Método utilitário para fechar uma conexão aberta com segurança.
     *
     * @param conn a conexão que será fechada.
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("Conexão com o banco de dados fechada com sucesso.");
            } catch (SQLException e) {
                System.err.println("Erro ao fechar a conexão com o banco de dados.");
                e.printStackTrace();
            }
        }
    }
}
