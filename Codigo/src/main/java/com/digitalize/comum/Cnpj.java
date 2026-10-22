package com.digitalize.comum;

public final class Cnpj {

    private Cnpj() {
    }

    /** Remove pontos, barras e traços: "12.345.678/0001-00" vira "12345678000100". */
    public static String somenteDigitos(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
    }

    public static String validar(String valor) {
        String cnpj = somenteDigitos(valor);
        if (cnpj.length() != 14) {
            throw ApiException.requisicaoInvalida("CNPJ deve conter 14 dígitos.");
        }
        return cnpj;
    }
}
