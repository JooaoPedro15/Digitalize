package app;

import static spark.Spark.*;
import service.EmpresaService;
import service.CanalService;
import service.RecomendacaoService;

/**
 * Classe Main
 * -----------------------------------------------------------
 * Ponto de entrada da aplicação backend.
 * 
 * É responsável por:
 *  1. Iniciar o servidor Spark Java;
 *  2. Configurar as rotas REST (endpoints HTTP);
 *  3. Definir configurações globais (porta, diretórios estáticos etc.);
 *  4. Registrar os serviços que tratam as requisições.
 */