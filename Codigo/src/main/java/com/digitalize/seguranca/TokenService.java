package com.digitalize.seguranca;

import com.digitalize.usuario.UsuarioDtos.UsuarioResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class TokenService {

    private final JwtEncoder encoder;
    private final Duration validade;

    public TokenService(JwtEncoder encoder, @Value("${digitalize.jwt.validade-horas}") long validadeHoras) {
        this.encoder = encoder;
        this.validade = Duration.ofHours(validadeHoras);
    }

    public record TokenEmitido(String token, Instant expiraEm) {
    }

    /**
     * Gera o token do usuário. O "sub" é o id; "papel" vira a autoridade ROLE_USUARIO ou ROLE_ADMIN.
     */
    public TokenEmitido emitir(UsuarioResponse usuario) {
        Instant agora = Instant.now();
        Instant expiraEm = agora.plus(validade);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(JwtConfig.EMISSOR)
                .issuedAt(agora)
                .expiresAt(expiraEm)
                .subject(usuario.id().toString())
                .claim("email", usuario.email())
                .claim("papel", usuario.tipo().toUpperCase())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new TokenEmitido(token, expiraEm);
    }
}
