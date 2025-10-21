package model;

/**
 * Classe Recomendacao
 * -------------------------------------------------------
 * Esta classe representa a tabela "recomendacao" do banco de dados.
 * Cada instância corresponde a uma recomendação feita para uma empresa —
 * podendo ser de tipo "POSTAGEM", "ESTRATEGIA" ou "OTIMIZACAO".
 *
 * Estrutura SQL de referência:
 *   recomendacao_id  BIGSERIAL PRIMARY KEY
 *   tipo             VARCHAR(20)
 *   detalhes         TEXT
 *   empresa_cnpj     CHAR(14) (FK para empresa)
 */

public class Recomendacao {

    // Identificador único da recomendação (gerado automaticamente)
    private long recomendacaoId;

    // Tipo da recomendação (POSTAGEM, ESTRATEGIA, OTIMIZACAO)
    private String tipo;

    // Texto com os detalhes da recomendação
    private String detalhes;

    // CNPJ da empresa para a qual a recomendação foi feita
    private String empresaCnpj;

    /**
     * Construtor vazio (necessário para frameworks e inicializações manuais).
     */
    public Recomendacao() {
    }

    /**
     * Construtor completo.
     *
     * @param recomendacaoId Identificador único da recomendação
     * @param tipo Tipo da recomendação (POSTAGEM, ESTRATEGIA, OTIMIZACAO)
     * @param detalhes Detalhes descritivos da recomendação
     * @param empresaCnpj CNPJ da empresa relacionada
     */
    public Recomendacao(long recomendacaoId, String tipo, String detalhes, String empresaCnpj) {
        this.recomendacaoId = recomendacaoId;
        this.tipo = tipo;
        this.detalhes = detalhes;
        this.empresaCnpj = empresaCnpj;
    }

    // Getters e Setters

    public long getRecomendacaoId() {
        return recomendacaoId;
    }

    public void setRecomendacaoId(long recomendacaoId) {
        this.recomendacaoId = recomendacaoId;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDetalhes() {
        return detalhes;
    }

    public void setDetalhes(String detalhes) {
        this.detalhes = detalhes;
    }

    public String getEmpresaCnpj() {
        return empresaCnpj;
    }

    public void setEmpresaCnpj(String empresaCnpj) {
        this.empresaCnpj = empresaCnpj;
    }

    /**
     * Retorna uma representação textual do objeto Recomendacao.
     * Útil para logs e depuração.
     */
    @Override
    public String toString() {
        return "Recomendacao {" +
                "recomendacaoId=" + recomendacaoId +
                ", tipo='" + tipo + '\'' +
                ", detalhes='" + detalhes + '\'' +
                ", empresaCnpj='" + empresaCnpj + '\'' +
                '}';
    }
}
