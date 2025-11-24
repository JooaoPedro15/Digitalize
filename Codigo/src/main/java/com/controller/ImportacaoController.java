package com.controller;

import static spark.Spark.*;

import com.google.gson.Gson;
import com.service.ImportacaoService;
import com.model.Importacao;

import java.time.LocalDate;

/**
 * Controlador responsável por expor as rotas REST da entidade Importacao.
 *
 * Cada importação é identificada por uma chave composta:
 *  - canal_id  (long)
 *  - arquivo   (String)
 *  - inicio    (LocalDate)
 *
 * Este controlador faz a ponte entre a camada HTTP (Spark) e a camada de serviço,
 * fornecendo endpoints para CRUD completo.
 *
 * Boas práticas aplicadas:
 *  - Respostas sempre em JSON.
 *  - Códigos HTTP corretos conforme o cenário.
 *  - Tratamento de erros e parâmetros inválidos.
 */
public class ImportacaoController {

    private final ImportacaoService service = new ImportacaoService();
    private final Gson gson = new Gson();

    public ImportacaoController() {

        // -------------------------------------------------------
        // LISTAR TODAS AS IMPORTAÇÕES
        // -------------------------------------------------------
        get("/importacoes", (req, res) -> {
            res.type("application/json");
            try {
                return gson.toJson(service.listar());
            } catch (Exception e) {
                res.status(500);
                return gson.toJson("Erro ao listar importações: " + e.getMessage());
            }
        });

        // -------------------------------------------------------
        // BUSCAR IMPORTAÇÃO POR CHAVE COMPOSTA
        // -------------------------------------------------------
        get("/importacoes/:canal_id/:arquivo/:inicio", (req, res) -> {
            res.type("application/json");

            try {
                long canalId = Long.parseLong(req.params(":canal_id"));
                String arquivo = req.params(":arquivo");
                LocalDate inicio = LocalDate.parse(req.params(":inicio"));

                Importacao imp = service.get(canalId, arquivo, inicio);

                if (imp != null) {
                    return gson.toJson(imp);
                } else {
                    res.status(404);
                    return gson.toJson("Importação não encontrada");
                }

            } catch (Exception e) {
                res.status(400);
                return gson.toJson("Parâmetros inválidos: " + e.getMessage());
            }
        });

        // -------------------------------------------------------
        // CRIAR UMA NOVA IMPORTAÇÃO
        // -------------------------------------------------------
        post("/importacoes", (req, res) -> {
            res.type("application/json");

            try {
                Importacao imp = gson.fromJson(req.body(), Importacao.class);
                boolean inserido = service.insert(imp);

                if (inserido) {
                    res.status(201);
                    return gson.toJson("Importação criada com sucesso");
                } else {
                    res.status(400);
                    return gson.toJson("Erro ao criar importação");
                }

            } catch (Exception e) {
                res.status(500);
                return gson.toJson("Erro ao criar importação: " + e.getMessage());
            }
        });

        // -------------------------------------------------------
        // ATUALIZAR IMPORTAÇÃO EXISTENTE
        // -------------------------------------------------------
        put("/importacoes/:canal_id/:arquivo/:inicio", (req, res) -> {
            res.type("application/json");

            try {
                long canalId = Long.parseLong(req.params(":canal_id"));
                String arquivo = req.params(":arquivo");
                LocalDate inicio = LocalDate.parse(req.params(":inicio"));

                Importacao imp = gson.fromJson(req.body(), Importacao.class);

                // Garante que os dados da chave composta sejam coerentes com a rota
                imp.setCanalId(canalId);
                imp.setArquivo(arquivo);
                imp.setInicio(inicio);

                boolean atualizado = service.update(imp);

                if (atualizado) {
                    return gson.toJson("Importação atualizada com sucesso");
                } else {
                    res.status(404);
                    return gson.toJson("Importação não encontrada");
                }

            } catch (Exception e) {
                res.status(400);
                return gson.toJson("Erro ao atualizar importação: " + e.getMessage());
            }
        });

        // -------------------------------------------------------
        // REMOVER IMPORTAÇÃO
        // -------------------------------------------------------
        delete("/importacoes/:canal_id/:arquivo/:inicio", (req, res) -> {
            res.type("application/json");

            try {
                long canalId = Long.parseLong(req.params(":canal_id"));
                String arquivo = req.params(":arquivo");
                LocalDate inicio = LocalDate.parse(req.params(":inicio"));

                boolean removido = service.remove(canalId, arquivo, inicio);

                if (removido) {
                    return gson.toJson("Importação removida com sucesso");
                } else {
                    res.status(404);
                    return gson.toJson("Importação não encontrada");
                }

            } catch (Exception e) {
                res.status(400);
                return gson.toJson("Erro ao remover importação: " + e.getMessage());
            }
        });
    }
}
