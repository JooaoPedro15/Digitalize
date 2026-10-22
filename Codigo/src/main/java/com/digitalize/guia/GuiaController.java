package com.digitalize.guia;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GuiaController {

    private final GuiaService guiaService;

    public GuiaController(GuiaService guiaService) {
        this.guiaService = guiaService;
    }

    @GetMapping("/empresa/{cnpj}/guia-postagem")
    public GuiaPostagem gerar(@PathVariable String cnpj) {
        return guiaService.gerar(cnpj);
    }
}
