package model;

import java.time.LocalDateTime;
import java.time.LocalDate;

public class Post
{
    private long canal_id;
    private LocalDateTime data_hora;
    private String legenda;
    private Integer duracao;
    private Integer alcance;
    private Integer views;
    private Integer likes;
    private Integer shares;
    private Integer comentarios;
    private Integer saves;
    private String imp_arquivo_original;
    private LocalDate imp_periodo_inicio;

    public Post() {}

    public long getCanal_id() { return canal_id; }
    public void setCanal_id(long canal_id) { this.canal_id = canal_id; }

    public LocalDateTime getData_hora() { return data_hora; }
    public void setData_hora(LocalDateTime data_hora) { this.data_hora = data_hora; }

    public String getLegenda() { return legenda; }
    public void setLegenda(String legenda) { this.legenda = legenda; }

    public Integer getDuracao() { return duracao; }
    public void setDuracao(Integer duracao) { this.duracao = duracao; }

    public Integer getAlcance() { return alcance; }
    public void setAlcance(Integer alcance) { this.alcance = alcance; }

    public Integer getViews() { return views; }
    public void setViews(Integer views) { this.views = views; }

    public Integer getLikes() { return likes; }
    public void setLikes(Integer likes) { this.likes = likes; }

    public Integer getShares() { return shares; }
    public void setShares(Integer shares) { this.shares = shares; }

    public Integer getComentarios() { return comentarios; }
    public void setComentarios(Integer comentarios) { this.comentarios = comentarios; }

    public Integer getSaves() { return saves; }
    public void setSaves(Integer saves) { this.saves = saves; }

    public String getImp_arquivo_original() { return imp_arquivo_original; }
    public void setImp_arquivo_original(String v) { this.imp_arquivo_original = v; }

    public LocalDate getImp_periodo_inicio() { return imp_periodo_inicio; }
    public void setImp_periodo_inicio(LocalDate v) { this.imp_periodo_inicio = v; }
}
