package controller;

import static spark.Spark.*;
import com.google.gson.Gson;
import service.CanalService;
import model.Canal;

/**
 * Controlador responsável pelas rotas da entidade Canal.
 * Define endpoints REST para CRUD de canais de empresas.
 */
public class CanalController {
    private CanalService service = new CanalService();
    private Gson gson = new Gson();

    public CanalController() {
        // Lista todos os canais
        get("/canais", (req, res) -> gson.toJson(service.listarCanais()));

        // Lista canais de uma empresa específica
        get("/empresas/:cnpj/canais", (req, res) -> {
            String cnpj = req.params(":cnpj");
            return gson.toJson(service.listarCanaisPorEmpresa(cnpj));
        });

        // Cria um novo canal
        post("/canais", (req, res) -> {
            Canal canal = gson.fromJson(req.body(), Canal.class);
            service.criarCanal(canal);
            res.status(201);
            return "Canal criado com sucesso";
        });

        // Atualiza um canal existente
        put("/canais/:id", (req, res) -> {
            long id = Long.parseLong(req.params(":id"));
            Canal canal = gson.fromJson(req.body(), Canal.class);
            service.atualizarCanal(id, canal);
            return "Canal atualizado com sucesso";
        });

        // Remove um canal
        delete("/canais/:id", (req, res) -> {
            long id = Long.parseLong(req.params(":id"));
            service.removerCanal(id);
            return "Canal removido com sucesso";
        });
    }
}
