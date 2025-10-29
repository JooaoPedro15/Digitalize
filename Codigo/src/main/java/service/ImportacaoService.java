package service;

import dao.ImportacaoDAO;
import model.Importacao;
import java.util.List;
import java.time.LocalDate;

public class ImportacaoService
{
    private final ImportacaoDAO dao;

    public ImportacaoService()
    {
        this.dao = new ImportacaoDAO();
    }

    public boolean insert(Importacao x) throws java.sql.SQLException
    {
        return dao.insert(x);
    }

    public boolean update(Importacao x) throws java.sql.SQLException
    {
        return dao.update(x);
    }

    public boolean remove(long canal_id, String arquivo, LocalDate inicio) throws java.sql.SQLException
    {
        return dao.remove(canal_id, arquivo, inicio);
    }

    public Importacao get(long canal_id, String arquivo, LocalDate inicio) throws java.sql.SQLException
    {
        return dao.get(canal_id, arquivo, inicio);
    }

    public java.util.List<Importacao> listar() throws java.sql.SQLException
    {
        return dao.listar();
    }
}
