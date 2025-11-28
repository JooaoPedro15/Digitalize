package com.model;

import com.google.gson.annotations.SerializedName;

public class Empresa {

    private String cnpj;

    @SerializedName(value="nome_fantasia", alternate={"nomeFantasia"})
    private String nome_fantasia;

    @SerializedName(value="razao_social", alternate={"razaoSocial"})
    private String razao_social;

    private String segmento;
    private String endereco;
    private String status;

    @SerializedName(value="responsavel_email", alternate={"responsavelEmail", "emailResponsavel"})
    private String responsavel_email;

    @SerializedName(value="email_contato", alternate={"emailContato"})
    private String email_contato;

    public Empresa() {}

    public Empresa(String cnpj, String nome_fantasia, String razao_social) {
        this.cnpj = cnpj;
        this.nome_fantasia = nome_fantasia;
        this.razao_social = razao_social;
        this.status = "pendente";
    }

    // Getters e Setters
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