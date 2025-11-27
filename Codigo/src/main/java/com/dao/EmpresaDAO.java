package com.dao;

import com.model.Empresa;
    import java.sql.*;
    import java.util.ArrayList;
    import java.util.List;

    /**
     * DAO responsável pelo acesso à tabela "empresa" no esquema "midiasocial".
     * 
     * Encapsula todas as operações CRUD:
     * - insert (cadastrar empresa)
     * - update (editar ou alterar status)
     * - remove (excluir pelo CNPJ)
     * - get (buscar empresa única)
     * - listar (listar todas)
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
                    (cnpj, nome_fantasia, razao_social, segmento,
                     endereco, status, responsavel_email, email_contato)
                    VALUES (?,?,?,?,?,?,?,?)
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
                ps.setString(7, x.getResponsavel_email());
                ps.setString(8, x.getEmail_contato());

                return ps.executeUpdate() > 0;
            }
            catch (SQLException e)
            {
                // Imprime para facilitar debug no servidor
                e.printStackTrace();
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
                    SET nome_fantasia=?, razao_social=?, segmento=?,
                        endereco=?, status=?, responsavel_email=?, email_contato=?
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
                ps.setString(6, x.getResponsavel_email());
                ps.setString(7, x.getEmail_contato());
                ps.setString(8, x.getCnpj());

                return ps.executeUpdate() > 0;
            }
            catch (SQLException e)
            {
                e.printStackTrace();
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
                e.printStackTrace();
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
                           endereco, status, responsavel_email, email_contato
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
                        e.setResponsavel_email(rs.getString("responsavel_email"));
                        e.setEmail_contato(rs.getString("email_contato"));
                        return e;
                    }
                }
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }

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
                           endereco, status, responsavel_email, email_contato
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
                    e.setResponsavel_email(rs.getString("responsavel_email"));
                    e.setEmail_contato(rs.getString("email_contato"));

                    out.add(e);
                }
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }

            return out;
        }
    }
