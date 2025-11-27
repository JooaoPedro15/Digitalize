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
 * Classe responsável por registrar e montar todas as rotas da API.
 * 
 * Funcionalidades expostas:
 * - /health: Verificação de saúde da API
 * - /canais: CRUD básico de canais
 * - /importacoes: Inserção/atualização de importações
 * - /posts: Inserção/atualização de posts e listagem por canal e período
 * - /empresa/:cnpj/guia-postagem: Geração do Guia de Postagem (Sistema
 * Inteligente)
 */
public class Routes {
    // Gson compartilhado para serialização e desserialização JSON
    public static final Gson gson = ApiConfig.gson;

    // Aplica um hash (SHA-256) na senha antes de armazenar ou comparar.
    // Assim, a senha verdadeira não fica exposta em texto puro na aplicação.
    private static String hashSenha(String senhaPura) {
        if (senhaPura == null) {
            return null;
        }
        try {
            // Cria um objeto que sabe calcular SHA-256
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // Converte a String da senha para bytes usando UTF-8
            byte[] hashBytes = md.digest(senhaPura.getBytes(StandardCharsets.UTF_8));

            // Transforma os bytes em uma String hexadecimal (ex: "8d969e...")
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }

            // Retorna o hash em formato de texto
            return sb.toString();
        } catch (Exception e) {
            // Em um sistema maior, seria melhor logar isso direito.
            // Aqui simplificamos lançando uma RuntimeException.
            throw new RuntimeException("Erro ao calcular hash da senha", e);
        }
    }

    /**
     * Representa um usuário da API. Esta classe foi movida para o escopo de
     * Routes para evitar problemas de serialização com classes locais
     * definidas dentro de métodos. Os campos são públicos para que o Gson
     * consiga serializar sem a necessidade de getters e setters.
     */
    public static class UsuarioApi {
        public long id;
        public String nome;
        public String email;
        public String senha;
        public String tipo; // "admin" ou "usuario"
        public boolean ativo;

        public UsuarioApi(long id, String nome, String email, String senha, String tipo) {
            this.id = id;
            this.nome = nome;
            this.email = email;
            this.senha = senha;
            this.tipo = tipo;
            this.ativo = true;
        }
    }

    // Lista em memória de usuários de teste. Ela é inicializada uma única vez
    // quando a classe Routes é carregada. Os hashes de senha são gerados
    // usando o método hashSenha() para manter a consistência com o login.
    private static final java.util.List<UsuarioApi> usuariosApi = new java.util.ArrayList<>();
    static {
        usuariosApi.add(new UsuarioApi(1L, "Admin", "admin@teste.com", hashSenha("123456"), "admin"));
        usuariosApi.add(new UsuarioApi(2L, "Usuario", "user@teste.com", hashSenha("123456"), "usuario"));
    }

    /**
     * Representa os dados de login enviados pelo front-end.
     * Os campos são públicos para permitir a desserialização automática
     * pelo Gson. Esta classe está aqui para evitar que uma definição
     * local dentro da rota de login cause problemas de visibilidade.
     */
    public static class LoginReq {
        public String email;
        public String senha;
        public LoginReq() {}
    }

    // -------------------------------------------------------
    // Listas em memória para outras entidades simples
    //
    // Estas listas simulam um armazenamento temporário para
    // empresas e avaliações. Elas são usadas pelas rotas
    // abaixo para demonstrar um fluxo completo de cadastro
    // de empresa e criação de avaliações sem depender de
    // um servidor Node.js ou de um banco de dados. Em uma
    // aplicação real, estas informações estariam em um
    // repositório persistente.
    private static final java.util.List<java.util.Map<String, Object>> empresasApi = new java.util.ArrayList<>();
    private static final java.util.List<java.util.Map<String, Object>> avaliacoesApi = new java.util.ArrayList<>();

    /**
     * Monta todas as rotas da aplicação.
     * Executa a migração inicial do schema do banco.
     */
    public static void mount() {
        // Executa migração do banco (criação de tabelas se não existirem)
        System.out.println("Migrating database...");
        SchemaMigrator.migrate();

        // Service do sistema inteligente (guia de postagem)
        GuiaService guiaService = new GuiaService();

        /**
         * Rota de verificação da saúde da API
         * GET /health
         */
        get("/health", (req, res) -> {
            res.type("application/json; charset=utf-8");
            return "{\"status\":\"ok\"}";
        });

        /**
         * Criação de um canal
         * POST /canais
         * Corpo esperado: JSON representando um Canal
         * Retorna o ID do canal criado
         */
        post("/canais", (req, res) -> {
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
        get("/canais/:cnpj", (req, res) -> {
            res.type("application/json; charset=utf-8");

            String cnpj = req.params(":cnpj");
            return gson.toJson(new CanalDAO().listByEmpresa(cnpj));
        });

        /**
         * Inserção/atualização de importações
         * POST /importacoes
         * Corpo esperado: JSON representando uma Importacao
         */
        post("/importacoes", (req, res) -> {
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
        post("/posts", (req, res) -> {
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
        get("/posts/:canalId", (req, res) -> {
            res.type("application/json; charset=utf-8");

            long canalId = Long.parseLong(req.params(":canalId"));

            // Recupera parâmetros de data da query string
            String startParam = req.queryParams("start");
            String endParam = req.queryParams("end");

            // Converte strings para java.sql.Date (formato "yyyy-MM-dd")
            Date start = Date.valueOf(startParam);
            Date end = Date.valueOf(endParam);

            return gson.toJson(new PostDAO().listByCanal(canalId, start, end));
        });

        /**
         * Geração do "Guia de Postagem" em JSON para um CNPJ.
         * Ex.: GET http://localhost:8080/empresa/00000000000001/guia-postagem
         */
        get("/empresa/:cnpj/guia-postagem", (req, res) -> {
            res.type("application/json; charset=utf-8");

            String cnpj = req.params(":cnpj"); // lê o CNPJ da URL

            try {
                // Delegamos a lógica para o GuiaService (que usa AzureOpenAIClient ou fallback)
                return guiaService.gerarGuiaParaEmpresa(cnpj);
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);

                String msg = e.getMessage();
                if (msg == null) {
                    msg = "Erro interno ao gerar guia.";
                }
                // Evita quebrar o JSON com aspas duplas
                msg = msg.replace("\"", "'");

                return "{\"erro\":\"" + msg + "\"}";
            }
        });

        get("/debug/azure-env", (req, res) -> {
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

        // ============================
        // AUTENTICACAO SIMPLES /API
        // ============================

        // GET /api/usuarios -> usado no register() para ver se o email já existe
        get("/api/usuarios", (req, res) -> {
            res.type("application/json; charset=utf-8");
            // Retorna a lista de usuários de teste.
            return gson.toJson(usuariosApi);
        });

        // POST /api/usuarios -> usado no register() para "cadastrar" novo usuário
        post("/api/usuarios", (req, res) -> {
            res.type("application/json; charset=utf-8");

            UsuarioApi novo = gson.fromJson(req.body(), UsuarioApi.class);
            if (novo == null || novo.email == null) {
                res.status(400);
                return "{\"success\":false,\"message\":\"Dados invalidos\"}";
            }

            // Converte a senha para hash antes de armazenar
            novo.senha = hashSenha(novo.senha);

            // Gera um novo ID baseado na maior chave atual
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
        post("/api/login", (req, res) -> {
            res.type("application/json; charset=utf-8");

            // Desserializa o corpo recebido em um LoginReq
            LoginReq body = gson.fromJson(req.body(), LoginReq.class);
            if (body == null || body.email == null || body.senha == null) {
                res.status(400);
                return "{\"success\":false,\"error\":\"Dados de login invalidos\"}";
            }

            // Calcula o hash da senha informada no login
            String senhaHash = hashSenha(body.senha);

            // Procura usuário com email + hash da senha
            UsuarioApi encontrado = null;
            for (UsuarioApi u : usuariosApi) {
                if (u.email.equals(body.email) && u.senha.equals(senhaHash) && u.ativo) {
                    encontrado = u;
                    break;
                }
            }

            if (encontrado == null) {
                res.status(401);
                return "{\"success\":false,\"error\":\"Email ou senha incorretos\"}";
            }

            // Monta objeto de resposta: { success, message, usuario }
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

        // =======================================================
        // Rotas adicionais para integração com o front-end unificado
        // =======================================================

        // Lista de empresas (GET /api/empresas)
        get("/api/empresas", (req, res) -> {
            res.type("application/json; charset=utf-8");
            String respEmail = req.queryParams("responsavelEmail");
            String email = req.queryParams("email");
            if (respEmail != null || email != null) {
                java.util.List<java.util.Map<String, Object>> filtradas = new java.util.ArrayList<>();
                for (java.util.Map<String, Object> e : empresasApi) {
                    Object rMail = e.get("responsavelEmail");
                    Object mail = e.get("email");
                    if (respEmail != null && respEmail.equals(String.valueOf(rMail))) {
                        filtradas.add(e);
                    } else if (email != null && email.equals(String.valueOf(mail))) {
                        filtradas.add(e);
                    }
                }
                return gson.toJson(filtradas);
            }
            return gson.toJson(empresasApi);
        });

        // Cadastrar nova empresa (POST /admin/cadastro)
        post("/admin/cadastro", (req, res) -> {
            res.type("application/json; charset=utf-8");
            java.util.Map<String, Object> novaEmpresa = gson.fromJson(req.body(), java.util.Map.class);
            if (novaEmpresa == null) {
                res.status(400);
                return "{\"mensagem\":\"Dados invalidos\"}";
            }
            // Gera ID único baseado em timestamp
            String id = String.valueOf(System.currentTimeMillis());
            novaEmpresa.put("id", id);
            // Define status pendente se não informado
            Object st = novaEmpresa.get("status");
            if (st == null) {
                novaEmpresa.put("status", "pendente");
            }
            novaEmpresa.put("dataCadastro", java.time.Instant.now().toString());
            empresasApi.add(novaEmpresa);
            res.status(200);
            java.util.Map<String, Object> resp = new java.util.HashMap<>();
            resp.put("mensagem", "Cadastro salvo com sucesso");
            resp.put("id", id);
            return gson.toJson(resp);
        });

        // Lista de avaliações (GET /api/avaliacoes)
        get("/api/avaliacoes", (req, res) -> {
            res.type("application/json; charset=utf-8");
            return gson.toJson(avaliacoesApi);
        });

        // Obter avaliações de uma empresa (GET /api/avaliacoes/empresa/:empresaId)
        get("/api/avaliacoes/empresa/:empresaId", (req, res) -> {
            res.type("application/json; charset=utf-8");
            String empresaId = req.params(":empresaId");
            java.util.List<java.util.Map<String, Object>> out = new java.util.ArrayList<>();
            for (java.util.Map<String, Object> av : avaliacoesApi) {
                Object eid = av.get("empresaId");
                if (empresaId != null && empresaId.equals(String.valueOf(eid))) {
                    out.add(av);
                }
            }
            return gson.toJson(out);
        });

        // Cadastrar nova avaliação (POST /api/avaliacoes)
        post("/api/avaliacoes", (req, res) -> {
            res.type("application/json; charset=utf-8");
            java.util.Map<String, Object> novaAvaliacao = gson.fromJson(req.body(), java.util.Map.class);
            if (novaAvaliacao == null) {
                res.status(400);
                return "{\"erro\":\"Dados invalidos\"}";
            }
            String id = String.valueOf(System.currentTimeMillis());
            novaAvaliacao.put("id", id);
            novaAvaliacao.put("dataAvaliacao", java.time.Instant.now().toString());
            avaliacoesApi.add(novaAvaliacao);
            res.status(201);
            return gson.toJson(novaAvaliacao);
        });

        // Endpoints vazios para produtos e parceiros (evita erros de conexão)
        get("/api/produtos", (req, res) -> {
            res.type("application/json; charset=utf-8");
            return "[]";
        });

        get("/api/parceiros", (req, res) -> {
            res.type("application/json; charset=utf-8");
            return "[]";
        });
    }
}
