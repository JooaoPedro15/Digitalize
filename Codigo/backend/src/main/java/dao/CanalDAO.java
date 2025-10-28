package dao;

import model.Canal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CanalDAO
{
    public long insert(Canal c) throws SQLException
    {
        String sql = "INSERT INTO midiasocial.canal (empresa_cnpj, plataforma, canal_identificador) VALUES (?,?,?) RETURNING canal_id";
        try (Connection conn = DAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setString(1, c.getEmpresa_cnpj());
            ps.setString(2, c.getPlataforma());
            ps.setString(3, c.getCanal_identificador());
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

    public List<Canal> listByEmpresa(String cnpj) throws SQLException
    {
        String sql = "SELECT canal_id, empresa_cnpj, plataforma, canal_identificador FROM midiasocial.canal WHERE empresa_cnpj = ? ORDER BY canal_id";
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
}
