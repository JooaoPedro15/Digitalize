package com.digitalize.usuario;

import com.digitalize.comum.Mensagem;
import com.digitalize.seguranca.TokenService;
import com.digitalize.seguranca.TokenService.TokenEmitido;
import com.digitalize.seguranca.UsuarioLogado;
import com.digitalize.usuario.UsuarioDtos.CadastroRequest;
import com.digitalize.usuario.UsuarioDtos.LoginRequest;
import com.digitalize.usuario.UsuarioDtos.LoginResponse;
import com.digitalize.usuario.UsuarioDtos.UsuarioResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final TokenService tokenService;

    public UsuarioController(UsuarioService usuarioService, TokenService tokenService) {
        this.usuarioService = usuarioService;
        this.tokenService = tokenService;
    }

    @PostMapping("/api/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        UsuarioResponse usuario = usuarioService.autenticar(request.email(), request.senha());
        TokenEmitido token = tokenService.emitir(usuario);
        return new LoginResponse(token.token(), token.expiraEm(), usuario);
    }

    @GetMapping("/api/me")
    public UsuarioResponse eu(@AuthenticationPrincipal Jwt jwt) {
        return usuarioService.buscar(UsuarioLogado.de(jwt).id());
    }

    @PostMapping("/api/usuarios")
    @ResponseStatus(HttpStatus.CREATED)
    public Mensagem cadastrar(@Valid @RequestBody CadastroRequest request) {
        usuarioService.cadastrar(request);
        return Mensagem.sucesso("Usuário criado com sucesso.");
    }

    @GetMapping("/api/usuarios")
    public List<UsuarioResponse> listar() {
        return usuarioService.listar();
    }
}
