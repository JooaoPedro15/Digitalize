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

/**
 * Classe responsavel por registrar e montar todas as rotas da API.
 */
public class Routes
{
    public static final Gson gson = new Gson(); // Usa instância padrão se ApiConfig falhar

    public static void mount()
    {
        // -----------------------------------------------------------
        // 1. MIGRAÇÃO DO BANCO (Cria tabelas se não existirem)
        // -----------------------------------------------------------
        try {
            System.out.println("Iniciando migração do banco...");
            SchemaMigrator.migrate();
            System.out.println("Migração concluída com sucesso.");
        } catch (Exception e) {
            // Loga o erro mas não derruba a aplicação, permitindo que as rotas carreguem
            System.err.println("AVISO: Erro na migração (o sistema continuará rodando): " + e.getMessage());
            e.printStackTrace();
        }

        GuiaService guiaService = new GuiaService();

        // ----------------- Health Check -----------------
        get("/health", (req, res) -> "{\"status\":\"ok\"}");

        // ==================================================================
        // 2. AUTENTICAÇÃO E USUÁRIOS
        // ==================================================================

        // ROTA DE LOGIN
        post("/api/login", (req, res) -> {
            res.type("application/json; charset=utf-8");
            
            try {
                JsonObject body = gson.fromJson(req.body(), JsonObject.class);
                String email = body.has("email") ? body.get("email").getAsString() : "";
                String senha = body.has("senha") ? body.get("senha").getAsString() : "";

                UsuarioDAO usuarioDAO = new UsuarioDAO();
                Usuario usuario = usuarioDAO.autenticar(email, senha);

                if (usuario != null) {
                    usuario.setSenha(null); // Segurança: Remove senha do retorno
                    return gson.toJson(usuario);
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

        // ROTA DE CADASTRO DE USUÁRIO
        post("/api/usuarios", (req, res) -> {
            res.type("application/json; charset=utf-8");
            try {
                Usuario u = gson.fromJson(req.body(), Usuario.class);
                
                // Define padrão se vier vazio
                if(u.getTipo() == null || u.getTipo().isEmpty()) u.setTipo("usuario");
                
                UsuarioDAO dao = new UsuarioDAO();
                if (dao.insert(u)) {
                    res.status(201);
                    return "{\"message\": \"Usuário criado com sucesso\"}";
                } else {
                    res.status(400);
                    return "{\"error\": \"Erro ao criar usuário (Email já existe?)\"}";
                }
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return "{\"error\": \"Erro interno ao cadastrar\"}";
            }
        });

        // ==================================================================
        // 3. EMPRESAS (Com aprovação automática)
        // ==================================================================

        // Cadastrar Empresa
        post("/admin/cadastro", (req, res) -> {
            res.type("application/json; charset=utf-8");
            try {
                Empresa e = gson.fromJson(req.body(), Empresa.class);
                
                // --- AQUI ESTÁ O TRUQUE ---
                // Força o status para "aprovada" para aparecer no site imediatamente
                e.setStatus("aprovada"); 
                // --------------------------

                EmpresaDAO dao = new EmpresaDAO();
                if(dao.insert(e)) {
                    res.status(201);
                    return "{\"mensagem\": \"Empresa cadastrada e aprovada com sucesso!\", \"id\": \"" + e.getCnpj() + "\"}";
                } else {
                    res.status(500);
                    return "{\"mensagem\": \"Erro ao salvar no banco. Verifique se o CNPJ já existe.\"}";
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                res.status(400);
                return "{\"mensagem\": \"Dados inválidos\"}";
            }
        });

        // Listar todas as empresas
        get("/api/empresas", (req, res) -> {
            res.type("application/json; charset=utf-8");
            try {
                EmpresaDAO dao = new EmpresaDAO();
                return gson.toJson(dao.listar());
            } catch (Exception e) {
                e.printStackTrace();
                return "[]";
            }
        });

        // Rota de compatibilidade para empresas aprovadas
        get("/api/empresas/aprovadas", (req, res) -> {
            res.type("application/json; charset=utf-8");
            return gson.toJson(new EmpresaDAO().listar());
        });

        // ==================================================================
        // 4. CANAIS, POSTS E IMPORTAÇÕES
        // ==================================================================

        // Criar Canal
        post("/canais", (req, res) -> {
            res.type("application/json; charset=utf-8");
            try {
                Canal c = gson.fromJson(req.body(), Canal.class);
                long id = new CanalDAO().insert(c);
                res.status(201);
                return "{\"canal_id\":" + id + "}";
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return "{\"error\":\"Erro ao criar canal\"}";
            }
        });

        // Listar Canais por Empresa
        get("/canais/:cnpj", (req, res) -> {
            res.type("application/json; charset=utf-8");
            String cnpj = req.params(":cnpj");
            return gson.toJson(new CanalDAO().listByEmpresa(cnpj));
        });

        // Criar Importação
        post("/importacoes", (req, res) -> {
            res.type("application/json; charset=utf-8");
            Importacao imp = gson.fromJson(req.body(), Importacao.class);
            new ImportacaoDAO().upsert(imp);
            res.status(201);
            return "{\"ok\":true}";
        });

        // Criar Post
        post("/posts", (req, res) -> {
            res.type("application/json; charset=utf-8");
            Post p = gson.fromJson(req.body(), Post.class);
            new PostDAO().upsert(p);
            res.status(201);
            return "{\"ok\":true}";
        });

        // Listar Posts
        get("/posts/:canalId", (req, res) -> {
            res.type("application/json; charset=utf-8");
            long canalId = Long.parseLong(req.params(":canalId"));
            String startParam = req.queryParams("start");
            String endParam   = req.queryParams("end");
            Date start = (startParam != null) ? Date.valueOf(startParam) : new Date(System.currentTimeMillis());
            Date end   = (endParam != null) ? Date.valueOf(endParam) : new Date(System.currentTimeMillis());
            return gson.toJson(new PostDAO().listByCanal(canalId, start, end));
        });
        // ==================================================================
        // ROTAS DE GERENCIAMENTO (ADMIN) - ADICIONE ISTO!
        // ==================================================================

        // Atualizar Status (Aprovar/Rejeitar)
        put("/api/empresas/:id/status", (req, res) -> {
            res.type("application/json; charset=utf-8");
            String id = req.params(":id"); // Isso é o CNPJ
            
            try {
                JsonObject body = gson.fromJson(req.body(), JsonObject.class);
                String novoStatus = body.get("status").getAsString();
                
                EmpresaDAO dao = new EmpresaDAO();
                // Busca a empresa existente
                Empresa e = dao.get(id);
                
                if (e != null) {
                    e.setStatus(novoStatus);
                    if (dao.update(e)) {
                        return "{\"message\": \"Status atualizado com sucesso\"}";
                    }
                }
                res.status(404);
                return "{\"error\": \"Empresa não encontrada ou erro ao atualizar\"}";
            } catch (Exception ex) {
                ex.printStackTrace();
                res.status(500);
                return "{\"error\": \"Erro interno\"}";
            }
        });

        // Atualizar Empresa Completa (Edição)
        put("/api/empresas/:id", (req, res) -> {
            res.type("application/json; charset=utf-8");
            String id = req.params(":id");
            try {
                Empresa e = gson.fromJson(req.body(), Empresa.class);
                e.setCnpj(id); // Garante que o ID da URL prevalece
                
                // Se o status não vier, mantém pendente ou o que estava
                if(e.getStatus() == null) e.setStatus("pendente");

                EmpresaDAO dao = new EmpresaDAO();
                if (dao.update(e)) {
                    return "{\"message\": \"Empresa atualizada\"}";
                } else {
                    res.status(404);
                    return "{\"error\": \"Empresa não encontrada\"}";
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                res.status(500);
                return "{\"error\": \"Erro ao atualizar\"}";
            }
        });

        // Excluir Empresa
        delete("/api/empresas/:id", (req, res) -> {
            res.type("application/json; charset=utf-8");
            String id = req.params(":id");
            EmpresaDAO dao = new EmpresaDAO();
            if (dao.remove(id)) {
                return "{\"message\": \"Empresa excluída\"}";
            } else {
                res.status(404);
                return "{\"error\": \"Empresa não encontrada\"}";
            }
        });
        
        // ==================================================================
        // 5. INTELIGÊNCIA ARTIFICIAL
        // ==================================================================
        
        get("/empresa/:cnpj/guia-postagem", (req, res) -> {
            res.type("application/json; charset=utf-8");
            String cnpj = req.params(":cnpj");
            try {
                return guiaService.gerarGuiaParaEmpresa(cnpj);
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return "{\"erro\":\"" + e.getMessage() + "\"}";
            }
        });

        // ==================================================================
        // 6. PLACEHOLDERS (Evitar 404 no Frontend)
        // ==================================================================
        get("/api/avaliacoes", (req, res) -> "[]");
        get("/api/produtos", (req, res) -> "[]");
        get("/api/parceiros", (req, res) -> "[]");
        get("/api/usuarios", (req, res) -> "[]"); 
    }
}