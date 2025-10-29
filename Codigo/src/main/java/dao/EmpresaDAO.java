package dao;

import model.Empresa;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmpresaDAO
{
    public boolean insert(Empresa x)
    {
        String sql = "INSERT INTO midiasocial.empresa (cnpj, nome_fantasia, razao_social, segmento, endereco, status) VALUES (?,?,?,?,?,?)";
        try (Connection c = DAO.getConnection(); PreparedStatement ps = c.prepareStatement(sql))
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

    public boolean update(Empresa x)
    {
        String sql = "UPDATE midiasocial.empresa SET nome_fantasia=?, razao_social=?, segmento=?, endereco=?, status=? WHERE cnpj=?";
        try (Connection c = DAO.getConnection(); PreparedStatement ps = c.prepareStatement(sql))
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

    public boolean remove(String cnpj)
    {
        String sql = "DELETE FROM midiasocial.empresa WHERE cnpj=?";
        try (Connection c = DAO.getConnection(); PreparedStatement ps = c.prepareStatement(sql))
        {
            ps.setString(1, cnpj);
            return ps.executeUpdate() > 0;
        }
        catch (SQLException e)
        {
            return false;
        }
    }

    public Empresa get(String cnpj)
    {
        String sql = "SELECT cnpj, nome_fantasia, razao_social, segmento, endereco, status FROM midiasocial.empresa WHERE cnpj=?";
        try (Connection c = DAO.getConnection(); PreparedStatement ps = c.prepareStatement(sql))
        {
            ps.setString(1, cnpj);
            try (ResultSet rs = ps.executeQuery())
            {
                if (rs.next())
                {
                    Empresa x = new Empresa();
                    x.setCnpj(rs.getString("cnpj"));
                    x.setNome_fantasia(rs.getString("nome_fantasia"));
                    x.setRazao_social(rs.getString("razao_social"));
                    x.setSegmento(rs.getString("segmento"));
                    x.setEndereco(rs.getString("endereco"));
                    x.setStatus(rs.getString("status"));
                    return x;
                }
            }
        }
        catch (SQLException e)
        {
            // ignore
        }
        return null;
    }

    public List<Empresa> listar()
    {
        List<Empresa> out = new ArrayList<>();
        String sql = "SELECT cnpj, nome_fantasia, razao_social, segmento, endereco, status FROM midiasocial.empresa ORDER BY nome_fantasia";
        try (Connection c = DAO.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery())
        {
            while (rs.next())
            {
                Empresa x = new Empresa();
                x.setCnpj(rs.getString("cnpj"));
                x.setNome_fantasia(rs.getString("nome_fantasia"));
                x.setRazao_social(rs.getString("razao_social"));
                x.setSegmento(rs.getString("segmento"));
                x.setEndereco(rs.getString("endereco"));
                x.setStatus(rs.getString("status"));
                out.add(x);
            }
        }
        catch (SQLException e)
        {
            // ignore
        }
        return out;
    }
}
