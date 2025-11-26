package com.controller;

import static spark.Spark.*;

import com.google.gson.Gson;
import com.service.ImportacaoService;
import com.model.Importacao;

import java.sql.Date;      // <-- usamos java.sql.Date
import java.time.LocalDate;

/**
 * Controlador responsável por expor as rotas REST da entidade Importacao.
 *
 * Chave composta: (canal_id, importacao_arquivo_original, importacao_periodo_inicio)
 */
public class ImportacaoController
{
    private final ImportacaoService service = new ImportacaoService();
    private final Gson gson = new Gson();

    public ImportacaoController()
    {
        // LISTAR TODAS
        get("/importacoes", (req, res) ->
        {
            res.type("application/json");
            try
            {
                return gson.toJson(service.listar());
            }
            catch (Exception e)
            {
                res.status(500);
                return gson.toJson("Erro ao listar importacoes: " + e.getMessage());
            }
        });

        // BUSCAR POR CHAVE COMPOSTA
        get("/importacoes/:canal_id/:arquivo/:inicio", (req, res) ->
        {
            res.type("application/json");
            try
            {
                long canalId   = Long.parseLong(req.params(":canal_id"));
                String arquivo = req.params(":arquivo");
                // converte "YYYY-MM-DD" -> LocalDate -> java.sql.Date
                LocalDate inicioLd = LocalDate.parse(req.params(":inicio"));
                Date inicio = Date.valueOf(inicioLd);

                Importacao imp = service.get(canalId, arquivo, inicio);

                if (imp != null)
                {
                    return gson.toJson(imp);
                }
                else
                {
                    res.status(404);
                    return gson.toJson("Importacao nao encontrada");
                }
            }
            catch (Exception e)
            {
                res.status(400);
                return gson.toJson("Parametros invalidos: " + e.getMessage());
            }
        });

        // CRIAR
        post("/importacoes", (req, res) ->
        {
            res.type("application/json");
            try
            {
                // O JSON deve bater com os nomes do model (importacao_arquivo_original, etc.)
                Importacao imp = gson.fromJson(req.body(), Importacao.class);

                // Garante status default (se existir no model):
                if (imp.getImportacao_status() == null || imp.getImportacao_status().isEmpty())
                {
                    imp.setImportacao_status("PENDENTE");
                }

                boolean inserido = service.insert(imp);
                if (inserido)
                {
                    res.status(201);
                    return gson.toJson("Importacao criada com sucesso");
                }
                else
                {
                    res.status(400);
                    return gson.toJson("Erro ao criar importacao");
                }
            }
            catch (Exception e)
            {
                res.status(500);
                return gson.toJson("Erro ao criar importacao: " + e.getMessage());
            }
        });

        // ATUALIZAR
        put("/importacoes/:canal_id/:arquivo/:inicio", (req, res) ->
        {
            res.type("application/json");
            try
            {
                long canalId   = Long.parseLong(req.params(":canal_id"));
                String arquivo = req.params(":arquivo");
                LocalDate inicioLd = LocalDate.parse(req.params(":inicio"));
                Date inicio = Date.valueOf(inicioLd);

                Importacao imp = gson.fromJson(req.body(), Importacao.class);

                // **setters reais do model** (compatíveis com seu print)
                imp.setCanal_id(canalId);
                imp.setImportacao_arquivo_original(arquivo);
                imp.setImportacao_periodo_inicio(inicio);

                boolean atualizado = service.update(imp);
                if (atualizado)
                {
                    return gson.toJson("Importacao atualizada com sucesso");
                }
                else
                {
                    res.status(404);
                    return gson.toJson("Importacao nao encontrada");
                }
            }
            catch (Exception e)
            {
                res.status(400);
                return gson.toJson("Erro ao atualizar importacao: " + e.getMessage());
            }
        });

        // REMOVER
        delete("/importacoes/:canal_id/:arquivo/:inicio", (req, res) ->
        {
            res.type("application/json");
            try
            {
                long canalId   = Long.parseLong(req.params(":canal_id"));
                String arquivo = req.params(":arquivo");
                LocalDate inicioLd = LocalDate.parse(req.params(":inicio"));
                Date inicio = Date.valueOf(inicioLd);

                boolean removido = service.remove(canalId, arquivo, inicio);
                if (removido)
                {
                    return gson.toJson("Importacao removida com sucesso");
                }
                else
                {
                    res.status(404);
                    return gson.toJson("Importacao nao encontrada");
                }
            }
            catch (Exception e)
            {
                res.status(400);
                return gson.toJson("Erro ao remover importacao: " + e.getMessage());
            }
        });
    }
}
