package com.controller;

import static spark.Spark.*;

import com.google.gson.Gson;
import com.service.EmpresaService;
import com.model.Empresa;

/**
 * Controlador responsável por expor as rotas REST da entidade Empresa.
 * 
 * Este controlador faz a ponte entre as requisições HTTP e a camada de serviço,
 * oferecendo endpoints completos para CRUD.
 *
 * Convenções aplicadas:
 *  - Todas as respostas são enviadas como JSON.
 *  - Uso de códigos HTTP adequados (200, 201, 400, 404).
 *  - Identificador principal da Empresa: CNPJ.
 *
 * Observação:
 * A definição das rotas ocorre dentro do construtor, pois a classe é instanciada
 * automaticamente pelo arquivo Routes.java durante a inicialização da aplicação.
 */
public class EmpresaController {

    private final EmpresaService service = new EmpresaService();
    private final Gson gson = new Gson();

    public EmpresaController() {

        // -------------------------------------
        // LISTAR TODAS AS EMPRESAS
        // -------------------------------------
        get("/empresas", (req, res) -> {
            res.type("application/json");
            return gson.toJson(service.listar());
        });

        // -------------------------------------
        // BUSCAR EMPRESA POR CNPJ
        // -------------------------------------
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

        // -------------------------------------
        // CRIAR UMA NOVA EMPRESA
        // -------------------------------------
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

        // -------------------------------------
        // ATUALIZAR EMPRESA EXISTENTE
        // -------------------------------------
        put("/empresas/:cnpj", (req, res) -> {
            res.type("application/json");

            String cnpj = req.params(":cnpj");
            Empresa emp = gson.fromJson(req.body(), Empresa.class);

            emp.setCnpj(cnpj); // Garante que o CNPJ do recurso é o mesmo da rota

            boolean atualizado = service.update(emp);

            if (atualizado) {
                return gson.toJson("Empresa atualizada com sucesso");
            } else {
                res.status(404);
                return gson.toJson("Empresa não encontrada");
            }
        });

        // -------------------------------------
        // REMOVER EMPRESA
        // -------------------------------------
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
