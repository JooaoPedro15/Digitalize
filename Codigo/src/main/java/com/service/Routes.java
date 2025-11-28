package com.service;

import com.dao.SchemaMigrator;
import com.dao.CanalDAO;
import com.dao.ImportacaoDAO;
import com.dao.PostDAO;
import com.dao.EmpresaDAO;

import com.model.Canal;
import com.model.Importacao;
import com.model.Post;
import com.model.Empresa;

import com.google.gson.Gson;

import static spark.Spark.*;

import java.sql.Date;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

/**
 * Classe responsavel por registrar e montar todas as rotas da API.
 */
public class Routes
{
    public static final Gson gson = ApiConfig.gson;

    // ===========================
    // Helpers de autenticacao
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

    /**
     * Remove tudo que nao for digito de um CNPJ.
     * Ex.: "12.345.678/0001-90" -> "12345678000190"
     */
    private static String limparCnpj(String raw)
    {
        if (raw == null)
        {
            return null;
        }
        return raw.replaceAll("\\D", "");
    }

    /** Representa um usuario de teste para login no front. */
    public static class UsuarioApi
    {
        public long id;
        public String nome;
        public String email;
        public String senha;
        public String tipo;
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

    /** Payload de login enviado pelo front. */
    public static class LoginReq
    {
        public String email;
        public String senha;
        public LoginReq() {}
    }

    // Lista em memoria de usuarios de teste e avaliacoes.
    private static final java.util.List<UsuarioApi> usuariosApi   = new java.util.ArrayList<>();
    private static final java.util.List<java.util.Map<String, Object>> avaliacoesApi = new java.util.ArrayList<>();

    // Inicializa usuarios fake.
    static
    {
        usuariosApi.add(new UsuarioApi(1L, "Admin",   "admin@teste.com", hashSenha("123456"), "admin"));
        usuariosApi.add(new UsuarioApi(2L, "Usuario", "user@teste.com",  hashSenha("123456"), "usuario"));
    }

    /**
     * Helper para mapear Empresa -> JSON compativel com o front.
     */
    private static java.util.Map<String, Object> empresaToApi(Empresa e)
    {
        java.util.Map<String, Object> m = new java.util.HashMap<>();

        // ID da empresa no front e o proprio CNPJ
        m.put("id", e.getCnpj());
        m.put("cnpj", e.getCnpj());

        // Campos de nome / razao em ambos os formatos
        m.put("nomeFantasia", e.getNome_fantasia());
        m.put("nome_fantasia", e.getNome_fantasia());
        m.put("razaoSocial", e.getRazao_social());
        m.put("razao_social", e.getRazao_social());

        m.put("segmento", e.getSegmento());
        m.put("endereco", e.getEndereco());

        // Status textual (pendente / aprovada / rejeitada)
        m.put("status", e.getStatus());

        // Emails usados pelos filtros e pela pagina "Minha Empresa"
        m.put("responsavelEmail", e.getResponsavel_email());
        m.put("email", e.getEmail_contato());

        return m;
    }

    public static void mount()
    {
        // Executa as migracoes de schema do banco.
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

        // --------------- Importacoes ---------------
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
        // AUTENTICACAO SIMPLES (em memoria)
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
        // EMPRESAS – usando PostgreSQL
        // ==========================================

        // GET /api/empresas – lista empresas com filtros (_limit, email, responsavelEmail)
        get("/api/empresas", (req, res) ->
        {
            res.type("application/json; charset=utf-8");

            String respEmail  = req.queryParams("responsavelEmail");
            String email      = req.queryParams("email");
            String limitParam = req.queryParams("_limit");

            String respEmailLower = respEmail != null ? respEmail.toLowerCase() : null;
            String emailLower     = email     != null ? email.toLowerCase()     : null;

            EmpresaDAO dao = new EmpresaDAO();
            java.util.List<Empresa> todas = dao.listar();
            java.util.List<java.util.Map<String, Object>> resultado = new java.util.ArrayList<>();

            for (Empresa e : todas)
            {
                String respLower = e.getResponsavel_email() != null
                        ? e.getResponsavel_email().toLowerCase()
                        : null;
                String mailLower = e.getEmail_contato() != null
                        ? e.getEmail_contato().toLowerCase()
                        : null;

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
                        if (respEmailLower.equals(respLower))
                        {
                            match = true;
                        }
                    }
                    if (!match && emailLower != null)
                    {
                        if (emailLower.equals(mailLower) || emailLower.equals(respLower))
                        {
                            match = true;
                        }
                    }
                }

                if (match)
                {
                    resultado.add(empresaToApi(e));
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
                catch (NumberFormatException ignored) {}
            }

            return gson.toJson(resultado);
        });

        // POST /admin/cadastro – cria empresa com status "pendente"
        post("/admin/cadastro", (req, res) ->
        {
            res.type("application/json; charset=utf-8");

            java.util.Map<String, Object> body = gson.fromJson(req.body(), java.util.Map.class);
            if (body == null)
            {
                res.status(400);
                return "{\"mensagem\":\"Dados invalidos\"}";
            }

            Empresa e = new Empresa();

            // CNPJ obrigatorio
            Object cnpjObj = body.get("cnpj");
            if (cnpjObj == null)
            {
                res.status(400);
                return "{\"mensagem\":\"CNPJ obrigatorio\"}";
            }

            String cnpjRaw   = String.valueOf(cnpjObj);
            String cnpjLimpo = limparCnpj(cnpjRaw);

            if (cnpjLimpo == null || cnpjLimpo.length() != 14)
            {
                res.status(400);
                return "{\"mensagem\":\"CNPJ invalido: use 14 digitos\"}";
            }

            e.setCnpj(cnpjLimpo);

            // Nome fantasia / razao social / segmento / endereco
            Object nf = body.get("nomeFantasia");
            if (nf == null) { nf = body.get("nome_fantasia"); }
            e.setNome_fantasia(nf != null ? String.valueOf(nf) : null);

            Object rz = body.get("razaoSocial");
            if (rz == null) { rz = body.get("razao_social"); }
            e.setRazao_social(rz != null ? String.valueOf(rz) : null);

            Object seg = body.get("segmento");
            e.setSegmento(seg != null ? String.valueOf(seg) : null);

            Object end = body.get("endereco");
            e.setEndereco(end != null ? String.valueOf(end) : null);

            // Status default = pendente
            e.setStatus("pendente");

            // E-mail do responsavel
            String respEmail = null;
            Object respEmailObj = body.get("responsavelEmail");
            if (respEmailObj != null)
            {
                respEmail = String.valueOf(respEmailObj);
            }
            Object respObj = body.get("responsavel");
            if (respEmail == null && respObj instanceof java.util.Map<?, ?>)
            {
                Object inside = ((java.util.Map<?, ?>) respObj).get("email");
                if (inside != null)
                {
                    respEmail = String.valueOf(inside);
                }
            }
            e.setResponsavel_email(respEmail);

            // E-mail de contato: se nao vier no body, usa o do responsavel
            String emailContato = null;
            Object emailObj = body.get("email");
            if (emailObj != null)
            {
                emailContato = String.valueOf(emailObj);
            }
            if (emailContato == null)
            {
                emailContato = respEmail;
            }
            e.setEmail_contato(emailContato);

            // Salva no banco
            EmpresaDAO dao = new EmpresaDAO();
            boolean ok = dao.insert(e);
            if (!ok)
            {
                res.status(500);
                return "{\"mensagem\":\"Erro ao salvar empresa\"}";
            }

            // (Opcional) Canal generico eh criado dentro de EmpresaDAO.insert
            // usando CanalDAO.criarCanalGenericoSeNaoExistir(e.getCnpj()).

            java.util.Map<String, Object> resp = new java.util.HashMap<>();
            resp.put("mensagem", "Cadastro salvo com sucesso");
            resp.put("id", e.getCnpj());

            return gson.toJson(resp);
        });

        // ----------------- Avaliacoes -----------------
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
            EmpresaDAO dao = new EmpresaDAO();
            java.util.List<java.util.Map<String, Object>> out = new java.util.ArrayList<>();
            for (Empresa e : dao.listar())
            {
                String st = e.getStatus() != null ? e.getStatus().toLowerCase() : "";
                if (st.startsWith("aprov"))
                {
                    out.add(empresaToApi(e));
                }
            }
            return gson.toJson(out);
        });

        get("/api/empresas/rejeitadas", (req, res) ->
        {
            res.type("application/json; charset=utf-8");
            EmpresaDAO dao = new EmpresaDAO();
            java.util.List<java.util.Map<String, Object>> out = new java.util.ArrayList<>();
            for (Empresa e : dao.listar())
            {
                String st = e.getStatus() != null ? e.getStatus().toLowerCase() : "";
                if (st.startsWith("rejeit"))
                {
                    out.add(empresaToApi(e));
                }
            }
            return gson.toJson(out);
        });

        get("/api/empresas/pendentes", (req, res) ->
        {
            res.type("application/json; charset=utf-8");
            EmpresaDAO dao = new EmpresaDAO();
            java.util.List<java.util.Map<String, Object>> out = new java.util.ArrayList<>();
            for (Empresa e : dao.listar())
            {
                String st = e.getStatus() != null ? e.getStatus().toLowerCase() : "";
                if (st.isEmpty() || st.equals("pendente"))
                {
                    out.add(empresaToApi(e));
                }
            }
            return gson.toJson(out);
        });

        // PUT /api/empresas/:id/status – aprovar / rejeitar
        put("/api/empresas/:id/status", (req, res) ->
        {
            res.type("application/json; charset=utf-8");

            String cnpj = req.params(":id");
            java.util.Map<String, Object> body = gson.fromJson(req.body(), java.util.Map.class);

            if (body == null || !body.containsKey("status"))
            {
                res.status(400);
                return "{\"error\":\"Status invalido\"}";
            }

            String novoStatus = String.valueOf(body.get("status"));
            String sLower = novoStatus.toLowerCase();

            if (!(sLower.startsWith("aprov") || sLower.startsWith("rejeit")))
            {
                res.status(400);
                return "{\"error\":\"Status invalido\"}";
            }

            EmpresaDAO dao = new EmpresaDAO();
            Empresa e = dao.get(cnpj);
            if (e == null)
            {
                res.status(404);
                return "{\"error\":\"Empresa nao encontrada\"}";
            }

            if (sLower.startsWith("aprov"))
            {
                e.setStatus("aprovada");
            }
            else
            {
                e.setStatus("rejeitada");
            }

            if (!dao.update(e))
            {
                res.status(500);
                return "{\"error\":\"Falha ao atualizar empresa\"}";
            }

            return "{\"message\":\"Empresa atualizada com sucesso\"}";
        });

        // PUT /api/empresas/:id – editar empresa (status volta para pendente)
        put("/api/empresas/:id", (req, res) ->
        {
            res.type("application/json; charset=utf-8");

            String cnpj = req.params(":id");
            java.util.Map<String, Object> body = gson.fromJson(req.body(), java.util.Map.class);

            if (body == null)
            {
                res.status(400);
                return "{\"error\":\"Dados invalidos\"}";
            }

            EmpresaDAO dao = new EmpresaDAO();
            Empresa e = dao.get(cnpj);
            if (e == null)
            {
                res.status(404);
                return "{\"error\":\"Empresa nao encontrada\"}";
            }

            // Atualiza campos recebidos
            Object nf = body.get("nomeFantasia");
            if (nf == null) { nf = body.get("nome_fantasia"); }
            if (nf != null) { e.setNome_fantasia(String.valueOf(nf)); }

            Object rz = body.get("razaoSocial");
            if (rz == null) { rz = body.get("razao_social"); }
            if (rz != null) { e.setRazao_social(String.valueOf(rz)); }

            Object seg = body.get("segmento");
            if (seg != null) { e.setSegmento(String.valueOf(seg)); }

            Object end = body.get("endereco");
            if (end != null) { e.setEndereco(String.valueOf(end)); }

            // Status volta para pendente quando edita
            e.setStatus("pendente");

            // E-mails se vierem no body
            Object respEmailObj = body.get("responsavelEmail");
            if (respEmailObj != null)
            {
                e.setResponsavel_email(String.valueOf(respEmailObj));
            }

            Object emailObj = body.get("email");
            if (emailObj != null)
            {
                e.setEmail_contato(String.valueOf(emailObj));
            }

            if (!dao.update(e))
            {
                res.status(500);
                return "{\"error\":\"Falha ao atualizar empresa\"}";
            }

            res.status(200);
            return gson.toJson(empresaToApi(e));
        });

        // DELETE /api/empresas/:id – excluir empresa
        delete("/api/empresas/:id", (req, res) ->
        {
            res.type("application/json; charset=utf-8");

            String cnpj = req.params(":id");
            EmpresaDAO dao = new EmpresaDAO();

            if (!dao.remove(cnpj))
            {
                res.status(404);
                return "{\"error\":\"Empresa nao encontrada\"}";
            }

            res.status(200);
            return "{\"message\":\"Empresa excluida com sucesso\"}";
        });

        // GET /api/empresas/usuario/:email – usado na tela "Minha Empresa"
        get("/api/empresas/usuario/:email", (req, res) ->
        {
            res.type("application/json; charset=utf-8");

            String emailUsr = req.params(":email");
            if (emailUsr == null)
            {
                return "[]";
            }

            String emailLower = emailUsr.toLowerCase();

            EmpresaDAO dao = new EmpresaDAO();
            java.util.List<java.util.Map<String, Object>> out = new java.util.ArrayList<>();

            for (Empresa e : dao.listar())
            {
                String respLower = e.getResponsavel_email() != null
                        ? e.getResponsavel_email().toLowerCase()
                        : null;
                String mailLower = e.getEmail_contato() != null
                        ? e.getEmail_contato().toLowerCase()
                        : null;

                if (emailLower.equals(respLower) || emailLower.equals(mailLower))
                {
                    out.add(empresaToApi(e));
                }
            }

            return gson.toJson(out);
        });

        // Placeholders para nao quebrar o front em telas que ainda nao existem
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
