package com.service;

import com.dao.*;
import com.model.*;

import main.java.com.dao.UsuarioDAO;
import main.java.com.model.Usuario;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import static spark.Spark.*;
import java.sql.Date;
import java.util.List;
import java.util.ArrayList;

public class Routes {
    public static final Gson gson = new Gson();

    public static void mount() {
        // 1. Migração Segura
        try {
            SchemaMigrator.migrate();
        } catch (Exception e) {
            e.printStackTrace();
        }

        GuiaService guiaService = new GuiaService();

        get("/health", (req, res) -> "{\"status\":\"ok\"}");

        // 2. Login
        post("/api/login", (req, res) -> {
            res.type("application/json");
            JsonObject body = gson.fromJson(req.body(), JsonObject.class);
            String email = body.has("email") ? body.get("email").getAsString() : "";
            String senha = body.has("senha") ? body.get("senha").getAsString() : "";

            UsuarioDAO dao = new UsuarioDAO();
            Usuario u = dao.autenticar(email, senha);

            if (u != null) {
                u.setSenha(null);
                return gson.toJson(u);
            } else {
                res.status(401);
                return "{\"error\": \"Email ou senha incorretos\"}";
            }
        });

        // 3. Cadastro de Usuário
        post("/api/usuarios", (req, res) -> {
            res.type("application/json");
            try {
                Usuario u = gson.fromJson(req.body(), Usuario.class);
                if(u.getTipo() == null) u.setTipo("usuario");
                
                UsuarioDAO dao = new UsuarioDAO();
                if (dao.insert(u)) {
                    res.status(201);
                    return "{\"message\": \"Criado com sucesso\"}";
                } else {
                    res.status(400);
                    return "{\"error\": \"Erro ao criar (Email já existe?)\"}";
                }
            } catch (Exception e) {
                res.status(500); return "{\"error\": \"Erro interno\"}";
            }
        });

        // 4. Cadastro de Empresa (Admin)
        post("/admin/cadastro", (req, res) -> {
            res.type("application/json");
            try {
                Empresa e = gson.fromJson(req.body(), Empresa.class);
                e.setStatus("aprovada"); // Força aprovação para teste

                EmpresaDAO dao = new EmpresaDAO();
                if(dao.insert(e)) {
                    res.status(201);
                    return "{\"mensagem\": \"Empresa cadastrada!\", \"id\": \"" + e.getCnpj() + "\"}";
                } else {
                    res.status(500); return "{\"mensagem\": \"Erro ao salvar. CNPJ duplicado?\"}";
                }
            } catch (Exception ex) {
                res.status(400); return "{\"mensagem\": \"Dados inválidos\"}";
            }
        });

        // 5. Listar Empresas
        get("/api/empresas", (req, res) -> {
            res.type("application/json");
            return gson.toJson(new EmpresaDAO().listar());
        });
        
        // 6. Buscar por Usuario (Novo)
        get("/api/empresas/usuario/:email", (req, res) -> {
            res.type("application/json");
            String email = req.params(":email");
            EmpresaDAO dao = new EmpresaDAO();
            List<Empresa> todas = dao.listar();
            List<Empresa> filtradas = new ArrayList<>();
            for (Empresa e : todas) {
                if (e.getResponsavel_email() != null && e.getResponsavel_email().equalsIgnoreCase(email)) {
                    filtradas.add(e);
                }
            }
            return gson.toJson(filtradas);
        });

        // 7. Rotas de Admin (Status e Delete)
        put("/api/empresas/:id/status", (req, res) -> {
            res.type("application/json");
            String id = req.params(":id");
            JsonObject body = gson.fromJson(req.body(), JsonObject.class);
            String status = body.get("status").getAsString();
            EmpresaDAO dao = new EmpresaDAO();
            Empresa e = dao.get(id);
            if(e != null) {
                e.setStatus(status);
                dao.update(e);
                return "{\"message\": \"Atualizado\"}";
            }
            res.status(404); return "{}";
        });

        delete("/api/empresas/:id", (req, res) -> {
            res.type("application/json");
            if(new EmpresaDAO().remove(req.params(":id"))) return "{\"message\": \"Removido\"}";
            res.status(404); return "{}";
        });

        // 8. IA
        get("/empresa/:cnpj/guia-postagem", (req, res) -> {
            res.type("application/json");
            try {
                return guiaService.gerarGuiaParaEmpresa(req.params(":cnpj"));
            } catch (Exception e) {
                return "{\"erro\":\"" + e.getMessage() + "\"}";
            }
        });
        
        // ==================================================================
        // 6. LISTAGEM DE USUÁRIOS (NOVO)
        // ==================================================================
        get("/api/usuarios", (req, res) -> {
            res.type("application/json; charset=utf-8");
            try {
                return gson.toJson(new UsuarioDAO().listar());
            } catch (Exception e) {
                e.printStackTrace();
                return "[]";
            }
        });

        // Placeholders restantes
        get("/api/avaliacoes", (req, res) -> "[]");
        get("/api/produtos", (req, res) -> "[]");
        get("/api/parceiros", (req, res) -> "[]");
    }
}
    }
}