package com.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import static spark.Spark.*;
import java.sql.Timestamp;
import java.sql.Date;

/**
 * Classe de configuração geral da API.
 * 
 * Responsabilidades:
 * 1. Configurar o Gson para serialização/deserialização de objetos JSON.
 * 2. Configurar CORS (Cross-Origin Resource Sharing) para permitir chamadas de outras origens.
 */
public class ApiConfig {

    /**
     * Instância Gson configurada com adaptadores para:
     * - java.sql.Date (formato "yyyy-MM-dd")
     * - java.sql.Timestamp (formato "yyyy-MM-dd HH:mm:ss")
     * 
     * Isto garante que datas e timestamps sejam interpretados corretamente ao receber JSON.
     */
    public static final Gson gson = new GsonBuilder()
        // Adaptador para java.sql.Date (JSON esperado: "yyyy-MM-dd")
        .registerTypeAdapter(Date.class, (com.google.gson.JsonDeserializer<Date>) 
            (json, typeOfT, context) -> Date.valueOf(json.getAsString()))
        
        // Adaptador para java.sql.Timestamp (JSON esperado: "yyyy-MM-ddTHH:mm:ss")
        // O 'T' é convertido em espaço para compatibilidade com Timestamp.valueOf
        .registerTypeAdapter(Timestamp.class, (com.google.gson.JsonDeserializer<Timestamp>) 
            (json, typeOfT, context) -> Timestamp.valueOf(json.getAsString().replace("T", " ")))
        
        .create();

    /**
     * Habilita o CORS globalmente.
     * 
     * 1. OPTIONS: responde pré-requisições CORS com os headers e métodos permitidos.
     * 2. BEFORE: adiciona headers CORS e força o tipo de resposta para JSON.
     */
    public static void enableCors() {
        // Responde requisições OPTIONS (pré-flight) de CORS
        options("/*", (request, response) -> {
            String reqHeaders = request.headers("Access-Control-Request-Headers");
            if (reqHeaders != null) {
                response.header("Access-Control-Allow-Headers", reqHeaders);
            }

            String reqMethod = request.headers("Access-Control-Request-Method");
            if (reqMethod != null) {
                response.header("Access-Control-Allow-Methods", reqMethod);
            }

            return "OK";
        });

        // Adiciona cabeçalhos CORS antes de cada requisição
        before((req, res) -> {
            res.header("Access-Control-Allow-Origin", "*"); // Permite qualquer origem
            res.type("application/json");                  // Resposta sempre JSON
        });
    }
}
