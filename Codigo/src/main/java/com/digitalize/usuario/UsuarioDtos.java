package com.digitalize.usuario;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public final class UsuarioDtos {

    private UsuarioDtos() {
    }

    public record CadastroRequest(
            @NotBlank(message = "Nome é obrigatório.")
            @Size(max = 120, message = "Nome muito longo.")
            String nome,

            @NotBlank(message = "Email é obrigatório.")
            @Email(message = "Email inválido.")
            String email,

            @NotBlank(message = "Senha é obrigatória.")
            @Size(min = 6, max = 72, message = "A senha precisa ter entre 6 e 72 caracteres.")
            String senha) {
    }

    public record LoginRequest(
            @NotBlank(message = "Informe email e senha.") String email,
            @NotBlank(message = "Informe email e senha.") String senha) {
    }

    /** Resposta do login: o token vai no cabeçalho Authorization das próximas requisições. */
    public record LoginResponse(
            String token,
            @JsonProperty("expira_em") Instant expiraEm,
            UsuarioResponse usuario) {
    }

    /**
     * Usuário como o front-end conhece: "tipo" em minúsculas ("usuario" ou "admin").
     */
    public record UsuarioResponse(Long id, String nome, String email, String tipo, boolean ativo) {

        public static UsuarioResponse de(Usuario usuario) {
            return new UsuarioResponse(
                    usuario.getId(),
                    usuario.getNome(),
                    usuario.getEmail(),
                    usuario.getPapel().name().toLowerCase(),
                    usuario.isAtivo());
        }
    }
}
