package com.digitalize.usuario;

import com.digitalize.comum.Mensagem;
import com.digitalize.usuario.UsuarioDtos.CadastroRequest;
import com.digitalize.usuario.UsuarioDtos.LoginRequest;
import com.digitalize.usuario.UsuarioDtos.UsuarioResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/api/login")
    public UsuarioResponse login(@Valid @RequestBody LoginRequest request) {
        return usuarioService.autenticar(request.email(), request.senha());
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
