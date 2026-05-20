package com.model;

import com.google.gson.annotations.SerializedName;

// Datas convertidas para java.sql.*,
// pois o DAO usa Timestamp e Date diretamente nas operações JDBC.
import java.sql.Timestamp;
import java.sql.Date;

/**
 * Representa uma postagem realizada em um canal de mídia social.
 * Contém todas as métricas coletadas durante a importação,
 * além das informações de identificação.
 *
 * Cada post é identificado pela combinação:
 * (canal_id, data_hora, legenda)
 */
public class Post {

    /** Identificador do canal ao qual o post pertence (FK para Canal). */
    private long canal_id;

    /**
     * Data e hora da postagem.
     * Usamos java.sql.Timestamp para compatibilidade direta com JDBC
     * e com operações INSERT/UPDATE no DAO.
     */
    private Timestamp data_hora;

    /** Texto da legenda do post. */
    private String legenda;

    /** Duração do vídeo (em segundos) — aplicável apenas quando houver vídeo. */
    private Integer duracao;

    /** Alcance estimado ou informado pela plataforma. */
    private Integer alcance;

    /** Quantidade total de visualizações. */
    private Integer views;

    /** Número de curtidas do post. */
    private Integer likes;

    /** Total de compartilhamentos. */
    private Integer shares;

    /** Número de comentários deixados pelos usuários. */
    private Integer comentarios;

    /** Total de salvamentos (ex: salvar no Instagram). */
    private Integer saves;

    /** Caminho ou referência ao arquivo original importado. */
    @SerializedName(value="imp_arquivo_original", alternate={"importacao_arquivo_original"})
    private String imp_arquivo_original;

    /**
     * Data de início do período referente ao arquivo de importação.
     * Uso de java.sql.Date para corresponder ao tipo DATE do banco.
     */
    @SerializedName(value="imp_periodo_inicio", alternate={"importacao_periodo_inicio"})
    private Date imp_periodo_inicio;

    /** Construtor padrão (necessário para JSON, frameworks e JDBC). */
    public Post() {}

    // -------------------- GETTERS / SETTERS --------------------

    public long getCanal_id() {
        return canal_id;
    }

    public void setCanal_id(long canal_id) {
        this.canal_id = canal_id;
    }

    public Timestamp getData_hora() {
        return data_hora;
    }

    public void setData_hora(Timestamp data_hora) {
        this.data_hora = data_hora;
    }

    public String getLegenda() {
        return legenda;
    }

    public void setLegenda(String legenda) {
        this.legenda = legenda;
    }

    public Integer getDuracao() {
        return duracao;
    }

    public void setDuracao(Integer duracao) {
        this.duracao = duracao;
    }

    public Integer getAlcance() {
        return alcance;
    }

    public void setAlcance(Integer alcance) {
        this.alcance = alcance;
    }

    public Integer getViews() {
        return views;
    }

    public void setViews(Integer views) {
        this.views = views;
    }

    public Integer getLikes() {
        return likes;
    }

    public void setLikes(Integer likes) {
        this.likes = likes;
    }

    public Integer getShares() {
        return shares;
    }

    public void setShares(Integer shares) {
        this.shares = shares;
    }

    public Integer getComentarios() {
        return comentarios;
    }

    public void setComentarios(Integer comentarios) {
        this.comentarios = comentarios;
    }

    public Integer getSaves() {
        return saves;
    }

    public void setSaves(Integer saves) {
        this.saves = saves;
    }

    public String getImp_arquivo_original() {
        return imp_arquivo_original;
    }

    public void setImp_arquivo_original(String v) {
        this.imp_arquivo_original = v;
    }

    public Date getImp_periodo_inicio() {
        return imp_periodo_inicio;
    }

    public void setImp_periodo_inicio(Date v) {
        this.imp_periodo_inicio = v;
    }
}
