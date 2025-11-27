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

    /** CNPJ da empresa (14 dígitos, somente números). */
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
     * Status da empresa no fluxo de aprovação:
     *  - "pendente" → cadastrada aguardando aprovação
     *  - "aprovada" → aprovada pelo administrador
     *  - "rejeitada" → rejeitada pelo administrador
     *  - "ATIVA"/"INATIVA" → usados somente em dados legados
     */
    private String status;

    /**
     * E-mail da pessoa responsável pelo cadastro da empresa.
     * Utilizado para vincular a empresa ao usuário logado (Minha Empresa).
     */
    private String responsavel_email;

    /**
     * E-mail principal de contato da empresa.
     * Pode coincidir com o e-mail do responsável.
     */
    private String email_contato;

    /** Construtor padrão (necessário para frameworks e JSON). */
    public Empresa() {}

    /**
     * Construtor rápido – define CNPJ, nome fantasia e razão social,
     * e inicia status como "pendente".
     */
    public Empresa(String cnpj, String nome_fantasia, String razao_social) {
        this.cnpj = cnpj;
        this.nome_fantasia = nome_fantasia;
        this.razao_social = razao_social;
        this.status = "pendente";
    }

    // -------------------- GETTERS / SETTERS --------------------

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    public String getNome_fantasia() { return nome_fantasia; }
    public void setNome_fantasia(String nome_fantasia) { this.nome_fantasia = nome_fantasia; }

    public String getRazao_social() { return razao_social; }
    public void setRazao_social(String razao_social) { this.razao_social = razao_social; }

    public String getSegmento() { return segmento; }
    public void setSegmento(String segmento) { this.segmento = segmento; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getResponsavel_email() { return responsavel_email; }
    public void setResponsavel_email(String responsavel_email) { this.responsavel_email = responsavel_email; }

    public String getEmail_contato() { return email_contato; }
    public void setEmail_contato(String email_contato) { this.email_contato = email_contato; }
}
