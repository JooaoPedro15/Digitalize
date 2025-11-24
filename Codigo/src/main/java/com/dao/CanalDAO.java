package com.dao;

import com.model.Canal;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) responsável por executar operações de CRUD
 * na tabela midiasocial.canal.
 *
 * Esta classe encapsula todo o acesso ao banco para a entidade Canal,
 * mantendo a separação entre regras de negócio (Service)
 * e regras de persistência (DAO).
 */
public class CanalDAO {

    /**
     * Insere um novo canal e retorna o ID gerado.
     *
     * @param c objeto Canal preenchido
     * @return canal_id gerado ou -1 se falhar
     */
    public long insert(Canal c) throws SQLException {
        String sql = """
                INSERT INTO midiasocial.canal 
                (empresa_cnpj, plataforma, canal_identificador)
                VALUES (?, ?, ?)
                RETURNING canal_id
                """;

        try (Connection conn = DAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, c.getEmpresa_cnpj());
            ps.setString(2, c.getPlataforma());
            ps.setString(3, c.getCanal_identificador());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }

        return -1;
    }

    /**
     * Lista todos os canais pertencentes a uma empresa.
     *
     * @param cnpj identificador da empresa
     * @return lista de objetos Canal
     */
    public List<Canal> listByEmpresa(String cnpj) throws SQLException {
        String sql = """
                SELECT canal_id, empresa_cnpj, plataforma, canal_identificador
                FROM midiasocial.canal
                WHERE empresa_cnpj = ?
                ORDER BY canal_id
                """;

        List<Canal> out = new ArrayList<>();

        try (Connection conn = DAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, cnpj);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Canal c = new Canal();
                    c.setCanal_id(rs.getLong("canal_id"));
                    c.setEmpresa_cnpj(rs.getString("empresa_cnpj"));
                    c.setPlataforma(rs.getString("plataforma"));
                    c.setCanal_identificador(rs.getString("canal_identificador"));
                    out.add(c);
                }
            }
        }

        return out;
    }

    /**
     * Atualiza os dados de um canal existente.
     *
     * @param x objeto Canal com dados atualizados
     * @return true se ocorreu a alteração
     */
    public boolean update(Canal x) throws SQLException {
        String sql = """
                UPDATE midiasocial.canal
                SET empresa_cnpj = ?, plataforma = ?, canal_identificador = ?
                WHERE canal_id = ?
                """;

        try (Connection conn = DAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, x.getEmpresa_cnpj());
            ps.setString(2, x.getPlataforma());
            ps.setString(3, x.getCanal_identificador());
            ps.setLong(4, x.getCanal_id());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Remove um canal pelo seu ID.
     *
     * @param canal_id identificador do canal
     * @return true se o registro foi removido
     */
    public boolean remove(long canal_id) throws SQLException {
        String sql = "DELETE FROM midiasocial.canal WHERE canal_id = ?";

        try (Connection conn = DAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, canal_id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Busca um canal específico pelo ID.
     *
     * @param canal_id id do canal
     * @return objeto Canal ou null se não encontrado
     */
    public Canal get(long canal_id) throws SQLException {
        String sql = """
                SELECT canal_id, empresa_cnpj, plataforma, canal_identificador
                FROM midiasocial.canal
                WHERE canal_id = ?
                """;

        try (Connection conn = DAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, canal_id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Canal c = new Canal();
                    c.setCanal_id(rs.getLong("canal_id"));
                    c.setEmpresa_cnpj(rs.getString("empresa_cnpj"));
                    c.setPlataforma(rs.getString("plataforma"));
                    c.setCanal_identificador(rs.getString("canal_identificador"));
                    return c;
                }
            }
        }

        return null;
    }

    /**
     * Lista todos os canais cadastrados.
     *
     * @return lista completa de canais
     */
    public List<Canal> listar() throws SQLException {
        String sql = """
                SELECT canal_id, empresa_cnpj, plataforma, canal_identificador
                FROM midiasocial.canal
                ORDER BY canal_id
                """;

        List<Canal> out = new ArrayList<>();

        try (Connection conn = DAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Canal c = new Canal();
                c.setCanal_id(rs.getLong("canal_id"));
                c.setEmpresa_cnpj(rs.getString("empresa_cnpj"));
                c.setPlataforma(rs.getString("plataforma"));
                c.setCanal_identificador(rs.getString("canal_identificador"));
                out.add(c);
            }
        }

        return out;
    }
}
