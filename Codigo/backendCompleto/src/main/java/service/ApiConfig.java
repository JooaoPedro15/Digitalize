package service;

import com.google.gson.Gson;
import static spark.Spark.*;

public class ApiConfig
{
    public static final Gson gson = new Gson();

    public static void enableCors()
    {
        options("/*", (request, response) ->
        {
            String reqHeaders = request.headers("Access-Control-Request-Headers");
            if (reqHeaders != null)
            {
                response.header("Access-Control-Allow-Headers", reqHeaders);
            }

            String reqMethod = request.headers("Access-Control-Request-Method");
            if (reqMethod != null)
            {
                response.header("Access-Control-Allow-Methods", reqMethod);
            }
            return "OK";
        });

        before((req, res) ->
        {
            res.header("Access-Control-Allow-Origin", "*");
            res.type("application/json");
        });
    }
}
