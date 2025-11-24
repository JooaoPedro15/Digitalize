package com.service;

import com.dao.SchemaMigrator;
import com.dao.CanalDAO;
import com.dao.ImportacaoDAO;
import com.dao.PostDAO;

import com.model.Canal;
import com.model.Importacao;
import com.model.Post;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import static spark.Spark.*;

import java.sql.Date;
import java.util.List;

/**
 * Classe responsável por registrar e montar todas as rotas da API.
 * 
 * Funcionalidades expostas:
 * - /health: Verificação de saúde da API
 * - /canais: CRUD básico de canais
 * - /importacoes: Inserção/atualização de importações
 * - /posts: Inserção/atualização de posts e listagem por canal e período
 */
public class Routes {

    // Gson compartilhado para serialização e desserialização JSON
    public static final Gson gson = ApiConfig.gson;

    /**
     * Monta todas as rotas da aplicação.
     * Executa a migração inicial do schema do banco.
     */
    public static void mount() {

        // Executa migração do banco (criação de tabelas se não existirem)
        System.out.println("Migrating database...");
        SchemaMigrator.migrate();

        // Rota de verificação da saúde da API
        get("/health", (req, res) -> "{\"status\":\"ok\"}");

        /**
         * Criação de um canal
         * POST /canais
         * Corpo esperado: JSON representando um Canal
         * Retorna o ID do canal criado
         */
        post("/canais", (req, res) -> {
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
            String cnpj = req.params(":cnpj");
            return gson.toJson(new CanalDAO().listByEmpresa(cnpj));
        });

        /**
         * Inserção/atualização de importações
         * POST /importacoes
         * Corpo esperado: JSON representando uma Importacao
         */
        post("/importacoes", (req, res) -> {
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
            long canalId = Long.parseLong(req.params(":canalId"));

            // Recupera parâmetros de data da query string
            String startParam = req.queryParams("start");
            String endParam = req.queryParams("end");

            // Converte strings para java.sql.Date (formato "yyyy-MM-dd")
            Date start = Date.valueOf(startParam);
            Date end = Date.valueOf(endParam);

            return gson.toJson(new PostDAO().listByCanal(canalId, start, end));
        });
    }
}
