package com.dao;

import com.model.Post;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO responsável por operações CRUD na tabela 'post' do schema 'midiasocial'.
 * 
 * Funções principais:
 * - Inserir ou atualizar posts (upsert)
 * - Listar posts por canal e período
 * - Buscar post específico
 * - Remover posts
 * 
 * Observações:
 * - Usa java.sql.Timestamp para data_hora e java.sql.Date para imp_periodo_inicio.
 * - Conexão com o banco obtida via DAO.getConnection().
 */
public class PostDAO {

    /**
     * Insere ou atualiza um post (upsert).
     * @param p Post a ser inserido ou atualizado
     * @throws SQLException em caso de erro
     */
    public void upsert(Post p) throws SQLException {
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
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, p.getCanal_id());
            ps.setTimestamp(2, p.getData_hora());
            ps.setString(3, p.getLegenda());
            ps.setObject(4, p.getDuracao());
            ps.setObject(5, p.getAlcance());
            ps.setObject(6, p.getViews());
            ps.setObject(7, p.getLikes());
            ps.setObject(8, p.getShares());
            ps.setObject(9, p.getComentarios());
            ps.setObject(10, p.getSaves());
            ps.setString(11, p.getImp_arquivo_original());
            ps.setDate(12, p.getImp_periodo_inicio());
            ps.executeUpdate();
        }
    }

    /**
     * Lista posts de um canal em um intervalo de datas.
     * @param canalId ID do canal
     * @param start Data inicial (inclusive)
     * @param end Data final (inclusive)
     * @return Lista de Post
     * @throws SQLException
     */
    public List<Post> listByCanal(long canalId, Date start, Date end) throws SQLException {
        String sql = "SELECT * FROM midiasocial.post WHERE canal_id = ? " +
                     "AND data_hora >= ?::timestamp AND data_hora < (?::date + INTERVAL '1 day') " +
                     "ORDER BY data_hora DESC";

        List<Post> out = new ArrayList<>();
        try (Connection conn = DAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, canalId);
            ps.setDate(2, start);
            ps.setDate(3, end);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Post p = new Post();
                    p.setCanal_id(rs.getLong("canal_id"));
                    p.setData_hora(rs.getTimestamp("data_hora"));
                    p.setLegenda(rs.getString("legenda"));
                    p.setDuracao(rs.getInt("duracao"));
                    p.setAlcance(rs.getInt("alcance"));
                    p.setViews(rs.getInt("views"));
                    p.setLikes(rs.getInt("likes"));
                    p.setShares(rs.getInt("shares"));
                    p.setComentarios(rs.getInt("comentarios"));
                    p.setSaves(rs.getInt("saves"));
                    p.setImp_arquivo_original(rs.getString("imp_arquivo_original"));
                    p.setImp_periodo_inicio(rs.getDate("imp_periodo_inicio"));
                    out.add(p);
                }
            }
        }
        return out;
    }

    public boolean insert(Post x) throws SQLException {
        upsert(x);
        return true;
    }

    public boolean update(Post x) throws SQLException {
        upsert(x);
        return true;
    }

    /**
     * Remove um post específico.
     * @param canal_id ID do canal
     * @param data_hora Data e hora do post
     * @param legenda Legenda do post
     * @return true se a remoção ocorreu
     * @throws SQLException
     */
    public boolean remove(long canal_id, Timestamp data_hora, String legenda) throws SQLException {
        String sql = "DELETE FROM midiasocial.post WHERE canal_id=? AND data_hora=? AND legenda=?";
        try (Connection conn = DAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, canal_id);
            ps.setTimestamp(2, data_hora);
            ps.setString(3, legenda);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Busca um post específico pelo canal, data_hora e legenda.
     * @param canal_id ID do canal
     * @param data_hora Data e hora do post
     * @param legenda Legenda do post
     * @return Post encontrado ou null se não existir
     * @throws SQLException
     */
    public Post get(long canal_id, Timestamp data_hora, String legenda) throws SQLException {
        String sql = "SELECT * FROM midiasocial.post WHERE canal_id=? AND data_hora=? AND legenda=?";
        try (Connection conn = DAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, canal_id);
            ps.setTimestamp(2, data_hora);
            ps.setString(3, legenda);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Post p = new Post();
                    p.setCanal_id(rs.getLong("canal_id"));
                    p.setData_hora(rs.getTimestamp("data_hora"));
                    p.setLegenda(rs.getString("legenda"));
                    p.setDuracao(rs.getInt("duracao"));
                    p.setAlcance(rs.getInt("alcance"));
                    p.setViews(rs.getInt("views"));
                    p.setLikes(rs.getInt("likes"));
                    p.setShares(rs.getInt("shares"));
                    p.setComentarios(rs.getInt("comentarios"));
                    p.setSaves(rs.getInt("saves"));
                    p.setImp_arquivo_original(rs.getString("imp_arquivo_original"));
                    p.setImp_periodo_inicio(rs.getDate("imp_periodo_inicio"));
                    return p;
                }
            }
        }
        return null;
    }

    /**
     * Lista todos os posts do banco.
     * @return Lista de Post
     * @throws SQLException
     */
    public List<Post> listar() throws SQLException {
        List<Post> out = new ArrayList<>();
        String sql = "SELECT * FROM midiasocial.post ORDER BY canal_id, data_hora";
        try (Connection conn = DAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Post p = new Post();
                p.setCanal_id(rs.getLong("canal_id"));
                p.setData_hora(rs.getTimestamp("data_hora"));
                p.setLegenda(rs.getString("legenda"));
                p.setDuracao(rs.getInt("duracao"));
                p.setAlcance(rs.getInt("alcance"));
                p.setViews(rs.getInt("views"));
                p.setLikes(rs.getInt("likes"));
                p.setShares(rs.getInt("shares"));
                p.setComentarios(rs.getInt("comentarios"));
                p.setSaves(rs.getInt("saves"));
                p.setImp_arquivo_original(rs.getString("imp_arquivo_original"));
                p.setImp_periodo_inicio(rs.getDate("imp_periodo_inicio"));
                out.add(p);
            }
        }
        return out;
    }
}
