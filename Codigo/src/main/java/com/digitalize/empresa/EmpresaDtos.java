package com.digitalize.empresa;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class EmpresaDtos {

    private EmpresaDtos() {
    }

    /**
     * Dados de cadastro/edição. Aceita snake_case e camelCase porque as telas
     * atuais enviam os dois formatos.
     */
    public record EmpresaRequest(
            String cnpj,

            @JsonProperty("nome_fantasia") @JsonAlias("nomeFantasia")
            @NotBlank(message = "Nome fantasia é obrigatório.")
            @Size(max = 120, message = "Nome fantasia muito longo.")
            String nomeFantasia,

            @JsonProperty("razao_social") @JsonAlias("razaoSocial")
            @NotBlank(message = "Razão social é obrigatória.")
            @Size(max = 120, message = "Razão social muito longa.")
            String razaoSocial,

            @Size(max = 60, message = "Segmento muito longo.")
            String segmento,

            @Size(max = 200, message = "Endereço muito longo.")
            String endereco,

            @JsonProperty("responsavel_email") @JsonAlias({"responsavelEmail", "emailResponsavel"})
            String responsavelEmail,

            @JsonProperty("email_contato") @JsonAlias("emailContato")
            @Email(message = "Email de contato inválido.")
            String emailContato) {
    }

    public record StatusRequest(String status) {
    }

    public record EmpresaResponse(
            Long id,
            String cnpj,
            @JsonProperty("nome_fantasia") String nomeFantasia,
            @JsonProperty("razao_social") String razaoSocial,
            String segmento,
            String endereco,
            String status,
            @JsonProperty("responsavel_email") String responsavelEmail,
            @JsonProperty("email_contato") String emailContato) {

        public static EmpresaResponse de(Empresa empresa) {
            return new EmpresaResponse(
                    empresa.getId(),
                    empresa.getCnpj(),
                    empresa.getNomeFantasia(),
                    empresa.getRazaoSocial(),
                    empresa.getSegmento(),
                    empresa.getEndereco(),
                    empresa.getStatus().rotulo(),
                    empresa.getUsuario() != null ? empresa.getUsuario().getEmail() : null,
                    empresa.getEmailContato());
        }
    }

    /** Dados que podem aparecer na vitrine pública do site. */
    public record EmpresaPublicaResponse(
            String cnpj,
            @JsonProperty("nome_fantasia") String nomeFantasia,
            @JsonProperty("razao_social") String razaoSocial,
            String segmento,
            String endereco,
            String status,
            @JsonProperty("email_contato") String emailContato) {

        public static EmpresaPublicaResponse de(Empresa empresa) {
            return new EmpresaPublicaResponse(
                    empresa.getCnpj(),
                    empresa.getNomeFantasia(),
                    empresa.getRazaoSocial(),
                    empresa.getSegmento(),
                    empresa.getEndereco(),
                    empresa.getStatus().rotulo(),
                    empresa.getEmailContato());
        }
    }

    public record CadastroResponse(String message, String mensagem, String id) {

        public static CadastroResponse de(Empresa empresa) {
            String texto = "Empresa cadastrada com sucesso.";
            return new CadastroResponse(texto, texto, empresa.getCnpj());
        }
    }
}
