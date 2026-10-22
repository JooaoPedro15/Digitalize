package com.digitalize.seguranca;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/**
 * Os tokens são assinados com HMAC-SHA256 usando a chave da variável JWT_SECRET.
 * O mesmo backend emite (no login) e valida (em cada requisição) os tokens.
 */
@Configuration
public class JwtConfig {

    static final String EMISSOR = "digitalize";
    private static final int TAMANHO_MINIMO_CHAVE = 32;
    private static final Logger log = LoggerFactory.getLogger(JwtConfig.class);

    @Bean
    SecretKey chaveJwt(@Value("${digitalize.jwt.segredo}") String segredo) {
        byte[] bytes;
        if (segredo.isBlank()) {
            // Útil para rodar localmente sem configurar nada. Os tokens deixam de valer ao reiniciar.
            bytes = new byte[TAMANHO_MINIMO_CHAVE];
            new SecureRandom().nextBytes(bytes);
            log.warn("JWT_SECRET não definido: usando uma chave temporária gerada nesta execução.");
        } else {
            bytes = segredo.getBytes(StandardCharsets.UTF_8);
            if (bytes.length < TAMANHO_MINIMO_CHAVE) {
                throw new IllegalStateException("JWT_SECRET precisa ter pelo menos " + TAMANHO_MINIMO_CHAVE + " caracteres.");
            }
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey chaveJwt) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(chaveJwt));
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey chaveJwt) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(chaveJwt)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        // Confere assinatura, validade (exp) e se o token foi emitido por este backend
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(EMISSOR));
        return decoder;
    }
}
