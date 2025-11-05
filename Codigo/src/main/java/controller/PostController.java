package controller;

import static spark.Spark.*;
import com.google.gson.Gson;
import service.PostService;
import model.Post;

import java.time.LocalDateTime;

/**
 * Controlador responsável pelas rotas da entidade Post.
 * Define endpoints REST para listar, criar, atualizar e remover postagens.
 */
public class PostController {
    private final PostService service = new PostService();
    private final Gson gson = new Gson();

    public PostController() {
        // Lista todos os posts
        get("/posts", (req, res) -> {
            res.type("application/json");
            try {
                return gson.toJson(service.listar());
            } catch (Exception e) {
                res.status(500);
                return gson.toJson("Erro ao listar posts: " + e.getMessage());
            }
        });

        // Busca um post específico pela chave composta
        get("/posts/:canal_id/:data_hora/:legenda", (req, res) -> {
            res.type("application/json");
            try {
                long canalId = Long.parseLong(req.params(":canal_id"));
                LocalDateTime dataHora = LocalDateTime.parse(req.params(":data_hora"));
                String legenda = req.params(":legenda");

                Post post = service.get(canalId, dataHora, legenda);
                if (post != null) {
                    return gson.toJson(post);
                } else {
                    res.status(404);
                    return gson.toJson("Post não encontrado");
                }
            } catch (Exception e) {
                res.status(400);
                return gson.toJson("Parâmetros inválidos ou erro: " + e.getMessage());
            }
        });

        // Cria um novo post
        post("/posts", (req, res) -> {
            res.type("application/json");
            try {
                Post post = gson.fromJson(req.body(), Post.class);
                boolean inserido = service.insert(post);
                if (inserido) {
                    res.status(201);
                    return gson.toJson("Post criado com sucesso");
                } else {
                    res.status(400);
                    return gson.toJson("Erro ao criar post");
                }
            } catch (Exception e) {
                res.status(500);
                return gson.toJson("Erro ao criar post: " + e.getMessage());
            }
        });

        // Atualiza um post existente
        put("/posts/:canal_id/:data_hora/:legenda", (req, res) -> {
            res.type("application/json");
            try {
                long canalId = Long.parseLong(req.params(":canal_id"));
                LocalDateTime dataHora = LocalDateTime.parse(req.params(":data_hora"));
                String legenda = req.params(":legenda");

                Post post = gson.fromJson(req.body(), Post.class);
                // Garante que os campos-chave estão corretos
                post.setCanalId(canalId);
                post.setDataHora(dataHora);
                post.setLegenda(legenda);

                boolean atualizado = service.update(post);
                if (atualizado) {
                    return gson.toJson("Post atualizado com sucesso");
                } else {
                    res.status(404);
                    return gson.toJson("Post não encontrado");
                }
            } catch (Exception e) {
                res.status(400);
                return gson.toJson("Erro ao atualizar post: " + e.getMessage());
            }
        });

        // Remove um post
        delete("/posts/:canal_id/:data_hora/:legenda", (req, res) -> {
            res.type("application/json");
            try {
                long canalId = Long.parseLong(req.params(":canal_id"));
                LocalDateTime dataHora = LocalDateTime.parse(req.params(":data_hora"));
                String legenda = req.params(":legenda");

                boolean removido = service.remove(canalId, dataHora, legenda);
                if (removido) {
                    return gson.toJson("Post removido com sucesso");
                } else {
                    res.status(404);
                    return gson.toJson("Post não encontrado");
                }
            } catch (Exception e) {
                res.status(400);
                return gson.toJson("Erro ao remover post: " + e.getMessage());
            }
        });
    }
}
