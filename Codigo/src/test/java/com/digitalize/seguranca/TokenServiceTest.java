package com.digitalize.seguranca;

import com.digitalize.usuario.UsuarioDtos.UsuarioResponse;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import javax.crypto.SecretKey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenServiceTest {

    private final JwtConfig config = new JwtConfig();
    private final SecretKey chave = config.chaveJwt("uma-chave-de-teste-com-mais-de-32-caracteres");
    private final TokenService tokenService = new TokenService(config.jwtEncoder(chave), 8);
    private final JwtDecoder decoder = config.jwtDecoder(chave);

    @Test
    void tokenEmitidoEhValidadoECarregaOsDadosDoUsuario() {
        UsuarioResponse usuario = new UsuarioResponse(7L, "Ana", "ana@teste.com", "admin", true);

        Jwt jwt = decoder.decode(tokenService.emitir(usuario).token());
        UsuarioLogado logado = UsuarioLogado.de(jwt);

        assertThat(logado.id()).isEqualTo(7L);
        assertThat(logado.email()).isEqualTo("ana@teste.com");
        assertThat(logado.admin()).isTrue();
    }

    @Test
    void tokenAssinadoComOutraChaveEhRecusado() {
        SecretKey outraChave = config.chaveJwt("outra-chave-de-teste-com-mais-de-32-caracteres");
        String token = new TokenService(config.jwtEncoder(outraChave), 8)
                .emitir(new UsuarioResponse(1L, "X", "x@x.com", "usuario", true)).token();

        assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void chaveCurtaDemaisImpedeASubida() {
        assertThatThrownBy(() -> config.chaveJwt("curta")).isInstanceOf(IllegalStateException.class);
    }
}
