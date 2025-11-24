package com.service;

import com.dao.CanalDAO;
import com.model.Canal;

import java.util.List;
import java.sql.SQLException;

/**
 * Serviço responsável por gerenciar operações relacionadas a Canal.
 * 
 * O Service atua como camada intermediária entre o Controller e o DAO,
 * podendo adicionar regras de negócio, validações ou transformações antes de acessar o banco.
 */
public class CanalService {

    // DAO responsável por executar operações no banco de dados
    private final CanalDAO dao;

    /**
     * Construtor do serviço.
     * Inicializa o CanalDAO para operações de persistência.
     */
    public CanalService() {
        this.dao = new CanalDAO();
    }

    /**
     * Insere um novo Canal no banco de dados.
     * @param x Canal a ser inserido
     * @return ID gerado para o registro
     * @throws SQLException em caso de erro na operação
     */
    public long insert(Canal x) throws SQLException {
        return dao.insert(x);
    }

    /**
     * Atualiza os dados de um Canal existente.
     * @param x Canal com dados atualizados
     * @return true se a atualização ocorreu com sucesso
     * @throws SQLException em caso de erro na operação
     */
    public boolean update(Canal x) throws SQLException {
        return dao.update(x);
    }

    /**
     * Remove um Canal do banco de dados pelo ID.
     * @param id ID do Canal a ser removido
     * @return true se a remoção ocorreu com sucesso
     * @throws SQLException em caso de erro na operação
     */
    public boolean remove(long id) throws SQLException {
        return dao.remove(id);
    }

    /**
     * Busca um Canal pelo ID.
     * @param id ID do Canal
     * @return Canal encontrado ou null se não existir
     * @throws SQLException em caso de erro na operação
     */
    public Canal get(long id) throws SQLException {
        return dao.get(id);
    }

    /**
     * Lista todos os Canais do banco de dados.
     * @return Lista de Canais
     * @throws SQLException em caso de erro na operação
     */
    public List<Canal> listar() throws SQLException {
        return dao.listar();
    }
}
