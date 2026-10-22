package com.digitalize.post;

import com.digitalize.canal.Canal;
import com.digitalize.importacao.Importacao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** Uma publicação de um canal, com as métricas vindas do CSV. */
@Entity
@Table(name = "post")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "canal_id", nullable = false)
    private Canal canal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "importacao_id")
    private Importacao importacao;

    @Column(name = "publicado_em", nullable = false)
    private LocalDateTime publicadoEm;

    @Column(nullable = false, columnDefinition = "text")
    private String legenda = "";

    @Column(name = "duracao_segundos", nullable = false)
    private int duracaoSegundos;

    @Column(nullable = false)
    private int alcance;

    @Column(nullable = false)
    private int visualizacoes;

    @Column(nullable = false)
    private int curtidas;

    @Column(nullable = false)
    private int compartilhamentos;

    @Column(nullable = false)
    private int comentarios;

    @Column(nullable = false)
    private int salvamentos;

    protected Post() {
    }

    public Post(Canal canal, LocalDateTime publicadoEm, String legenda) {
        this.canal = canal;
        this.publicadoEm = publicadoEm;
        this.legenda = legenda;
    }

    /** Taxa de engajamento por mil visualizações: (curtidas + comentários + compartilhamentos) / views * 1000. */
    public int engajamentoPorMil() {
        if (visualizacoes <= 0) {
            return 0;
        }
        return (int) ((curtidas + comentarios + compartilhamentos) * 1000L / visualizacoes);
    }

    public void atualizarMetricas(int duracaoSegundos, int alcance, int visualizacoes, int curtidas,
                                  int compartilhamentos, int comentarios, int salvamentos) {
        this.duracaoSegundos = duracaoSegundos;
        this.alcance = alcance;
        this.visualizacoes = visualizacoes;
        this.curtidas = curtidas;
        this.compartilhamentos = compartilhamentos;
        this.comentarios = comentarios;
        this.salvamentos = salvamentos;
    }

    public Long getId() {
        return id;
    }

    public Canal getCanal() {
        return canal;
    }

    public Importacao getImportacao() {
        return importacao;
    }

    public void setImportacao(Importacao importacao) {
        this.importacao = importacao;
    }

    public LocalDateTime getPublicadoEm() {
        return publicadoEm;
    }

    public String getLegenda() {
        return legenda;
    }

    public int getDuracaoSegundos() {
        return duracaoSegundos;
    }

    public int getAlcance() {
        return alcance;
    }

    public int getVisualizacoes() {
        return visualizacoes;
    }

    public int getCurtidas() {
        return curtidas;
    }

    public int getCompartilhamentos() {
        return compartilhamentos;
    }

    public int getComentarios() {
        return comentarios;
    }

    public int getSalvamentos() {
        return salvamentos;
    }
}
