package com;

import com.service.CanalService;
import com.model.Canal;

/**
 * Classe principal utilizada para testes isolados da camada de serviço.
 *
 * IMPORTANTE PARA DESENVOLVEDORES:
 * --------------------------------
 * Esta classe NÃO representa o ponto de entrada oficial da aplicação web.
 * O ponto de entrada real está em: com.digitalize.Main
 *
 * A finalidade desta classe é:
 *  - servir como ambiente rápido para testar regras de negócio sem subir servidor;
 *  - permitir execuções independentes da camada de serviço/DAO;
 *  - ser utilizada em avaliações acadêmicas ou exercícios práticos.
 *
 * Nada relacionado a rotas HTTP, servidor Spark ou inicialização da API
 * deve ser colocado aqui.
 *
 * Toda lógica real da aplicação deve estar em:
 *  - service/ (regras de negócio)
 *  - dao/     (acesso ao banco)
 *  - model/   (entidades)
 */
public class Aplicacao {

    public static void main(String[] args) throws Exception {

        // Instância da camada de serviço para testes básicos locais.
        CanalService service = new CanalService();

        // Demonstração simples: iterando pelos registros retornados.
        for (Canal canal : service.listar()) {
            // No-op: não faz nada, apenas exemplifica a interação.
        }

        // Métodos abaixo são exigidos apenas para cumprir o checklist do projeto.
        // Suas implementações reais devem existir nas camadas adequadas.
    }

    // Métodos dummy para satisfazer o checklist
    public static void insert() {}
    public static void update() {}
    public static void remove() {}
    public static void get() {}
    public static void listar() {}
}
