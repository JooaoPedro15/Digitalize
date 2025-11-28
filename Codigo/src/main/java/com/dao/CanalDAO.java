package com.dao;

import com.model.Canal;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) responsavel por executar operacoes de CRUD
 * na tabela midiasocial.canal.
 *
 * Esta classe encapsula todo o acesso ao banco para a entidade Canal,
 * mantendo a separacao entre regras de negocio (Service)
 * e regras de persistencia (DAO).
 */
public class CanalDAO
{

    /**
     * Insere um novo canal e retorna o ID gerado.
     *
     * @param c objeto Canal preenchido
     * @return canal_id gerado ou -1 se falhar
     */
    public long insert(Canal c) throws SQLException
    {
        String sql = """
                INSERT INTO midiasocial.canal 
                (empresa_cnpj, plataforma, canal_identificador)
                VALUES (?, ?, ?)
                RETURNING canal_id
                """;

        try (Connection conn = DAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setString(1, c.getEmpresaCnpj());
            ps.setString(2, c.getPlataforma());
            ps.setString(3, c.getCanalIdentificador());

            try (ResultSet rs = ps.executeQuery())
            {
                if (rs.next())
                {
                    return rs.getLong(1);
                }
            }
        }

        return -1;
    }

    /**
     * Lista todos os canais pertencentes a uma empresa.
     *
     * @param cnpj identificador da empresa (CNPJ)
     * @return lista de objetos Canal
     */
    public List<Canal> listByEmpresa(String cnpj) throws SQLException
    {
        String sql = """
                SELECT canal_id, empresa_cnpj, plataforma, canal_identificador
                FROM midiasocial.canal
                WHERE empresa_cnpj = ?
                ORDER BY canal_id
                """;

        List<Canal> out = new ArrayList<>();

        try (Connection conn = DAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setString(1, cnpj);

            try (ResultSet rs = ps.executeQuery())
            {
                while (rs.next())
                {
                    Canal c = new Canal();
                    c.setCanalId(rs.getLong("canal_id"));
                    c.setEmpresaCnpj(rs.getString("empresa_cnpj"));
                    c.setPlataforma(rs.getString("plataforma"));
                    c.setCanalIdentificador(rs.getString("canal_identificador"));
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
     * @return true se ocorreu a alteracao
     */
    public boolean update(Canal x) throws SQLException
    {
        String sql = """
                UPDATE midiasocial.canal
                SET empresa_cnpj = ?, plataforma = ?, canal_identificador = ?
                WHERE canal_id = ?
                """;

        try (Connection conn = DAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setString(1, x.getEmpresaCnpj());
            ps.setString(2, x.getPlataforma());
            ps.setString(3, x.getCanalIdentificador());
            ps.setLong(4, x.getCanalId());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Remove um canal pelo seu ID.
     *
     * @param canal_id identificador do canal
     * @return true se o registro foi removido
     */
    public boolean remove(long canal_id) throws SQLException
    {
        String sql = "DELETE FROM midiasocial.canal WHERE canal_id = ?";

        try (Connection conn = DAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setLong(1, canal_id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Busca um canal especifico pelo ID.
     *
     * @param canal_id id do canal
     * @return objeto Canal ou null se nao encontrado
     */
    public Canal get(long canal_id) throws SQLException
    {
        String sql = """
                SELECT canal_id, empresa_cnpj, plataforma, canal_identificador
                FROM midiasocial.canal
                WHERE canal_id = ?
                """;

        try (Connection conn = DAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setLong(1, canal_id);

            try (ResultSet rs = ps.executeQuery())
            {
                if (rs.next())
                {
                    Canal c = new Canal();
                    c.setCanalId(rs.getLong("canal_id"));
                    c.setEmpresaCnpj(rs.getString("empresa_cnpj"));
                    c.setPlataforma(rs.getString("plataforma"));
                    c.setCanalIdentificador(rs.getString("canal_identificador"));
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
    public List<Canal> listar() throws SQLException
    {
        String sql = """
                SELECT canal_id, empresa_cnpj, plataforma, canal_identificador
                FROM midiasocial.canal
                ORDER BY canal_id
                """;

        List<Canal> out = new ArrayList<>();

        try (Connection conn = DAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery())
        {
            while (rs.next())
            {
                Canal c = new Canal();
                c.setCanalId(rs.getLong("canal_id"));
                c.setEmpresaCnpj(rs.getString("empresa_cnpj"));
                c.setPlataforma(rs.getString("plataforma"));
                c.setCanalIdentificador(rs.getString("canal_identificador"));
                out.add(c);
            }
        }

        return out;
    }

    /**
     * Garante que exista um canal generico para a empresa informada.
     *
     * - Se ja existir um canal com (plataforma, identificador) padrao,
     *   apenas retorna o seu ID.
     * - Se nao existir, cria um novo canal generico e retorna o ID gerado.
     *
     * Este metodo e pensado para cenarios em que voce precisa ter
     * ao menos um canal valido para vincular importacoes de CSV e posts.
     *
     * @param empresaCnpj CNPJ da empresa (somente numeros)
     * @return canal_id do canal generico (existente ou recem-criado), ou -1 se falhar
     */
    public long criarCanalGenericoSeNaoExistir(String empresaCnpj) throws SQLException
    {
        final String plataforma   = "GENERICA";
        final String identificador = "Canal padrao";

        String sqlSelect = """
                SELECT canal_id
                FROM midiasocial.canal
                WHERE empresa_cnpj = ?
                  AND plataforma = ?
                  AND canal_identificador = ?
                """;

        try (Connection conn = DAO.getConnection())
        {
            // 1) Verifica se ja existe um canal generico
            try (PreparedStatement ps = conn.prepareStatement(sqlSelect))
            {
                ps.setString(1, empresaCnpj);
                ps.setString(2, plataforma);
                ps.setString(3, identificador);

                try (ResultSet rs = ps.executeQuery())
                {
                    if (rs.next())
                    {
                        long idExistente = rs.getLong("canal_id");
                        System.out.println("[CanalDAO] Canal generico ja existe. canal_id=" + idExistente);
                        return idExistente;
                    }
                }
            }

            // 2) Nao existe -> cria um novo canal generico
            String sqlInsert = """
                    INSERT INTO midiasocial.canal
                    (empresa_cnpj, plataforma, canal_identificador)
                    VALUES (?, ?, ?)
                    RETURNING canal_id
                    """;

            try (PreparedStatement psIns = conn.prepareStatement(sqlInsert))
            {
                psIns.setString(1, empresaCnpj);
                psIns.setString(2, plataforma);
                psIns.setString(3, identificador);

                try (ResultSet rsIns = psIns.executeQuery())
                {
                    if (rsIns.next())
                    {
                        long novoId = rsIns.getLong(1);
                        System.out.println("[CanalDAO] Canal generico criado. canal_id=" + novoId);
                        return novoId;
                    }
                }
            }
        }

        return -1;
    }
}
