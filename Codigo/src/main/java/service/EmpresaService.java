package service;

import dao.EmpresaDAO;
import model.Empresa;
import java.util.List;

public class EmpresaService
{
    private final EmpresaDAO dao;

    public EmpresaService()
    {
        this.dao = new EmpresaDAO();
    }

    public boolean insert(Empresa x)
    {
        return dao.insert(x);
    }

    public boolean update(Empresa x)
    {
        return dao.update(x);
    }

    public boolean remove(String cnpj)
    {
        return dao.remove(cnpj);
    }

    public Empresa get(String cnpj)
    {
        return dao.get(cnpj);
    }

    public List<Empresa> listar()
    {
        return dao.listar();
    }
}
