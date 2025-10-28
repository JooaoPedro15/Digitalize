package service;

import dao.SchemaMigrator;
import dao.CanalDAO;
import dao.ImportacaoDAO;
import dao.PostDAO;

import model.Canal;
import model.Importacao;
import model.Post;

import com.google.gson.Gson;
import static spark.Spark.*;

import java.time.LocalDate;
import java.util.List;

public class Routes
{
    private static final Gson gson = ApiConfig.gson;

    public static void mount()
    {
        SchemaMigrator.migrate();

        get("/health", (req, res) -> "{\"status\":\"ok\"}");

        post("/canais", (req, res) ->
        {
            Canal c = gson.fromJson(req.body(), Canal.class);
            long id = new CanalDAO().insert(c);
            res.status(201);
            return "{\"canal_id\":" + id + "}";
        });

        get("/canais/:cnpj", (req, res) ->
        {
            String cnpj = req.params(":cnpj");
            return gson.toJson(new CanalDAO().listByEmpresa(cnpj));
        });

        post("/importacoes", (req, res) ->
        {
            Importacao imp = gson.fromJson(req.body(), Importacao.class);
            new ImportacaoDAO().upsert(imp);
            res.status(201);
            return "{\"ok\":true}";
        });

        post("/posts", (req, res) ->
        {
            Post p = gson.fromJson(req.body(), Post.class);
            new PostDAO().upsert(p);
            res.status(201);
            return "{\"ok\":true}";
        });

        get("/posts/:canalId", (req, res) ->
        {
            long canalId = Long.parseLong(req.params(":canalId"));
            LocalDate start = LocalDate.parse(req.queryParams("start"));
            LocalDate end = LocalDate.parse(req.queryParams("end"));
            return gson.toJson(new PostDAO().listByCanal(canalId, start, end));
        });
    }
}
