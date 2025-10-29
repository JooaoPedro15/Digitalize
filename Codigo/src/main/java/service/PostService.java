package service;

import dao.PostDAO;
import model.Post;
import java.util.List;
import java.time.LocalDateTime;

public class PostService
{
    private final PostDAO dao;

    public PostService()
    {
        this.dao = new PostDAO();
    }

    public boolean insert(Post x) throws java.sql.SQLException
    {
        return dao.insert(x);
    }

    public boolean update(Post x) throws java.sql.SQLException
    {
        return dao.update(x);
    }

    public boolean remove(long canal_id, LocalDateTime data_hora, String legenda) throws java.sql.SQLException
    {
        return dao.remove(canal_id, data_hora, legenda);
    }

    public Post get(long canal_id, LocalDateTime data_hora, String legenda) throws java.sql.SQLException
    {
        return dao.get(canal_id, data_hora, legenda);
    }

    public List<Post> listar() throws java.sql.SQLException
    {
        return dao.listar();
    }
}
