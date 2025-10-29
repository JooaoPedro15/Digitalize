package controller;

import static spark.Spark.*;
import com.google.gson.Gson;
import service.RecomendacaoService;
import model.Recomendacao;

/**
 * Controlador responsável pelas rotas da entidade Recomendacao.
 * Define endpoints REST para listar, criar, atualizar e remover recomendações.
 */
public class RecomendacaoController {
    private RecomendacaoService service = new RecomendacaoService();
    private Gson gson = new Gson();

    public RecomendacaoController() {
        // Lista todas as recomendações
        get("/recomendacoes", (req, res) -> gson.toJson(service.listarRecomendacoes()));

        // Lista recomendações de uma empresa específica
        get("/empresas/:cnpj/recomendacoes", (req, res) -> {
            String cnpj = req.params(":cnpj");
            return gson.toJson(service.listarPorEmpresa(cnpj));
        });

        // Cria uma nova recomendação
        post("/recomendacoes", (req, res) -> {
            Recomendacao rec = gson.fromJson(req.body(), Recomendacao.class);
            service.criarRecomendacao(rec);
            res.status(201);
            return "Recomendação criada com sucesso";
        });

        // Atualiza uma recomendação existente
        put("/recomendacoes/:id", (req, res) -> {
            long id = Long.parseLong(req.params(":id"));
            Recomendacao rec = gson.fromJson(req.body(), Recomendacao.class);
            service.atualizarRecomendacao(id, rec);
            return "Recomendação atualizada com sucesso";
        });

        // Remove uma recomendação
        delete("/recomendacoes/:id", (req, res) -> {
            long id = Long.parseLong(req.params(":id"));
            service.removerRecomendacao(id);
            return "Recomendação removida com sucesso";
        });
    }
}
