package dao;

import model.Importacao;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ImportacaoDAO
{
    public void upsert(Importacao i) throws SQLException
    {
        String sql = "INSERT INTO midiasocial.importacao " +
                     "(canal_id, importacao_arquivo_original, importacao_periodo_inicio, importacao_periodo_fim, importacao_status) " +
                     "VALUES (?,?,?,?,?) " +
                     "ON CONFLICT (canal_id, importacao_arquivo_original, importacao_periodo_inicio) " +
                     "DO UPDATE SET importacao_periodo_fim = EXCLUDED.importacao_periodo_fim, importacao_status = EXCLUDED.importacao_status";

        try (Connection conn = DAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setLong(1, i.getCanal_id());
            ps.setString(2, i.getImportacao_arquivo_original());
            ps.setObject(3, i.getImportacao_periodo_inicio());
            ps.setObject(4, i.getImportacao_periodo_fim());
            ps.setString(5, i.getImportacao_status());
            ps.executeUpdate();
        }
    }

    public List<Importacao> listByCanal(long canalId) throws SQLException
    {
        String sql = "SELECT * FROM midiasocial.importacao WHERE canal_id = ? ORDER BY importacao_periodo_inicio DESC";
        List<Importacao> out = new ArrayList<>();
        try (Connection conn = DAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setLong(1, canalId);
            try (ResultSet rs = ps.executeQuery())
            {
                while (rs.next())
                {
                    Importacao i = new Importacao();
                    i.setCanal_id(rs.getLong("canal_id"));
                    i.setImportacao_arquivo_original(rs.getString("importacao_arquivo_original"));
                    i.setImportacao_periodo_inicio(rs.getObject("importacao_periodo_inicio", LocalDate.class));
                    i.setImportacao_periodo_fim(rs.getObject("importacao_periodo_fim", LocalDate.class));
                    i.setImportacao_status(rs.getString("importacao_status"));
                    out.add(i);
                }
            }
        }
        return out;
    }


public boolean insert(Importacao x) throws SQLException
{
    upsert(x);
    return true;
}

public boolean update(Importacao x) throws SQLException
{
    upsert(x);
    return true;
}

public boolean remove(long canal_id, String arquivo, java.time.LocalDate inicio) throws SQLException
{
    String sql = "DELETE FROM midiasocial.importacao WHERE canal_id=? AND importacao_arquivo_original=? AND importacao_periodo_inicio=?";
    try (Connection conn = DAO.getConnection(); PreparedStatement ps = conn.prepareStatement(sql))
    {
        ps.setLong(1, canal_id);
        ps.setString(2, arquivo);
        ps.setDate(3, java.sql.Date.valueOf(inicio));
        return ps.executeUpdate() > 0;
    }
}

public Importacao get(long canal_id, String arquivo, java.time.LocalDate inicio) throws SQLException
{
    String sql = "SELECT canal_id, importacao_arquivo_original, importacao_periodo_inicio, importacao_periodo_fim, importacao_status FROM midiasocial.importacao WHERE canal_id=? AND importacao_arquivo_original=? AND importacao_periodo_inicio=?";
    try (Connection conn = DAO.getConnection(); PreparedStatement ps = conn.prepareStatement(sql))
    {
        ps.setLong(1, canal_id);
        ps.setString(2, arquivo);
        ps.setDate(3, java.sql.Date.valueOf(inicio));
        try (ResultSet rs = ps.executeQuery())
        {
            if (rs.next())
            {
                Importacao x = new Importacao();
                x.setCanal_id(rs.getLong("canal_id"));
                x.setImportacao_arquivo_original(rs.getString("importacao_arquivo_original"));
                x.setImportacao_periodo_inicio(rs.getDate("importacao_periodo_inicio").toLocalDate());
                Date f = rs.getDate("importacao_periodo_fim");
                x.setImportacao_periodo_fim(f != null ? f.toLocalDate() : null);
                x.setImportacao_status(rs.getString("importacao_status"));
                return x;
            }
        }
    }
    return null;
}

public List<Importacao> listar() throws SQLException
{
    List<Importacao> out = new ArrayList<>();
    String sql = "SELECT canal_id, importacao_arquivo_original, importacao_periodo_inicio, importacao_periodo_fim, importacao_status FROM midiasocial.importacao ORDER BY canal_id, importacao_periodo_inicio";
    try (Connection conn = DAO.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery())
    {
        while (rs.next())
        {
            Importacao x = new Importacao();
            x.setCanal_id(rs.getLong("canal_id"));
            x.setImportacao_arquivo_original(rs.getString("importacao_arquivo_original"));
            x.setImportacao_periodo_inicio(rs.getDate("importacao_periodo_inicio").toLocalDate());
            Date f = rs.getDate("importacao_periodo_fim");
            x.setImportacao_periodo_fim(f != null ? f.toLocalDate() : null);
            x.setImportacao_status(rs.getString("importacao_status"));
            out.add(x);
        }
    }
    return out;
}
}