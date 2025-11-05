package controller;

import static spark.Spark.*;
import com.google.gson.Gson;
import service.ImportacaoService;
import model.Importacao;
import java.time.LocalDate;

/**
 * Controlador responsável pelas rotas da entidade Importacao.
 * Define endpoints REST para listar, criar, atualizar e remover importações.
 */
public class ImportacaoController {
    private final ImportacaoService service = new ImportacaoService();
    private final Gson gson = new Gson();

    public ImportacaoController() {
        // Lista todas as importações
        get("/importacoes", (req, res) -> {
            res.type("application/json");
            try {
                return gson.toJson(service.listar());
            } catch (Exception e) {
                res.status(500);
                return gson.toJson("Erro ao listar importações: " + e.getMessage());
            }
        });

        // Busca uma importação específica
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
                return gson.toJson("Parâmetros inválidos ou erro: " + e.getMessage());
            }
        });

        // Cria uma nova importação
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

        // Atualiza uma importação existente
        put("/importacoes/:canal_id/:arquivo/:inicio", (req, res) -> {
            res.type("application/json");
            try {
                long canalId = Long.parseLong(req.params(":canal_id"));
                String arquivo = req.params(":arquivo");
                LocalDate inicio = LocalDate.parse(req.params(":inicio"));

                Importacao imp = gson.fromJson(req.body(), Importacao.class());
                // Garante que os campos de chave estão corretos
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

        // Remove uma importação
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
