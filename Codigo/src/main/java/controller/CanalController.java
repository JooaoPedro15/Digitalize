package controller;

import static spark.Spark.*;
import com.google.gson.Gson;
import service.CanalService;
import model.Canal;

/**
 * Controlador responsável pelas rotas da entidade Canal.
 * Define endpoints REST para CRUD de canais.
 */
public class CanalController {
    private final CanalService service = new CanalService();
    private final Gson gson = new Gson();

    public CanalController() {
        // Lista todos os canais
        get("/canais", (req, res) -> {
            res.type("application/json");
            return gson.toJson(service.listar());
        });

        // Busca um canal pelo ID
        get("/canais/:id", (req, res) -> {
            res.type("application/json");
            long id = Long.parseLong(req.params(":id"));
            Canal canal = service.get(id);
            if (canal != null) {
                return gson.toJson(canal);
            } else {
                res.status(404);
                return gson.toJson("Canal não encontrado");
            }
        });

        // Cria um novo canal
        post("/canais", (req, res) -> {
            res.type("application/json");
            Canal canal = gson.fromJson(req.body(), Canal.class);
            long id = service.insert(canal);
            res.status(201);
            return gson.toJson("Canal criado com sucesso. ID: " + id);
        });

        // Atualiza um canal existente
        put("/canais/:id", (req, res) -> {
            res.type("application/json");
            long id = Long.parseLong(req.params(":id"));
            Canal canal = gson.fromJson(req.body(), Canal.class);
            canal.setId(id); // garante que o ID está correto
            boolean atualizado = service.update(canal);
            if (atualizado) {
                return gson.toJson("Canal atualizado com sucesso");
            } else {
                res.status(404);
                return gson.toJson("Canal não encontrado");
            }
        });

        // Remove um canal
        delete("/canais/:id", (req, res) -> {
            res.type("application/json");
            long id = Long.parseLong(req.params(":id"));
            boolean removido = service.remove(id);
            if (removido) {
                return gson.toJson("Canal removido com sucesso");
            } else {
                res.status(404);
                return gson.toJson("Canal não encontrado");
            }
        });
    }
}
