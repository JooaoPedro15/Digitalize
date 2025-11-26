package com.service;

import com.dao.ImportacaoDAO;
import com.model.Importacao;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Camada de servico para Importacao.
 * Alinha assinaturas com o controller/DAO (java.sql.Date) e
 * expõe overloads que aceitam LocalDate.
 */
public class ImportacaoService
{
    private final ImportacaoDAO dao;

    public ImportacaoService()
    {
        this.dao = new ImportacaoDAO();
    }

    public boolean insert(Importacao x) throws SQLException
    {
        return dao.insert(x);
    }

    public boolean update(Importacao x) throws SQLException
    {
        return dao.update(x);
    }

    // ------------------------------
    // Assinaturas principal (Date)
    // ------------------------------
    public boolean remove(long canalId, String arquivo, Date inicio) throws SQLException
    {
        return dao.remove(canalId, arquivo, inicio);
    }

    public Importacao get(long canalId, String arquivo, Date inicio) throws SQLException
    {
        return dao.get(canalId, arquivo, inicio);
    }

    public List<Importacao> listar() throws SQLException
    {
        return dao.listar();
    }

    // --------------------------------------------
    // Overloads convenientes (aceitam LocalDate)
    // --------------------------------------------
    public boolean remove(long canalId, String arquivo, LocalDate inicio) throws SQLException
    {
        return dao.remove(canalId, arquivo, Date.valueOf(inicio));
    }

    public Importacao get(long canalId, String arquivo, LocalDate inicio) throws SQLException
    {
        return dao.get(canalId, arquivo, Date.valueOf(inicio));
    }
}
