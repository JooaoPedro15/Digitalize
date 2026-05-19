package com;

import com.model.Canal;
import com.service.CanalService;

/**
 * Classe auxiliar para testes manuais da camada de servico.
 *
 * O ponto de entrada da aplicacao web e {@link Main}.
 */
public class Aplicacao {

    public static void main(String[] args) throws Exception {
        CanalService service = new CanalService();

        for (Canal canal : service.listar()) {
            System.out.println(canal);
        }
    }
}
