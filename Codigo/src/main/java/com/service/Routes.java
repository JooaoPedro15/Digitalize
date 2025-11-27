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
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

/**
 * Classe responsavel por registrar e montar as rotas da API.
 */
public class Routes
{
    // Instancia compartilhada de Gson para JSON
    public static final Gson gson = ApiConfig.gson;

    // Calcula o hash (SHA-256) de uma senha em texto puro
    private static String hashSenha(String senhaPura)
    {
        if (senhaPura == null)
        {
            return null;
        }
        try
        {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(senhaPura.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes)
            {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Erro ao calcular hash da senha", e);
        }
    }

    /**
     * Estrutura simples de usuario usada nas rotas de autenticacao da API.
     */
    public static class UsuarioApi
    {
        public long id;
        public String nome;
        public String email;
        public String senha;
        public String tipo; // "admin" ou "usuario"
        public boolean ativo;

        public UsuarioApi(long id, String nome, String email, String senha, String tipo)
        {
            this.id = id;
            this.nome = nome;
            this.email = email;
            this.senha = senha;
            this.tipo = tipo;
            this.ativo = true;
        }
    }

    /**
     * Estrutura usada para receber os dados de login enviados pelo front-end.
     */
    public static class LoginReq
    {
        public String email;
        public String senha;

        public LoginReq()
        {
        }
    }

    // Lista em memoria com usuarios de teste para as rotas de autenticacao
    private static final java.util.List<UsuarioApi> usuariosApi = new java.util.ArrayList<>();

    static
    {
        usuariosApi.add(new UsuarioApi(1L, "Admin", "admin@teste.com", hashSenha("123456"), "admin"));
        usuariosApi.add(new UsuarioApi(2L, "Usuario", "user@teste.com", hashSenha("123456"), "usuario"));
    }

    /**
     * Registra todas as rotas da aplicacao e executa a migracao do banco.
     */
    public static void mount()
    {
        // Cria estruturas de banco (tabelas) se ainda nao existirem
        System.out.println("Migrating database...");
        SchemaMigrator.migrate();

        // Serviço responsavel por gerar o guia de postagem
        GuiaService guiaService = new GuiaService();

        // -----------------------------
        // Rotas gerais da API
        // -----------------------------

        // Verificacao simples de saude da API
        // GET /health
        get("/health", (req, res) ->
        {
            res.type("application/json; charset=utf-8");
            return "{\"status\":\"ok\"}";
        });

        // Criacao de um canal
        // POST /canais
        post("/canais", (req, res) ->
        {
            res.type("application/json; charset=utf-8");

            Canal c = gson.fromJson(req.body(), Canal.class);
            long id = new CanalDAO().insert(c);
            res.status(201);
            return "{\"canal_id\":" + id + "}";
        });

        // Listagem de canais de uma empresa
        // GET /canais/:cnpj
        get("/canais/:cnpj", (req, res) ->
        {
            res.type("application/json; charset=utf-8");

            String cnpj = req.params(":cnpj");
            return gson.toJson(new CanalDAO().listByEmpresa(cnpj));
        });

        // Insercao/atualizacao de importacoes
        // POST /importacoes
        post("/importacoes", (req, res) ->
        {
            res.type("application/json; charset=utf-8");

            Importacao imp = gson.fromJson(req.body(), Importacao.class);
            new ImportacaoDAO().upsert(imp);
            res.status(201);
            return "{\"ok\":true}";
        });

        // Insercao/atualizacao de posts
        // POST /posts
        post("/posts", (req, res) ->
        {
            res.type("application/json; charset=utf-8");

            Post p = gson.fromJson(req.body(), Post.class);
            new PostDAO().upsert(p);
            res.status(201);
            return "{\"ok\":true}";
        });

        // Listagem de posts de um canal em um intervalo de datas
        // GET /posts/:canalId?start=yyyy-MM-dd&end=yyyy-MM-dd
        get("/posts/:canalId", (req, res) ->
        {
            res.type("application/json; charset=utf-8");

            long canalId = Long.parseLong(req.params(":canalId"));

            String startParam = req.queryParams("start");
            String endParam = req.queryParams("end");

            Date start = Date.valueOf(startParam);
            Date end = Date.valueOf(endParam);

            return gson.toJson(new PostDAO().listByCanal(canalId, start, end));
        });

        // Geracao do Guia de Postagem para uma empresa
        // GET /empresa/:cnpj/guia-postagem
        get("/empresa/:cnpj/guia-postagem", (req, res) ->
        {
            res.type("application/json; charset=utf-8");

            String cnpj = req.params(":cnpj");

            try
            {
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

                msg = msg.replace("\"", "'");

                return "{\"erro\":\"" + msg + "\"}";
            }
        });

        // Rota de debug para verificar variaveis de ambiente do Azure
        // GET /debug/azure-env
        get("/debug/azure-env", (req, res) ->
        {
            res.type("application/json; charset=utf-8");

            String endpoint = System.getenv("AZURE_OPENAI_ENDPOINT");
            String key = System.getenv("AZURE_OPENAI_API_KEY");
            String ver = System.getenv("AZURE_OPENAI_API_VERSION");
            String chatDep = System.getenv("AZURE_OPENAI_CHAT_DEPLOYMENT");
            String embDep = System.getenv("AZURE_OPENAI_EMBEDDING_DEPLOYMENT");

            String keyPreview = (key == null) ? null
                    : (key.length() > 6 ? key.substring(0, 6) + "..." : key);

            return "{"
                    + "\"endpoint\":\"" + String.valueOf(endpoint) + "\","
                    + "\"apiKey\":\"" + String.valueOf(keyPreview) + "\","
                    + "\"apiVersion\":\"" + String.valueOf(ver) + "\","
                    + "\"chatDep\":\"" + String.valueOf(chatDep) + "\","
                    + "\"embDep\":\"" + String.valueOf(embDep) + "\""
                    + "}";
        });

        // -----------------------------
        // Rotas de autenticacao simples
        // -----------------------------

        // Lista usuarios cadastrados na API de autenticacao
        // GET /api/usuarios
        get("/api/usuarios", (req, res) ->
        {
            res.type("application/json; charset=utf-8");
            return gson.toJson(usuariosApi);
        });

        // Cadastro de um novo usuario simples em memoria
        // POST /api/usuarios
        post("/api/usuarios", (req, res) ->
        {
            res.type("application/json; charset=utf-8");

            UsuarioApi novo = gson.fromJson(req.body(), UsuarioApi.class);
            if (novo == null || novo.email == null)
            {
                res.status(400);
                return "{\"success\":false,\"message\":\"Dados invalidos\"}";
            }

            novo.senha = hashSenha(novo.senha);

            long novoId = usuariosApi.stream()
                    .mapToLong(u -> u.id)
                    .max()
                    .orElse(0L) + 1L;
            novo.id = novoId;
            usuariosApi.add(novo);

            res.status(201);
            return "{\"success\":true,\"message\":\"Usuario cadastrado com sucesso\"}";
        });

        // Login de usuario a partir de email e senha
        // POST /api/login
        post("/api/login", (req, res) ->
        {
            res.type("application/json; charset=utf-8");

            LoginReq body = gson.fromJson(req.body(), LoginReq.class);
            if (body == null || body.email == null || body.senha == null)
            {
                res.status(400);
                return "{\"success\":false,\"error\":\"Dados de login invalidos\"}";
            }

            String senhaHash = hashSenha(body.senha);

            UsuarioApi encontrado = null;
            for (UsuarioApi u : usuariosApi)
            {
                if (u.email.equals(body.email) && u.senha.equals(senhaHash) && u.ativo)
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

            java.util.Map<String, Object> resp = new java.util.HashMap<>();
            resp.put("success", true);
            resp.put("message", "Login realizado com sucesso");

            java.util.Map<String, Object> usuario = new java.util.HashMap<>();
            usuario.put("id", encontrado.id);
            usuario.put("nome", encontrado.nome);
            usuario.put("email", encontrado.email);
            usuario.put("tipo", encontrado.tipo);
            usuario.put("ativo", encontrado.ativo);

            resp.put("usuario", usuario);

            return gson.toJson(resp);
        });
    }
}
