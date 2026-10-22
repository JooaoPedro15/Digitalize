package com.digitalize.comum;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Corpo padrão das respostas simples da API.
 * O front-end lê "message"/"error" e também "mensagem", por isso os dois campos.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Mensagem(String message, String error, String mensagem) {

    public static Mensagem sucesso(String texto) {
        return new Mensagem(texto, null, texto);
    }

    public static Mensagem erro(String texto) {
        return new Mensagem(null, texto, texto);
    }
}
