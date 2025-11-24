package com.model;

// Usamos java.sql.Date para compatibilidade direta com JDBC e PostgreSQL.
// Evita conversões manuais entre LocalDate e Date.
import java.sql.Date;

/**
 * Representa um registro de importação de dados associado a um canal
 * (por exemplo: importação de métricas de redes sociais).
 *
 * Cada linha corresponde a uma "entrada de importação" identificada
 * pela combinação:
 *   - canal_id
 *   - importacao_arquivo_original
 *   - importacao_periodo_inicio
 *
 * Este padrão segue exatamente a chave primária composta definida
 * no SchemaMigrator para a tabela "importacao".
 */
public class Importacao {

    /**
     * Canal vinculado à importação.
     * Chave estrangeira para a tabela "canal".
     */
    private long canal_id;

    /**
     * Nome do arquivo original da importação.
     * Usado também como parte da chave primária composta.
     */
    private String importacao_arquivo_original;

    /**
     * Data do início do período importado (java.sql.Date).
     * Também faz parte da chave primária composta.
     */
    private Date importacao_periodo_inicio;

    /**
     * Data do fim do período importado.
     * Deve ser >= início (há uma constraint no banco).
     */
    private Date importacao_periodo_fim;

    /**
     * Status da importação: 'PENDENTE', 'PROCESSADA' ou 'FALHA'.
     * Definido via CHECK no banco pelo SchemaMigrator.
     */
    private String importacao_status;

    /** Construtor padrão — necessário para mapeamento JDBC e JSON. */
    public Importacao() {}

    /**
     * Construtor principal.
     * @param canal_id ID do canal relacionado
     * @param arquivo nome do arquivo original da importação
     * @param inicio data de início do período importado
     * @param fim data de fim do período importado
     * @param status estado atual da importação
     */
    public Importacao(long canal_id, String arquivo, Date inicio, Date fim, String status) {
        this.canal_id = canal_id;
        this.importacao_arquivo_original = arquivo;
        this.importacao_periodo_inicio = inicio;
        this.importacao_periodo_fim = fim;
        this.importacao_status = status;
    }

    // ------------------------------------------------------------
    // GETTERS e SETTERS
    // ------------------------------------------------------------

    public long getCanal_id() {
        return canal_id;
    }

    public void setCanal_id(long canal_id) {
        this.canal_id = canal_id;
    }

    public String getImportacao_arquivo_original() {
        return importacao_arquivo_original;
    }

    public void setImportacao_arquivo_original(String v) {
        this.importacao_arquivo_original = v;
    }

    public Date getImportacao_periodo_inicio() {
        return importacao_periodo_inicio;
    }

    public void setImportacao_periodo_inicio(Date v) {
        this.importacao_periodo_inicio = v;
    }

    public Date getImportacao_periodo_fim() {
        return importacao_periodo_fim;
    }

    public void setImportacao_periodo_fim(Date v) {
        this.importacao_periodo_fim = v;
    }

    public String getImportacao_status() {
        return importacao_status;
    }

    public void setImportacao_status(String v) {
        this.importacao_status = v;
    }
}
