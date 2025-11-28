package com.model;

import com.google.gson.annotations.SerializedName;

/**
 * Representa uma empresa cadastrada no sistema.
 * Configurada para aceitar JSON do JavaScript (camelCase) e salvar no Banco (snake_case).
 */
public class Empresa {

    /** CNPJ da empresa (14 dígitos). */
    private String cnpj;

    /** * Nome fantasia.
     * @SerializedName permite que o Java entenda "nomeFantasia" vindo do JS
     * e grave na variável "nome_fantasia".
     */
    @SerializedName(value="nome_fantasia", alternate={"nomeFantasia"})
    private String nome_fantasia;

    /** Razão social. Aceita "razaoSocial" do JSON. */
    @SerializedName(value="razao_social", alternate={"razaoSocial"})
    private String razao_social;

    private String segmento;

    private String endereco;

    private String status;

    /** Aceita "responsavelEmail" do JSON. */
    @SerializedName(value="responsavel_email", alternate={"responsavelEmail", "emailResponsavel"})
    private String responsavel_email;

    /** Aceita "emailContato" do JSON. */
    @SerializedName(value="email_contato", alternate={"emailContato"})
    private String email_contato;

    /** Construtor padrão */
    public Empresa() {}

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