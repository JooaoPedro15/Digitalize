package com.digitalize.demo;

import com.digitalize.comum.ApiException;
import com.digitalize.usuario.UsuarioDtos.UsuarioResponse;
import com.digitalize.usuario.UsuarioService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DemoController {

    private final DemoSeeder demo;
    private final UsuarioService usuarioService;

    public DemoController(DemoSeeder demo, UsuarioService usuarioService) {
        this.demo = demo;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/api/demo")
    public Map<String, Boolean> status() {
        return Map.of("ativo", demo.ativo());
    }

    @PostMapping("/api/login/demo")
    public UsuarioResponse entrar() {
        if (!demo.ativo()) {
            throw ApiException.naoEncontrado("Modo demonstração desativado.");
        }
        return usuarioService.autenticar(DemoSeeder.EMAIL, DemoSeeder.SENHA);
    }
}
