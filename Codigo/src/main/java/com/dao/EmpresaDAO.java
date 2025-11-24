package com.dao;

import com.model.Empresa;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO responsável pelo acesso à tabela "empresa" no esquema "midiasocial".
 *
 * Este DAO encapsula todas as operações CRUD:
 * - insert
 * - update
 * - remove
 * - get
 * - listar
 *
 * Toda conexão é obtida via DAO.getConnection(), garantindo centralização
 * e fácil manutenção da camada de persistência.
 */
public class EmpresaDAO
{
    /**
     * Insere uma nova empresa no banco de dados.
     */
    public boolean insert(Empresa x)
    {
        String sql = """
                INSERT INTO midiasocial.empresa
                (cnpj, nome_fantasia, razao_social, segmento, endereco, status)
                VALUES (?,?,?,?,?,?)
                """;

        try (Connection c = DAO.getConnection();
             PreparedStatement ps = c.prepareStatement(sql))
        {
            ps.setString(1, x.getCnpj());
            ps.setString(2, x.getNome_fantasia());
            ps.setString(3, x.getRazao_social());
            ps.setString(4, x.getSegmento());
            ps.setString(5, x.getEndereco());
            ps.setString(6, x.getStatus());

            return ps.executeUpdate() > 0;
        }
        catch (SQLException e)
        {
            return false;
        }
    }

    /**
     * Atualiza os dados de uma empresa existente.
     */
    public boolean update(Empresa x)
    {
        String sql = """
                UPDATE midiasocial.empresa
                SET nome_fantasia=?, razao_social=?, segmento=?, endereco=?, status=?
                WHERE cnpj=?
                """;

        try (Connection c = DAO.getConnection();
             PreparedStatement ps = c.prepareStatement(sql))
        {
            ps.setString(1, x.getNome_fantasia());
            ps.setString(2, x.getRazao_social());
            ps.setString(3, x.getSegmento());
            ps.setString(4, x.getEndereco());
            ps.setString(5, x.getStatus());
            ps.setString(6, x.getCnpj());

            return ps.executeUpdate() > 0;
        }
        catch (SQLException e)
        {
            return false;
        }
    }

    /**
     * Remove uma empresa pelo CNPJ.
     */
    public boolean remove(String cnpj)
    {
        String sql = "DELETE FROM midiasocial.empresa WHERE cnpj=?";

        try (Connection c = DAO.getConnection();
             PreparedStatement ps = c.prepareStatement(sql))
        {
            ps.setString(1, cnpj);
            return ps.executeUpdate() > 0;
        }
        catch (SQLException e)
        {
            return false;
        }
    }

    /**
     * Retorna uma empresa específica pelo CNPJ.
     */
    public Empresa get(String cnpj)
    {
        String sql = """
                SELECT cnpj, nome_fantasia, razao_social, segmento,
                       endereco, status
                FROM midiasocial.empresa
                WHERE cnpj=?
                """;

        try (Connection c = DAO.getConnection();
             PreparedStatement ps = c.prepareStatement(sql))
        {
            ps.setString(1, cnpj);

            try (ResultSet rs = ps.executeQuery())
            {
                if (rs.next())
                {
                    Empresa e = new Empresa();
                    e.setCnpj(rs.getString("cnpj"));
                    e.setNome_fantasia(rs.getString("nome_fantasia"));
                    e.setRazao_social(rs.getString("razao_social"));
                    e.setSegmento(rs.getString("segmento"));
                    e.setEndereco(rs.getString("endereco"));
                    e.setStatus(rs.getString("status"));
                    return e;
                }
            }
        }
        catch (SQLException ignored) {}

        return null;
    }

    /**
     * Lista todas as empresas cadastradas.
     */
    public List<Empresa> listar()
    {
        List<Empresa> out = new ArrayList<>();

        String sql = """
                SELECT cnpj, nome_fantasia, razao_social, segmento,
                       endereco, status
                FROM midiasocial.empresa
                ORDER BY nome_fantasia
                """;

        try (Connection c = DAO.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery())
        {
            while (rs.next())
            {
                Empresa e = new Empresa();
                e.setCnpj(rs.getString("cnpj"));
                e.setNome_fantasia(rs.getString("nome_fantasia"));
                e.setRazao_social(rs.getString("razao_social"));
                e.setSegmento(rs.getString("segmento"));
                e.setEndereco(rs.getString("endereco"));
                e.setStatus(rs.getString("status"));

                out.add(e);
            }
        }
        catch (SQLException ignored) {}

        return out;
    }
}
