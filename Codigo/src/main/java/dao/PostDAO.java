package dao;

import model.Post;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PostDAO
{
    public void upsert(Post p) throws SQLException
    {
        String sql = "INSERT INTO midiasocial.post " +
                     "(canal_id, data_hora, legenda, duracao, alcance, views, likes, shares, comentarios, saves, imp_arquivo_original, imp_periodo_inicio) " +
                     "VALUES (?,?,?,?,?,?,?,?,?,?,?,?) " +
                     "ON CONFLICT (canal_id, data_hora, legenda) DO UPDATE SET " +
                     "duracao = EXCLUDED.duracao, " +
                     "alcance = EXCLUDED.alcance, " +
                     "views = EXCLUDED.views, " +
                     "likes = EXCLUDED.likes, " +
                     "shares = EXCLUDED.shares, " +
                     "comentarios = EXCLUDED.comentarios, " +
                     "saves = EXCLUDED.saves, " +
                     "imp_arquivo_original = EXCLUDED.imp_arquivo_original, " +
                     "imp_periodo_inicio = EXCLUDED.imp_periodo_inicio";

        try (Connection conn = DAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setLong(1, p.getCanal_id());
            ps.setObject(2, p.getData_hora());
            ps.setString(3, p.getLegenda());
            ps.setObject(4, p.getDuracao());
            ps.setObject(5, p.getAlcance());
            ps.setObject(6, p.getViews());
            ps.setObject(7, p.getLikes());
            ps.setObject(8, p.getShares());
            ps.setObject(9, p.getComentarios());
            ps.setObject(10, p.getSaves());
            ps.setString(11, p.getImp_arquivo_original());
            ps.setObject(12, p.getImp_periodo_inicio());
            ps.executeUpdate();
        }
    }

    public List<Post> listByCanal(long canalId, LocalDate start, LocalDate end) throws SQLException
    {
        String sql = "SELECT * FROM midiasocial.post WHERE canal_id = ? " +
                     "AND data_hora >= ?::timestamp AND data_hora < (?::date + INTERVAL '1 day') " +
                     "ORDER BY data_hora DESC";

        List<Post> out = new ArrayList<>();
        try (Connection conn = DAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setLong(1, canalId);
            ps.setObject(2, start);
            ps.setObject(3, end);
            try (ResultSet rs = ps.executeQuery())
            {
                while (rs.next())
                {
                    Post p = new Post();
                    p.setCanal_id(rs.getLong("canal_id"));
                    p.setData_hora(rs.getObject("data_hora", LocalDateTime.class));
                    p.setLegenda(rs.getString("legenda"));
                    p.setDuracao((Integer) rs.getObject("duracao"));
                    p.setAlcance((Integer) rs.getObject("alcance"));
                    p.setViews((Integer) rs.getObject("views"));
                    p.setLikes((Integer) rs.getObject("likes"));
                    p.setShares((Integer) rs.getObject("shares"));
                    p.setComentarios((Integer) rs.getObject("comentarios"));
                    p.setSaves((Integer) rs.getObject("saves"));
                    p.setImp_arquivo_original(rs.getString("imp_arquivo_original"));
                    p.setImp_periodo_inicio(rs.getObject("imp_periodo_inicio", LocalDate.class));
                    out.add(p);
                }
            }
        }
        return out;
    }


public boolean insert(Post x) throws SQLException
{
    upsert(x);
    return true;
}

public boolean update(Post x) throws SQLException
{
    upsert(x);
    return true;
}

public boolean remove(long canal_id, java.time.LocalDateTime data_hora, String legenda) throws SQLException
{
    String sql = "DELETE FROM midiasocial.post WHERE canal_id=? AND data_hora=? AND legenda=?";
    try (Connection conn = DAO.getConnection(); PreparedStatement ps = conn.prepareStatement(sql))
    {
        ps.setLong(1, canal_id);
        ps.setTimestamp(2, java.sql.Timestamp.valueOf(data_hora));
        ps.setString(3, legenda);
        return ps.executeUpdate() > 0;
    }
}

public Post get(long canal_id, java.time.LocalDateTime data_hora, String legenda) throws SQLException
{
    String sql = "SELECT canal_id, data_hora, legenda, duracao, alcance, views, likes, shares, comentarios, saves, imp_arquivo_original, imp_periodo_inicio FROM midiasocial.post WHERE canal_id=? AND data_hora=? AND legenda=?";
    try (Connection conn = DAO.getConnection(); PreparedStatement ps = conn.prepareStatement(sql))
    {
        ps.setLong(1, canal_id);
        ps.setTimestamp(2, java.sql.Timestamp.valueOf(data_hora));
        ps.setString(3, legenda);
        try (ResultSet rs = ps.executeQuery())
        {
            if (rs.next())
            {
                Post p = new Post();
                p.setCanal_id(rs.getLong("canal_id"));
                p.setData_hora(rs.getTimestamp("data_hora").toLocalDateTime());
                p.setLegenda(rs.getString("legenda"));
                p.setDuracao(rs.getInt("duracao"));
                p.setAlcance(rs.getInt("alcance"));
                p.setViews(rs.getInt("views"));
                p.setLikes(rs.getInt("likes"));
                p.setShares(rs.getInt("shares"));
                p.setComentarios(rs.getInt("comentarios"));
                p.setSaves(rs.getInt("saves"));
                p.setImp_arquivo_original(rs.getString("imp_arquivo_original"));
                java.sql.Date d = rs.getDate("imp_periodo_inicio");
                p.setImp_periodo_inicio(d != null ? d.toLocalDate() : null);
                return p;
            }
        }
    }
    return null;
}

public List<Post> listar() throws SQLException
{
    List<Post> out = new ArrayList<>();
    String sql = "SELECT canal_id, data_hora, legenda, duracao, alcance, views, likes, shares, comentarios, saves, imp_arquivo_original, imp_periodo_inicio FROM midiasocial.post ORDER BY canal_id, data_hora";
    try (Connection conn = DAO.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery())
    {
        while (rs.next())
        {
            Post p = new Post();
            p.setCanal_id(rs.getLong("canal_id"));
            p.setData_hora(rs.getTimestamp("data_hora").toLocalDateTime());
            p.setLegenda(rs.getString("legenda"));
            p.setDuracao(rs.getInt("duracao"));
            p.setAlcance(rs.getInt("alcance"));
            p.setViews(rs.getInt("views"));
            p.setLikes(rs.getInt("likes"));
            p.setShares(rs.getInt("shares"));
            p.setComentarios(rs.getInt("comentarios"));
            p.setSaves(rs.getInt("saves"));
            p.setImp_arquivo_original(rs.getString("imp_arquivo_original"));
            java.sql.Date d = rs.getDate("imp_periodo_inicio");
            p.setImp_periodo_inicio(d != null ? d.toLocalDate() : null);
            out.add(p);
        }
    }
    return out;
}
}