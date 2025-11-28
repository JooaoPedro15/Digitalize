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
    public static final Gson gson = ApiConfig.gson;

    /**
     * Helper para mapear Empresa -> JSON compativel com o front.
     */
    private static java.util.Map<String, Object> empresaToApi(Empresa e)
    {
        java.util.Map<String, Object> m = new java.util.HashMap<>();
        m.put("id", e.getCnpj());
        m.put("cnpj", e.getCnpj());
        m.put("nomeFantasia", e.getNome_fantasia());
        m.put("nome_fantasia", e.getNome_fantasia());
        m.put("razaoSocial", e.getRazao_social());
        m.put("razao_social", e.getRazao_social());
        m.put("segmento", e.getSegmento());
        m.put("endereco", e.getEndereco());
        m.put("status", e.getStatus());
        m.put("responsavelEmail", e.getResponsavel_email());
        m.put("email", e.getEmail_contato());
        return m;
    }

    public static void mount()
    {
        // -----------------------------------------------------------
        // 1. MIGRAÇÃO DO BANCO (Cria tabelas se não existirem)
        // -----------------------------------------------------------
        try {
            System.out.println("Iniciando migração do banco...");
            SchemaMigrator.migrate();
            System.out.println("Migração concluída.");
        } catch (Exception e) {
            System.err.println("AVISO: Erro na migração (o sistema continuará rodando): " + e.getMessage());
            e.printStackTrace();
        }

        GuiaService guiaService = new GuiaService();

        // ----------------- Health Check -----------------
        get("/health", (req, res) -> "{\"status\":\"ok\"}");

        // ==================================================================
        // 2. AUTENTICAÇÃO REAL (Conectada ao Banco de Dados)
        // ==================================================================

        // ROTA DE LOGIN
        post("/api/login", (req, res) -> {
            res.type("application/json; charset=utf-8");
            
            try {
                // Lê email e senha do JSON
                JsonObject body = gson.fromJson(req.body(), JsonObject.class);
                String email = body.has("email") ? body.get("email").getAsString() : "";
                String senha = body.has("senha") ? body.get("senha").getAsString() : "";

                // Chama o DAO para verificar no banco
                UsuarioDAO usuarioDAO = new UsuarioDAO();
                Usuario usuario = usuarioDAO.autenticar(email, senha);

                if (usuario != null) {
                    // Sucesso!
                    usuario.setSenha(null); // Não devolve a senha pro front
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
                    res.status(400); // Provavelmente email duplicado
                    return "{\"error\": \"Erro ao criar usuário (Email já existe?)\"}";
                }
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return "{\"error\": \"Erro interno ao cadastrar\"}";
            }
        });

        // ==================================================================
        // 3. ROTAS DE NEGÓCIO (Empresas, Canais, Posts)
        // ==================================================================

        // POST /admin/cadastro – cria empresa (já aprovada para teste)
        post("/admin/cadastro", (req, res) -> {
            res.type("application/json; charset=utf-8");
            try {
                Empresa e = gson.fromJson(req.body(), Empresa.class);
                
                // FORÇA APROVAÇÃO AUTOMÁTICA (Para facilitar sua apresentação)
                e.setStatus("aprovada"); 

                EmpresaDAO dao = new EmpresaDAO();
                if(dao.insert(e)) {
                    res.status(201);
                    return "{\"mensagem\": \"Empresa cadastrada e aprovada com sucesso!\", \"id\": \"" + e.getCnpj() + "\"}";
                } else {
                    res.status(500);
                    return "{\"mensagem\": \"Erro ao salvar no banco\"}";
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                res.status(400);
                return "{\"mensagem\": \"Dados inválidos\"}";
            }
        });

        // Listar empresas com filtros
        get("/api/empresas", (req, res) -> {
            res.type("application/json; charset=utf-8");
            EmpresaDAO dao = new EmpresaDAO();
            return gson.toJson(dao.listar());
        });

        // Rotas específicas de status (mantidas para compatibilidade)
        get("/api/empresas/aprovadas", (req, res) -> {
            res.type("application/json; charset=utf-8");
            return gson.toJson(new EmpresaDAO().listar()); // Retorna todas por enquanto
        });

        // ----------------- Canais -----------------
        post("/canais", (req, res) -> {
            res.type("application/json; charset=utf-8");
            Canal c = gson.fromJson(req.body(), Canal.class);
            long id = new CanalDAO().insert(c);
            res.status(201);
            return "{\"canal_id\":" + id + "}";
        });

        get("/canais/:cnpj", (req, res) -> {
            res.type("application/json; charset=utf-8");
            String cnpj = req.params(":cnpj");
            return gson.toJson(new CanalDAO().listByEmpresa(cnpj));
        });

        // --------------- Importacoes ---------------
        post("/importacoes", (req, res) -> {
            res.type("application/json; charset=utf-8");
            Importacao imp = gson.fromJson(req.body(), Importacao.class);
            new ImportacaoDAO().upsert(imp);
            res.status(201);
            return "{\"ok\":true}";
        });

        // ------------------ Posts ------------------
        post("/posts", (req, res) -> {
            res.type("application/json; charset=utf-8");
            Post p = gson.fromJson(req.body(), Post.class);
            new PostDAO().upsert(p);
            res.status(201);
            return "{\"ok\":true}";
        });

        get("/posts/:canalId", (req, res) -> {
            res.type("application/json; charset=utf-8");
            long canalId = Long.parseLong(req.params(":canalId"));
            String startParam = req.queryParams("start");
            String endParam   = req.queryParams("end");
            Date start = (startParam != null) ? Date.valueOf(startParam) : new Date(System.currentTimeMillis());
            Date end   = (endParam != null) ? Date.valueOf(endParam) : new Date(System.currentTimeMillis());
            return gson.toJson(new PostDAO().listByCanal(canalId, start, end));
        });

        // -------- Guia de Postagem (IA) -----------
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

        // Rota placeholder para evitar erros 404 no front
        get("/api/avaliacoes", (req, res) -> "[]");
        get("/api/produtos", (req, res) -> "[]");
        get("/api/parceiros", (req, res) -> "[]");
        get("/api/usuarios", (req, res) -> "[]"); // Listagem pública bloqueada por segurança
    }
}