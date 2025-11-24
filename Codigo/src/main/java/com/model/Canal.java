package com.model;

/**
 * Representa um canal de mídia social pertencente a uma empresa.
 * Cada canal está vinculado a uma empresa através do CNPJ,
 * e possui um identificador único por plataforma.
 */
public class Canal {

    /** ID gerado automaticamente pelo banco (BIGSERIAL). */
    private long canalId;

    /** CNPJ da empresa proprietária do canal (FK para empresa). */
    private String empresaCnpj;

    /** Plataforma onde o canal existe (ex: Instagram, Facebook, TikTok). */
    private String plataforma;

    /** Identificador do canal na plataforma (ex: @empresa_oficial). */
    private String canalIdentificador;

    /** Construtor vazio necessário para frameworks e serialização. */
    public Canal() {}

    /**
     * Construtor completo para criação de objetos manualmente.
     */
    public Canal(long canalId, String empresaCnpj, String plataforma, String canalIdentificador) {
        this.canalId = canalId;
        this.empresaCnpj = empresaCnpj;
        this.plataforma = plataforma;
        this.canalIdentificador = canalIdentificador;
    }

    public long getCanalId() {
        return canalId;
    }

    public void setCanalId(long canalId) {
        this.canalId = canalId;
    }

    public String getEmpresaCnpj() {
        return empresaCnpj;
    }

    public void setEmpresaCnpj(String empresaCnpj) {
        this.empresaCnpj = empresaCnpj;
    }

    public String getPlataforma() {
        return plataforma;
    }

    public void setPlataforma(String plataforma) {
        this.plataforma = plataforma;
    }

    public String getCanalIdentificador() {
        return canalIdentificador;
    }

    public void setCanalIdentificador(String canalIdentificador) {
        this.canalIdentificador = canalIdentificador;
    }
}
