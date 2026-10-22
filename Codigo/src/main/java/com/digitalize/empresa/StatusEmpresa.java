package com.digitalize.empresa;

import java.util.Arrays;
import java.util.Optional;

public enum StatusEmpresa {
    PENDENTE,
    APROVADA,
    REJEITADA;

    /** Valor como o front-end usa: em minúsculas. */
    public String rotulo() {
        return name().toLowerCase();
    }

    public static Optional<StatusEmpresa> de(String valor) {
        if (valor == null) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(status -> status.name().equalsIgnoreCase(valor.trim()))
                .findFirst();
    }
}
