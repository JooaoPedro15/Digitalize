package model;

public class Canal
{
    private long canal_id;
    private String empresa_cnpj;
    private String plataforma;
    private String canal_identificador;

    public Canal() {}

    public Canal(long canal_id, String empresa_cnpj, String plataforma, String canal_identificador)
    {
        this.canal_id = canal_id;
        this.empresa_cnpj = empresa_cnpj;
        this.plataforma = plataforma;
        this.canal_identificador = canal_identificador;
    }

    public long getCanal_id() { return canal_id; }
    public void setCanal_id(long canal_id) { this.canal_id = canal_id; }

    public String getEmpresa_cnpj() { return empresa_cnpj; }
    public void setEmpresa_cnpj(String empresa_cnpj) { this.empresa_cnpj = empresa_cnpj; }

    public String getPlataforma() { return plataforma; }
    public void setPlataforma(String plataforma) { this.plataforma = plataforma; }

    public String getCanal_identificador() { return canal_identificador; }
    public void setCanal_identificador(String canal_identificador) { this.canal_identificador = canal_identificador; }
}
