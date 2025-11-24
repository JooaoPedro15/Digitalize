package com.service;

import com.dao.PostDAO;
import com.model.Post;

import java.sql.Timestamp;
import java.sql.SQLException;
import java.util.List;

/**
 * Serviço responsável por operações de negócios relacionadas a posts.
 * 
 * Este service atua como camada intermediária entre o controller e o DAO,
 * delegando as operações CRUD para o PostDAO.
 */
public class PostService {

    private final PostDAO dao;

    /**
     * Construtor padrão.
     * Inicializa o DAO de Post.
     */
    public PostService() {
        this.dao = new PostDAO();
    }

    /**
     * Insere um novo post ou atualiza caso já exista (delegando para DAO).
     * @param x Post a ser inserido
     * @return true se operação bem-sucedida
     * @throws SQLException em caso de erro no banco
     */
    public boolean insert(Post x) throws SQLException {
        return dao.insert(x);
    }

    /**
     * Atualiza um post existente (delegando para DAO).
     * @param x Post a ser atualizado
     * @return true se operação bem-sucedida
     * @throws SQLException em caso de erro no banco
     */
    public boolean update(Post x) throws SQLException {
        return dao.update(x);
    }

    /**
     * Remove um post específico.
     * @param canal_id ID do canal
     * @param data_hora Timestamp do post
     * @param legenda Legenda do post
     * @return true se remoção bem-sucedida
     * @throws SQLException em caso de erro no banco
     */
    public boolean remove(long canal_id, Timestamp data_hora, String legenda) throws SQLException {
        return dao.remove(canal_id, data_hora, legenda);
    }

    /**
     * Busca um post específico.
     * @param canal_id ID do canal
     * @param data_hora Timestamp do post
     * @param legenda Legenda do post
     * @return Post encontrado ou null se não existir
     * @throws SQLException em caso de erro no banco
     */
    public Post get(long canal_id, Timestamp data_hora, String legenda) throws SQLException {
        return dao.get(canal_id, data_hora, legenda);
    }

    /**
     * Lista todos os posts do banco.
     * @return Lista de Post
     * @throws SQLException em caso de erro no banco
     */
    public List<Post> listar() throws SQLException {
        return dao.listar();
    }
}
