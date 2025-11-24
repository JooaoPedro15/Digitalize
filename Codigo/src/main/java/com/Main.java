package com;

import static spark.Spark.*;

import com.service.ApiConfig;
import com.service.Routes;

/**
 * Classe responsável por inicializar o servidor Spark e configurar
 * os componentes principais da aplicação.
 *
 * Esta classe define:
 * - Pasta de arquivos estáticos (front-end)
 * - Porta utilizada pelo servidor
 * - Configurações de CORS
 * - Registro das rotas da API
 */
public class Main {

    public static void main(String[] args) {

        // Define o diretório de arquivos estáticos servidos pela aplicação.
        // O caminho é relativo a src/main/resources.
        staticFiles.location("/public/front-end/webapp/public");

        // Define a porta na qual o servidor irá rodar.
        // Usa a variável de ambiente PORT (usada em deploys),
        // e, caso não exista, usa a porta 8080 como padrão.
        port(getPort());

        // Habilita CORS permitindo comunicação entre front-end e API.
        ApiConfig.enableCors();

        // Registra todas as rotas da aplicação.
        Routes.mount();
    }

    /**
     * Obtém a porta utilizada pelo servidor.
     * Caso a variável de ambiente "PORT" não esteja definida,
     * utiliza 8080 como porta padrão.
     */
    private static int getPort() {
        String p = System.getenv("PORT");
        if (p == null || p.isBlank()) {
            return 8080;
        }
        return Integer.parseInt(p);
    }
}
