package com.controller;

import static spark.Spark.*;

import com.google.gson.Gson;
import com.service.PostService;
import com.model.Post;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Controlador responsavel por expor as rotas REST relacionadas à entidade Post.
 */
public class PostController
{
    private final PostService service = new PostService();
    private final Gson gson = new Gson();

    public PostController()
    {
        // LISTAR TODOS
        get("/posts", (req, res) ->
        {
            res.type("application/json");
            try
            {
                return gson.toJson(service.listar());
            }
            catch (Exception e)
            {
                res.status(500);
                return gson.toJson("Erro ao listar posts: " + e.getMessage());
            }
        });

        // BUSCAR POR CHAVE COMPOSTA
        // /posts/:canal_id/:data_hora/:legenda
        get("/posts/:canal_id/:data_hora/:legenda", (req, res) ->
        {
            res.type("application/json");
            try
            {
                long canalId = Long.parseLong(req.params(":canal_id"));
                LocalDateTime ldt = LocalDateTime.parse(req.params(":data_hora"));
                Timestamp dataHora = Timestamp.valueOf(ldt);
                String legenda = req.params(":legenda");

                Post post = service.get(canalId, dataHora, legenda);

                if (post != null)
                {
                    return gson.toJson(post);
                }
                else
                {
                    res.status(404);
                    return gson.toJson("Post nao encontrado");
                }
            }
            catch (Exception e)
            {
                res.status(400);
                return gson.toJson("Parametros invalidos ou erro: " + e.getMessage());
            }
        });

        // CRIAR
        post("/posts", (req, res) ->
        {
            res.type("application/json");
            try
            {
                Post post = gson.fromJson(req.body(), Post.class);

                boolean inserido = service.insert(post);
                if (inserido)
                {
                    res.status(201);
                    return gson.toJson("Post criado com sucesso");
                }
                else
                {
                    res.status(400);
                    return gson.toJson("Erro ao criar post");
                }
            }
            catch (Exception e)
            {
                res.status(500);
                return gson.toJson("Erro ao criar post: " + e.getMessage());
            }
        });

        // ATUALIZAR
        // /posts/:canal_id/:data_hora/:legenda
        put("/posts/:canal_id/:data_hora/:legenda", (req, res) ->
        {
            res.type("application/json");
            try
            {
                long canalId = Long.parseLong(req.params(":canal_id"));
                LocalDateTime ldt = LocalDateTime.parse(req.params(":data_hora"));
                Timestamp dataHora = Timestamp.valueOf(ldt);
                String legenda = req.params(":legenda");

                Post post = gson.fromJson(req.body(), Post.class);

                // alinhar com a PK da rota
                post.setCanal_id(canalId);
                post.setData_hora(dataHora);
                post.setLegenda(legenda);

                boolean atualizado = service.update(post);
                if (atualizado)
                {
                    return gson.toJson("Post atualizado com sucesso");
                }
                else
                {
                    res.status(404);
                    return gson.toJson("Post nao encontrado");
                }
            }
            catch (Exception e)
            {
                res.status(400);
                return gson.toJson("Erro ao atualizar post: " + e.getMessage());
            }
        });

        // REMOVER
        // /posts/:canal_id/:data_hora/:legenda
        delete("/posts/:canal_id/:data_hora/:legenda", (req, res) ->
        {
            res.type("application/json");
            try
            {
                long canalId = Long.parseLong(req.params(":canal_id"));
                LocalDateTime ldt = LocalDateTime.parse(req.params(":data_hora"));
                Timestamp dataHora = Timestamp.valueOf(ldt);
                String legenda = req.params(":legenda");

                boolean removido = service.remove(canalId, dataHora, legenda);
                if (removido)
                {
                    return gson.toJson("Post removido com sucesso");
                }
                else
                {
                    res.status(404);
                    return gson.toJson("Post nao encontrado");
                }
            }
            catch (Exception e)
            {
                res.status(400);
                return gson.toJson("Erro ao remover post: " + e.getMessage());
            }
        });
    }
}
