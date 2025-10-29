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
    private EmpresaService service = new EmpresaService();
    private Gson gson = new Gson();

    public EmpresaController() {
        // Lista todas as empresas
        get("/empresas", (req, res) -> gson.toJson(service.listarEmpresas()));

        // Cria uma nova empresa
        post("/empresas", (req, res) -> {
            Empresa emp = gson.fromJson(req.body(), Empresa.class);
            service.criarEmpresa(emp);
            res.status(201);
            return "Empresa criada com sucesso";
        });

        // Busca uma empresa pelo CNPJ
        get("/empresas/:cnpj", (req, res) -> {
            String cnpj = req.params(":cnpj");
            Empresa emp = service.buscarEmpresa(cnpj);
            if (emp != null) {
                return gson.toJson(emp);
            } else {
                res.status(404);
                return "Empresa não encontrada";
            }
        });

        // Atualiza uma empresa existente
        put("/empresas/:cnpj", (req, res) -> {
            String cnpj = req.params(":cnpj");
            Empresa emp = gson.fromJson(req.body(), Empresa.class);
            service.atualizarEmpresa(cnpj, emp);
            return "Empresa atualizada com sucesso";
        });

        // Remove uma empresa
        delete("/empresas/:cnpj", (req, res) -> {
            String cnpj = req.params(":cnpj");
            service.removerEmpresa(cnpj);
            return "Empresa removida com sucesso";
        });
    }
}
