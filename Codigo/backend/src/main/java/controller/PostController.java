package controller;

import static spark.Spark.*;
import com.google.gson.Gson;
import service.PostService;
import model.Post;

import java.time.LocalDate;

/**
 * Controlador responsável pelas rotas da entidade Post.
 * Define endpoints REST para criar, atualizar, listar e remover postagens
 * vinculadas a canais de empresas.
 */
public class PostController {
    private PostService service = new PostService();
    private Gson gson = new Gson();

    public PostController() {
        // Cria (ou atualiza) um post — similar ao comportamento de "upsert"
        post("/posts", (req, res) -> {
            Post p = gson.fromJson(req.body(), Post.class);
            service.salvarOuAtualizar(p);
            res.status(201);
            return "{\"ok\":true}";
        });

        // Lista posts de um canal dentro de um intervalo de datas
        get("/posts/:canalId", (req, res) -> {
            long canalId = Long.parseLong(req.params(":canalId"));

            // parâmetros: ?start=2025-01-01&end=2025-01-31
            LocalDate start = req.queryParams("start") != null
                    ? LocalDate.parse(req.queryParams("start"))
                    : null;
            LocalDate end = req.queryParams("end") != null
                    ? LocalDate.parse(req.queryParams("end"))
                    : null;

            return gson.toJson(service.listarPorCanal(canalId, start, end));
        });

        // Atualiza um post existente
        put("/posts/:id", (req, res) -> {
            long id = Long.parseLong(req.params(":id"));
            Post p = gson.fromJson(req.body(), Post.class);
            service.atualizarPost(id, p);
            return "Post atualizado com sucesso";
        });

        // Remove um post
        delete("/posts/:id", (req, res) -> {
            long id = Long.parseLong(req.params(":id"));
            service.removerPost(id);
            return "Post removido com sucesso";
        });
    }
}
