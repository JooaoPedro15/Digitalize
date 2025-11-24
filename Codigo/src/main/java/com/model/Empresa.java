package com.model;

/**
 * Representa uma empresa cadastrada no sistema.
 * Cada empresa é identificada unicamente pelo seu CNPJ,
 * seguindo o mesmo padrão definido na tabela "empresa" do banco.
 *
 * Esta entidade é usada pelos serviços e controllers
 * para consultas, cadastros e vinculação com canais de mídia social.
 */
public class Empresa {

    /**
     * CNPJ da empresa (14 dígitos, somente números).
     * É a chave primária da tabela e identifica de forma única a empresa.
     */
    private String cnpj;

    /** Nome fantasia da empresa — usado para exibição e organização. */
    private String nome_fantasia;

    /** Razão social da empresa — nome jurídico oficial. */
    private String razao_social;

    /** Segmento de mercado ou área de atuação. */
    private String segmento;

    /** Endereço cadastrado da empresa. */
    private String endereco;

    /**
     * Status da empresa.
     * Pode ser 'ATIVA' ou 'INATIVA', conforme definido no banco.
     */
    private String status;

    /** Construtor padrão — necessário para JSON, frameworks e JDBC. */
    public Empresa() {}

    /**
     * Construtor usado para criar rapidamente uma instância mínima da empresa.
     * Também define o status inicial como 'ATIVA', seguindo o padrão do banco.
     */
    public Empresa(String cnpj, String nome_fantasia, String razao_social) {
        this.cnpj = cnpj;
        this.nome_fantasia = nome_fantasia;
        this.razao_social = razao_social;
        this.status = "ATIVA";
    }

    // -------------------- GETTERS / SETTERS --------------------

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getNome_fantasia() {
        return nome_fantasia;
    }

    public void setNome_fantasia(String nome_fantasia) {
        this.nome_fantasia = nome_fantasia;
    }

    public String getRazao_social() {
        return razao_social;
    }

    public void setRazao_social(String razao_social) {
        this.razao_social = razao_social;
    }

    public String getSegmento() {
        return segmento;
    }

    public void setSegmento(String segmento) {
        this.segmento = segmento;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
