package com.digitalize.usuario;

import com.digitalize.comum.ApiException;
import com.digitalize.usuario.UsuarioDtos.CadastroRequest;
import com.digitalize.usuario.UsuarioDtos.UsuarioResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarios, PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UsuarioResponse cadastrar(CadastroRequest request) {
        String email = normalizarEmail(request.email());
        if (usuarios.existsByEmail(email)) {
            throw ApiException.conflito("Já existe um usuário cadastrado com esse email.");
        }

        Usuario usuario = new Usuario(request.nome().trim(), email, passwordEncoder.encode(request.senha()), Papel.USUARIO);
        return UsuarioResponse.de(usuarios.save(usuario));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse autenticar(String email, String senha) {
        return usuarios.findByEmail(normalizarEmail(email))
                .filter(Usuario::isAtivo)
                .filter(usuario -> passwordEncoder.matches(senha, usuario.getSenhaHash()))
                .map(UsuarioResponse::de)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Email ou senha incorretos."));
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarios.findAll().stream().map(UsuarioResponse::de).toList();
    }

    /**
     * Cria o usuário se ainda não existir, ou redefine a senha e reativa se já existir.
     * Usado para o administrador inicial e para a conta de demonstração.
     */
    @Transactional
    public Usuario garantir(String nome, String email, String senha, Papel papel) {
        String emailNormalizado = normalizarEmail(email);
        Usuario usuario = usuarios.findByEmail(emailNormalizado)
                .orElseGet(() -> new Usuario(nome, emailNormalizado, null, papel));
        usuario.setSenhaHash(passwordEncoder.encode(senha));
        usuario.setPapel(papel);
        usuario.setAtivo(true);
        return usuarios.save(usuario);
    }

    public static String normalizarEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
