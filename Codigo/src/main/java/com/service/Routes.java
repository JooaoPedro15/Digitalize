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
 */
public class Routes
{
    public static final Gson gson = ApiConfig.gson;

    // ===========================
    // Helpers de autenticação
    // ===========================
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

    public static class UsuarioApi
    {
        public long id;
        public String nome;
        public String email;
        public String senha;
        public String tipo; // admin | usuario
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

    public static class LoginReq
    {
        public String email;
        public String senha;

        public LoginReq()
        {
        }
    }

    // ===========================
    // “Banco” em memória
    // ===========================
    private static final java.util.List<UsuarioApi> usuariosApi = new java.util.ArrayList<>();
    private static final java.util.List<java.util.Map<String, Object>> empresasApi  = new java.util.ArrayList<>();
    private static final java.util.List<java.util.Map<String, Object>> avaliacoesApi = new java.util.ArrayList<>();

    static
    {
        usuariosApi.add(new UsuarioApi(1L, "Admin", "admin@teste.com", hashSenha("123456"), "admin"));
        usuariosApi.add(new UsuarioApi(2L, "Usuario", "user@teste.com",  hashSenha("123456"), "usuario"));
    }

    // ===========================
    // Rotas
    // ===========================
    public static void mount()
    {
        System.out.println("Migrating database...");
        SchemaMigrator.migrate();

        GuiaService guiaService = new GuiaService();

        // ----------------- Health -----------------
        get("/health", (req, res) ->
        {
            res.type("application/json; charset=utf-8");
            return "{\"status\":\"ok\"}";
        });

        // ----------------- Canais -----------------
        post("/canais", (req, res) ->
        {
            res.type("application/json; charset=utf-8");
            Canal c = gson.fromJson(req.body(), Canal.class);
            long id = new CanalDAO().insert(c);
            res.status(201);
            return "{\"canal_id\":" + id + "}";
        });

        get("/canais/:cnpj", (req, res) ->
        {
            res.type("application/json; charset=utf-8");
            String cnpj = req.params(":cnpj");
            return gson.toJson(new CanalDAO().listByEmpresa(cnpj));
        });

        // --------------- Importações ---------------
        post("/importacoes", (req, res) ->
        {
            res.type("application/json; charset=utf-8");
            Importacao imp = gson.fromJson(req.body(), Importacao.class);
            new ImportacaoDAO().upsert(imp);
            res.status(201);
            return "{\"ok\":true}";
        });

        // ------------------ Posts ------------------
        post("/posts", (req, res) ->
        {
            res.type("application/json; charset=utf-8");
            Post p = gson.fromJson(req.body(), Post.class);
            new PostDAO().upsert(p);
            res.status(201);
            return "{\"ok\":true}";
        });

        get("/posts/:canalId", (req, res) ->
        {
            res.type("application/json; charset=utf-8");

            long canalId = Long.parseLong(req.params(":canalId"));
            String startParam = req.queryParams("start");
            String endParam   = req.queryParams("end");

            Date start = Date.valueOf(startParam);
            Date end   = Date.valueOf(endParam);

            return gson.toJson(new PostDAO().listByCanal(canalId, start, end));
        });

        // -------- Guia de Postagem (SI) -----------
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

        // --------------- Debug Azure ---------------
        get("/debug/azure-env", (req, res) ->
        {
            res.type("application/json; charset=utf-8");

            String endpoint = System.getenv("AZURE_OPENAI_ENDPOINT");
            String key      = System.getenv("AZURE_OPENAI_API_KEY");
            String ver      = System.getenv("AZURE_OPENAI_API_VERSION");
            String chatDep  = System.getenv("AZURE_OPENAI_CHAT_DEPLOYMENT");
            String embDep   = System.getenv("AZURE_OPENAI_EMBEDDING_DEPLOYMENT");

            String keyPreview = (key == null) ? null :
                    (key.length() > 6 ? key.substring(0, 6) + "..." : key);

            return "{"
                    + "\"endpoint\":\"" + String.valueOf(endpoint) + "\","
                    + "\"apiKey\":\"" + String.valueOf(keyPreview) + "\","
                    + "\"apiVersion\":\"" + String.valueOf(ver) + "\","
                    + "\"chatDep\":\"" + String.valueOf(chatDep) + "\","
                    + "\"embDep\":\"" + String.valueOf(embDep) + "\""
                    + "}";
        });

        // ==========================================
        // AUTENTICAÇÃO SIMPLES
        // ==========================================
        get("/api/usuarios", (req, res) ->
        {
            res.type("application/json; charset=utf-8");
            return gson.toJson(usuariosApi);
        });

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

        // ==========================================
        // EMPRESAS – compatível com front unificado
        // ==========================================

        // GET /api/empresas  (lista + filtros)
        get("/api/empresas", (req, res) ->
        {
            res.type("application/json; charset=utf-8");

            String respEmail = req.queryParams("responsavelEmail");
            String email     = req.queryParams("email");
            String limitParam = req.queryParams("_limit");

            String respEmailLower = respEmail != null ? respEmail.toLowerCase() : null;
            String emailLower     = email     != null ? email.toLowerCase()     : null;

            java.util.List<java.util.Map<String, Object>> resultado = new java.util.ArrayList<>();

            for (java.util.Map<String, Object> e : empresasApi)
            {
                Object rMailObj = e.get("responsavelEmail");
                Object mailObj  = e.get("email");
                Object respObj  = e.get("responsavel");

                String rMailLower = rMailObj != null ? String.valueOf(rMailObj).toLowerCase() : null;
                String mailLower  = mailObj  != null ? String.valueOf(mailObj).toLowerCase()  : null;
                String respInsideLower = null;

                if (respObj instanceof java.util.Map<?, ?>)
                {
                    Object inside = ((java.util.Map<?, ?>) respObj).get("email");
                    if (inside != null)
                    {
                        respInsideLower = String.valueOf(inside).toLowerCase();
                    }
                }

                boolean match = false;

                if (respEmailLower == null && emailLower == null)
                {
                    // sem filtros -> lista tudo
                    match = true;
                }
                else
                {
                    if (respEmailLower != null)
                    {
                        if (respEmailLower.equals(rMailLower) ||
                            respEmailLower.equals(respInsideLower))
                        {
                            match = true;
                        }
                    }
                    if (!match && emailLower != null)
                    {
                        if (emailLower.equals(mailLower) ||
                            emailLower.equals(respInsideLower) ||
                            emailLower.equals(rMailLower))
                        {
                            match = true;
                        }
                    }
                }

                if (match)
                {
                    resultado.add(e);
                }
            }

            if (limitParam != null)
            {
                try
                {
                    int limit = Integer.parseInt(limitParam);
                    if (limit > 0 && resultado.size() > limit)
                    {
                        resultado = new java.util.ArrayList<>(resultado.subList(0, limit));
                    }
                }
                catch (NumberFormatException ex)
                {
                    // ignora _limit invalido
                }
            }

            return gson.toJson(resultado);
        });

        // POST /admin/cadastro  (criar empresa)
        post("/admin/cadastro", (req, res) ->
        {
            res.type("application/json; charset=utf-8");

            java.util.Map<String, Object> novaEmpresa = gson.fromJson(req.body(), java.util.Map.class);
            if (novaEmpresa == null)
            {
                res.status(400);
                return "{\"mensagem\":\"Dados invalidos\"}";
            }

            // id
            String id = String.valueOf(System.currentTimeMillis());
            novaEmpresa.put("id", id);

            // se vier responsavel.email, replicar em responsavelEmail e email
            Object respObj = novaEmpresa.get("responsavel");
            if (respObj instanceof java.util.Map<?, ?>)
            {
                Object inside = ((java.util.Map<?, ?>) respObj).get("email");
                if (inside != null)
                {
                    String emailResp = String.valueOf(inside);
                    if (!novaEmpresa.containsKey("responsavelEmail"))
                    {
                        novaEmpresa.put("responsavelEmail", emailResp);
                    }
                    if (!novaEmpresa.containsKey("email"))
                    {
                        novaEmpresa.put("email", emailResp);
                    }
                }
            }

            // status padrão
            Object st = novaEmpresa.get("status");
            if (st == null)
            {
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

        // ----------------- Avaliações -----------------
        get("/api/avaliacoes", (req, res) ->
        {
            res.type("application/json; charset=utf-8");
            return gson.toJson(avaliacoesApi);
        });

        get("/api/avaliacoes/empresa/:empresaId", (req, res) ->
        {
            res.type("application/json; charset=utf-8");
            String empresaId = req.params(":empresaId");
            java.util.List<java.util.Map<String, Object>> out = new java.util.ArrayList<>();
            for (java.util.Map<String, Object> av : avaliacoesApi)
            {
                Object eid = av.get("empresaId");
                if (empresaId != null && empresaId.equals(String.valueOf(eid)))
                {
                    out.add(av);
                }
            }
            return gson.toJson(out);
        });

        post("/api/avaliacoes", (req, res) ->
        {
            res.type("application/json; charset=utf-8");
            java.util.Map<String, Object> nova = gson.fromJson(req.body(), java.util.Map.class);
            if (nova == null)
            {
                res.status(400);
                return "{\"erro\":\"Dados invalidos\"}";
            }
            String id = String.valueOf(System.currentTimeMillis());
            nova.put("id", id);
            nova.put("dataAvaliacao", java.time.Instant.now().toString());
            avaliacoesApi.add(nova);
            res.status(201);
            return gson.toJson(nova);
        });

        // -------------- Status de empresas --------------
        get("/api/empresas/aprovadas", (req, res) ->
        {
            res.type("application/json; charset=utf-8");
            java.util.List<java.util.Map<String, Object>> out = new java.util.ArrayList<>();
            for (java.util.Map<String, Object> e : empresasApi)
            {
                Object stObj = e.get("status");
                if (stObj != null)
                {
                    String st = String.valueOf(stObj).toLowerCase();
                    if (st.equals("aprovada") || st.equals("aprovado"))
                    {
                        out.add(e);
                    }
                }
            }
            return gson.toJson(out);
        });

        get("/api/empresas/rejeitadas", (req, res) ->
        {
            res.type("application/json; charset=utf-8");
            java.util.List<java.util.Map<String, Object>> out = new java.util.ArrayList<>();
            for (java.util.Map<String, Object> e : empresasApi)
            {
                Object stObj = e.get("status");
                if (stObj != null)
                {
                    String st = String.valueOf(stObj).toLowerCase();
                    if (st.equals("rejeitada") || st.equals("rejeitado"))
                    {
                        out.add(e);
                    }
                }
            }
            return gson.toJson(out);
        });

        get("/api/empresas/pendentes", (req, res) ->
        {
            res.type("application/json; charset=utf-8");
            java.util.List<java.util.Map<String, Object>> out = new java.util.ArrayList<>();
            for (java.util.Map<String, Object> e : empresasApi)
            {
                Object stObj = e.get("status");
                String st = (stObj == null) ? "" : String.valueOf(stObj).toLowerCase();
                if (stObj == null || st.equals("pendente") || st.isEmpty())
                {
                    out.add(e);
                }
            }
            return gson.toJson(out);
        });

        // PUT /api/empresas/:id/status
        put("/api/empresas/:id/status", (req, res) ->
        {
            res.type("application/json; charset=utf-8");
            String id = req.params(":id");
            java.util.Map<String, Object> body = gson.fromJson(req.body(), java.util.Map.class);

            if (body == null || !body.containsKey("status"))
            {
                res.status(400);
                return "{\"error\":\"Status invalido\"}";
            }

            String status = String.valueOf(body.get("status"));
            String sLower = status.toLowerCase();
            if (!(sLower.startsWith("aprov") || sLower.startsWith("rejeit")))
            {
                res.status(400);
                return "{\"error\":\"Status invalido\"}";
            }
            // normaliza
            if (sLower.startsWith("aprov"))
            {
                status = "aprovada";
            }
            else
            {
                status = "rejeitada";
            }

            boolean found = false;
            for (java.util.Map<String, Object> e : empresasApi)
            {
                Object eid = e.get("id");
                if (id != null && id.equals(String.valueOf(eid)))
                {
                    e.put("status", status);
                    e.put("dataAtualizacao", java.time.Instant.now().toString());
                    found = true;
                    break;
                }
            }

            if (!found)
            {
                res.status(404);
                return "{\"error\":\"Empresa nao encontrada\"}";
            }

            return "{\"message\":\"Empresa " + status + " com sucesso\"}";
        });

        // PUT /api/empresas/:id
        put("/api/empresas/:id", (req, res) ->
        {
            res.type("application/json; charset=utf-8");
            String id = req.params(":id");
            java.util.Map<String, Object> dados = gson.fromJson(req.body(), java.util.Map.class);
            if (dados == null)
            {
                res.status(400);
                return "{\"error\":\"Dados invalidos\"}";
            }

            for (java.util.Map<String, Object> e : empresasApi)
            {
                Object eid = e.get("id");
                if (id != null && id.equals(String.valueOf(eid)))
                {
                    dados.put("id", id);
                    dados.put("status", "pendente");
                    if (e.containsKey("dataCadastro"))
                    {
                        dados.put("dataCadastro", e.get("dataCadastro"));
                    }
                    dados.put("dataAtualizacao", java.time.Instant.now().toString());
                    e.clear();
                    e.putAll(dados);
                    res.status(200);
                    return gson.toJson(e);
                }
            }

            res.status(404);
            return "{\"error\":\"Empresa nao encontrada\"}";
        });

        // DELETE /api/empresas/:id
        delete("/api/empresas/:id", (req, res) ->
        {
            res.type("application/json; charset=utf-8");
            String id = req.params(":id");

            java.util.Iterator<java.util.Map<String, Object>> it = empresasApi.iterator();
            while (it.hasNext())
            {
                java.util.Map<String, Object> e = it.next();
                Object eid = e.get("id");
                if (id != null && id.equals(String.valueOf(eid)))
                {
                    it.remove();
                    res.status(200);
                    return "{\"message\":\"Empresa excluida com sucesso\"}";
                }
            }

            res.status(404);
            return "{\"error\":\"Empresa nao encontrada\"}";
        });

        // GET /api/empresas/usuario/:email  (usado para mostrar “Minha Empresa”)
        get("/api/empresas/usuario/:email", (req, res) ->
        {
            res.type("application/json; charset=utf-8");
            String emailUsr = req.params(":email");
            if (emailUsr == null)
            {
                return "[]";
            }
            String emailLower = emailUsr.toLowerCase();

            java.util.List<java.util.Map<String, Object>> resultado = new java.util.ArrayList<>();

            for (java.util.Map<String, Object> e : empresasApi)
            {
                Object respEmailObj = e.get("responsavelEmail");
                Object emailObj     = e.get("email");
                Object respObj      = e.get("responsavel");

                String respLower  = respEmailObj != null ? String.valueOf(respEmailObj).toLowerCase() : null;
                String mailLower  = emailObj     != null ? String.valueOf(emailObj).toLowerCase()     : null;
                String insideLower = null;

                if (respObj instanceof java.util.Map<?, ?>)
                {
                    Object inside = ((java.util.Map<?, ?>) respObj).get("email");
                    if (inside != null)
                    {
                        insideLower = String.valueOf(inside).toLowerCase();
                    }
                }

                if (emailLower.equals(respLower) ||
                    emailLower.equals(mailLower) ||
                    emailLower.equals(insideLower))
                {
                    resultado.add(e);
                }
            }

            return gson.toJson(resultado);
        });

        // Placeholders para não quebrar o front
        get("/api/produtos", (req, res) ->
        {
            res.type("application/json; charset=utf-8");
            return "[]";
        });

        get("/api/parceiros", (req, res) ->
        {
            res.type("application/json; charset=utf-8");
            return "[]";
        });
    }
}
