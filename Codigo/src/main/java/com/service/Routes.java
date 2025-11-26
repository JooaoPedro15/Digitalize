package com.service;

import com.dao.SchemaMigrator;
import com.dao.CanalDAO;
import com.dao.ImportacaoDAO;
import com.dao.PostDAO;

import com.model.Canal;
import com.model.Importacao;
import com.model.Post;

import com.google.gson.Gson;

import static spark.Spark.*;

import java.sql.Date;

/**
 * Classe responsável por registrar e montar todas as rotas da API.
 * 
 * Funcionalidades expostas:
 * - /health: Verificação de saúde da API
 * - /canais: CRUD básico de canais
 * - /importacoes: Inserção/atualização de importações
 * - /posts: Inserção/atualização de posts e listagem por canal e período
 * - /empresa/:cnpj/guia-postagem: Geração do Guia de Postagem (Sistema Inteligente)
 */
public class Routes
{
    // Gson compartilhado para serialização e desserialização JSON
    public static final Gson gson = ApiConfig.gson;

    /**
     * Monta todas as rotas da aplicação.
     * Executa a migração inicial do schema do banco.
     */
    public static void mount()
    {
        // Executa migração do banco (criação de tabelas se não existirem)
        System.out.println("Migrating database...");
        SchemaMigrator.migrate();

        // Service do sistema inteligente (guia de postagem)
        GuiaService guiaService = new GuiaService();

        /**
         * Rota de verificação da saúde da API
         * GET /health
         */
        get("/health", (req, res) ->
        {
            res.type("application/json; charset=utf-8");
            return "{\"status\":\"ok\"}";
        });

        /**
         * Criação de um canal
         * POST /canais
         * Corpo esperado: JSON representando um Canal
         * Retorna o ID do canal criado
         */
        post("/canais", (req, res) ->
        {
            res.type("application/json; charset=utf-8");

            Canal c = gson.fromJson(req.body(), Canal.class);
            long id = new CanalDAO().insert(c);
            res.status(201);
            return "{\"canal_id\":" + id + "}";
        });

        /**
         * Listagem de canais de uma empresa
         * GET /canais/:cnpj
         * Retorna JSON com todos os canais da empresa
         */
        get("/canais/:cnpj", (req, res) ->
        {
            res.type("application/json; charset=utf-8");

            String cnpj = req.params(":cnpj");
            return gson.toJson(new CanalDAO().listByEmpresa(cnpj));
        });

        /**
         * Inserção/atualização de importações
         * POST /importacoes
         * Corpo esperado: JSON representando uma Importacao
         */
        post("/importacoes", (req, res) ->
        {
            res.type("application/json; charset=utf-8");

            Importacao imp = gson.fromJson(req.body(), Importacao.class);
            new ImportacaoDAO().upsert(imp);
            res.status(201);
            return "{\"ok\":true}";
        });

        /**
         * Inserção/atualização de posts
         * POST /posts
         * Corpo esperado: JSON representando um Post
         */
        post("/posts", (req, res) ->
        {
            res.type("application/json; charset=utf-8");

            Post p = gson.fromJson(req.body(), Post.class);
            new PostDAO().upsert(p);
            res.status(201);
            return "{\"ok\":true}";
        });

        /**
         * Listagem de posts de um canal em um intervalo de datas
         * GET /posts/:canalId?start=yyyy-MM-dd&end=yyyy-MM-dd
         */
        get("/posts/:canalId", (req, res) ->
        {
            res.type("application/json; charset=utf-8");

            long canalId = Long.parseLong(req.params(":canalId"));

            // Recupera parâmetros de data da query string
            String startParam = req.queryParams("start");
            String endParam   = req.queryParams("end");

            // Converte strings para java.sql.Date (formato "yyyy-MM-dd")
            Date start = Date.valueOf(startParam);
            Date end   = Date.valueOf(endParam);

            return gson.toJson(new PostDAO().listByCanal(canalId, start, end));
        });

        /**
         * Geração do "Guia de Postagem" em JSON para um CNPJ.
         * Ex.: GET http://localhost:8080/empresa/00000000000001/guia-postagem
         */
        get("/empresa/:cnpj/guia-postagem", (req, res) ->
        {
            res.type("application/json; charset=utf-8");

            String cnpj = req.params(":cnpj");   // lê o CNPJ da URL

            try
            {
                // Delegamos a lógica para o GuiaService (que usa AzureOpenAIClient ou fallback)
                return guiaService.gerarGuiaParaEmpresa(cnpj);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                res.status(500);

                String msg = e.getMessage();
                if (msg == null)
                {
                    msg = "Erro interno ao gerar guia.";
                }
                // Evita quebrar o JSON com aspas duplas
                msg = msg.replace("\"", "'");

                return "{\"erro\":\"" + msg + "\"}";
            }
        });
        
        get("/debug/azure-env", (req, res) ->
        {
            res.type("application/json; charset=utf-8");

            String endpoint = System.getenv("AZURE_OPENAI_ENDPOINT");
            String key      = System.getenv("AZURE_OPENAI_API_KEY");
            String ver      = System.getenv("AZURE_OPENAI_API_VERSION");
            String chatDep  = System.getenv("AZURE_OPENAI_CHAT_DEPLOYMENT");
            String embDep   = System.getenv("AZURE_OPENAI_EMBEDDING_DEPLOYMENT");

            String keyPreview = (key == null) ? null
                : (key.length() > 6 ? key.substring(0, 6) + "..." : key);

            return "{"
                + "\"endpoint\":\""   + String.valueOf(endpoint) + "\","
                + "\"apiKey\":\""     + String.valueOf(keyPreview) + "\","
                + "\"apiVersion\":\"" + String.valueOf(ver) + "\","
                + "\"chatDep\":\""    + String.valueOf(chatDep) + "\","
                + "\"embDep\":\""     + String.valueOf(embDep) + "\""
                + "}";
        });
        
        // ============================
        //  AUTENTICACAO SIMPLES /API
        // ============================

        // Classe interna simples so para representar usuario na API
        class UsuarioApi
        {
            long id;
            String nome;
            String email;
            String senha;
            String tipo; // "admin" ou "usuario"
            boolean ativo;

            UsuarioApi(long id, String nome, String email, String senha, String tipo)
            {
                this.id    = id;
                this.nome  = nome;
                this.email = email;
                this.senha = senha;
                this.tipo  = tipo;
                this.ativo = true;
            }
        }

        // Lista em memoria de usuarios de teste
        java.util.List<UsuarioApi> usuariosApi = new java.util.ArrayList<>();
        // TODO: ajuste estes dados para bater com o login de teste que voce ja descobriu
        usuariosApi.add(new UsuarioApi(1L, "Admin", "admin@teste.com", "123456", "admin"));
        usuariosApi.add(new UsuarioApi(2L, "Usuario", "user@teste.com", "123456", "usuario"));

        // GET /api/usuarios  -> usado no register() para ver se o email ja existe
        get("/api/usuarios", (req, res) ->
        {
            res.type("application/json; charset=utf-8");
            return gson.toJson(usuariosApi);
        });

        // POST /api/usuarios -> usado no register() para "cadastrar" novo usuario
        post("/api/usuarios", (req, res) ->
        {
            res.type("application/json; charset=utf-8");

            UsuarioApi novo = gson.fromJson(req.body(), UsuarioApi.class);
            if (novo == null || novo.email == null)
            {
                res.status(400);
                return "{\"success\":false,\"message\":\"Dados invalidos\"}";
            }

            // So para simular: adiciona na lista em memoria
            long novoId = usuariosApi.stream()
                                     .mapToLong(u -> u.id)
                                     .max()
                                     .orElse(0L) + 1L;
            novo.id = novoId;
            usuariosApi.add(novo);

            res.status(201);
            return "{\"success\":true,\"message\":\"Usuario cadastrado com sucesso\"}";
        });

        // POST /api/login -> usado no login()
        post("/api/login", (req, res) ->
        {
            res.type("application/json; charset=utf-8");

            // Corpo esperado: { "email": "...", "senha": "..." }
            class LoginReq
            {
                String email;
                String senha;
            }

            LoginReq body = gson.fromJson(req.body(), LoginReq.class);
            if (body == null || body.email == null || body.senha == null)
            {
                res.status(400);
                return "{\"success\":false,\"error\":\"Dados de login invalidos\"}";
            }

            // Procura usuario com email/senha
            UsuarioApi encontrado = null;
            for (UsuarioApi u : usuariosApi)
            {
                if (u.email.equals(body.email) && u.senha.equals(body.senha) && u.ativo)
                {
                    encontrado = u;
                    break;
                }
            }

            if (encontrado == null)
            {
                res.status(401);
                return "{\"success\":false,\"error\":\"Email ou senha incorretos\"}";
            }

            // Monta objeto de resposta igual o que o front espera: { success, message, usuario }
            java.util.Map<String, Object> resp = new java.util.HashMap<>();
            resp.put("success", true);
            resp.put("message", "Login realizado com sucesso");

            java.util.Map<String, Object> usuario = new java.util.HashMap<>();
            usuario.put("id",    encontrado.id);
            usuario.put("nome",  encontrado.nome);
            usuario.put("email", encontrado.email);
            usuario.put("tipo",  encontrado.tipo);
            usuario.put("ativo", encontrado.ativo);

            resp.put("usuario", usuario);

            return gson.toJson(resp);
        });


    }
}
