package service;

import dao.CanalDAO;
import model.Canal;
import java.util.List;

public class CanalService
{
    private final CanalDAO dao;

    public CanalService()
    {
        this.dao = new CanalDAO();
    }

    public long insert(Canal x) throws java.sql.SQLException
    {
        return dao.insert(x);
    }

    public boolean update(Canal x) throws java.sql.SQLException
    {
        return dao.update(x);
    }

    public boolean remove(long id) throws java.sql.SQLException
    {
        return dao.remove(id);
    }

    public Canal get(long id) throws java.sql.SQLException
    {
        return dao.get(id);
    }

    public List<Canal> listar() throws java.sql.SQLException
    {
        return dao.listar();
    }
}
