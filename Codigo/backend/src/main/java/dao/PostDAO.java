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
}
