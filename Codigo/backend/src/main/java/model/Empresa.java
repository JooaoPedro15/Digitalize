package model;

public class Empresa
{
    private String cnpj;
    private String nome_fantasia;
    private String razao_social;
    private String segmento;
    private String endereco;
    private String status;

    public Empresa() {}

    public Empresa(String cnpj, String nome_fantasia, String razao_social)
    {
        this.cnpj = cnpj;
        this.nome_fantasia = nome_fantasia;
        this.razao_social = razao_social;
        this.status = "ATIVA";
    }

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
}
