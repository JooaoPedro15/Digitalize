package com.digitalize.usuario;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Cria o administrador inicial na subida da aplicação, se
 * DIGITALIZE_ADMIN_EMAIL e DIGITALIZE_ADMIN_PASSWORD estiverem definidos.
 */
@Component
public class AdminInicial implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInicial.class);

    private final UsuarioService usuarioService;
    private final String email;
    private final String senha;

    public AdminInicial(UsuarioService usuarioService,
                        @Value("${digitalize.admin.email}") String email,
                        @Value("${digitalize.admin.senha}") String senha) {
        this.usuarioService = usuarioService;
        this.email = email;
        this.senha = senha;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (email.isBlank() || senha.isBlank()) {
            return;
        }
        usuarioService.garantir("Admin", email, senha, Papel.ADMIN);
        log.info("Administrador inicial pronto: {}", UsuarioService.normalizarEmail(email));
    }
}
