package com.controller;

import static spark.Spark.*;

import com.google.gson.Gson;
import com.service.CanalService;
import com.model.Canal;

/**
 * Controlador responsável por expor as rotas REST referentes à entidade Canal.
 *
 * Este controlador integra a camada de serviço ao servidor HTTP (Spark),
 * definindo endpoints para as operações CRUD.
 *
 * Convenções aplicadas:
 *  - Todas as respostas são enviadas em JSON.
 *  - Código HTTP correto para cada situação (200, 201, 404).
 *  - Conversão de objetos ↔ JSON via Gson.
 *
 * Observação:
 * A inicialização das rotas acontece automaticamente no construtor,
 * pois a classe é instanciada dentro de Routes.java.
 */
public class CanalController {

    private final CanalService service = new CanalService();
    private final Gson gson = new Gson();

    public CanalController() {

        // -------------------------
        // LISTAR TODOS OS CANAIS
        // -------------------------
        get("/canais", (req, res) -> {
            res.type("application/json");
            return gson.toJson(service.listar());
        });

        // -------------------------
        // BUSCAR CANAL POR ID
        // -------------------------
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

        // -------------------------
        // CRIAR UM NOVO CANAL
        // -------------------------
        post("/canais", (req, res) -> {
            res.type("application/json");

            Canal canal = gson.fromJson(req.body(), Canal.class);
            long id = service.insert(canal);

            res.status(201);
            return gson.toJson("Canal criado com sucesso. ID: " + id);
        });

        // -------------------------
        // ATUALIZAR CANAL EXISTENTE
        // -------------------------
        put("/canais/:id", (req, res) -> {
            res.type("application/json");

            long id = Long.parseLong(req.params(":id"));
            Canal canal = gson.fromJson(req.body(), Canal.class);

            canal.setCanalId(id); // garante vinculação correta do recurso

            boolean atualizado = service.update(canal);

            if (atualizado) {
                return gson.toJson("Canal atualizado com sucesso");
            } else {
                res.status(404);
                return gson.toJson("Canal não encontrado");
            }
        });

        // -------------------------
        // REMOVER UM CANAL
        // -------------------------
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
