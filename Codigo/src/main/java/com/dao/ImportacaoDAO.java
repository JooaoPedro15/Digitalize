package com.dao;

import com.model.Importacao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO responsável por operações CRUD na tabela 'importacao' do schema 'midiasocial'.
 * 
 * Funções principais:
 * - Inserir ou atualizar registros (upsert)
 * - Listar importações por canal
 * - Buscar importação específica
 * - Remover registros
 * 
 * Observações:
 * - Usa java.sql.Date para datas de início e fim da importação.
 * - Conexão com o banco obtida via DAO.getConnection().
 */
public class ImportacaoDAO {

    /**
     * Insere ou atualiza uma importação (upsert).
     * @param i Importacao a ser inserida ou atualizada
     * @throws SQLException em caso de erro na operação
     */
    public void upsert(Importacao i) throws SQLException {
        String sql = "INSERT INTO midiasocial.importacao " +
                     "(canal_id, importacao_arquivo_original, importacao_periodo_inicio, importacao_periodo_fim, importacao_status) " +
                     "VALUES (?,?,?,?,?) " +
                     "ON CONFLICT (canal_id, importacao_arquivo_original, importacao_periodo_inicio) " +
                     "DO UPDATE SET importacao_periodo_fim = EXCLUDED.importacao_periodo_fim, importacao_status = EXCLUDED.importacao_status";

        try (Connection conn = DAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, i.getCanal_id());
            ps.setString(2, i.getImportacao_arquivo_original());
            ps.setDate(3, i.getImportacao_periodo_inicio());
            ps.setDate(4, i.getImportacao_periodo_fim());
            ps.setString(5, i.getImportacao_status());
            ps.executeUpdate();
        }
    }

    /**
     * Lista todas as importações de um canal específico.
     * @param canalId ID do canal
     * @return Lista de Importacao
     * @throws SQLException em caso de erro na consulta
     */
    public List<Importacao> listByCanal(long canalId) throws SQLException {
        String sql = "SELECT * FROM midiasocial.importacao WHERE canal_id = ? ORDER BY importacao_periodo_inicio DESC";
        List<Importacao> out = new ArrayList<>();
        try (Connection conn = DAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, canalId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Importacao i = new Importacao();
                    i.setCanal_id(rs.getLong("canal_id"));
                    i.setImportacao_arquivo_original(rs.getString("importacao_arquivo_original"));
                    i.setImportacao_periodo_inicio(rs.getDate("importacao_periodo_inicio"));
                    i.setImportacao_periodo_fim(rs.getDate("importacao_periodo_fim"));
                    i.setImportacao_status(rs.getString("importacao_status"));
                    out.add(i);
                }
            }
        }
        return out;
    }

    /**
     * Insere uma nova importação (chama upsert).
     * @param x Importacao
     * @return sempre true
     * @throws SQLException
     */
    public boolean insert(Importacao x) throws SQLException {
        upsert(x);
        return true;
    }

    /**
     * Atualiza uma importação existente (chama upsert).
     * @param x Importacao
     * @return sempre true
     * @throws SQLException
     */
    public boolean update(Importacao x) throws SQLException {
        upsert(x);
        return true;
    }

    /**
     * Remove uma importação específica.
     * @param canal_id ID do canal
     * @param arquivo Nome do arquivo
     * @param inicio Data de início da importação
     * @return true se a remoção ocorreu
     * @throws SQLException
     */
    public boolean remove(long canal_id, String arquivo, java.sql.Date inicio) throws SQLException {
        String sql = "DELETE FROM midiasocial.importacao WHERE canal_id=? AND importacao_arquivo_original=? AND importacao_periodo_inicio=?";
        try (Connection conn = DAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, canal_id);
            ps.setString(2, arquivo);
            ps.setDate(3, inicio);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Busca uma importação específica pelo canal, arquivo e data de início.
     * @param canal_id ID do canal
     * @param arquivo Nome do arquivo
     * @param inicio Data de início
     * @return Importacao encontrada ou null se não existir
     * @throws SQLException
     */
    public Importacao get(long canal_id, String arquivo, java.sql.Date inicio) throws SQLException {
        String sql = "SELECT canal_id, importacao_arquivo_original, importacao_periodo_inicio, importacao_periodo_fim, importacao_status " +
                     "FROM midiasocial.importacao WHERE canal_id=? AND importacao_arquivo_original=? AND importacao_periodo_inicio=?";
        try (Connection conn = DAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, canal_id);
            ps.setString(2, arquivo);
            ps.setDate(3, inicio);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Importacao x = new Importacao();
                    x.setCanal_id(rs.getLong("canal_id"));
                    x.setImportacao_arquivo_original(rs.getString("importacao_arquivo_original"));
                    x.setImportacao_periodo_inicio(rs.getDate("importacao_periodo_inicio"));
                    x.setImportacao_periodo_fim(rs.getDate("importacao_periodo_fim"));
                    x.setImportacao_status(rs.getString("importacao_status"));
                    return x;
                }
            }
        }
        return null;
    }

    /**
     * Lista todas as importações do banco.
     * @return Lista de Importacao
     * @throws SQLException
     */
    public List<Importacao> listar() throws SQLException {
        List<Importacao> out = new ArrayList<>();
        String sql = "SELECT canal_id, importacao_arquivo_original, importacao_periodo_inicio, importacao_periodo_fim, importacao_status " +
                     "FROM midiasocial.importacao ORDER BY canal_id, importacao_periodo_inicio";
        try (Connection conn = DAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Importacao x = new Importacao();
                x.setCanal_id(rs.getLong("canal_id"));
                x.setImportacao_arquivo_original(rs.getString("importacao_arquivo_original"));
                x.setImportacao_periodo_inicio(rs.getDate("importacao_periodo_inicio"));
                x.setImportacao_periodo_fim(rs.getDate("importacao_periodo_fim"));
                x.setImportacao_status(rs.getString("importacao_status"));
                out.add(x);
            }
        }
        return out;
    }
}
