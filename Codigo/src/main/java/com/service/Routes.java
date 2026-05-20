package com.service;

import com.dao.CanalDAO;
import com.dao.EmpresaDAO;
import com.dao.ImportacaoDAO;
import com.dao.PostDAO;
import com.dao.SchemaMigrator;
import com.dao.UsuarioDAO;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.model.Canal;
import com.model.Empresa;
import com.model.Importacao;
import com.model.Post;
import com.model.Usuario;
import spark.Request;
import spark.Response;

import static spark.Spark.*;

import java.sql.Date;

public class Routes {
    public static final Gson gson = ApiConfig.gson;

    public static void mount() {
        try {
            System.out.println("Iniciando migracao do banco...");
            SchemaMigrator.migrate();
            System.out.println("Migracao concluida com sucesso.");
        } catch (Exception e) {
            System.err.println("Aviso: erro na migracao. O sistema continuara rodando: " + e.getMessage());
        }

        GuiaService guiaService = new GuiaService();

        get("/health", (req, res) -> {
            res.type("application/json; charset=utf-8");
            return "{\"status\":\"ok\"}";
        });

        post("/api/login", (req, res) -> {
            res.type("application/json; charset=utf-8");
            try {
                JsonObject body = gson.fromJson(req.body(), JsonObject.class);
                String email = jsonString(body, "email");
                String senha = jsonString(body, "senha");

                if (!hasText(email) || !hasText(senha)) {
                    res.status(400);
                    return error("Informe email e senha.");
                }

                Usuario usuario = new UsuarioDAO().autenticar(email, senha);
                if (usuario == null) {
                    res.status(401);
                    return error("Email ou senha incorretos.");
                }

                usuario.setSenha(null);
                return gson.toJson(usuario);
            } catch (Exception e) {
                res.status(500);
                return error("Erro interno no servidor.");
            }
        });

        post("/api/usuarios", (req, res) -> {
            res.type("application/json; charset=utf-8");
            try {
                Usuario usuario = gson.fromJson(req.body(), Usuario.class);
                String erro = prepararUsuarioParaCadastro(usuario);
                if (erro != null) {
                    res.status(400);
                    return error(erro);
                }

                if (new UsuarioDAO().insert(usuario)) {
                    res.status(201);
                    return message("Usuario criado com sucesso.");
                }

                res.status(409);
                return error("Ja existe um usuario cadastrado com esse email.");
            } catch (Exception e) {
                res.status(500);
                return error("Erro interno ao criar usuario.");
            }
        });

        post("/api/empresas", (req, res) -> salvarEmpresa(req, res, "pendente"));
        post("/admin/cadastro", (req, res) -> salvarEmpresa(req, res, "aprovada"));

        get("/api/empresas", (req, res) -> {
            res.type("application/json; charset=utf-8");
            try {
                return gson.toJson(new EmpresaDAO().listar());
            } catch (Exception e) {
                res.status(500);
                return error("Erro ao listar empresas.");
            }
        });

        get("/api/empresas/usuario/:email", (req, res) -> {
            res.type("application/json; charset=utf-8");
            try {
                return gson.toJson(new EmpresaDAO().listarPorResponsavelEmail(req.params(":email")));
            } catch (Exception e) {
                res.status(500);
                return error("Erro ao listar empresas do usuario.");
            }
        });

        get("/api/empresas/:cnpj", (req, res) -> {
            res.type("application/json; charset=utf-8");
            try {
                Empresa empresa = new EmpresaDAO().get(onlyDigits(req.params(":cnpj")));
                if (empresa == null) {
                    res.status(404);
                    return error("Empresa nao encontrada.");
                }
                return gson.toJson(empresa);
            } catch (Exception e) {
                res.status(500);
                return error("Erro ao buscar empresa.");
            }
        });

        get("/api/usuarios", (req, res) -> {
            res.type("application/json; charset=utf-8");
            try {
                return gson.toJson(new UsuarioDAO().listar());
            } catch (Exception e) {
                res.status(500);
                return error("Erro ao listar usuarios.");
            }
        });

        put("/api/empresas/:cnpj/status", (req, res) -> {
            res.type("application/json; charset=utf-8");
            try {
                JsonObject body = gson.fromJson(req.body(), JsonObject.class);
                String status = jsonString(body, "status");
                if (!isValidStatus(status)) {
                    res.status(400);
                    return error("Status invalido.");
                }

                String cnpj = onlyDigits(req.params(":cnpj"));
                EmpresaDAO dao = new EmpresaDAO();
                Empresa empresa = dao.get(cnpj);
                if (empresa == null) {
                    res.status(404);
                    return error("Empresa nao encontrada.");
                }

                empresa.setStatus(status);
                dao.update(empresa);
                return message("Status atualizado.");
            } catch (Exception e) {
                res.status(500);
                return error("Erro ao atualizar status.");
            }
        });

        put("/api/empresas/:cnpj", (req, res) -> {
            res.type("application/json; charset=utf-8");
            try {
                String cnpj = onlyDigits(req.params(":cnpj"));
                Empresa empresa = gson.fromJson(req.body(), Empresa.class);
                if (empresa != null) {
                    empresa.setCnpj(cnpj);
                }

                String erro = prepararEmpresaParaSalvar(empresa, "pendente");
                if (erro != null) {
                    res.status(400);
                    return error(erro);
                }

                if (new EmpresaDAO().update(empresa)) {
                    return message("Empresa atualizada.");
                }

                res.status(404);
                return error("Empresa nao encontrada.");
            } catch (Exception e) {
                res.status(500);
                return error("Erro ao atualizar empresa.");
            }
        });

        delete("/api/empresas/:cnpj", (req, res) -> {
            res.type("application/json; charset=utf-8");
            try {
                if (new EmpresaDAO().remove(onlyDigits(req.params(":cnpj")))) {
                    return message("Empresa removida.");
                }
                res.status(404);
                return error("Empresa nao encontrada.");
            } catch (Exception e) {
                res.status(500);
                return error("Erro ao remover empresa.");
            }
        });

        get("/empresa/:cnpj/guia-postagem", (req, res) -> {
            res.type("application/json; charset=utf-8");
            try {
                return guiaService.gerarGuiaParaEmpresa(onlyDigits(req.params(":cnpj")));
            } catch (Exception e) {
                res.status(500);
                return error("Erro ao gerar guia de postagem.");
            }
        });

        post("/canais", (req, res) -> {
            res.type("application/json; charset=utf-8");
            try {
                Canal canal = gson.fromJson(req.body(), Canal.class);
                String erro = prepararCanal(canal);
                if (erro != null) {
                    res.status(400);
                    return error(erro);
                }

                long id = new CanalDAO().insert(canal);
                res.status(201);
                JsonObject out = new JsonObject();
                out.addProperty("canal_id", id);
                return gson.toJson(out);
            } catch (Exception e) {
                res.status(500);
                return error("Erro ao criar canal.");
            }
        });

        get("/canais/:cnpj", (req, res) -> {
            res.type("application/json; charset=utf-8");
            try {
                return gson.toJson(new CanalDAO().listByEmpresa(onlyDigits(req.params(":cnpj"))));
            } catch (Exception e) {
                res.status(500);
                return error("Erro ao listar canais.");
            }
        });

        post("/importacoes", (req, res) -> {
            res.type("application/json; charset=utf-8");
            try {
                Importacao importacao = gson.fromJson(req.body(), Importacao.class);
                if (importacao == null || importacao.getCanal_id() <= 0
                        || importacao.getImportacao_arquivo_original() == null
                        || importacao.getImportacao_periodo_inicio() == null
                        || importacao.getImportacao_periodo_fim() == null) {
                    res.status(400);
                    return error("Dados da importacao incompletos.");
                }

                if (!hasText(importacao.getImportacao_status())) {
                    importacao.setImportacao_status("PROCESSADA");
                }

                new ImportacaoDAO().upsert(importacao);
                res.status(201);
                return ok();
            } catch (Exception e) {
                res.status(500);
                return error("Erro ao salvar importacao.");
            }
        });

        post("/posts", (req, res) -> {
            res.type("application/json; charset=utf-8");
            try {
                Post post = gson.fromJson(req.body(), Post.class);
                if (post == null || post.getCanal_id() <= 0 || post.getData_hora() == null) {
                    res.status(400);
                    return error("Dados do post incompletos.");
                }
                if (post.getLegenda() == null) {
                    post.setLegenda("");
                }

                new PostDAO().upsert(post);
                res.status(201);
                return ok();
            } catch (Exception e) {
                res.status(500);
                return error("Erro ao salvar post.");
            }
        });

        get("/posts/:canalId", (req, res) -> {
            res.type("application/json; charset=utf-8");
            try {
                long canalId = Long.parseLong(req.params(":canalId"));
                Date start = req.queryParams("start") != null
                        ? Date.valueOf(req.queryParams("start"))
                        : new Date(System.currentTimeMillis());
                Date end = req.queryParams("end") != null
                        ? Date.valueOf(req.queryParams("end"))
                        : new Date(System.currentTimeMillis());
                return gson.toJson(new PostDAO().listByCanal(canalId, start, end));
            } catch (Exception e) {
                res.status(400);
                return error("Periodo ou canal invalido.");
            }
        });

        get("/api/avaliacoes", (req, res) -> "[]");
        get("/api/produtos", (req, res) -> "[]");
        get("/api/parceiros", (req, res) -> "[]");
    }

    private static String salvarEmpresa(Request req, Response res, String statusPadrao) {
        res.type("application/json; charset=utf-8");
        try {
            Empresa empresa = gson.fromJson(req.body(), Empresa.class);
            String erro = prepararEmpresaParaSalvar(empresa, statusPadrao);
            if (erro != null) {
                res.status(400);
                return error(erro);
            }

            if (new EmpresaDAO().insert(empresa)) {
                res.status(201);
                JsonObject out = new JsonObject();
                out.addProperty("message", "Empresa cadastrada com sucesso.");
                out.addProperty("mensagem", "Empresa cadastrada com sucesso.");
                out.addProperty("id", empresa.getCnpj());
                return gson.toJson(out);
            }

            res.status(409);
            return error("Ja existe uma empresa cadastrada com esse CNPJ.");
        } catch (Exception e) {
            res.status(500);
            return error("Erro ao cadastrar empresa.");
        }
    }

    private static String prepararUsuarioParaCadastro(Usuario usuario) {
        if (usuario == null) {
            return "Dados do usuario nao enviados.";
        }
        if (!hasText(usuario.getNome())) {
            return "Nome e obrigatorio.";
        }
        if (!isValidEmail(usuario.getEmail())) {
            return "Email invalido.";
        }
        if (usuario.getSenha() == null || usuario.getSenha().length() < 6) {
            return "A senha precisa ter pelo menos 6 caracteres.";
        }

        usuario.setNome(usuario.getNome().trim());
        usuario.setEmail(usuario.getEmail().trim().toLowerCase());
        usuario.setTipo("usuario");
        usuario.setAtivo(true);
        return null;
    }

    private static String prepararEmpresaParaSalvar(Empresa empresa, String statusPadrao) {
        if (empresa == null) {
            return "Dados da empresa nao enviados.";
        }

        String cnpj = onlyDigits(empresa.getCnpj());
        if (cnpj.length() != 14) {
            return "CNPJ deve conter 14 digitos.";
        }
        if (!hasText(empresa.getNome_fantasia())) {
            return "Nome fantasia e obrigatorio.";
        }
        if (!hasText(empresa.getRazao_social())) {
            return "Razao social e obrigatoria.";
        }

        empresa.setCnpj(cnpj);
        empresa.setNome_fantasia(empresa.getNome_fantasia().trim());
        empresa.setRazao_social(empresa.getRazao_social().trim());
        if (hasText(empresa.getResponsavel_email())) {
            empresa.setResponsavel_email(empresa.getResponsavel_email().trim().toLowerCase());
        }
        if (hasText(empresa.getEmail_contato())) {
            empresa.setEmail_contato(empresa.getEmail_contato().trim().toLowerCase());
        }
        empresa.setStatus(statusPadrao);
        return null;
    }

    private static String prepararCanal(Canal canal) {
        if (canal == null) {
            return "Dados do canal nao enviados.";
        }

        String cnpj = onlyDigits(canal.getEmpresaCnpj());
        if (cnpj.length() != 14) {
            return "CNPJ da empresa invalido.";
        }

        canal.setEmpresaCnpj(cnpj);
        if (!hasText(canal.getPlataforma())) {
            canal.setPlataforma("GENERICA");
        }
        if (!hasText(canal.getCanalIdentificador())) {
            canal.setCanalIdentificador("canal-principal");
        }
        return null;
    }

    private static String jsonString(JsonObject body, String key) {
        if (body == null || !body.has(key) || body.get(key).isJsonNull()) {
            return "";
        }
        return body.get(key).getAsString();
    }

    private static String onlyDigits(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static boolean isValidEmail(String email) {
        return hasText(email) && email.contains("@") && email.indexOf('@') > 0;
    }

    private static boolean isValidStatus(String status) {
        return "pendente".equals(status) || "aprovada".equals(status) || "rejeitada".equals(status)
                || "ATIVA".equals(status) || "INATIVA".equals(status);
    }

    private static String ok() {
        return "{\"ok\":true}";
    }

    private static String message(String message) {
        JsonObject out = new JsonObject();
        out.addProperty("message", message);
        out.addProperty("mensagem", message);
        return gson.toJson(out);
    }

    private static String error(String message) {
        JsonObject out = new JsonObject();
        out.addProperty("error", message);
        out.addProperty("mensagem", message);
        return gson.toJson(out);
    }
}
