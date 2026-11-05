package com.digitalize.seguranca;

import com.digitalize.comum.ApiException;
import com.digitalize.empresa.Empresa;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Quem fez a requisição, lido do token JWT já validado pelo Spring Security.
 */
public record UsuarioLogado(Long id, String email, boolean admin) {

    /** Conta pública de demonstração: pode importar posts, mas não altera cadastros. */
    public static final String EMAIL_DEMONSTRACAO = "demo@digitalize.app";

    public static UsuarioLogado de(Jwt jwt) {
        return new UsuarioLogado(
                Long.valueOf(jwt.getSubject()),
                jwt.getClaimAsString("email"),
                "ADMIN".equals(jwt.getClaimAsString("papel")));
    }

    /** Admin acessa tudo; os demais, só as empresas das quais são responsáveis. */
    public boolean podeAcessar(Empresa empresa) {
        return admin || (empresa.getUsuario() != null && id.equals(empresa.getUsuario().getId()));
    }

    public boolean demonstracao() {
        return EMAIL_DEMONSTRACAO.equals(email);
    }

    public void recusarSeDemonstracao() {
        if (demonstracao()) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "A conta de demonstração não pode alterar cadastros. Crie uma conta para cadastrar o seu negócio.");
        }
    }

    public void exigirAcesso(Empresa empresa) {
        if (!podeAcessar(empresa)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Você não tem acesso a esta empresa.");
        }
    }
}
