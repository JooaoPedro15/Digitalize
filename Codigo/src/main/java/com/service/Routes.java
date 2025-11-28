package com.service;

import com.dao.SchemaMigrator;
import com.dao.CanalDAO;
import com.dao.ImportacaoDAO;
import com.dao.PostDAO;
import com.dao.EmpresaDAO;
import com.dao.UsuarioDAO;

import com.model.Canal;
import com.model.Importacao;
import com.model.Post;
import com.model.Empresa;
import com.model.Usuario;

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
            System.out.println("Iniciando migração do banco...");
            SchemaMigrator.migrate();
            System.out.println("Migração concluída com sucesso.");
        } catch (Exception e) {
            System.err.println("AVISO: Erro na migração (o sistema continuará rodando): " + e.getMessage());
            e.printStackTrace();
        }

        GuiaService guiaService = new GuiaService();

        get("/health", (req, res) -> "{\"status\":\"ok\"}");

        // 2. Login
        post("/api/login", (req, res) -> {
            res.type("application/json; charset=utf-8");
            try {
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
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return "{\"error\": \"Erro interno no servidor\"}";
            }
        });

        // 3. Cadastro de Usuário
        post("/api/usuarios", (req, res) -> {
            res.type("application/json; charset=utf-8");
            try {
                Usuario u = gson.fromJson(req.body(), Usuario.class);
                if(u.getTipo() == null || u.getTipo().isEmpty()) u.setTipo("usuario");
                
                UsuarioDAO dao = new UsuarioDAO();
                if (dao.insert(u)) {
                    res.status(201);
                    return "{\"message\": \"Criado com sucesso\"}";
                } else {
                    res.status(400);
                    return "{\"error\": \"Erro ao criar (Email já existe?)\"}";
                }
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500); return "{\"error\": \"Erro interno\"}";
            }
        });

        // 4. Cadastro de Empresa (Admin)
        post("/admin/cadastro", (req, res) -> {
            res.type("application/json; charset=utf-8");
            try {
                Empresa e = gson.fromJson(req.body(), Empresa.class);
                e.setStatus("aprovada"); 

                EmpresaDAO dao = new EmpresaDAO();
                if(dao.insert(e)) {
                    res.status(201);
                    return "{\"mensagem\": \"Empresa cadastrada!\", \"id\": \"" + e.getCnpj() + "\"}";
                } else {
                    res.status(500); return "{\"mensagem\": \"Erro ao salvar. CNPJ duplicado?\"}";
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                res.status(400); return "{\"mensagem\": \"Dados inválidos\"}";
            }
        });

        // 5. Listar Empresas
        get("/api/empresas", (req, res) -> {
            res.type("application/json; charset=utf-8");
            try {
                return gson.toJson(new EmpresaDAO().listar());
            } catch (Exception e) {
                e.printStackTrace();
                return "[]";
            }
        });
        
        // Buscar por Usuario
        get("/api/empresas/usuario/:email", (req, res) -> {
            res.type("application/json; charset=utf-8");
            String email = req.params(":email");
            try {
                EmpresaDAO dao = new EmpresaDAO();
                List<Empresa> todas = dao.listar();
                List<Empresa> filtradas = new ArrayList<>();
                for (Empresa e : todas) {
                    if (e.getResponsavel_email() != null && e.getResponsavel_email().equalsIgnoreCase(email)) {
                        filtradas.add(e);
                    }
                }
                return gson.toJson(filtradas);
            } catch (Exception e) {
                e.printStackTrace();
                return "[]";
            }
        });

        // 6. Listar Usuários (O que faltava para o Admin)
        get("/api/usuarios", (req, res) -> {
            res.type("application/json; charset=utf-8");
            try {
                return gson.toJson(new UsuarioDAO().listar());
            } catch (Exception e) {
                e.printStackTrace();
                return "[]";
            }
        });

        // 7. Rotas de Admin (Status e Delete)
        put("/api/empresas/:id/status", (req, res) -> {
            res.type("application/json; charset=utf-8");
            String id = req.params(":id");
            try {
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
            } catch (Exception e) {
                res.status(500); return "{\"error\":\"Erro\"}";
            }
        });

        delete("/api/empresas/:id", (req, res) -> {
            res.type("application/json; charset=utf-8");
            try {
                if(new EmpresaDAO().remove(req.params(":id"))) return "{\"message\": \"Removido\"}";
                res.status(404); return "{}";
            } catch (Exception e) {
                res.status(500); return "{\"error\":\"Erro\"}";
            }
        });

        // 8. IA
        get("/empresa/:cnpj/guia-postagem", (req, res) -> {
            res.type("application/json; charset=utf-8");
            try {
                return guiaService.gerarGuiaParaEmpresa(req.params(":cnpj"));
            } catch (Exception e) {
                return "{\"erro\":\"" + e.getMessage() + "\"}";
            }
        });

        // 9. Outros (Canais, Posts, Importações)
        post("/canais", (req, res) -> {
            res.type("application/json; charset=utf-8");
            try {
                Canal c = gson.fromJson(req.body(), Canal.class);
                long id = new CanalDAO().insert(c);
                res.status(201);
                return "{\"canal_id\":" + id + "}";
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500); return "{\"error\":\"Erro ao criar canal\"}";
            }
        });

        get("/canais/:cnpj", (req, res) -> {
            res.type("application/json; charset=utf-8");
            String cnpj = req.params(":cnpj");
            return gson.toJson(new CanalDAO().listByEmpresa(cnpj));
        });

        post("/importacoes", (req, res) -> {
            res.type("application/json; charset=utf-8");
            try {
                Importacao imp = gson.fromJson(req.body(), Importacao.class);
                new ImportacaoDAO().upsert(imp);
                res.status(201); return "{\"ok\":true}";
            } catch(Exception e) { res.status(500); return "{\"error\":\"Erro\"}"; }
        });

        post("/posts", (req, res) -> {
            res.type("application/json; charset=utf-8");
            try {
                Post p = gson.fromJson(req.body(), Post.class);
                new PostDAO().upsert(p);
                res.status(201); return "{\"ok\":true}";
            } catch(Exception e) { res.status(500); return "{\"error\":\"Erro\"}"; }
        });

        get("/posts/:canalId", (req, res) -> {
            res.type("application/json; charset=utf-8");
            try {
                long canalId = Long.parseLong(req.params(":canalId"));
                Date start = req.queryParams("start") != null ? Date.valueOf(req.queryParams("start")) : new Date(System.currentTimeMillis());
                Date end = req.queryParams("end") != null ? Date.valueOf(req.queryParams("end")) : new Date(System.currentTimeMillis());
                return gson.toJson(new PostDAO().listByCanal(canalId, start, end));
            } catch(Exception e) { return "[]"; }
        });
        
        // Placeholders
        get("/api/avaliacoes", (req, res) -> "[]");
        get("/api/produtos", (req, res) -> "[]");
        get("/api/parceiros", (req, res) -> "[]");
    }
}