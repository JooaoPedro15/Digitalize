package com.digitalize.demo;

import com.digitalize.comum.ApiException;
import com.digitalize.seguranca.TokenService;
import com.digitalize.seguranca.TokenService.TokenEmitido;
import com.digitalize.usuario.UsuarioDtos.LoginResponse;
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
    private final TokenService tokenService;

    public DemoController(DemoSeeder demo, UsuarioService usuarioService, TokenService tokenService) {
        this.demo = demo;
        this.usuarioService = usuarioService;
        this.tokenService = tokenService;
    }

    @GetMapping("/api/demo")
    public Map<String, Boolean> status() {
        return Map.of("ativo", demo.ativo());
    }

    @PostMapping("/api/login/demo")
    public LoginResponse entrar() {
        if (!demo.ativo()) {
            throw ApiException.naoEncontrado("Modo demonstração desativado.");
        }
        UsuarioResponse usuario = usuarioService.autenticar(DemoSeeder.EMAIL, DemoSeeder.SENHA);
        TokenEmitido token = tokenService.emitir(usuario);
        return new LoginResponse(token.token(), token.expiraEm(), usuario);
    }
}
