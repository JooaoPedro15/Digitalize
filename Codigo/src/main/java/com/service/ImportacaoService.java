package com.service;

import com.dao.ImportacaoDAO;
import com.model.Importacao;

import java.util.List;
import java.time.LocalDate;
import java.sql.SQLException;

/**
 * Serviço responsável por gerenciar operações relacionadas a Importacao.
 * 
 * O Service atua como camada intermediária entre o Controller e o DAO,
 * podendo adicionar regras de negócio, validações ou transformações antes de acessar o banco.
 */
public class ImportacaoService {

    // DAO responsável por executar operações no banco de dados
    private final ImportacaoDAO dao;

    /**
     * Construtor do serviço.
     * Inicializa o ImportacaoDAO para operações de persistência.
     */
    public ImportacaoService() {
        this.dao = new ImportacaoDAO();
    }

    /**
     * Insere uma nova Importacao no banco de dados.
     * @param x Importacao a ser inserida
     * @return true se a inserção ocorreu com sucesso
     * @throws SQLException em caso de erro na operação
     */
    public boolean insert(Importacao x) throws SQLException {
        return dao.insert(x);
    }

    /**
     * Atualiza os dados de uma Importacao existente.
     * @param x Importacao com dados atualizados
     * @return true se a atualização ocorreu com sucesso
     * @throws SQLException em caso de erro na operação
     */
    public boolean update(Importacao x) throws SQLException {
        return dao.update(x);
    }

    /**
     * Remove uma Importacao do banco de dados.
     * @param canal_id ID do Canal relacionado
     * @param arquivo Nome do arquivo da importacao
     * @param inicio Data de início da importacao
     * @return true se a remoção ocorreu com sucesso
     * @throws SQLException em caso de erro na operação
     */
    public boolean remove(long canal_id, String arquivo, LocalDate inicio) throws SQLException {
        return dao.remove(canal_id, arquivo, inicio);
    }

    /**
     * Busca uma Importacao específica no banco de dados.
     * @param canal_id ID do Canal relacionado
     * @param arquivo Nome do arquivo
     * @param inicio Data de início
     * @return Importacao encontrada ou null se não existir
     * @throws SQLException em caso de erro na operação
     */
    public Importacao get(long canal_id, String arquivo, LocalDate inicio) throws SQLException {
        return dao.get(canal_id, arquivo, inicio);
    }

    /**
     * Lista todas as Importacoes do banco de dados.
     * @return Lista de Importacoes
     * @throws SQLException em caso de erro na operação
     */
    public List<Importacao> listar() throws SQLException {
        return dao.listar();
    }
}
