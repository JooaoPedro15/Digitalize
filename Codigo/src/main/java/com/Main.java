package com;

import static spark.Spark.*;
import com.config.EnvConfig;
import com.service.ApiConfig;
import com.service.Routes;

public class Main {
    public static void main(String[] args) {
        staticFiles.location("/front-end/webapp/public");
        port(getPort());
        ApiConfig.enableCors();
        
        // O PONTO CRÍTICO: Inicia as rotas
        Routes.mount();
        
        System.out.println("Servidor iniciado na porta " + getPort());
    }

    private static int getPort() {
        String p = EnvConfig.get("PORT");
        if (p == null || p.isBlank()) return 8080;
        return Integer.parseInt(p);
    }
}
