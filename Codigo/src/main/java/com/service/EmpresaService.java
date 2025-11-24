package com.service;

import com.dao.EmpresaDAO;
import com.model.Empresa;

import java.util.List;

/**
 * Serviço responsável por gerenciar operações relacionadas a Empresa.
 * 
 * O Service atua como camada intermediária entre o Controller e o DAO,
 * podendo adicionar regras de negócio, validações ou transformações antes de acessar o banco.
 */
public class EmpresaService {

    // DAO responsável por executar operações no banco de dados
    private final EmpresaDAO dao;

    /**
     * Construtor do serviço.
     * Inicializa o EmpresaDAO para operações de persistência.
     */
    public EmpresaService() {
        this.dao = new EmpresaDAO();
    }

    /**
     * Insere uma nova Empresa no banco de dados.
     * @param x Empresa a ser inserida
     * @return true se a inserção ocorreu com sucesso
     */
    public boolean insert(Empresa x) {
        return dao.insert(x);
    }

    /**
     * Atualiza os dados de uma Empresa existente.
     * @param x Empresa com dados atualizados
     * @return true se a atualização ocorreu com sucesso
     */
    public boolean update(Empresa x) {
        return dao.update(x);
    }

    /**
     * Remove uma Empresa do banco de dados pelo CNPJ.
     * @param cnpj CNPJ da Empresa a ser removida
     * @return true se a remoção ocorreu com sucesso
     */
    public boolean remove(String cnpj) {
        return dao.remove(cnpj);
    }

    /**
     * Busca uma Empresa pelo CNPJ.
     * @param cnpj CNPJ da Empresa
     * @return Empresa encontrada ou null se não existir
     */
    public Empresa get(String cnpj) {
        return dao.get(cnpj);
    }

    /**
     * Lista todas as Empresas do banco de dados.
     * @return Lista de Empresas
     */
    public List<Empresa> listar() {
        return dao.listar();
    }
}
