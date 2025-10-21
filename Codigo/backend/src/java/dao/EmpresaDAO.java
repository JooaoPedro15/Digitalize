package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.Empresa;

/**
 * Classe EmpresaDAO
 * -------------------------
 * Esta classe é responsável por realizar as operações de acesso a dados
 * (CRUD) da entidade Empresa no banco de dados PostgreSQL.
 *
 * CRUD = Create (inserir), Read (consultar), Update (atualizar), Delete (excluir)
 *
 * A classe utiliza a conexão gerenciada pela classe DAO.
 */
public class EmpresaDAO {

    private Connection connection;

    /**
     * Construtor padrão.
     * Ao ser instanciada, a classe abre uma conexão com o banco de dados.
     */
    public EmpresaDAO() {
        this.connection = DAO.getConnection();
    }

    /**
     * Método responsável por inserir uma nova empresa no banco de dados.
     *
     * @param empresa objeto Empresa a ser inserido.
     * @return true se o registro for inserido com sucesso, false caso contrário.
     */
    public boolean inserir(Empresa empresa) {
        String sql = "INSERT INTO empresa (cnpj, nome_fantasia, razao_social, segmento, endereco, status) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, empresa.getCnpj());
            stmt.setString(2, empresa.getNomeFantasia());
            stmt.setString(3, empresa.getRazaoSocial());
            stmt.setString(4, empresa.getSegmento());
            stmt.setString(5, empresa.getEndereco());
            stmt.setString(6, empresa.getStatus());

            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Erro ao inserir empresa no banco de dados.");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Método responsável por buscar uma empresa específica pelo CNPJ.
     *
     * @param cnpj identificador único da empresa.
     * @return objeto Empresa correspondente ou null se não encontrado.
     */
    public Empresa buscarPorCnpj(String cnpj) {
        String sql = "SELECT * FROM empresa WHERE cnpj = ?";
        Empresa empresa = null;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, cnpj);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                empresa = new Empresa(
                    rs.getString("cnpj"),
                    rs.getString("nome_fantasia"),
                    rs.getString("razao_social"),
                    rs.getString("segmento"),
                    rs.getString("endereco"),
                    rs.getString("status")
                );
            }
            rs.close();

        } catch (SQLException e) {
            System.err.println("Erro ao buscar empresa pelo CNPJ.");
            e.printStackTrace();
        }

        return empresa;
    }

    /**
     * Método responsável por retornar todas as empresas cadastradas no banco.
     *
     * @return lista de objetos Empresa.
     */
    public List<Empresa> listarTodas() {
        String sql = "SELECT * FROM empresa ORDER BY nome_fantasia";
        List<Empresa> empresas = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Empresa empresa = new Empresa(
                    rs.getString("cnpj"),
                    rs.getString("nome_fantasia"),
                    rs.getString("razao_social"),
                    rs.getString("segmento"),
                    rs.getString("endereco"),
                    rs.getString("status")
                );
                empresas.add(empresa);
            }

        } catch (SQLException e) {
            System.err.println("Erro ao listar empresas.");
            e.printStackTrace();
        }

        return empresas;
    }

    /**
     * Método responsável por atualizar os dados de uma empresa existente.
     *
     * @param empresa objeto Empresa com os dados atualizados.
     * @return true se a atualização for bem-sucedida, false caso contrário.
     */
    public boolean atualizar(Empresa empresa) {
        String sql = "UPDATE empresa SET nome_fantasia = ?, razao_social = ?, segmento = ?, endereco = ?, status = ? "
                   + "WHERE cnpj = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, empresa.getNomeFantasia());
            stmt.setString(2, empresa.getRazaoSocial());
            stmt.setString(3, empresa.getSegmento());
            stmt.setString(4, empresa.getEndereco());
            stmt.setString(5, empresa.getStatus());
            stmt.setString(6, empresa.getCnpj());

            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Erro ao atualizar empresa.");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Método responsável por excluir uma empresa com base no CNPJ.
     *
     * @param cnpj identificador único da empresa a ser excluída.
     * @return true se a exclusão for bem-sucedida, false caso contrário.
     */
    public boolean excluir(String cnpj) {
        String sql = "DELETE FROM empresa WHERE cnpj = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, cnpj);
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Erro ao excluir empresa.");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Método responsável por fechar a conexão com o banco de dados.
     * Deve ser chamado ao encerrar o uso do DAO.
     */
    public void fecharConexao() {
        DAO.closeConnection(connection);
    }
}
