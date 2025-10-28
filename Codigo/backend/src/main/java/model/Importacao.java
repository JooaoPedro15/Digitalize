package model;

import java.time.LocalDate;

public class Importacao
{
    private long canal_id;
    private String importacao_arquivo_original;
    private LocalDate importacao_periodo_inicio;
    private LocalDate importacao_periodo_fim;
    private String importacao_status;

    public Importacao() {}

    public Importacao(long canal_id, String arquivo, LocalDate inicio, LocalDate fim, String status)
    {
        this.canal_id = canal_id;
        this.importacao_arquivo_original = arquivo;
        this.importacao_periodo_inicio = inicio;
        this.importacao_periodo_fim = fim;
        this.importacao_status = status;
    }

    public long getCanal_id() { return canal_id; }
    public void setCanal_id(long canal_id) { this.canal_id = canal_id; }

    public String getImportacao_arquivo_original() { return importacao_arquivo_original; }
    public void setImportacao_arquivo_original(String v) { this.importacao_arquivo_original = v; }

    public LocalDate getImportacao_periodo_inicio() { return importacao_periodo_inicio; }
    public void setImportacao_periodo_inicio(LocalDate v) { this.importacao_periodo_inicio = v; }

    public LocalDate getImportacao_periodo_fim() { return importacao_periodo_fim; }
    public void setImportacao_periodo_fim(LocalDate v) { this.importacao_periodo_fim = v; }

    public String getImportacao_status() { return importacao_status; }
    public void setImportacao_status(String v) { this.importacao_status = v; }
}
