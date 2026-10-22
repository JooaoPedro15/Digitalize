package com.digitalize.config;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Converte a URL no formato dos provedores (postgresql://usuario:senha@host:porta/banco?params)
 * para o formato JDBC, separando usuário e senha.
 */
public record DatabaseUrl(String jdbcUrl, String usuario, String senha) {

    public static DatabaseUrl parse(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("DATABASE_URL vazia.");
        }

        String valor = url.trim();
        if (valor.startsWith("jdbc:")) {
            return new DatabaseUrl(valor, null, null);
        }
        if (!valor.matches("^postgres(ql)?://.*")) {
            throw new IllegalArgumentException("DATABASE_URL deve começar com postgresql:// ou jdbc:postgresql://");
        }

        URI uri = URI.create(valor.replaceFirst("^postgres(ql)?://", "http://"));
        String usuario = null;
        String senha = null;
        if (uri.getRawUserInfo() != null) {
            String[] partes = uri.getRawUserInfo().split(":", 2);
            usuario = decode(partes[0]);
            senha = partes.length > 1 ? decode(partes[1]) : "";
        }

        String jdbcUrl = "jdbc:postgresql://" + uri.getHost()
                + (uri.getPort() > 0 ? ":" + uri.getPort() : "")
                + uri.getRawPath()
                + (uri.getRawQuery() != null ? "?" + uri.getRawQuery() : "");

        return new DatabaseUrl(jdbcUrl, usuario, senha);
    }

    private static String decode(String valor) {
        return URLDecoder.decode(valor, StandardCharsets.UTF_8);
    }
}
