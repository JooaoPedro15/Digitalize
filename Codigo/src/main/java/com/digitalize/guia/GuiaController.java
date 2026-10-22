package com.digitalize.guia;

import com.digitalize.seguranca.UsuarioLogado;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
    public GuiaPostagem gerar(@PathVariable String cnpj, @AuthenticationPrincipal Jwt jwt) {
        return guiaService.gerar(cnpj, UsuarioLogado.de(jwt));
    }
}
