package com.digitalize.importacao;

import com.digitalize.canal.Canal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDate;

/** Registro de um arquivo CSV de métricas importado para um canal. */
@Entity
@Table(name = "importacao")
public class Importacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "canal_id", nullable = false)
    private Canal canal;

    @Column(name = "arquivo_original", nullable = false)
    private String arquivoOriginal;

    @Column(name = "periodo_inicio", nullable = false)
    private LocalDate periodoInicio;

    @Column(name = "periodo_fim", nullable = false)
    private LocalDate periodoFim;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private StatusImportacao status = StatusImportacao.PENDENTE;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    protected Importacao() {
    }

    public Importacao(Canal canal, String arquivoOriginal, LocalDate periodoInicio) {
        this.canal = canal;
        this.arquivoOriginal = arquivoOriginal;
        this.periodoInicio = periodoInicio;
    }

    public Long getId() {
        return id;
    }

    public Canal getCanal() {
        return canal;
    }

    public String getArquivoOriginal() {
        return arquivoOriginal;
    }

    public LocalDate getPeriodoInicio() {
        return periodoInicio;
    }

    public LocalDate getPeriodoFim() {
        return periodoFim;
    }

    public void setPeriodoFim(LocalDate periodoFim) {
        this.periodoFim = periodoFim;
    }

    public StatusImportacao getStatus() {
        return status;
    }

    public void setStatus(StatusImportacao status) {
        this.status = status;
    }
}
