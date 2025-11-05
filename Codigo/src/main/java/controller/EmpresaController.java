package controller;

import static spark.Spark.*;
import com.google.gson.Gson;
import service.EmpresaService;
import model.Empresa;

/**
 * Controlador responsável pelas rotas da entidade Empresa.
 * Define endpoints REST para listar, criar, atualizar e remover empresas.
 */
public class EmpresaController {
    private final EmpresaService service = new EmpresaService();
    private final Gson gson = new Gson();

    public EmpresaController() {
        // Lista todas as empresas
        get("/empresas", (req, res) -> {
            res.type("application/json");
            return gson.toJson(service.listar());
        });

        // Busca uma empresa pelo CNPJ
        get("/empresas/:cnpj", (req, res) -> {
            res.type("application/json");
            String cnpj = req.params(":cnpj");
            Empresa emp = service.get(cnpj);
            if (emp != null) {
                return gson.toJson(emp);
            } else {
                res.status(404);
                return gson.toJson("Empresa não encontrada");
            }
        });

        // Cria uma nova empresa
        post("/empresas", (req, res) -> {
            res.type("application/json");
            Empresa emp = gson.fromJson(req.body(), Empresa.class);
            boolean inserido = service.insert(emp);
            if (inserido) {
                res.status(201);
                return gson.toJson("Empresa criada com sucesso");
            } else {
                res.status(400);
                return gson.toJson("Erro ao criar empresa");
            }
        });

        // Atualiza uma empresa existente
        put("/empresas/:cnpj", (req, res) -> {
            res.type("application/json");
            String cnpj = req.params(":cnpj");
            Empresa emp = gson.fromJson(req.body(), Empresa.class);
            emp.setCnpj(cnpj); // garante que o CNPJ está correto
            boolean atualizado = service.update(emp);
            if (atualizado) {
                return gson.toJson("Empresa atualizada com sucesso");
            } else {
                res.status(404);
                return gson.toJson("Empresa não encontrada");
            }
        });

        // Remove uma empresa
        delete("/empresas/:cnpj", (req, res) -> {
            res.type("application/json");
            String cnpj = req.params(":cnpj");
            boolean removido = service.remove(cnpj);
            if (removido) {
                return gson.toJson("Empresa removida com sucesso");
            } else {
                res.status(404);
                return gson.toJson("Empresa não encontrada");
            }
        });
    }
}
